package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingScheduleCommand;
import com.pagely.meetingservice.meeting.application.dto.command.UpdateScheduleStatusCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingScheduleResult;
import com.pagely.meetingservice.meeting.application.port.BookProvider;
import com.pagely.meetingservice.meeting.application.port.EventPublisher;
import com.pagely.meetingservice.meeting.domain.event.MeetingAttendanceStatusChangedEvent;
import com.pagely.meetingservice.meeting.domain.event.MeetingScheduleCreatedEvent;
import com.pagely.meetingservice.meeting.domain.event.MeetingScheduleStatusChangedEvent;
import com.pagely.meetingservice.meeting.domain.exception.MeetingErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingMemberErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingScheduleErrorCode;
import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleStatus;
import com.pagely.meetingservice.meeting.domain.repository.MeetingAttendanceRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingScheduleRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeetingScheduleCommandService {

    private final MeetingRepository meetingRepository;
    private final MeetingScheduleRepository meetingScheduleRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final MeetingAttendanceRepository meetingAttendanceRepository;
    private final EventPublisher eventPublisher;
    private final BookProvider bookProvider;
    private final MissingReportPenaltyService missingReportPenaltyService;

    // 모임 일정 생성
    @Transactional
    public MeetingScheduleResult createSchedule(CreateMeetingScheduleCommand command) {
        // 일정을 생성할 모임이 실제 존재하는지 확인
        Meeting meeting = meetingRepository.findByIdForUpdate(command.meetingId())
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        // 일회성 모임은 모임 생성 시 일정이 자동 생성되므로 추가 일정 생성을 막는다.
        if (meeting.isOneTime()) {
            throw new BusinessException(
                    MeetingScheduleErrorCode.ONE_TIME_MEETING_CANNOT_CREATE_ADDITIONAL_SCHEDULE
            );
        }

        // 요청자가 해당 모임의 멤버인지 확인
        MeetingMember requester = meetingMemberRepository.findByMeetingIdAndUserId(
                        command.meetingId(),
                        command.requesterId()
                )
                .orElseThrow(() -> new BusinessException(MeetingMemberErrorCode.ONLY_MEETING_MEMBER_ALLOWED));

        // 일정 생성은 ACTIVE 상태의 모임장만 가능
        if (!requester.isActive() || !requester.isHost()) {
            throw new BusinessException(MeetingScheduleErrorCode.ONLY_HOST_CAN_CREATE_SCHEDULE);
        }

        // 정기 모임 일정 생성 전에 bookId 유효성을 Book Service 내부 API로 검증한다.
        bookProvider.validateBook(command.bookId());

        // 기존 일정 중 가장 큰 회차 번호를 기준으로 다음 회차 번호 계산
        int nextScheduleNumber = calculateNextScheduleNumber(command.meetingId());

        // 일정 엔티티 생성
        MeetingSchedule schedule = MeetingSchedule.create(
                UUID.randomUUID(),
                command.meetingId(),
                nextScheduleNumber,
                command.bookId(),
                command.startAt(),
                null,
                LocalDateTime.now(),
                command.requesterId()
        );

        // 생성된 일정 저장
        MeetingSchedule savedSchedule = meetingScheduleRepository.save(schedule);

        // 일정 생성 이벤트 발행
        eventPublisher.publish(MeetingScheduleCreatedEvent.of(savedSchedule));

        return MeetingScheduleResult.from(savedSchedule);
    }

    // 모임 일정 상태 변경
    @Transactional
    public MeetingScheduleResult changeScheduleStatus(UpdateScheduleStatusCommand command) {
        // 요청한 모임이 실제 존재하는지 확인
        Meeting meeting = meetingRepository.findByIdForUpdate(command.meetingId())
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        // 상태 변경 중복을 막기 위해 일정도 쓰기 락으로 조회한다.
        MeetingSchedule schedule = meetingScheduleRepository.findByIdForUpdate(command.scheduleId())
                .orElseThrow(() -> new BusinessException(MeetingScheduleErrorCode.MEETING_SCHEDULE_NOT_FOUND));

        // 일정이 요청한 모임에 속한 일정인지 검증
        if (!schedule.getMeetingId().equals(command.meetingId())) {
            throw new BusinessException(MeetingScheduleErrorCode.SCHEDULE_NOT_FOR_MEETING);
        }

        // 이미 ONGOING인 일정을 다시 ONGOING으로 변경하는 것은 허용하지 않는다.
        if (schedule.getStatus() == MeetingScheduleStatus.ONGOING
                && command.status() == MeetingScheduleStatus.ONGOING) {
            throw new BusinessException(MeetingScheduleErrorCode.INVALID_SCHEDULE_STATUS_CHANGE);
        }

        // 상태 변경 요청자가 해당 모임의 멤버인지 확인
        MeetingMember updater = meetingMemberRepository.findByMeetingIdAndUserId(
                        command.meetingId(),
                        command.updatedBy()
                )
                .orElseThrow(() -> new BusinessException(MeetingMemberErrorCode.ONLY_MEETING_MEMBER_ALLOWED));

        // 일정 상태 변경은 ACTIVE 상태의 모임장만 가능
        if (!updater.isActive() || !updater.isHost()) {
            throw new BusinessException(MeetingScheduleErrorCode.ONLY_HOST_CAN_CHANGE_SCHEDULE_STATUS);
        }

        // 일정 종료 시 자동 결석 처리된 참석자 목록
        List<MeetingAttendance> autoAbsentAttendances = List.of();

        if (command.status() == MeetingScheduleStatus.FINISHED) {
            // 해당 일정의 전체 출석부를 조회한다.
            List<MeetingAttendance> attendances =
                    meetingAttendanceRepository.findByScheduleId(command.scheduleId());

            // 일정 종료 처리와 함께 PENDING 참석자를 ABSENT로 자동 변경한다.
            autoAbsentAttendances = schedule.finish(attendances, command.updatedBy());
        } else {
            // SCHEDULED -> ONGOING
            // SCHEDULED -> CANCELLED
            // ONGOING -> FINISHED 외의 상태 변경은 도메인에서 차단한다.
            schedule.changeStatus(command.status(), command.updatedBy());
        }

        // 일정이 진행 중이 되면 독후감 미작성 경고를 처리하고 모임 상태도 진행 중으로 변경한다.
        if (schedule.getStatus() == MeetingScheduleStatus.ONGOING) {
            // 일정 시작 시점에 독후감 미작성자에게 경고를 누적한다.
            missingReportPenaltyService.applyMissingReportWarnings(
                    schedule.getMeetingId(),
                    schedule.getId()
            );

            // 일정이 시작되면 모임도 진행 중으로 변경한다.
            meeting.start(command.updatedBy());
        }

        // 일회성 모임의 일정이 종료되면 모임 상태도 COMPLETED로 함께 변경한다.
        if (meeting.isOneTime() && schedule.getStatus() == MeetingScheduleStatus.FINISHED) {
            meeting.finish(command.updatedBy());
        }

        // 일정 종료로 인해 자동 결석 처리된 출석에 대해 출석 상태 변경 이벤트를 발행한다.
        // 이 이벤트를 Kafka Consumer가 수신하여 absentCount +1, warningCount +1을 반영한다.
        for (MeetingAttendance attendance : autoAbsentAttendances) {
            eventPublisher.publish(
                    MeetingAttendanceStatusChangedEvent.of(
                            attendance,
                            command.updatedBy()
                    )
            );
        }

        // 일정 상태 변경 이벤트 발행
        eventPublisher.publish(MeetingScheduleStatusChangedEvent.of(schedule));

        return MeetingScheduleResult.from(schedule);
    }

    // 다음 회차 번호 계산
    private int calculateNextScheduleNumber(UUID meetingId) {
        return meetingScheduleRepository.findByMeetingId(meetingId)
                .stream()
                .mapToInt(MeetingSchedule::getScheduleNumber)
                .max()
                .orElse(0) + 1;
    }
}

package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingScheduleCommand;
import com.pagely.meetingservice.meeting.application.dto.command.UpdateScheduleStatusCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingScheduleResult;
import com.pagely.meetingservice.meeting.application.port.BookProvider;
import com.pagely.meetingservice.meeting.application.port.EventPublisher;
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
                command.discussionNote(),
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

        // 상태를 변경할 일정 조회
        MeetingSchedule schedule = meetingScheduleRepository.findById(command.scheduleId())
                .orElseThrow(() -> new BusinessException(MeetingScheduleErrorCode.MEETING_SCHEDULE_NOT_FOUND));

        // 일정이 요청한 모임에 속한 일정인지 검증
        if (!schedule.getMeetingId().equals(command.meetingId())) {
            throw new BusinessException(MeetingScheduleErrorCode.SCHEDULE_NOT_FOR_MEETING);
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

        // FINISHED 상태로 변경하는 경우 출석 정보를 함께 확인하여 일정 종료 처리
        if (command.status() == MeetingScheduleStatus.FINISHED) {
            List<MeetingAttendance> attendances = meetingAttendanceRepository.findByScheduleId(command.scheduleId());
            schedule.finish(attendances, command.updatedBy());
        } else {
            schedule.changeStatus(command.status(), command.updatedBy());
        }

        // 일회성 모임의 일정이 종료되면 모임 상태도 COMPLETED로 함께 변경한다.
        if (meeting.isOneTime() && schedule.getStatus() == MeetingScheduleStatus.FINISHED) {
            meeting.finish(command.updatedBy());
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
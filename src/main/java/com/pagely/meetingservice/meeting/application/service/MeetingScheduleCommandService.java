package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingScheduleCommand;
import com.pagely.meetingservice.meeting.application.dto.command.UpdateScheduleStatusCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingScheduleResult;
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

    // 모임 일정 생성
    @Transactional
    public MeetingScheduleResult createSchedule(CreateMeetingScheduleCommand command) {
        // 일정을 생성할 모임이 실제 존재하는지 확인
        Meeting meeting = meetingRepository.findById(command.meetingId())
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

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

        // 생성된 일정 저장 후 결과 DTO로 변환
        MeetingSchedule savedSchedule = meetingScheduleRepository.save(schedule);

        return MeetingScheduleResult.from(savedSchedule);
    }

    // 모임 일정 상태 변경
    @Transactional
    public MeetingScheduleResult changeScheduleStatus(UpdateScheduleStatusCommand command) {
        // 요청한 모임이 실제 존재하는지 확인
        meetingRepository.findById(command.meetingId())
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
        // 그 외 상태 변경은 도메인의 상태 전이 규칙에 따라 처리
        if (command.status() == MeetingScheduleStatus.FINISHED) {
            List<MeetingAttendance> attendances = meetingAttendanceRepository.findByScheduleId(command.scheduleId());
            schedule.finish(attendances, command.updatedBy());
        } else {
            schedule.changeStatus(command.status(), command.updatedBy());
        }

        return MeetingScheduleResult.from(schedule);
    }

    // 다음 회차 번호 계산
    private int calculateNextScheduleNumber(UUID meetingId) {
        // 해당 모임의 기존 일정 목록에서 가장 큰 회차 번호를 찾고 +1
        // 일정이 하나도 없으면 1회차부터 시작
        return meetingScheduleRepository.findByMeetingId(meetingId)
                .stream()
                .mapToInt(MeetingSchedule::getScheduleNumber)
                .max()
                .orElse(0) + 1;
    }
}
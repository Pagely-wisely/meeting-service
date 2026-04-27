package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.command.UpdateAttendanceStatusCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingAttendanceResult;
import com.pagely.meetingservice.meeting.domain.exception.MeetingAttendanceErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingMemberErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingScheduleErrorCode;
import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.repository.MeetingAttendanceRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingScheduleRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 모임 일정 참석 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingAttendanceCommandService {

    private final MeetingRepository meetingRepository;
    private final MeetingScheduleRepository meetingScheduleRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final MeetingAttendanceRepository meetingAttendanceRepository;

    // 모임 일정 참석 등록
    @Transactional
    public MeetingAttendanceResult joinSchedule(UUID meetingId, UUID scheduleId, UUID userId) {
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        MeetingSchedule schedule = meetingScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(MeetingScheduleErrorCode.MEETING_SCHEDULE_NOT_FOUND));

        if (!schedule.getMeetingId().equals(meetingId)) {
            throw new BusinessException(MeetingScheduleErrorCode.SCHEDULE_NOT_FOR_MEETING);
        }

        // 일정 참석 등록은 ACTIVE 상태의 모임원만 가능
        MeetingMember member = meetingMemberRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new BusinessException(MeetingAttendanceErrorCode.ONLY_MEMBER_CAN_JOIN_SCHEDULE));

        if (!member.isActive()) {
            throw new BusinessException(MeetingAttendanceErrorCode.ONLY_ACTIVE_MEMBER_CAN_JOIN_SCHEDULE);
        }

        boolean alreadyJoined = meetingAttendanceRepository.existsByScheduleIdAndUserId(scheduleId, userId);
        if (alreadyJoined) {
            throw new BusinessException(MeetingAttendanceErrorCode.ATTENDANCE_ALREADY_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();

        MeetingAttendance attendance = MeetingAttendance.create(
                UUID.randomUUID(),
                meetingId,
                scheduleId,
                userId,
                now,
                userId
        );

        MeetingAttendance saved = meetingAttendanceRepository.save(attendance);

        return MeetingAttendanceResult.from(saved);
    }

    // 출석 상태 변경
    @Transactional
    public MeetingAttendanceResult changeAttendanceStatus(UpdateAttendanceStatusCommand command) {
        meetingRepository.findById(command.meetingId())
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        MeetingSchedule schedule = meetingScheduleRepository.findById(command.scheduleId())
                .orElseThrow(() -> new BusinessException(MeetingScheduleErrorCode.MEETING_SCHEDULE_NOT_FOUND));

        if (!schedule.getMeetingId().equals(command.meetingId())) {
            throw new BusinessException(MeetingScheduleErrorCode.SCHEDULE_NOT_FOR_MEETING);
        }

        if (!schedule.isOngoing()) {
            throw new BusinessException(MeetingAttendanceErrorCode.ONLY_ONGOING_SCHEDULE_CAN_CHANGE_ATTENDANCE);
        }

        MeetingMember updater = meetingMemberRepository.findByMeetingIdAndUserId(
                        command.meetingId(),
                        command.updatedBy()
                )
                .orElseThrow(() -> new BusinessException(MeetingMemberErrorCode.ONLY_MEETING_MEMBER_ALLOWED));

        if (!updater.isActive() || !updater.isHost()) {
            throw new BusinessException(MeetingAttendanceErrorCode.ONLY_HOST_CAN_CHANGE_ATTENDANCE);
        }

        // 출석 상태 변경 대상자는 ACTIVE 상태의 모임원이어야 함
        MeetingMember targetMember = meetingMemberRepository.findByMeetingIdAndUserId(
                        command.meetingId(),
                        command.userId()
                )
                .orElseThrow(
                        () -> new BusinessException(MeetingAttendanceErrorCode.ATTENDANCE_USER_NOT_MEETING_MEMBER));

        if (!targetMember.isActive()) {
            throw new BusinessException(MeetingAttendanceErrorCode.ONLY_ACTIVE_MEMBER_CAN_CHANGE_ATTENDANCE);
        }

        MeetingAttendance attendance = meetingAttendanceRepository.findByScheduleIdAndUserId(
                        command.scheduleId(),
                        command.userId()
                )
                .orElseThrow(() -> new BusinessException(MeetingAttendanceErrorCode.ATTENDANCE_NOT_FOUND));

        AttendanceStatus finalStatus = command.status();

        // 출석 처리 요청이 들어왔더라도 시작 시간 기준 10분 초과면 지각 처리
        if (command.status() == AttendanceStatus.ATTENDED
                && LocalDateTime.now().isAfter(schedule.getStartAt().plusMinutes(10))) {
            finalStatus = AttendanceStatus.LATE;
        }

        System.out.println("요청 scheduleId = " + command.scheduleId());
        System.out.println("요청 userId = " + command.userId());
        System.out.println("DB 출석 ID = " + attendance.getId());
        System.out.println("DB 출석 상태 = " + attendance.getStatus());
        System.out.println("요청 변경 상태 = " + command.status());

        attendance.changeStatus(
                finalStatus,
                command.note(),
                command.updatedBy()
        );

        return MeetingAttendanceResult.from(attendance);
    }
}
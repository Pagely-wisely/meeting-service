package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.command.UpdateAttendanceStatusCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingAttendanceResult;
import com.pagely.meetingservice.meeting.domain.exception.MeetingAttendanceErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingMemberErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingScheduleErrorCode;
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

        boolean isMember = meetingMemberRepository.existsByMeetingIdAndUserId(meetingId, userId);
        if (!isMember) {
            throw new BusinessException(MeetingAttendanceErrorCode.ONLY_MEMBER_CAN_JOIN_SCHEDULE);
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

        boolean targetIsMember = meetingMemberRepository.existsByMeetingIdAndUserId(
                command.meetingId(),
                command.userId()
        );
        if (!targetIsMember) {
            throw new BusinessException(MeetingAttendanceErrorCode.ATTENDANCE_USER_NOT_MEETING_MEMBER);
        }

        MeetingAttendance attendance = meetingAttendanceRepository.findByScheduleIdAndUserId(
                        command.scheduleId(),
                        command.userId()
                )
                .orElseThrow(() -> new BusinessException(MeetingAttendanceErrorCode.ATTENDANCE_NOT_FOUND));

        attendance.changeStatus(
                command.status(),
                command.note(),
                command.updatedBy()
        );

        return MeetingAttendanceResult.from(attendance);
    }
}
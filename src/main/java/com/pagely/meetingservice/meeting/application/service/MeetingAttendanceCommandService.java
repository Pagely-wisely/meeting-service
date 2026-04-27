package com.pagely.meetingservice.meeting.application.service;

import com.pagely.meetingservice.meeting.application.dto.command.UpdateAttendanceStatusCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingAttendanceResult;
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
                .orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다."));

        MeetingSchedule schedule = meetingScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("모임 일정이 존재하지 않습니다."));

        if (!schedule.getMeetingId().equals(meetingId)) {
            throw new IllegalArgumentException("해당 모임에 속한 일정이 아닙니다.");
        }

        boolean isMember = meetingMemberRepository.existsByMeetingIdAndUserId(meetingId, userId);
        if (!isMember) {
            throw new IllegalArgumentException("모임원만 일정 참석 등록이 가능합니다.");
        }

        boolean alreadyJoined = meetingAttendanceRepository.existsByScheduleIdAndUserId(scheduleId, userId);
        if (alreadyJoined) {
            throw new IllegalArgumentException("이미 참석 등록한 일정입니다.");
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
                .orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다."));

        MeetingSchedule schedule = meetingScheduleRepository.findById(command.scheduleId())
                .orElseThrow(() -> new IllegalArgumentException("모임 일정이 존재하지 않습니다."));

        if (!schedule.getMeetingId().equals(command.meetingId())) {
            throw new IllegalArgumentException("해당 모임에 속한 일정이 아닙니다.");
        }

        if (!schedule.isOngoing()) {
            throw new IllegalArgumentException("진행중인 일정에서만 출석 상태를 변경할 수 있습니다.");
        }

        MeetingMember updater = meetingMemberRepository.findByMeetingIdAndUserId(
                        command.meetingId(),
                        command.updatedBy()
                )
                .orElseThrow(() -> new IllegalArgumentException("모임원만 출석 상태를 변경할 수 있습니다."));

        if (!updater.isActive() || !updater.isHost()) {
            throw new IllegalArgumentException("모임장만 출석 상태를 변경할 수 있습니다.");
        }

        boolean targetIsMember = meetingMemberRepository.existsByMeetingIdAndUserId(
                command.meetingId(),
                command.userId()
        );
        if (!targetIsMember) {
            throw new IllegalArgumentException("출석 상태 변경 대상 유저가 해당 모임원이 아닙니다.");
        }

        MeetingAttendance attendance = meetingAttendanceRepository.findByScheduleIdAndUserId(
                        command.scheduleId(),
                        command.userId()
                )
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 참석 등록 내역이 없습니다."));

        attendance.changeStatus(
                command.status(),
                command.note(),
                command.updatedBy()
        );

        return MeetingAttendanceResult.from(attendance);
    }
}
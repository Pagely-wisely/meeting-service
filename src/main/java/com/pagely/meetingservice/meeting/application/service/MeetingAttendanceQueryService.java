package com.pagely.meetingservice.meeting.application.service;

import com.pagely.meetingservice.meeting.application.dto.result.MeetingAttendanceResult;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.repository.MeetingAttendanceRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingScheduleRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 모임 일정 출석 조회 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingAttendanceQueryService {

    private final MeetingRepository meetingRepository;
    private final MeetingScheduleRepository meetingScheduleRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final MeetingAttendanceRepository meetingAttendanceRepository;

    // 특정 일정의 출석부 조회
    public List<MeetingAttendanceResult> getScheduleAttendances(
            UUID meetingId,
            UUID scheduleId,
            UUID userId
    ) {
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다."));

        MeetingSchedule schedule = meetingScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("모임 일정이 존재하지 않습니다."));

        if (!schedule.getMeetingId().equals(meetingId)) {
            throw new IllegalArgumentException("해당 모임에 속한 일정이 아닙니다.");
        }

        MeetingMember member = meetingMemberRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("모임원만 출석부를 조회할 수 있습니다."));

        if (!member.isActive() || !member.isHost()) {
            throw new IllegalArgumentException("모임장만 전체 출석부를 조회할 수 있습니다.");
        }

        return meetingAttendanceRepository.findByScheduleId(scheduleId)
                .stream()
                .map(MeetingAttendanceResult::from)
                .toList();
    }

    // 내 출석부 조회
    public List<MeetingAttendanceResult> getMyAttendances(UUID meetingId, UUID userId) {
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다."));

        boolean isMember = meetingMemberRepository.existsByMeetingIdAndUserId(meetingId, userId);
        if (!isMember) {
            throw new IllegalArgumentException("모임원만 내 출석부를 조회할 수 있습니다.");
        }

        return meetingAttendanceRepository.findByMeetingIdAndUserId(meetingId, userId)
                .stream()
                .map(MeetingAttendanceResult::from)
                .toList();
    }
}
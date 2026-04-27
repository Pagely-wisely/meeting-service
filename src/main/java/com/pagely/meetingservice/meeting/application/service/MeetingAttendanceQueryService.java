package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingAttendanceResult;
import com.pagely.meetingservice.meeting.domain.exception.MeetingAttendanceErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingMemberErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingScheduleErrorCode;
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
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        MeetingSchedule schedule = meetingScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(MeetingScheduleErrorCode.MEETING_SCHEDULE_NOT_FOUND));

        if (!schedule.getMeetingId().equals(meetingId)) {
            throw new BusinessException(MeetingScheduleErrorCode.SCHEDULE_NOT_FOR_MEETING);
        }

        MeetingMember member = meetingMemberRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new BusinessException(MeetingMemberErrorCode.ONLY_MEETING_MEMBER_ALLOWED));

        if (!member.isActive() || !member.isHost()) {
            throw new BusinessException(MeetingAttendanceErrorCode.ONLY_HOST_CAN_VIEW_ATTENDANCES);
        }

        return meetingAttendanceRepository.findByScheduleId(scheduleId)
                .stream()
                .map(MeetingAttendanceResult::from)
                .toList();
    }

    // 내 출석부 조회
    public List<MeetingAttendanceResult> getMyAttendances(UUID meetingId, UUID userId) {
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        boolean isMember = meetingMemberRepository.existsByMeetingIdAndUserId(meetingId, userId);
        if (!isMember) {
            throw new BusinessException(MeetingMemberErrorCode.ONLY_MEETING_MEMBER_ALLOWED);
        }

        return meetingAttendanceRepository.findByMeetingIdAndUserId(meetingId, userId)
                .stream()
                .map(MeetingAttendanceResult::from)
                .toList();
    }
}
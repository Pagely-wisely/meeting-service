package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.result.AttendanceStatisticsResult;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // 특정 일정의 출석부 조회 - 페이징 처리
    public Page<MeetingAttendanceResult> getScheduleAttendances(
            UUID meetingId,
            UUID scheduleId,
            UUID userId,
            Pageable pageable
    ) {
        validateScheduleAttendanceViewPermission(meetingId, scheduleId, userId);

        Page<MeetingAttendance> attendances =
                meetingAttendanceRepository.findByScheduleId(scheduleId, pageable);

        if (attendances.isEmpty()) {
            throw new BusinessException(MeetingAttendanceErrorCode.ATTENDANCE_LIST_NOT_FOUND);
        }

        return attendances.map(MeetingAttendanceResult::from);
    }

    // 특정 일정의 출석 통계 조회
    public AttendanceStatisticsResult getScheduleAttendanceStatistics(
            UUID meetingId,
            UUID scheduleId,
            UUID userId
    ) {
        // 출석부 조회 권한과 동일한 권한 정책을 적용한다.
        validateScheduleAttendanceViewPermission(meetingId, scheduleId, userId);

        // DB에서 상태별 건수 집계
        Map<AttendanceStatus, Long> counts =
                meetingAttendanceRepository.countByScheduleIdGroupedByStatus(scheduleId);

        long attended = counts.getOrDefault(AttendanceStatus.ATTENDED, 0L);
        long late = counts.getOrDefault(AttendanceStatus.LATE, 0L);
        long absent = counts.getOrDefault(AttendanceStatus.ABSENT, 0L);
        long excused = counts.getOrDefault(AttendanceStatus.EXCUSED, 0L);

        return AttendanceStatisticsResult.fromStatusCounts(attended, late, absent, excused);
    }

    // 내 출석부 조회 - 페이징 처리
    public Page<MeetingAttendanceResult> getMyAttendances(
            UUID meetingId,
            UUID userId,
            Pageable pageable
    ) {
        validateMyAttendanceViewPermission(meetingId, userId);

        // 해당 사용자의 모임 내 출석 이력을 페이징 조회 후 결과 DTO로 변환
        return meetingAttendanceRepository.findByMeetingIdAndUserId(meetingId, userId, pageable)
                .map(MeetingAttendanceResult::from);
    }

    // 내 출석 통계 조회
    public AttendanceStatisticsResult getMyAttendanceStatistics(UUID meetingId, UUID userId) {
        validateMyAttendanceViewPermission(meetingId, userId);

        // DB에서 해당 모임/유저 출석을 상태별로 집계
        Map<AttendanceStatus, Long> counts =
                meetingAttendanceRepository.countByMeetingIdAndUserIdGroupedByStatus(meetingId, userId);

        long attended = counts.getOrDefault(AttendanceStatus.ATTENDED, 0L);
        long late = counts.getOrDefault(AttendanceStatus.LATE, 0L);
        long absent = counts.getOrDefault(AttendanceStatus.ABSENT, 0L);
        long excused = counts.getOrDefault(AttendanceStatus.EXCUSED, 0L);

        return AttendanceStatisticsResult.fromStatusCounts(attended, late, absent, excused);
    }

    // 특정 일정 출석부 조회 권한 검증
    private void validateScheduleAttendanceViewPermission(
            UUID meetingId,
            UUID scheduleId,
            UUID userId
    ) {
        if (!meetingRepository.existsById(meetingId)) {
            throw new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND);
        }

        Optional<MeetingSchedule> schedule = meetingScheduleRepository.findByIdAndMeetingIdAndDeletedAtIsNull(
                scheduleId, meetingId);

        if (schedule.isEmpty()) {
            if (meetingScheduleRepository.findById(scheduleId).isPresent()) {
                throw new BusinessException(MeetingScheduleErrorCode.SCHEDULE_NOT_FOR_MEETING);
            }

            throw new BusinessException(MeetingScheduleErrorCode.MEETING_SCHEDULE_NOT_FOUND);
        }

        MeetingMember member = meetingMemberRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new BusinessException(MeetingMemberErrorCode.ONLY_MEETING_MEMBER_ALLOWED));

        if (!member.isActive() || !member.isHost()) {
            throw new BusinessException(MeetingAttendanceErrorCode.ONLY_HOST_CAN_VIEW_ATTENDANCES);
        }
    }

    // 내 출석부 조회 권한 검증
    private void validateMyAttendanceViewPermission(UUID meetingId, UUID userId) {

        if (!meetingRepository.existsById(meetingId)) {
            throw new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND);
        }

        boolean isMember = meetingMemberRepository.existsByMeetingIdAndUserId(meetingId, userId);
        if (!isMember) {
            throw new BusinessException(MeetingMemberErrorCode.ONLY_MEETING_MEMBER_ALLOWED);
        }
    }
}

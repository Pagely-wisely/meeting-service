package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.result.AttendanceStatisticsResult;
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
        validateScheduleAttendanceViewPermission(meetingId, scheduleId, userId);

        // 해당 일정에 등록된 출석 정보를 조회 후 결과 DTO로 변환
        return meetingAttendanceRepository.findByScheduleId(scheduleId)
                .stream()
                .map(MeetingAttendanceResult::from)
                .toList();
    }

    // 특정 일정의 출석 통계 조회
    public AttendanceStatisticsResult getScheduleAttendanceStatistics(
            UUID meetingId,
            UUID scheduleId,
            UUID userId
    ) {
        // 출석부 조회 권한과 동일한 권한 정책을 적용한다.
        // 즉, 특정 일정의 전체 출석 통계는 ACTIVE 상태의 모임장만 조회 가능하다.
        validateScheduleAttendanceViewPermission(meetingId, scheduleId, userId);

        List<MeetingAttendanceResult> attendances = meetingAttendanceRepository.findByScheduleId(scheduleId)
                .stream()
                .map(MeetingAttendanceResult::from)
                .toList();

        // 조회된 출석 상태를 기준으로 통계를 즉시 계산한다.
        // 지각 3회 = 결석 1회 환산 정책도 여기서 계산된다.
        return AttendanceStatisticsResult.from(attendances);
    }

    // 내 출석부 조회
    public List<MeetingAttendanceResult> getMyAttendances(UUID meetingId, UUID userId) {
        validateMyAttendanceViewPermission(meetingId, userId);

        // 해당 사용자의 모임 내 출석 이력을 조회 후 결과 DTO로 변환
        return meetingAttendanceRepository.findByMeetingIdAndUserId(meetingId, userId)
                .stream()
                .map(MeetingAttendanceResult::from)
                .toList();
    }

    // 내 출석 통계 조회
    public AttendanceStatisticsResult getMyAttendanceStatistics(UUID meetingId, UUID userId) {
        // 내 출석 통계는 해당 모임의 멤버만 조회 가능하다.
        validateMyAttendanceViewPermission(meetingId, userId);

        List<MeetingAttendanceResult> attendances = meetingAttendanceRepository.findByMeetingIdAndUserId(meetingId,
                        userId)
                .stream()
                .map(MeetingAttendanceResult::from)
                .toList();

        // 내 출석 이력을 기준으로 출석/지각/결석/사유결석 통계를 계산한다.
        // LATE 상태 자체는 유지하고, 지각 3회 환산은 통계 값에만 반영한다.
        return AttendanceStatisticsResult.from(attendances);
    }

    // 특정 일정 출석부 조회 권한 검증
    private void validateScheduleAttendanceViewPermission(
            UUID meetingId,
            UUID scheduleId,
            UUID userId
    ) {
        // 요청한 모임이 실제 존재하는지 확인
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        // 출석부를 조회할 일정이 실제 존재하는지 확인
        MeetingSchedule schedule = meetingScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(MeetingScheduleErrorCode.MEETING_SCHEDULE_NOT_FOUND));

        // 일정이 요청한 모임에 속한 일정인지 검증
        // 다른 모임의 scheduleId로 출석부를 조회하는 것을 방지
        if (!schedule.getMeetingId().equals(meetingId)) {
            throw new BusinessException(MeetingScheduleErrorCode.SCHEDULE_NOT_FOR_MEETING);
        }

        // 출석부 조회 요청자가 해당 모임의 멤버인지 확인
        MeetingMember member = meetingMemberRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new BusinessException(MeetingMemberErrorCode.ONLY_MEETING_MEMBER_ALLOWED));

        // 출석부 전체 조회는 ACTIVE 상태의 모임장만 가능
        // 일반 모임원이 다른 회원들의 출석 정보를 조회하는 것을 방지
        if (!member.isActive() || !member.isHost()) {
            throw new BusinessException(MeetingAttendanceErrorCode.ONLY_HOST_CAN_VIEW_ATTENDANCES);
        }
    }

    // 내 출석부 조회 권한 검증
    private void validateMyAttendanceViewPermission(UUID meetingId, UUID userId) {
        // 요청한 모임이 실제 존재하는지 확인
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        // 내 출석부 조회는 해당 모임의 멤버만 가능
        // 모임에 속하지 않은 사용자가 출석 이력을 조회하는 것을 방지
        boolean isMember = meetingMemberRepository.existsByMeetingIdAndUserId(meetingId, userId);
        if (!isMember) {
            throw new BusinessException(MeetingMemberErrorCode.ONLY_MEETING_MEMBER_ALLOWED);
        }
    }
}
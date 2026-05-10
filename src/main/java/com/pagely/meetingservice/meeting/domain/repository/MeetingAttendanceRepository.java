package com.pagely.meetingservice.meeting.domain.repository;

import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// 출석 저장/조회 인터페이스
public interface MeetingAttendanceRepository {

    // 출석 저장
    MeetingAttendance save(MeetingAttendance meetingAttendance);

    // ID로 출석 조회
    Optional<MeetingAttendance> findById(UUID attendanceId);

    // 일정 + 유저 기준 출석 조회
    Optional<MeetingAttendance> findByScheduleIdAndUserId(UUID scheduleId, UUID userId);

    // 특정 일정의 출석부 조회 - 페이징 처리
    Page<MeetingAttendance> findByScheduleId(UUID scheduleId, Pageable pageable);

    // 특정 일정의 출석부 조회 - 통계 계산용 전체 조회
    List<MeetingAttendance> findByScheduleId(UUID scheduleId);

    // 특정 일정의 상태별 출석 조회
    List<MeetingAttendance> findByScheduleIdAndStatus(UUID scheduleId, AttendanceStatus status);

    // 특정 모임에서 내 출석 목록 조회 - 페이징 처리
    Page<MeetingAttendance> findByMeetingIdAndUserId(UUID meetingId, UUID userId, Pageable pageable);

    // 특정 모임에서 내 출석 목록 조회 - 통계 계산용 전체 조회
    List<MeetingAttendance> findByMeetingIdAndUserId(UUID meetingId, UUID userId);

    // 특정 유저의 참석 행 전체 조회
    List<MeetingAttendance> findAllByUserId(UUID userId);

    // 특정 일정에 이미 출석 등록했는지 확인
    boolean existsByScheduleIdAndUserId(UUID scheduleId, UUID userId);

    // 지정한 모임 ID에 한정된 특정 유저의 참석 목록 조회
    List<MeetingAttendance> findAllByUserIdAndMeetingIdIn(UUID userId, Collection<UUID> meetingIds);

    // 일정 단위 출석 행을 상태 별로 집계
    Map<AttendanceStatus, Long> countByScheduleIdGroupedByStatus(UUID scheduleId);

    // 모임 +유저 단위 출석 행을 상태 별로 집계
    Map<AttendanceStatus, Long> countByMeetingIdAndUserIdGroupedByStatus(UUID meetingId, UUID userId);
}
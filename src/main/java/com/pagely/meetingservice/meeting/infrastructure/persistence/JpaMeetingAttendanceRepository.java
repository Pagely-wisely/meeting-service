package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 모임 일정 참석 JPA Repository
public interface JpaMeetingAttendanceRepository extends JpaRepository<MeetingAttendance, UUID> {

    // 일정 + 유저 기준 참석 조회
    Optional<MeetingAttendance> findByScheduleIdAndUserIdAndDeletedAtIsNull(UUID scheduleId, UUID userId);

    // 특정 일정의 참석 목록 조회 - 페이징 처리
    Page<MeetingAttendance> findAllByScheduleIdAndDeletedAtIsNull(UUID scheduleId, Pageable pageable);

    // 특정 일정의 참석 목록 조회 - 통계 계산용 전체 조회
    List<MeetingAttendance> findAllByScheduleIdAndDeletedAtIsNull(UUID scheduleId);

    // 특정 일정의 상태별 참석 목록 조회
    List<MeetingAttendance> findAllByScheduleIdAndStatusAndDeletedAtIsNull(
            UUID scheduleId,
            AttendanceStatus status
    );

    // 특정 모임에서 특정 유저의 참석 목록 조회 - 페이징 처리
    Page<MeetingAttendance> findAllByMeetingIdAndUserIdAndDeletedAtIsNull(
            UUID meetingId,
            UUID userId,
            Pageable pageable
    );

    // 특정 모임에서 특정 유저의 참석 목록 조회 - 통계 계산용 전체 조회
    List<MeetingAttendance> findAllByMeetingIdAndUserIdAndDeletedAtIsNull(UUID meetingId, UUID userId);

    // 특정 일정에 이미 참석 등록했는지 확인
    boolean existsByScheduleIdAndUserIdAndDeletedAtIsNull(UUID scheduleId, UUID userId);

    // 특정 유저의 모든 참석 행 조회
    List<MeetingAttendance> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    // 지정한 모임 ID에 한정된 특정 유저의 참석 목록 조회
    List<MeetingAttendance> findAllByUserIdAndMeetingIdInAndDeletedAtIsNull(UUID userId, Collection<UUID> meetingIds);

    // 특정 일정의 출석 행을 상태 별로 묶어 집계
    @Query("""
            SELECT a.status, COUNT(a)
            FROM MeetingAttendance a
            WHERE a.scheduleId = :scheduleId
              AND a.deletedAt IS NULL
            GROUP BY a.status
            """)
    List<Object[]> countGroupedByStatusForSchedule(@Param("scheduleId") UUID scheduleId);

    // 특정 모임과 유저 출석 행을 상태별로 집계
    @Query("""
            SELECT a.status, COUNT(a)
            FROM MeetingAttendance a
            WHERE a.meetingId = :meetingId
              AND a.userId = :userId
              AND a.deletedAt IS NULL
            GROUP BY a.status
            """)
    List<Object[]> countGroupedByStatusForMeetingAndUser(@Param("meetingId") UUID meetingId,
                                                         @Param("userId") UUID userId);
}
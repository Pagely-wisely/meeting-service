package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 모임 일정 JPA Repository
public interface JpaMeetingScheduleRepository extends JpaRepository<MeetingSchedule, UUID> {

    // 특정 모임의 일정 목록 조회
    List<MeetingSchedule> findByMeetingId(UUID meetingId);

    // 특정 모임의 삭제되지 않은 일정 목록 조회 - 페이징 처리
    Page<MeetingSchedule> findByMeetingIdAndDeletedAtIsNull(UUID meetingId, Pageable pageable);

    // 특정 모임의 삭제되지 않은 일정 상세 조회
    Optional<MeetingSchedule> findByIdAndMeetingIdAndDeletedAtIsNull(UUID scheduleId, UUID meetingId);

    // 특정 모임의 상태별 일정 목록 조회
    List<MeetingSchedule> findByMeetingIdAndStatus(UUID meetingId, MeetingScheduleStatus status);

    // 특정 모임의 회차 번호로 일정 조회
    Optional<MeetingSchedule> findByMeetingIdAndScheduleNumber(UUID meetingId, int scheduleNumber);

    // 특정 모임 내 회차 번호 중복 여부 확인
    boolean existsByMeetingIdAndScheduleNumber(UUID meetingId, int scheduleNumber);

    // 시작 시간이 지난 SCHEDULED 상태의 일정만 제한적으로 조회한다.
    @Query("""
            SELECT s
            FROM MeetingSchedule s
            WHERE s.status = 'SCHEDULED'
              AND s.startAt <= :now
              AND s.deletedAt IS NULL
            ORDER BY s.startAt ASC
            """)
    List<MeetingSchedule> findStartDueSchedules(
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    // 일정 상태 변경 시 동시 변경을 막기 위한 쓰기 락 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM MeetingSchedule s
            WHERE s.id = :scheduleId
              AND s.deletedAt IS NULL
            """)
    Optional<MeetingSchedule> findByIdForUpdate(@Param("scheduleId") UUID scheduleId);
}

package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleReport;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

// 모임 일정별 독후감 작성 기록 JPA Repository
public interface JpaMeetingScheduleReportRepository extends JpaRepository<MeetingScheduleReport, UUID> {

    // 특정 일정에서 독후감을 작성한 유저 ID 목록 조회
    @Query("""
            SELECT r.userId
            FROM MeetingScheduleReport r
            WHERE r.scheduleId = :scheduleId
            """)
    List<UUID> findUserIdsByScheduleId(UUID scheduleId);

    // 특정 일정에서 특정 유저가 독후감을 작성했는지 확인
    boolean existsByScheduleIdAndUserId(UUID scheduleId, UUID userId);
}

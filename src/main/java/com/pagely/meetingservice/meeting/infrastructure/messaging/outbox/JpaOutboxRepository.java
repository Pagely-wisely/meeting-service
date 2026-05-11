package com.pagely.meetingservice.meeting.infrastructure.messaging.outbox;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

// Outbox JPA Repository
public interface JpaOutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    // 다중 인스턴스 중복 발행 방지를 위해 SKIP LOCKED로 발행 대상만 선점한다.
    @Query(
            value = """
                    SELECT *
                    FROM p_outbox
                    WHERE published = FALSE
                      AND publishing = FALSE
                    ORDER BY created_at ASC
                    FOR UPDATE SKIP LOCKED
                    """,
            nativeQuery = true
    )
    List<OutboxEvent> findPublishCandidates(Pageable pageable);

    // 실패 횟수가 기준 이상인 이벤트를 조회한다.
    @Query("""
            SELECT o
            FROM OutboxEvent o
            WHERE o.published = false
              AND o.failureCount >= :threshold
            ORDER BY o.lastFailureAt ASC
            """)
    List<OutboxEvent> findFailedExceedingThreshold(int threshold, Pageable pageable);
}

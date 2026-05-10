package com.pagely.meetingservice.meeting.infrastructure.messaging.outbox;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

// Outbox JPA Repository
public interface JpaOutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    // 아직 발행되지 않은 이벤트를 오래된 순서로 조회한다.
    @Query("""
            SELECT o
            FROM OutboxEvent o
            WHERE o.published = false
            ORDER BY o.createdAt ASC
            """)
    List<OutboxEvent> findUnpublished(Pageable pageable);

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

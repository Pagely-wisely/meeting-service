package com.pagely.meetingservice.meeting.infrastructure.messaging.outbox;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

// Outbox Repository 구현체
@Repository
@RequiredArgsConstructor
public class OutboxRepositoryAdapter implements OutboxRepository {

    private final JpaOutboxRepository jpaOutboxRepository;

    @Override
    public OutboxEvent save(OutboxEvent event) {
        return jpaOutboxRepository.save(event);
    }

    @Override
    public List<OutboxEvent> findUnpublished(Pageable pageable) {
        return jpaOutboxRepository.findUnpublished(pageable);
    }

    @Override
    public List<OutboxEvent> findFailedExceedingThreshold(int threshold, Pageable pageable) {
        return jpaOutboxRepository.findFailedExceedingThreshold(threshold, pageable);
    }
}

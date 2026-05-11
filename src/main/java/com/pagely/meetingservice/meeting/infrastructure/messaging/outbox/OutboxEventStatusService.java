package com.pagely.meetingservice.meeting.infrastructure.messaging.outbox;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// Outbox 이벤트 상태 변경 전용 서비스
@Service
@RequiredArgsConstructor
public class OutboxEventStatusService {

    private final OutboxRepository outboxRepository;

    // 발행 대상 이벤트를 짧은 트랜잭션 안에서 선점한다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<OutboxEvent> claimPublishTargets(int batchSize) {
        List<OutboxEvent> events = outboxRepository.findPublishCandidates(
                PageRequest.of(0, batchSize)
        );

        for (OutboxEvent event : events) {
            event.markPublishing();
        }

        return events;
    }

    // Kafka 발행 성공 처리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublished(UUID outboxId) {
        outboxRepository.findById(outboxId)
                .ifPresent(OutboxEvent::markPublished);
    }

    // Kafka 발행 실패 처리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(UUID outboxId, String message) {
        outboxRepository.findById(outboxId)
                .ifPresent(event -> event.recordFailure(message));
    }
}

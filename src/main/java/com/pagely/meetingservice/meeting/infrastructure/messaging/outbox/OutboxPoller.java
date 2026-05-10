package com.pagely.meetingservice.meeting.infrastructure.messaging.outbox;

import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private static final int BATCH_SIZE = 100;
    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> stringKafkaTemplate;

    // 미발행 Outbox 이벤트를 주기적으로 Kafka에 발행한다.
    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxRepository.findUnpublished(
                PageRequest.of(0, BATCH_SIZE)
        );

        if (events.isEmpty()) {
            return;
        }

        for (OutboxEvent event : events) {
            try {
                publishToKafka(event);
                event.markPublished();

                log.info(
                        "Outbox Kafka 발행 성공. outboxId={}, topic={}, aggregateId={}",
                        event.getId(),
                        event.getTopic(),
                        event.getAggregateId()
                );
            } catch (Exception e) {
                event.recordFailure(e.getMessage());

                log.error(
                        "Outbox Kafka 발행 실패. outboxId={}, topic={}, failureCount={}",
                        event.getId(),
                        event.getTopic(),
                        event.getFailureCount(),
                        e
                );
            }
        }
    }

    private void publishToKafka(OutboxEvent event) throws Exception {
        // Kafka ACK를 확인한 뒤 published 처리하기 위해 동기 발행한다.
        stringKafkaTemplate.send(
                event.getTopic(),
                event.getAggregateId().toString(),
                event.getPayload()
        ).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}

package com.pagely.meetingservice.meeting.infrastructure.messaging.outbox;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private static final int BATCH_SIZE = 100;

    private final OutboxEventStatusService outboxEventStatusService;
    private final KafkaTemplate<String, String> stringKafkaTemplate;
    private final MeterRegistry meterRegistry;

    // 미발행 Outbox 이벤트를 주기적으로 Kafka에 발행한다.
    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:5000}")
    public void publishPendingEvents() {
        // Outbox Poller 실행 횟수를 기록한다.
        meterRegistry.counter("outbox.poll.count").increment();

        List<OutboxEvent> events = outboxEventStatusService.claimPublishTargets(BATCH_SIZE);

        // 이번 Polling에서 선점한 이벤트 수를 기록한다.
        meterRegistry.counter("outbox.poll.claimed.count")
                .increment(events.size());

        if (events.isEmpty()) {
            return;
        }

        for (OutboxEvent event : events) {
            publishToKafka(event);
        }
    }

    private void publishToKafka(OutboxEvent event) {
        // Kafka 발행 결과는 callback에서 처리한다.
        stringKafkaTemplate.send(
                event.getTopic(),
                event.getAggregateId().toString(),
                event.getPayload()
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                outboxEventStatusService.recordFailure(
                        event.getId(),
                        ex.getMessage()
                );

                // Kafka 발행 실패 횟수를 topic/eventType 기준으로 기록한다.
                Counter.builder("outbox.kafka.publish.failure")
                        .tag("topic", event.getTopic())
                        .tag("eventType", event.getEventType())
                        .register(meterRegistry)
                        .increment();

                log.error(
                        "Outbox Kafka 비동기 발행 실패. outboxId={}, topic={}, failureCount={}",
                        event.getId(),
                        event.getTopic(),
                        event.getFailureCount(),
                        ex
                );
                return;
            }

            outboxEventStatusService.markPublished(event.getId());

            // Kafka 발행 성공 횟수를 topic/eventType 기준으로 기록한다.
            Counter.builder("outbox.kafka.publish.success")
                    .tag("topic", event.getTopic())
                    .tag("eventType", event.getEventType())
                    .register(meterRegistry)
                    .increment();

            log.info(
                    "Outbox Kafka 비동기 발행 성공. outboxId={}, topic={}, aggregateId={}, partition={}, offset={}",
                    event.getId(),
                    event.getTopic(),
                    event.getAggregateId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
    }
}

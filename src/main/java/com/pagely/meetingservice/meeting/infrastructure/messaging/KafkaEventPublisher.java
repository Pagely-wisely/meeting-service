package com.pagely.meetingservice.meeting.infrastructure.messaging;

import com.pagely.meetingservice.meeting.application.port.EventPublisher;
import com.pagely.meetingservice.meeting.domain.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private static final String MEETING_EVENT_TOPIC = "meeting-event";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(BaseEvent event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event);
                }
            });
            return;
        }

        send(event);
    }

    private void send(BaseEvent event) {
        kafkaTemplate.send(
                MEETING_EVENT_TOPIC,
                event.getDomainId(),
                event
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error(
                        "Kafka publish failed. topic={}, domainId={}, eventType={}",
                        MEETING_EVENT_TOPIC,
                        event.getDomainId(),
                        event.getEventType(),
                        ex
                );
                return;
            }

            log.info(
                    "Kafka publish success. topic={}, partition={}, offset={}, domainId={}, eventType={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    event.getDomainId(),
                    event.getEventType()
            );
        });
    }
}
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

    private static final String MEETING_CREATED_TOPIC = "meeting.created";
    private static final String MEETING_SCHEDULE_CREATED_TOPIC = "meeting.schedule.created";
    private static final String MEETING_SCHEDULE_STATUS_CHANGED_TOPIC = "meeting.schedule.status-changed";
    private static final String MEETING_ATTENDANCE_JOINED_TOPIC = "meeting.attendance.joined";
    private static final String MEETING_ATTENDANCE_STATUS_CHANGED_TOPIC = "meeting.attendance.status-changed";

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
        String topic = resolveTopic(event);

        kafkaTemplate.send(
                topic,
                event.getDomainId(),
                event
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error(
                        "Kafka publish failed. topic={}, domainId={}, eventType={}",
                        topic,
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

    private String resolveTopic(BaseEvent event) {
        return switch (event.getEventType()) {
            case "MeetingCreatedEvent" -> MEETING_CREATED_TOPIC;
            case "MeetingScheduleCreatedEvent" -> MEETING_SCHEDULE_CREATED_TOPIC;
            case "MeetingScheduleStatusChangedEvent" -> MEETING_SCHEDULE_STATUS_CHANGED_TOPIC;
            case "MeetingAttendanceJoinedEvent" -> MEETING_ATTENDANCE_JOINED_TOPIC;
            case "MeetingAttendanceStatusChangedEvent" -> MEETING_ATTENDANCE_STATUS_CHANGED_TOPIC;
            default -> throw new IllegalArgumentException("Unsupported event type: " + event.getEventType());
        };
    }
}
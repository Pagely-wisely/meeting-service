package com.pagely.meetingservice.meeting.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pagely.meetingservice.meeting.application.port.EventPublisher;
import com.pagely.meetingservice.meeting.domain.event.BaseEvent;
import com.pagely.meetingservice.meeting.infrastructure.messaging.outbox.OutboxEvent;
import com.pagely.meetingservice.meeting.infrastructure.messaging.outbox.OutboxRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(BaseEvent event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void beforeCommit(boolean readOnly) {
                    // 커밋 직전에 같은 트랜잭션 안에서 Outbox에 저장한다.
                    saveOutbox(event);
                }
            });
            return;
        }

        // 트랜잭션 밖에서 호출된 경우 바로 Outbox에 저장한다.
        saveOutbox(event);
    }

    private void saveOutbox(BaseEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            String topic = resolveTopic(event);

            OutboxEvent outboxEvent = OutboxEvent.of(
                    UUID.fromString(event.getEventId()),
                    event.getDomainType(),
                    UUID.fromString(event.getDomainId()),
                    event.getEventType(),
                    topic,
                    payload
            );

            outboxRepository.save(outboxEvent);

            log.info(
                    "Outbox 저장 완료. eventType={}, domainId={}, topic={}",
                    event.getEventType(),
                    event.getDomainId(),
                    topic
            );
        } catch (JsonProcessingException e) {
            log.error("Outbox 이벤트 직렬화 실패. eventType={}", event.getEventType(), e);
            throw new IllegalStateException("Outbox 이벤트 직렬화 실패: " + event.getEventType(), e);
        }
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

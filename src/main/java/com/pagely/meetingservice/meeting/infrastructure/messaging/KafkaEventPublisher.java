package com.pagely.meetingservice.meeting.infrastructure.messaging;

import com.pagely.meetingservice.meeting.application.port.EventPublisher;
import com.pagely.meetingservice.meeting.domain.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

// Kafka를 이용해 이벤트를 발행하는 구현체
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private static final String MEETING_EVENT_TOPIC = "meeting-event";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(BaseEvent event) {
        // domainId를 메시지 키로 사용하여 같은 도메인 이벤트가 같은 파티션으로 갈 수 있도록 한다.
        kafkaTemplate.send(
                MEETING_EVENT_TOPIC,
                event.getDomainId(),
                event
        );
    }
}
package com.pagely.meetingservice.meeting.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "p_processed_event")
public class ProcessedEvent {

    @Id
    private UUID id;

    @Column(name = "consumer_name", nullable = false, length = 100)
    private String consumerName;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    protected ProcessedEvent() {}

    private ProcessedEvent(UUID id, String consumerName, String eventId, LocalDateTime processedAt){
        this.id = id;
        this.consumerName = consumerName;
        this.eventId = eventId;
        this.processedAt = processedAt;
    }

    private static ProcessedEvent of(String consumerName, String eventId) {
        return new ProcessedEvent(UUID.randomUUID(), consumerName, eventId, LocalDateTime.now());
    }
    
}

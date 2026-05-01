package com.pagely.meetingservice.meeting.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

// Kafka Consumer 이벤트 중복 처리 방지용 엔티티
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

    public static ProcessedEvent of(String consumerName, String eventId) {
        return new ProcessedEvent(UUID.randomUUID(), consumerName, eventId, LocalDateTime.now());
    }

    public UUID getId(){
        return id;
    }

    public String getConsumerName(){
        return consumerName;
    }

    public String getEventId(){
        return eventId;
    }

    public LocalDateTime getProcessedAt(){
        return processedAt;
    }
    
}

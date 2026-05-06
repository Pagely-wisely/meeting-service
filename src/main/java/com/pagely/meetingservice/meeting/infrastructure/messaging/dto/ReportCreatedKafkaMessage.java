package com.pagely.meetingservice.meeting.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

// report-service에서 발행한 ReportCreatedEvent Kafka 메시지 DTO
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReportCreatedKafkaMessage(
        String eventId,
        String eventType,
        String domainType,
        String domainId,
        Instant occurredAt,
        Payload payload
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            String bookId,
            String bookName,
            String bookCategory,
            String bookAuthors,
            UUID userId,
            UUID reportId,
            String reportContent,
            UUID meetingId,
            UUID meetingScheduleId,
            LocalDateTime createdAt
    ) {
    }
}

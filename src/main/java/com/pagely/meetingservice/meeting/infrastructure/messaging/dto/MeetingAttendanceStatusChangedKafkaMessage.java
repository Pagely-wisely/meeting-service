package com.pagely.meetingservice.meeting.infrastructure.messaging.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;

// kafka JSON 매핑 레코드 
@JsonIgnoreProperties(ignoreUnknown = true)
public record MeetingAttendanceStatusChangedKafkaMessage(
    String eventId,
    String eventType,
    String domainType,
    String domainId,
    Instant occurredAt,
    Payload payload
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
        UUID attendanceId,
        UUID meetingId,
        UUID scheduleId,
        UUID userId,
        AttendanceStatus status,
        LocalDateTime checkedAt,
        String note
    ){}
}

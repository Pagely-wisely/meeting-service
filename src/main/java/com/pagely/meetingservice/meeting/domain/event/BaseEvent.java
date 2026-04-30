package com.pagely.meetingservice.meeting.domain.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public abstract class BaseEvent {

    private final String eventId;
    private final String eventType;
    private final String domainType;
    private final String domainId;
    private final Instant occurredAt;
    private final Object payload;

    protected BaseEvent(String domainType, UUID domainId, Object payload) {
        this(
                domainType,
                Objects.requireNonNull(domainId, "domainId must not be null").toString(),
                payload
        );
    }

    protected BaseEvent(String domainType, String domainId, Object payload) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = this.getClass().getSimpleName();
        this.domainType = Objects.requireNonNull(domainType, "domainType must not be null");
        this.domainId = Objects.requireNonNull(domainId, "domainId must not be null");
        this.occurredAt = Instant.now();
        this.payload = Objects.requireNonNull(payload, "payload must not be null");
    }
}
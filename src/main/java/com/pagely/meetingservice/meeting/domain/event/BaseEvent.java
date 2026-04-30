package com.pagely.meetingservice.meeting.domain.event;

import java.time.Instant;
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
        this(domainType, domainId == null ? null : domainId.toString(), payload);
    }

    protected BaseEvent(String domainType, Object payload) {
        this(domainType, (String) null, payload);
    }

    protected BaseEvent(String domainType, String domainId, Object payload) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = this.getClass().getSimpleName();
        this.domainType = domainType;
        this.domainId = domainId;
        this.occurredAt = Instant.now();
        this.payload = payload;
    }
}
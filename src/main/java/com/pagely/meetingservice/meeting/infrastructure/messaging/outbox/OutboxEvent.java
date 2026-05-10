package com.pagely.meetingservice.meeting.infrastructure.messaging.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "p_outbox")
public class OutboxEvent {

    @Id
    private UUID id;

    // 이벤트가 발생한 도메인 종류
    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    // 이벤트가 발생한 도메인 ID
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    // 이벤트 타입
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    // Kafka 발행 대상 토픽
    @Column(name = "topic", nullable = false, length = 100)
    private String topic;

    // JSON 직렬화된 이벤트 payload
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "JSONB")
    private String payload;

    // 발행 성공 여부
    @Column(name = "published", nullable = false)
    private boolean published;

    // 발행 성공 시각
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // 발행 실패 횟수
    @Column(name = "failure_count", nullable = false)
    private int failureCount;

    // 마지막 실패 시각
    @Column(name = "last_failure_at")
    private LocalDateTime lastFailureAt;

    // 마지막 실패 메시지
    @Column(name = "last_failure_message", columnDefinition = "TEXT")
    private String lastFailureMessage;

    // Outbox 저장 시각
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected OutboxEvent() {
    }

    // Outbox 이벤트 생성
    public static OutboxEvent of(
            UUID id,
            String aggregateType,
            UUID aggregateId,
            String eventType,
            String topic,
            String payload
    ) {
        validate(id, aggregateType, aggregateId, eventType, topic, payload);

        OutboxEvent event = new OutboxEvent();
        event.id = id;
        event.aggregateType = aggregateType;
        event.aggregateId = aggregateId;
        event.eventType = eventType;
        event.topic = topic;
        event.payload = payload;
        event.published = false;
        event.failureCount = 0;
        event.createdAt = LocalDateTime.now();
        return event;
    }

    // 발행 성공 처리
    public void markPublished() {
        this.published = true;
        this.publishedAt = LocalDateTime.now();
    }

    // 발행 실패 기록
    public void recordFailure(String message) {
        this.failureCount++;
        this.lastFailureAt = LocalDateTime.now();
        this.lastFailureMessage = truncate(message);
    }

    private static void validate(
            UUID id,
            String aggregateType,
            UUID aggregateId,
            String eventType,
            String topic,
            String payload
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id는 필수입니다.");
        }
        if (aggregateType == null || aggregateType.isBlank()) {
            throw new IllegalArgumentException("aggregateType은 필수입니다.");
        }
        if (aggregateId == null) {
            throw new IllegalArgumentException("aggregateId는 필수입니다.");
        }
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType은 필수입니다.");
        }
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("topic은 필수입니다.");
        }
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("payload는 필수입니다.");
        }
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }

        // 실패 메시지가 너무 길면 DB 저장 실패를 막기 위해 자른다.
        return message.length() > 2000 ? message.substring(0, 2000) : message;
    }
}

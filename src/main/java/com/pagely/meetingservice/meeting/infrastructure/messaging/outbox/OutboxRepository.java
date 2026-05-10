package com.pagely.meetingservice.meeting.infrastructure.messaging.outbox;

import java.util.List;
import org.springframework.data.domain.Pageable;

// Outbox 저장소 포트
public interface OutboxRepository {

    // Outbox 이벤트 저장
    OutboxEvent save(OutboxEvent event);

    // 미발행 이벤트 조회
    List<OutboxEvent> findUnpublished(Pageable pageable);

    // 실패 횟수 기준 이상 이벤트 조회
    List<OutboxEvent> findFailedExceedingThreshold(int threshold, Pageable pageable);
}

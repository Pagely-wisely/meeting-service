package com.pagely.meetingservice.meeting.infrastructure.messaging.outbox;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

// Outbox 저장소 포트
public interface OutboxRepository {

    // Outbox 이벤트 저장
    OutboxEvent save(OutboxEvent event);

    // ID로 Outbox 이벤트 조회
    Optional<OutboxEvent> findById(UUID id);

    // 발행 대상 이벤트 조회
    List<OutboxEvent> findPublishCandidates(Pageable pageable);

    // 실패 횟수 기준 이상 이벤트 조회
    List<OutboxEvent> findFailedExceedingThreshold(int threshold, Pageable pageable);
}

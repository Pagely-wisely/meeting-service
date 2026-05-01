package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.ProcessedEvent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// ProcessedEvent 전용 JPA Repository
public interface JpaProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

    // Consumer 이름과 BaseEvent eventId 기준으로 기존 처리 여부 조회
    Optional<ProcessedEvent> findByConsumerNameAndEventId(
            String consumerName,
            String eventId
    );

    // 중복 처리 여부 확인
    boolean existsByConsumerNameAndEventId(
            String consumerName,
            String eventId
    );
}
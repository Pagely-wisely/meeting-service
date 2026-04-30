package com.pagely.meetingservice.meeting.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pagely.meetingservice.meeting.domain.model.ProcessedEvent;

// ProcessedEvent 전용 JPA Repository
public interface JpaProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID>{

    // consumerName과 eventId로 기존 처리 여부를 조회
    Optional<ProcessedEvent> findByConsumerNameAndEventId(String consumerName, String eventId);
}

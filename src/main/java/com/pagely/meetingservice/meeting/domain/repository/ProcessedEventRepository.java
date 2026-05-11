package com.pagely.meetingservice.meeting.domain.repository;

import com.pagely.meetingservice.meeting.domain.model.ProcessedEvent;

// Kafka Consumer 처리 완료 이벤트 저장소
public interface ProcessedEventRepository {

    // 이미 처리한 이벤트인지 확인
    boolean existsByConsumerNameAndEventId(String consumerName, String eventId);

    // 처리 완료 이벤트 저장
    ProcessedEvent save(ProcessedEvent processedEvent);
}

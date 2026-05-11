package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.ProcessedEvent;
import com.pagely.meetingservice.meeting.domain.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// ProcessedEvent Repository 구현체
@Repository
@RequiredArgsConstructor
public class ProcessedEventRepositoryAdapter implements ProcessedEventRepository {

    private final JpaProcessedEventRepository jpaProcessedEventRepository;

    @Override
    public boolean existsByConsumerNameAndEventId(String consumerName, String eventId) {
        return jpaProcessedEventRepository.existsByConsumerNameAndEventId(consumerName, eventId);
    }

    @Override
    public ProcessedEvent save(ProcessedEvent processedEvent) {
        return jpaProcessedEventRepository.save(processedEvent);
    }
}

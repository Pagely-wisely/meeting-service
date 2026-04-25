package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// domain repository의 실제 구현체
@Repository
@RequiredArgsConstructor
public class MeetingRepositoryImpl implements MeetingRepository {

    private final MeetingJpaRepository meetingJpaRepository;

    @Override
    public Meeting save(Meeting meeting) {
        return meetingJpaRepository.save(meeting);
    }

    @Override
    public Optional<Meeting> findById(UUID meetingId) {
        return meetingJpaRepository.findByIdAndDeletedAtIsNull(meetingId);
    }

    @Override
    public List<Meeting> findAll() {
        return meetingJpaRepository.findByDeletedAtIsNull();
    }

    @Override
    public List<Meeting> findByHostId(UUID hostId) {
        return meetingJpaRepository.findAll()
                .stream()
                .filter(m -> m.getHostId().equals(hostId))
                .toList();
    }

    @Override
    public boolean existsById(UUID meetingId) {
        return meetingJpaRepository.existsById(meetingId);
    }
}
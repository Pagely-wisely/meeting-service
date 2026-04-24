package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class MeetingRepositoryAdapter implements MeetingRepository {

    private final JpaMeetingRepository jpaMeetingRepository;

    public MeetingRepositoryAdapter(JpaMeetingRepository jpaMeetingRepository) {
        this.jpaMeetingRepository = jpaMeetingRepository;
    }

    @Override
    public Meeting save(Meeting meeting) {
        return jpaMeetingRepository.save(meeting);
    }

    @Override
    public Optional<Meeting> findById(UUID meetingId) {
        return jpaMeetingRepository.findById(meetingId);
    }

    @Override
    public List<Meeting> findAll() {  // 조회 페이징 API 구현 때 수정
        return jpaMeetingRepository.findAll();
    }

    @Override
    public List<Meeting> findByHostId(UUID hostId) {
        return jpaMeetingRepository.findByHostId(hostId);
    }

    @Override
    public boolean existsById(UUID meetingId) {
        return jpaMeetingRepository.existsById(meetingId);
    }
}

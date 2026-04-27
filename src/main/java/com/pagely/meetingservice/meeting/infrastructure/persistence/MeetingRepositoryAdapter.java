package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// 도메인 Repository 인터페이스의 실제 JPA 구현체
@Repository
@RequiredArgsConstructor
public class MeetingRepositoryAdapter implements MeetingRepository {

    private final MeetingJpaRepository meetingJpaRepository;

    // 모임 저장
    @Override
    public Meeting save(Meeting meeting) {
        return meetingJpaRepository.save(meeting);
    }

    // 삭제되지 않은 모임 단건 조회
    @Override
    public Optional<Meeting> findById(UUID meetingId) {
        return meetingJpaRepository.findByIdAndDeletedAtIsNull(meetingId);
    }

    // 삭제되지 않은 전체 모임 조회
    @Override
    public List<Meeting> findAll() {
        return meetingJpaRepository.findByDeletedAtIsNull();
    }

    // 모임장 기준 삭제되지 않은 모임 조회
    @Override
    public List<Meeting> findByHostId(UUID hostId) {
        return meetingJpaRepository.findByHostIdAndDeletedAtIsNull(hostId);
    }

    // 삭제되지 않은 모임 존재 여부 확인
    @Override
    public boolean existsById(UUID meetingId) {
        return meetingJpaRepository.existsByIdAndDeletedAtIsNull(meetingId);
    }
}
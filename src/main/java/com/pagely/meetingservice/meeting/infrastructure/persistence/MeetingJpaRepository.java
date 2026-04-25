package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// JPA를 이용한 실제 DB 접근 레포지토리
public interface MeetingJpaRepository extends JpaRepository<Meeting, UUID> {

    // 삭제되지 않은 모임 전체 조회
    List<Meeting> findByDeletedAtIsNull();

    // 삭제되지 않은 특정 모임 조회
    java.util.Optional<Meeting> findByIdAndDeletedAtIsNull(UUID meetingId);
}
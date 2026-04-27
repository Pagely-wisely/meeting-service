package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA 전용 레포지토리
public interface JpaMeetingRepository extends JpaRepository<Meeting, UUID> {

    // 삭제되지 않은 모임 전체 조회
    List<Meeting> findByDeletedAtIsNull();

    // ID로 삭제되지 않은 모임 조회
    Optional<Meeting> findByIdAndDeletedAtIsNull(UUID meetingId);

    // 모임장 기준 삭제되지 않은 모임 조회
    List<Meeting> findByHostIdAndDeletedAtIsNull(UUID hostId);

    // 삭제되지 않은 모임 존재 여부 확인
    boolean existsByIdAndDeletedAtIsNull(UUID meetingId);
}
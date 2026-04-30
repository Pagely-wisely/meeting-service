package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.Meeting;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Spring Data JPA 전용 레포지토리
public interface JpaMeetingRepository extends JpaRepository<Meeting, UUID> {

    // 삭제되지 않은 모임 전체 조회 - 페이징
    Page<Meeting> findByDeletedAtIsNull(Pageable pageable);

    // ID로 삭제되지 않은 모임 조회
    Optional<Meeting> findByIdAndDeletedAtIsNull(UUID meetingId);

    // 모임장 기준 삭제되지 않은 모임 조회
    List<Meeting> findByHostIdAndDeletedAtIsNull(UUID hostId);

    // 삭제되지 않은 모임 존재 여부 확인
    boolean existsByIdAndDeletedAtIsNull(UUID meetingId);

    // 삭제되지 않은 모임 존재 여부 확인(쓰기 락)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Meeting m where m.id = :meetingId and m.deletedAt is null")
    Optional<Meeting> findByIdForUpdate(@Param("meetingId") UUID meetingId);
}
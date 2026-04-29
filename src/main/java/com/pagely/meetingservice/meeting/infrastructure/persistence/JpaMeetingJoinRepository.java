package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.MeetingJoin;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoinStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// 모임 가입 신청 JPA Repository
public interface JpaMeetingJoinRepository extends JpaRepository<MeetingJoin, UUID> {

    // 모임 ID 기준 가입 신청 목록 조회 - 페이징 처리
    Page<MeetingJoin> findByMeetingId(UUID meetingId, Pageable pageable);

    // 모임 ID + 가입 신청 상태 기준 목록 조회 - 페이징 처리
    Page<MeetingJoin> findByMeetingIdAndJoinStatus(
            UUID meetingId,
            MeetingJoinStatus joinStatus,
            Pageable pageable
    );

    // 모임 ID + 유저 ID 기준 단건 조회
    Optional<MeetingJoin> findByMeetingIdAndRecruitUserId(UUID meetingId, UUID recruitUserId);

    // 모임 ID + 유저 ID 기준 존재 여부 조회
    boolean existsByMeetingIdAndRecruitUserId(UUID meetingId, UUID recruitUserId);
}
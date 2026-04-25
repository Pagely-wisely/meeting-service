package com.pagely.meetingservice.meeting.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pagely.meetingservice.meeting.domain.model.MeetingJoin;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoinStatus;

public interface JpaMeetingJoinRepository extends JpaRepository<MeetingJoin, UUID> {
    
    List<MeetingJoin> findByMeetingId(UUID meetingId); // 모임 ID 기준 목록 조회

    List<MeetingJoin> findByMeetingIdAndJoinStatus(UUID meetingId, MeetingJoinStatus joinStatus); // 모임+상태 기준 목록 조회

    Optional<MeetingJoin> findByMeetingIdAndRecruitUserId(UUID meetingId, UUID recruitUserId); // 모임+유저 기준 단건 조회

    boolean existsByMeetingIdAndRecruitUserId(UUID meetingId, UUID recruitUserId); // 모임+유저 기준 존재 여부 조회
    
}

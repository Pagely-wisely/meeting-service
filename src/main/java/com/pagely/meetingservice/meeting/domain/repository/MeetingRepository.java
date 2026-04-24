package com.pagely.meetingservice.meeting.domain.repository;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 모임 저장/조회 인터페이스
public interface MeetingRepository {

    // 모임 저장
    Meeting save(Meeting meeting);

    // ID로 모임 조회
    Optional<Meeting> findById(UUID meetingId);

    // 전체 모임 조회
    List<Meeting> findAll();

    // 모임장 기준 모임 조회
    List<Meeting> findByHostId(UUID hostId);

    // 모임 존재 여부 확인
    boolean existsById(UUID meetingId);
    
}
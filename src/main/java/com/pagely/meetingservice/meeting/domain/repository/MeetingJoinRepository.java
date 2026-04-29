package com.pagely.meetingservice.meeting.domain.repository;

import com.pagely.meetingservice.meeting.domain.model.MeetingJoin;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoinStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// 모임 가입 신청 저장/조회 인터페이스
public interface MeetingJoinRepository {

    // 가입 신청 저장
    MeetingJoin save(MeetingJoin meetingJoin);

    // ID로 가입 신청 조회
    Optional<MeetingJoin> findById(UUID joinId);

    // 특정 모임의 전체 가입 신청 조회 - 페이징 처리
    Page<MeetingJoin> findByMeetingId(UUID meetingId, Pageable pageable);

    // 특정 모임의 상태별 가입 신청 조회 - 페이징 처리
    Page<MeetingJoin> findByMeetingIdAndJoinStatus(
            UUID meetingId,
            MeetingJoinStatus joinStatus,
            Pageable pageable
    );

    // 특정 모임 + 특정 유저의 신청 조회
    Optional<MeetingJoin> findByMeetingIdAndRecruitUserId(UUID meetingId, UUID recruitUserId);

    // 이미 가입 신청했는지 확인
    boolean existsByMeetingIdAndRecruitUserId(UUID meetingId, UUID recruitUserId);
}
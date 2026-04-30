package com.pagely.meetingservice.meeting.domain.repository;

import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberRole;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 모임 멤버 저장/조회 인터페이스
public interface MeetingMemberRepository {

    // 멤버 저장
    MeetingMember save(MeetingMember meetingMember);

    // ID로 멤버 조회
    Optional<MeetingMember> findById(UUID memberId);

    // 모임과 유저 기준 멤버 조회
    Optional<MeetingMember> findByMeetingIdAndUserId(UUID meetingId, UUID userId);

    // 특정 모임의 전체 멤버 조회
    List<MeetingMember> findByMeetingId(UUID meetingId);

    // 특정 모임의 상태별 멤버 조회
    List<MeetingMember> findByMeetingIdAndStatus(UUID meetingId, MeetingMemberStatus status);

    // 특정 모임의 역할별 멤버 조회
    List<MeetingMember> findByMeetingIdAndRole(UUID meetingId, MeetingMemberRole role);

    // 특정 모임의 활성 멤버 수 조회
    long countByMeetingIdAndStatus(UUID meetingId, MeetingMemberStatus status);

    // 특정 유저가 해당 모임 멤버인지 확인
    boolean existsByMeetingIdAndUserId(UUID meetingId, UUID userId);

    // 특정 유저가 속한 모임의 id 목록 조회
    List<UUID> findMeetingIdsByUserIdAndStatuses(
            UUID userId,
            List<MeetingMemberStatus> statuses
    );

}
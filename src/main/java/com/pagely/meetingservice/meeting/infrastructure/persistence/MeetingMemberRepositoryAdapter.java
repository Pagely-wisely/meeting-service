package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberRole;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// 모임 멤버 Repository Adapter
@Repository
@RequiredArgsConstructor
public class MeetingMemberRepositoryAdapter implements MeetingMemberRepository {

    private final MeetingMemberJpaRepository meetingMemberJpaRepository;

    // 멤버 저장
    @Override
    public MeetingMember save(MeetingMember meetingMember) {
        return meetingMemberJpaRepository.save(meetingMember);
    }

    // ID로 멤버 조회
    @Override
    public Optional<MeetingMember> findById(UUID memberId) {
        return meetingMemberJpaRepository.findById(memberId);
    }

    // 모임과 유저 기준 멤버 조회
    @Override
    public Optional<MeetingMember> findByMeetingIdAndUserId(UUID meetingId, UUID userId) {
        return meetingMemberJpaRepository.findByMeetingIdAndUserIdAndDeletedAtIsNull(
                meetingId,
                userId
        );
    }

    // 특정 모임의 전체 멤버 조회
    @Override
    public List<MeetingMember> findByMeetingId(UUID meetingId) {
        return meetingMemberJpaRepository.findAllByMeetingIdAndDeletedAtIsNull(meetingId);
    }

    // 특정 모임의 상태별 멤버 조회
    @Override
    public List<MeetingMember> findByMeetingIdAndStatus(
            UUID meetingId,
            MeetingMemberStatus status
    ) {
        return meetingMemberJpaRepository.findAllByMeetingIdAndStatusAndDeletedAtIsNull(
                meetingId,
                status
        );
    }

    // 특정 모임의 역할별 멤버 조회
    @Override
    public List<MeetingMember> findByMeetingIdAndRole(
            UUID meetingId,
            MeetingMemberRole role
    ) {
        return meetingMemberJpaRepository.findAllByMeetingIdAndRoleAndDeletedAtIsNull(
                meetingId,
                role
        );
    }

    // 특정 모임의 활성 멤버 수 조회
    @Override
    public long countByMeetingIdAndStatus(UUID meetingId, MeetingMemberStatus status) {
        return meetingMemberJpaRepository.countByMeetingIdAndStatusAndDeletedAtIsNull(
                meetingId,
                status
        );
    }

    // 특정 유저가 해당 모임의 활성 멤버인지 확인
    @Override
    public boolean existsByMeetingIdAndUserId(UUID meetingId, UUID userId) {
        return meetingMemberJpaRepository.existsByMeetingIdAndUserIdAndStatusAndDeletedAtIsNull(
                meetingId,
                userId,
                MeetingMemberStatus.ACTIVE
        );
    }
}
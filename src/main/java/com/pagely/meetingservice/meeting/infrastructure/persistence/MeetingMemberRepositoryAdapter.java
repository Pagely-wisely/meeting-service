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

    private final JpaMeetingMemberRepository jpaMeetingMemberRepository;

    // 멤버 저장
    @Override
    public MeetingMember save(MeetingMember meetingMember) {
        return jpaMeetingMemberRepository.save(meetingMember);
    }

    // ID로 멤버 조회
    @Override
    public Optional<MeetingMember> findById(UUID memberId) {
        return jpaMeetingMemberRepository.findById(memberId);
    }

    // 모임과 유저 기준 멤버 조회
    @Override
    public Optional<MeetingMember> findByMeetingIdAndUserId(UUID meetingId, UUID userId) {
        return jpaMeetingMemberRepository.findByMeetingIdAndUserIdAndDeletedAtIsNull(
                meetingId,
                userId
        );
    }

    // 특정 모임의 전체 멤버 조회
    @Override
    public List<MeetingMember> findByMeetingId(UUID meetingId) {
        return jpaMeetingMemberRepository.findAllByMeetingIdAndDeletedAtIsNull(meetingId);
    }

    // 특정 모임의 상태별 멤버 조회
    @Override
    public List<MeetingMember> findByMeetingIdAndStatus(
            UUID meetingId,
            MeetingMemberStatus status
    ) {
        return jpaMeetingMemberRepository.findAllByMeetingIdAndStatusAndDeletedAtIsNull(
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
        return jpaMeetingMemberRepository.findAllByMeetingIdAndRoleAndDeletedAtIsNull(
                meetingId,
                role
        );
    }

    // 특정 모임의 활성 멤버 수 조회
    @Override
    public long countByMeetingIdAndStatus(UUID meetingId, MeetingMemberStatus status) {
        return jpaMeetingMemberRepository.countByMeetingIdAndStatusAndDeletedAtIsNull(
                meetingId,
                status
        );
    }

    // 특정 유저가 해당 모임의 활성 멤버인지 확인
    @Override
    public boolean existsByMeetingIdAndUserId(UUID meetingId, UUID userId) {
        return jpaMeetingMemberRepository.existsByMeetingIdAndUserIdAndStatusAndDeletedAtIsNull(
                meetingId,
                userId,
                MeetingMemberStatus.ACTIVE
        );
    }

    // 특정 유저가 속한 모임의 id 목록 조회
    @Override
    public List<UUID> findMeetingIdsByUserIdAndStatuses(UUID userId, List<MeetingMemberStatus> statuses) {
        return jpaMeetingMemberRepository.findDistinctMeetingIdsByUserIdAndStatusesAndDeletedAtIsNull(
                userId,
                statuses
        );
    }

    @Override
    public Optional<MeetingMember> findByMeetingIdAndUserIdForUpdate(UUID meetingId, UUID userId){
        return jpaMeetingMemberRepository.findByMeetingIdAndUserIdForUpdate(meetingId, userId);
    }
}
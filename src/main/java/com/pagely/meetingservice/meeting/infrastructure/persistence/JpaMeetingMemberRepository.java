package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberRole;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 모임 멤버 JPA Repository
public interface JpaMeetingMemberRepository extends JpaRepository<MeetingMember, UUID> {

    List<MeetingMember> findByMeetingId(UUID meetingId); // 모임 전체 멤버

    // 모임과 유저 기준 멤버 조회
    Optional<MeetingMember> findByMeetingIdAndUserIdAndDeletedAtIsNull(UUID meetingId, UUID userId);

    // 특정 모임의 전체 멤버 조회
    List<MeetingMember> findAllByMeetingIdAndDeletedAtIsNull(UUID meetingId);

    // 특정 모임의 상태별 멤버 조회
    List<MeetingMember> findAllByMeetingIdAndStatusAndDeletedAtIsNull(
            UUID meetingId,
            MeetingMemberStatus status
    );

    // 특정 모임의 역할별 멤버 조회
    List<MeetingMember> findAllByMeetingIdAndRoleAndDeletedAtIsNull(
            UUID meetingId,
            MeetingMemberRole role
    );

    // 특정 모임의 활성 멤버 수 조회
    long countByMeetingIdAndStatusAndDeletedAtIsNull(
            UUID meetingId,
            MeetingMemberStatus status
    );

    // 특정 유저가 해당 모임의 활성 멤버인지 확인
    boolean existsByMeetingIdAndUserIdAndStatusAndDeletedAtIsNull(
            UUID meetingId,
            UUID userId,
            MeetingMemberStatus status
    );

    // 유저가 속한 모임의 id 목록 조회
    @Query("select distinct m.meetingId from MeetingMember m where m.userId = :userId and m.status in :statuses and m.deletedAt is null")
    List<UUID> findDistinctMeetingIdsByUserIdAndStatusesAndDeletedAtIsNull(
            @Param("userId") UUID userId,
            @Param("statuses") List<MeetingMemberStatus> statuses
    );
}
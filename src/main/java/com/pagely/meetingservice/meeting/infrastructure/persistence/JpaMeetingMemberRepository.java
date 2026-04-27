package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberRole;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMeetingMemberRepository extends JpaRepository<MeetingMember, UUID> {

    Optional<MeetingMember> findByMeetingIdAndUserId(UUID meetingId, UUID userId); // 모임+유저 단건

    List<MeetingMember> findByMeetingId(UUID meetingId); // 모임 전체 멤버

    List<MeetingMember> findByMeetingIdAndStatus(UUID meetingId, MeetingMemberStatus status); // 상태별

    List<MeetingMember> findByMeetingIdAndRole(UUID meetingId, MeetingMemberRole role); // 역할별

    long countByMeetingIdAndStatus(UUID meetingId, MeetingMemberStatus status); // 정원 계산용 ACTIVE 카운트

    boolean existsByMeetingIdAndUserId(UUID meetingId, UUID userId); // 이미 멤버인지

}

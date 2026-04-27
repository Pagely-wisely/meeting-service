package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberRole;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MeetingMemberRepositoryAdapter implements MeetingMemberRepository {

    private final JpaMeetingMemberRepository jpaMeetingMemberRepository;

    public MeetingMemberRepositoryAdapter(JpaMeetingMemberRepository jpaMeetingMemberRepository) {
        this.jpaMeetingMemberRepository = jpaMeetingMemberRepository;
    }

    @Override
    public MeetingMember save(MeetingMember meetingMember) {
        return jpaMeetingMemberRepository.save(meetingMember);
    }

    @Override
    public Optional<MeetingMember> findById(UUID memberId) {
        return jpaMeetingMemberRepository.findById(memberId);
    }

    @Override
    public Optional<MeetingMember> findByMeetingIdAndUserId(UUID meetingId, UUID userId) {
        return jpaMeetingMemberRepository.findByMeetingIdAndUserId(meetingId, userId);
    }

    @Override
    public List<MeetingMember> findByMeetingId(UUID meetingId) {
        return jpaMeetingMemberRepository.findByMeetingId(meetingId);
    }

    @Override
    public List<MeetingMember> findByMeetingIdAndStatus(UUID meetingId, MeetingMemberStatus status) {
        return jpaMeetingMemberRepository.findByMeetingIdAndStatus(meetingId, status);
    }

    @Override
    public List<MeetingMember> findByMeetingIdAndRole(UUID meetingId, MeetingMemberRole role) {
        return jpaMeetingMemberRepository.findByMeetingIdAndRole(meetingId, role);
    }

    @Override
    public long countByMeetingIdAndStatus(UUID meetingId, MeetingMemberStatus status) {
        return jpaMeetingMemberRepository.countByMeetingIdAndStatus(meetingId, status);
    }

    @Override
    public boolean existsByMeetingIdAndUserId(UUID meetingId, UUID userId) {
        return jpaMeetingMemberRepository.existsByMeetingIdAndUserId(meetingId, userId);
    }

}

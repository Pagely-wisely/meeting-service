package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.MeetingJoin;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoinStatus;
import com.pagely.meetingservice.meeting.domain.repository.MeetingJoinRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class MeetingJoinRepositoryAdapter implements MeetingJoinRepository {

    private final JpaMeetingJoinRepository jpaMeetingJoinRepository;

    public MeetingJoinRepositoryAdapter(JpaMeetingJoinRepository jpaMeetingJoinRepository) {
        this.jpaMeetingJoinRepository = jpaMeetingJoinRepository;
    }


    @Override
    public MeetingJoin save(MeetingJoin meetingJoin) { // 저장 메서드
        return jpaMeetingJoinRepository.save(meetingJoin);
    }

    @Override
    public Optional<MeetingJoin> findById(UUID joinId) { // Id 조회 메서드
        return jpaMeetingJoinRepository.findById(joinId);
    }

    // 모임 기준 가입 신청 목록 조회 - 페이징 처리
    @Override
    public Page<MeetingJoin> findByMeetingId(UUID meetingId, Pageable pageable) {
        return jpaMeetingJoinRepository.findByMeetingId(meetingId, pageable);
    }

    // 모임 + 상태 기준 가입 신청 목록 조회 - 페이징 처리
    @Override
    public Page<MeetingJoin> findByMeetingIdAndJoinStatus(
            UUID meetingId,
            MeetingJoinStatus joinStatus,
            Pageable pageable
    ) {
        return jpaMeetingJoinRepository.findByMeetingIdAndJoinStatus(
                meetingId,
                joinStatus,
                pageable
        );
    }

    @Override
    public Optional<MeetingJoin> findByMeetingIdAndRecruitUserId(UUID meetingId,
                                                                 UUID recruitUserId) { // 모임+유저 단건 조회 메서드
        return jpaMeetingJoinRepository.findByMeetingIdAndRecruitUserId(meetingId, recruitUserId);
    }

    @Override
    public boolean existsByMeetingIdAndRecruitUserId(UUID meetingId, UUID recruitUserId) { // 모임+유저 존재 여부 메서드
        return jpaMeetingJoinRepository.existsByMeetingIdAndRecruitUserId(meetingId, recruitUserId);
    }

}

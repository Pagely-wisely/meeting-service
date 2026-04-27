package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.MeetingJoin;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoinStatus;
import com.pagely.meetingservice.meeting.domain.repository.MeetingJoinRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    @Override
    public List<MeetingJoin> findByMeetingId(UUID meetingId) { // 모임 기준 목록 조회 메서드
        return jpaMeetingJoinRepository.findByMeetingId(meetingId);
    }

    @Override
    public List<MeetingJoin> findByMeetingIdAndJoinStatus(UUID meetingId,
                                                          MeetingJoinStatus joinStatus) { // 모임+상태 목록 조회 메서드
        return jpaMeetingJoinRepository.findByMeetingIdAndJoinStatus(meetingId, joinStatus);
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

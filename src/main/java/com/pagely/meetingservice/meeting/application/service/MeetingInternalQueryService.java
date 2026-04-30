package com.pagely.meetingservice.meeting.application.service;

import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingInternalQueryService {

    // 조회를 허용할 멤버 상태
    private static final List<MeetingMemberStatus> READABLE_STATUSES = List.of(
            MeetingMemberStatus.ACTIVE,
            MeetingMemberStatus.LEFT,
            MeetingMemberStatus.REMOVED,
            MeetingMemberStatus.EXPELLED
    );

    private final MeetingMemberRepository meetingMemberRepository;


    // 특정 유저가 조회할 수 있는 모임의 id 목록 반환
    public List<UUID> getReadableMeetingIds(UUID userId) {
        return meetingMemberRepository.findMeetingIdsByUserIdAndStatuses(
                userId,
                READABLE_STATUSES);
    }

}

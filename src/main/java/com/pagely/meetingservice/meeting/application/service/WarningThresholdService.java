package com.pagely.meetingservice.meeting.application.service;

import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
import com.pagely.meetingservice.meeting.domain.model.ProcessedEvent;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import com.pagely.meetingservice.meeting.infrastructure.persistence.JpaProcessedEventRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WarningThresholdService {

    private static final int WARNING_EXPEL_THRESHOLD = 3; // 경고 임계치 3회
    private static final UUID SYSTEM_ACTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000000"); // 시스템 감사용 UUID

    private final MeetingMemberRepository meetingMemberRepository;

    @Transactional
    public void handleAttendanceStatusChanged(String eventId, UUID meetingId, UUID userId) {

        MeetingMember member = meetingMemberRepository.findByMeetingIdAndUserIdForUpdate(meetingId, userId).orElse(null);
        if (member == null) {
            return;
        }
        if (member.getStatus() != MeetingMemberStatus.ACTIVE) { // 활성 상태의 멤버인지 검증
            return;
        }
        if (member.getWarningCount() < WARNING_EXPEL_THRESHOLD) { // 경고 횟수가 임계치 미만인지 검증
            return;
        }

        member.expelBySystem(SYSTEM_ACTOR_ID); // 시스템 감사용 UUID로 멤버 강퇴 처리
        meetingMemberRepository.save(member);
    }
}
package com.pagely.meetingservice.meeting.application.service;

import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingResult;
import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberRole;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 모임 명령 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingCommandService {

    private final MeetingRepository meetingRepository;
    private final MeetingMemberRepository meetingMemberRepository;

    // 모임 생성
    @Transactional
    public MeetingResult createMeeting(CreateMeetingCommand command) {
        UUID meetingId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // 모임 엔티티 생성
        Meeting meeting = command.toMeeting(meetingId, now);

        // 모임 저장
        Meeting saved = meetingRepository.save(meeting);

        // 모임 생성자는 자동으로 모임장 멤버로 등록한다.
        MeetingMember hostMember = MeetingMember.create(
                UUID.randomUUID(),
                saved.getId(),
                saved.getHostId(),
                MeetingMemberRole.HOST,
                MeetingMemberStatus.ACTIVE,
                now,
                saved.getHostId()
        );

        // 모임장 멤버 저장
        meetingMemberRepository.save(hostMember);

        return MeetingResult.from(saved);
    }
}
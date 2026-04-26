package com.pagely.meetingservice.meeting.application.service;

import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingResult;
import com.pagely.meetingservice.meeting.domain.model.Meeting;
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

    // 모임 생성
    @Transactional
    public MeetingResult createMeeting(CreateMeetingCommand command) {
        UUID meetingId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Meeting meeting = command.toMeeting(meetingId, now);

        Meeting saved = meetingRepository.save(meeting);
        return MeetingResult.from(saved);
    }
}
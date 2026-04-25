package com.pagely.meetingservice.meeting.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingResult;
import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;


@Service
@Transactional(readOnly = true)
public class MeetingService {

    private final MeetingRepository meetingRepository;

    public MeetingService(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    @Transactional
    public MeetingResult createMeeting(CreateMeetingCommand command) {
        UUID meetingId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Meeting meeting = Meeting.create(
            meetingId,
            command.hostId(),
            command.bookId(),
            command.title(),
            command.description(),
            command.meetingType(),
            command.recruitStartAt(),
            command.recruitEndAt(),
            command.recruitMax(),
            command.readingLevel(),
            command.ruleMemo(),
            command.recruitRate(),
            command.freePaid(),
            now,
            command.createdBy()
        );
        Meeting saved = meetingRepository.save(meeting);
        return MeetingResult.from(saved);
    }
    
}

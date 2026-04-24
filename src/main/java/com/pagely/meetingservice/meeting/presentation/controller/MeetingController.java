package com.pagely.meetingservice.meeting.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingResult;
import com.pagely.meetingservice.meeting.application.service.MeetingApplicationService;
import com.pagely.meetingservice.meeting.presentation.dto.request.CreateMeetingRequest;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingResponse;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/meetings")
public class MeetingController {
    private final MeetingApplicationService meetingApplicationService;

    public MeetingController(MeetingApplicationService meetingApplicationService) {
        this.meetingApplicationService = meetingApplicationService;
    }

    @PostMapping // 모임 생성 API
    public ResponseEntity<MeetingResponse> createMeeting(@Valid @RequestBody CreateMeetingRequest req){
        CreateMeetingCommand command = new CreateMeetingCommand(
            req.hostId(),
            req.bookId(),
            req.title(),
            req.description(),
            req.meetingType(),
            req.recruitStartAt(),
            req.recruitEndAt(),
            req.recruitMax(),
            req.readingLevel(),
            req.ruleMemo(),
            req.recruitRate(),
            req.freePaid(),
            req.hostId() // createdBy 임시로 hostId 사용
        );
        MeetingResult result = meetingApplicationService.createMeeting(command);
        MeetingResponse response = MeetingResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 응답 반환
    }
    
}

package com.pagely.meetingservice.meeting.presentation.controller;

import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingResult;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingSummaryResult;
import com.pagely.meetingservice.meeting.application.service.MeetingApplicationService;
import com.pagely.meetingservice.meeting.application.service.MeetingQueryService;
import com.pagely.meetingservice.meeting.presentation.dto.request.CreateMeetingRequest;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingResponse;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingSummaryResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 모임 API 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/meetings")
public class MeetingController {

    private final MeetingApplicationService meetingApplicationService;
    private final MeetingQueryService meetingQueryService;

    // 모임 생성
    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @Valid @RequestBody CreateMeetingRequest req
    ) {
        CreateMeetingCommand command = new CreateMeetingCommand(
                // TODO: 인증 컨텍스트 연결
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
                req.hostId() // 임시: createdBy를 hostId로 사용
        );

        MeetingResult result = meetingApplicationService.createMeeting(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MeetingResponse.from(result));
    }

    // 모임 전체 조회
    @GetMapping
    public List<MeetingSummaryResponse> getMeetings() {
        List<MeetingSummaryResult> results = meetingQueryService.getMeetings();

        return results.stream()
                .map(MeetingSummaryResponse::from)
                .toList();
    }

    // 모임 상세 조회
    @GetMapping("/{meetingId}")
    public MeetingResponse getMeeting(@PathVariable UUID meetingId) {
        MeetingResult result = meetingQueryService.getMeeting(meetingId);
        return MeetingResponse.from(result);
    }
}
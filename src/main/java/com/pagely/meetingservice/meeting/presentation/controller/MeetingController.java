package com.pagely.meetingservice.meeting.presentation.controller;

import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingCommand;
import com.pagely.meetingservice.meeting.application.dto.command.JoinMeetingCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingJoinResult;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingResult;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingSummaryResult;
import com.pagely.meetingservice.meeting.application.service.MeetingService;
import com.pagely.meetingservice.meeting.application.service.MeetingJoinService;
import com.pagely.meetingservice.meeting.application.service.MeetingQueryService;
import com.pagely.meetingservice.meeting.presentation.dto.request.CreateMeetingRequest;
import com.pagely.meetingservice.meeting.presentation.dto.request.JoinMeetingRequest;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingJoinResponse;
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

    private final MeetingService meetingApplicationService;
    private final MeetingQueryService meetingQueryService;
    private final MeetingJoinService meetingJoinService;

    // 모임 생성
    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @Valid @RequestBody CreateMeetingRequest req
    ) {
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
                req.hostId() // TODO: createdBy를 hostId로 사용
        );

        MeetingResult result = meetingApplicationService.createMeeting(command);
        MeetingResponse response = MeetingResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
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

    // 모임 가입 신청
    @PostMapping("/{meetingId}/join")
    public ResponseEntity<MeetingJoinResponse> joinMeeting(
            @PathVariable UUID meetingId,
            @Valid @RequestBody JoinMeetingRequest req
    ) {
        JoinMeetingCommand command = new JoinMeetingCommand(
                meetingId,
                req.recruitUserId(),
                req.content(),
                req.recruitUserId()
        );
        MeetingJoinResult result = meetingJoinService.createMeetingJoin(command);
        MeetingJoinResponse response = MeetingJoinResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
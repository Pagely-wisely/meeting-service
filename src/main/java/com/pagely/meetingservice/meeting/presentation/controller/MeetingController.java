package com.pagely.meetingservice.meeting.presentation.controller;

import com.pagely.meetingservice.meeting.application.dto.result.MeetingJoinResult;
import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingCommand;
import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingScheduleCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingResult;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingScheduleResult;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingSummaryResult;
import com.pagely.meetingservice.meeting.application.service.MeetingJoinService;
import com.pagely.meetingservice.meeting.application.service.MeetingQueryService;
import com.pagely.meetingservice.meeting.application.service.MeetingService;
import com.pagely.meetingservice.meeting.presentation.dto.request.CreateMeetingRequest;
import com.pagely.meetingservice.meeting.presentation.dto.request.JoinMeetingRequest;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingJoinResponse;
import com.pagely.meetingservice.meeting.application.service.MeetingScheduleApplicationService;
import com.pagely.meetingservice.meeting.presentation.dto.request.CreateMeetingRequest;
import com.pagely.meetingservice.meeting.presentation.dto.request.CreateMeetingScheduleRequest;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingResponse;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingScheduleResponse;
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

    private final MeetingService meetingService;
    private final MeetingQueryService meetingQueryService;
    private final MeetingJoinService meetingJoinService;
    private final MeetingScheduleApplicationService meetingScheduleApplicationService;

    // 모임 생성
    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @Valid @RequestBody CreateMeetingRequest req) {
        MeetingResult result = meetingService.createMeeting(req.toCommand(req.getHostId()));
        MeetingResponse response = MeetingResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
        MeetingJoinResult result = meetingJoinService.createMeetingJoin(
                req.toCommand(meetingId, req.getRecruitUserId()));
        MeetingJoinResponse response = MeetingJoinResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    ;
    // 모임 일정 생성
    @PostMapping("/{meetingId}/schedules")
    public ResponseEntity<MeetingScheduleResponse> createMeetingSchedule(
            @PathVariable UUID meetingId,
            @Valid @RequestBody CreateMeetingScheduleRequest req
    ) {
        CreateMeetingScheduleCommand command = new CreateMeetingScheduleCommand(
                meetingId,
                req.bookId(),
                req.startAt(),
                req.discussionNote(),
                // TODO: 인증 컨텍스트 연결 후 현재 로그인 사용자 ID로 변경
                UUID.fromString("00000000-0000-0000-0000-000000000001")
        );

        MeetingScheduleResult result = meetingScheduleApplicationService.createSchedule(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MeetingScheduleResponse.from(result));
    }
}
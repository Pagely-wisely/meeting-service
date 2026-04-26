package com.pagely.meetingservice.meeting.presentation.controller;

import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingScheduleCommand;
import com.pagely.meetingservice.meeting.application.dto.command.UpdateAttendanceStatusCommand;
import com.pagely.meetingservice.meeting.application.dto.command.UpdateScheduleStatusCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingAttendanceResult;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingJoinResult;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingResult;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingScheduleResult;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingSummaryResult;
import com.pagely.meetingservice.meeting.application.service.MeetingAttendanceCommandService;
import com.pagely.meetingservice.meeting.application.service.MeetingAttendanceQueryService;
import com.pagely.meetingservice.meeting.application.service.MeetingCommandService;
import com.pagely.meetingservice.meeting.application.service.MeetingJoinService;
import com.pagely.meetingservice.meeting.application.service.MeetingQueryService;
import com.pagely.meetingservice.meeting.application.service.MeetingScheduleCommandService;
import com.pagely.meetingservice.meeting.application.service.MeetingScheduleQueryService;
import com.pagely.meetingservice.meeting.presentation.dto.request.CreateMeetingRequest;
import com.pagely.meetingservice.meeting.presentation.dto.request.CreateMeetingScheduleRequest;
import com.pagely.meetingservice.meeting.presentation.dto.request.JoinMeetingRequest;
import com.pagely.meetingservice.meeting.presentation.dto.request.UpdateAttendanceStatusRequest;
import com.pagely.meetingservice.meeting.presentation.dto.request.UpdateScheduleStatusRequest;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingAttendanceResponse;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingJoinResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
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

    private final MeetingCommandService meetingCommandService;
    private final MeetingQueryService meetingQueryService;
    private final MeetingJoinService meetingJoinService;
    private final MeetingScheduleCommandService meetingScheduleCommandService;
    private final MeetingScheduleQueryService meetingScheduleQueryService;
    private final MeetingAttendanceCommandService meetingAttendanceCommandService;
    private final MeetingAttendanceQueryService meetingAttendanceQueryService;

    // 모임 생성
    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @Valid @RequestBody CreateMeetingRequest req
    ) {
        MeetingResult result = meetingCommandService.createMeeting(
                req.toCommand(req.getHostId()) // DTO → Command 변환
        );

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

    // 모임 가입 신청
    @PostMapping("/{meetingId}/join")
    public ResponseEntity<MeetingJoinResponse> joinMeeting(
            @PathVariable UUID meetingId,
            @Valid @RequestBody JoinMeetingRequest req
    ) {
        MeetingJoinResult result = meetingJoinService.createMeetingJoin(
                req.toCommand(meetingId, req.getRecruitUserId())
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MeetingJoinResponse.from(result));
    }

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

        MeetingScheduleResult result = meetingScheduleCommandService.createSchedule(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MeetingScheduleResponse.from(result));
    }

    // 모임 일정 목록 조회
    @GetMapping("/{meetingId}/schedules")
    public List<MeetingScheduleResponse> getMeetingSchedules(
            @PathVariable UUID meetingId
    ) {
        List<MeetingScheduleResult> results = meetingScheduleQueryService.getSchedules(meetingId);

        return results.stream()
                .map(MeetingScheduleResponse::from)
                .toList();
    }

    // 모임 일정 상세 조회
    @GetMapping("/{meetingId}/schedules/{scheduleId}")
    public MeetingScheduleResponse getMeetingSchedule(
            @PathVariable UUID meetingId,
            @PathVariable UUID scheduleId
    ) {
        MeetingScheduleResult result = meetingScheduleQueryService.getSchedule(meetingId, scheduleId);

        return MeetingScheduleResponse.from(result);
    }

    // 모임 일정 참석 등록
    @PostMapping("/{meetingId}/schedules/{scheduleId}/join")
    public ResponseEntity<MeetingAttendanceResponse> joinMeetingSchedule(
            @PathVariable UUID meetingId,
            @PathVariable UUID scheduleId
    ) {
        // TODO: 인증 컨텍스트 연결 후 현재 로그인 사용자 ID로 변경
        UUID userId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

        MeetingAttendanceResult result = meetingAttendanceCommandService.joinSchedule(
                meetingId,
                scheduleId,
                userId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MeetingAttendanceResponse.from(result));
    }

    // 모임 일정 상태 변경
    @PatchMapping("/{meetingId}/schedules/{scheduleId}/status")
    public ResponseEntity<MeetingScheduleResponse> changeMeetingScheduleStatus(
            @PathVariable UUID meetingId,
            @PathVariable UUID scheduleId,
            @Valid @RequestBody UpdateScheduleStatusRequest req
    ) {
        // TODO: 인증 컨텍스트 연결 후 현재 로그인 사용자 ID로 변경
        UUID updatedBy = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        UpdateScheduleStatusCommand command = req.toCommand(
                meetingId,
                scheduleId,
                updatedBy
        );

        MeetingScheduleResult result = meetingScheduleCommandService.changeScheduleStatus(command);

        return ResponseEntity.ok(MeetingScheduleResponse.from(result));
    }

    // 출석부 조회
    @GetMapping("/{meetingId}/schedules/{scheduleId}/attendances")
    public List<MeetingAttendanceResponse> getScheduleAttendances(
            @PathVariable UUID meetingId,
            @PathVariable UUID scheduleId
    ) {
        // TODO: 인증 컨텍스트 연결 후 현재 로그인 사용자 ID로 변경 (테스트 ID : 모임장)
        UUID userId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        List<MeetingAttendanceResult> results = meetingAttendanceQueryService.getScheduleAttendances(
                meetingId,
                scheduleId,
                userId
        );

        return results.stream()
                .map(MeetingAttendanceResponse::from)
                .toList();
    }

    // 내 출석부 조회
    @GetMapping("/{meetingId}/attendances/me")
    public List<MeetingAttendanceResponse> getMyAttendances(
            @PathVariable UUID meetingId
    ) {
        // TODO: 인증 컨텍스트 연결 후 현재 로그인 사용자 ID로 변경 (테스트 ID : 모임원)
        UUID userId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

        List<MeetingAttendanceResult> results = meetingAttendanceQueryService.getMyAttendances(
                meetingId,
                userId
        );

        return results.stream()
                .map(MeetingAttendanceResponse::from)
                .toList();
    }

    // 출석 상태 변경
    @PatchMapping("/{meetingId}/schedules/{scheduleId}/attendances")
    public ResponseEntity<MeetingAttendanceResponse> changeAttendanceStatus(
            @PathVariable UUID meetingId,
            @PathVariable UUID scheduleId,
            @Valid @RequestBody UpdateAttendanceStatusRequest req
    ) {
        // TODO: 인증 컨텍스트 연결 후 현재 로그인 사용자 ID로 변경
        UUID updatedBy = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        UpdateAttendanceStatusCommand command = req.toCommand(
                meetingId,
                scheduleId,
                updatedBy
        );

        MeetingAttendanceResult result = meetingAttendanceCommandService.changeAttendanceStatus(command);

        return ResponseEntity.ok(MeetingAttendanceResponse.from(result));
    }
}
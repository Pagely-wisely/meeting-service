package com.pagely.meetingservice.meeting.presentation.controller;

import com.pagely.common.auth.annotation.AuthRequired;
import com.pagely.common.auth.annotation.CurrentUserId;
import com.pagely.common.pagination.PageRequest;
import com.pagely.common.pagination.PageResponse;
import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingScheduleCommand;
import com.pagely.meetingservice.meeting.application.dto.command.UpdateAttendanceStatusCommand;
import com.pagely.meetingservice.meeting.application.dto.command.UpdateScheduleStatusCommand;
import com.pagely.meetingservice.meeting.application.dto.result.AttendanceStatisticsResult;
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
import com.pagely.meetingservice.meeting.domain.model.MeetingJoinStatus;
import com.pagely.meetingservice.meeting.presentation.dto.request.CreateMeetingRequest;
import com.pagely.meetingservice.meeting.presentation.dto.request.CreateMeetingScheduleRequest;
import com.pagely.meetingservice.meeting.presentation.dto.request.JoinMeetingRequest;
import com.pagely.meetingservice.meeting.presentation.dto.request.UpdateAttendanceStatusRequest;
import com.pagely.meetingservice.meeting.presentation.dto.request.UpdateScheduleStatusRequest;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingAttendancePageResponse;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingAttendanceResponse;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingJoinResponse;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingResponse;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingScheduleResponse;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingSummaryResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    @AuthRequired
    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @CurrentUserId UUID currentUserId,
            @Valid @RequestBody CreateMeetingRequest req
    ) {
        MeetingResult result = meetingCommandService.createMeeting(
                req.toCommand(currentUserId)
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MeetingResponse.from(result));
    }

    // 모임 전체 조회 - 페이징 처리
    @AuthRequired
    @GetMapping
    public PageResponse<MeetingSummaryResponse> getMeetings(
            @CurrentUserId UUID currentUserId,
            PageRequest pageRequest
    ) {
        Page<MeetingSummaryResult> results = meetingQueryService.getMeetings(
                pageRequest.toPageable()
        );

        return PageResponse.of(results, MeetingSummaryResponse::from);
    }

    // 모임 상세 조회
    @AuthRequired
    @GetMapping("/{meetingId}")
    public MeetingResponse getMeeting(
            @PathVariable UUID meetingId,
            @CurrentUserId UUID currentUserId
    ) {
        MeetingResult result = meetingQueryService.getMeeting(meetingId);
        return MeetingResponse.from(result);
    }

    // 모임 가입 신청
    @AuthRequired
    @PostMapping("/{meetingId}/join")
    public ResponseEntity<MeetingJoinResponse> joinMeeting(
            @PathVariable UUID meetingId,
            @CurrentUserId UUID currentUserId,
            @Valid @RequestBody JoinMeetingRequest req
    ) {
        MeetingJoinResult result = meetingJoinService.createMeetingJoin(
                req.toCommand(meetingId, currentUserId)
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MeetingJoinResponse.from(result));
    }

    // 가입 신청 목록 조회 - 페이징 처리
    @AuthRequired
    @GetMapping("/{meetingId}/join")
    public PageResponse<MeetingJoinResponse> getMeetingJoinList(
            @PathVariable UUID meetingId,
            @CurrentUserId UUID currentUserId,
            @RequestParam(required = false) MeetingJoinStatus joinStatus,
            PageRequest pageRequest
    ) {
        // 최신 가입 신청이 먼저 보이도록 생성일 내림차순 정렬
        Page<MeetingJoinResult> results = meetingJoinService.getMeetingJoinList(
                meetingId,
                currentUserId,
                joinStatus,
                pageRequest.toPageable(Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return PageResponse.of(results, MeetingJoinResponse::from);
    }

    // 모임 가입 승인
    @AuthRequired
    @PostMapping("/{meetingId}/join/{joinId}/approve")
    public MeetingJoinResponse approveMeetingJoin(
            @PathVariable UUID meetingId,
            @PathVariable UUID joinId,
            @CurrentUserId UUID currentUserId
    ) {
        MeetingJoinResult result = meetingJoinService.approveMeetingJoin(
                meetingId,
                joinId,
                currentUserId
        );

        return MeetingJoinResponse.from(result);
    }

    // 모임 일정 생성
    @AuthRequired
    @PostMapping("/{meetingId}/schedules")
    public ResponseEntity<MeetingScheduleResponse> createMeetingSchedule(
            @PathVariable UUID meetingId,
            @CurrentUserId UUID currentUserId,
            @Valid @RequestBody CreateMeetingScheduleRequest req
    ) {
        CreateMeetingScheduleCommand command = new CreateMeetingScheduleCommand(
                meetingId,
                req.bookId(),
                req.startAt(),
                req.discussionNote(),
                currentUserId
        );

        MeetingScheduleResult result = meetingScheduleCommandService.createSchedule(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MeetingScheduleResponse.from(result));
    }

    // 모임 일정 목록 조회 - 페이징 처리
    @AuthRequired
    @GetMapping("/{meetingId}/schedules")
    public PageResponse<MeetingScheduleResponse> getMeetingSchedules(
            @PathVariable UUID meetingId,
            @CurrentUserId UUID currentUserId,
            PageRequest pageRequest
    ) {
        Page<MeetingScheduleResult> results = meetingScheduleQueryService.getSchedules(
                meetingId,
                pageRequest.toPageable(Sort.by(Sort.Direction.ASC, "scheduleNumber"))
        );

        return PageResponse.of(results, MeetingScheduleResponse::from);
    }

    // 모임 일정 상세 조회
    @AuthRequired
    @GetMapping("/{meetingId}/schedules/{scheduleId}")
    public MeetingScheduleResponse getMeetingSchedule(
            @PathVariable UUID meetingId,
            @PathVariable UUID scheduleId,
            @CurrentUserId UUID currentUserId
    ) {
        MeetingScheduleResult result = meetingScheduleQueryService.getSchedule(meetingId, scheduleId);
        return MeetingScheduleResponse.from(result);
    }

    // 모임 일정 참석 등록
    @AuthRequired
    @PostMapping("/{meetingId}/schedules/{scheduleId}/join")
    public ResponseEntity<MeetingAttendanceResponse> joinMeetingSchedule(
            @PathVariable UUID meetingId,
            @PathVariable UUID scheduleId,
            @CurrentUserId UUID currentUserId
    ) {
        MeetingAttendanceResult result = meetingAttendanceCommandService.joinSchedule(
                meetingId,
                scheduleId,
                currentUserId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MeetingAttendanceResponse.from(result));
    }

    // 모임 일정 상태 변경
    @AuthRequired
    @PatchMapping("/{meetingId}/schedules/{scheduleId}/status")
    public ResponseEntity<MeetingScheduleResponse> changeMeetingScheduleStatus(
            @PathVariable UUID meetingId,
            @PathVariable UUID scheduleId,
            @CurrentUserId UUID currentUserId,
            @Valid @RequestBody UpdateScheduleStatusRequest req
    ) {
        UpdateScheduleStatusCommand command = req.toCommand(
                meetingId,
                scheduleId,
                currentUserId
        );

        MeetingScheduleResult result = meetingScheduleCommandService.changeScheduleStatus(command);

        return ResponseEntity.ok(MeetingScheduleResponse.from(result));
    }

    // 특정 일정 출석부 조회 - 페이징 처리
    @AuthRequired
    @GetMapping("/{meetingId}/schedules/{scheduleId}/attendances")
    public MeetingAttendancePageResponse getScheduleAttendances(
            @PathVariable UUID meetingId,
            @PathVariable UUID scheduleId,
            @CurrentUserId UUID currentUserId,
            PageRequest pageRequest
    ) {
        Page<MeetingAttendanceResult> results = meetingAttendanceQueryService.getScheduleAttendances(
                meetingId,
                scheduleId,
                currentUserId,
                pageRequest.toPageable(Sort.by(Sort.Direction.ASC, "createdAt"))
        );

        AttendanceStatisticsResult statistics = meetingAttendanceQueryService.getScheduleAttendanceStatistics(
                meetingId,
                scheduleId,
                currentUserId
        );

        return MeetingAttendancePageResponse.from(results, statistics);
    }

    // 내 출석부 조회 - 페이징 처리
    @AuthRequired
    @GetMapping("/{meetingId}/attendances/me")
    public MeetingAttendancePageResponse getMyAttendances(
            @PathVariable UUID meetingId,
            @CurrentUserId UUID currentUserId,
            PageRequest pageRequest
    ) {
        Page<MeetingAttendanceResult> results = meetingAttendanceQueryService.getMyAttendances(
                meetingId,
                currentUserId,
                pageRequest.toPageable(Sort.by(Sort.Direction.ASC, "createdAt"))
        );

        AttendanceStatisticsResult statistics = meetingAttendanceQueryService.getMyAttendanceStatistics(
                meetingId,
                currentUserId
        );

        return MeetingAttendancePageResponse.from(results, statistics);
    }

    // 출석 상태 변경
    @AuthRequired
    @PatchMapping("/{meetingId}/schedules/{scheduleId}/attendances")
    public ResponseEntity<MeetingAttendanceResponse> changeAttendanceStatus(
            @PathVariable UUID meetingId,
            @PathVariable UUID scheduleId,
            @CurrentUserId UUID currentUserId,
            @Valid @RequestBody UpdateAttendanceStatusRequest req
    ) {
        UpdateAttendanceStatusCommand command = req.toCommand(
                meetingId,
                scheduleId,
                currentUserId
        );

        MeetingAttendanceResult result = meetingAttendanceCommandService.changeAttendanceStatus(command);

        return ResponseEntity.ok(MeetingAttendanceResponse.from(result));
    }
}
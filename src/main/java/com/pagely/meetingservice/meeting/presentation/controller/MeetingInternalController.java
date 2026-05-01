package com.pagely.meetingservice.meeting.presentation.controller;

import com.pagely.meetingservice.meeting.application.service.MeetingInternalQueryService;
import com.pagely.meetingservice.meeting.presentation.dto.response.InternalReadableMeetingsResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/meetings")
@RequiredArgsConstructor
public class MeetingInternalController {

    private final MeetingInternalQueryService meetingInternalQueryService;

    // 유저 기준 열람 가능 모임 / 모임별 참가 일정 ID
    @GetMapping("/access/readable")
    public ResponseEntity<InternalReadableMeetingsResponse> getReadableMeetings(@RequestParam UUID userId){
        return ResponseEntity.ok(
            meetingInternalQueryService.getReadableMeetings(userId)
        );
    }
}


package com.pagely.meetingservice.meeting.presentation.controller;

import com.pagely.meetingservice.meeting.application.service.MeetingInternalQueryService;
import com.pagely.meetingservice.meeting.presentation.dto.response.InternalReadableMeetingIdsResponse;
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

    // 권한 기준 모임 ID 목록 조회
    @GetMapping("/access/readable")
    public ResponseEntity<InternalReadableMeetingIdsResponse> getReadableMeetingIds(@RequestParam UUID userId) {
        return ResponseEntity.ok(
                InternalReadableMeetingIdsResponse.of(
                        meetingInternalQueryService.getReadableMeetingIds(userId)
                )
        );
    }
}


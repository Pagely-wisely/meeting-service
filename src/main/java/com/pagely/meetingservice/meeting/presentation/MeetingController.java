package com.pagely.meetingservice.meeting.presentation;

import com.pagely.meetingservice.meeting.application.dto.result.MeetingResult;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingSummaryResult;
import com.pagely.meetingservice.meeting.application.service.MeetingQueryService;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingResponse;
import com.pagely.meetingservice.meeting.presentation.dto.response.MeetingSummaryResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 모임 조회 API 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/meetings")
public class MeetingController {

    private final MeetingQueryService meetingQueryService;

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
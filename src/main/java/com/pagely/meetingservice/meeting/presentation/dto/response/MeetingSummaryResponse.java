package com.pagely.meetingservice.meeting.presentation.dto.response;

import com.pagely.meetingservice.meeting.application.dto.result.MeetingSummaryResult;
import java.util.UUID;

// 모임 목록 응답 DTO
public record MeetingSummaryResponse(
        UUID id, // 모임 ID
        String title, // 모임명
        String description // 모임 설명
) {

    // 결과 DTO → 응답 DTO 변환
    public static MeetingSummaryResponse from(MeetingSummaryResult result) {
        return new MeetingSummaryResponse(
                result.id(),
                result.title(),
                result.description()
        );
    }
}
package com.pagely.meetingservice.meeting.presentation.dto.response;

import com.pagely.meetingservice.meeting.application.dto.result.MeetingSummaryResult;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

// 모임 목록 응답 DTO
@Getter
@Builder
public class MeetingSummaryResponse {

    private UUID id;
    private String title;
    private String description;

    public static MeetingSummaryResponse from(MeetingSummaryResult result) {
        return MeetingSummaryResponse.builder()
                .id(result.getId())
                .title(result.getTitle())
                .description(result.getDescription())
                .build();
    }
}
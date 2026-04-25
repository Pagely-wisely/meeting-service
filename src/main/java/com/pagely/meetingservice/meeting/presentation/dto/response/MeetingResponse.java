package com.pagely.meetingservice.meeting.presentation.dto.response;

import com.pagely.meetingservice.meeting.application.dto.result.MeetingResult;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

// 모임 상세 응답 DTO
@Getter
@Builder
public class MeetingResponse {

    private UUID id;
    private String title;
    private String description;
    private UUID hostId;
    private String bookId;

    public static MeetingResponse from(MeetingResult result) {
        return MeetingResponse.builder()
                .id(result.getId())
                .title(result.getTitle())
                .description(result.getDescription())
                .hostId(result.getHostId())
                .bookId(result.getBookId())
                .build();
    }
}
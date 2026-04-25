package com.pagely.meetingservice.meeting.application.dto.result;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

// 모임 목록 조회용 결과 DTO
@Getter
@Builder
public class MeetingSummaryResult {

    private UUID id;
    private String title;
    private String description;

    // 엔티티 → Result 변환
    public static MeetingSummaryResult from(Meeting meeting) {
        return MeetingSummaryResult.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .build();
    }
}
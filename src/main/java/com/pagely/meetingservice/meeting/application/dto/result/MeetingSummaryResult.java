package com.pagely.meetingservice.meeting.application.dto.result;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import java.util.UUID;

// 모임 목록 조회용 결과 DTO
public record MeetingSummaryResult(
        UUID id, // 모임 ID
        String title, // 모임명
        String description // 모임 설명
) {

    // 엔티티 → Result 변환
    public static MeetingSummaryResult from(Meeting meeting) {
        return new MeetingSummaryResult(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getDescription()
        );
    }
}
package com.pagely.meetingservice.meeting.application.dto.result;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

// 모임 상세 조회용 결과 DTO
@Getter
@Builder
public class MeetingResult {

    private UUID id;
    private String title;
    private String description;
    private UUID hostId;
    private String bookId;

    public static MeetingResult from(Meeting meeting) {
        return MeetingResult.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .hostId(meeting.getHostId())
                .bookId(meeting.getBookId())
                .build();
    }
}
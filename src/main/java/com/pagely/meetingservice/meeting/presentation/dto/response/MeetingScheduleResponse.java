package com.pagely.meetingservice.meeting.presentation.dto.response;

import com.pagely.meetingservice.meeting.application.dto.result.MeetingScheduleResult;
import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record MeetingScheduleResponse(
        UUID id,
        UUID meetingId,
        int scheduleNumber,
        String bookId,
        MeetingScheduleStatus status,
        LocalDateTime startAt,
        String discussionNote,
        LocalDateTime createdAt,
        UUID createdBy
) {

    // Result를 API 응답 DTO로 변환
    public static MeetingScheduleResponse from(MeetingScheduleResult result) {
        return new MeetingScheduleResponse(
                result.id(),
                result.meetingId(),
                result.scheduleNumber(),
                result.bookId(),
                result.status(),
                result.startAt(),
                result.discussionNote(),
                result.createdAt(),
                result.createdBy()
        );
    }
}
package com.pagely.meetingservice.meeting.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateMeetingScheduleRequest(

        String bookId,

        @NotNull
        LocalDateTime startAt
) {
}

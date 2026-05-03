package com.pagely.meetingservice.meeting.application.dto.command;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateMeetingScheduleCommand(
        UUID meetingId,
        String bookId,
        LocalDateTime startAt,
        UUID requesterId
) {
}

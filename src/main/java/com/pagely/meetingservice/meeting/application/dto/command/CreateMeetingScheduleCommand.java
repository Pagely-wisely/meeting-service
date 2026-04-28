package com.pagely.meetingservice.meeting.application.dto.command;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateMeetingScheduleCommand(
        UUID meetingId,
        String bookId,
        LocalDateTime startAt,
        String discussionNote,
        // 일정 생성을 요청한 사용자 ID
        UUID requesterId
) {

}
package com.pagely.meetingservice.meeting.application.dto.command;

import java.util.UUID;

public record JoinMeetingCommand(
    UUID meetingId,
    UUID recruitUserId,
    String content,
    UUID createdBy // TODO: 임시로 recruitUserId 사용
) {
    
}

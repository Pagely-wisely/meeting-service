package com.pagely.meetingservice.meeting.presentation.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JoinMeetingRequest(
    @NotNull UUID recruitUserId,
    @Size(max = 500) String content) {
}

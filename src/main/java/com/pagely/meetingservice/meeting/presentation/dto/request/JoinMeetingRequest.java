package com.pagely.meetingservice.meeting.presentation.dto.request;

import com.pagely.meetingservice.meeting.application.dto.command.JoinMeetingCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

// 모임 가입 신청 요청 DTO
public record JoinMeetingRequest(
        @NotNull
        UUID recruitUserId, // 가입 신청 유저 ID

        @Size(max = 500)
        String content // 가입 신청 내용
) {

    // 요청 DTO → 가입 커맨드 변환
    public JoinMeetingCommand toCommand(UUID meetingId, UUID createdBy) {
        return new JoinMeetingCommand(
                meetingId,
                recruitUserId,
                content,
                createdBy
        );
    }
}
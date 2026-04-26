package com.pagely.meetingservice.meeting.presentation.dto.request;

import java.util.UUID;

import com.pagely.meetingservice.meeting.application.dto.command.JoinMeetingCommand;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinMeetingRequest {
    @NotNull
    private UUID recruitUserId;

    @Size(max = 500)
    private String content;
    
public JoinMeetingCommand toCommand(UUID meetingId, UUID createdBy) { // 요청 DTO → 가입 커맨드 변환
    return JoinMeetingCommand.builder()
            .meetingId(meetingId)
            .recruitUserId(this.recruitUserId)
            .content(this.content)
            .createdBy(createdBy)
            .build();
}
}

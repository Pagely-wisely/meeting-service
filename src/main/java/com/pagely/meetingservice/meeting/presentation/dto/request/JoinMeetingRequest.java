package com.pagely.meetingservice.meeting.presentation.dto.request;

import com.pagely.meetingservice.meeting.application.dto.command.JoinMeetingCommand;
import jakarta.validation.constraints.Size;
import java.util.UUID;

// 모임 가입 신청 요청 DTO
public record JoinMeetingRequest(

        @Size(max = 500)
        String content // 가입 신청 내용
) {

    // 요청 DTO → 가입 커맨드 변환
    public JoinMeetingCommand toCommand(UUID meetingId, UUID currentUserId) {
        return new JoinMeetingCommand(
                meetingId,
                currentUserId, //recruitUserId
                content,
                currentUserId // createdBy
        );
    }
}
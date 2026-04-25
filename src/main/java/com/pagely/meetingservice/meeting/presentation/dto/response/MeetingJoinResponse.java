package com.pagely.meetingservice.meeting.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.pagely.meetingservice.meeting.application.dto.result.MeetingJoinResult;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoinStatus;

// 가입 신청 응답 DTO
public record MeetingJoinResponse( 
    UUID id,
    UUID meetingId,
    UUID recruitUserId,
    MeetingJoinStatus joinStatus,
    String content,
    LocalDateTime createdAt,
    UUID createdBy
) {
    public static MeetingJoinResponse from(MeetingJoinResult result) { // 결과 DTO → 응답 DTO 변환
        return new MeetingJoinResponse(
            result.id(),
            result.meetingId(),
            result.recruitUserId(),
            result.joinStatus(),
            result.content(),
            result.createdAt(),
            result.createdBy()
        );
}
}
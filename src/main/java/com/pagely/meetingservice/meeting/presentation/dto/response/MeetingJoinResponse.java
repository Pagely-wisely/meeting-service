package com.pagely.meetingservice.meeting.presentation.dto.response;

import com.pagely.meetingservice.meeting.application.dto.result.MeetingJoinResult;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoinStatus;
import java.time.LocalDateTime;
import java.util.UUID;

// 가입 신청 응답 DTO
public record MeetingJoinResponse(
        UUID id, // 가입 신청 ID
        UUID meetingId, // 모임 ID
        UUID recruitUserId, // 가입 신청 유저 ID
        MeetingJoinStatus joinStatus, // 가입 신청 상태
        String content, // 가입 신청 내용
        LocalDateTime createdAt, // 생성 일시
        UUID createdBy // 생성자 ID
) {

    // 결과 DTO → 응답 DTO 변환
    public static MeetingJoinResponse from(MeetingJoinResult result) {
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
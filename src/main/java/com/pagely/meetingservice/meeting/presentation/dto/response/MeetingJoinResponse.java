package com.pagely.meetingservice.meeting.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.pagely.meetingservice.meeting.application.dto.result.MeetingJoinResult;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoinStatus;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingJoinResponse{ // 가입 신청 응답 DTO
    private UUID id;
    private UUID meetingId;
    private UUID recruitUserId;
    private MeetingJoinStatus joinStatus;
    private String content;
    private LocalDateTime createdAt;
    private UUID createdBy;
    public static MeetingJoinResponse from(MeetingJoinResult result) { // 결과 DTO → 응답 DTO 변환
        return MeetingJoinResponse.builder()
            .id(result.getId())
            .meetingId(result.getMeetingId())
            .recruitUserId(result.getRecruitUserId())
            .joinStatus(result.getJoinStatus())
            .content(result.getContent())
            .createdAt(result.getCreatedAt())
            .createdBy(result.getCreatedBy())
            .build();
}
}
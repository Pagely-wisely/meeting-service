package com.pagely.meetingservice.meeting.application.dto.result;

import java.time.LocalDateTime;
import java.util.UUID;

import com.pagely.meetingservice.meeting.domain.model.MeetingJoin;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoinStatus;


// 가입 신청 결과 레코드 
public record MeetingJoinResult(
    UUID id,
    UUID meetingId,
    UUID recruitUserId,
    MeetingJoinStatus joinStatus,
    String content,
    LocalDateTime createdAt,
    UUID createdBy
) {
    public static MeetingJoinResult from(MeetingJoin join) { // 엔티티 → 결과 DTO 변환
        return new MeetingJoinResult(
            join.getId(),
            join.getMeetingId(),
            join.getRecruitUserId(),
            join.getJoinStatus(),
            join.getContent(),
            join.getCreatedAt(),
            join.getCreatedBy()
        );
    }
}

package com.pagely.meetingservice.meeting.application.dto.result;

import com.pagely.meetingservice.meeting.domain.model.MeetingJoin;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoinStatus;
import java.time.LocalDateTime;
import java.util.UUID;

// 가입 신청 결과 DTO
public record MeetingJoinResult(
        UUID id, // 가입 신청 ID
        UUID meetingId, // 모임 ID
        UUID recruitUserId, // 가입 신청 유저 ID
        MeetingJoinStatus joinStatus, // 가입 신청 상태
        String content, // 가입 신청 내용
        LocalDateTime createdAt, // 생성 일시
        UUID createdBy // 생성자 ID
) {

    // 엔티티 → 결과 DTO 변환
    public static MeetingJoinResult from(MeetingJoin join) {
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
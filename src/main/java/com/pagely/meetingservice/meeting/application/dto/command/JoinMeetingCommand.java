package com.pagely.meetingservice.meeting.application.dto.command;

import com.pagely.meetingservice.meeting.domain.model.MeetingJoin;
import java.time.LocalDateTime;
import java.util.UUID;

// 모임 가입 신청 Command
public record JoinMeetingCommand(
        UUID meetingId, // 모임 ID
        UUID recruitUserId, // 가입 신청 유저 ID
        String content, // 가입 신청 내용
        UUID createdBy // 생성자 ID TODO: 임시로 recruitUserId 사용
) {

    // Command → 가입 신청 엔티티 변환
    public MeetingJoin toMeetingJoin(UUID joinId, LocalDateTime now) {
        return MeetingJoin.create(
                joinId,
                meetingId,
                recruitUserId,
                content,
                now,
                createdBy
        );
    }
}
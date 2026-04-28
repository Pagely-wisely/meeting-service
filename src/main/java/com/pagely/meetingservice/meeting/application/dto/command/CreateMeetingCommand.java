package com.pagely.meetingservice.meeting.application.dto.command;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingType;
import com.pagely.meetingservice.meeting.domain.model.ReadingLevel;
import com.pagely.meetingservice.meeting.domain.model.RecruitRate;
import java.time.LocalDateTime;
import java.util.UUID;

// 모임 생성 Command
public record CreateMeetingCommand(
        UUID hostId, // 모임장 ID
        String bookId, // 책 ID
        String title, // 모임명
        String description, // 모임 설명
        MeetingType meetingType, // 모임 유형
        LocalDateTime recruitStartAt, // 모집 시작 일시
        LocalDateTime recruitEndAt, // 모집 종료 일시
        Integer recruitMax, // 모집 정원
        ReadingLevel readingLevel, // 독서 난이도
        String ruleMemo, // 규칙 메모
        RecruitRate recruitRate, // 모집 주기
        Boolean freePaid, // 무료/유료 여부
        UUID createdBy // 생성자 ID
) {

    // Command → 모임 엔티티 변환
    public Meeting toMeeting(UUID meetingId, LocalDateTime now) {
        return Meeting.create(
                meetingId,
                hostId,
                bookId,
                title,
                description,
                meetingType,
                recruitStartAt,
                recruitEndAt,
                recruitMax,
                readingLevel,
                ruleMemo,
                recruitRate,
                Boolean.TRUE.equals(freePaid)
        );
    }
}
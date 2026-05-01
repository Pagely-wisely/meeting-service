package com.pagely.meetingservice.meeting.application.dto.command;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingType;
import com.pagely.meetingservice.meeting.domain.model.ReadingLevel;
import com.pagely.meetingservice.meeting.domain.model.RecruitRate;
import java.time.LocalDateTime;
import java.util.UUID;

// 모임 생성 Command
public record CreateMeetingCommand(
        UUID hostId,
        String bookId,
        String title,
        String description,
        MeetingType meetingType,
        LocalDateTime recruitStartAt,
        LocalDateTime recruitEndAt,
        Integer recruitMax,
        ReadingLevel readingLevel,
        String ruleMemo,
        RecruitRate recruitRate,
        Boolean freePaid,
        LocalDateTime scheduleStartAt,
        String discussionNote,
        UUID createdBy
) {

    private static final String DEFAULT_MEETING_BOOK_ID = "-";

    // Command → 모임 엔티티 변환
    public Meeting toMeeting(UUID meetingId, LocalDateTime now) {
        return Meeting.create(
                meetingId,
                hostId,
                resolveBookId(),
                title,
                description,
                meetingType,
                recruitStartAt,
                recruitEndAt,
                recruitMax,
                readingLevel,
                ruleMemo,
                recruitRate,
                freePaid,
                now,
                createdBy
        );
    }

    // 정기 모임처럼 모임 자체에 도서가 없어도 되는 경우 null 저장을 방지하기 위한 기본값 보정
    private String resolveBookId() {
        if (bookId == null || bookId.isBlank()) {
            return DEFAULT_MEETING_BOOK_ID;
        }

        return bookId;
    }
}
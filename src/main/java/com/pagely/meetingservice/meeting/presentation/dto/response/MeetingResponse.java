package com.pagely.meetingservice.meeting.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.pagely.meetingservice.meeting.application.dto.result.MeetingResult;
import com.pagely.meetingservice.meeting.domain.model.MeetingStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingType;
import com.pagely.meetingservice.meeting.domain.model.ReadingLevel;
import com.pagely.meetingservice.meeting.domain.model.RecruitRate;
import com.pagely.meetingservice.meeting.domain.model.RecruitStatus;

public record MeetingResponse(
    UUID id,
    UUID hostId,
    UUID bookId,
    String title,
    String description,
    MeetingType meetingType,
    MeetingStatus meetingStatus,
    RecruitStatus recruitStatus,
    LocalDateTime recruitStartAt,
    LocalDateTime recruitEndAt,
    Integer recruitMax,
    ReadingLevel readingLevel,
    String ruleMemo,
    RecruitRate recruitRate,
    Boolean freePaid,
    LocalDateTime createdAt,
    UUID createdBy
) {
    public static MeetingResponse from(MeetingResult r){
        return new MeetingResponse(
            r.id(),
            r.hostId(),
            r.bookId(),
            r.title(),
            r.description(),
            r.meetingType(),
            r.meetingStatus(),
            r.recruitStatus(),
            r.recruitStartAt(),
            r.recruitEndAt(),
            r.recruitMax(),
            r.readingLevel(),
            r.ruleMemo(),
            r.recruitRate(),
            r.freePaid(),
            r.createdAt(),
            r.createdBy()
        );
    }
}
package com.pagely.meetingservice.meeting.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

import com.pagely.meetingservice.meeting.domain.model.MeetingType;
import com.pagely.meetingservice.meeting.domain.model.ReadingLevel;
import com.pagely.meetingservice.meeting.domain.model.RecruitRate;

public record CreateMeetingRequest(

        @NotNull UUID hostId,

        UUID bookId,

        @NotBlank @Size(max = 20) String title,

        @Size(max = 500) String description,

        @NotNull MeetingType meetingType, // ONES, REGULAR

        @NotNull LocalDateTime recruitStartAt,

        @NotNull LocalDateTime recruitEndAt,

        @NotNull @Positive Integer recruitMax,

        @NotNull ReadingLevel readingLevel, // BEGINNER, NORMAL, ADVANCED

        String ruleMemo,

        @NotNull RecruitRate recruitRate, // WEEKLY, BIWEEKLY, MONTHLY

        @NotNull Boolean freePaid) {
}

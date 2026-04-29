package com.pagely.meetingservice.meeting.presentation.dto.request;

import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingCommand;
import com.pagely.meetingservice.meeting.domain.model.MeetingType;
import com.pagely.meetingservice.meeting.domain.model.ReadingLevel;
import com.pagely.meetingservice.meeting.domain.model.RecruitRate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

// 모임 생성 요청 DTO
public record CreateMeetingRequest(

        String bookId, // 책 ID

        @NotBlank
        @Size(max = 100)
        String title, // 모임명

        @Size(max = 200)
        String description, // 모임 설명

        @NotNull
        MeetingType meetingType, // 모임 유형 (ONES, REGULAR)

        @NotNull
        LocalDateTime recruitStartAt, // 모집 시작일

        @NotNull
        LocalDateTime recruitEndAt, // 모집 종료일

        @NotNull
        @Positive
        Integer recruitMax, // 모집 정원

        @NotNull
        ReadingLevel readingLevel, // 독서 난이도 (BEGINNER, NORMAL, ADVANCED)

        String ruleMemo, // 규칙 메모

        @NotNull
        RecruitRate recruitRate, // 모집 주기 (WEEKLY, BIWEEKLY, MONTHLY)

        @NotNull
        Boolean freePaid // 무료/유료 여부
) {

    // 요청 DTO → 생성 커맨드 변환
    public CreateMeetingCommand toCommand(UUID currentUserId) {
        return new CreateMeetingCommand(
                currentUserId,
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
                freePaid,
                currentUserId // createdBy
        );
    }
}
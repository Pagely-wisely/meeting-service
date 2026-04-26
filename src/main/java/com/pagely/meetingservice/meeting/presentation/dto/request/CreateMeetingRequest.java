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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 모임 생성 요청 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMeetingRequest {

    @NotNull
    private UUID hostId; // 모임장 ID

    private String bookId; // 책 ID

    @NotBlank
    @Size(max = 100)
    private String title; // 모임명

    @Size(max = 200)
    private String description; // 모임 설명

    @NotNull
    private MeetingType meetingType; // 모임 유형 (ONES, REGULAR)

    @NotNull
    private LocalDateTime recruitStartAt; // 모집 시작일

    @NotNull
    private LocalDateTime recruitEndAt; // 모집 종료일

    @NotNull
    @Positive
    private Integer recruitMax; // 모집 정원

    @NotNull
    private ReadingLevel readingLevel; // 독서 난이도 (BEGINNER, NORMAL, ADVANCED)

    private String ruleMemo; // 규칙 메모

    @NotNull
    private RecruitRate recruitRate; // 모집 주기 (WEEKLY, BIWEEKLY, MONTHLY)

    @NotNull
    private Boolean freePaid; // 무료/유료 여부

    // 요청 DTO → 생성 커맨드 변환
    public CreateMeetingCommand toCommand(UUID createdBy) {
        return CreateMeetingCommand.builder()
                .hostId(this.hostId)
                .bookId(this.bookId)
                .title(this.title)
                .description(this.description)
                .meetingType(this.meetingType)
                .recruitStartAt(this.recruitStartAt)
                .recruitEndAt(this.recruitEndAt)
                .recruitMax(this.recruitMax)
                .readingLevel(this.readingLevel)
                .ruleMemo(this.ruleMemo)
                .recruitRate(this.recruitRate)
                .freePaid(this.freePaid)
                .createdBy(createdBy)
                .build();
    }
}
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


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMeetingRequest {

    @NotNull
    private UUID hostId;

    private String bookId;

    @NotBlank
    @Size(max = 20)
    private String title;

    @Size(max = 200)
    private String description;
    
    @NotNull
    private MeetingType meetingType; // ONES, REGULAR

    @NotNull
    private LocalDateTime recruitStartAt;

    @NotNull
    private LocalDateTime recruitEndAt;

    @NotNull
    @Positive
    private Integer recruitMax;

    @NotNull
    private ReadingLevel readingLevel; // BEGINNER, NORMAL, ADVANCED

    private String ruleMemo;

    @NotNull
    private RecruitRate recruitRate; // WEEKLY, BIWEEKLY, MONTHLY

    @NotNull
    private Boolean freePaid;

    public CreateMeetingCommand toCommand(UUID createdBy) { // 요청 DTO → 생성 커맨드 변환
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

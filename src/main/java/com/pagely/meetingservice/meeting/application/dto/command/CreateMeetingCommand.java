package com.pagely.meetingservice.meeting.application.dto.command;

import java.time.LocalDateTime;
import java.util.UUID;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingType;
import com.pagely.meetingservice.meeting.domain.model.ReadingLevel;
import com.pagely.meetingservice.meeting.domain.model.RecruitRate;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMeetingCommand{
    private UUID hostId; // 모임장 ID
    private String bookId; // 책 ID
    private String title; // 모임명
    private String description; // 모임 설명
    private MeetingType meetingType; // 모임 유형
    private LocalDateTime recruitStartAt; // 모임 시작
    private LocalDateTime recruitEndAt; // 모임 종료
    private Integer recruitMax; // 모집 정원
    private ReadingLevel readingLevel; // 독서 난이도
    private String ruleMemo; // 규칙 메모
    private RecruitRate recruitRate; // 모집 주기
    private Boolean freePaid; // 무료/유료 여부
    private UUID createdBy;

    public Meeting toMeeting(UUID meetingId, LocalDateTime now) {
        return Meeting.create(
                meetingId,
                this.hostId,
                this.bookId,
                this.title,
                this.description,
                this.meetingType,
                this.recruitStartAt,
                this.recruitEndAt,
                this.recruitMax,
                this.readingLevel,
                this.ruleMemo,
                this.recruitRate,
                Boolean.TRUE.equals(this.freePaid),
                now,
                this.createdBy
        );
    }
}

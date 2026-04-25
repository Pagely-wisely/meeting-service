package com.pagely.meetingservice.meeting.presentation.dto.response;

import com.pagely.meetingservice.meeting.application.dto.result.MeetingResult;
import com.pagely.meetingservice.meeting.domain.model.MeetingStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingType;
import com.pagely.meetingservice.meeting.domain.model.ReadingLevel;
import com.pagely.meetingservice.meeting.domain.model.RecruitRate;
import com.pagely.meetingservice.meeting.domain.model.RecruitStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

// 모임 상세 응답 DTO
@Getter
@Builder
public class MeetingResponse {

    private UUID id;
    private String title;
    private String description;
    private UUID hostId;
    private String bookId;
    private MeetingType meetingType;
    private MeetingStatus meetingStatus;
    private RecruitStatus recruitStatus;
    private LocalDateTime recruitStartAt;
    private LocalDateTime recruitEndAt;
    private Integer recruitMax;
    private ReadingLevel readingLevel;
    private String ruleMemo;
    private RecruitRate recruitRate;
    private Boolean freePaid;
    private LocalDateTime createdAt;
    private UUID createdBy;

    public static MeetingResponse from(MeetingResult result) {
        return MeetingResponse.builder()
                .id(result.getId())
                .title(result.getTitle())
                .description(result.getDescription())
                .hostId(result.getHostId())
                .bookId(result.getBookId())
                .meetingType(result.getMeetingType())
                .meetingStatus(result.getMeetingStatus())
                .recruitStatus(result.getRecruitStatus())
                .recruitStartAt(result.getRecruitStartAt())
                .recruitEndAt(result.getRecruitEndAt())
                .recruitMax(result.getRecruitMax())
                .readingLevel(result.getReadingLevel())
                .ruleMemo(result.getRuleMemo())
                .recruitRate(result.getRecruitRate())
                .freePaid(result.getFreePaid())
                .createdAt(result.getCreatedAt())
                .createdBy(result.getCreatedBy())
                .build();
    }
}
package com.pagely.meetingservice.meeting.application.dto.result;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingType;
import com.pagely.meetingservice.meeting.domain.model.ReadingLevel;
import com.pagely.meetingservice.meeting.domain.model.RecruitRate;
import com.pagely.meetingservice.meeting.domain.model.RecruitStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

// 모임 상세 조회 결과 DTO
@Getter
@Builder
public class MeetingResult {

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

    // Entity를 application 결과 DTO로 변환
    public static MeetingResult from(Meeting meeting) {
        return MeetingResult.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .hostId(meeting.getHostId())
                .bookId(meeting.getBookId())
                .meetingType(meeting.getMeetingType())
                .meetingStatus(meeting.getMeetingStatus())
                .recruitStatus(meeting.getRecruitStatus())
                .recruitStartAt(meeting.getRecruitStartAt())
                .recruitEndAt(meeting.getRecruitEndAt())
                .recruitMax(meeting.getRecruitMax())
                .readingLevel(meeting.getReadingLevel())
                .ruleMemo(meeting.getRuleMemo())
                .recruitRate(meeting.getRecruitRate())
                .freePaid(meeting.isFreePaid())
                .createdAt(meeting.getCreatedAt())
                .createdBy(meeting.getCreatedBy())
                .build();
    }
}
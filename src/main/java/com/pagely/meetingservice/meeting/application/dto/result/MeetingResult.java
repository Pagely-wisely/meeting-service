package com.pagely.meetingservice.meeting.application.dto.result;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingType;
import com.pagely.meetingservice.meeting.domain.model.ReadingLevel;
import com.pagely.meetingservice.meeting.domain.model.RecruitRate;
import com.pagely.meetingservice.meeting.domain.model.RecruitStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record MeetingResult(
        UUID id,
        UUID hostId,
        String bookId,
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
    public static MeetingResult from(Meeting m) {
        return new MeetingResult(
                m.getId(),
                m.getHostId(),
                m.getBookId(),
                m.getTitle(),
                m.getDescription(),
                m.getMeetingType(),
                m.getMeetingStatus(),
                m.getRecruitStatus(),
                m.getRecruitStartAt(),
                m.getRecruitEndAt(),
                m.getRecruitMax(),
                m.getReadingLevel(),
                m.getRuleMemo(),
                m.getRecruitRate(),
                m.isFreePaid(),
                m.getCreatedAt(),
                m.getCreatedBy()
        );
    }

}

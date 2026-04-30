package com.pagely.meetingservice.meeting.domain.event;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingType;
import com.pagely.meetingservice.meeting.domain.model.ReadingLevel;
import com.pagely.meetingservice.meeting.domain.model.RecruitRate;
import com.pagely.meetingservice.meeting.domain.model.RecruitStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class MeetingCreatedEvent extends BaseEvent {

    private static final String DOMAIN_TYPE = "MEETING";

    private MeetingCreatedEvent(UUID meetingId, Object payload) {
        super(DOMAIN_TYPE, meetingId, payload);
    }

    public static MeetingCreatedEvent of(Meeting meeting) {
        return new MeetingCreatedEvent(
                meeting.getId(),
                new Payload(
                        meeting.getId(),
                        meeting.getHostId(),
                        meeting.getBookId(),
                        meeting.getTitle(),
                        meeting.getMeetingType(),
                        meeting.getMeetingStatus(),
                        meeting.getRecruitStatus(),
                        meeting.getRecruitStartAt(),
                        meeting.getRecruitEndAt(),
                        meeting.getRecruitMax(),
                        meeting.getReadingLevel(),
                        meeting.getRecruitRate(),
                        meeting.isFreePaid()
                )
        );
    }

    public record Payload(
            UUID meetingId,
            UUID hostId,
            String bookId,
            String title,
            MeetingType meetingType,
            MeetingStatus meetingStatus,
            RecruitStatus recruitStatus,
            LocalDateTime recruitStartAt,
            LocalDateTime recruitEndAt,
            int recruitMax,
            ReadingLevel readingLevel,
            RecruitRate recruitRate,
            boolean freePaid
    ) {
    }
}
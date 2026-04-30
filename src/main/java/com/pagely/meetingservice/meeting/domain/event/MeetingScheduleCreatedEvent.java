package com.pagely.meetingservice.meeting.domain.event;

import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class MeetingScheduleCreatedEvent extends BaseEvent {

    private static final String DOMAIN_TYPE = "SCHEDULE";

    private MeetingScheduleCreatedEvent(UUID scheduleId, Object payload) {
        super(DOMAIN_TYPE, scheduleId, payload);
    }

    public static MeetingScheduleCreatedEvent of(MeetingSchedule schedule) {
        return new MeetingScheduleCreatedEvent(
                schedule.getId(),
                new Payload(
                        schedule.getId(),
                        schedule.getMeetingId(),
                        schedule.getScheduleNumber(),
                        schedule.getBookId(),
                        schedule.getStatus(),
                        schedule.getStartAt(),
                        schedule.getDiscussionNote()
                )
        );
    }

    public record Payload(
            UUID scheduleId,
            UUID meetingId,
            int scheduleNumber,
            String bookId,
            MeetingScheduleStatus status,
            LocalDateTime startAt,
            String discussionNote
    ) {
    }
}
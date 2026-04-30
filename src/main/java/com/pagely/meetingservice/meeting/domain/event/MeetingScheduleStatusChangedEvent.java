package com.pagely.meetingservice.meeting.domain.event;

import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleStatus;
import java.util.UUID;
import lombok.Getter;

@Getter
public class MeetingScheduleStatusChangedEvent extends BaseEvent {

    private static final String DOMAIN_TYPE = "SCHEDULE";

    private MeetingScheduleStatusChangedEvent(UUID scheduleId, Object payload) {
        super(DOMAIN_TYPE, scheduleId, payload);
    }

    public static MeetingScheduleStatusChangedEvent of(MeetingSchedule schedule) {
        return new MeetingScheduleStatusChangedEvent(
                schedule.getId(),
                new Payload(
                        schedule.getId(),
                        schedule.getMeetingId(),
                        schedule.getStatus()
                )
        );
    }

    public record Payload(
            UUID scheduleId,
            UUID meetingId,
            MeetingScheduleStatus status
    ) {
    }
}
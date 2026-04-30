package com.pagely.meetingservice.meeting.domain.event;

import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;
import java.util.UUID;
import lombok.Getter;

@Getter
public class MeetingAttendanceJoinedEvent extends BaseEvent {

    private static final String DOMAIN_TYPE = "ATTENDANCE";

    private MeetingAttendanceJoinedEvent(UUID attendanceId, Object payload) {
        super(DOMAIN_TYPE, attendanceId, payload);
    }

    public static MeetingAttendanceJoinedEvent of(MeetingAttendance attendance) {
        return new MeetingAttendanceJoinedEvent(
                attendance.getId(),
                new Payload(
                        attendance.getId(),
                        attendance.getMeetingId(),
                        attendance.getScheduleId(),
                        attendance.getUserId(),
                        attendance.getStatus()
                )
        );
    }

    public record Payload(
            UUID attendanceId,
            UUID meetingId,
            UUID scheduleId,
            UUID userId,
            AttendanceStatus status
    ) {
    }
}
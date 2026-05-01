package com.pagely.meetingservice.meeting.domain.event;

import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class MeetingAttendanceStatusChangedEvent extends BaseEvent {

    private static final String DOMAIN_TYPE = "ATTENDANCE";

    private MeetingAttendanceStatusChangedEvent(UUID attendanceId, Object payload) {
        super(DOMAIN_TYPE, attendanceId, payload);
    }

    public static MeetingAttendanceStatusChangedEvent of(
            MeetingAttendance attendance,
            UUID changedBy
    ) {
        return new MeetingAttendanceStatusChangedEvent(
                attendance.getId(),
                new Payload(
                        attendance.getId(),
                        attendance.getMeetingId(),
                        attendance.getScheduleId(),
                        attendance.getUserId(),
                        attendance.getStatus(),
                        attendance.getCheckedAt(),
                        attendance.getNote(),
                        changedBy
                )
        );
    }

    public record Payload(
            UUID attendanceId,
            UUID meetingId,
            UUID scheduleId,
            UUID userId,
            AttendanceStatus status,
            LocalDateTime checkedAt,
            String note,
            UUID changedBy
    ) {
    }
}
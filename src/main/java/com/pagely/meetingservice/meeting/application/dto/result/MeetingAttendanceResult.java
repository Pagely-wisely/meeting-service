package com.pagely.meetingservice.meeting.application.dto.result;

import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;
import java.time.LocalDateTime;
import java.util.UUID;

// 모임 일정 참석 등록 결과 DTO
public record MeetingAttendanceResult(
        UUID attendanceId,
        UUID meetingId,
        UUID scheduleId,
        UUID userId,
        AttendanceStatus status,
        LocalDateTime checkedAt,
        String note,
        LocalDateTime createdAt
) {

    // 엔티티 → 결과 DTO 변환
    public static MeetingAttendanceResult from(MeetingAttendance attendance) {
        return new MeetingAttendanceResult(
                attendance.getId(),
                attendance.getMeetingId(),
                attendance.getScheduleId(),
                attendance.getUserId(),
                attendance.getStatus(),
                attendance.getCheckedAt(),
                attendance.getNote(),
                attendance.getCreatedAt()
        );
    }
}
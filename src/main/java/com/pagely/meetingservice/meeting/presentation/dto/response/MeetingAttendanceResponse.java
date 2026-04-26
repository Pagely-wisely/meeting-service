package com.pagely.meetingservice.meeting.presentation.dto.response;

import com.pagely.meetingservice.meeting.application.dto.result.MeetingAttendanceResult;
import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import java.time.LocalDateTime;
import java.util.UUID;

// 모임 일정 참석 등록 응답 DTO
public record MeetingAttendanceResponse(
        UUID attendanceId,
        UUID meetingId,
        UUID scheduleId,
        UUID userId,
        AttendanceStatus status,
        LocalDateTime checkedAt,
        String note,
        LocalDateTime createdAt
) {

    // 결과 DTO → 응답 DTO 변환
    public static MeetingAttendanceResponse from(MeetingAttendanceResult result) {
        return new MeetingAttendanceResponse(
                result.attendanceId(),
                result.meetingId(),
                result.scheduleId(),
                result.userId(),
                result.status(),
                result.checkedAt(),
                result.note(),
                result.createdAt()
        );
    }
}
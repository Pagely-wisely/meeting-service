package com.pagely.meetingservice.meeting.application.dto.command;

import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import java.util.UUID;

// 출석 상태 변경 커맨드 DTO
public record UpdateAttendanceStatusCommand(
        UUID meetingId, // 모임 ID
        UUID scheduleId, // 일정 ID
        UUID userId, // 출석 상태 변경 대상 유저 ID
        AttendanceStatus status, // 변경할 출석 상태
        String note, // 비고
        UUID updatedBy // 변경자 ID
) {
}
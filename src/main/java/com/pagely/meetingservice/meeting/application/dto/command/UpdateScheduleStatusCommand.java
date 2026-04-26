package com.pagely.meetingservice.meeting.application.dto.command;

import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleStatus;
import java.util.UUID;

// 모임 일정 상태 변경 커맨드 DTO
public record UpdateScheduleStatusCommand(
        UUID meetingId, // 모임 ID
        UUID scheduleId, // 일정 ID
        MeetingScheduleStatus status, // 변경할 일정 상태
        UUID updatedBy // 변경자 ID
) {
}
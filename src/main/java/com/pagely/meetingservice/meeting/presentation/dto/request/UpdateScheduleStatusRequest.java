package com.pagely.meetingservice.meeting.presentation.dto.request;

import com.pagely.meetingservice.meeting.application.dto.command.UpdateScheduleStatusCommand;
import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleStatus;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

// 모임 일정 상태 변경 요청 DTO
public record UpdateScheduleStatusRequest(

        @NotNull
        MeetingScheduleStatus status // 변경할 일정 상태
) {

    // 요청 DTO → 일정 상태 변경 커맨드 변환
    public UpdateScheduleStatusCommand toCommand(
            UUID meetingId,
            UUID scheduleId,
            UUID updatedBy
    ) {
        return new UpdateScheduleStatusCommand(
                meetingId,
                scheduleId,
                status,
                updatedBy
        );
    }
}
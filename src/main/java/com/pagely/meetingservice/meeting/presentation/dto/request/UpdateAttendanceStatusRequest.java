package com.pagely.meetingservice.meeting.presentation.dto.request;

import com.pagely.meetingservice.meeting.application.dto.command.UpdateAttendanceStatusCommand;
import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

// 출석 상태 변경 요청 DTO
public record UpdateAttendanceStatusRequest(

        @NotNull
        UUID userId, // 출석 상태를 변경할 대상 유저 ID

        @NotNull
        AttendanceStatus status, // 변경할 출석 상태

        @Size(max = 255)
        String note // 출석 상태 변경 비고
) {

    // 요청 DTO → 출석 상태 변경 커맨드 변환
    public UpdateAttendanceStatusCommand toCommand(
            UUID meetingId,
            UUID scheduleId,
            UUID updatedBy
    ) {
        return new UpdateAttendanceStatusCommand(
                meetingId,
                scheduleId,
                userId,
                status,
                note,
                updatedBy
        );
    }
}
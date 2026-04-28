package com.pagely.meetingservice.meeting.presentation.dto.request;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.command.UpdateAttendanceStatusCommand;
import com.pagely.meetingservice.meeting.domain.exception.MeetingAttendanceErrorCode;
import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

// 출석 상태 변경 요청 DTO
public record UpdateAttendanceStatusRequest(

        @NotNull
        UUID userId,

        @NotNull
        AttendanceStatus status,

        @Size(max = 255)
        String note
) {

    private static boolean isUpdatableStatus(AttendanceStatus status) {
        return status == AttendanceStatus.ATTENDED
                || status == AttendanceStatus.LATE
                || status == AttendanceStatus.ABSENT
                || status == AttendanceStatus.EXCUSED;
    }

    public UpdateAttendanceStatusCommand toCommand(
            UUID meetingId,
            UUID scheduleId,
            UUID updatedBy
    ) {
        if (!isUpdatableStatus(status)) {
            throw new BusinessException(MeetingAttendanceErrorCode.INVALID_ATTENDANCE_STATUS);
        }

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
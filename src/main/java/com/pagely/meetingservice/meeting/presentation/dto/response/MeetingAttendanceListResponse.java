package com.pagely.meetingservice.meeting.presentation.dto.response;

import com.pagely.meetingservice.meeting.application.dto.result.AttendanceStatisticsResult;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingAttendanceResult;
import java.util.List;

// 출석부 목록 + 출석 통계 응답 DTO
public record MeetingAttendanceListResponse(
        List<MeetingAttendanceResponse> attendances,
        AttendanceStatisticsResult statistics
) {

    public static MeetingAttendanceListResponse from(
            List<MeetingAttendanceResult> results,
            AttendanceStatisticsResult statistics
    ) {
        return new MeetingAttendanceListResponse(
                results.stream()
                        .map(MeetingAttendanceResponse::from)
                        .toList(),
                statistics
        );
    }
}
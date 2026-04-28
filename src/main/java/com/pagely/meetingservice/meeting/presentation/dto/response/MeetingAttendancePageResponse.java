package com.pagely.meetingservice.meeting.presentation.dto.response;

import com.pagely.common.pagination.PageResponse;
import com.pagely.meetingservice.meeting.application.dto.result.AttendanceStatisticsResult;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingAttendanceResult;
import org.springframework.data.domain.Page;

// 출석부 페이징 목록 + 출석 통계 응답 DTO
public record MeetingAttendancePageResponse(
        PageResponse<MeetingAttendanceResponse> attendances,
        AttendanceStatisticsResult statistics
) {

    // 출석 결과 Page와 통계 결과를 응답 DTO로 변환
    public static MeetingAttendancePageResponse from(
            Page<MeetingAttendanceResult> attendances,
            AttendanceStatisticsResult statistics
    ) {
        return new MeetingAttendancePageResponse(
                PageResponse.of(attendances, MeetingAttendanceResponse::from),
                statistics
        );
    }
}
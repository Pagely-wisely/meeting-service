package com.pagely.meetingservice.meeting.presentation.dto.response;

import java.util.List;
import java.util.UUID;


// 사용자가 열람 가능한 모임의 참석 등록한 일정 ID 목록 반환
public record InternalReadableMeetingsResponse(

        List<MeetingWithSchedules> meetings
) {
    public record MeetingWithSchedules( // 한 모임에 대한 meetingId와 해당 모임의 일정 ID 목록
                                        UUID meetingId,
                                        List<UUID> scheduleIds
    ) {
    }

    public static InternalReadableMeetingsResponse of(List<MeetingWithSchedules> meetings) {
        return new InternalReadableMeetingsResponse(
                meetings == null ? List.of() : List.copyOf(meetings));
    }
}

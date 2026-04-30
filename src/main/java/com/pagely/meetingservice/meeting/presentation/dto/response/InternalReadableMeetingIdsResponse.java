package com.pagely.meetingservice.meeting.presentation.dto.response;

import java.util.List;
import java.util.UUID;

public record InternalReadableMeetingIdsResponse(
        List<UUID> meetingIds
) {
    // 서비스 결과 리스트 -> DTO 변환
    public static InternalReadableMeetingIdsResponse of(List<UUID> meetingIds) {
        return new InternalReadableMeetingIdsResponse(
                meetingIds == null ? List.of() : meetingIds
        );
    }
}

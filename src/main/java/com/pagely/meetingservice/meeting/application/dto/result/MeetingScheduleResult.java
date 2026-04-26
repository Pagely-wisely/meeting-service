package com.pagely.meetingservice.meeting.application.dto.result;

import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record MeetingScheduleResult(
        UUID id,
        UUID meetingId,
        int scheduleNumber,
        String bookId,
        MeetingScheduleStatus status,
        LocalDateTime startAt,
        String discussionNote,
        LocalDateTime createdAt,
        UUID createdBy
) {

    // 엔티티를 응답용 Result로 변환
    public static MeetingScheduleResult from(MeetingSchedule schedule) {
        return new MeetingScheduleResult(
                schedule.getId(),
                schedule.getMeetingId(),
                schedule.getScheduleNumber(),
                schedule.getBookId(),
                schedule.getStatus(),
                schedule.getStartAt(),
                schedule.getDiscussionNote(),
                schedule.getCreatedAt(),
                schedule.getCreatedBy()
        );
    }
}
package com.pagely.meetingservice.meeting.application.dto.command;

import java.time.LocalDateTime;
import java.util.UUID;

import com.pagely.meetingservice.meeting.domain.model.MeetingType;
import com.pagely.meetingservice.meeting.domain.model.ReadingLevel;
import com.pagely.meetingservice.meeting.domain.model.RecruitRate;

public record CreateMeetingCommand(
        UUID hostId, // 모임장 ID
        UUID bookId, // 책 ID
        String title, // 모임명
        String description, // 모임 설명
        MeetingType meetingType, // 모임 유형
        LocalDateTime recruitStartAt, // 모임 시작
        LocalDateTime recruitEndAt, // 모임 종료
        Integer recruitMax, // 모집 정원
        ReadingLevel readingLevel, // 독서 난이도
        String ruleMemo, // 규칙 메모
        RecruitRate recruitRate, // 모집 주기
        Boolean freePaid, // 무료/유료 여부
        UUID createdBy
    ) {
}

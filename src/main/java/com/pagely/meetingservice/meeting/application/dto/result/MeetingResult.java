package com.pagely.meetingservice.meeting.application.dto.result;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingType;
import com.pagely.meetingservice.meeting.domain.model.ReadingLevel;
import com.pagely.meetingservice.meeting.domain.model.RecruitRate;
import com.pagely.meetingservice.meeting.domain.model.RecruitStatus;
import java.time.LocalDateTime;
import java.util.UUID;

// 모임 상세 조회 결과 DTO
public record MeetingResult(
        UUID id, // 모임 ID
        String title, // 모임명
        String description, // 모임 설명
        UUID hostId, // 모임장 ID
        String bookId, // 책 ID
        MeetingType meetingType, // 모임 유형
        MeetingStatus meetingStatus, // 모임 상태
        RecruitStatus recruitStatus, // 모집 상태
        LocalDateTime recruitStartAt, // 모집 시작 일시
        LocalDateTime recruitEndAt, // 모집 종료 일시
        Integer recruitMax, // 모집 정원
        ReadingLevel readingLevel, // 독서 난이도
        String ruleMemo, // 규칙 메모
        RecruitRate recruitRate, // 모집 주기
        Boolean freePaid, // 무료/유료 여부
        LocalDateTime createdAt, // 생성 일시
        UUID createdBy // 생성자 ID
) {

    // Entity → application 결과 DTO 변환
    public static MeetingResult from(Meeting meeting) {
        return new MeetingResult(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getDescription(),
                meeting.getHostId(),
                meeting.getBookId(),
                meeting.getMeetingType(),
                meeting.getMeetingStatus(),
                meeting.getRecruitStatus(),
                meeting.getRecruitStartAt(),
                meeting.getRecruitEndAt(),
                meeting.getRecruitMax(),
                meeting.getReadingLevel(),
                meeting.getRuleMemo(),
                meeting.getRecruitRate(),
                meeting.isFreePaid(),
                meeting.getCreatedAt(),
                meeting.getCreatedBy()
        );
    }
}
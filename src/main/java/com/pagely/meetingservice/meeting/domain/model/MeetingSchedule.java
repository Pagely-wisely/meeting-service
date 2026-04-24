package com.pagely.meetingservice.meeting.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_meeting_schedules")
public class MeetingSchedule {

    @Id
    private UUID id;

    // 모임 ID
    @Column(name = "meeting_id", nullable = false)
    private UUID meetingId;

    // 회차 번호
    @Column(name = "schedule_number", nullable = false)
    private int scheduleNumber;

    // 해당 일정의 도서 ID
    @Column(name = "book_id")
    private String bookId;

    // 일정 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MeetingScheduleStatus status;

    // 시작 시각
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    // 토론 메모
    @Column(name = "discussion_note")
    private String discussionNote;

    // 공통 감사 컬럼
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    protected MeetingSchedule() {
    }
}
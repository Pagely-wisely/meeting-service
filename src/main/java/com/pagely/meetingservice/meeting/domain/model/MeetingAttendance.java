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
@Table(name = "p_meeting_attendance")
public class MeetingAttendance {

    @Id
    private UUID id;

    // 모임 ID
    @Column(name = "meeting_id", nullable = false)
    private UUID meetingId;

    // 일정 ID
    @Column(name = "schedule_id", nullable = false)
    private UUID scheduleId;

    // 출석 대상 유저 ID
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // 출석 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;

    // 출석 체크 시각
    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    // 비고
    @Column(name = "note", length = 255)
    private String note;

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

    protected MeetingAttendance() {
    }

    // 출석 상태 변경
    public void changeStatus(AttendanceStatus status, String note, UUID updatedBy) {
        this.status = status;
        this.note = note;
        this.checkedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }
}
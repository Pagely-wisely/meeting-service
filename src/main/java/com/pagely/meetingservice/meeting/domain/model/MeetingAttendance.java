package com.pagely.meetingservice.meeting.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

// 모임 일정 참석 엔티티
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

    // 참석 등록 유저 ID
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // 출석 상태
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "attendance_status")
    private AttendanceStatus status;

    // 출석 체크 시각
    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    // 비고
    @Column(name = "note", length = 255)
    private String note;

    // 생성일시
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 생성자 ID
    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    // 수정일시
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 수정자 ID
    @Column(name = "updated_by")
    private UUID updatedBy;

    // 삭제일시
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 삭제자 ID
    @Column(name = "deleted_by")
    private UUID deletedBy;

    protected MeetingAttendance() {
    }

    private MeetingAttendance(
            UUID id,
            UUID meetingId,
            UUID scheduleId,
            UUID userId,
            AttendanceStatus status,
            LocalDateTime checkedAt,
            String note,
            LocalDateTime createdAt,
            UUID createdBy
    ) {
        this.id = id;
        this.meetingId = meetingId;
        this.scheduleId = scheduleId;
        this.userId = userId;
        this.status = status;
        this.checkedAt = checkedAt;
        this.note = note;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    // 모임 일정 참석 등록 생성
    public static MeetingAttendance create(
            UUID id,
            UUID meetingId,
            UUID scheduleId,
            UUID userId,
            LocalDateTime now,
            UUID createdBy
    ) {
        return new MeetingAttendance(
                id,
                meetingId,
                scheduleId,
                userId,
                AttendanceStatus.PENDING,
                null,
                null,
                now,
                createdBy
        );
    }

    // 출석 상태 변경
    public void changeStatus(AttendanceStatus status, String note, UUID updatedBy) {
        this.status = status;
        this.note = note;
        this.checkedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getMeetingId() {
        return meetingId;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public UUID getUserId() {
        return userId;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }
}
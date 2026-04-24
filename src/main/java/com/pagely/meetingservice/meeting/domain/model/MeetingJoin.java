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
@Table(name = "p_meeting_recruit")
public class MeetingJoin {

    @Id
    private UUID id;

    // 신청 대상 모임 ID
    @Column(name = "meeting_id", nullable = false)
    private UUID meetingId;

    // 신청 유저 ID
    @Column(name = "recruit_user_id", nullable = false)
    private UUID recruitUserId;

    // 신청 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "recruit_status", nullable = false)
    private MeetingJoinStatus joinStatus;

    // 신청 내용
    @Column(name = "content")
    private String content;

    // 거절 사유
    @Column(name = "reject_reason")
    private String rejectReason;

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

    protected MeetingJoin() {
    }

    // 승인 처리
    public void approve(UUID approvedBy) {
        this.joinStatus = MeetingJoinStatus.APPROVED;
        this.updatedBy = approvedBy;
        this.updatedAt = LocalDateTime.now();
    }

    // 거절 처리
    public void reject(String rejectReason, UUID rejectedBy) {
        this.joinStatus = MeetingJoinStatus.REJECTED;
        this.rejectReason = rejectReason;
        this.updatedBy = rejectedBy;
        this.updatedAt = LocalDateTime.now();
    }
}
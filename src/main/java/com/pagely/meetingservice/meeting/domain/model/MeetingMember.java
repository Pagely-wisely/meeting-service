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

// 모임원 엔티티
@Entity
@Table(name = "p_meeting_members")
public class MeetingMember {

    @Id
    private UUID id;

    // 소속 모임 ID
    @Column(name = "meeting_id", nullable = false)
    private UUID meetingId;

    // 멤버 유저 ID
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // 모임원 역할
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false, columnDefinition = "meeting_member_role")
    private MeetingMemberRole role;

    // 모임원 상태
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "meeting_member_status")
    private MeetingMemberStatus status;

    // 결석 횟수
    @Column(name = "absent_count", nullable = false)
    private int absentCount;

    // 경고 횟수
    @Column(name = "warning_count", nullable = false)
    private int warningCount;

    // 생성 시각
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 생성자 ID
    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    // 수정 시각
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 수정자 ID
    @Column(name = "updated_by")
    private UUID updatedBy;

    // 삭제 시각
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 삭제자 ID
    @Column(name = "deleted_by")
    private UUID deletedBy;

    protected MeetingMember() {
    }

    // 활성 멤버인지 확인
    public boolean isActive() {
        return status == MeetingMemberStatus.ACTIVE;
    }

    // 모임장인지 확인
    public boolean isHost() {
        return role == MeetingMemberRole.HOST;
    }

    public static MeetingMember create(
            UUID id,
            UUID meetingId,
            UUID userId,
            MeetingMemberRole role,
            MeetingMemberStatus status,
            LocalDateTime createdAt,
            UUID createdBy
    ) {
        MeetingMember member = new MeetingMember();
        member.id = id;
        member.meetingId = meetingId;
        member.userId = userId;
        member.role = role;
        member.status = status;
        member.absentCount = 0;
        member.warningCount = 0;
        member.createdAt = createdAt;
        member.createdBy = createdBy;
        return member;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMeetingId() {
        return meetingId;
    }

    public UUID getUserId() {
        return userId;
    }

    public MeetingMemberRole getRole() {
        return role;
    }

    public MeetingMemberStatus getStatus() {
        return status;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

}
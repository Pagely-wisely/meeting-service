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

    // 멤버 역할
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MeetingMemberRole role;

    // 멤버 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MeetingMemberStatus status;

    // 결석 횟수
    @Column(name = "absent_count", nullable = false)
    private int absentCount;

    // 경고 횟수
    @Column(name = "warning_count", nullable = false)
    private int warningCount;

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

    protected MeetingMember() {
    }

    public static MeetingMember create(
        UUID id,
        UUID meetingId,
        UUID userId,
        MeetingMemberRole role,
        MeetingMemberStatus status,
        LocalDateTime createdAt,
        UUID createdBy
    ){
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

//    // 활성 멤버인지 확인
//    public boolean isActive() {
//        return status == MeetingMemberStatus.ACTIVE;
//    }
//
//    // 모임장인지 확인
//    public boolean isHost() {
//        return role == MeetingMemberRole.HOST;
//    }
}
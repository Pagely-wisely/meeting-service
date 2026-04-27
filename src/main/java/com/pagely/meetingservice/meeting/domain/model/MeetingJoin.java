package com.pagely.meetingservice.meeting.domain.model;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.domain.exception.MeetingJoinErrorCode;
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
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "recruit_status", nullable = false, columnDefinition = "meeting_join_status")
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

        if (this.joinStatus != MeetingJoinStatus.PENDING) {
            throw new BusinessException(MeetingJoinErrorCode.INVALID_JOIN_STATUS);
        }
        this.joinStatus = MeetingJoinStatus.APPROVED;
        this.updatedBy = approvedBy;
        this.updatedAt = LocalDateTime.now();
    }
//
//    // 거절 처리
//    public void reject(String rejectReason, UUID rejectedBy) {
//        this.joinStatus = MeetingJoinStatus.REJECTED;
//        this.rejectReason = rejectReason;
//        this.updatedBy = rejectedBy;
//        this.updatedAt = LocalDateTime.now();
//    }


    public static MeetingJoin create( // 가입 신청 생성 팩토리 메서드
                                      UUID id,
                                      UUID meetingId,
                                      UUID recruitUserId,
                                      String content,
                                      LocalDateTime createdAt,
                                      UUID createdBy
    ) {
        MeetingJoin join = new MeetingJoin();
        join.id = id;
        join.meetingId = meetingId;
        join.recruitUserId = recruitUserId;
        join.joinStatus = MeetingJoinStatus.PENDING; // 생성 시 기본 상태를 PENDING으로 고정
        join.content = content;
        join.rejectReason = null; // 생성 시 거절 사유 없음
        join.createdAt = createdAt;
        join.createdBy = createdBy;
        return join;

    }

    public UUID getId() {
        return id;
    }

    public UUID getMeetingId() {
        return meetingId;
    }

    public UUID getRecruitUserId() {
        return recruitUserId;
    }

    public MeetingJoinStatus getJoinStatus() {
        return joinStatus;
    }

    public String getContent() {
        return content;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }
}
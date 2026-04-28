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

import com.pagely.common.entity.BaseEntity;

// 모임원 엔티티
@Entity
@Table(name = "p_meeting_members")
public class MeetingMember extends BaseEntity {

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

    // 지각 횟수
    @Column(name = "late_count", nullable = false)
    private int lateCount;

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
        member.lateCount = 0;
        member.createdAt = createdAt;
        member.createdBy = createdBy;
        return member;
    }

    // 출석 상태에 따른 패널티 반영
    public void applyAttendancePenalty(AttendanceStatus attendanceStatus, UUID updatedBy) {
        // null 상태가 들어오면 패널티 기준을 판단할 수 없으므로 즉시 종료
        // 필요하면 BusinessException으로 바꿔도 됨
        if (attendanceStatus == null) {
            return;
        }

        // 정상 출석과 사유 인정 결석은 패널티 대상이 아님
        if (attendanceStatus == AttendanceStatus.ATTENDED
                || attendanceStatus == AttendanceStatus.EXCUSED) {
            return;
        }

        // 지각 처리
        if (attendanceStatus == AttendanceStatus.LATE) {
            this.lateCount++;

            // 현재 임시 정책:
            // 지각 1회는 경고 1회로 누적한다.
            this.warningCount++;

            // 지각 3회마다 결석 1회로 환산한다.
            // lateCount = 3  -> absentCount +1
            // lateCount = 6  -> absentCount +1
            // lateCount = 9  -> absentCount +1
            // 실제 LATE 상태를 ABSENT로 바꾸지는 않고,
            // 모임원 패널티 카운트에만 환산 결석을 누적한다.
            if (this.lateCount % 3 == 0) {
                this.absentCount++;
            }
        }

        // 결석 처리
        if (attendanceStatus == AttendanceStatus.ABSENT) {
            this.absentCount++;
        }

        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public int getLateCount() {
        return lateCount;
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
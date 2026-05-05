package com.pagely.meetingservice.meeting.domain.model;

import com.pagely.common.entity.BaseEntity;
import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.domain.exception.MeetingMemberErrorCode;
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

        // 출석 상태가 null이면 잘못된 이벤트이므로 즉시 예외 발생
        if (attendanceStatus == null) {
            throw new BusinessException(MeetingMemberErrorCode.INVALID_ATTENDANCE_STATUS);
        }

        // 정상 출석과 사유 인정 결석은 패널티 대상이 아님
        if (attendanceStatus == AttendanceStatus.ATTENDED
                || attendanceStatus == AttendanceStatus.EXCUSED) {
            return;
        }

        // 지각 처리
        // 정책:
        // - 지각 1회마다 lateCount만 증가
        // - 지각 3회 누적 시 경고 1회 증가
        if (attendanceStatus == AttendanceStatus.LATE) {
            this.lateCount++;

            // 지각 횟수가 3의 배수가 될 때마다 경고 1회 누적
            if (this.lateCount % 3 == 0) {
                this.warningCount++;
            }
        }

        // 결석 처리
        // 정책:
        // - 결석 1회 누적
        // - 결석 1회마다 경고 1회 증가
        if (attendanceStatus == AttendanceStatus.ABSENT) {
            this.absentCount++;
            this.warningCount++;
        }

        // 경고 3회 이상이면 모임 강퇴 처리
        // if (this.warningCount >= 3) {
        //     this.status = MeetingMemberStatus.EXPELLED;
        // }

        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    // 시스템 정책에 의한 자동 강퇴 처리
    public void expelBySystem(UUID updatedBy) {
        if (this.status == MeetingMemberStatus.EXPELLED) { // 이미 강퇴된 경우 강퇴 불가
            return;
        }
        if (this.role == MeetingMemberRole.HOST) { // 모임장인 경우 강퇴 불가
            return;
        }
        this.status = MeetingMemberStatus.EXPELLED;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    // 독후감 미작성에 따른 경고 1회 누적
    public void applyMissingReportWarning(UUID updatedBy) {
        // 활성 모임원이 아니면 경고 누적 대상이 아님
        if (!isActive()) {
            return;
        }

        this.warningCount++;

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

}

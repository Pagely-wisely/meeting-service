package com.pagely.meetingservice.meeting.domain.model;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.domain.exception.MeetingScheduleErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
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
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "meeting_schedule_status")
    private MeetingScheduleStatus status;

    // 시작 시각
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    // 토론 메모
    @Column(name = "discussion_note")
    private String discussionNote;

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

    protected MeetingSchedule() {
    }

    // 신규 모임 일정 생성
    public static MeetingSchedule create(
            UUID id,
            UUID meetingId,
            int scheduleNumber,
            String bookId,
            LocalDateTime startAt,
            String discussionNote,
            LocalDateTime createdAt,
            UUID createdBy
    ) {
        if (startAt == null) {
            throw new BusinessException(MeetingScheduleErrorCode.INVALID_SCHEDULE_START_AT);
        }

        MeetingSchedule schedule = new MeetingSchedule();
        schedule.id = id;
        schedule.meetingId = meetingId;
        schedule.scheduleNumber = scheduleNumber;
        schedule.bookId = bookId;
        schedule.status = MeetingScheduleStatus.SCHEDULED;
        schedule.startAt = startAt;
        schedule.discussionNote = discussionNote;
        schedule.createdAt = createdAt;
        schedule.createdBy = createdBy;
        return schedule;
    }

    // 모임 일정 상태 변경
    public void changeStatus(MeetingScheduleStatus newStatus, UUID updatedBy) {
        // null 상태가 들어오면 잘못된 상태가 엔티티에 저장될 수 있으므로 즉시 차단
        if (newStatus == null) {
            throw new BusinessException(MeetingScheduleErrorCode.INVALID_SCHEDULE_STATUS_CHANGE);
        }

        // 동일 상태로 변경하는 것은 허용하지 않음
        if (this.status == newStatus) {
            throw new BusinessException(MeetingScheduleErrorCode.INVALID_SCHEDULE_STATUS_CHANGE);
        }

        // FINISHED, CANCELLED 상태는 최종 상태이므로 더 이상 변경 불가
        if (this.status == MeetingScheduleStatus.FINISHED
                || this.status == MeetingScheduleStatus.CANCELLED) {
            throw new BusinessException(MeetingScheduleErrorCode.INVALID_SCHEDULE_STATUS_CHANGE);
        }

        // SCHEDULED 상태에서는 ONGOING 또는 CANCELLED 로만 변경 가능
        if (this.status == MeetingScheduleStatus.SCHEDULED) {
            if (newStatus != MeetingScheduleStatus.ONGOING
                    && newStatus != MeetingScheduleStatus.CANCELLED) {
                throw new BusinessException(MeetingScheduleErrorCode.INVALID_SCHEDULE_STATUS_CHANGE);
            }
        }

        // ONGOING 상태에서는 FINISHED 로만 변경 가능
        if (this.status == MeetingScheduleStatus.ONGOING) {
            if (newStatus != MeetingScheduleStatus.FINISHED) {
                throw new BusinessException(MeetingScheduleErrorCode.INVALID_SCHEDULE_STATUS_CHANGE);
            }
        }

        this.status = newStatus;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    // 모임 일정 종료 처리
    public void finish(List<MeetingAttendance> attendances, UUID updatedBy) {
        // 일정 상태를 FINISHED로 변경한다.
        // 상태 전이 규칙은 changeStatus() 내부에서 검증한다.
        changeStatus(MeetingScheduleStatus.FINISHED, updatedBy);

        // 일정 종료 시 출석 상태가 아직 PENDING인 참석자만 ABSENT로 확정한다.
        for (MeetingAttendance attendance : attendances) {
            attendance.markAbsentIfPending(updatedBy);
        }
    }

    // 진행중인 일정인지 확인
    public boolean isOngoing() {
        return status == MeetingScheduleStatus.ONGOING;
    }
}
package com.pagely.meetingservice.meeting.domain.model;

import com.pagely.common.entity.BaseEntity;
import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.domain.exception.MeetingScheduleErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "p_meeting_schedules")
public class MeetingSchedule extends BaseEntity {

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

        // SCHEDULED 상태에서는 ONGOING 또는 CANCELLED로만 변경 가능
        if (this.status == MeetingScheduleStatus.SCHEDULED) {
            if (newStatus != MeetingScheduleStatus.ONGOING
                    && newStatus != MeetingScheduleStatus.CANCELLED) {
                throw new BusinessException(MeetingScheduleErrorCode.INVALID_SCHEDULE_STATUS_CHANGE);
            }
        }

        // ONGOING 상태에서는 FINISHED로만 변경 가능
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
    public List<MeetingAttendance> finish(List<MeetingAttendance> attendances, UUID updatedBy) {
        // 일정 상태를 FINISHED로 변경한다.
        changeStatus(MeetingScheduleStatus.FINISHED, updatedBy);

        // 일정 종료로 인해 실제 ABSENT 처리된 출석 목록
        List<MeetingAttendance> changedAttendances = new ArrayList<>();

        for (MeetingAttendance attendance : attendances) {
            // PENDING인 참석자만 ABSENT로 변경된다.
            boolean changed = attendance.markAbsentIfPending(updatedBy);

            // 실제 상태가 변경된 출석만 이벤트 발행 대상으로 모은다.
            if (changed) {
                changedAttendances.add(attendance);
            }
        }

        return changedAttendances;
    }

    // 진행중인 일정인지 확인
    public boolean isOngoing() {
        return status == MeetingScheduleStatus.ONGOING;
    }

    // 시작 시간이 되었는지 확인
    public boolean isStartTimeReached(LocalDateTime now) {
        return this.status == MeetingScheduleStatus.SCHEDULED
                && !this.startAt.isAfter(now);
    }

    // 일정 진행 시작 처리
    public void start(UUID updatedBy) {
        // 상태 전이 규칙은 기존 changeStatus()를 재사용한다.
        changeStatus(MeetingScheduleStatus.ONGOING, updatedBy);
    }
}

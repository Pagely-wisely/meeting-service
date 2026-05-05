package com.pagely.meetingservice.meeting.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

// 모임 일정별 독후감 작성 기록 엔티티
@Entity
@Table(name = "p_meeting_schedule_report")
public class MeetingScheduleReport {

    @Id
    private UUID id;

    // Kafka 이벤트 중복 처리를 위한 이벤트 ID
    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    // report-service에서 생성된 독후감 ID
    @Column(name = "report_id", nullable = false)
    private UUID reportId;

    // 독후감이 작성된 모임 ID
    @Column(name = "meeting_id", nullable = false)
    private UUID meetingId;

    // 독후감이 작성된 일정 ID
    @Column(name = "schedule_id", nullable = false)
    private UUID scheduleId;

    // 독후감을 작성한 유저 ID
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // 독후감 작성 시각
    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    // meeting-service에 이벤트를 저장한 시각
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected MeetingScheduleReport() {
    }

    private MeetingScheduleReport(
            UUID id,
            String eventId,
            UUID reportId,
            UUID meetingId,
            UUID scheduleId,
            UUID userId,
            LocalDateTime reportedAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.eventId = eventId;
        this.reportId = reportId;
        this.meetingId = meetingId;
        this.scheduleId = scheduleId;
        this.userId = userId;
        this.reportedAt = reportedAt;
        this.createdAt = createdAt;
    }

    // 독후감 생성 이벤트를 기반으로 작성 기록 생성
    public static MeetingScheduleReport of(
            String eventId,
            UUID reportId,
            UUID meetingId,
            UUID scheduleId,
            UUID userId,
            LocalDateTime reportedAt
    ) {
        return new MeetingScheduleReport(
                UUID.randomUUID(),
                eventId,
                reportId,
                meetingId,
                scheduleId,
                userId,
                reportedAt,
                LocalDateTime.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public UUID getReportId() {
        return reportId;
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

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

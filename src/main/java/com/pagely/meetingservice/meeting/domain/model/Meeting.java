package com.pagely.meetingservice.meeting.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "p_meeting")
public class Meeting {

    @Id
    private UUID id;

    @Column(name = "host_id", nullable = false)
    private UUID hostId;

    @Column(name = "book_id")
    private UUID bookId;

    @Column(name = "title", nullable = false, length = 20)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "meeting_type", nullable = false, length = 20)
    private MeetingType meetingType;

    @Column(name = "meeting_status", nullable = false, length = 20)
    private MeetingStatus meetingStatus;

    @Column(name = "recruit_status", nullable = false, length = 20)
    private RecruitStatus recruitStatus;

    @Column(name = "recruit_start_at", nullable = false)
    private LocalDateTime recruitStartDate;

    @Column(name = "recruit_end_at", nullable = false)
    private LocalDateTime recruitEndDate;

    @Column(name = "recruit_max", nullable = false)
    private int recruitMax;

    @Column(name = "reading_level", nullable = false, length = 20)
    private ReadingLevel readingLevel;

    @Column(name = "rule_memo", columnDefinition = "TEXT")
    private String ruleMemo;

    @Column(name = "recruit_rate", nullable = false, length = 20)
    private RecruitRate recruitRate;

    @Column(name = "free_paid", nullable = false)
    private boolean freePaid;

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

    public enum MeetingType {
        ONES,
        REGULAR;
    }

    public enum MeetingStatus {
        UPCOMING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED;
    }

    public enum RecruitStatus {
        RECRUITING,
        FULL,
        CLOSED;
    }

    public enum ReadingLevel {
        BEGINNER,
        NORMAL,
        ADVANCED;
    }

    public enum RecruitRate {
        WEEKLY,
        BIWEEKLY,
        MONTHLY
    }
}

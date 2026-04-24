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
@Table(name = "p_meeting")
public class Meeting {

    @Id
    private UUID id;

    // 모임장 유저 ID
    @Column(name = "host_id", nullable = false)
    private UUID hostId;

    // 연관 도서 ID
    @Column(name = "book_id")
    private UUID bookId;

    // 모임 제목
    @Column(name = "title", nullable = false, length = 20)
    private String title;

    // 모임 설명
    @Column(name = "description")
    private String description;

    // 모임 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_type", nullable = false)
    private MeetingType meetingType;

    // 모임 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_status", nullable = false)
    private MeetingStatus meetingStatus;

    // 모집 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "recruit_status", nullable = false)
    private RecruitStatus recruitStatus;

    // 모집 시작 시각
    @Column(name = "recruit_start_at", nullable = false)
    private LocalDateTime recruitStartAt;

    // 모집 종료 시각
    @Column(name = "recruit_end_at", nullable = false)
    private LocalDateTime recruitEndAt;

    // 최대 모집 인원
    @Column(name = "recruit_max", nullable = false)
    private int recruitMax;

    // 독서 난이도
    @Enumerated(EnumType.STRING)
    @Column(name = "reading_level", nullable = false)
    private ReadingLevel readingLevel;

    // 모임 규칙 메모
    @Column(name = "rule_memo")
    private String ruleMemo;

    // 모임 주기
    @Enumerated(EnumType.STRING)
    @Column(name = "recruit_rate", nullable = false)
    private RecruitRate recruitRate;

    // 무료/유료 여부
    @Column(name = "free_paid", nullable = false)
    private boolean freePaid;

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

    protected Meeting() {
    }

    // // 소프트 삭제 여부 확인
    // public boolean isDeleted() {
    // return deletedAt != null;
    // }
    //
    // // 모집 가능 여부 확인
    // public boolean isRecruiting() {
    // return recruitStatus == RecruitStatus.RECRUITING;
    // }
    //
    // // 모임장 여부 확인
    // public boolean isHost(UUID userId) {
    // return hostId.equals(userId);
    // }

    // 신규 모임 생성 (문서: 생성 직후 UPCOMING / RECRUITING)
    public static Meeting create(
            UUID id,
            UUID hostId,
            UUID bookId,
            String title,
            String description,
            MeetingType meetingType,
            LocalDateTime recruitStartAt,
            LocalDateTime recruitEndAt,
            int recruitMax,
            ReadingLevel readingLevel,
            String ruleMemo,
            RecruitRate recruitRate,
            boolean freePaid,
            LocalDateTime createdAt,
            UUID createdBy) {
        if (recruitStartAt.isAfter(recruitEndAt)) {
            throw new IllegalArgumentException("모집 시작 시각은 종료 시각보다 늦을 수 없습니다.");
        }
        Meeting meeting = new Meeting();
        meeting.id = id;
        meeting.hostId = hostId;
        meeting.bookId = bookId;
        meeting.title = title;
        meeting.description = description;
        meeting.meetingType = meetingType;
        meeting.meetingStatus = MeetingStatus.UPCOMING;
        meeting.recruitStatus = RecruitStatus.RECRUITING;
        meeting.recruitStartAt = recruitStartAt;
        meeting.recruitEndAt = recruitEndAt;
        meeting.recruitMax = recruitMax;
        meeting.readingLevel = readingLevel;
        meeting.ruleMemo = ruleMemo;
        meeting.recruitRate = recruitRate;
        meeting.freePaid = freePaid;
        meeting.createdAt = createdAt;
        meeting.createdBy = createdBy;
        return meeting;
    }

    public UUID getId() {
        return id;
    }

    public UUID getHostId() {
        return hostId;
    }

    public UUID getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public MeetingType getMeetingType() {
        return meetingType;
    }

    public MeetingStatus getMeetingStatus() {
        return meetingStatus;
    }

    public RecruitStatus getRecruitStatus() {
        return recruitStatus;
    }

    public LocalDateTime getRecruitStartAt() {
        return recruitStartAt;
    }

    public LocalDateTime getRecruitEndAt() {
        return recruitEndAt;
    }

    public int getRecruitMax() {
        return recruitMax;
    }

    public ReadingLevel getReadingLevel() {
        return readingLevel;
    }

    public String getRuleMemo() {
        return ruleMemo;
    }

    public RecruitRate getRecruitRate() {
        return recruitRate;
    }

    public boolean isFreePaid() {
        return freePaid;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }
}

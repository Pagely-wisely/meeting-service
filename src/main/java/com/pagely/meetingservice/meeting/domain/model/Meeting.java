package com.pagely.meetingservice.meeting.domain.model;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.domain.exception.MeetingErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
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
    private String bookId;

    // 모임 제목
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    // 모임 설명
    @Column(name = "description")
    private String description;

    // 모임 유형
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "meeting_type", nullable = false, columnDefinition = "meeting_type")
    private MeetingType meetingType;

    // 모임 상태
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "meeting_status", nullable = false, columnDefinition = "meeting_status")
    private MeetingStatus meetingStatus;

    // 모집 상태
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "recruit_status", nullable = false, columnDefinition = "recruit_status")
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
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "reading_level", nullable = false, columnDefinition = "reading_level")
    private ReadingLevel readingLevel;

    // 모임 규칙 메모
    @Column(name = "rule_memo")
    private String ruleMemo;

    // 모임 주기
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "recruit_rate", nullable = false, columnDefinition = "recruit_rate")
    private RecruitRate recruitRate;

    // 무료/유료 여부
    @Column(name = "free_paid", nullable = false)
    private boolean freePaid;

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

    protected Meeting() {
    }

    // 신규 모임 생성
    public static Meeting create(
            UUID id,
            UUID hostId,
            String bookId,
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
            UUID createdBy
    ) {
        if (recruitMax <= 0) {
            // TODO: 에러처리
            throw new BusinessException(MeetingErrorCode.INVALID_RECRUIT_MAX);
        }
        if (recruitStartAt.isAfter(recruitEndAt)) {
            // TODO: 에러처리
            throw new BusinessException(MeetingErrorCode.INVALID_RECRUIT_PERIOD);
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

    public String getBookId() {
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

    public boolean canViewJoinApplications(UUID userId) {
        return this.hostId.equals(userId);
    }
}
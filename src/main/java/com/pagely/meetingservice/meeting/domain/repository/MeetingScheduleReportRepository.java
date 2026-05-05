package com.pagely.meetingservice.meeting.domain.repository;

import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleReport;
import java.util.List;
import java.util.UUID;

// 모임 일정별 독후감 작성 기록 Repository
public interface MeetingScheduleReportRepository {

    // 독후감 작성 기록 저장
    MeetingScheduleReport save(MeetingScheduleReport report);

    // 특정 일정에서 독후감을 작성한 유저 ID 목록 조회
    List<UUID> findUserIdsByScheduleId(UUID scheduleId);

    // 특정 일정에서 특정 유저가 독후감을 작성했는지 확인
    boolean existsByScheduleIdAndUserId(UUID scheduleId, UUID userId);
}

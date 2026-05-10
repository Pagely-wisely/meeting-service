package com.pagely.meetingservice.meeting.application.port;

import java.time.LocalDateTime;
import java.util.UUID;

// 일정 자동 시작 Job 관리 포트
public interface ScheduleJobManager {

    // 일정 시작 Job 등록
    void scheduleStartJob(UUID scheduleId, LocalDateTime startAt);

    // 일정 시작 Job 삭제
    void deleteStartJob(UUID scheduleId);
}

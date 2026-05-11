package com.pagely.meetingservice.meeting.infrastructure.scheduler.quartz;

import java.util.UUID;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

// Quartz Job/Trigger Key 생성기
public final class QuartzJobKeyGenerator {

    private static final String GROUP = "meeting-schedule-start";

    private QuartzJobKeyGenerator() {
    }

    // 일정 시작 Job Key 생성
    public static JobKey startJobKey(UUID scheduleId) {
        return JobKey.jobKey("schedule-start-" + scheduleId, GROUP);
    }

    // 일정 시작 Trigger Key 생성
    public static TriggerKey startTriggerKey(UUID scheduleId) {
        return TriggerKey.triggerKey("schedule-start-trigger-" + scheduleId, GROUP);
    }
}

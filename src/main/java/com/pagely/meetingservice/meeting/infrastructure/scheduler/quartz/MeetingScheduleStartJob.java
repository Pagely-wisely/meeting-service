package com.pagely.meetingservice.meeting.infrastructure.scheduler.quartz;

import com.pagely.meetingservice.meeting.application.service.MeetingScheduleAutoStartService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

// Quartz가 실행하는 일정 자동 시작 Job
@Component
@RequiredArgsConstructor
public class MeetingScheduleStartJob implements Job {

    public static final String SCHEDULE_ID = "scheduleId";

    private final MeetingScheduleAutoStartService meetingScheduleAutoStartService;

    @Override
    public void execute(JobExecutionContext context) {
        // JobDataMap에서 scheduleId를 꺼내 단일 일정 자동 시작을 실행한다.
        String scheduleId = context.getMergedJobDataMap().getString(SCHEDULE_ID);
        meetingScheduleAutoStartService.startSchedule(UUID.fromString(scheduleId));
    }
}

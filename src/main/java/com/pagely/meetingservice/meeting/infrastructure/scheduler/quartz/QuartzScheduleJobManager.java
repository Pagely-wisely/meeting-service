package com.pagely.meetingservice.meeting.infrastructure.scheduler.quartz;

import com.pagely.meetingservice.meeting.application.port.ScheduleJobManager;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Component;

// Quartz 기반 일정 Job 관리 어댑터
@Component
@RequiredArgsConstructor
public class QuartzScheduleJobManager implements ScheduleJobManager {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final Scheduler scheduler;

    @Override
    public void scheduleStartJob(UUID scheduleId, LocalDateTime startAt) {
        try {
            // 기존 Job이 있으면 삭제하고 다시 등록한다. deleteJob은 없어도 안전하게 동작한다.
            scheduler.deleteJob(QuartzJobKeyGenerator.startJobKey(scheduleId));

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(MeetingScheduleStartJob.SCHEDULE_ID, scheduleId.toString());

            var jobDetail = JobBuilder.newJob(MeetingScheduleStartJob.class)
                    .withIdentity(QuartzJobKeyGenerator.startJobKey(scheduleId))
                    .usingJobData(jobDataMap)
                    .build();

            Date startDate = Date.from(
                    startAt.atZone(SERVICE_ZONE_ID).toInstant()
            );

            var trigger = TriggerBuilder.newTrigger()
                    .withIdentity(QuartzJobKeyGenerator.startTriggerKey(scheduleId))
                    .forJob(jobDetail)
                    .startAt(startDate)
                    .build();

            // startAt 시각에 단일 일정 시작 Job을 실행하도록 등록한다.
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            throw new IllegalStateException("일정 자동 시작 Job 등록에 실패했습니다.", e);
        }
    }

    @Override
    public void deleteStartJob(UUID scheduleId) {
        try {
            // 취소/종료된 일정의 자동 시작 Job을 제거한다.
            scheduler.deleteJob(QuartzJobKeyGenerator.startJobKey(scheduleId));
        } catch (Exception e) {
            throw new IllegalStateException("일정 자동 시작 Job 삭제에 실패했습니다.", e);
        }
    }
}

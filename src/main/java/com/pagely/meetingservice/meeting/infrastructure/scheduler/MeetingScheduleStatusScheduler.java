package com.pagely.meetingservice.meeting.infrastructure.scheduler;

import com.pagely.meetingservice.meeting.application.service.MeetingScheduleAutoStartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 모임 일정 상태 자동 변경 스케줄러
@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingScheduleStatusScheduler {

    private final MeetingScheduleAutoStartService meetingScheduleAutoStartService;

    // 1분마다 시작 시간이 지난 일정을 ONGOING으로 변경한다.
    @Scheduled(cron = "${meeting.schedule.auto-start-cron:0 * * * * *}")
    public void startDueSchedules() {
        log.debug("모임 일정 자동 시작 스케줄러 실행");
        meetingScheduleAutoStartService.startDueSchedules();
    }
}
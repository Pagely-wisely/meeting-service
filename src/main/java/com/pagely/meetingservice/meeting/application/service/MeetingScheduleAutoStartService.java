package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.port.EventPublisher;
import com.pagely.meetingservice.meeting.domain.event.MeetingScheduleStatusChangedEvent;
import com.pagely.meetingservice.meeting.domain.exception.MeetingErrorCode;
import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingScheduleRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 일정 자동 시작 처리 서비스
@Service
@RequiredArgsConstructor
public class MeetingScheduleAutoStartService {

    private static final int BATCH_SIZE = 100;

    private final MeetingScheduleRepository meetingScheduleRepository;
    private final MeetingRepository meetingRepository;
    private final EventPublisher eventPublisher;

    // 시스템 자동 처리용 사용자 ID
    private static final UUID SYSTEM_USER_ID = new UUID(0L, 0L);

    // 시작 시간이 지난 일정을 ONGOING으로 자동 변경한다.
    @Transactional
    public void startDueSchedules() {
        LocalDateTime now = LocalDateTime.now();

        // 전체 일정을 조회하지 않고, 시작 시간이 지난 SCHEDULED 일정만 제한적으로 조회한다.
        List<MeetingSchedule> schedules = meetingScheduleRepository.findStartDueSchedules(
                now,
                PageRequest.of(0, BATCH_SIZE)
        );

        for (MeetingSchedule schedule : schedules) {
            startSchedule(schedule, now);
        }
    }

    // 단일 일정 자동 시작 처리
    private void startSchedule(MeetingSchedule schedule, LocalDateTime now) {
        // 조회 이후 상태가 바뀐 경우를 대비해 한 번 더 검증한다.
        if (!schedule.isStartTimeReached(now)) {
            return;
        }

        // 일정 상태를 ONGOING으로 변경한다.
        schedule.start(SYSTEM_USER_ID);

        // 일정이 시작되면 해당 모임도 진행 중으로 변경한다.
        Meeting meeting = meetingRepository.findByIdForUpdate(schedule.getMeetingId())
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        meeting.start(SYSTEM_USER_ID);

        // 일정 상태 변경 이벤트를 발행한다.
        eventPublisher.publish(MeetingScheduleStatusChangedEvent.of(schedule));
    }
}
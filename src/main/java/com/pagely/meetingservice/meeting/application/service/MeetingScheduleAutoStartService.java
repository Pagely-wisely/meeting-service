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

    // 시스템 자동 처리용 사용자 ID
    private static final UUID SYSTEM_USER_ID = new UUID(0L, 0L);

    private final MeetingScheduleRepository meetingScheduleRepository;
    private final MeetingRepository meetingRepository;
    private final EventPublisher eventPublisher;
    private final MissingReportPenaltyService missingReportPenaltyService;

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
            startSchedule(schedule.getId(), now);
        }
    }

    // 단일 일정 자동 시작 처리
    private void startSchedule(UUID scheduleId, LocalDateTime now) {
        // 일정 상태 변경 중복을 막기 위해 쓰기 락으로 다시 조회한다.
        MeetingSchedule schedule = meetingScheduleRepository.findByIdForUpdate(scheduleId)
                .orElse(null);

        // 조회 시점 사이에 삭제되었거나 존재하지 않으면 스킵
        if (schedule == null) {
            return;
        }

        // 이미 수동으로 ONGOING 처리되었거나, CANCELLED/FINISHED 된 경우에는 스킵
        // isStartTimeReached 내부에서 SCHEDULED 상태인지도 함께 검증한다.
        if (!schedule.isStartTimeReached(now)) {
            return;
        }

        // 일정 상태를 ONGOING으로 변경한다.
        schedule.start(SYSTEM_USER_ID);

        // 일정 시작 시점에 독후감 미작성자에게 경고를 누적한다.
        missingReportPenaltyService.applyMissingReportWarnings(
                schedule.getMeetingId(),
                schedule.getId()
        );

        // 일정이 시작되면 해당 모임도 진행 중으로 변경한다.
        Meeting meeting = meetingRepository.findByIdForUpdate(schedule.getMeetingId())
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        meeting.start(SYSTEM_USER_ID);

        // 일정 상태 변경 이벤트를 발행한다.
        eventPublisher.publish(MeetingScheduleStatusChangedEvent.of(schedule));
    }
}

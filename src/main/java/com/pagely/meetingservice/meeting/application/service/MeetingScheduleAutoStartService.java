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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Quartz Job에서 호출되는 일정 자동 시작 서비스
@Service
@RequiredArgsConstructor
public class MeetingScheduleAutoStartService {

    // 시스템 자동 처리용 사용자 ID
    private static final UUID SYSTEM_USER_ID = new UUID(0L, 0L);

    private final MeetingScheduleRepository meetingScheduleRepository;
    private final MeetingRepository meetingRepository;
    private final EventPublisher eventPublisher;
    private final MissingReportPenaltyService missingReportPenaltyService;

    // 단일 일정을 자동 시작한다.
    @Transactional
    public void startSchedule(UUID scheduleId) {
        LocalDateTime now = LocalDateTime.now();

        // 동시 상태 변경을 막기 위해 쓰기 락으로 조회한다.
        MeetingSchedule schedule = meetingScheduleRepository.findByIdForUpdate(scheduleId)
                .orElse(null);

        // 삭제되었거나 존재하지 않는 일정이면 무시한다.
        if (schedule == null) {
            return;
        }

        // 이미 시작/종료/취소되었거나 아직 시작 시간이 아니면 무시한다.
        if (!schedule.isStartTimeReached(now)) {
            return;
        }

        // 일정 상태를 ONGOING으로 변경한다.
        schedule.start(SYSTEM_USER_ID);

        // 일정 시작 시점에 독후감 미작성 경고를 처리한다.
        missingReportPenaltyService.applyMissingReportWarnings(
                schedule.getMeetingId(),
                schedule.getId()
        );

        // 일정이 시작되면 모임도 진행 중으로 변경한다.
        Meeting meeting = meetingRepository.findByIdForUpdate(schedule.getMeetingId())
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        meeting.start(SYSTEM_USER_ID);

        // 일정 상태 변경 이벤트를 발행한다.
        eventPublisher.publish(MeetingScheduleStatusChangedEvent.of(schedule));
    }
}

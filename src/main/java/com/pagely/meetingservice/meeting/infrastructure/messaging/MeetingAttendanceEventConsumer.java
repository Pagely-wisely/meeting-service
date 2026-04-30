package com.pagely.meetingservice.meeting.infrastructure.messaging;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.domain.event.MeetingAttendanceStatusChangedEvent;
import com.pagely.meetingservice.meeting.domain.exception.MeetingMemberErrorCode;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 출석 관련 Kafka 이벤트 Consumer
@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingAttendanceEventConsumer {

    private final MeetingMemberRepository meetingMemberRepository;

    // 출석 상태 변경 이벤트 수신
    // 출석 상태가 변경되면 해당 모임원의 지각/결석/경고 카운트를 누적한다.
    @Transactional
    @KafkaListener(
            topics = "meeting.attendance.status-changed",
            groupId = "meeting-service"
    )
    public void consumeAttendanceStatusChanged(
            MeetingAttendanceStatusChangedEvent event
    ) {
        MeetingAttendanceStatusChangedEvent.Payload payload =
                (MeetingAttendanceStatusChangedEvent.Payload) event.getPayload();

        log.info(
                "출석 상태 변경 이벤트 수신: attendanceId={}, meetingId={}, userId={}, status={}",
                payload.attendanceId(),
                payload.meetingId(),
                payload.userId(),
                payload.status()
        );

        // 이벤트 payload의 meetingId, userId 기준으로 모임원을 조회한다.
        MeetingMember member = meetingMemberRepository.findByMeetingIdAndUserId(
                        payload.meetingId(),
                        payload.userId()
                )
                .orElseThrow(() -> new BusinessException(MeetingMemberErrorCode.MEETING_MEMBER_NOT_FOUND));

        // 비활성 모임원은 패널티 누적 대상에서 제외한다.
        if (!member.isActive()) {
            log.warn(
                    "비활성 모임원 출석 패널티 누적 제외: meetingId={}, userId={}, status={}",
                    payload.meetingId(),
                    payload.userId(),
                    member.getStatus()
            );
            return;
        }

        // 출석 상태에 따라 지각/결석/경고 카운트를 누적한다.
        member.applyAttendancePenalty(
                payload.status(),
                payload.changedBy()
        );

        log.info(
                "출석 패널티 누적 완료: meetingId={}, userId={}, status={}, lateCount={}, absentCount={}, warningCount={}",
                payload.meetingId(),
                payload.userId(),
                payload.status(),
                member.getLateCount(),
                member.getAbsentCount(),
                member.getWarningCount()
        );
    }
}
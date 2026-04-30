package com.pagely.meetingservice.meeting.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.domain.exception.MeetingMemberErrorCode;
import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import java.util.UUID;
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
    private final ObjectMapper objectMapper;

    // 출석 상태 변경 이벤트 수신
    // Kafka 메시지가 JSON 문자열로 들어오기 때문에 String으로 받은 뒤 payload만 직접 파싱한다.
    @Transactional
    @KafkaListener(
            topics = "meeting.attendance.status-changed",
            groupId = "meeting-service"
    )
    public void consumeAttendanceStatusChanged(String message) {
        try {
            // Kafka로 수신한 전체 이벤트 JSON 파싱
            JsonNode root = objectMapper.readTree(message);

            // BaseEvent 내부의 payload 필드 추출
            JsonNode payload = root.get("payload");

            UUID attendanceId = UUID.fromString(payload.get("attendanceId").asText());
            UUID meetingId = UUID.fromString(payload.get("meetingId").asText());
            UUID userId = UUID.fromString(payload.get("userId").asText());
            UUID changedBy = UUID.fromString(payload.get("changedBy").asText());
            AttendanceStatus status = AttendanceStatus.valueOf(payload.get("status").asText());

            log.info(
                    "출석 상태 변경 이벤트 수신: attendanceId={}, meetingId={}, userId={}, status={}",
                    attendanceId,
                    meetingId,
                    userId,
                    status
            );

            // 이벤트 payload의 meetingId, userId 기준으로 모임원을 조회한다.
            MeetingMember member = meetingMemberRepository.findByMeetingIdAndUserId(
                            meetingId,
                            userId
                    )
                    .orElseThrow(() -> new BusinessException(MeetingMemberErrorCode.MEETING_MEMBER_NOT_FOUND));

            // 비활성 모임원은 패널티 누적 대상에서 제외한다.
            if (!member.isActive()) {
                log.warn(
                        "비활성 모임원 출석 패널티 누적 제외: meetingId={}, userId={}, memberStatus={}",
                        meetingId,
                        userId,
                        member.getStatus()
                );
                return;
            }

            // 패널티 적용 전 현재 카운트 확인
// 지각 1회에 warningCount가 증가하는지,
// 아니면 적용 전부터 warningCount가 이미 1이었는지 확인하기 위한 로그
            log.info(
                    "출석 패널티 적용 전: meetingId={}, userId={}, status={}, lateCount={}, absentCount={}, warningCount={}",
                    meetingId,
                    userId,
                    status,
                    member.getLateCount(),
                    member.getAbsentCount(),
                    member.getWarningCount()
            );

// 출석 상태에 따라 지각/결석/경고 카운트를 누적한다.
// 실제 누적 규칙은 MeetingMember 도메인 메서드에 위임한다.
            member.applyAttendancePenalty(status, changedBy);

// 패널티 적용 후 카운트 확인
            log.info(
                    "출석 패널티 적용 후: meetingId={}, userId={}, status={}, lateCount={}, absentCount={}, warningCount={}",
                    meetingId,
                    userId,
                    status,
                    member.getLateCount(),
                    member.getAbsentCount(),
                    member.getWarningCount()
            );

            log.info(
                    "출석 패널티 누적 완료: meetingId={}, userId={}, status={}, lateCount={}, absentCount={}, warningCount={}",
                    meetingId,
                    userId,
                    status,
                    member.getLateCount(),
                    member.getAbsentCount(),
                    member.getWarningCount()
            );

        } catch (Exception e) {
            // 이벤트 처리 중 예외가 발생하면 로그를 남기고 예외를 다시 던진다.
            // 그래야 Kafka listener가 실패 이벤트로 인식할 수 있다.
            log.error("출석 상태 변경 이벤트 처리 실패: message={}", message, e);
            throw new IllegalStateException("출석 상태 변경 이벤트 처리 실패", e);
        }
    }
}
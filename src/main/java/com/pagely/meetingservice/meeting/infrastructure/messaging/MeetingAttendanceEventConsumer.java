package com.pagely.meetingservice.meeting.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.service.WarningThresholdService;
import com.pagely.meetingservice.meeting.domain.exception.MeetingMemberErrorCode;
import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.ProcessedEvent;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import com.pagely.meetingservice.meeting.domain.repository.ProcessedEventRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 출석 관련 Kafka 이벤트 Consumer
@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingAttendanceEventConsumer {

    private static final String CONSUMER_NAME = "meeting-attendance-event-consumer";

    private final MeetingMemberRepository meetingMemberRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final WarningThresholdService warningThresholdService;

    @Qualifier("kafkaConsumerObjectMapper")
    private final ObjectMapper objectMapper;

    // 출석 상태 변경 이벤트 수신
    // Kafka 메시지가 JSON 문자열로 들어오기 때문에 String으로 받은 뒤 payload만 직접 파싱한다.
    @Transactional
    @KafkaListener(
            topics = "meeting.attendance.status-changed",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeAttendanceStatusChanged(String message) {
        String eventId = null;
        UUID attendanceId = null;

        try {
            // Kafka로 수신한 전체 이벤트 JSON 파싱
            JsonNode root = objectMapper.readTree(message);

            // 멱등성 기준은 BaseEvent의 eventId를 사용한다.
            eventId = requiredText(root, "eventId");

            // 이미 처리 완료된 이벤트면 중복 처리하지 않는다.
            if (isAlreadyProcessed(eventId)) {
                log.info(
                        "이미 처리된 출석 이벤트 skip: consumerName={}, eventId={}",
                        CONSUMER_NAME,
                        eventId
                );
                return;
            }

            // BaseEvent 내부 payload 필드 추출
            JsonNode payload = root.get("payload");
            if (payload == null || payload.isNull()) {
                throw new IllegalArgumentException("Missing required field: payload");
            }

            attendanceId = UUID.fromString(requiredText(payload, "attendanceId"));
            UUID meetingId = UUID.fromString(requiredText(payload, "meetingId"));
            UUID userId = UUID.fromString(requiredText(payload, "userId"));
            UUID changedBy = UUID.fromString(requiredText(payload, "changedBy"));
            AttendanceStatus status = AttendanceStatus.valueOf(requiredText(payload, "status"));

            log.info(
                    "출석 상태 변경 이벤트 수신: eventId={}, attendanceId={}, meetingId={}, userId={}, status={}",
                    eventId,
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
                        "비활성 모임원 출석 패널티 누적 제외: eventId={}, attendanceId={}, meetingId={}, userId={}, memberStatus={}",
                        eventId,
                        attendanceId,
                        meetingId,
                        userId,
                        member.getStatus()
                );

                // 재처리해도 결과가 같으므로 처리 완료로 기록한다.
                saveProcessedEvent(eventId);
                return;
            }

            // 패널티 적용 전 현재 카운트 확인
            log.info(
                    "출석 패널티 적용 전: eventId={}, attendanceId={}, lateCount={}, absentCount={}, warningCount={}",
                    eventId,
                    attendanceId,
                    member.getLateCount(),
                    member.getAbsentCount(),
                    member.getWarningCount()
            );

            // 출석 상태에 따라 지각/결석/경고 카운트를 누적한다.
            // 실제 누적 규칙은 MeetingMember 도메인 메서드에 위임한다.
            member.applyAttendancePenalty(status, changedBy);

            meetingMemberRepository.save(member);

            // 누적 반영 후 경고 횟수로 강퇴 여부만 처리
            warningThresholdService.handleAttendanceStatusChanged(eventId, meetingId, userId);

            // 비즈니스 처리가 성공한 뒤 처리 완료 이벤트를 저장한다.
            saveProcessedEvent(eventId);

            // 패널티 적용 후 카운트 확인
            log.info(
                    "출석 패널티 적용 후: eventId={}, attendanceId={}, lateCount={}, absentCount={}, warningCount={}, memberStatus={}",
                    eventId,
                    attendanceId,
                    member.getLateCount(),
                    member.getAbsentCount(),
                    member.getWarningCount(),
                    member.getStatus()
            );

        } catch (DataIntegrityViolationException e) {
            // 동시에 같은 이벤트가 처리되어 unique 제약이 발생한 경우 중복 수신으로 보고 skip한다.
            log.info(
                    "이미 처리된 출석 이벤트로 판단하여 skip: consumerName={}, eventId={}, attendanceId={}",
                    CONSUMER_NAME,
                    eventId,
                    attendanceId
            );

        } catch (BusinessException | IllegalArgumentException e) {
            // payload 필드 누락/공백, UUID 오류, enum 오류, 모임원 없음 등은 재시도해도 성공하지 않는 비재시도성 오류
            // 원본 message에는 note 등 사용자 입력값이 포함될 수 있으므로 로그에 남기지 않는다.
            log.warn(
                    "출석 이벤트 비재시도성 실패: eventId={}, attendanceId={}, reason={}",
                    eventId,
                    attendanceId,
                    e.getMessage(),
                    e
            );

        } catch (Exception e) {
            // DB 일시 장애 등 예측 불가 오류는 재시도 대상
            // 원본 message 전체는 개인정보/메모 노출 위험이 있으므로 로그에 남기지 않는다.
            log.error(
                    "출석 상태 변경 이벤트 처리 실패: eventId={}, attendanceId={}",
                    eventId,
                    attendanceId,
                    e
            );
            throw new IllegalStateException("출석 상태 변경 이벤트 처리 실패", e);
        }
    }

    // 이미 처리한 이벤트인지 확인한다.
    private boolean isAlreadyProcessed(String eventId) {
        return processedEventRepository.existsByConsumerNameAndEventId(CONSUMER_NAME, eventId);
    }

    // 처리 성공 후 이벤트 처리 이력을 저장한다.
    private void saveProcessedEvent(String eventId) {
        processedEventRepository.save(
                ProcessedEvent.of(CONSUMER_NAME, eventId)
        );
    }

    // 필수 문자열 필드 추출
    // 필드가 없거나 null 또는 공백이면 비재시도성 예외로 분류되도록 IllegalArgumentException을 발생시킨다.
    private String requiredText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);

        if (value == null || value.isNull()) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }

        String text = value.asText();

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Blank required field: " + fieldName);
        }

        return text;
    }
}

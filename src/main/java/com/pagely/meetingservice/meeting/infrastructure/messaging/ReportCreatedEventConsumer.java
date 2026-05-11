package com.pagely.meetingservice.meeting.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleReport;
import com.pagely.meetingservice.meeting.domain.model.ProcessedEvent;
import com.pagely.meetingservice.meeting.domain.repository.MeetingScheduleReportRepository;
import com.pagely.meetingservice.meeting.domain.repository.ProcessedEventRepository;
import com.pagely.meetingservice.meeting.infrastructure.messaging.dto.ReportCreatedKafkaMessage;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 독후감 생성 Kafka 이벤트 Consumer
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportCreatedEventConsumer {

    private static final String CONSUMER_NAME = "report-created-event-consumer";

    private final MeetingScheduleReportRepository meetingScheduleReportRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Qualifier("kafkaConsumerObjectMapper")
    private final ObjectMapper objectMapper;

    // 독후감 생성 이벤트 수신
    @Transactional
    @KafkaListener(
            topics = "report-created",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeReportCreated(String message) {
        String eventId = null;

        try {
            // Kafka 메시지를 DTO로 역직렬화
            ReportCreatedKafkaMessage event = objectMapper.readValue(
                    message,
                    ReportCreatedKafkaMessage.class
            );

            eventId = event.eventId();

            // 이미 처리 완료된 이벤트면 중복 처리하지 않는다.
            if (isAlreadyProcessed(eventId)) {
                log.info(
                        "이미 처리된 독후감 생성 이벤트 skip: consumerName={}, eventId={}",
                        CONSUMER_NAME,
                        eventId
                );
                return;
            }

            ReportCreatedKafkaMessage.Payload payload = event.payload();

            // payload가 없으면 처리할 수 없는 이벤트이므로 스킵
            if (payload == null) {
                log.warn("독후감 생성 이벤트 payload 누락: eventId={}", eventId);

                // 재처리해도 성공 가능성이 낮으므로 처리 완료로 기록한다.
                saveProcessedEvent(eventId);
                return;
            }

            // 모임 독후감이 아닌 경우에는 meeting-service에서 관리할 필요가 없으므로 스킵
            if (payload.meetingId() == null || payload.meetingScheduleId() == null) {
                log.info(
                        "모임 독후감이 아니므로 독후감 작성 기록 저장 생략: eventId={}, reportId={}",
                        eventId,
                        payload.reportId()
                );

                // 처리 대상이 아니므로 처리 완료로 기록한다.
                saveProcessedEvent(eventId);
                return;
            }

            // 독후감 작성 기록 생성
            MeetingScheduleReport report = MeetingScheduleReport.of(
                    eventId,
                    payload.reportId(),
                    payload.meetingId(),
                    payload.meetingScheduleId(),
                    payload.userId(),
                    payload.createdAt() == null ? LocalDateTime.now() : payload.createdAt()
            );

            // meeting-service DB에 독후감 작성 여부를 저장
            meetingScheduleReportRepository.save(report);

            // 비즈니스 처리가 성공한 뒤 처리 완료 이벤트를 저장한다.
            saveProcessedEvent(eventId);

            log.info(
                    "독후감 작성 기록 저장 완료: eventId={}, reportId={}, meetingId={}, scheduleId={}, userId={}",
                    eventId,
                    payload.reportId(),
                    payload.meetingId(),
                    payload.meetingScheduleId(),
                    payload.userId()
            );

        } catch (DataIntegrityViolationException e) {
            // 동일 이벤트 또는 동일 일정/유저 독후감이 이미 저장된 경우
            // Kafka 중복 수신으로 보고 정상 스킵
            log.info("이미 저장된 독후감 생성 이벤트이므로 skip: eventId={}", eventId);

        } catch (Exception e) {
            // 일시적 DB 장애 등은 Kafka 재시도 대상
            log.error("독후감 생성 이벤트 처리 실패: eventId={}", eventId, e);
            throw new IllegalStateException("독후감 생성 이벤트 처리 실패", e);
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
}

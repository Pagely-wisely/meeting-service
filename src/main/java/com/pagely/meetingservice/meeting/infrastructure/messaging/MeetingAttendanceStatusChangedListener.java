package com.pagely.meetingservice.meeting.infrastructure.messaging;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pagely.meetingservice.meeting.application.service.WarningThresholdService;
import com.pagely.meetingservice.meeting.infrastructure.messaging.dto.MeetingAttendanceStatusChangedKafkaMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class MeetingAttendanceStatusChangedListener {

    private static final String TOPIC = "meeting.attendance.status-changed";

    @Qualifier("kafkaConsumerObjectMapper")
    private final ObjectMapper objectMapper;
    private final WarningThresholdService warningThresholdService;

    @KafkaListener(
        topics = TOPIC,
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(String json){ // String 수동 파싱
        try {
            MeetingAttendanceStatusChangedKafkaMessage message = objectMapper.readValue(
                json,
                MeetingAttendanceStatusChangedKafkaMessage.class  // 원본 String -> 객체 변환
            );
            if(!"MeetingAttendanceStatusChangedEvent".equals(message.eventType())){ // 이벤트 타입 검증
                log.warn("Unexpected event type: {}, skip", message.eventType());
                return;
            }
            warningThresholdService.handleAttendanceStatusChanged( 
                message.eventId(),
                message.payload().meetingId(),
                message.payload().userId()
            );

        } catch (Exception ex) {
            log.error("Failed to process attendance status changed message",ex);
            throw new RuntimeException(ex);
        }
    }
    
}

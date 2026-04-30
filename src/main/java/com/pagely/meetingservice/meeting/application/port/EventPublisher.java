package com.pagely.meetingservice.meeting.application.port;

import com.pagely.meetingservice.meeting.domain.event.BaseEvent;

// 이벤트 발행 책임을 추상화한 포트
// 애플리케이션 서비스는 Kafka를 직접 알지 않고 이 인터페이스에만 의존한다.
public interface EventPublisher {

    // 도메인 이벤트를 외부 메시지 브로커로 발행한다.
    void publish(BaseEvent event);
}
package com.pagely.meetingservice.meeting.infrastructure.messaging.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaProducerConfig {

    /**
     * Kafka 이벤트 객체 발행용 ProducerFactory 설정 현재 Outbox 도입 후에는 직접 발행보다 호환성 유지 목적이 크다.
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildProducerProperties();

        ObjectMapper objectMapper = new ObjectMapper();

        // LocalDateTime, Instant 등 Java Time 타입 직렬화를 지원한다.
        objectMapper.registerModule(new JavaTimeModule());

        // 날짜/시간 값을 배열이 아니라 문자열로 변환한다.
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonSerializer<Object> jsonSerializer = new JsonSerializer<>(objectMapper);

        // MSA 환경에서 Java 클래스 경로를 Kafka Header에 넣지 않는다.
        jsonSerializer.setAddTypeInfo(false);

        DefaultKafkaProducerFactory<String, Object> factory =
                new DefaultKafkaProducerFactory<>(
                        props,
                        new StringSerializer(),
                        jsonSerializer
                );

        // JsonSerializer를 코드로 직접 설정했으므로 yml 기반 serializer 재설정을 막는다.
        factory.setConfigureSerializers(false);

        return factory;
    }

    /**
     * 객체 발행용 KafkaTemplate Outbox 도입 후에는 직접 발행에는 사용하지 않는다.
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
            ProducerFactory<String, Object> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * Outbox 발행 전용 ProducerFactory Outbox payload는 이미 JSON 문자열이므로 StringSerializer를 사용한다.
     */
    @Bean
    public ProducerFactory<String, String> stringProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildProducerProperties();

        DefaultKafkaProducerFactory<String, String> factory =
                new DefaultKafkaProducerFactory<>(
                        props,
                        new StringSerializer(),
                        new StringSerializer()
                );

        // StringSerializer를 코드로 직접 설정했으므로 yml 기반 serializer 재설정을 막는다.
        factory.setConfigureSerializers(false);

        return factory;
    }

    /**
     * OutboxPoller에서 사용하는 KafkaTemplate
     */
    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate(
            ProducerFactory<String, String> stringProducerFactory
    ) {
        return new KafkaTemplate<>(stringProducerFactory);
    }
}

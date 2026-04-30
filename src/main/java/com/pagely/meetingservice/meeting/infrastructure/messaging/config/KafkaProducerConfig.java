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
     * Kafka 이벤트 발행용 ProducerFactory 설정
     * <p>
     * 목적: 1. LocalDateTime을 배열이 아닌 문자열로 직렬화한다. 2. Kafka Header에 Java 클래스 경로(__TypeId__)를 넣지 않는다. 3. MSA 환경에서 수신 서비스가
     * ObjectMapper로 직접 역직렬화할 수 있게 한다.
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildProducerProperties();

        ObjectMapper objectMapper = new ObjectMapper();

        // LocalDateTime, Instant 등 Java Time 타입 직렬화를 지원한다.
        objectMapper.registerModule(new JavaTimeModule());

        // 날짜/시간 값을 [2026, 5, 12, 19, 0] 같은 배열이 아니라 문자열로 변환한다.
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonSerializer<Object> jsonSerializer = new JsonSerializer<>(objectMapper);

        // MSA 환경에서 발행 서버의 Java 클래스 경로를 Kafka Header에 넣지 않는다.
        jsonSerializer.setAddTypeInfo(false);

        DefaultKafkaProducerFactory<String, Object> factory =
                new DefaultKafkaProducerFactory<>(
                        props,
                        new StringSerializer(),
                        jsonSerializer
                );

        // JsonSerializer를 코드로 직접 설정했으므로 yml 기반 추가 설정을 다시 주입하지 않도록 막는다.
        factory.setConfigureSerializers(false);

        return factory;
    }

    /**
     * KafkaEventPublisher에서 주입받아 사용하는 KafkaTemplate
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
            ProducerFactory<String, Object> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}
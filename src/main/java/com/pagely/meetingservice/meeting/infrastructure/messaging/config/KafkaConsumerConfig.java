package com.pagely.meetingservice.meeting.infrastructure.messaging.config;

import java.util.Map;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ObjectMapper kafkaConsumerObjectMapper(){ // Producer와 동일한 ObjectMapper 설정
        ObjectMapper obj = new ObjectMapper();
        obj.registerModule(new JavaTimeModule());
        obj.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return obj;
    }

    @Bean
    public ConsumerFactory<String, String> kafkaStringConsumerFactory(KafkaProperties properties){ // ConsumerFactory 설정
        Map<String, Object> props = properties.buildConsumerProperties();
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory( // 리스너 컨테이너 팩토리 빈
            ConsumerFactory<String, String> kafkaStringConsumerFactory
    ){
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        return factory;
    }
    
}

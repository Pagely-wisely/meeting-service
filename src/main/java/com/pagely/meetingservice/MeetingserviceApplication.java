package com.pagely.meetingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // 스케줄러 기능 활성화
@EnableFeignClients
@SpringBootApplication
public class MeetingserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetingserviceApplication.class, args);
    }
}
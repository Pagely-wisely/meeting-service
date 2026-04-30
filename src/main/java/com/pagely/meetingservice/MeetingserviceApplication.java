package com.pagely.meetingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MeetingserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetingserviceApplication.class, args);
    }
}
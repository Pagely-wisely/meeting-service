package com.pagely.meetingservice.meeting.infrastructure.client.book;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// Book Service Feign 호출 설정
@Configuration
@RequiredArgsConstructor
public class BookFeignConfig {

    @Bean
    public RequestInterceptor authorizationHeaderInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                System.out.println("Feign 인증 헤더 전달 실패: RequestContext 없음");
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            String authorization = request.getHeader("Authorization");
            String userId = request.getHeader("X-User-Id");
            String userRole = request.getHeader("X-User-Role");

            System.out.println("Feign Authorization = " + authorization);
            System.out.println("Feign X-User-Id = " + userId);
            System.out.println("Feign X-User-Role = " + userRole);

            if (authorization != null && !authorization.isBlank()) {
                template.header("Authorization", authorization);
            }

            if (userId != null && !userId.isBlank()) {
                template.header("X-User-Id", userId);
            }

            if (userRole != null && !userRole.isBlank()) {
                template.header("X-User-Role", userRole);
            }
        };
    }
}
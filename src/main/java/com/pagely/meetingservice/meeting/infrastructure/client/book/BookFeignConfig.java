package com.pagely.meetingservice.meeting.infrastructure.client.book;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// Book Service Feign 호출 설정
@Slf4j
@Configuration
public class BookFeignConfig {

    @Bean
    public RequestInterceptor authorizationHeaderInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                log.debug("Feign 헤더 전달 생략: RequestContext 없음");
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            String authorization = request.getHeader("Authorization");
            String userId = request.getHeader("X-User-Id");
            String userRole = request.getHeader("X-User-Role");

            log.debug("Feign 헤더 전달: hasAuthorization={}, hasUserId={}, hasUserRole={}",
                    authorization != null && !authorization.isBlank(),
                    userId != null && !userId.isBlank(),
                    userRole != null && !userRole.isBlank());

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
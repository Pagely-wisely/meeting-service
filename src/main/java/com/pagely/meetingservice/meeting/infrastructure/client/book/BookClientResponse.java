package com.pagely.meetingservice.meeting.infrastructure.client.book;

import java.time.LocalDateTime;

// Book Service 내부 API의 data 응답 DTO
public record BookClientResponse(
        String id,
        String title,
        String author,
        String publisher,
        String thumbnailUrl,
        String description,
        LocalDateTime publishedAt,
        Long categoryId,
        String categoryName
) {
}
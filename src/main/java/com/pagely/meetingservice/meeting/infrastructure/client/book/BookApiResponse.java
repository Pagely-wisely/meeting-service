package com.pagely.meetingservice.meeting.infrastructure.client.book;

// Book Service의 ApiResponse 응답을 받기 위한 Meeting Service 전용 DTO
public record BookApiResponse(
        boolean success,
        BookClientResponse data,
        Object error
) {
}
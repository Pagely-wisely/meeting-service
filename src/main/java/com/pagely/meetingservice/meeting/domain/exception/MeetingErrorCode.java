package com.pagely.meetingservice.meeting.domain.exception;


import org.springframework.http.HttpStatus;

import com.pagely.common.exception.ErrorCode;

import lombok.Getter;

@Getter
public enum MeetingErrorCode implements ErrorCode {

    /*
     * =========================================================
     * 404 NOT_FOUND
     * =========================================================
     */
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING_NOT_FOUND", "모임을 찾을 수 없습니다."), // 모임 조회 실패
    /*
     * =========================================================
     * 403 FORBIDDEN
     * =========================================================
     */
    MEETING_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MEETING_ACCESS_DENIED", "모임 접근 권한이 없습니다."), // 모임 접근 권한 없음

    

    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    MeetingErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
    
}

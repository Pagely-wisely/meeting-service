package com.pagely.meetingservice.meeting.domain.exception;

import com.pagely.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// 모임 관련 에러 코드
@Getter
@RequiredArgsConstructor
public enum MeetingErrorCode implements ErrorCode {

    /*
     * =========================================================
     * 400 BAD_REQUEST
     * =========================================================
     */
    INVALID_RECRUIT_MAX(HttpStatus.BAD_REQUEST, "INVALID_RECRUIT_MAX", "모집 인원은 1명 이상이어야 합니다."), // 모집 인원 유효성 실패
    INVALID_RECRUIT_PERIOD(HttpStatus.BAD_REQUEST, "INVALID_RECRUIT_PERIOD",
            "모집 시작 시각은 종료 시각보다 늦을 수 없습니다."), // 모집 기간 유효성 실패
    INVALID_MEETING_STATUS(HttpStatus.BAD_REQUEST, "INVALID_MEETING_STATUS", "현재 모임 상태에서는 변경할 수 없습니다."), // 모임 상태 변경 불가

    /*
     * =========================================================
     * 403 FORBIDDEN
     * =========================================================
     */
    MEETING_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MEETING_ACCESS_DENIED", "모임 접근 권한이 없습니다."), // 모임 접근 권한 없음
    ONLY_HOST_ALLOWED(HttpStatus.FORBIDDEN, "ONLY_HOST_ALLOWED", "모임장만 수행할 수 있습니다."), // 모임장 권한 필요

    /*
     * =========================================================
     * 404 NOT_FOUND
     * =========================================================
     */
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING_NOT_FOUND", "모임을 찾을 수 없습니다."); // 모임 조회 실패

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
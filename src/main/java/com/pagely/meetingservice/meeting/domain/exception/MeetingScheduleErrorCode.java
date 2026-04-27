package com.pagely.meetingservice.meeting.domain.exception;

import com.pagely.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// 모임 일정 관련 에러 코드
@Getter
@RequiredArgsConstructor
public enum MeetingScheduleErrorCode implements ErrorCode {

    /*
     * =========================================================
     * 400 BAD_REQUEST
     * =========================================================
     */
    INVALID_SCHEDULE_START_AT(HttpStatus.BAD_REQUEST, "INVALID_SCHEDULE_START_AT", "일정 시작 시각은 필수입니다."), // 일정 시작 시각 누락
    SCHEDULE_NOT_FOR_MEETING(HttpStatus.BAD_REQUEST, "SCHEDULE_NOT_FOR_MEETING", "해당 모임에 속한 일정이 아닙니다."), // 모임과 일정 불일치
    INVALID_SCHEDULE_STATUS_CHANGE(HttpStatus.BAD_REQUEST, "INVALID_SCHEDULE_STATUS_CHANGE",
            "허용되지 않은 일정 상태 변경입니다."), // 일정 상태 전이 불가

    /*
     * =========================================================
     * 403 FORBIDDEN
     * =========================================================
     */
    ONLY_HOST_CAN_CREATE_SCHEDULE(HttpStatus.FORBIDDEN, "ONLY_HOST_CAN_CREATE_SCHEDULE",
            "모임장만 일정을 생성할 수 있습니다."), // 일정 생성 권한 없음
    ONLY_HOST_CAN_CHANGE_SCHEDULE_STATUS(HttpStatus.FORBIDDEN, "ONLY_HOST_CAN_CHANGE_SCHEDULE_STATUS",
            "모임장만 일정 상태를 변경할 수 있습니다."), // 일정 상태 변경 권한 없음

    /*
     * =========================================================
     * 404 NOT_FOUND
     * =========================================================
     */
    MEETING_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING_SCHEDULE_NOT_FOUND", "모임 일정을 찾을 수 없습니다."); // 일정 조회 실패

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
package com.pagely.meetingservice.meeting.domain.exception;

import com.pagely.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// 출석 및 일정 참석 관련 에러 코드
@Getter
@RequiredArgsConstructor
public enum MeetingAttendanceErrorCode implements ErrorCode {

    /*
     * =========================================================
     * 400 BAD_REQUEST
     * =========================================================
     */
    ONLY_ONGOING_SCHEDULE_CAN_CHANGE_ATTENDANCE(HttpStatus.BAD_REQUEST, "ONLY_ONGOING_SCHEDULE_CAN_CHANGE_ATTENDANCE",
            "진행 중인 일정에서만 출석 상태를 변경할 수 있습니다."), // 진행 중 일정만 출석 변경 가능
    ATTENDANCE_USER_NOT_MEETING_MEMBER(HttpStatus.BAD_REQUEST, "ATTENDANCE_USER_NOT_MEETING_MEMBER",
            "출석 상태 변경 대상 유저가 해당 모임원이 아닙니다."), // 출석 대상 유저가 모임원이 아님
    ATTENDANCE_STATUS_ALREADY_CHANGED(HttpStatus.BAD_REQUEST, "ATTENDANCE_STATUS_ALREADY_CHANGED",
            "이미 변경된 출석 상태는 다시 변경할 수 없습니다."), // 출석 상태 재변경 불가
    INVALID_ATTENDANCE_STATUS(HttpStatus.BAD_REQUEST, "INVALID_ATTENDANCE_STATUS", "허용되지 않은 출석 상태입니다."), // 출석 상태 유효성 실패

    /*
     * =========================================================
     * 403 FORBIDDEN
     * =========================================================
     */
    ONLY_MEMBER_CAN_JOIN_SCHEDULE(HttpStatus.FORBIDDEN, "ONLY_MEMBER_CAN_JOIN_SCHEDULE",
            "모임원만 일정 참석 등록이 가능합니다."), // 일정 참석 권한 없음
    ONLY_HOST_CAN_VIEW_ATTENDANCES(HttpStatus.FORBIDDEN, "ONLY_HOST_CAN_VIEW_ATTENDANCES",
            "모임장만 전체 출석부를 조회할 수 있습니다."), // 전체 출석부 조회 권한 없음
    ONLY_HOST_CAN_CHANGE_ATTENDANCE(HttpStatus.FORBIDDEN, "ONLY_HOST_CAN_CHANGE_ATTENDANCE",
            "모임장만 출석 상태를 변경할 수 있습니다."), // 출석 상태 변경 권한 없음

    /*
     * =========================================================
     * 404 NOT_FOUND
     * =========================================================
     */
    ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "ATTENDANCE_NOT_FOUND", "해당 유저의 참석 등록 내역이 없습니다."), // 참석 등록 내역 없음

    /*
     * =========================================================
     * 409 CONFLICT
     * =========================================================
     */
    ATTENDANCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "ATTENDANCE_ALREADY_EXISTS", "이미 참석 등록한 일정입니다."); // 중복 참석 등록

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
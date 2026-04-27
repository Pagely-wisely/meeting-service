package com.pagely.meetingservice.meeting.domain.exception;

import com.pagely.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// 모임 가입 신청 관련 에러 코드
@Getter
@RequiredArgsConstructor
public enum MeetingJoinErrorCode implements ErrorCode {

    /*
     * =========================================================
     * 400 BAD_REQUEST
     * =========================================================
     */
    NOT_RECRUITING_MEETING(HttpStatus.BAD_REQUEST, "NOT_RECRUITING_MEETING", "현재 모집 중인 모임이 아닙니다."), // 모집 중이 아닌 모임
    INVALID_JOIN_STATUS(HttpStatus.BAD_REQUEST, "INVALID_JOIN_STATUS", "승인 대기 상태의 신청만 처리할 수 있습니다."), // 가입 신청 상태 유효성 실패
    JOIN_NOT_FOR_MEETING(HttpStatus.BAD_REQUEST, "JOIN_NOT_FOR_MEETING", "해당 모임의 가입 신청이 아닙니다."), // 모임과 가입 신청 불일치
    REJECTED_USER_CANNOT_REAPPLY(HttpStatus.BAD_REQUEST, "REJECTED_USER_CANNOT_REAPPLY",
            "가입 거절된 사용자는 다시 신청할 수 없습니다."), // 거절 유저 재신청 불가

    /*
     * =========================================================
     * 403 FORBIDDEN
     * =========================================================
     */
    ONLY_HOST_CAN_VIEW_JOIN_LIST(HttpStatus.FORBIDDEN, "ONLY_HOST_CAN_VIEW_JOIN_LIST",
            "모임장만 가입 신청 목록을 조회할 수 있습니다."), // 가입 신청 목록 조회 권한 없음
    ONLY_HOST_CAN_APPROVE_JOIN(HttpStatus.FORBIDDEN, "ONLY_HOST_CAN_APPROVE_JOIN",
            "모임장만 가입 승인할 수 있습니다."), // 가입 승인 권한 없음

    /*
     * =========================================================
     * 404 NOT_FOUND
     * =========================================================
     */
    MEETING_JOIN_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING_JOIN_NOT_FOUND", "가입 신청을 찾을 수 없습니다."), // 가입 신청 조회 실패

    /*
     * =========================================================
     * 409 CONFLICT
     * =========================================================
     */
    MEETING_JOIN_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEETING_JOIN_ALREADY_EXISTS", "이미 가입 신청한 모임입니다."), // 중복 가입 신청
    ALREADY_MEETING_MEMBER(HttpStatus.CONFLICT, "ALREADY_MEETING_MEMBER", "이미 모임에 참여 중인 사용자입니다."), // 이미 모임원
    MEETING_RECRUIT_FULL(HttpStatus.CONFLICT, "MEETING_RECRUIT_FULL", "모임 정원이 마감되었습니다."); // 모집 정원 초과

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
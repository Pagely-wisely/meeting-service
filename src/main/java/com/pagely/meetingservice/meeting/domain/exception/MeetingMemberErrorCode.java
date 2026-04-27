package com.pagely.meetingservice.meeting.domain.exception;

import com.pagely.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// 모임원 관련 에러 코드
@Getter
@RequiredArgsConstructor
public enum MeetingMemberErrorCode implements ErrorCode {

    /*
     * =========================================================
     * 400 BAD_REQUEST
     * =========================================================
     */
    INACTIVE_MEMBER(HttpStatus.BAD_REQUEST, "INACTIVE_MEMBER", "활성 상태의 모임원만 수행할 수 있습니다."), // 비활성 모임원
    REMOVED_OR_EXPELLED_MEMBER(HttpStatus.BAD_REQUEST, "REMOVED_OR_EXPELLED_MEMBER",
            "강퇴 또는 제거된 사용자는 가입 신청할 수 없습니다."), // 제거/강퇴 유저 제한

    /*
     * =========================================================
     * 403 FORBIDDEN
     * =========================================================
     */
    ONLY_MEETING_MEMBER_ALLOWED(HttpStatus.FORBIDDEN, "ONLY_MEETING_MEMBER_ALLOWED", "모임원만 수행할 수 있습니다."), // 모임원 권한 필요

    /*
     * =========================================================
     * 404 NOT_FOUND
     * =========================================================
     */
    MEETING_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING_MEMBER_NOT_FOUND", "모임원을 찾을 수 없습니다."); // 모임원 조회 실패

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
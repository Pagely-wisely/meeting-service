package com.pagely.meetingservice.meeting.domain.model;

// 가입 신청 상태
public enum MeetingJoinStatus {
    PENDING,    // 승인 대기
    APPROVED,   // 승인
    REJECTED,   // 거절
    CANCELLED,  // 신청 취소
    EXPIRED     // 만료
}
package com.pagely.meetingservice.meeting.domain.model;

// 모임 진행 상태
public enum MeetingStatus {
    UPCOMING,       // 시작 전
    IN_PROGRESS,    // 진행 중
    COMPLETED,      // 종료
    CANCELLED       // 취소
}
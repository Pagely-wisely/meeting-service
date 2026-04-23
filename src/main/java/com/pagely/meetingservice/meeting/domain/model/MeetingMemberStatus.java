package com.pagely.meetingservice.meeting.domain.model;

// 모임 멤버 상태
public enum MeetingMemberStatus {
    ACTIVE,     // 활동중
    LEFT,       // 모임원이 탈퇴
    REMOVED,    // 모임장이 퇴출
    EXPELLED    // 자동 제명
}
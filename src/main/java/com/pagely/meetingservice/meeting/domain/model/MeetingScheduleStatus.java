package com.pagely.meetingservice.meeting.domain.model;

// 일정 상태
public enum MeetingScheduleStatus {
    SCHEDULED,  // 진행 예정
    ONGOING,    // 진행중
    FINISHED,   // 종료
    CANCELLED   // 취소
}
package com.pagely.meetingservice.meeting.domain.model;

// 출석 상태
public enum AttendanceStatus {
    PENDING,    // 출석 대기
    ATTENDED,   // 출석 완료
    LATE,       // 지각
    ABSENT,     // 결석
    EXCUSED     // 사유 인정 결석
}
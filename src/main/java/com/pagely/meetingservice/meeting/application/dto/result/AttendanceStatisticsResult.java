package com.pagely.meetingservice.meeting.application.dto.result;

import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;
import java.util.List;

public record AttendanceStatisticsResult(
        long attendedCount,
        long lateCount,
        long absentCount,
        long excusedCount,
        long convertedAbsentCount,
        long remainingLateCount,
        long totalAbsentCount
) {
    public static AttendanceStatisticsResult from(List<MeetingAttendance> attendances) {
        long attendedCount = attendances.stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.ATTENDED)
                .count();

        long lateCount = attendances.stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.LATE)
                .count();

        long absentCount = attendances.stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.ABSENT)
                .count();

        long excusedCount = attendances.stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.EXCUSED)
                .count();

        // 지각 3회는 결석 1회로 환산한다.
        // 단, 실제 출석 상태를 LATE에서 ABSENT로 변경하지 않는다.
        // 이 값은 통계 계산에서만 사용하는 환산 결석 수이다.
        long convertedAbsentCount = lateCount / 3;

        // 결석으로 환산되고 남은 지각 횟수
        // 예: 지각 4회 -> 환산 결석 1회, 남은 지각 1회
        long remainingLateCount = lateCount % 3;

        // 실제 결석 수 + 지각 환산 결석 수
        long totalAbsentCount = absentCount + convertedAbsentCount;

        return new AttendanceStatisticsResult(
                attendedCount,
                lateCount,
                absentCount,
                excusedCount,
                convertedAbsentCount,
                remainingLateCount,
                totalAbsentCount
        );
    }
}
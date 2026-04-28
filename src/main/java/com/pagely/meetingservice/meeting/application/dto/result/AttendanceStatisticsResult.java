package com.pagely.meetingservice.meeting.application.dto.result;

import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import java.util.List;

// 출석 통계 결과 DTO
public record AttendanceStatisticsResult(
        long attendedCount,          // 정상 출석 수
        long lateCount,              // 실제 지각 수
        long absentCount,            // 실제 결석 수
        long excusedCount,           // 사유 인정 결석 수
        long convertedAbsentCount,   // 지각 3회 환산 결석 수
        long remainingLateCount,     // 결석으로 환산되고 남은 지각 수
        long totalAbsentCount        // 실제 결석 수 + 지각 환산 결석 수
) {

    // 출석 결과 목록을 기준으로 통계 계산
    public static AttendanceStatisticsResult from(List<MeetingAttendanceResult> attendances) {
        long attendedCount = countByStatus(attendances, AttendanceStatus.ATTENDED);
        long lateCount = countByStatus(attendances, AttendanceStatus.LATE);
        long absentCount = countByStatus(attendances, AttendanceStatus.ABSENT);
        long excusedCount = countByStatus(attendances, AttendanceStatus.EXCUSED);

        // 지각 3회는 결석 1회로 환산한다.
        // 단, 실제 출석 상태를 LATE에서 ABSENT로 변경하지 않는다.
        long convertedAbsentCount = lateCount / 3;

        // 결석으로 환산되고 남은 지각 수
        // 예: 지각 4회 = 환산 결석 1회 + 남은 지각 1회
        long remainingLateCount = lateCount % 3;

        // 최종 결석 수는 실제 결석 수와 지각 환산 결석 수를 합산한다.
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

    private static long countByStatus(
            List<MeetingAttendanceResult> attendances,
            AttendanceStatus status
    ) {
        return attendances.stream()
                .filter(attendance -> attendance.status() == status)
                .count();
    }
}
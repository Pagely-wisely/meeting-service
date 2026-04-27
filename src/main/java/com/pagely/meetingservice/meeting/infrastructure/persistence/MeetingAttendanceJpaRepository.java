package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// 모임 일정 참석 JPA Repository
public interface MeetingAttendanceJpaRepository extends JpaRepository<MeetingAttendance, UUID> {

    // 일정 + 유저 기준 참석 조회
    Optional<MeetingAttendance> findByScheduleIdAndUserIdAndDeletedAtIsNull(UUID scheduleId, UUID userId);

    // 특정 일정의 참석 목록 조회
    List<MeetingAttendance> findAllByScheduleIdAndDeletedAtIsNull(UUID scheduleId);

    // 특정 일정의 상태별 참석 목록 조회
    List<MeetingAttendance> findAllByScheduleIdAndStatusAndDeletedAtIsNull(
            UUID scheduleId,
            AttendanceStatus status
    );

    // 특정 모임에서 특정 유저의 참석 목록 조회
    List<MeetingAttendance> findAllByMeetingIdAndUserIdAndDeletedAtIsNull(UUID meetingId, UUID userId);

    // 특정 일정에 이미 참석 등록했는지 확인
    boolean existsByScheduleIdAndUserIdAndDeletedAtIsNull(UUID scheduleId, UUID userId);
}
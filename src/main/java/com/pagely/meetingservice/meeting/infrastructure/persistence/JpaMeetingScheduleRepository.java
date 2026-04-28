package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// 모임 일정 JPA Repository
public interface JpaMeetingScheduleRepository extends JpaRepository<MeetingSchedule, UUID> {

    // 특정 모임의 일정 목록 조회
    List<MeetingSchedule> findByMeetingId(UUID meetingId);

    // 특정 모임의 삭제되지 않은 일정 목록 조회
    List<MeetingSchedule> findByMeetingIdAndDeletedAtIsNullOrderByScheduleNumberAsc(UUID meetingId);

    // 특정 모임의 삭제되지 않은 일정 상세 조회
    Optional<MeetingSchedule> findByIdAndMeetingIdAndDeletedAtIsNull(UUID scheduleId, UUID meetingId);

    // 특정 모임의 상태별 일정 목록 조회
    List<MeetingSchedule> findByMeetingIdAndStatus(UUID meetingId, MeetingScheduleStatus status);

    // 특정 모임의 회차 번호로 일정 조회
    Optional<MeetingSchedule> findByMeetingIdAndScheduleNumber(UUID meetingId, int scheduleNumber);

    // 특정 모임 내 회차 번호 중복 여부 확인
    boolean existsByMeetingIdAndScheduleNumber(UUID meetingId, int scheduleNumber);
}
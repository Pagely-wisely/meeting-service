package com.pagely.meetingservice.meeting.domain.repository;

import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// 모임 일정 저장/조회 인터페이스
public interface MeetingScheduleRepository {

    // 일정 저장
    MeetingSchedule save(MeetingSchedule meetingSchedule);

    // ID로 일정 조회
    Optional<MeetingSchedule> findById(UUID scheduleId);

    // 특정 모임의 전체 일정 조회
    List<MeetingSchedule> findByMeetingId(UUID meetingId);

    // 특정 모임의 삭제되지 않은 일정 목록 조회
    Page<MeetingSchedule> findByMeetingIdAndDeletedAtIsNull(UUID meetingId, Pageable pageable);

    // 특정 모임의 삭제되지 않은 일정 상세 조회
    Optional<MeetingSchedule> findByIdAndMeetingIdAndDeletedAtIsNull(UUID scheduleId, UUID meetingId);

    // 특정 모임의 상태별 일정 조회
    List<MeetingSchedule> findByMeetingIdAndStatus(UUID meetingId, MeetingScheduleStatus status);

    // 모임 + 회차 번호로 일정 조회
    Optional<MeetingSchedule> findByMeetingIdAndScheduleNumber(UUID meetingId, int scheduleNumber);

    // 회차 번호 중복 여부 확인
    boolean existsByMeetingIdAndScheduleNumber(UUID meetingId, int scheduleNumber);

    // 시작 시간이 지난 SCHEDULED 상태의 일정 목록 조회
    List<MeetingSchedule> findStartDueSchedules(LocalDateTime now, Pageable pageable);

    // ID로 일정 조회 - 쓰기 락
    Optional<MeetingSchedule> findByIdForUpdate(UUID scheduleId);
}

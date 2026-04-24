package com.pagely.meetingservice.meeting.domain.repository;

import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 모임 일정 저장/조회 인터페이스
public interface MeetingScheduleRepository {

    // 일정 저장
    MeetingSchedule save(MeetingSchedule meetingSchedule);

    // ID로 일정 조회
    Optional<MeetingSchedule> findById(UUID scheduleId);

    // 특정 모임의 전체 일정 조회
    List<MeetingSchedule> findByMeetingId(UUID meetingId);

    // 특정 모임의 상태별 일정 조회
    List<MeetingSchedule> findByMeetingIdAndStatus(UUID meetingId, MeetingScheduleStatus status);

    // 모임 + 회차 번호로 일정 조회
    Optional<MeetingSchedule> findByMeetingIdAndScheduleNumber(UUID meetingId, int scheduleNumber);

    // 회차 번호 중복 여부 확인
    boolean existsByMeetingIdAndScheduleNumber(UUID meetingId, int scheduleNumber);
}
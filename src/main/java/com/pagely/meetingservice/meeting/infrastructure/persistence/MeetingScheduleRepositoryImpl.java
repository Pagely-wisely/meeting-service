package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleStatus;
import com.pagely.meetingservice.meeting.domain.repository.MeetingScheduleRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// 모임 일정 Repository 구현체
@Repository
@RequiredArgsConstructor
public class MeetingScheduleRepositoryImpl implements MeetingScheduleRepository {

    private final MeetingScheduleJpaRepository meetingScheduleJpaRepository;

    // 일정 저장
    @Override
    public MeetingSchedule save(MeetingSchedule meetingSchedule) {
        return meetingScheduleJpaRepository.save(meetingSchedule);
    }

    // ID로 일정 조회
    @Override
    public Optional<MeetingSchedule> findById(UUID scheduleId) {
        return meetingScheduleJpaRepository.findById(scheduleId);
    }

    // 특정 모임의 전체 일정 조회
    @Override
    public List<MeetingSchedule> findByMeetingId(UUID meetingId) {
        return meetingScheduleJpaRepository.findByMeetingId(meetingId);
    }

    // 특정 모임의 상태별 일정 조회
    @Override
    public List<MeetingSchedule> findByMeetingIdAndStatus(
            UUID meetingId,
            MeetingScheduleStatus status
    ) {
        return meetingScheduleJpaRepository.findByMeetingIdAndStatus(meetingId, status);
    }

    // 모임 + 회차 번호로 일정 조회
    @Override
    public Optional<MeetingSchedule> findByMeetingIdAndScheduleNumber(
            UUID meetingId,
            int scheduleNumber
    ) {
        return meetingScheduleJpaRepository.findByMeetingIdAndScheduleNumber(meetingId, scheduleNumber);
    }

    // 회차 번호 중복 여부 확인
    @Override
    public boolean existsByMeetingIdAndScheduleNumber(UUID meetingId, int scheduleNumber) {
        return meetingScheduleJpaRepository.existsByMeetingIdAndScheduleNumber(meetingId, scheduleNumber);
    }

    // 특정 모임의 삭제되지 않은 일정 목록 조회
    @Override
    public List<MeetingSchedule> findByMeetingIdAndDeletedAtIsNullOrderByScheduleNumberAsc(UUID meetingId) {
        return meetingScheduleJpaRepository.findByMeetingIdAndDeletedAtIsNullOrderByScheduleNumberAsc(meetingId);
    }

    // 특정 모임의 삭제되지 않은 일정 상세 조회
    @Override
    public Optional<MeetingSchedule> findByIdAndMeetingIdAndDeletedAtIsNull(
            UUID scheduleId,
            UUID meetingId
    ) {
        return meetingScheduleJpaRepository.findByIdAndMeetingIdAndDeletedAtIsNull(scheduleId, meetingId);
    }

}
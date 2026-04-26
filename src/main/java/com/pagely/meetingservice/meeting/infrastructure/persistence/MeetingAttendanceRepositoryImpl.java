package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;
import com.pagely.meetingservice.meeting.domain.repository.MeetingAttendanceRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// 모임 일정 참석 Repository 구현체
@Repository
@RequiredArgsConstructor
public class MeetingAttendanceRepositoryImpl implements MeetingAttendanceRepository {

    private final MeetingAttendanceJpaRepository meetingAttendanceJpaRepository;

    // 출석 저장
    @Override
    public MeetingAttendance save(MeetingAttendance meetingAttendance) {
        return meetingAttendanceJpaRepository.save(meetingAttendance);
    }

    // ID로 출석 조회
    @Override
    public Optional<MeetingAttendance> findById(UUID attendanceId) {
        return meetingAttendanceJpaRepository.findById(attendanceId);
    }

    // 일정 + 유저 기준 출석 조회
    @Override
    public Optional<MeetingAttendance> findByScheduleIdAndUserId(UUID scheduleId, UUID userId) {
        return meetingAttendanceJpaRepository.findByScheduleIdAndUserIdAndDeletedAtIsNull(
                scheduleId,
                userId
        );
    }

    // 특정 일정의 출석부 조회
    @Override
    public List<MeetingAttendance> findByScheduleId(UUID scheduleId) {
        return meetingAttendanceJpaRepository.findAllByScheduleIdAndDeletedAtIsNull(scheduleId);
    }

    // 특정 일정의 상태별 출석 조회
    @Override
    public List<MeetingAttendance> findByScheduleIdAndStatus(UUID scheduleId, AttendanceStatus status) {
        return meetingAttendanceJpaRepository.findAllByScheduleIdAndStatusAndDeletedAtIsNull(
                scheduleId,
                status
        );
    }

    // 특정 모임에서 내 출석 목록 조회
    @Override
    public List<MeetingAttendance> findByMeetingIdAndUserId(UUID meetingId, UUID userId) {
        return meetingAttendanceJpaRepository.findAllByMeetingIdAndUserIdAndDeletedAtIsNull(
                meetingId,
                userId
        );
    }

    // 특정 일정에 이미 출석 등록했는지 확인
    @Override
    public boolean existsByScheduleIdAndUserId(UUID scheduleId, UUID userId) {
        return meetingAttendanceJpaRepository.existsByScheduleIdAndUserIdAndDeletedAtIsNull(
                scheduleId,
                userId
        );
    }
}
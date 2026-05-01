package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;
import com.pagely.meetingservice.meeting.domain.repository.MeetingAttendanceRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

// 모임 일정 참석 Repository 구현체
@Repository
@RequiredArgsConstructor
public class MeetingAttendanceRepositoryAdapter implements MeetingAttendanceRepository {

    private final JpaMeetingAttendanceRepository jpaMeetingAttendanceRepository;

    // 출석 저장
    @Override
    public MeetingAttendance save(MeetingAttendance meetingAttendance) {
        return jpaMeetingAttendanceRepository.save(meetingAttendance);
    }

    // ID로 출석 조회
    @Override
    public Optional<MeetingAttendance> findById(UUID attendanceId) {
        return jpaMeetingAttendanceRepository.findById(attendanceId);
    }

    // 일정 + 유저 기준 출석 조회
    @Override
    public Optional<MeetingAttendance> findByScheduleIdAndUserId(UUID scheduleId, UUID userId) {
        return jpaMeetingAttendanceRepository.findByScheduleIdAndUserIdAndDeletedAtIsNull(
                scheduleId,
                userId
        );
    }

    // 특정 일정의 상태별 출석 조회
    @Override
    public List<MeetingAttendance> findByScheduleIdAndStatus(UUID scheduleId, AttendanceStatus status) {
        return jpaMeetingAttendanceRepository.findAllByScheduleIdAndStatusAndDeletedAtIsNull(
                scheduleId,
                status
        );
    }

    // 특정 일정에 이미 출석 등록했는지 확인
    @Override
    public boolean existsByScheduleIdAndUserId(UUID scheduleId, UUID userId) {
        return jpaMeetingAttendanceRepository.existsByScheduleIdAndUserIdAndDeletedAtIsNull(
                scheduleId,
                userId
        );
    }

    // 특정 일정의 출석부 조회 - 페이징 처리
    @Override
    public Page<MeetingAttendance> findByScheduleId(UUID scheduleId, Pageable pageable) {
        return jpaMeetingAttendanceRepository.findAllByScheduleIdAndDeletedAtIsNull(
                scheduleId,
                pageable
        );
    }

    // 특정 일정의 출석부 조회 - 통계 계산용 전체 조회
    @Override
    public List<MeetingAttendance> findByScheduleId(UUID scheduleId) {
        return jpaMeetingAttendanceRepository.findAllByScheduleIdAndDeletedAtIsNull(scheduleId);
    }

    // 특정 모임에서 내 출석 목록 조회 - 페이징 처리
    @Override
    public Page<MeetingAttendance> findByMeetingIdAndUserId(
            UUID meetingId,
            UUID userId,
            Pageable pageable
    ) {
        return jpaMeetingAttendanceRepository.findAllByMeetingIdAndUserIdAndDeletedAtIsNull(
                meetingId,
                userId,
                pageable
        );
    }

    // 특정 모임에서 내 출석 목록 조회 - 통계 계산용 전체 조회
    @Override
    public List<MeetingAttendance> findByMeetingIdAndUserId(UUID meetingId, UUID userId) {
        return jpaMeetingAttendanceRepository.findAllByMeetingIdAndUserIdAndDeletedAtIsNull(
                meetingId,
                userId
        );
    }

    // 특정 유저의 모든 참석 행 조회
    @Override
    public List<MeetingAttendance> findAllByUserId(UUID userId) {
        return jpaMeetingAttendanceRepository.findAllByUserIdAndDeletedAtIsNull(userId);
    }
}
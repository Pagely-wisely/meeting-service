package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;
import com.pagely.meetingservice.meeting.domain.repository.MeetingAttendanceRepository;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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

    private Map<AttendanceStatus, Long> toStatusCountMap(List<Object[]> rows) {
        Map<AttendanceStatus, Long> map = new EnumMap<>(AttendanceStatus.class);
        for (Object[] row : rows) {
            AttendanceStatus status = (AttendanceStatus) row[0];
            Long count = (Long) row[1];
            map.put(status, count);
        }
        return map;
    }

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

    // 지정한 모임 ID에 한정된 특정 유저의 참석 목록 조회
    @Override
    public List<MeetingAttendance> findAllByUserIdAndMeetingIdIn(UUID userId, Collection<UUID> meetingIds) {
        return jpaMeetingAttendanceRepository.findAllByUserIdAndMeetingIdInAndDeletedAtIsNull(userId, meetingIds);
    }

    // 일정 단위 출석 행을 상태 별로 집계
    @Override
    public Map<AttendanceStatus, Long> countByScheduleIdGroupedByStatus(UUID scheduleId) {
        return toStatusCountMap(jpaMeetingAttendanceRepository.countGroupedByStatusForSchedule(scheduleId));
    }

    // 모임 +유저 단위 출석 행을 상태 별로 집계
    @Override
    public Map<AttendanceStatus, Long> countByMeetingIdAndUserIdGroupedByStatus(UUID meetingId, UUID userId) {
        return toStatusCountMap(
                jpaMeetingAttendanceRepository.countGroupedByStatusForMeetingAndUser(meetingId, userId));
    }

}
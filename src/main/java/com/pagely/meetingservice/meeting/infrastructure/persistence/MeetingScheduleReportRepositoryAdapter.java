package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleReport;
import com.pagely.meetingservice.meeting.domain.repository.MeetingScheduleReportRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// 모임 일정별 독후감 작성 기록 Repository Adapter
@Repository
@RequiredArgsConstructor
public class MeetingScheduleReportRepositoryAdapter implements MeetingScheduleReportRepository {

    private final JpaMeetingScheduleReportRepository jpaMeetingScheduleReportRepository;

    // 독후감 작성 기록 저장
    @Override
    public MeetingScheduleReport save(MeetingScheduleReport report) {
        return jpaMeetingScheduleReportRepository.save(report);
    }

    // 특정 일정에서 독후감을 작성한 유저 ID 목록 조회
    @Override
    public List<UUID> findUserIdsByScheduleId(UUID scheduleId) {
        return jpaMeetingScheduleReportRepository.findUserIdsByScheduleId(scheduleId);
    }

    // 특정 일정에서 특정 유저가 독후감을 작성했는지 확인
    @Override
    public boolean existsByScheduleIdAndUserId(UUID scheduleId, UUID userId) {
        return jpaMeetingScheduleReportRepository.existsByScheduleIdAndUserId(scheduleId, userId);
    }
}

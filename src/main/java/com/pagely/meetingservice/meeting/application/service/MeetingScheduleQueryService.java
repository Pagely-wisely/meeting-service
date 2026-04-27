package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingScheduleResult;
import com.pagely.meetingservice.meeting.domain.exception.MeetingErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingScheduleErrorCode;
import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingScheduleRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 모임 일정 조회 전용 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingScheduleQueryService {

    private final MeetingRepository meetingRepository;
    private final MeetingScheduleRepository meetingScheduleRepository;

    // 모임 일정 목록 조회
    public List<MeetingScheduleResult> getSchedules(UUID meetingId) {
        validateMeetingExists(meetingId);

        return meetingScheduleRepository.findByMeetingIdAndDeletedAtIsNullOrderByScheduleNumberAsc(meetingId)
                .stream()
                .map(MeetingScheduleResult::from)
                .toList();
    }

    // 모임 일정 상세 조회
    public MeetingScheduleResult getSchedule(UUID meetingId, UUID scheduleId) {
        validateMeetingExists(meetingId);

        MeetingSchedule schedule = meetingScheduleRepository.findByIdAndMeetingIdAndDeletedAtIsNull(
                        scheduleId,
                        meetingId
                )
                .orElseThrow(() -> new BusinessException(MeetingScheduleErrorCode.MEETING_SCHEDULE_NOT_FOUND));

        return MeetingScheduleResult.from(schedule);
    }

    // 모임 존재 여부 검증
    private void validateMeetingExists(UUID meetingId) {
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));
    }
}
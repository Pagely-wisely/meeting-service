package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingScheduleResult;
import com.pagely.meetingservice.meeting.domain.exception.MeetingErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingScheduleErrorCode;
import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingScheduleRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 모임 일정 조회 전용 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingScheduleQueryService {

    private final MeetingRepository meetingRepository;
    private final MeetingScheduleRepository meetingScheduleRepository;

    // 모임 일정 목록 조회 - 페이징 처리
    public Page<MeetingScheduleResult> getSchedules(UUID meetingId, Pageable pageable) {
        // 요청한 모임이 실제 존재하는지 확인
        validateMeetingExists(meetingId);

        // 삭제되지 않은 일정만 페이징 조회
        // 정렬 기준은 Controller에서 Pageable 생성 시 전달한다.
        return meetingScheduleRepository.findByMeetingIdAndDeletedAtIsNull(meetingId, pageable)
                .map(MeetingScheduleResult::from);
    }

    // 모임 일정 상세 조회
    public MeetingScheduleResult getSchedule(UUID meetingId, UUID scheduleId) {
        // 요청한 모임이 실제 존재하는지 확인
        validateMeetingExists(meetingId);

        // 요청한 일정이 해당 모임에 속해 있고 삭제되지 않은 일정인지 확인
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
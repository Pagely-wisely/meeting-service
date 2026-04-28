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
        // 요청한 모임이 실제 존재하는지 확인
        validateMeetingExists(meetingId);

        // 삭제되지 않은 일정만 회차 번호 오름차순으로 조회
        // 사용자에게는 유효한 일정 목록만 노출
        return meetingScheduleRepository.findByMeetingIdAndDeletedAtIsNullOrderByScheduleNumberAsc(meetingId)
                .stream()
                .map(MeetingScheduleResult::from)
                .toList();
    }

    // 모임 일정 상세 조회
    public MeetingScheduleResult getSchedule(UUID meetingId, UUID scheduleId) {
        // 요청한 모임이 실제 존재하는지 확인
        validateMeetingExists(meetingId);

        // 요청한 일정이 해당 모임에 속해 있고 삭제되지 않은 일정인지 확인
        // 다른 모임의 일정 조회 및 삭제된 일정 조회를 방지
        MeetingSchedule schedule = meetingScheduleRepository.findByIdAndMeetingIdAndDeletedAtIsNull(
                        scheduleId,
                        meetingId
                )
                .orElseThrow(() -> new BusinessException(MeetingScheduleErrorCode.MEETING_SCHEDULE_NOT_FOUND));

        // 조회된 일정 엔티티를 응답용 결과 DTO로 변환
        return MeetingScheduleResult.from(schedule);
    }

    // 모임 존재 여부 검증
    private void validateMeetingExists(UUID meetingId) {
        // 일정 조회 전에 상위 리소스인 모임이 존재하는지 먼저 검증
        // 존재하지 않는 모임에 대한 일정 조회 요청을 명확한 예외로 처리
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));
    }
}
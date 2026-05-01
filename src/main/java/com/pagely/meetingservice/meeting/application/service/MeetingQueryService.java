package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingResult;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingSummaryResult;
import com.pagely.meetingservice.meeting.domain.exception.MeetingErrorCode;
import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

// 조회 전용 서비스
@Service
@RequiredArgsConstructor
public class MeetingQueryService {

    private final MeetingRepository meetingRepository;

    // 전체 모임 조회
    public Page<MeetingSummaryResult> getMeetings(Pageable pageable) {
        return meetingRepository.findAll(pageable)
                .map(MeetingSummaryResult::from);
    }

    // 모임 상세 조회
    public MeetingResult getMeeting(UUID meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        return MeetingResult.from(meeting);
    }
}
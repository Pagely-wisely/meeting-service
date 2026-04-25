package com.pagely.meetingservice.meeting.application.service;

import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingScheduleCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingScheduleResult;
import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingScheduleRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeetingScheduleApplicationService {

    private final MeetingRepository meetingRepository;
    private final MeetingScheduleRepository meetingScheduleRepository;

    // 모임 일정 생성
    @Transactional
    public MeetingScheduleResult createSchedule(CreateMeetingScheduleCommand command) {
        Meeting meeting = meetingRepository.findById(command.meetingId())
                .orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다."));

        // TODO: 인증 컨텍스트 연결 후 모임장 권한 검증
        if (!meeting.getHostId().equals(command.createdBy())) {
            throw new IllegalArgumentException("모임장만 일정을 생성할 수 있습니다.");
        }

        int nextScheduleNumber = calculateNextScheduleNumber(command.meetingId());

        MeetingSchedule schedule = MeetingSchedule.create(
                UUID.randomUUID(),
                command.meetingId(),
                nextScheduleNumber,
                command.bookId(),
                command.startAt(),
                command.discussionNote(),
                LocalDateTime.now(),
                command.createdBy()
        );

        MeetingSchedule savedSchedule = meetingScheduleRepository.save(schedule);

        return MeetingScheduleResult.from(savedSchedule);
    }

    // 다음 회차 번호 계산
    private int calculateNextScheduleNumber(UUID meetingId) {
        return meetingScheduleRepository.findByMeetingId(meetingId)
                .stream()
                .mapToInt(MeetingSchedule::getScheduleNumber)
                .max()
                .orElse(0) + 1;
    }
}
package com.pagely.meetingservice.meeting.application.service;

import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingScheduleCommand;
import com.pagely.meetingservice.meeting.application.dto.command.UpdateScheduleStatusCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingScheduleResult;
import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleStatus;
import com.pagely.meetingservice.meeting.domain.repository.MeetingAttendanceRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingScheduleRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeetingScheduleCommandService {

    private final MeetingRepository meetingRepository;
    private final MeetingScheduleRepository meetingScheduleRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final MeetingAttendanceRepository meetingAttendanceRepository;

    // 모임 일정 생성
    @Transactional
    public MeetingScheduleResult createSchedule(CreateMeetingScheduleCommand command) {
        Meeting meeting = meetingRepository.findById(command.meetingId())
                .orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다."));

        // TODO: 인증 컨텍스트 연결 후 모임장 권한 검증
//        if (!meeting.getHostId().equals(...) {
//            throw new IllegalArgumentException("모임장만 일정을 생성할 수 있습니다.");
//        }

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

    // 모임 일정 상태 변경
    @Transactional
    public MeetingScheduleResult changeScheduleStatus(UpdateScheduleStatusCommand command) {
        meetingRepository.findById(command.meetingId())
                .orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다."));

        MeetingSchedule schedule = meetingScheduleRepository.findById(command.scheduleId())
                .orElseThrow(() -> new IllegalArgumentException("모임 일정이 존재하지 않습니다."));

        if (!schedule.getMeetingId().equals(command.meetingId())) {
            throw new IllegalArgumentException("해당 모임에 속한 일정이 아닙니다.");
        }

        MeetingMember updater = meetingMemberRepository.findByMeetingIdAndUserId(
                        command.meetingId(),
                        command.updatedBy()
                )
                .orElseThrow(() -> new IllegalArgumentException("모임원만 일정 상태를 변경할 수 있습니다."));

        if (!updater.isActive() || !updater.isHost()) {
            throw new IllegalArgumentException("모임장만 일정 상태를 변경할 수 있습니다.");
        }

        if (command.status() == MeetingScheduleStatus.FINISHED) {
            List<MeetingAttendance> attendances = meetingAttendanceRepository.findByScheduleId(command.scheduleId());
            schedule.finish(attendances, command.updatedBy());
        } else {
            schedule.changeStatus(command.status(), command.updatedBy());
        }

        return MeetingScheduleResult.from(schedule);
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
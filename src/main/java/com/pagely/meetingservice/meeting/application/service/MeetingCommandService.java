package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingResult;
import com.pagely.meetingservice.meeting.application.port.BookProvider;
import com.pagely.meetingservice.meeting.application.port.EventPublisher;
import com.pagely.meetingservice.meeting.domain.event.MeetingCreatedEvent;
import com.pagely.meetingservice.meeting.domain.event.MeetingScheduleCreatedEvent;
import com.pagely.meetingservice.meeting.domain.exception.MeetingErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingScheduleErrorCode;
import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberRole;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.model.MeetingType;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingScheduleRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 모임 명령 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingCommandService {

    private static final int ONE_TIME_SCHEDULE_NUMBER = 1;

    private final MeetingRepository meetingRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final MeetingScheduleRepository meetingScheduleRepository;
    private final EventPublisher eventPublisher;
    private final BookProvider bookProvider;

    // 모임 생성
    @Transactional
    public MeetingResult createMeeting(CreateMeetingCommand command) {
        UUID meetingId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // 일회성 모임 생성에 필요한 값들을 먼저 검증한다.
        // 일회성 모임은 생성과 동시에 일정이 자동 생성되므로 bookId와 scheduleStartAt이 필수다.
        validateOneTimeMeeting(command);

        // 일회성 모임 한정, 생성 전에 bookId 유효성을 Book Service 내부 API로 검증한다.
        // 정기 모임은 모임 자체에 도서를 필수로 연결하지 않으므로 검증하지 않는다.
        if (command.meetingType() == MeetingType.ONES) {
            bookProvider.validateBook(command.bookId());
        }

        // 모임 엔티티 생성
        // 정기 모임에서 bookId가 비어 있으면 CreateMeetingCommand 내부에서 기본값("-")으로 보정한다.
        Meeting meeting = command.toMeeting(meetingId, now);

        // 모임 저장
        Meeting saved = meetingRepository.save(meeting);

        // 모임 생성자는 자동으로 모임장 멤버로 등록한다.
        MeetingMember hostMember = MeetingMember.create(
                UUID.randomUUID(),
                saved.getId(),
                saved.getHostId(),
                MeetingMemberRole.HOST,
                MeetingMemberStatus.ACTIVE,
                now,
                saved.getHostId()
        );

        // 모임장 멤버 저장
        meetingMemberRepository.save(hostMember);

        // 일회성 모임이면 1회차 일정을 자동 생성한다.
        if (saved.isOneTime()) {
            MeetingSchedule savedSchedule = createOneTimeSchedule(saved, command, now);

            // 자동 생성된 일정 이벤트 발행
            eventPublisher.publish(MeetingScheduleCreatedEvent.of(savedSchedule));
        }

        // 모임 생성 이벤트 발행
        eventPublisher.publish(MeetingCreatedEvent.of(saved));

        return MeetingResult.from(saved);
    }

    // 일회성 모임 생성에 필요한 값을 검증한다.
    private void validateOneTimeMeeting(CreateMeetingCommand command) {
        if (command.meetingType() != MeetingType.ONES) {
            return;
        }

        // 일회성 모임은 자동 생성되는 일정에도 도서가 들어가므로 bookId가 필수다.
        if (command.bookId() == null || command.bookId().isBlank()) {
            throw new BusinessException(MeetingErrorCode.INVALID_BOOK);
        }

        // 일회성 모임은 생성과 동시에 일정이 만들어지므로 일정 시작 시각이 필수다.
        if (command.scheduleStartAt() == null) {
            throw new BusinessException(MeetingScheduleErrorCode.INVALID_SCHEDULE_START_AT);
        }
    }

    // 일회성 모임의 1회차 일정을 생성한다.
    private MeetingSchedule createOneTimeSchedule(
            Meeting meeting,
            CreateMeetingCommand command,
            LocalDateTime now
    ) {
        MeetingSchedule schedule = MeetingSchedule.create(
                UUID.randomUUID(),
                meeting.getId(),
                ONE_TIME_SCHEDULE_NUMBER,
                meeting.getBookId(),
                command.scheduleStartAt(),
                command.discussionNote(),
                now,
                meeting.getHostId()
        );

        return meetingScheduleRepository.save(schedule);
    }
}
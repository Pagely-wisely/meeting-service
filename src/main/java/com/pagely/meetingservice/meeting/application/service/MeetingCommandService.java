package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingResult;
import com.pagely.meetingservice.meeting.application.port.BookProvider;
import com.pagely.meetingservice.meeting.application.port.EventPublisher;
import com.pagely.meetingservice.meeting.application.port.ScheduleJobManager;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    private final ScheduleJobManager scheduleJobManager;

    // 모임 생성
    @Transactional
    public MeetingResult createMeeting(CreateMeetingCommand command) {
        UUID meetingId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        validateOneTimeMeeting(command);

        if (command.meetingType() == MeetingType.ONES) {
            bookProvider.validateBook(command.bookId());
        }

        Meeting meeting = command.toMeeting(meetingId, now);

        Meeting saved = meetingRepository.save(meeting);

        MeetingMember hostMember = MeetingMember.create(
                UUID.randomUUID(),
                saved.getId(),
                saved.getHostId(),
                MeetingMemberRole.HOST,
                MeetingMemberStatus.ACTIVE,
                now,
                saved.getHostId()
        );

        meetingMemberRepository.save(hostMember);

        if (saved.isOneTime()) {
            MeetingSchedule savedSchedule = createOneTimeSchedule(saved, command, now);

            // 트랜잭션 커밋 이후 일회성 모임의 자동 시작 Job을 등록한다.
            registerAfterCommit(() -> scheduleJobManager.scheduleStartJob(
                    savedSchedule.getId(),
                    savedSchedule.getStartAt()
            ));

            eventPublisher.publish(MeetingScheduleCreatedEvent.of(savedSchedule));
        }

        eventPublisher.publish(MeetingCreatedEvent.of(saved));

        return MeetingResult.from(saved);
    }

    // 일회성 모임 생성에 필요한 값을 검증한다.
    private void validateOneTimeMeeting(CreateMeetingCommand command) {
        if (command.meetingType() != MeetingType.ONES) {
            return;
        }

        if (command.bookId() == null || command.bookId().isBlank()) {
            throw new BusinessException(MeetingErrorCode.INVALID_BOOK);
        }

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

    // 트랜잭션 커밋 이후 작업을 실행한다.
    private void registerAfterCommit(Runnable task) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            task.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }
}

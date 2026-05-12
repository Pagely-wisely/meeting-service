package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingScheduleCommand;
import com.pagely.meetingservice.meeting.application.dto.command.UpdateScheduleStatusCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingScheduleResult;
import com.pagely.meetingservice.meeting.application.port.BookProvider;
import com.pagely.meetingservice.meeting.application.port.EventPublisher;
import com.pagely.meetingservice.meeting.application.port.ScheduleJobManager;
import com.pagely.meetingservice.meeting.domain.event.MeetingAttendanceStatusChangedEvent;
import com.pagely.meetingservice.meeting.domain.event.MeetingScheduleCreatedEvent;
import com.pagely.meetingservice.meeting.domain.event.MeetingScheduleStatusChangedEvent;
import com.pagely.meetingservice.meeting.domain.exception.MeetingErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingMemberErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingScheduleErrorCode;
import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleStatus;
import com.pagely.meetingservice.meeting.domain.policy.MeetingSchedulePolicy;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class MeetingScheduleCommandService {

    private final MeetingRepository meetingRepository;
    private final MeetingScheduleRepository meetingScheduleRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final MeetingAttendanceRepository meetingAttendanceRepository;
    private final EventPublisher eventPublisher;
    private final BookProvider bookProvider;
    private final MissingReportPenaltyService missingReportPenaltyService;
    private final ScheduleJobManager scheduleJobManager;

    // 모임 일정 생성
    @Transactional
    public MeetingScheduleResult createSchedule(CreateMeetingScheduleCommand command) {
        Meeting meeting = meetingRepository.findByIdForUpdate(command.meetingId())
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        MeetingSchedulePolicy.validateAdditionalScheduleAllowed(meeting);

        MeetingMember requester = meetingMemberRepository.findByMeetingIdAndUserId(
                        command.meetingId(),
                        command.requesterId()
                )
                .orElseThrow(() -> new BusinessException(MeetingMemberErrorCode.ONLY_MEETING_MEMBER_ALLOWED));

        MeetingSchedulePolicy.validateHostCreateSchedule(requester);

        bookProvider.validateBook(command.bookId());

        int nextScheduleNumber = calculateNextScheduleNumber(command.meetingId());

        MeetingSchedule schedule = MeetingSchedule.create(
                UUID.randomUUID(),
                command.meetingId(),
                nextScheduleNumber,
                command.bookId(),
                command.startAt(),
                null,
                LocalDateTime.now(),
                command.requesterId()
        );

        MeetingSchedule savedSchedule = meetingScheduleRepository.save(schedule);

        // 트랜잭션 커밋 이후 Quartz Job을 등록한다.
        registerAfterCommit(() -> scheduleJobManager.scheduleStartJob(
                savedSchedule.getId(),
                savedSchedule.getStartAt()
        ));

        eventPublisher.publish(MeetingScheduleCreatedEvent.of(savedSchedule));

        return MeetingScheduleResult.from(savedSchedule);
    }

    // 모임 일정 상태 변경
    @Transactional
    public MeetingScheduleResult changeScheduleStatus(UpdateScheduleStatusCommand command) {
        Meeting meeting = meetingRepository.findByIdForUpdate(command.meetingId())
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        MeetingSchedule schedule = meetingScheduleRepository.findByIdForUpdate(command.scheduleId())
                .orElseThrow(() -> new BusinessException(MeetingScheduleErrorCode.MEETING_SCHEDULE_NOT_FOUND));

        MeetingSchedulePolicy.validateScheduleBelongsToMeeting(schedule, command.meetingId());

        MeetingSchedulePolicy.validateAlreadyOngoing(schedule, command.status());

        MeetingMember updater = meetingMemberRepository.findByMeetingIdAndUserId(
                        command.meetingId(),
                        command.updatedBy()
                )
                .orElseThrow(() -> new BusinessException(MeetingMemberErrorCode.ONLY_MEETING_MEMBER_ALLOWED));

        MeetingSchedulePolicy.validateHostChangesScheduleStatus(updater);

        List<MeetingAttendance> autoAbsentAttendances = List.of();

        if (command.status() == MeetingScheduleStatus.FINISHED) {
            List<MeetingAttendance> attendances =
                    meetingAttendanceRepository.findByScheduleId(command.scheduleId());

            autoAbsentAttendances = schedule.finish(attendances, command.updatedBy());
        } else {
            schedule.changeStatus(command.status(), command.updatedBy());
        }

        if (schedule.getStatus() == MeetingScheduleStatus.ONGOING) {
            missingReportPenaltyService.applyMissingReportWarnings(
                    schedule.getMeetingId(),
                    schedule.getId()
            );

            meeting.start(command.updatedBy());

            // 수동으로 시작된 일정은 예약 Job을 정리한다.
            registerAfterCommit(() -> scheduleJobManager.deleteStartJob(schedule.getId()));
        }

        if (schedule.getStatus() == MeetingScheduleStatus.CANCELLED) {
            // 취소된 일정은 자동 시작되지 않도록 Job을 삭제한다.
            registerAfterCommit(() -> scheduleJobManager.deleteStartJob(schedule.getId()));
        }

        if (schedule.getStatus() == MeetingScheduleStatus.FINISHED) {
            // 시작 전에 종료된 일정의 잔여 자동 시작 Job을 제거한다.
            registerAfterCommit(() -> scheduleJobManager.deleteStartJob(schedule.getId()));
        }

        if (meeting.isOneTime() && schedule.getStatus() == MeetingScheduleStatus.FINISHED) {
            meeting.finish(command.updatedBy());
        }

        for (MeetingAttendance attendance : autoAbsentAttendances) {
            eventPublisher.publish(
                    MeetingAttendanceStatusChangedEvent.of(
                            attendance,
                            command.updatedBy()
                    )
            );
        }

        eventPublisher.publish(MeetingScheduleStatusChangedEvent.of(schedule));

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

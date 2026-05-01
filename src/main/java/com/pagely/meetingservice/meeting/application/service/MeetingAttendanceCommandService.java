package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.command.UpdateAttendanceStatusCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingAttendanceResult;
import com.pagely.meetingservice.meeting.application.port.EventPublisher;
import com.pagely.meetingservice.meeting.domain.event.MeetingAttendanceJoinedEvent;
import com.pagely.meetingservice.meeting.domain.event.MeetingAttendanceStatusChangedEvent;
import com.pagely.meetingservice.meeting.domain.exception.MeetingAttendanceErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingMemberErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingScheduleErrorCode;
import com.pagely.meetingservice.meeting.domain.model.AttendanceStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.repository.MeetingAttendanceRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingScheduleRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 모임 일정 참석 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingAttendanceCommandService {

    private final MeetingRepository meetingRepository;
    private final MeetingScheduleRepository meetingScheduleRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final MeetingAttendanceRepository meetingAttendanceRepository;
    private final EventPublisher eventPublisher;

    // 모임 일정 참석 등록
    @Transactional
    public MeetingAttendanceResult joinSchedule(UUID meetingId, UUID scheduleId, UUID userId) {
        // 요청한 모임이 실제 존재하는지 확인
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        // 요청한 일정이 실제 존재하는지 확인
        MeetingSchedule schedule = meetingScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(MeetingScheduleErrorCode.MEETING_SCHEDULE_NOT_FOUND));

        // 일정이 요청한 모임에 속한 일정인지 검증
        // 다른 모임의 scheduleId로 접근하는 것을 방지
        if (!schedule.getMeetingId().equals(meetingId)) {
            throw new BusinessException(MeetingScheduleErrorCode.SCHEDULE_NOT_FOR_MEETING);
        }

        // 일정 참석은 해당 모임에 가입된 사용자만 가능
        MeetingMember member = meetingMemberRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new BusinessException(MeetingAttendanceErrorCode.ONLY_MEMBER_CAN_JOIN_SCHEDULE));

        // 탈퇴/강퇴/비활성 상태의 모임원은 일정 참석 불가
        if (!member.isActive()) {
            throw new BusinessException(MeetingAttendanceErrorCode.ONLY_ACTIVE_MEMBER_CAN_JOIN_SCHEDULE);
        }

        // 동일 사용자가 같은 일정에 중복 참석 등록하는 것을 방지
        boolean alreadyJoined = meetingAttendanceRepository.existsByScheduleIdAndUserId(scheduleId, userId);
        if (alreadyJoined) {
            throw new BusinessException(MeetingAttendanceErrorCode.ATTENDANCE_ALREADY_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();

        // 참석 정보 생성
        // 최초 참석 상태, 생성자, 생성 시각 등은 도메인 생성 메서드에서 초기화
        MeetingAttendance attendance = MeetingAttendance.create(
                UUID.randomUUID(),
                meetingId,
                scheduleId,
                userId,
                now,
                userId
        );

        // 생성된 참석 정보 저장
        MeetingAttendance saved = meetingAttendanceRepository.save(attendance);

        // 출석 등록 이벤트 메시지 생성
        // 저장된 출석 ID를 기준으로 ATTENDANCE 도메인 이벤트를 구성한다.
        MeetingAttendanceJoinedEvent event = MeetingAttendanceJoinedEvent.of(saved);

        // 출석 등록 이벤트를 Kafka로 발행한다.
        eventPublisher.publish(event);

        // 저장된 출석 결과 DTO 반환
        return MeetingAttendanceResult.from(saved);
    }

    // 출석 상태 변경
    @Transactional
    public MeetingAttendanceResult changeAttendanceStatus(UpdateAttendanceStatusCommand command) {
        // 요청한 모임이 실제 존재하는지 확인
        meetingRepository.findById(command.meetingId())
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        // 요청한 일정이 실제 존재하는지 확인
        MeetingSchedule schedule = meetingScheduleRepository.findById(command.scheduleId())
                .orElseThrow(() -> new BusinessException(MeetingScheduleErrorCode.MEETING_SCHEDULE_NOT_FOUND));

        // 일정이 해당 모임에 속해 있는지 검증
        if (!schedule.getMeetingId().equals(command.meetingId())) {
            throw new BusinessException(MeetingScheduleErrorCode.SCHEDULE_NOT_FOR_MEETING);
        }

        // 출석 상태 변경은 진행 중인 일정에서만 가능
        if (!schedule.isOngoing()) {
            throw new BusinessException(MeetingAttendanceErrorCode.ONLY_ONGOING_SCHEDULE_CAN_CHANGE_ATTENDANCE);
        }

        // 출석 상태를 변경하려는 사용자가 해당 모임의 멤버인지 확인
        MeetingMember updater = meetingMemberRepository.findByMeetingIdAndUserId(
                        command.meetingId(),
                        command.updatedBy()
                )
                .orElseThrow(() -> new BusinessException(MeetingMemberErrorCode.ONLY_MEETING_MEMBER_ALLOWED));

        // 출석 상태 변경은 ACTIVE 상태의 모임장만 가능
        if (!updater.isActive() || !updater.isHost()) {
            throw new BusinessException(MeetingAttendanceErrorCode.ONLY_HOST_CAN_CHANGE_ATTENDANCE);
        }

        // 출석 상태 변경 대상자가 해당 모임의 멤버인지 확인
        MeetingMember targetMember = meetingMemberRepository.findByMeetingIdAndUserId(
                        command.meetingId(),
                        command.userId()
                )
                .orElseThrow(
                        () -> new BusinessException(MeetingAttendanceErrorCode.ATTENDANCE_USER_NOT_MEETING_MEMBER));

        // 탈퇴/강퇴/비활성 상태의 모임원은 출석 상태 변경 대상이 될 수 없음
        if (!targetMember.isActive()) {
            throw new BusinessException(MeetingAttendanceErrorCode.ONLY_ACTIVE_MEMBER_CAN_CHANGE_ATTENDANCE);
        }

        // 변경할 출석 정보 조회
        MeetingAttendance attendance = meetingAttendanceRepository.findByScheduleIdAndUserId(
                        command.scheduleId(),
                        command.userId()
                )
                .orElseThrow(() -> new BusinessException(MeetingAttendanceErrorCode.ATTENDANCE_NOT_FOUND));

        AttendanceStatus finalStatus = command.status();

        // 출석 요청이 들어와도 일정 시작 후 10분이 지난 경우 자동으로 지각 처리
        if (command.status() == AttendanceStatus.ATTENDED
                && LocalDateTime.now().isAfter(schedule.getStartAt().plusMinutes(10))) {
            finalStatus = AttendanceStatus.LATE;
        }

        // 출석 상태만 변경한다.
        // 경고/지각/결석 누적은 이 메서드에서 직접 처리하지 않고,
        // 출석 상태 변경 이벤트를 수신한 Consumer가 담당한다.
        attendance.changeStatus(
                finalStatus,
                command.note(),
                command.updatedBy()
        );

        // 출석 상태 변경 이벤트 생성
        MeetingAttendanceStatusChangedEvent event = MeetingAttendanceStatusChangedEvent.of(
                attendance,
                command.updatedBy()
        );

        // 출석 상태 변경 이벤트 발행
        eventPublisher.publish(event);

        return MeetingAttendanceResult.from(attendance);
    }
}
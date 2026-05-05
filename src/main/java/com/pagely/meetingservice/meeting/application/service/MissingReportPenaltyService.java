package com.pagely.meetingservice.meeting.application.service;

import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
import com.pagely.meetingservice.meeting.domain.repository.MeetingAttendanceRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingScheduleReportRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 일정 시작 시 독후감 미작성자에게 경고를 누적하는 서비스
@Slf4j
@Service
@RequiredArgsConstructor
public class MissingReportPenaltyService {

    // 시스템 자동 처리용 사용자 ID
    private static final UUID SYSTEM_ACTOR_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final MeetingAttendanceRepository meetingAttendanceRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final MeetingScheduleReportRepository meetingScheduleReportRepository;
    private final WarningThresholdService warningThresholdService;

    // 해당 일정에 참석 등록했지만 독후감을 작성하지 않은 멤버에게 경고 1회 부여
    @Transactional
    public void applyMissingReportWarnings(UUID meetingId, UUID scheduleId) {
        // 일정에 참석 신청한 출석부 목록 조회
        List<MeetingAttendance> attendances = meetingAttendanceRepository.findByScheduleId(scheduleId);

        if (attendances.isEmpty()) {
            log.info(
                    "독후감 미작성 경고 처리 대상 없음: meetingId={}, scheduleId={}",
                    meetingId,
                    scheduleId
            );
            return;
        }

        // 해당 일정에 독후감을 작성한 유저 ID 목록 조회
        Set<UUID> reportedUserIds = new HashSet<>(
                meetingScheduleReportRepository.findUserIdsByScheduleId(scheduleId)
        );

        for (MeetingAttendance attendance : attendances) {
            UUID userId = attendance.getUserId();

            // 이미 독후감을 작성한 사용자는 경고 대상에서 제외
            if (reportedUserIds.contains(userId)) {
                continue;
            }

            // 경고 카운트 갱신은 동시성 보호를 위해 쓰기 락으로 조회
            MeetingMember member = meetingMemberRepository.findByMeetingIdAndUserIdForUpdate(
                    meetingId,
                    userId
            ).orElse(null);

            // 모임원 정보가 없으면 스킵
            if (member == null) {
                log.warn(
                        "독후감 미작성 경고 대상 모임원 없음: meetingId={}, scheduleId={}, userId={}",
                        meetingId,
                        scheduleId,
                        userId
                );
                continue;
            }

            // 활성 모임원만 경고 대상
            if (member.getStatus() != MeetingMemberStatus.ACTIVE) {
                continue;
            }

            // 독후감 미작성 경고 1회 누적
            member.applyMissingReportWarning(SYSTEM_ACTOR_ID);
            meetingMemberRepository.save(member);

            // 경고 3회 이상이면 자동 강퇴 처리
            warningThresholdService.handleAttendanceStatusChanged(
                    "missing-report-" + scheduleId + "-" + userId,
                    meetingId,
                    userId
            );

            log.info(
                    "독후감 미작성 경고 누적 완료: meetingId={}, scheduleId={}, userId={}, warningCount={}",
                    meetingId,
                    scheduleId,
                    userId,
                    member.getWarningCount()
            );
        }
    }
}

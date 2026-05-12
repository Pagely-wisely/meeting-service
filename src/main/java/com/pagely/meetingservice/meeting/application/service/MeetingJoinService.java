package com.pagely.meetingservice.meeting.application.service;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.dto.command.JoinMeetingCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingJoinResult;
import com.pagely.meetingservice.meeting.domain.exception.MeetingErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingJoinErrorCode;
import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoin;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoinStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberRole;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
import com.pagely.meetingservice.meeting.domain.model.RecruitStatus;
import com.pagely.meetingservice.meeting.domain.policy.MeetingJoinPolicy;
import com.pagely.meetingservice.meeting.domain.repository.MeetingJoinRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


// 가입 신청 서비스
@Service
@Transactional(readOnly = true)
public class MeetingJoinService {

    private final MeetingJoinRepository meetingJoinRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingMemberRepository meetingMemberRepository;

    private static final UUID RECRUIT_PERIOD = UUID.fromString("00000000-0000-0000-0000-000000000000");

    // 생성자
    public MeetingJoinService(MeetingJoinRepository meetingJoinRepository,
                              MeetingRepository meetingRepository,
                              MeetingMemberRepository meetingMemberRepository) {
        this.meetingJoinRepository = meetingJoinRepository;
        this.meetingRepository = meetingRepository;
        this.meetingMemberRepository = meetingMemberRepository;
    }

    // 모임 가입 신청 목록 조회 - 페이징 처리
    public Page<MeetingJoinResult> getMeetingJoinList(
            UUID meetingId,
            UUID requesterHostId,
            MeetingJoinStatus joinStatus,
            Pageable pageable
    ) {
        // 모임 조회
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        // 모집 기간이 종료된 경우 모집 상태를 CLOSED로 동기화
        syncRecruitClosedPeriod(meeting);

        // 모임장만 가입 신청 목록을 조회할 수 있음
        MeetingJoinPolicy.validateHost(meeting, requesterHostId);

        // 가입 상태 조건이 있으면 상태별 조회, 없으면 전체 조회
        Page<MeetingJoin> joins = joinStatus != null
                ? meetingJoinRepository.findByMeetingIdAndJoinStatus(meetingId, joinStatus, pageable)
                : meetingJoinRepository.findByMeetingId(meetingId, pageable);

        // 엔티티 Page를 결과 DTO Page로 변환
        return joins.map(MeetingJoinResult::from);
    }

    // 가입 신청 처리
    @Transactional
    public MeetingJoinResult createMeetingJoin(JoinMeetingCommand command) {
        Meeting meeting = meetingRepository.findByIdForUpdate(command.meetingId())
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        syncRecruitClosedPeriod(meeting);

        MeetingJoinPolicy.validateMeetingAllowsJoin(meeting);
        MeetingJoinPolicy.validateRecruitOpenForApply(meeting.getRecruitStatus());

        long activeForApply = meetingMemberRepository.countByMeetingIdAndStatus(
                command.meetingId(), MeetingMemberStatus.ACTIVE);

        MeetingJoinPolicy.validateCapacityNotFullForApply(activeForApply, meeting.getRecruitMax());
        MeetingJoin existingJoin = meetingJoinRepository
                .findByMeetingIdAndRecruitUserId(command.meetingId(), command.recruitUserId())
                .orElse(null);

        MeetingJoinPolicy.validateReapplyPolicy(existingJoin);
        MeetingMember existingMember = meetingMemberRepository
                .findByMeetingIdAndUserId(command.meetingId(), command.recruitUserId())
                .orElse(null);

        MeetingJoinPolicy.validateMemberAllowsApply(existingMember); // 강퇴 등 차단

        UUID joinId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        MeetingJoin join = command.toMeetingJoin(joinId, now);
        MeetingJoin saved = meetingJoinRepository.save(join);
        return MeetingJoinResult.from(saved);

    }

    // 가입 승인
    @Transactional
    public MeetingJoinResult approveMeetingJoin(UUID meetingId, UUID joinId, UUID hostId) {

        Meeting meeting = meetingRepository.findByIdForUpdate(meetingId)
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        syncRecruitClosedPeriod(meeting);

        MeetingJoinPolicy.validateMeetingAllowsJoin(meeting);
        MeetingJoinPolicy.validateHostForApprove(meeting, hostId);

        MeetingJoin join = meetingJoinRepository.findById(joinId)
                .orElseThrow(() -> new BusinessException(MeetingJoinErrorCode.MEETING_JOIN_NOT_FOUND));

        MeetingJoinPolicy.validateJoinForMeeting(join, meetingId);
        boolean alreadyMember = meetingMemberRepository.existsByMeetingIdAndUserId(meetingId, join.getRecruitUserId());
        MeetingJoinPolicy.validateNotAlreadyMember(alreadyMember);

        long activeCount = meetingMemberRepository.countByMeetingIdAndStatus(meetingId, MeetingMemberStatus.ACTIVE);

        MeetingJoinPolicy.validateCapacityForApprove(activeCount, meeting.getRecruitMax());

        join.approve(hostId);

        MeetingJoin savedJoin = meetingJoinRepository.save(join);

        LocalDateTime now = LocalDateTime.now();

        MeetingMember member = MeetingMember.create(
                UUID.randomUUID(),
                meetingId,
                join.getRecruitUserId(),
                MeetingMemberRole.MEMBER,
                MeetingMemberStatus.ACTIVE,
                now,
                hostId
        );

        meetingMemberRepository.save(member);

        syncRecruitStatusAfterApprove(meeting, hostId);

        meetingRepository.save(meeting);

        return MeetingJoinResult.from(savedJoin);

    }

    private void syncRecruitStatusAfterApprove(Meeting meeting, UUID updaterId) { // 승인 이후 모집 상태 동기화
        long activeCount = meetingMemberRepository.countByMeetingIdAndStatus(meeting.getId(),
                MeetingMemberStatus.ACTIVE);
        if (activeCount >= meeting.getRecruitMax()) {
            meeting.changeRecruitStatus(RecruitStatus.FULL, updaterId);
            return;
        }
        if (LocalDateTime.now().isAfter(meeting.getRecruitEndAt())) {
            meeting.changeRecruitStatus(RecruitStatus.CLOSED, updaterId);
        }
    }

    private void syncRecruitClosedPeriod(Meeting meeting) { //모집기간 종료 시 CLOSED 동기화
        RecruitStatus before = meeting.getRecruitStatus();
        LocalDateTime now = LocalDateTime.now();
        meeting.applyRecruitClosedIfPeriodEnded(now, RECRUIT_PERIOD);
        if (before != meeting.getRecruitStatus()) {
            meetingRepository.save(meeting);
        }
    }

}
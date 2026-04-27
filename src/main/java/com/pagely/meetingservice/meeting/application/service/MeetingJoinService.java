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
import com.pagely.meetingservice.meeting.domain.model.MeetingStatus;
import com.pagely.meetingservice.meeting.domain.model.RecruitStatus;
import com.pagely.meetingservice.meeting.domain.repository.MeetingJoinRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


// 가입 신청 서비스
@Service
@Transactional(readOnly = true)
public class MeetingJoinService {

    private final MeetingJoinRepository meetingJoinRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingJoinListSortPolicy meetingJoinListSortPolicy;
    private final MeetingMemberRepository meetingMemberRepository;

    // 생성자
    public MeetingJoinService(MeetingJoinRepository meetingJoinRepository,
                              MeetingRepository meetingRepository,
                              MeetingJoinListSortPolicy meetingJoinListSortPolicy,
                              MeetingMemberRepository meetingMemberRepository) {
        this.meetingJoinRepository = meetingJoinRepository;
        this.meetingRepository = meetingRepository;
        this.meetingJoinListSortPolicy = meetingJoinListSortPolicy;
        this.meetingMemberRepository = meetingMemberRepository;
    }

    // 모임 가입 신청 목록 조회
    public List<MeetingJoinResult> getMeetingJoinList(UUID meetingId, UUID requesterHostId,
                                                      MeetingJoinStatus joinStatus) {
        Meeting meeting = meetingRepository.findById(meetingId) // 모임 조회    
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        if (!meeting.getHostId().equals(requesterHostId)) { // 모임장 검증
            throw new BusinessException(MeetingJoinErrorCode.ONLY_HOST_CAN_VIEW_JOIN_LIST);

        }
        List<MeetingJoin> joins;

        if (joinStatus != null) {
            joins = meetingJoinRepository.findByMeetingIdAndJoinStatus(meetingId, joinStatus);
        } else {
            joins = meetingJoinRepository.findByMeetingId(meetingId);
        }

        meetingJoinListSortPolicy.sortForHostJoinList(joins);

        return joins.stream().map(MeetingJoinResult::from).toList();


    }

    // 가입 신청 처리
    @Transactional
    public MeetingJoinResult createMeetingJoin(JoinMeetingCommand command) {
        Meeting meeting = meetingRepository.findById(command.meetingId())
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        validateMeetingChangeableStatus(meeting);
        validateRecruitStatusForApply(meeting.getRecruitStatus());
        validateJoinReapplyPolicy(command.meetingId(), command.recruitUserId());
        validateBlockedMemberStatus(command.meetingId(), command.recruitUserId());

        UUID joinId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        MeetingJoin join = command.toMeetingJoin(joinId, now);
        MeetingJoin saved = meetingJoinRepository.save(join);
        return MeetingJoinResult.from(saved);

    }

    // 가입 승인
    @Transactional
    public MeetingJoinResult approveMeetingJoin(UUID meetingId, UUID joinId, UUID hostId) {

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(MeetingErrorCode.MEETING_NOT_FOUND));

        validateMeetingChangeableStatus(meeting);
        if (!meeting.getHostId().equals(hostId)) { // 모임장 검증
            throw new BusinessException(MeetingJoinErrorCode.ONLY_HOST_CAN_APPROVE_JOIN);
        }

        MeetingJoin join = meetingJoinRepository.findById(joinId)
                .orElseThrow(() -> new BusinessException(MeetingJoinErrorCode.MEETING_JOIN_NOT_FOUND));

        if (!join.getMeetingId().equals(meetingId)) {
            throw new BusinessException(MeetingJoinErrorCode.JOIN_NOT_FOR_MEETING);
        }

        if (meetingMemberRepository.existsByMeetingIdAndUserId(meetingId,
                join.getRecruitUserId())) { // 이미 모임에 참여중인 사용자 검증
            throw new BusinessException(MeetingJoinErrorCode.ALREADY_MEETING_MEMBER);
        }

        long activeCount = meetingMemberRepository.countByMeetingIdAndStatus(meetingId, MeetingMemberStatus.ACTIVE);

        if (activeCount >= meeting.getRecruitMax()) { // 모임 정원 검증
            throw new BusinessException(MeetingJoinErrorCode.MEETING_RECRUIT_FULL);
        }

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

    private void validateMeetingChangeableStatus(Meeting meeting) { // 모임 변경 가능 상태 검증
        if (meeting.getMeetingStatus() != MeetingStatus.UPCOMING
                && meeting.getMeetingStatus() != MeetingStatus.IN_PROGRESS) {
            throw new BusinessException(MeetingErrorCode.INVALID_MEETING_STATUS);
        }
    }

    private void validateRecruitStatusForApply(RecruitStatus recruitStatus) { // 가입 신청 가능 모집 상태 검증
        if (recruitStatus == RecruitStatus.CLOSED) {
            throw new BusinessException(MeetingJoinErrorCode.NOT_RECRUITING_MEETING);
        }
        if (recruitStatus == RecruitStatus.FULL) {
            throw new BusinessException(MeetingJoinErrorCode.MEETING_RECRUIT_FULL);
        }
        if (recruitStatus != RecruitStatus.RECRUITING) {
            throw new BusinessException(MeetingJoinErrorCode.NOT_RECRUITING_MEETING);
        }
    }

    private void validateJoinReapplyPolicy(UUID meetingId, UUID recruitUserId) { // 기존 신청 이력을 기준으로 재신청 검증
        MeetingJoin existingJoin = meetingJoinRepository.findByMeetingIdAndRecruitUserId(meetingId, recruitUserId)
                .orElse(null);
        if (existingJoin == null) {
            return;
        }
        if (existingJoin.getJoinStatus() == MeetingJoinStatus.REJECTED) {
            throw new BusinessException(MeetingJoinErrorCode.REJECTED_USER_CANNOT_REAPPLY);
        }
        if (existingJoin.getJoinStatus() == MeetingJoinStatus.PENDING
                || existingJoin.getJoinStatus() == MeetingJoinStatus.APPROVED) {
            throw new BusinessException(MeetingJoinErrorCode.MEETING_JOIN_ALREADY_EXISTS);
        }
    }

    private void validateBlockedMemberStatus(UUID meetingId, UUID recruitUserId) { // 멤버 상태 기반 신청 차단
        MeetingMember existingMember = meetingMemberRepository.findByMeetingIdAndUserId(meetingId, recruitUserId)
                .orElse(null);
        if (existingMember == null) {
            return;
        }
        if (existingMember.getStatus() == MeetingMemberStatus.REMOVED
                || existingMember.getStatus() == MeetingMemberStatus.EXPELLED) {
            throw new BusinessException(MeetingJoinErrorCode.REJECTED_USER_CANNOT_REAPPLY);
        }
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
}
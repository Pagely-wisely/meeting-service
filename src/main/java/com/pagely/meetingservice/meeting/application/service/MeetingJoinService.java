package com.pagely.meetingservice.meeting.application.service;

import com.pagely.meetingservice.meeting.application.dto.command.JoinMeetingCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingJoinResult;
import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoin;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoinStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberRole;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
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
                .orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다."));

        if (!meeting.getHostId().equals(requesterHostId)) { // 모임장 검증
            throw new IllegalArgumentException("모임장만 가입 신청 목록을 조회할 수 있습니다.");

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
        Meeting meeting = meetingRepository.findById(command.getMeetingId())
                .orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다"));

        // 모집 상태가 RECRUITING인지 검사
        if (meeting.getRecruitStatus() != RecruitStatus.RECRUITING) {
            throw new IllegalStateException("현재 모집 중인 모임이 아닙니다.");
        }

        // 중복 신청 여부 검사
        if (meetingJoinRepository.existsByMeetingIdAndRecruitUserId(command.getMeetingId(),
                command.getRecruitUserId())) {
            throw new IllegalStateException("이미 가입 신청한 모임입니다.");
        }

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
                .orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다"));

        if (!meeting.getHostId().equals(hostId)) { // 모임장 검증
            throw new IllegalStateException("모임장만 가입 승인할 수 있습니다.");
        }

        MeetingJoin join = meetingJoinRepository.findById(joinId)
                .orElseThrow(() -> new IllegalArgumentException("가입 신청이 존재하지 않습니다."));

        if (!join.getMeetingId().equals(meetingId)) {
            throw new IllegalArgumentException("해당 모임의 가입 신청이 아닙니다.");
        }

        if (meetingMemberRepository.existsByMeetingIdAndUserId(meetingId,
                join.getRecruitUserId())) { // 이미 모임에 참여중인 사용자 검증
            throw new IllegalStateException("이미 모임에 참여중인 사용자입니다.");
        }

        long activeCount = meetingMemberRepository.countByMeetingIdAndStatus(meetingId, MeetingMemberStatus.ACTIVE);

        if (activeCount >= meeting.getRecruitMax()) { // 모임 정원 검증
            throw new IllegalStateException("모임 정원이 마감되었습니다.");
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

        return MeetingJoinResult.from(savedJoin);

    }
}
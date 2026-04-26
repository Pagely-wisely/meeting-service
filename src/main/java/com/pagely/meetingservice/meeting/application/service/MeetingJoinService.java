package com.pagely.meetingservice.meeting.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pagely.meetingservice.meeting.application.dto.command.JoinMeetingCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingJoinResult;
import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoin;
import com.pagely.meetingservice.meeting.domain.model.RecruitStatus;
import com.pagely.meetingservice.meeting.domain.repository.MeetingJoinRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;


// 가입 신청 서비스
@Service
@Transactional(readOnly = true)
public class MeetingJoinService {

    private final MeetingJoinRepository meetingJoinRepository;
    private final MeetingRepository meetingRepository;

    // 생성자
    public MeetingJoinService(MeetingJoinRepository meetingJoinRepository, MeetingRepository meetingRepository) {
        this.meetingJoinRepository = meetingJoinRepository;
        this.meetingRepository = meetingRepository;
    }
    
    // 가입 신청 처리
    @Transactional
    public MeetingJoinResult createMeetingJoin(JoinMeetingCommand command){
        Meeting meeting = meetingRepository.findById(command.getMeetingId())
        .orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다"));
    

    // 모집 상태가 RECRUITING인지 검사
    if(meeting.getRecruitStatus() != RecruitStatus.RECRUITING){ 
        throw new IllegalStateException("현재 모집 중인 모임이 아닙니다.");
    }

    // 중복 신청 여부 검사
    if(meetingJoinRepository.existsByMeetingIdAndRecruitUserId(command.getMeetingId(), command.getRecruitUserId())){
        throw new IllegalStateException("이미 가입 신청한 모임입니다.");
    }

    UUID joinId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();
    MeetingJoin join = command.toMeetingJoin(joinId, now);
    MeetingJoin saved = meetingJoinRepository.save(join);
    return MeetingJoinResult.from(saved);
}
}

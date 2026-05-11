package com.pagely.meetingservice.meeting.domain.policy;

import java.util.UUID;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.domain.exception.MeetingErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingJoinErrorCode;
import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoin;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoinStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
import com.pagely.meetingservice.meeting.domain.model.MeetingStatus;
import com.pagely.meetingservice.meeting.domain.model.RecruitStatus;

public final class MeetingJoinPolicy {
    
    private MeetingJoinPolicy(){}

    // 모임 진행 상태 변경 가능 검증
    public static void validateMeetingAllowsJoin(Meeting meeting){
        if(meeting.getMeetingStatus()!=MeetingStatus.UPCOMING &&
    meeting.getMeetingStatus() != MeetingStatus.IN_PROGRESS){
        throw new BusinessException(MeetingErrorCode.INVALID_MEETING_STATUS);
    }
    }

    // 모집 상태 RECRUITING 인지 검증
    public static void validateRecruitOpenForApply(RecruitStatus recruitStatus){
        if(recruitStatus==RecruitStatus.CLOSED){
            throw new BusinessException(MeetingJoinErrorCode.NOT_RECRUITING_MEETING);
        }
        if(recruitStatus==RecruitStatus.FULL){
            throw new BusinessException(MeetingJoinErrorCode.MEETING_RECRUIT_FULL);
        }
        if(recruitStatus!=RecruitStatus.RECRUITING){
            throw new BusinessException(MeetingJoinErrorCode.NOT_RECRUITING_MEETING);
        }
    }

    // 정원 여유가 있는지 검사(신청 시점 기준 ACTIVE 상태 멤버 수)
    public static void validateCapacityNotFullForApply(long activeCount, int recruitMax){
        if(activeCount >= recruitMax){
            throw new BusinessException(MeetingJoinErrorCode.MEETING_RECRUIT_FULL);
        }
    }

    // 목록 조회 요청자가 모임장인지 체크
    public static void validateHost(Meeting meeting, UUID requestId){
        if(!meeting.getHostId().equals(requestId)){
            throw new BusinessException(MeetingJoinErrorCode.ONLY_HOST_CAN_VIEW_JOIN_LIST);
        }
    }

    // 승인 요청자가 모임장인지 검사
    public static void validateHostForApprove(Meeting meeting, UUID hostId){
        if(!meeting.getHostId().equals(hostId)){
            throw new BusinessException(MeetingJoinErrorCode.ONLY_HOST_CAN_APPROVE_JOIN);
        }
    }

    // 기존 신청 이력을 기준으로 재신청 가능 여부 검사
    public static void validateReapplyPolicy(MeetingJoin join){
        if(join == null){
            return;
        }
        if(join.getJoinStatus()==MeetingJoinStatus.REJECTED){
            throw new BusinessException(MeetingJoinErrorCode.REJECTED_USER_CANNOT_REAPPLY);
        }
        if(join.getJoinStatus()==MeetingJoinStatus.PENDING || join.getJoinStatus()==MeetingJoinStatus.APPROVED){
            throw new BusinessException(MeetingJoinErrorCode.MEETING_JOIN_ALREADY_EXISTS);
        }
    }

    // REMOVED, EXPELLED 상태 멤버 신청 차단
    public static void validateMemberAllowsApply(MeetingMember member){
        if(member==null){
            return;
        }
        if(member.getStatus()==MeetingMemberStatus.REMOVED || member.getStatus()==MeetingMemberStatus.EXPELLED){
            throw new BusinessException(MeetingJoinErrorCode.REJECTED_USER_CANNOT_REAPPLY);
        }
    }

    // 가입 신청이 모임에 속하는지 검사
    public static void validateJoinForMeeting(MeetingJoin join, UUID meetingId){
        if(!join.getMeetingId().equals(meetingId)){
            throw new BusinessException(MeetingJoinErrorCode.JOIN_NOT_FOR_MEETING);
        }
    }

    // 이미 모임에 참여중인 사용자인지 검사
    public static void validateNotAlreadyMember(boolean alreadyMember){
        if(alreadyMember){
            throw new BusinessException(MeetingJoinErrorCode.ALREADY_MEETING_MEMBER);
        }
    }

    // 정원 초과 여부 검사
    public static void validateCapacityForApprove(long activeMemberCount, int recruitMax){
        if(activeMemberCount >= recruitMax){
            throw new BusinessException(MeetingJoinErrorCode.MEETING_RECRUIT_FULL);
        }
    }

}

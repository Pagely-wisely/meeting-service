package com.pagely.meetingservice.meeting.domain.policy;

import java.util.UUID;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.domain.exception.MeetingScheduleErrorCode;
import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;
import com.pagely.meetingservice.meeting.domain.model.MeetingScheduleStatus;

public final class MeetingSchedulePolicy {
    private MeetingSchedulePolicy() {
    }

    // 정기 외 일정 추가 가능 여부
    public static void validateAdditionalScheduleAllowed(Meeting meeting) {
        if (meeting.isOneTime()) {
            throw new BusinessException(
                    MeetingScheduleErrorCode.ONE_TIME_MEETING_CANNOT_CREATE_ADDITIONAL_SCHEDULE
            );
        }
    }

    // 일정 생성 권한 검증
    public static void validateHostCreateSchedule(MeetingMember requester) {
        if (!requester.isActive() || !requester.isHost()) {
            throw new BusinessException(MeetingScheduleErrorCode.ONLY_HOST_CAN_CREATE_SCHEDULE);
        }
    }

    // 일정이 모임에 속하는지 검사
    public static void validateScheduleBelongsToMeeting(MeetingSchedule schedule, UUID meetingId) {
        if (!schedule.getMeetingId().equals(meetingId)) {
            throw new BusinessException(MeetingScheduleErrorCode.SCHEDULE_NOT_FOR_MEETING);
        }
    }

    // ONGOING 중복 전이 방지
    public static void validateAlreadyOngoing(MeetingSchedule schedule, MeetingScheduleStatus requested) {
        if (schedule.getStatus() == MeetingScheduleStatus.ONGOING && requested == MeetingScheduleStatus.ONGOING) {
            throw new BusinessException(MeetingScheduleErrorCode.INVALID_SCHEDULE_STATUS_CHANGE);
        }
    }

    // 일정 상태 변경 권한 검사
    public static void validateHostChangesScheduleStatus(MeetingMember updater) {
        if (!updater.isActive() || !updater.isHost()) {
            throw new BusinessException(MeetingScheduleErrorCode.ONLY_HOST_CAN_CHANGE_SCHEDULE_STATUS);
        }
    }
}

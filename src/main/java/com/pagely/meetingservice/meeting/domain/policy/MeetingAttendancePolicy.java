package com.pagely.meetingservice.meeting.domain.policy;

import java.util.UUID;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.domain.exception.MeetingAttendanceErrorCode;
import com.pagely.meetingservice.meeting.domain.exception.MeetingScheduleErrorCode;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingSchedule;

public final class MeetingAttendancePolicy {

    private MeetingAttendancePolicy() {
    }

    public static void validateScheduleBelongsToMeeting(MeetingSchedule schedule, UUID meetingId) {
        if (!schedule.getMeetingId().equals(meetingId)) {
            throw new BusinessException(MeetingScheduleErrorCode.SCHEDULE_NOT_FOR_MEETING);
        }
    }

    public static void validateActiveMemberForScheduleJoin(MeetingMember member) {
        if (!member.isActive()) {
            throw new BusinessException(MeetingAttendanceErrorCode.ONLY_ACTIVE_MEMBER_CAN_JOIN_SCHEDULE);
        }
    }

    public static void validateNoDuplicateAttendance(boolean alreadyJoined) {
        if (alreadyJoined) {
            throw new BusinessException(MeetingAttendanceErrorCode.ATTENDANCE_ALREADY_EXISTS);
        }
    }

    public static void validateAlreadyOngoing(MeetingSchedule schedule) {
        if (!schedule.isOngoing()) {
            throw new BusinessException(MeetingAttendanceErrorCode.ONLY_ONGOING_SCHEDULE_CAN_CHANGE_ATTENDANCE);
        }
    }

    public static void validateHostChangesAttendance(MeetingMember updater) {
        if (!updater.isActive() || !updater.isHost()) {
            throw new BusinessException(MeetingAttendanceErrorCode.ONLY_HOST_CAN_CHANGE_ATTENDANCE);
        }
    }

    public static void validateAttendanceTargetIsActive(MeetingMember target) {
        if (!target.isActive()) {
            throw new BusinessException(MeetingAttendanceErrorCode.ONLY_ACTIVE_MEMBER_CAN_CHANGE_ATTENDANCE);
        }
    }

}

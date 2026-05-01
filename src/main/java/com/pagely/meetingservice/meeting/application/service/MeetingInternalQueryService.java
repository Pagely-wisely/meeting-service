package com.pagely.meetingservice.meeting.application.service;

import com.pagely.meetingservice.meeting.domain.model.MeetingAttendance;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
import com.pagely.meetingservice.meeting.domain.repository.MeetingAttendanceRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import com.pagely.meetingservice.meeting.presentation.dto.response.InternalReadableMeetingsResponse;
import com.pagely.meetingservice.meeting.presentation.dto.response.InternalReadableMeetingsResponse.MeetingWithSchedules;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingInternalQueryService {

    // 조회를 허용할 멤버 상태
    private static final List<MeetingMemberStatus> READABLE_STATUSES = List.of(
            MeetingMemberStatus.ACTIVE,
            MeetingMemberStatus.LEFT,
            MeetingMemberStatus.REMOVED,
            MeetingMemberStatus.EXPELLED
    );

    private final MeetingMemberRepository meetingMemberRepository;
    private final MeetingAttendanceRepository meetingAttendanceRepository;


    // 특정 유저가 속한 모임마다 참석 등록한 일정 ID 목록
    public InternalReadableMeetingsResponse getReadableMeetings(UUID userId) {
        List<UUID> meetingIds = new ArrayList<>(
                meetingMemberRepository.findMeetingIdsByUserIdAndStatuses(userId, READABLE_STATUSES)
        );
        meetingIds.sort(Comparator.naturalOrder());

        List<MeetingAttendance> attendances = meetingAttendanceRepository.findAllByUserId(userId);
        Map<UUID, List<UUID>> scheduleIdsByMeeting = new LinkedHashMap<>();
        for (MeetingAttendance attendance : attendances) {
            UUID meetingId = attendance.getMeetingId();
            scheduleIdsByMeeting
                    .computeIfAbsent(meetingId, ignored -> new ArrayList<>())
                    .add(attendance.getScheduleId());
        }
        scheduleIdsByMeeting.replaceAll((ignored, ids) -> ids // 일정 ID 리스트 정리
                .stream().distinct().sorted().toList()); // 중복 제거 후 UUID 순 정렬

        List<MeetingWithSchedules> items = new ArrayList<>();
        for (UUID meetingId : meetingIds) {
            List<UUID> scheduleIds = scheduleIdsByMeeting.getOrDefault(meetingId, List.of());
            items.add(new MeetingWithSchedules(meetingId, scheduleIds));
        }

        return InternalReadableMeetingsResponse.of(items);
    }

}

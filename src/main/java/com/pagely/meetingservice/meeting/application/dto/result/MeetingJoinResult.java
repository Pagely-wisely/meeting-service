package com.pagely.meetingservice.meeting.application.dto.result;

import java.time.LocalDateTime;
import java.util.UUID;

import com.pagely.meetingservice.meeting.domain.model.MeetingJoin;
import com.pagely.meetingservice.meeting.domain.model.MeetingJoinStatus;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingJoinResult{ // 가입 신청 결과 레코드
    private UUID id;
    private UUID meetingId;
    private UUID recruitUserId;
    private MeetingJoinStatus joinStatus;
    private String content;
    private LocalDateTime createdAt;
    private UUID createdBy;

    public static MeetingJoinResult from(MeetingJoin join) { // 엔티티 → 결과 DTO 변환
        return MeetingJoinResult.builder()
            .id(join.getId())
            .meetingId(join.getMeetingId())
            .recruitUserId(join.getRecruitUserId())
            .joinStatus(join.getJoinStatus())
            .content(join.getContent())
            .createdAt(join.getCreatedAt())
            .createdBy(join.getCreatedBy())
            .build();
    }
}

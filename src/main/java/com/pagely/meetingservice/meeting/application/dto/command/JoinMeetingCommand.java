package com.pagely.meetingservice.meeting.application.dto.command;

import java.time.LocalDateTime;
import java.util.UUID;

import com.pagely.meetingservice.meeting.domain.model.MeetingJoin;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinMeetingCommand{
    private UUID meetingId;
    private UUID recruitUserId;
    private String content;
    private UUID createdBy; // TODO: 임시로 recruitUserId 사용

    public MeetingJoin toMeetingJoin(UUID joinId, LocalDateTime now) {
        return MeetingJoin.create(
                joinId,
                this.meetingId,
                this.recruitUserId,
                this.content,
                now,
                this.createdBy
        );
    }
}

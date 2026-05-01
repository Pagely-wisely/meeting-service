package com.pagely.meetingservice.meeting.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;

@ExtendWith(MockitoExtension.class)
class WarningThresholdServiceTest {

    @Mock
    MeetingMemberRepository meetingMemberRepository;

    @InjectMocks
    WarningThresholdService sut;

    private static final UUID SYSTEM_ACTOR_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Test
    void 임계치_이상이면_강퇴() {
        String eventId = UUID.randomUUID().toString();
        UUID meetingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        MeetingMember member = mock(MeetingMember.class);
        when(meetingMemberRepository.findByMeetingIdAndUserIdForUpdate(meetingId, userId))
                .thenReturn(Optional.of(member));
        when(member.getStatus()).thenReturn(MeetingMemberStatus.ACTIVE);
        when(member.getWarningCount()).thenReturn(3);

        sut.handleAttendanceStatusChanged(eventId, meetingId, userId);

        verify(member).expelBySystem(eq(SYSTEM_ACTOR_ID));
        verify(meetingMemberRepository).save(member);
    }

    @Test
    void 멤버가_없으면_스킵() {
        String eventId = UUID.randomUUID().toString();
        UUID meetingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(meetingMemberRepository.findByMeetingIdAndUserIdForUpdate(meetingId, userId))
                .thenReturn(Optional.empty());

        sut.handleAttendanceStatusChanged(eventId, meetingId, userId);

        verify(meetingMemberRepository, never()).save(any());
    }

    @Test
    void 활성_멤버가_아니면_스킵() {
        String eventId = UUID.randomUUID().toString();
        UUID meetingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        MeetingMember member = mock(MeetingMember.class);
        when(meetingMemberRepository.findByMeetingIdAndUserIdForUpdate(meetingId, userId))
                .thenReturn(Optional.of(member));
        when(member.getStatus()).thenReturn(MeetingMemberStatus.LEFT);

        sut.handleAttendanceStatusChanged(eventId, meetingId, userId);

        verify(member, never()).expelBySystem(any());
        verify(meetingMemberRepository, never()).save(any());
    }

    @Test
    void 경고가_임계치_미만이면_스킵() {
        String eventId = UUID.randomUUID().toString();
        UUID meetingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        MeetingMember member = mock(MeetingMember.class);
        when(meetingMemberRepository.findByMeetingIdAndUserIdForUpdate(meetingId, userId))
                .thenReturn(Optional.of(member));
        when(member.getStatus()).thenReturn(MeetingMemberStatus.ACTIVE);
        when(member.getWarningCount()).thenReturn(2);

        sut.handleAttendanceStatusChanged(eventId, meetingId, userId);

        verify(member, never()).expelBySystem(any());
        verify(meetingMemberRepository, never()).save(any());
    }
}
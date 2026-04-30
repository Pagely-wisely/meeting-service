package com.pagely.meetingservice.meeting.application.service;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import com.pagely.meetingservice.meeting.infrastructure.persistence.JpaProcessedEventRepository;

@ExtendWith(MockitoExtension.class)
class WarningThresholdServiceTest {

    @Mock
    JpaProcessedEventRepository processedEventRepository;

    @Mock
    MeetingMemberRepository meetingMemberRepository;   

    @InjectMocks
    WarningThresholdService sut;

    @Test
    void 임계치_이상이면_강퇴() {
        String eventId = UUID.randomUUID().toString();
        UUID meetingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(processedEventRepository.findByConsumerNameAndEventId(WarningThresholdService.CONSUMER_NAME, eventId))
                .thenReturn(Optional.empty());

        MeetingMember member = mock(MeetingMember.class);
        when(meetingMemberRepository.findByMeetingIdAndUserIdForUpdate(meetingId, userId))
                .thenReturn(Optional.of(member));
        when(member.getStatus()).thenReturn(MeetingMemberStatus.ACTIVE);
        when(member.getWarningCount()).thenReturn(3);

        sut.handleAttendanceStatusChanged(eventId, meetingId, userId);

        verify(member).expelBySystem(any(UUID.class));
        verify(meetingMemberRepository).save(member);
    }



@Test
void 이미_처리된_이벤트_스킵() {
    String eventId = UUID.randomUUID().toString();
    UUID meetingId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(processedEventRepository.findByConsumerNameAndEventId(WarningThresholdService.CONSUMER_NAME, eventId))
            .thenReturn(Optional.of(mock(com.pagely.meetingservice.meeting.domain.model.ProcessedEvent.class)));
    sut.handleAttendanceStatusChanged(eventId, meetingId, userId);
    verify(meetingMemberRepository, never()).findByMeetingIdAndUserIdForUpdate(any(), any());
    verify(meetingMemberRepository, never()).save(any());
}
@Test
void 멤버가_없으면_스킵() {
    String eventId = UUID.randomUUID().toString();
    UUID meetingId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(processedEventRepository.findByConsumerNameAndEventId(WarningThresholdService.CONSUMER_NAME, eventId))
            .thenReturn(Optional.empty());
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
    when(processedEventRepository.findByConsumerNameAndEventId(WarningThresholdService.CONSUMER_NAME, eventId))
            .thenReturn(Optional.empty());
    MeetingMember member = mock(MeetingMember.class);
    when(meetingMemberRepository.findByMeetingIdAndUserIdForUpdate(meetingId, userId))
            .thenReturn(Optional.of(member));
    when(member.getStatus()).thenReturn(MeetingMemberStatus.LEFT);
    sut.handleAttendanceStatusChanged(eventId, meetingId, userId);
    verify(member, never()).expelBySystem(any(UUID.class));
    verify(meetingMemberRepository, never()).save(any());
}
@Test
void 경고가_임계치_미만이면_스킵() {
    String eventId = UUID.randomUUID().toString();
    UUID meetingId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(processedEventRepository.findByConsumerNameAndEventId(WarningThresholdService.CONSUMER_NAME, eventId))
            .thenReturn(Optional.empty());
    MeetingMember member = mock(MeetingMember.class);
    when(meetingMemberRepository.findByMeetingIdAndUserIdForUpdate(meetingId, userId))
            .thenReturn(Optional.of(member));
    when(member.getStatus()).thenReturn(MeetingMemberStatus.ACTIVE);
    when(member.getWarningCount()).thenReturn(2);
    sut.handleAttendanceStatusChanged(eventId, meetingId, userId);
    verify(member, never()).expelBySystem(any(UUID.class));
    verify(meetingMemberRepository, never()).save(any());
}
@Test
void 저장_중복이면_스킵() {
    String eventId = UUID.randomUUID().toString();
    UUID meetingId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(processedEventRepository.findByConsumerNameAndEventId(WarningThresholdService.CONSUMER_NAME, eventId))
            .thenReturn(Optional.empty());
    when(processedEventRepository.save(any()))
            .thenThrow(new DataIntegrityViolationException("duplicate"));
    sut.handleAttendanceStatusChanged(eventId, meetingId, userId);
    verify(meetingMemberRepository, never()).findByMeetingIdAndUserIdForUpdate(any(), any());
    verify(meetingMemberRepository, never()).save(any());
}
}
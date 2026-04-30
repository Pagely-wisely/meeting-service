package com.pagely.meetingservice.meeting.application.service;

import com.pagely.meetingservice.meeting.application.dto.command.CreateMeetingCommand;
import com.pagely.meetingservice.meeting.application.dto.result.MeetingResult;
import com.pagely.meetingservice.meeting.application.port.BookProvider;
import com.pagely.meetingservice.meeting.application.port.EventPublisher;
import com.pagely.meetingservice.meeting.domain.event.MeetingCreatedEvent;
import com.pagely.meetingservice.meeting.domain.model.Meeting;
import com.pagely.meetingservice.meeting.domain.model.MeetingMember;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberRole;
import com.pagely.meetingservice.meeting.domain.model.MeetingMemberStatus;
import com.pagely.meetingservice.meeting.domain.repository.MeetingMemberRepository;
import com.pagely.meetingservice.meeting.domain.repository.MeetingRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 모임 명령 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingCommandService {

    private final MeetingRepository meetingRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final EventPublisher eventPublisher;
    private final BookProvider bookProvider;

    // 모임 생성
    @Transactional
    public MeetingResult createMeeting(CreateMeetingCommand command) {
        UUID meetingId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // 모임 생성 전에 bookId 유효성을 Book Service 내부 API로 검증한다.
        // Book Service에 책이 없으면 Book Service가 외부 API를 통해 생성 후 반환한다.
        // bookId가 비어 있는 경우에는 BookProvider 내부에서 검증을 건너뛴다.
        bookProvider.validateBook(command.bookId());

        // 모임 엔티티 생성
        Meeting meeting = command.toMeeting(meetingId, now);

        // 모임 저장
        Meeting saved = meetingRepository.save(meeting);

        // 모임 생성자는 자동으로 모임장 멤버로 등록한다.
        MeetingMember hostMember = MeetingMember.create(
                UUID.randomUUID(),
                saved.getId(),
                saved.getHostId(),
                MeetingMemberRole.HOST,
                MeetingMemberStatus.ACTIVE,
                now,
                saved.getHostId()
        );

        // 모임장 멤버 저장
        meetingMemberRepository.save(hostMember);

        // 모임 생성 이벤트 발행
        eventPublisher.publish(MeetingCreatedEvent.of(saved));

        return MeetingResult.from(saved);
    }
}
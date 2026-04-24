package com.pagely.meetingservice.meeting.infrastructure.persistence;

import com.pagely.meetingservice.meeting.domain.model.Meeting;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMeetingRepository extends JpaRepository<Meeting, UUID> {

    List<Meeting> findByHostId(UUID hostId);
}

// package com.pagely.meetingservice.meeting.application.service;

// import com.pagely.meetingservice.meeting.domain.model.MeetingJoin;
// import com.pagely.meetingservice.meeting.domain.model.MeetingJoinStatus;
// import java.time.LocalDateTime;
// import java.util.Comparator;
// import java.util.List;
// import org.springframework.stereotype.Component;

// @Component
// public class MeetingJoinListSortPolicy {

//     public void sortForHostJoinList(List<MeetingJoin> joins) {
//         joins.sort(defaultHostListComparator());
//     }

//     private Comparator<MeetingJoin> defaultHostListComparator() {
//         Comparator<LocalDateTime> createdAtDesc =
//                 Comparator.nullsLast(Comparator.<LocalDateTime>naturalOrder()).reversed();
//         return Comparator
//                 .comparing((MeetingJoin j) -> j.getJoinStatus() != MeetingJoinStatus.PENDING)
//                 .thenComparing(MeetingJoin::getCreatedAt, createdAtDesc);
//     }
// }

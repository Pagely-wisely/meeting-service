package com.pagely.meetingservice.meeting.infrastructure.seeder;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DummyDataSeeder implements CommandLineRunner {

    private static final int MEETING_COUNT = 1000;
    private static final int SCHEDULE_PER_MEETING = 3;
    private static final int ATTENDANCE_PER_SCHEDULE = 10;

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();

    @Override
    public void run(String... args) {
        // 기존 데이터가 있으면 더미 데이터 생성을 건너뛴다.
        Long meetingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM p_meeting",
                Long.class
        );

        if (meetingCount != null && meetingCount >= MEETING_COUNT) {
            log.info("더미 데이터가 이미 충분히 존재하므로 생성을 건너뜁니다. meetingCount={}", meetingCount);
            return;
        }

        log.info("더미 데이터 생성을 시작합니다. currentMeetingCount={}", meetingCount);

        for (int i = 1; i <= MEETING_COUNT; i++) {
            UUID meetingId = UUID.randomUUID();
            UUID hostId = UUID.randomUUID();

            insertMeeting(meetingId, hostId, i);
            insertHostMember(meetingId, hostId);

            for (int j = 1; j <= SCHEDULE_PER_MEETING; j++) {
                UUID scheduleId = UUID.randomUUID();

                insertSchedule(scheduleId, meetingId, hostId, j);

                for (int k = 1; k <= ATTENDANCE_PER_SCHEDULE; k++) {
                    UUID userId = UUID.randomUUID();

                    insertMember(meetingId, userId, hostId);
                    insertAttendance(meetingId, scheduleId, userId);
                }
            }

            if (i % 100 == 0) {
                log.info("더미 모임 생성 진행 중... {}/{}", i, MEETING_COUNT);
            }
        }

        log.info("더미 데이터 생성 완료");
        log.info("Meeting 추가 생성={}", MEETING_COUNT);
        log.info("Schedule 추가 생성={}", MEETING_COUNT * SCHEDULE_PER_MEETING);
        log.info("Attendance 추가 생성={}", MEETING_COUNT * SCHEDULE_PER_MEETING * ATTENDANCE_PER_SCHEDULE);
    }

    private void insertMeeting(UUID meetingId, UUID hostId, int index) {
        jdbcTemplate.update("""
                        INSERT INTO p_meeting (
                            id,
                            host_id,
                            book_id,
                            title,
                            description,
                            meeting_type,
                            meeting_status,
                            recruit_status,
                            recruit_start_at,
                            recruit_end_at,
                            recruit_max,
                            reading_level,
                            rule_memo,
                            recruit_rate,
                            free_paid,
                            created_at,
                            created_by
                        )
                        VALUES (?, ?, ?, ?, ?, ?::meeting_type, ?::meeting_status, ?::meeting_recruit_status, ?, ?, ?, ?::reading_level, ?, ?::recruit_rate, ?, ?, ?)
                        """,
                meetingId,
                hostId,
                shortBookId(),
                "부하테스트 모임 " + index,
                "JMeter 부하테스트용 더미 모임입니다.",
                randomMeetingType(),
                randomMeetingStatus(),
                randomRecruitStatus(),
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now().plusDays(30),
                30,
                randomReadingLevel(),
                "부하테스트용 규칙입니다.",
                randomRecruitRate(),
                true,
                LocalDateTime.now(),
                hostId
        );
    }

    private void insertHostMember(UUID meetingId, UUID hostId) {
        jdbcTemplate.update("""
                        INSERT INTO p_meeting_members (
                            id,
                            meeting_id,
                            user_id,
                            role,
                            status,
                            absent_count,
                            warning_count,
                            late_count,
                            created_at,
                            created_by
                        )
                        VALUES (?, ?, ?, ?::meeting_member_role, ?::meeting_member_status, ?, ?, ?, ?, ?)
                        """,
                UUID.randomUUID(),
                meetingId,
                hostId,
                "HOST",
                "ACTIVE",
                0,
                0,
                0,
                LocalDateTime.now(),
                hostId
        );
    }

    private void insertMember(UUID meetingId, UUID userId, UUID createdBy) {
        jdbcTemplate.update("""
                        INSERT INTO p_meeting_members (
                            id,
                            meeting_id,
                            user_id,
                            role,
                            status,
                            absent_count,
                            warning_count,
                            late_count,
                            created_at,
                            created_by
                        )
                        VALUES (?, ?, ?, ?::meeting_member_role, ?::meeting_member_status, ?, ?, ?, ?, ?)
                        """,
                UUID.randomUUID(),
                meetingId,
                userId,
                "MEMBER",
                "ACTIVE",
                random.nextInt(3),
                random.nextInt(3),
                random.nextInt(5),
                LocalDateTime.now(),
                createdBy
        );
    }

    private void insertSchedule(UUID scheduleId, UUID meetingId, UUID createdBy, int scheduleNumber) {
        jdbcTemplate.update("""
                        INSERT INTO p_meeting_schedules (
                            id,
                            meeting_id,
                            schedule_number,
                            book_id,
                            status,
                            start_at,
                            discussion_note,
                            created_at,
                            created_by
                        )
                        VALUES (?, ?, ?, ?, ?::meeting_schedule_status, ?, ?, ?, ?)
                        """,
                scheduleId,
                meetingId,
                scheduleNumber,
                shortBookId(),
                randomScheduleStatus(),
                LocalDateTime.now().minusDays(random.nextInt(30)).plusHours(random.nextInt(24)),
                "부하테스트용 토론 메모입니다.",
                LocalDateTime.now(),
                createdBy
        );
    }

    private void insertAttendance(UUID meetingId, UUID scheduleId, UUID userId) {
        jdbcTemplate.update("""
                        INSERT INTO p_meeting_attendance (
                            id,
                            meeting_id,
                            schedule_id,
                            user_id,
                            status,
                            checked_at,
                            note,
                            created_at,
                            created_by
                        )
                        VALUES (?, ?, ?, ?, ?::attendance_status, ?, ?, ?, ?)
                        """,
                UUID.randomUUID(),
                meetingId,
                scheduleId,
                userId,
                randomAttendanceStatus(),
                LocalDateTime.now().minusMinutes(random.nextInt(120)),
                "부하테스트용 출석 데이터입니다.",
                LocalDateTime.now(),
                userId
        );
    }

    private String shortBookId() {
        // p_meeting.book_id, p_meeting_schedules.book_id 컬럼이 VARCHAR(20)이므로 짧은 문자열을 사용한다.
        return UUID.randomUUID().toString().substring(0, 20);
    }

    private String randomMeetingType() {
        return random.nextBoolean() ? "REGULAR" : "ONES";
    }

    private String randomMeetingStatus() {
        String[] values = {"UPCOMING", "IN_PROGRESS", "COMPLETED"};
        return values[random.nextInt(values.length)];
    }

    private String randomRecruitStatus() {
        String[] values = {"RECRUITING", "FULL", "CLOSED"};
        return values[random.nextInt(values.length)];
    }

    private String randomReadingLevel() {
        String[] values = {"BEGINNER", "NORMAL", "ADVANCED"};
        return values[random.nextInt(values.length)];
    }

    private String randomRecruitRate() {
        String[] values = {"ONCE", "WEEKLY", "BIWEEKLY", "MONTHLY"};
        return values[random.nextInt(values.length)];
    }

    private String randomScheduleStatus() {
        String[] values = {"SCHEDULED", "ONGOING", "FINISHED", "CANCELLED"};
        return values[random.nextInt(values.length)];
    }

    private String randomAttendanceStatus() {
        String[] values = {"PENDING", "ATTENDED", "LATE", "ABSENT", "EXCUSED"};
        return values[random.nextInt(values.length)];
    }
}

-- =========================
-- p_meeting
-- =========================
INSERT INTO p_meeting (id, host_id, book_id, title, description,
                       meeting_type, meeting_status, recruit_status,
                       recruit_start_at, recruit_end_at, recruit_max,
                       reading_level, rule_memo, recruit_rate, free_paid,
                       created_by, updated_by, deleted_at, deleted_by)
VALUES ('11111111-1111-1111-1111-111111111111',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        NULL,
        '주말 독서모임',
        '매주 토요일 아침 함께 책을 읽고 이야기하는 정기 모임입니다.',
        'REGULAR',
        'UPCOMING',
        'RECRUITING',
        '2026-04-23 09:00:00',
        '2026-05-10 23:59:59',
        8,
        'NORMAL',
        '지각 3회 시 경고 1회, 월 회비 5000원',
        'WEEKLY',
        TRUE,
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        NULL,
        NULL,
        NULL),
       ('22222222-2222-2222-2222-222222222222',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        '9788936434120',
        '한강 작품 같이 읽기',
        '한강 작품 한 권을 읽고 하루 동안 토론하는 일회성 모임입니다.',
        'ONES',
        'UPCOMING',
        'RECRUITING',
        '2026-04-23 09:00:00',
        '2026-05-03 18:00:00',
        5,
        'BEGINNER',
        '참석 필수, 노쇼 금지',
        'ONCE',
        FALSE,
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        NULL,
        NULL,
        NULL);

-- =========================
-- p_meeting_members
-- =========================
INSERT INTO p_meeting_members (id, meeting_id, user_id, role, status,
                               absent_count, warning_count, created_by)
VALUES ('31111111-1111-1111-1111-111111111111',
        '11111111-1111-1111-1111-111111111111',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'HOST',
        'ACTIVE',
        0,
        0,
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
       ('32222222-2222-2222-2222-222222222222',
        '11111111-1111-1111-1111-111111111111',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'MEMBER',
        'ACTIVE',
        0,
        0,
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
       ('33333333-3333-3333-3333-333333333333',
        '22222222-2222-2222-2222-222222222222',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'HOST',
        'ACTIVE',
        0,
        0,
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb');

-- =========================
-- p_meeting_recruit
-- =========================
INSERT INTO p_meeting_recruit (id, meeting_id, recruit_user_id, recruit_status,
                               content, reject_reason, created_by)
VALUES ('41111111-1111-1111-1111-111111111111',
        '11111111-1111-1111-1111-111111111111',
        'dddddddd-dddd-dddd-dddd-dddddddddddd',
        'PENDING',
        '정기적으로 참여하고 싶습니다.',
        NULL,
        'dddddddd-dddd-dddd-dddd-dddddddddddd'),
       ('42222222-2222-2222-2222-222222222222',
        '22222222-2222-2222-2222-222222222222',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
        'APPROVED',
        '한강 작품 모임 참여 희망합니다.',
        NULL,
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee');

-- =========================
-- p_meeting_schedules
-- =========================
INSERT INTO p_meeting_schedules (id, meeting_id, schedule_number, book_id, status,
                                 start_at, discussion_note, created_by)
VALUES ('51111111-1111-1111-1111-111111111111',
        '11111111-1111-1111-1111-111111111111',
        1,
        '9788936434120',
        'SCHEDULED',
        '2026-05-11 10:00:00',
        NULL,
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
       ('52222222-2222-2222-2222-222222222222',
        '22222222-2222-2222-2222-222222222222',
        1,
        '9788936434120',
        'SCHEDULED',
        '2026-05-04 14:00:00',
        NULL,
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb');

-- =========================
-- p_meeting_attendance
-- =========================
INSERT INTO p_meeting_attendance (id, meeting_id, schedule_id, user_id, status,
                                  checked_at, note, created_by)
VALUES ('61111111-1111-1111-1111-111111111111',
        '11111111-1111-1111-1111-111111111111',
        '51111111-1111-1111-1111-111111111111',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'PENDING',
        NULL,
        NULL,
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
       ('62222222-2222-2222-2222-222222222222',
        '11111111-1111-1111-1111-111111111111',
        '51111111-1111-1111-1111-111111111111',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'PENDING',
        NULL,
        NULL,
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
       ('63333333-3333-3333-3333-333333333333',
        '22222222-2222-2222-2222-222222222222',
        '52222222-2222-2222-2222-222222222222',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'PENDING',
        NULL,
        NULL,
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb');

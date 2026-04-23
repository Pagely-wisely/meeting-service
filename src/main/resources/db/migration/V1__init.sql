-- =========================
-- ENUM TYPES
-- =========================
CREATE TYPE meeting_type AS ENUM ('ONES', 'REGULAR');

CREATE TYPE meeting_status AS ENUM (
    'UPCOMING',
    'IN_PROGRESS',
    'COMPLETED',
    'CANCELLED'
);

CREATE TYPE meeting_recruit_status AS ENUM (
    'RECRUITING',
    'FULL',
    'CLOSED'
);

CREATE TYPE reading_level AS ENUM (
    'BEGINNER',
    'NORMAL',
    'ADVANCED'
);

CREATE TYPE recruit_rate AS ENUM (
    'ONCE',
    'WEEKLY',
    'BIWEEKLY',
    'MONTHLY'
);

CREATE TYPE meeting_join_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'CANCELLED',
    'EXPIRED'
);

CREATE TYPE meeting_member_role AS ENUM (
    'HOST',
    'MEMBER'
);

CREATE TYPE meeting_member_status AS ENUM (
    'ACTIVE',
    'LEFT',
    'REMOVED',
    'EXPELLED'
);

CREATE TYPE meeting_schedule_status AS ENUM (
    'SCHEDULED',
    'ONGOING',
    'FINISHED',
    'CANCELLED'
);

CREATE TYPE attendance_status AS ENUM (
    'PENDING',
    'ATTENDED',
    'LATE',
    'ABSENT',
    'EXCUSED'
);

-- =========================
-- TABLE: p_meeting
-- =========================
CREATE TABLE p_meeting (
                           id UUID PRIMARY KEY,
                           host_id UUID NOT NULL,
                           book_id VARCHAR(20),
                           title VARCHAR(20) NOT NULL,
                           description TEXT,
                           meeting_type meeting_type NOT NULL,
                           meeting_status meeting_status NOT NULL,
                           recruit_status meeting_recruit_status NOT NULL,
                           recruit_start_at TIMESTAMP NOT NULL,
                           recruit_end_at TIMESTAMP NOT NULL,
                           recruit_max INT NOT NULL,
                           reading_level reading_level NOT NULL,
                           rule_memo TEXT,
                           recruit_rate recruit_rate NOT NULL,
                           free_paid BOOLEAN NOT NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                           created_by UUID NOT NULL,
                           updated_at TIMESTAMP,
                           updated_by UUID,
                           deleted_at TIMESTAMP,
                           deleted_by UUID,
                           CONSTRAINT ck_p_meeting_recruit_max CHECK (recruit_max > 0),
                           CONSTRAINT ck_p_meeting_recruit_period CHECK (recruit_start_at <= recruit_end_at)
);

-- =========================
-- TABLE: p_meeting_recruit
-- =========================
CREATE TABLE p_meeting_recruit (
                                   id UUID PRIMARY KEY,
                                   meeting_id UUID NOT NULL,
                                   recruit_user_id UUID NOT NULL,
                                   recruit_status meeting_join_status NOT NULL,
                                   content TEXT,
                                   reject_reason TEXT,
                                   created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                   created_by UUID NOT NULL,
                                   updated_at TIMESTAMP,
                                   updated_by UUID,
                                   deleted_at TIMESTAMP,
                                   deleted_by UUID,
                                   CONSTRAINT fk_p_meeting_recruit_meeting
                                       FOREIGN KEY (meeting_id) REFERENCES p_meeting(id)
);

-- =========================
-- TABLE: p_meeting_members
-- =========================
CREATE TABLE p_meeting_members (
                                   id UUID PRIMARY KEY,
                                   meeting_id UUID NOT NULL,
                                   user_id UUID NOT NULL,
                                   role meeting_member_role NOT NULL,
                                   status meeting_member_status NOT NULL,
                                   absent_count INT NOT NULL DEFAULT 0,
                                   warning_count INT NOT NULL DEFAULT 0,
                                   created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                   created_by UUID NOT NULL,
                                   updated_at TIMESTAMP,
                                   updated_by UUID,
                                   deleted_at TIMESTAMP,
                                   deleted_by UUID,
                                   CONSTRAINT fk_p_meeting_members_meeting
                                       FOREIGN KEY (meeting_id) REFERENCES p_meeting(id),
                                   CONSTRAINT ck_p_meeting_members_absent_count CHECK (absent_count >= 0),
                                   CONSTRAINT ck_p_meeting_members_warning_count CHECK (warning_count >= 0)
);

-- =========================
-- TABLE: p_meeting_schedules
-- =========================
CREATE TABLE p_meeting_schedules (
                                     id UUID PRIMARY KEY,
                                     meeting_id UUID NOT NULL,
                                     schedule_number INT NOT NULL,
                                     book_id VARCHAR(20),
                                     status meeting_schedule_status NOT NULL,
                                     start_at TIMESTAMP NOT NULL,
                                     discussion_note TEXT,
                                     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                     created_by UUID NOT NULL,
                                     updated_at TIMESTAMP,
                                     updated_by UUID,
                                     deleted_at TIMESTAMP,
                                     deleted_by UUID,
                                     CONSTRAINT fk_p_meeting_schedules_meeting
                                         FOREIGN KEY (meeting_id) REFERENCES p_meeting(id),
                                     CONSTRAINT ck_p_meeting_schedules_number CHECK (schedule_number > 0)
);

-- =========================
-- TABLE: p_meeting_attendance
-- =========================
CREATE TABLE p_meeting_attendance (
                                      id UUID PRIMARY KEY,
                                      meeting_id UUID NOT NULL,
                                      schedule_id UUID NOT NULL,
                                      user_id UUID NOT NULL,
                                      status attendance_status NOT NULL,
                                      checked_at TIMESTAMP,
                                      note VARCHAR(255),
                                      created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                      created_by UUID NOT NULL,
                                      updated_at TIMESTAMP,
                                      updated_by UUID,
                                      deleted_at TIMESTAMP,
                                      deleted_by UUID,
                                      CONSTRAINT fk_p_meeting_attendance_meeting
                                          FOREIGN KEY (meeting_id) REFERENCES p_meeting(id),
                                      CONSTRAINT fk_p_meeting_attendance_schedule
                                          FOREIGN KEY (schedule_id) REFERENCES p_meeting_schedules(id)
);

-- =========================
-- INDEX
-- =========================
CREATE INDEX idx_p_meeting_host_id ON p_meeting(host_id);
CREATE INDEX idx_p_meeting_book_id ON p_meeting(book_id);
CREATE INDEX idx_p_meeting_meeting_status ON p_meeting(meeting_status);
CREATE INDEX idx_p_meeting_recruit_status ON p_meeting(recruit_status);
CREATE INDEX idx_p_meeting_deleted_at ON p_meeting(deleted_at);

CREATE INDEX idx_p_meeting_recruit_meeting_id ON p_meeting_recruit(meeting_id);
CREATE INDEX idx_p_meeting_recruit_user_id ON p_meeting_recruit(recruit_user_id);
CREATE INDEX idx_p_meeting_recruit_deleted_at ON p_meeting_recruit(deleted_at);

CREATE INDEX idx_p_meeting_members_meeting_id ON p_meeting_members(meeting_id);
CREATE INDEX idx_p_meeting_members_user_id ON p_meeting_members(user_id);
CREATE INDEX idx_p_meeting_members_deleted_at ON p_meeting_members(deleted_at);

CREATE INDEX idx_p_meeting_schedules_meeting_id ON p_meeting_schedules(meeting_id);
CREATE INDEX idx_p_meeting_schedules_start_at ON p_meeting_schedules(start_at);
CREATE INDEX idx_p_meeting_schedules_deleted_at ON p_meeting_schedules(deleted_at);

CREATE INDEX idx_p_meeting_attendance_meeting_id ON p_meeting_attendance(meeting_id);
CREATE INDEX idx_p_meeting_attendance_schedule_id ON p_meeting_attendance(schedule_id);
CREATE INDEX idx_p_meeting_attendance_user_id ON p_meeting_attendance(user_id);
CREATE INDEX idx_p_meeting_attendance_deleted_at ON p_meeting_attendance(deleted_at);

-- =========================
-- UNIQUE CONSTRAINT
-- =========================
ALTER TABLE p_meeting_members
    ADD CONSTRAINT uk_p_meeting_members_meeting_user UNIQUE (meeting_id, user_id);

ALTER TABLE p_meeting_schedules
    ADD CONSTRAINT uk_p_meeting_schedules_meeting_schedule UNIQUE (meeting_id, schedule_number);

ALTER TABLE p_meeting_attendance
    ADD CONSTRAINT uk_p_meeting_attendance_schedule_user UNIQUE (schedule_id, user_id);
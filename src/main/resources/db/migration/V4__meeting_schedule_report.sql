-- =========================
-- TABLE: p_meeting_schedule_report
-- =========================
CREATE TABLE p_meeting_schedule_report
(
    id          UUID PRIMARY KEY,
    event_id    VARCHAR(100) NOT NULL,
    report_id   UUID         NOT NULL,
    meeting_id  UUID         NOT NULL,
    schedule_id UUID         NOT NULL,
    user_id     UUID         NOT NULL,
    reported_at TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- =========================
-- UNIQUE CONSTRAINT
-- =========================

-- 같은 독후감 생성 이벤트가 중복 수신되어도 한 번만 저장되도록 방지
ALTER TABLE p_meeting_schedule_report
    ADD CONSTRAINT uk_p_meeting_schedule_report_event UNIQUE (event_id);

-- 한 사용자가 같은 일정에 독후감을 여러 개 작성해도
-- 일정 시작 판단 기준으로는 "작성함" 한 번만 인정
ALTER TABLE p_meeting_schedule_report
    ADD CONSTRAINT uk_p_meeting_schedule_report_schedule_user UNIQUE (schedule_id, user_id);

-- =========================
-- INDEX
-- =========================
CREATE INDEX idx_p_meeting_schedule_report_meeting_id
    ON p_meeting_schedule_report (meeting_id);

CREATE INDEX idx_p_meeting_schedule_report_schedule_id
    ON p_meeting_schedule_report (schedule_id);

CREATE INDEX idx_p_meeting_schedule_report_user_id
    ON p_meeting_schedule_report (user_id);

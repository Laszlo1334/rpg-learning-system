-- Migration V5: Add missing tables for Submission and UserQuestionFailure entities

CREATE TABLE IF NOT EXISTS submissions (
    id               BIGSERIAL PRIMARY KEY,
    task_id          BIGINT REFERENCES tasks(id),
    student_id       BIGINT REFERENCES users(id),
    student_answer   TEXT,
    status           VARCHAR(50),
    teacher_comment  TEXT,
    attempt_number   INTEGER DEFAULT 1,
    submitted_at     TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_question_failures (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id),
    question_id BIGINT NOT NULL REFERENCES questions(id),
    failed_at   TIMESTAMP,
    CONSTRAINT uq_user_question_failure UNIQUE (user_id, question_id)
);

-- User analytics metrics
ALTER TABLE users ADD COLUMN IF NOT EXISTS total_login_days INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS longest_login_streak INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS total_play_time_seconds BIGINT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS current_flawless_streak INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS longest_flawless_streak INTEGER NOT NULL DEFAULT 0;

-- Completed task effort metrics
ALTER TABLE completed_tasks ADD COLUMN IF NOT EXISTS attempts_taken INTEGER NOT NULL DEFAULT 1;
ALTER TABLE completed_tasks ADD COLUMN IF NOT EXISTS hints_used BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE completed_tasks ADD COLUMN IF NOT EXISTS time_spent_seconds BIGINT NOT NULL DEFAULT 0;

-- User question failure tracking (upsert-friendly)
ALTER TABLE user_question_failures ADD COLUMN IF NOT EXISTS failure_count INTEGER NOT NULL DEFAULT 1;
ALTER TABLE user_question_failures ADD COLUMN IF NOT EXISTS last_wrong_answer VARCHAR(1024);

UPDATE user_question_failures SET failure_count = 1 WHERE failure_count IS NULL;

-- Activity event timeline
CREATE TABLE IF NOT EXISTS activity_logs (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id),
    action_type VARCHAR(50) NOT NULL,
    details     TEXT,
    timestamp   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_activity_logs_user_timestamp ON activity_logs (user_id, timestamp DESC);

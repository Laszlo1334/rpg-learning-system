-- Migration V4: Sync courses table with Course entity
-- Adds missing columns: author_id, access_code, created_at

ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS author_id  BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS access_code VARCHAR(255),
    ADD COLUMN IF NOT EXISTS created_at  TIMESTAMP;

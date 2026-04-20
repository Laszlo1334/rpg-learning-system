-- Migration V3: Add course_students table
-- Matches the CourseStudent @Entity with @JoinColumn(name = "course_id") and @JoinColumn(name = "student_id")

CREATE TABLE course_students (
    id          BIGSERIAL PRIMARY KEY,
    course_id   BIGINT NOT NULL REFERENCES courses(id),
    student_id  BIGINT NOT NULL REFERENCES users(id),
    joined_at   TIMESTAMP
);

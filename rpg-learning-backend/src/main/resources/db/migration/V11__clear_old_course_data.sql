-- Migration V11: Clear old course, task, question, and progress data before seeding new course data

DELETE FROM course_students;
DELETE FROM submissions;
DELETE FROM completed_tasks;
DELETE FROM user_question_failures;
DELETE FROM question_correct_answers;
DELETE FROM question_options;
DELETE FROM questions;
DELETE FROM task_prerequisites;
DELETE FROM tasks;
DELETE FROM courses;

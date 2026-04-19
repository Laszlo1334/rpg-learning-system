CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    role VARCHAR(50),
    avatar_url VARCHAR(255),
    level INTEGER NOT NULL DEFAULT 1,
    current_xp INTEGER NOT NULL DEFAULT 0,
    gold INTEGER NOT NULL DEFAULT 0,
    crystals INTEGER NOT NULL DEFAULT 0,
    campfire_level INTEGER NOT NULL DEFAULT 1,
    last_login_date TIMESTAMP,
    energy INTEGER NOT NULL DEFAULT 100,
    last_task_completion_date TIMESTAMP,
    is_public_profile BOOLEAN NOT NULL DEFAULT TRUE,
    xp_buff_ends_at TIMESTAMP,
    gold_buff_ends_at TIMESTAMP,
    energy_stasis_ends_at TIMESTAMP,
    has_active_shield BOOLEAN DEFAULT FALSE,
    lifetime_gold INTEGER NOT NULL DEFAULT 0,
    lifetime_crystals INTEGER NOT NULL DEFAULT 0,
    total_tasks_completed INTEGER NOT NULL DEFAULT 0,
    total_failures INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255),
    price INTEGER NOT NULL,
    currency_type VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    effect VARCHAR(50) NOT NULL,
    slot VARCHAR(50) NOT NULL,
    asset_url VARCHAR(255)
);

CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    item_id BIGINT NOT NULL REFERENCES items(id),
    is_equipped BOOLEAN NOT NULL DEFAULT FALSE,
    quantity INTEGER NOT NULL DEFAULT 1,
    purchased_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    status VARCHAR(50)
);

CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT REFERENCES courses(id),
    title VARCHAR(255),
    theory_content TEXT,
    branch_name VARCHAR(255),
    order_index INTEGER NOT NULL,
    is_theory_hidden BOOLEAN NOT NULL DEFAULT FALSE,
    reward_xp INTEGER,
    reward_gold INTEGER,
    dynamic_question_count INTEGER NOT NULL DEFAULT 5,
    type VARCHAR(50) NOT NULL DEFAULT 'REGULAR',
    boss_name VARCHAR(255),
    boss_avatar_url VARCHAR(255),
    time_limit_seconds INTEGER
);

CREATE TABLE task_prerequisites (
    task_id BIGINT NOT NULL REFERENCES tasks(id),
    prerequisite_task_id BIGINT NOT NULL
);

CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id),
    question_text TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    explanation TEXT
);

CREATE TABLE question_options (
    question_id BIGINT NOT NULL REFERENCES questions(id),
    option_text VARCHAR(255)
);

CREATE TABLE question_correct_answers (
    question_id BIGINT NOT NULL REFERENCES questions(id),
    correct_answer VARCHAR(255)
);

CREATE TABLE completed_tasks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    task_id BIGINT NOT NULL REFERENCES tasks(id),
    completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

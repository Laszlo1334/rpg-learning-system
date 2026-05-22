# Thesis Technical Context — RPG Learning Platform

---

## 1. Executive Summary & Technology Stack

### Backend Stack
| Layer | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 3.2.3 |
| Security | Spring Security + OAuth2 Client | (Boot-managed) |
| Persistence | Spring Data JPA / Hibernate | (Boot-managed) |
| Database | PostgreSQL | 15 (Docker) |
| Migrations | Flyway | (Boot-managed) |
| API Docs | springdoc-openapi (Swagger UI) | 2.4.0 |
| Boilerplate reduction | Lombok | 1.18.42 |
| Build tool | Maven | - |

### Frontend Stack
| Layer | Technology | Version |
|---|---|---|
| Language | TypeScript | ~5.9.3 |
| UI Library | React | ^19.2.0 |
| Routing | React Router DOM | ^7.13.1 |
| State Management | Zustand | ^5.0.11 |
| HTTP Client | Axios | ^1.13.5 |
| CSS Framework | Tailwind CSS | ^4.2.1 |
| Build Tool | Vite | ^7.3.1 |
| Node Graph | ReactFlow | ^11.11.4 |
| Icon Library | Lucide React | ^0.575.0 |
| Confetti FX | canvas-confetti | ^1.9.4 |

### High-Level Architecture
- **Client–Server, SPA + REST API pattern.**
- The React SPA runs on `http://localhost:5173` (Vite dev server).
- All API calls go to `http://localhost:8080/api` via a shared Axios instance (`src/services/api.ts`) configured with `withCredentials: true` to send the Spring Session cookie (`JSESSIONID`) on every request.
- Authentication is entirely session-based via **Google OAuth2**. Spring Security stores the principal in an HTTP session; the browser forwards the session cookie automatically.
- The PostgreSQL database runs in Docker (`docker-compose.yml`) on port `5433`, mapped from container port `5432`. The database name is `edu_rpg`.
- Flyway runs automatically on startup and applies versioned SQL migrations from `src/main/resources/db/migration/`.
- The `DatabaseSeeder` (`CommandLineRunner` bean) executes after Flyway and upserts course/item data from JSON.

---

## 2. Database Schema & Data Modeling

### Primary Tables

#### `users`
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | Auto-increment |
| username | VARCHAR(255) | Display name |
| email | VARCHAR(255) UNIQUE | Login identity |
| password | VARCHAR(255) | BCrypt hash; empty string for OAuth users |
| role | VARCHAR(50) | Enum: STUDENT, TEACHER, ADMIN |
| avatar_url | VARCHAR(255) | Default: `/assets/default_avatar.png` |
| level | INTEGER DEFAULT 1 | Derived: `(currentXp / 1000) + 1` |
| current_xp | INTEGER DEFAULT 0 | Triggers level recalc in `setCurrentXp()` |
| gold | INTEGER DEFAULT 0 | Current spendable balance |
| crystals | INTEGER DEFAULT 0 | Failure-reward currency |
| campfire_level | INTEGER DEFAULT 1 | Login streak tier (1–5) |
| last_login_date | TIMESTAMP | Used for campfire decay check (48 h) |
| energy | INTEGER DEFAULT 100 | Max 100; costs deducted on task attempt |
| last_task_completion_date | TIMESTAMP | Anchor for energy regeneration (+1/6 min) |
| is_public_profile | BOOLEAN DEFAULT TRUE | Opt-out system for leaderboard |
| xp_buff_ends_at | TIMESTAMP | Active XP Boost expiry |
| gold_buff_ends_at | TIMESTAMP | Active Gold Boost expiry |
| energy_stasis_ends_at | TIMESTAMP | Reserved for energy-freeze item |
| has_active_shield | BOOLEAN DEFAULT FALSE | Rune of Protection flag |
| lifetime_gold | INTEGER DEFAULT 0 | All-time gold earned (for leaderboard sort) |
| lifetime_crystals | INTEGER DEFAULT 0 | All-time crystals earned |
| total_tasks_completed | INTEGER DEFAULT 0 | Global task counter |
| total_failures | INTEGER DEFAULT 0 | Key metric for "Productive Failure" analysis |
| total_login_days | INTEGER DEFAULT 0 | Added V10 |
| longest_login_streak | INTEGER DEFAULT 0 | Added V10 |
| total_play_time_seconds | BIGINT DEFAULT 0 | Accumulated seconds in Arena |
| current_flawless_streak | INTEGER DEFAULT 0 | Resets on any failure |
| longest_flawless_streak | INTEGER DEFAULT 0 | All-time record |

#### `courses`
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| title | VARCHAR(255) | |
| description | TEXT | |
| status | VARCHAR(50) | Deprecated field; status computed dynamically |

#### `tasks`
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| course_id | BIGINT FK → courses(id) | Many tasks per course |
| title | VARCHAR(255) | |
| theory_content | TEXT | HTML content rendered in Arena |
| branch_name | VARCHAR(255) | e.g., "Практика", "Теорія", "Арена" |
| order_index | INTEGER NOT NULL | Defines display order |
| is_theory_hidden | BOOLEAN DEFAULT FALSE | Mode B: no theory shown |
| reward_xp | INTEGER | Base XP granted on completion |
| reward_gold | INTEGER | Base Gold granted on completion |
| dynamic_question_count | INTEGER DEFAULT 5 | How many questions to sample per run |
| type | VARCHAR(50) DEFAULT 'REGULAR' | Enum: REGULAR, BOSS, MEMORY |
| boss_name | VARCHAR(255) | Null for REGULAR |
| boss_avatar_url | VARCHAR(255) | Null for REGULAR |
| time_limit_seconds | INTEGER | Null for REGULAR |

#### `task_prerequisites`
| Column | Type | Notes |
|---|---|---|
| task_id | BIGINT FK → tasks(id) | |
| prerequisite_task_id | BIGINT | The task that must be completed first |

*This is an `@ElementCollection` on `Task.prerequisiteTaskIds`. It implements the DAG (Directed Acyclic Graph) prerequisite system — a task is locked until all prerequisite task IDs appear in `completed_tasks` for the current user.*

#### `questions`
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| task_id | BIGINT NOT NULL FK → tasks(id) | |
| question_text | TEXT NOT NULL | |
| type | VARCHAR(50) NOT NULL | Enum: TEST (multiple choice), TEXT (open) |
| explanation | TEXT | Shown after a wrong answer |

#### `question_options` (ElementCollection)
| Column | Type | Notes |
|---|---|---|
| question_id | BIGINT FK → questions(id) | |
| option_text | VARCHAR(255) | Answer choice text |

#### `question_correct_answers` (ElementCollection)
| Column | Type | Notes |
|---|---|---|
| question_id | BIGINT FK → questions(id) | |
| correct_answer | VARCHAR(255) | One or more acceptable answers |

#### `completed_tasks`
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| user_id | BIGINT FK → users(id) | |
| task_id | BIGINT FK → tasks(id) | |
| completed_at | TIMESTAMP DEFAULT NOW() | |
| attempts_taken | INTEGER DEFAULT 1 | Added V10 |
| hints_used | BOOLEAN DEFAULT FALSE | Added V10; true if user toggled theory during run |
| time_spent_seconds | BIGINT DEFAULT 0 | Added V10 |

#### `user_question_failures`
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| user_id | BIGINT FK → users(id) | |
| question_id | BIGINT FK → questions(id) | |
| failure_count | INTEGER DEFAULT 1 | Added V10; tracks repeat mistakes |
| last_wrong_answer | VARCHAR(1024) | Added V10 |
| failed_at | TIMESTAMP | |
| UNIQUE(user_id, question_id) | | Prevents duplicate rows per user/question pair |

#### `items`
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| name | VARCHAR(255) | Unique; used as dedup key in seeder |
| description | VARCHAR(255) | |
| price | INTEGER NOT NULL | |
| currency_type | VARCHAR(50) NOT NULL | Enum: GOLD, CRYSTAL |
| category | VARCHAR(50) NOT NULL | Enum: COSMETIC, CONSUMABLE |
| effect | VARCHAR(50) NOT NULL | Enum: XP_BOOST, GOLD_BOOST, ENERGY_REFILL, SHIELD, NONE |
| slot | VARCHAR(50) NOT NULL | Enum: HEAD, BODY, HANDS, LEGS, WEAPON, AVATAR, NONE |
| rarity | VARCHAR(50) NOT NULL DEFAULT 'COMMON' | Enum: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY |
| asset_url | VARCHAR(255) | Path to image asset |
| attribute_bonus | INTEGER DEFAULT 0 | ATK for weapons, DEF for armor |

#### `inventory`
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| user_id | BIGINT FK → users(id) | |
| item_id | BIGINT FK → items(id) | |
| is_equipped | BOOLEAN DEFAULT FALSE | |
| quantity | INTEGER DEFAULT 1 | Stacks for consumables |
| purchased_at | TIMESTAMP DEFAULT NOW() | |

#### `activity_logs`
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| user_id | BIGINT FK → users(id) | |
| action_type | VARCHAR(50) NOT NULL | Enum: LOGIN, TASK_COMPLETED, BOSS_FAILED, LEVEL_UP, ITEM_BOUGHT |
| details | TEXT | JSON string with context (taskId, multipliers, etc.) |
| timestamp | TIMESTAMP DEFAULT NOW() | |

#### Other tables
- **`course_students`** — Many-to-Many join between `courses` and `users` (enrollment tracking).
- **`submissions`** — Teacher-graded text submissions (task_id, student_id, student_answer, status, teacher_comment, attempt_number).
- **`transaction_history`** — Audit log of every shop purchase (user_id, item_id, cost, currency_used, purchased_at).

### Key Relationships
- `Course` 1 → N `Task` (via `task.course_id`)
- `Task` 1 → N `Question` (`@OneToMany`, `CascadeType.ALL`, `orphanRemoval=true`, EAGER)
- `User` 1 → N `CompletedTask` (user's progress)
- `User` 1 → N `Inventory` (items owned)
- `User` 1 → N `UserQuestionFailure` (mistake tracking)
- `User` M → N `Course` via `CourseStudent`

---

## 3. Core Business Logic & Gamification Engine

### 3.1 Reward Calculation Formula (`SubmissionService.grantRewards`)

**Entry conditions:** Task completed for the first time (`!alreadyCompleted`) AND `isVictory = true`.

```
finalXp   = round(baseXp   × flawlessMultiplier × campfireMultiplier × xpBuffMultiplier   × energyMultiplier)
finalGold = round(baseGold × flawlessMultiplier × campfireMultiplier × goldBuffMultiplier × energyMultiplier)
```

**Multiplier definitions:**

| Multiplier | Variable | Value | Condition |
|---|---|---|---|
| Flawless Streak | `flawlessMultiplier` | `1.0 + min(currentFlawlessStreak × 0.1, 1.0)` | Only if `failedQuestionIds` is empty |
| Flawless Streak (no flawless) | `flawlessMultiplier` | `1.0` | Any failure in run |
| Campfire Level 1 | `campfireMultiplier` | `1.0` | Default |
| Campfire Level 2 | `campfireMultiplier` | `1.05` | |
| Campfire Level 3 | `campfireMultiplier` | `1.15` | |
| Campfire Level 4 | `campfireMultiplier` | `1.50` | |
| Campfire Level 5 | `campfireMultiplier` | `2.0` | Max |
| XP Buff active | `xpBuffMultiplier` | `2.0` | `now.isBefore(xpBuffEndsAt)` |
| XP Buff inactive | `xpBuffMultiplier` | `1.0` | |
| Gold Buff active | `goldBuffMultiplier` | `2.0` | `now.isBefore(goldBuffEndsAt)` |
| Gold Buff inactive | `goldBuffMultiplier` | `1.0` | |
| Energy > 0 | `energyMultiplier` | `1.5` | Rest Energy bonus |
| Energy = 0 | `energyMultiplier` | `1.0` | Depleted state |

**Flawless Streak cap:** `min(streak × 0.1, 1.0)` → max bonus is `+100%` (i.e., `×2.0`), achieved at streak ≥ 10.

**All four multipliers stack multiplicatively**, not additively. A fully buffed flawless run at Campfire 5 with Energy and both item buffs active yields: `2.0 × 2.0 × 2.0 × 2.0 × 1.5 = ×24.0` theoretical maximum.

### 3.2 Level Progression Formula (`User.setCurrentXp`)

```java
this.level = (currentXp / 1000) + 1;
```

- Level 1: 0–999 XP
- Level 2: 1000–1999 XP
- Level N: (N-1) × 1000 XP

This is computed on every XP update directly in the setter — no separate computation needed.

### 3.3 Energy System (`SubmissionService.processRunCompletion`, `UserService.getUserProfileByEmail`)

**Energy deduction on task attempt (first completion only):**
- REGULAR task: `-2 energy`
- BOSS task: `-5 energy`
- Energy minimum: `max(0, currentEnergy - cost)` — cannot go negative.

**Energy regeneration (on profile fetch, `GET /api/users/me`):**
```
energyToAdd = minutesSinceLastTaskCompletion / 6
newEnergy   = min(100, currentEnergy + energyToAdd)
```
Rate: **+1 energy per 6 minutes** of inactivity. Max energy is **100**.

The `lastTaskCompletionDate` is advanced by `energyToAdd × 6` minutes to avoid losing fractional minutes on each poll.

### 3.4 Campfire System (`UserService`, `SubmissionService`)

**Level increase:** On first victory of the day (when `lastLoginDate.toLocalDate() < today`), `campfireLevel = min(campfireLevel + 1, 5)`.

**Level decrease (decay):** On any `GET /api/users/me` call, if `hoursSinceLastLogin >= 48`, then `campfireLevel = max(1, campfireLevel - 1)`.

Campfire level directly maps to `campfireMultiplier` (see table above).

### 3.5 Crystal Economy & Anti-Exploit (`SubmissionService.checkAnswerAndProcessFailure`)

**Crystals are earned on wrong answers ("Productive Failure" mechanic):**

```java
private static final int CRYSTALS_PER_FIRST_FAILURE = 5;
int maxCrystals = 15;
int maxAwardableFailures = maxCrystals / CRYSTALS_PER_FIRST_FAILURE; // = 3
int crystalsToAward = (priorFailuresInTask < maxAwardableFailures) ? 5 : 0;
```

**Rules:**
1. Crystals are only awarded on the **first time** a user fails a specific question (tracked via `UserQuestionFailure` unique constraint on `(user_id, question_id)`).
2. The maximum crystals per task is **15** (3 unique question failures × 5 crystals each).
3. Repeat mistakes on the same question increment `failureCount` but award **0 crystals**.
4. This hard cap prevents farming by re-attempting the same question repeatedly.

**Answer normalization before comparison:**
```java
String normalizedUserAnswer = userAnswer.trim().replaceAll("\\s+", " ").toLowerCase();
```
Each correct answer in the database is normalized the same way before comparison.

### 3.6 Item System (`InventoryService.useConsumable`)

| Item Name (Ukrainian) | Effect Enum | Mechanic |
|---|---|---|
| Бустер досвіду | `XP_BOOST` | Sets `xpBuffEndsAt = now + 30 minutes` |
| Магніт гобліна | `GOLD_BOOST` | Sets `goldBuffEndsAt = now + 60 minutes` |
| Оновлення енергії | `ENERGY_REFILL` | Sets `energy = 100` immediately |
| Руна захисту | `SHIELD` | Sets `hasActiveShield = true`; absorbed on next defeat |

**Shield absorption:** In `processRunCompletion`, if `isVictory == false` AND `hasActiveShield == true`, the defeat is negated and `hasActiveShield` is set to `false`. The student's flawless streak is still reset to 0.

**Consumables stack** (quantity increments in `inventory`). **Cosmetics** are purchased once (`existsByUserAndItem` check). Cosmetics have `effect = NONE`.

**Item Rarity tiers:** COMMON → UNCOMMON → RARE → EPIC → LEGENDARY.

**Weapon ATK bonus tiers:**
- COMMON: +5
- UNCOMMON: +10
- RARE: +20
- EPIC: +35
- LEGENDARY: +50 (Sword of Fate: +55)

**Armor DEF bonus tiers (HEAD slot example):**
- COMMON: +3 / 200g
- UNCOMMON: +6 / 500g
- RARE: +12 / 900g
- EPIC: +20 / 1600g
- LEGENDARY: +30 / 2500g

**All cosmetics are purchased with GOLD. All consumables are purchased with CRYSTALS.**

---

## 4. Data Seeding & Persistence Strategy (`DatabaseSeeder`)

The `DatabaseSeeder` is a Spring `@Configuration` class containing a `CommandLineRunner` `@Bean`. It executes every time the application starts, after Flyway migrations.

### Guard conditions
- Users block: only runs if `userRepository.count() == 0`
- Procedurally-generated courses: only runs if `courseRepository.count() == 0`
- Shop items: always runs; individual items are guarded by `if (repo.existsByName(name)) return;`

### JSON-Based Course Loading: `loadTasksFromJson`

The `networks.json` file (`/data/networks.json`) contains the full "Computer Networks" course. The method uses a **two-pass upsert algorithm** to maintain stable database IDs across restarts:

**Pass 1 — Upsert and save without prerequisites:**
1. Parse all tasks from JSON using Jackson `ObjectMapper`.
2. Build a Map `existingTaskByTitle` from currently saved tasks.
3. For each JSON task:
   - If a task with matching `title` exists in DB → update its fields (theory, rewards, type, boss metadata, questions).
   - If no match → create new entity, `setId(null)` so Hibernate auto-generates.
   - Questions are upserted the same way by matching on `questionText`.
4. Clear `prerequisiteTaskIds` on all tasks (set to empty list).
5. `taskRepository.saveAll(tasksToSave)` — Hibernate assigns real DB IDs.

**Pass 2 — Remap prerequisites:**
1. An `indexedOldIds` list records the order in which tasks were processed (preserving JSON order).
2. After save, `savedTasks[i].getId()` corresponds to `indexedOldIds[i]`.
3. Build Map `oldIdToNewId: Map<Long, Long>`.
4. For each task, translate old JSON prerequisite IDs → new DB IDs.
5. `taskRepository.saveAll(savedTasks)` — writes the remapped prerequisites.

**Effect:** User progress in `completed_tasks` and `user_question_failures` is preserved across restarts because entity IDs remain stable for existing tasks (matched by title).

---

## 5. API Design & Security

### Authentication

The system uses **Spring Security OAuth2 Login with Google** as the sole authentication provider. There is no JWT.

- `SecurityConfig` configures `oauth2Login()` with `CustomOAuth2UserService` as the `userService`.
- On first Google login, `CustomOAuth2UserService.loadUser()` creates a new `User` record with `Role.STUDENT` and default stats.
- Subsequent logins find the existing record by email.
- The authenticated principal is a Spring `OAuth2User` (session-backed). Every controller extracts the user's email via `principal.getAttribute("email")`.
- CSRF protection is **disabled** (`AbstractHttpConfigurer::disable`) to allow cross-origin REST calls from the SPA.
- CORS is configured via a separate `CorsConfig` bean.

**Route protection rules (`SecurityConfig`):**
- `POST /api/auth/**` → `permitAll()` (email/password registration)
- `/v3/api-docs/**`, `/swagger-ui/**` → `permitAll()` (API documentation)
- All other requests → `authenticated()`

**Password encoding:** `BCryptPasswordEncoder` (used for email/password registration path; OAuth users have an empty password string).

### REST API Controllers

| Controller | Base Path | Key Endpoints |
|---|---|---|
| `AuthController` | `/api/auth` | `POST /register`, `GET /me` |
| `UserController` | `/api/users` | `GET /me`, `GET /me/stats`, `PUT /me/privacy`, `POST /me/consume-shield` |
| `CourseController` | `/api/courses` | `GET /` (all courses with progress), `GET /{id}` |
| `TaskController` | `/api/tasks` | `GET /course/{courseId}`, `GET /{taskId}` |
| `ArenaController` | `/api/arena` | `POST /check-answer`, `POST /finish-run` |
| `ItemController` | `/api/items` | `GET /` (shop catalogue), `POST /{id}/buy` |
| `InventoryController` | `/api/inventory` | `GET /`, `POST /{id}/use`, `POST /{id}/equip`, `PUT /equip` |
| `LeaderboardController` | `/api/leaderboard` | `GET /global?sortBy=xp\|gold`, `GET /course/{courseId}` |
| `SubmissionController` | `/api/submissions` | Teacher grading endpoints |

**Key DTO flow for a task run:**

`POST /api/arena/finish-run` receives a `RunCompletionRequest`:
```json
{
  "taskId": 42,
  "isVictory": true,
  "failedQuestionIds": [],
  "attemptsTaken": 1,
  "hintsUsed": false,
  "timeSpentSeconds": 183
}
```

Returns a `RunCompletionResponse`:
```json
{
  "message": "...",
  "baseXp": 100,
  "baseGold": 50,
  "earnedXp": 345,
  "earnedGold": 172,
  "flawlessMultiplier": 1.1,
  "campfireMultiplier": 1.15,
  "xpBuffMultiplier": 1.0,
  "goldBuffMultiplier": 1.0,
  "energyMultiplier": 1.5
}
```

---

## 6. Frontend Architecture

### State Management (Zustand)

A single global store (`src/store/authStore.ts`) holds the authenticated user state:

```typescript
interface AuthState {
  user: User | null;
  isLoading: boolean;
  setUser: (user: User | null) => void;
  setLoading: (status: boolean) => void;
  refreshUser: () => Promise<void>;   // GET /api/users/me
  spendGold: (amount: number) => boolean;
  spendCrystals: (amount: number) => boolean;
}
```

`refreshUser()` is called after every `POST /api/arena/finish-run` to sync XP, Gold, Energy, and level with the backend. `spendGold` / `spendCrystals` perform optimistic local state updates before the backend confirms.

### API Communication Layer

`src/services/api.ts` creates a shared Axios instance:
- `baseURL: 'http://localhost:8080/api'`
- `withCredentials: true` (forwards `JSESSIONID` cookie)
- Global response interceptor: redirects to `/login` on HTTP 401.

Individual service modules (`arenaService`, `courseService`, `taskService`, `inventoryService`, `shopService`, `leaderboardService`) wrap this instance.

### Routing (`src/App.tsx`)

React Router DOM v7 with `createBrowserRouter`:

| Path | Component | Notes |
|---|---|---|
| `/` | `Login` | Google OAuth redirect page |
| `/dashboard` | `StudentDashboard` | XP bar, stats, campfire display |
| `/courses` | `CoursesPage` | Course world map with status filters |
| `/courses/:courseId/foyer` | `FoyerPage` | Task graph (ReactFlow DAG) for one course |
| `/arena/:id` | `ArenaPage` | Live quiz session |
| `/shop` | `ShopPage` | Item shop |
| `/inventory` | `InventoryPage` | Equipped items & consumables |
| `/leaderboard` | `LeaderboardPage` | Global & per-course rankings |

All routes except `/` are wrapped in `ProtectedRoute` which checks `useAuthStore().user`.

### Key Dynamic UI Components

**ArenaPage (`src/pages/student/ArenaPage.tsx`)**
- State machine with `runStatus: 'playing' | 'victory' | 'defeat' | 'timeout' | 'cheated'`
- Boss anti-cheat: `document.visibilitychange` listener triggers defeat if the tab is hidden during a BOSS task.
- Countdown timer (`timeLeft`) decremented via `setInterval` every 1 second; triggers `handleFinishRun(false, 'timeout')` at 0.
- 3-heart system: `hearts` state starts at 3, decremented on wrong answer (unless shield is active).
- On run end: `arenaService.finishRun(payload)` sends `RunCompletionRequest`; the `RunCompletionResponse` populates the **Victory Screen multiplier breakdown** table showing each multiplier only if it is `> 1`.
- Confetti burst (`canvas-confetti`) fires on victory with two-sided angle spread (60° left, 120° right).
- Theory panel: toggleable split-panel layout; toggling to "Show Theory" mid-run sets `hintsUsed = true`.
- Question shuffling is done **server-side** in `TaskService.convertToDto` using `Collections.shuffle()`, limiting to `dynamicQuestionCount` questions per run.

**FoyerPage** — Uses **ReactFlow** to render the task prerequisite DAG as a node-edge graph. Each node shows task title, completion status, and lock state. Edges represent prerequisite relationships.

**CoursesPage** — Displays all courses as cards. Status is computed by `CourseService.getAllCoursesWithProgress`:
- `new`: `completedTasks == 0`
- `in_progress`: `0 < completedTasks < totalTasks`
- `completed`: `completedTasks == totalTasks`

**LeaderboardPage** — Supports sorting by XP (`sortBy=xp`) or Lifetime Gold (`sortBy=gold`). Avatar URLs are resolved from the `inventory` table (equipped `AVATAR` slot item's `asset_url`), falling back to `users.avatar_url`.

**ShopPage / InventoryPage** — Items visually distinguished by `rarity` using CSS color classes (COMMON=grey, UNCOMMON=green, RARE=blue, EPIC=purple, LEGENDARY=orange/gold). Consumables show quantity badges; cosmetics show equipped state.

---

## 7. Flyway Migration History

| Version | File | Purpose |
|---|---|---|
| V1 | `V1__init_schema.sql` | Core tables: users, courses, tasks, questions, inventory, completed_tasks |
| V2 | `V2__add_transaction_history.sql` | Purchase audit log |
| V3 | `V3__add_course_students.sql` | Course enrollment M:N |
| V4 | `V4__sync_courses_columns.sql` | Schema sync |
| V5 | `V5__add_missing_tables.sql` | `submissions`, `user_question_failures` |
| V6 | `V6__add_courses_seed.sql` | Placeholder (cleared for seeder) |
| V7 | `V7__add_rarity_to_items.sql` | `rarity VARCHAR(50) DEFAULT 'COMMON'` on items |
| V8 | `V8__update_old_item_slots.sql` | Migrate BACKGROUND→AVATAR, FRAME→NONE slots |
| V9 | `V9__remove_duplicate_items.sql` | Deduplicate items by name using ROW_NUMBER() |
| V10 | `V10__add_analytics_tracking.sql` | Login streak columns, completed_tasks effort metrics, `activity_logs` table |
| V11 | `V11__clear_old_course_data.sql` | Truncate course/task data for fresh JSON seeding |

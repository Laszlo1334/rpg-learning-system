package com.education.rpg.rpglearningbackend.config;

import com.education.rpg.rpglearningbackend.model.*;
import com.education.rpg.rpglearningbackend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Configuration
public class DatabaseSeeder {

    @Bean
    public CommandLineRunner seedDatabase(
            UserRepository userRepository,
            CourseRepository courseRepository,
            TaskRepository taskRepository,
            ItemRepository itemRepository,
            UserQuestionFailureRepository userQuestionFailureRepository,
            CompletedTaskRepository completedTaskRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("🌱 Запуск модульної перевірки бази даних...");

            // === Users ===
            if (userRepository.count() == 0) {
                System.out.println("Створення користувачів...");
                generateUsers(userRepository, passwordEncoder);
            }

            // === Courses & Tasks ===
            if (courseRepository.count() == 0) {
                System.out.println("Створення курсів...");
                User teacher = userRepository.findByEmail("teacher@rpg.com").orElseThrow();
                List<Course> courses = new ArrayList<>();
                courses.add(createCourse("Основи магії (Java Spring Boot)", "Вступний курс.", "MAGIC101", teacher));
                courses.add(createCourse("Некромантія Баз Даних (SQL)", "JOIN-и та магія.", "SQL666", teacher));
                courses.add(createCourse("Алгоритмічні закляття", "Оптимізація коду.", "ALGO99", teacher));
                courseRepository.saveAll(courses);

                generateBranchedCourse(courses.get(0), 30, taskRepository);
                generateLinearCourse(courses.get(1), 25, "SQL Запит", taskRepository);
                generateBranchedCourse(courses.get(2), 20, taskRepository);
            }

            // Reuse the networking course if it already exists, otherwise create and persist it.
            Course networkCourse = courseRepository.findAll().stream()
                    .filter(c -> "NET101".equals(c.getAccessCode()))
                    .findFirst()
                    .orElseGet(() -> {
                        System.out.println("✨ Додавання нового курсу: Комп'ютерні мережі...");
                        User teacher = userRepository.findByEmail("teacher@rpg.com").orElseThrow();
                        Course newCourse = createCourse("Комп'ютерні мережі", "Основи маршрутизації та OSI.", "NET101", teacher);
                        return courseRepository.save(newCourse);
                    });

            loadTasksFromJson(networkCourse, taskRepository);

            // === Shop Items === (runs every startup; helpers skip existing items to prevent duplicates)
            System.out.println("Перевірка та оновлення асортименту магазину...");
            generateShopItems(itemRepository);

            System.out.println("✅ База даних успішно синхронізована!");
        };
    }

    // ==========================================
    // Procedural generation helpers
    // ==========================================

    private void generateLinearCourse(Course course, int taskCount, String prefix, TaskRepository taskRepository) {
        Task prevTask = null;
        for (int i = 1; i <= taskCount; i++) {
            boolean isBoss = (i % 5 == 0); // Every 5th task is a Boss task
            Task task = new Task();
            task.setTitle(prefix + " " + i);
            task.setTheoryContent(isBoss ? "Приготуйтесь до перевірки знань!" : "Детальна теорія для завдання " + i);
            task.setRewardXp(isBoss ? 200 : 50);
            task.setRewardGold(isBoss ? 100 : 20);
            task.setCourse(course);
            task.setBranchName("Основний Шлях");
            task.setOrderIndex(i);
            task.setType(isBoss ? Task.TaskType.BOSS : Task.TaskType.REGULAR);
            if (isBoss) {
                task.setBossName("Вартовий Етапу " + (i / 5));
                task.setBossAvatarUrl("/assets/bosses/golem.png");
                task.setTimeLimitSeconds(120);
            }
            task.setDynamicQuestionCount(isBoss ? 3 : 1);
            if (prevTask != null)
                task.setPrerequisiteTaskIds(List.of(prevTask.getId()));

            Question q = createTestQuestion(task, "Тестове питання для " + task.getTitle(),
                    List.of("Варіант 1", "Варіант 2", "Варіант 3"), "Варіант 1");
            task.setQuestions(List.of(q));
            taskRepository.save(task);
            prevTask = task;
        }
    }

    private void generateBranchedCourse(Course course, int totalTasks, TaskRepository taskRepository) {

        Task root = new Task();
        root.setTitle("Вступ до " + course.getTitle());
        root.setTheoryContent("Основи основ.");
        root.setRewardXp(50);
        root.setRewardGold(20);
        root.setCourse(course);
        root.setBranchName("Старт");
        root.setOrderIndex(1);
        root.setType(Task.TaskType.REGULAR);
        root.setQuestions(List.of(createTestQuestion(root, "Готові?", List.of("Так", "Ні"), "Так")));
        taskRepository.save(root);

        Task currentDivergence = root;
        int order = 2;
        int remaining = totalTasks - 1;

        while (remaining > 0) {
            int leftLen = Math.min(3, remaining / 2 + 1);
            int rightLen = Math.min(2, remaining / 2);
            if (leftLen == 0)
                break;

            Task leftLast = currentDivergence;
            for (int i = 0; i < leftLen; i++) {
                Task t = new Task();
                t.setTitle("Гілка Практики " + order);
                t.setCourse(course);
                t.setBranchName("Практика");
                t.setOrderIndex(order++);
                t.setType(Task.TaskType.REGULAR);
                t.setRewardXp(60);
                t.setRewardGold(20);
                t.setPrerequisiteTaskIds(List.of(leftLast.getId()));
                t.setQuestions(List.of(createTestQuestion(t, "Питання", List.of("1", "2"), "1")));
                taskRepository.save(t);
                leftLast = t;
                remaining--;
            }

            Task rightLast = currentDivergence;
            for (int i = 0; i < rightLen; i++) {
                Task t = new Task();
                t.setTitle("Гілка Теорії " + order);
                t.setCourse(course);
                t.setBranchName("Теорія");
                t.setOrderIndex(order++);
                t.setType(Task.TaskType.REGULAR);
                t.setRewardXp(60);
                t.setRewardGold(20);
                t.setPrerequisiteTaskIds(List.of(rightLast.getId()));
                t.setQuestions(List.of(createTestQuestion(t, "Питання", List.of("1", "2"), "1")));
                taskRepository.save(t);
                rightLast = t;
                remaining--;
            }

            // Merge Boss: unlocked only after completing both branches
            if (remaining > 0) {
                Task boss = new Task();
                boss.setTitle("Бос Злиття " + order);
                boss.setCourse(course);
                boss.setBranchName("Арена");
                boss.setOrderIndex(order++);
                boss.setType(Task.TaskType.BOSS);
                boss.setRewardXp(300);
                boss.setRewardGold(150);
                boss.setBossName("Хранитель Гілок");
                boss.setBossAvatarUrl("/assets/bosses/golem.png");
                boss.setPrerequisiteTaskIds(List.of(leftLast.getId(), rightLast.getId()));
                boss.setQuestions(List.of(createTestQuestion(boss, "Тест", List.of("Так", "Ні"), "Так")));
                taskRepository.save(boss);
                currentDivergence = boss;
                remaining--;
            }
        }
    }

    private Course createCourse(String title, String desc, String code, User author) {
        Course c = new Course();
        c.setTitle(title);
        c.setDescription(desc);
        c.setAccessCode(code);
        c.setAuthor(author);
        return c;
    }

    private Question createTestQuestion(Task task, String text, List<String> options, String correctAnswer) {
        Question q = new Question();
        q.setTask(task);
        q.setQuestionText(text);
        q.setType(Question.QuestionType.TEST);
        q.setOptions(options);
        q.setCorrectAnswers(List.of(correctAnswer));
        q.setExplanation("Уважно перегляньте теорію.");
        return q;
    }

    private void generateUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        User teacher = new User();
        teacher.setUsername("Гендальф (Вчитель)");
        teacher.setEmail("teacher@rpg.com");
        teacher.setPassword(passwordEncoder.encode("12345678"));
        teacher.setRole(Role.TEACHER);
        teacher.setLevel(50);
        userRepository.save(teacher);

        User student = new User();
        student.setUsername("Фродо Багінс");
        student.setEmail("student@rpg.com");
        student.setPassword(passwordEncoder.encode("12345678"));
        student.setRole(Role.STUDENT);
        student.setLevel(2);
        student.setCampfireLevel(1);
        student.setEnergy(100);
        student.setLastLoginDate(LocalDateTime.now());
        userRepository.save(student);

        // Fictional students to populate the leaderboard
        String[] heroNames = {
                "Сем Гемджі", "Піппін Тук", "Меррі Брендібак",
                "Арагорн", "Леголас", "Гімлі",
                "Боромір", "Фарамір", "Еовін",
                "Галадріель", "Елронд", "Саруман"
        };
        // Each avatar maps to an existing file under /assets/avatars/
        String[] heroAvatars = {
                "/assets/avatars/boy.png",
                "/assets/avatars/man.png",
                "/assets/avatars/man_2.png",
                "/assets/avatars/viking.png",
                "/assets/avatars/goblin_archer.png",
                "/assets/avatars/ogr_warrior.png",
                "/assets/avatars/knight.png",
                "/assets/avatars/man_3.png",
                "/assets/avatars/lady.png",
                "/assets/avatars/lady_ginger.png",
                "/assets/avatars/elder.png",
                "/assets/avatars/mrmustage.png"
        };
        int[] privateIndexes = { 3, 7 };
        Random seedRandom = new Random(42);

        for (int i = 0; i < heroNames.length; i++) {
            int xp = 100 + seedRandom.nextInt(4901);
            int level = (xp / 1000) + 1;
            int gold = 50 + seedRandom.nextInt(751);
            boolean isPrivate = (i == privateIndexes[0] || i == privateIndexes[1]);

            User hero = new User();
            hero.setUsername(heroNames[i]);
            hero.setEmail("hero" + i + "@rpg.com");
            hero.setPassword(passwordEncoder.encode("12345678"));
            hero.setRole(Role.STUDENT);
            hero.setLevel(level);
            hero.setCurrentXp(xp);
            hero.setGold(gold);
            hero.setCrystals(seedRandom.nextInt(100));
            hero.setCampfireLevel(1 + seedRandom.nextInt(5));
            hero.setEnergy(50 + seedRandom.nextInt(51));
            hero.setIsPublicProfile(!isPrivate);
            hero.setLastLoginDate(LocalDateTime.now().minusDays(seedRandom.nextInt(7)));
            hero.setTotalTasksCompleted(seedRandom.nextInt(15));
            hero.setAvatarUrl(heroAvatars[i]);
            userRepository.save(hero);
        }
    }

    private void generateShopItems(ItemRepository itemRepository) {
        // --- Consumables ---
        createConsumable(itemRepository, "Бустер досвіду", "+50% XP на 30 хвилин.", 200, Item.CurrencyType.CRYSTAL,
                Item.EffectType.XP_BOOST, "/assets/items/potion_wisdom.png");
        createConsumable(itemRepository, "Магніт гобліна", "Подвійне золото на 60 хвилин.", 150,
                Item.CurrencyType.CRYSTAL, Item.EffectType.GOLD_BOOST, "/assets/items/goblin_magnet.png");
        createConsumable(itemRepository, "Оновлення енергії", "Миттєво відновлює 100 Енергії.", 400,
                Item.CurrencyType.CRYSTAL, Item.EffectType.ENERGY_REFILL, "/assets/items/elixir_vigor.png");
        createConsumable(itemRepository, "Руна захисту", "Поглинає одну поразку.", 100, Item.CurrencyType.CRYSTAL,
                Item.EffectType.SHIELD, "/assets/items/rune_protection.png");

        // --- Avatars — all paths correspond to real files under /assets/avatars/ ---
        createEquipment(itemRepository, "Базовий Аватар",   0,    Item.ItemSlot.AVATAR, Item.ItemRarity.COMMON, "/assets/default_avatar.png");
        createEquipment(itemRepository, "Лицар",         300,  Item.ItemSlot.AVATAR, Item.ItemRarity.COMMON, "/assets/avatars/knight.png");
        createEquipment(itemRepository, "Старець",        500,  Item.ItemSlot.AVATAR, Item.ItemRarity.COMMON, "/assets/avatars/elder.png");
        createEquipment(itemRepository, "Вікінг",         700,  Item.ItemSlot.AVATAR, Item.ItemRarity.RARE,   "/assets/avatars/viking.png");
        createEquipment(itemRepository, "Вікінг-Бос",     1000, Item.ItemSlot.AVATAR, Item.ItemRarity.EPIC,   "/assets/avatars/viking_boss.png");
        createEquipment(itemRepository, "Гоблін-Лучник",  600,  Item.ItemSlot.AVATAR, Item.ItemRarity.RARE,   "/assets/avatars/goblin_archer.png");
        createEquipment(itemRepository, "Гоблін-Воїн",    600,  Item.ItemSlot.AVATAR, Item.ItemRarity.RARE,   "/assets/avatars/goblin_warrior.png");
        createEquipment(itemRepository, "Огр-Воїн",       800,  Item.ItemSlot.AVATAR, Item.ItemRarity.EPIC,   "/assets/avatars/ogr_warrior.png");
        createEquipment(itemRepository, "Дама",           500,  Item.ItemSlot.AVATAR, Item.ItemRarity.COMMON, "/assets/avatars/lady.png");
        createEquipment(itemRepository, "Рудоволоса",     700,  Item.ItemSlot.AVATAR, Item.ItemRarity.RARE,   "/assets/avatars/lady_ginger.png");
        createEquipment(itemRepository, "Голем",          1200, Item.ItemSlot.AVATAR, Item.ItemRarity.EPIC,   "/assets/avatars/golem.png");
        createEquipment(itemRepository, "Кракен",         1500, Item.ItemSlot.AVATAR, Item.ItemRarity.EPIC,   "/assets/avatars/kraken.png");
        createEquipment(itemRepository, "Білий Вовк",     1000, Item.ItemSlot.AVATAR, Item.ItemRarity.EPIC,   "/assets/avatars/white_wolf.png");
        createEquipment(itemRepository, "Вовк",           800,  Item.ItemSlot.AVATAR, Item.ItemRarity.RARE,   "/assets/avatars/wolf.png");
        createEquipment(itemRepository, "Ведмідь",        900,  Item.ItemSlot.AVATAR, Item.ItemRarity.RARE,   "/assets/avatars/bear.png");
        createEquipment(itemRepository, "Носоріг",        900,  Item.ItemSlot.AVATAR, Item.ItemRarity.RARE,   "/assets/avatars/rhino.png");
        createEquipment(itemRepository, "Кабан",          600,  Item.ItemSlot.AVATAR, Item.ItemRarity.COMMON, "/assets/avatars/boar.png");
        createEquipment(itemRepository, "Злий Кабан",     1000, Item.ItemSlot.AVATAR, Item.ItemRarity.EPIC,   "/assets/avatars/evil_hog.png");
        createEquipment(itemRepository, "Король Жаб",     1500, Item.ItemSlot.AVATAR, Item.ItemRarity.LEGENDARY, "/assets/avatars/frog_king.png");
        createEquipment(itemRepository, "Супер-Жаба",     1000, Item.ItemSlot.AVATAR, Item.ItemRarity.EPIC,   "/assets/avatars/super_frog.png");
        createEquipment(itemRepository, "Слиз",           400,  Item.ItemSlot.AVATAR, Item.ItemRarity.COMMON, "/assets/avatars/slime.png");
        createEquipment(itemRepository, "Слиз-Змій",      700,  Item.ItemSlot.AVATAR, Item.ItemRarity.RARE,   "/assets/avatars/slime_snake.png");
        createEquipment(itemRepository, "Павук",          800,  Item.ItemSlot.AVATAR, Item.ItemRarity.RARE,   "/assets/avatars/spider.png");
        createEquipment(itemRepository, "Крук",           900,  Item.ItemSlot.AVATAR, Item.ItemRarity.RARE,   "/assets/avatars/raven.png");
        createEquipment(itemRepository, "Мімік",          2000, Item.ItemSlot.AVATAR, Item.ItemRarity.LEGENDARY, "/assets/avatars/mimic.png");
        createEquipment(itemRepository, "Барас",          2000, Item.ItemSlot.AVATAR, Item.ItemRarity.LEGENDARY, "/assets/avatars/barathrum.png");
        createEquipment(itemRepository, "Золота Куля",    2500, Item.ItemSlot.AVATAR, Item.ItemRarity.LEGENDARY, "/assets/avatars/golden_ball.png");
        createEquipment(itemRepository, "Черепаха",       500,  Item.ItemSlot.AVATAR, Item.ItemRarity.COMMON, "/assets/avatars/turtle.png");
        createEquipment(itemRepository, "Синій Крок",     700,  Item.ItemSlot.AVATAR, Item.ItemRarity.RARE,   "/assets/avatars/blue_croc.png");
        createEquipment(itemRepository, "Мураха",         300,  Item.ItemSlot.AVATAR, Item.ItemRarity.COMMON,   "/assets/avatars/ant.png");
        createEquipment(itemRepository, "Кажан",          400,  Item.ItemSlot.AVATAR, Item.ItemRarity.COMMON,   "/assets/avatars/bat.png");
        // --- UNCOMMON avatars (10 previously unregistered files from /assets/avatars/) ---
        createEquipment(itemRepository, "Хлопчик",        400,  Item.ItemSlot.AVATAR, Item.ItemRarity.UNCOMMON, "/assets/avatars/boy.png");
        createEquipment(itemRepository, "Чоловік",        400,  Item.ItemSlot.AVATAR, Item.ItemRarity.UNCOMMON, "/assets/avatars/man.png");
        createEquipment(itemRepository, "Чоловік II",     450,  Item.ItemSlot.AVATAR, Item.ItemRarity.UNCOMMON, "/assets/avatars/man_2.png");
        createEquipment(itemRepository, "Чоловік III",    450,  Item.ItemSlot.AVATAR, Item.ItemRarity.UNCOMMON, "/assets/avatars/man_3.png");
        createEquipment(itemRepository, "Жаба",           450,  Item.ItemSlot.AVATAR, Item.ItemRarity.UNCOMMON, "/assets/avatars/frog.png");
        createEquipment(itemRepository, "Жаба II",        500,  Item.ItemSlot.AVATAR, Item.ItemRarity.UNCOMMON, "/assets/avatars/frog_2.png");
        createEquipment(itemRepository, "Дика Свиня",     500,  Item.ItemSlot.AVATAR, Item.ItemRarity.UNCOMMON, "/assets/avatars/hog.png");
        createEquipment(itemRepository, "Торговець",      550,  Item.ItemSlot.AVATAR, Item.ItemRarity.UNCOMMON, "/assets/avatars/seller.png");
        createEquipment(itemRepository, "Морж",           550,  Item.ItemSlot.AVATAR, Item.ItemRarity.UNCOMMON, "/assets/avatars/swap.png");
        createEquipment(itemRepository, "Чарівник",       550,  Item.ItemSlot.AVATAR, Item.ItemRarity.UNCOMMON, "/assets/avatars/mrmustage.png");


        // --- Head (HEAD) — 21 helmets; tiers: COMMON(+3 DEF,200g) UNCOMMON(+6,500g) RARE(+12,900g) EPIC(+20,1600g) LEGENDARY(+30,2500g) ---
        createArmor(itemRepository, "Шолом Новачка",        200,  Item.ItemSlot.HEAD, Item.ItemRarity.COMMON,    "/assets/cosmetics/Head/head1.png",   3);
        createArmor(itemRepository, "Залізний Шолом",       200,  Item.ItemSlot.HEAD, Item.ItemRarity.COMMON,    "/assets/cosmetics/Head/head2.png",   3);
        createArmor(itemRepository, "Бойовий Шолом",        200,  Item.ItemSlot.HEAD, Item.ItemRarity.COMMON,    "/assets/cosmetics/Head/head3.png",   3);
        createArmor(itemRepository, "Шолом Стражника",      200,  Item.ItemSlot.HEAD, Item.ItemRarity.COMMON,    "/assets/cosmetics/Head/head4.png",   3);
        createArmor(itemRepository, "Шолом Лісника",        500,  Item.ItemSlot.HEAD, Item.ItemRarity.UNCOMMON,  "/assets/cosmetics/Head/head5.png",   6);
        createArmor(itemRepository, "Шолом Вартового",      500,  Item.ItemSlot.HEAD, Item.ItemRarity.UNCOMMON,  "/assets/cosmetics/Head/head6.png",   6);
        createArmor(itemRepository, "Шолом Мисливця",       500,  Item.ItemSlot.HEAD, Item.ItemRarity.UNCOMMON,  "/assets/cosmetics/Head/head7.png",   6);
        createArmor(itemRepository, "Шолом Рейнджера",      500,  Item.ItemSlot.HEAD, Item.ItemRarity.UNCOMMON,  "/assets/cosmetics/Head/head8.png",   6);
        createArmor(itemRepository, "Шолом Лицаря",         900,  Item.ItemSlot.HEAD, Item.ItemRarity.RARE,      "/assets/cosmetics/Head/head9.png",  12);
        createArmor(itemRepository, "Шолом Воїна",          900,  Item.ItemSlot.HEAD, Item.ItemRarity.RARE,      "/assets/cosmetics/Head/head10.png", 12);
        createArmor(itemRepository, "Сталевий Шолом",       900,  Item.ItemSlot.HEAD, Item.ItemRarity.RARE,      "/assets/cosmetics/Head/head11.png", 12);
        createArmor(itemRepository, "Шолом Капітана",       900,  Item.ItemSlot.HEAD, Item.ItemRarity.RARE,      "/assets/cosmetics/Head/head12.png", 12);
        createArmor(itemRepository, "Шолом Берсерка",       900,  Item.ItemSlot.HEAD, Item.ItemRarity.RARE,      "/assets/cosmetics/Head/head13.png", 12);
        createArmor(itemRepository, "Шолом Командира",     1600,  Item.ItemSlot.HEAD, Item.ItemRarity.EPIC,      "/assets/cosmetics/Head/head14.png", 20);
        createArmor(itemRepository, "Епічний Шолом",       1600,  Item.ItemSlot.HEAD, Item.ItemRarity.EPIC,      "/assets/cosmetics/Head/head15.png", 20);
        createArmor(itemRepository, "Шолом Паладина",      1600,  Item.ItemSlot.HEAD, Item.ItemRarity.EPIC,      "/assets/cosmetics/Head/head16.png", 20);
        createArmor(itemRepository, "Шолом Темного Лорда", 1600,  Item.ItemSlot.HEAD, Item.ItemRarity.EPIC,      "/assets/cosmetics/Head/head17.png", 20);
        createArmor(itemRepository, "Шолом Короля",        2500,  Item.ItemSlot.HEAD, Item.ItemRarity.LEGENDARY, "/assets/cosmetics/Head/head18.png", 30);
        createArmor(itemRepository, "Шолом Легенди",       2500,  Item.ItemSlot.HEAD, Item.ItemRarity.LEGENDARY, "/assets/cosmetics/Head/head19.png", 30);
        createArmor(itemRepository, "Шолом Богатиря",      2500,  Item.ItemSlot.HEAD, Item.ItemRarity.LEGENDARY, "/assets/cosmetics/Head/head20.png", 30);
        createArmor(itemRepository, "Корона Воїна",        3000,  Item.ItemSlot.HEAD, Item.ItemRarity.LEGENDARY, "/assets/cosmetics/Head/head21.png", 35);

        // --- Body (BODY) — 20 chest pieces; tiers: COMMON(+5 DEF,300g) UNCOMMON(+10,700g) RARE(+18,1200g) EPIC(+28,2000g) LEGENDARY(+40,3200g) ---
        createArmor(itemRepository, "Мантія Учня",            300,  Item.ItemSlot.BODY, Item.ItemRarity.COMMON,    "/assets/cosmetics/Chest/chest1.png",   5);
        createArmor(itemRepository, "Шкіряна Броня",          300,  Item.ItemSlot.BODY, Item.ItemRarity.COMMON,    "/assets/cosmetics/Chest/chest2.png",   5);
        createArmor(itemRepository, "Броня Рекрута",          300,  Item.ItemSlot.BODY, Item.ItemRarity.COMMON,    "/assets/cosmetics/Chest/chest3.png",   5);
        createArmor(itemRepository, "Броня Стражника",        300,  Item.ItemSlot.BODY, Item.ItemRarity.COMMON,    "/assets/cosmetics/Chest/chest4.png",   5);
        createArmor(itemRepository, "Броня Мисливця",         700,  Item.ItemSlot.BODY, Item.ItemRarity.UNCOMMON,  "/assets/cosmetics/Chest/chest5.png",  10);
        createArmor(itemRepository, "Кольчуга Воїна",         700,  Item.ItemSlot.BODY, Item.ItemRarity.UNCOMMON,  "/assets/cosmetics/Chest/chest6.png",  10);
        createArmor(itemRepository, "Броня Рейнджера",        700,  Item.ItemSlot.BODY, Item.ItemRarity.UNCOMMON,  "/assets/cosmetics/Chest/chest7.png",  10);
        createArmor(itemRepository, "Броня Вартового",        700,  Item.ItemSlot.BODY, Item.ItemRarity.UNCOMMON,  "/assets/cosmetics/Chest/chest8.png",  10);
        createArmor(itemRepository, "Сталева Броня",         1200,  Item.ItemSlot.BODY, Item.ItemRarity.RARE,      "/assets/cosmetics/Chest/chest9.png",  18);
        createArmor(itemRepository, "Лицарська Броня",       1200,  Item.ItemSlot.BODY, Item.ItemRarity.RARE,      "/assets/cosmetics/Chest/chest10.png", 18);
        createArmor(itemRepository, "Броня Командира",       1200,  Item.ItemSlot.BODY, Item.ItemRarity.RARE,      "/assets/cosmetics/Chest/chest11.png", 18);
        createArmor(itemRepository, "Броня Берсерка",        1200,  Item.ItemSlot.BODY, Item.ItemRarity.RARE,      "/assets/cosmetics/Chest/chest12.png", 18);
        createArmor(itemRepository, "Броня Капітана",        1200,  Item.ItemSlot.BODY, Item.ItemRarity.RARE,      "/assets/cosmetics/Chest/chest13.png", 18);
        createArmor(itemRepository, "Епічна Броня",          2000,  Item.ItemSlot.BODY, Item.ItemRarity.EPIC,      "/assets/cosmetics/Chest/chest14.png", 28);
        createArmor(itemRepository, "Броня Паладина",        2000,  Item.ItemSlot.BODY, Item.ItemRarity.EPIC,      "/assets/cosmetics/Chest/chest15.png", 28);
        createArmor(itemRepository, "Броня Темного Лорда",   2000,  Item.ItemSlot.BODY, Item.ItemRarity.EPIC,      "/assets/cosmetics/Chest/chest16.png", 28);
        createArmor(itemRepository, "Броня Архімага",        2000,  Item.ItemSlot.BODY, Item.ItemRarity.EPIC,      "/assets/cosmetics/Chest/chest17.png", 28);
        createArmor(itemRepository, "Броня Короля",          3200,  Item.ItemSlot.BODY, Item.ItemRarity.LEGENDARY, "/assets/cosmetics/Chest/chest18.png", 40);
        createArmor(itemRepository, "Броня Легенди",         3200,  Item.ItemSlot.BODY, Item.ItemRarity.LEGENDARY, "/assets/cosmetics/Chest/chest19.png", 40);
        createArmor(itemRepository, "Броня Богатиря",        3200,  Item.ItemSlot.BODY, Item.ItemRarity.LEGENDARY, "/assets/cosmetics/Chest/chest20.png", 40);

        // --- Hands (HANDS) — 20 gloves; tiers: COMMON(+2 DEF,150g) UNCOMMON(+5,400g) RARE(+10,800g) EPIC(+16,1400g) LEGENDARY(+25,2200g) ---
        createArmor(itemRepository, "Шкіряні Рукавиці",       150,  Item.ItemSlot.HANDS, Item.ItemRarity.COMMON,    "/assets/cosmetics/Hands/hands1.png",   2);
        createArmor(itemRepository, "Рукавиці Учня",          150,  Item.ItemSlot.HANDS, Item.ItemRarity.COMMON,    "/assets/cosmetics/Hands/hands2.png",   2);
        createArmor(itemRepository, "Рукавиці Рекрута",       150,  Item.ItemSlot.HANDS, Item.ItemRarity.COMMON,    "/assets/cosmetics/Hands/hands3.png",   2);
        createArmor(itemRepository, "Рукавиці Стражника",     150,  Item.ItemSlot.HANDS, Item.ItemRarity.COMMON,    "/assets/cosmetics/Hands/hands4.png",   2);
        createArmor(itemRepository, "Рукавиці Мисливця",      400,  Item.ItemSlot.HANDS, Item.ItemRarity.UNCOMMON,  "/assets/cosmetics/Hands/hands5.png",   5);
        createArmor(itemRepository, "Рукавиці Вартового",     400,  Item.ItemSlot.HANDS, Item.ItemRarity.UNCOMMON,  "/assets/cosmetics/Hands/hands6.png",   5);
        createArmor(itemRepository, "Рукавиці Рейнджера",     400,  Item.ItemSlot.HANDS, Item.ItemRarity.UNCOMMON,  "/assets/cosmetics/Hands/hands7.png",   5);
        createArmor(itemRepository, "Залізні Рукавиці",       400,  Item.ItemSlot.HANDS, Item.ItemRarity.UNCOMMON,  "/assets/cosmetics/Hands/hands8.png",   5);
        createArmor(itemRepository, "Сталеві Рукавиці",       800,  Item.ItemSlot.HANDS, Item.ItemRarity.RARE,      "/assets/cosmetics/Hands/hands9.png",  10);
        createArmor(itemRepository, "Рукавиці Воїна",         800,  Item.ItemSlot.HANDS, Item.ItemRarity.RARE,      "/assets/cosmetics/Hands/hands10.png", 10);
        createArmor(itemRepository, "Рукавиці Лицаря",        800,  Item.ItemSlot.HANDS, Item.ItemRarity.RARE,      "/assets/cosmetics/Hands/hands11.png", 10);
        createArmor(itemRepository, "Рукавиці Командира",     800,  Item.ItemSlot.HANDS, Item.ItemRarity.RARE,      "/assets/cosmetics/Hands/hands12.png", 10);
        createArmor(itemRepository, "Рукавиці Берсерка",      800,  Item.ItemSlot.HANDS, Item.ItemRarity.RARE,      "/assets/cosmetics/Hands/hands13.png", 10);
        createArmor(itemRepository, "Рукавиці Паладина",     1400,  Item.ItemSlot.HANDS, Item.ItemRarity.EPIC,      "/assets/cosmetics/Hands/hands14.png", 16);
        createArmor(itemRepository, "Рукавиці Архімага",     1400,  Item.ItemSlot.HANDS, Item.ItemRarity.EPIC,      "/assets/cosmetics/Hands/hands15.png", 16);
        createArmor(itemRepository, "Рукавиці Темного Лорда",1400,  Item.ItemSlot.HANDS, Item.ItemRarity.EPIC,      "/assets/cosmetics/Hands/hands16.png", 16);
        createArmor(itemRepository, "Епічні Рукавиці",       1400,  Item.ItemSlot.HANDS, Item.ItemRarity.EPIC,      "/assets/cosmetics/Hands/hands17.png", 16);
        createArmor(itemRepository, "Рукавиці Короля",       2200,  Item.ItemSlot.HANDS, Item.ItemRarity.LEGENDARY, "/assets/cosmetics/Hands/hands18.png", 25);
        createArmor(itemRepository, "Рукавиці Легенди",      2200,  Item.ItemSlot.HANDS, Item.ItemRarity.LEGENDARY, "/assets/cosmetics/Hands/hands19.png", 25);
        createArmor(itemRepository, "Рукавиці Богатиря",     2200,  Item.ItemSlot.HANDS, Item.ItemRarity.LEGENDARY, "/assets/cosmetics/Hands/hands20.png", 25);

        // --- Legs (LEGS) — 20 boots; tiers: COMMON(+2 DEF,150g) UNCOMMON(+5,400g) RARE(+10,800g) EPIC(+16,1400g) LEGENDARY(+25,2200g) ---
        createArmor(itemRepository, "Чоботи Мандрівника",     150,  Item.ItemSlot.LEGS, Item.ItemRarity.COMMON,    "/assets/cosmetics/Legs/legs1.png",   2);
        createArmor(itemRepository, "Чоботи Рекрута",         150,  Item.ItemSlot.LEGS, Item.ItemRarity.COMMON,    "/assets/cosmetics/Legs/legs2.png",   2);
        createArmor(itemRepository, "Шкіряні Чоботи",         150,  Item.ItemSlot.LEGS, Item.ItemRarity.COMMON,    "/assets/cosmetics/Legs/legs3.png",   2);
        createArmor(itemRepository, "Чоботи Стражника",       150,  Item.ItemSlot.LEGS, Item.ItemRarity.COMMON,    "/assets/cosmetics/Legs/legs4.png",   2);
        createArmor(itemRepository, "Чоботи Мисливця",        400,  Item.ItemSlot.LEGS, Item.ItemRarity.UNCOMMON,  "/assets/cosmetics/Legs/legs5.png",   5);
        createArmor(itemRepository, "Чоботи Вартового",       400,  Item.ItemSlot.LEGS, Item.ItemRarity.UNCOMMON,  "/assets/cosmetics/Legs/legs6.png",   5);
        createArmor(itemRepository, "Чоботи Рейнджера",       400,  Item.ItemSlot.LEGS, Item.ItemRarity.UNCOMMON,  "/assets/cosmetics/Legs/legs7.png",   5);
        createArmor(itemRepository, "Залізні Чоботи",         400,  Item.ItemSlot.LEGS, Item.ItemRarity.UNCOMMON,  "/assets/cosmetics/Legs/legs8.png",   5);
        createArmor(itemRepository, "Сталеві Чоботи",         800,  Item.ItemSlot.LEGS, Item.ItemRarity.RARE,      "/assets/cosmetics/Legs/legs9.png",  10);
        createArmor(itemRepository, "Чоботи Воїна",           800,  Item.ItemSlot.LEGS, Item.ItemRarity.RARE,      "/assets/cosmetics/Legs/legs10.png", 10);
        createArmor(itemRepository, "Чоботи Лицаря",          800,  Item.ItemSlot.LEGS, Item.ItemRarity.RARE,      "/assets/cosmetics/Legs/legs11.png", 10);
        createArmor(itemRepository, "Чоботи Командира",       800,  Item.ItemSlot.LEGS, Item.ItemRarity.RARE,      "/assets/cosmetics/Legs/legs12.png", 10);
        createArmor(itemRepository, "Чоботи Берсерка",        800,  Item.ItemSlot.LEGS, Item.ItemRarity.RARE,      "/assets/cosmetics/Legs/legs13.png", 10);
        createArmor(itemRepository, "Чоботи Паладина",       1400,  Item.ItemSlot.LEGS, Item.ItemRarity.EPIC,      "/assets/cosmetics/Legs/legs14.png", 16);
        createArmor(itemRepository, "Чоботи Архімага",       1400,  Item.ItemSlot.LEGS, Item.ItemRarity.EPIC,      "/assets/cosmetics/Legs/legs15.png", 16);
        createArmor(itemRepository, "Чоботи Темного Лорда",  1400,  Item.ItemSlot.LEGS, Item.ItemRarity.EPIC,      "/assets/cosmetics/Legs/legs16.png", 16);
        createArmor(itemRepository, "Епічні Чоботи",         1400,  Item.ItemSlot.LEGS, Item.ItemRarity.EPIC,      "/assets/cosmetics/Legs/legs17.png", 16);
        createArmor(itemRepository, "Чоботи Короля",         2200,  Item.ItemSlot.LEGS, Item.ItemRarity.LEGENDARY, "/assets/cosmetics/Legs/legs18.png", 25);
        createArmor(itemRepository, "Чоботи Легенди",        2200,  Item.ItemSlot.LEGS, Item.ItemRarity.LEGENDARY, "/assets/cosmetics/Legs/legs19.png", 25);
        createArmor(itemRepository, "Чоботи Богатиря",       2200,  Item.ItemSlot.LEGS, Item.ItemRarity.LEGENDARY, "/assets/cosmetics/Legs/legs20.png", 25);

        // --- Weapons (WEAPON) — all 57 files from /assets/weapons/ ---
        // Suffix _1=COMMON(+5, 500g)  _2=UNCOMMON(+10, 1000g)  _3=RARE(+20, 1800g)
        //        _4=EPIC(+35, 3000g)  _5=LEGENDARY(+50, 4500g)

        // Axes
        createWeapon(itemRepository, "Сокира Новачка",   500,  Item.ItemRarity.COMMON,    "/assets/weapons/axe_1.png",   5);
        createWeapon(itemRepository, "Бойова Сокира",     1000, Item.ItemRarity.UNCOMMON,  "/assets/weapons/axe_2.png",  10);
        createWeapon(itemRepository, "Сталева Сокира",    1800, Item.ItemRarity.RARE,      "/assets/weapons/axe_3.png",  20);
        createWeapon(itemRepository, "Рунічна Сокира",    3000, Item.ItemRarity.EPIC,      "/assets/weapons/axe_4.png",  35);
        createWeapon(itemRepository, "Сокира Зберігача",  4500, Item.ItemRarity.LEGENDARY, "/assets/weapons/axe_5.png",  50);
        // Bows
        createWeapon(itemRepository, "Лук Лісника",       500,  Item.ItemRarity.COMMON,    "/assets/weapons/bow_1.png",   5);
        createWeapon(itemRepository, "Мисливський Лук",   1000, Item.ItemRarity.UNCOMMON,  "/assets/weapons/bow_2.png",  10);
        createWeapon(itemRepository, "Ельфійський Лук",   1800, Item.ItemRarity.RARE,      "/assets/weapons/bow_3.png",  20);
        createWeapon(itemRepository, "Лук Грому",         3000, Item.ItemRarity.EPIC,      "/assets/weapons/bow_4.png",  35);
        createWeapon(itemRepository, "Лук Долі",          4500, Item.ItemRarity.LEGENDARY, "/assets/weapons/bow_5.png",  50);
        // Crossbows
        createWeapon(itemRepository, "Арбалет Новачка",   500,  Item.ItemRarity.COMMON,    "/assets/weapons/crossbow_1.png",   5);
        createWeapon(itemRepository, "Бойовий Арбалет",   1000, Item.ItemRarity.UNCOMMON,  "/assets/weapons/crossbow_2.png",  10);
        createWeapon(itemRepository, "Арбалет Снайпера",  1800, Item.ItemRarity.RARE,      "/assets/weapons/crossbow_3.png",  20);
        createWeapon(itemRepository, "Рунічний Арбалет",  3000, Item.ItemRarity.EPIC,      "/assets/weapons/crossbow_4.png",  35);
        createWeapon(itemRepository, "Арбалет Смерті",    4500, Item.ItemRarity.LEGENDARY, "/assets/weapons/crossbow_5.png",  50);
        // Daggers
        createWeapon(itemRepository, "Кинджал Злодія",    500,  Item.ItemRarity.COMMON,    "/assets/weapons/dagger_1.png",   5);
        createWeapon(itemRepository, "Бойовий Кинджал",   1000, Item.ItemRarity.UNCOMMON,  "/assets/weapons/dagger_2.png",  10);
        createWeapon(itemRepository, "Кинджал Асасина",   1800, Item.ItemRarity.RARE,      "/assets/weapons/dagger_3.png",  20);
        createWeapon(itemRepository, "Рунічний Кинджал",  3000, Item.ItemRarity.EPIC,      "/assets/weapons/dagger_4.png",  35);
        createWeapon(itemRepository, "Кинджал Тіні",      4500, Item.ItemRarity.LEGENDARY, "/assets/weapons/dagger_5.png",  50);
        // Forks (only 3 tiers available on disk)
        createWeapon(itemRepository, "Тризуб Рибалки",    500,  Item.ItemRarity.COMMON,    "/assets/weapons/fork_1.png",   5);
        createWeapon(itemRepository, "Бойовий Тризуб",    1000, Item.ItemRarity.UNCOMMON,  "/assets/weapons/fork_2.png",  10);
        createWeapon(itemRepository, "Тризуб Нептуна",    1800, Item.ItemRarity.RARE,      "/assets/weapons/fork_3.png",  20);
        // Halberds
        createWeapon(itemRepository, "Алебарда Охорони",  500,  Item.ItemRarity.COMMON,    "/assets/weapons/halberd_1.png",   5);
        createWeapon(itemRepository, "Бойова Алебарда",   1000, Item.ItemRarity.UNCOMMON,  "/assets/weapons/halberd_2.png",  10);
        createWeapon(itemRepository, "Лицарська Алебарда",1800, Item.ItemRarity.RARE,      "/assets/weapons/halberd_3.png",  20);
        createWeapon(itemRepository, "Алебарда Короля",   3000, Item.ItemRarity.EPIC,      "/assets/weapons/halberd_4.png",  35);
        createWeapon(itemRepository, "Алебарда Чемпіона", 4500, Item.ItemRarity.LEGENDARY, "/assets/weapons/halberd_5.png",  50);
        // Scythes
        createWeapon(itemRepository, "Коса Фермера",      500,  Item.ItemRarity.COMMON,    "/assets/weapons/scythe_1.png",   5);
        createWeapon(itemRepository, "Бойова Коса",       1000, Item.ItemRarity.UNCOMMON,  "/assets/weapons/scythe_2.png",  10);
        createWeapon(itemRepository, "Коса Жниваря",      1800, Item.ItemRarity.RARE,      "/assets/weapons/scythe_3.png",  20);
        createWeapon(itemRepository, "Рунічна Коса",      3000, Item.ItemRarity.EPIC,      "/assets/weapons/scythe_4.png",  35);
        createWeapon(itemRepository, "Коса Смерті",       4500, Item.ItemRarity.LEGENDARY, "/assets/weapons/scythe_5.png",  50);
        // Spears
        createWeapon(itemRepository, "Спис Мисливця",     500,  Item.ItemRarity.COMMON,    "/assets/weapons/spear_1.png",   5);
        createWeapon(itemRepository, "Бойовий Спис",      1000, Item.ItemRarity.UNCOMMON,  "/assets/weapons/spear_2.png",  10);
        createWeapon(itemRepository, "Спис Рейнджера",    1800, Item.ItemRarity.RARE,      "/assets/weapons/spear_3.png",  20);
        createWeapon(itemRepository, "Рунічний Спис",     3000, Item.ItemRarity.EPIC,      "/assets/weapons/spear_4.png",  35);
        createWeapon(itemRepository, "Спис Воїна",        4500, Item.ItemRarity.LEGENDARY, "/assets/weapons/spear_5.png",  50);
        // Staves
        createWeapon(itemRepository, "Посох Учня",        500,  Item.ItemRarity.COMMON,    "/assets/weapons/staff_1.png",   5);
        createWeapon(itemRepository, "Бойовий Посох",     1000, Item.ItemRarity.UNCOMMON,  "/assets/weapons/staff_2.png",  10);
        createWeapon(itemRepository, "Посох Мага",        1800, Item.ItemRarity.RARE,      "/assets/weapons/staff_3.png",  20);
        createWeapon(itemRepository, "Рунічний Посох",    3000, Item.ItemRarity.EPIC,      "/assets/weapons/staff_4.png",  35);
        createWeapon(itemRepository, "Посох Архімага",    4500, Item.ItemRarity.LEGENDARY, "/assets/weapons/staff_5.png",  50);
        // Swords (6 tiers — sword_6 is a special 2nd Legendary)
        createWeapon(itemRepository, "Меч Новачка",       500,  Item.ItemRarity.COMMON,    "/assets/weapons/sword_1.png",   5);
        createWeapon(itemRepository, "Бойовий Меч",       1000, Item.ItemRarity.UNCOMMON,  "/assets/weapons/sword_2.png",  10);
        createWeapon(itemRepository, "Лицарський Меч",    1800, Item.ItemRarity.RARE,      "/assets/weapons/sword_3.png",  20);
        createWeapon(itemRepository, "Рунічний Меч",      3000, Item.ItemRarity.EPIC,      "/assets/weapons/sword_4.png",  35);
        createWeapon(itemRepository, "Меч Чемпіона",      4500, Item.ItemRarity.LEGENDARY, "/assets/weapons/sword_5.png",  50);
        createWeapon(itemRepository, "Меч Долі",          5000, Item.ItemRarity.LEGENDARY, "/assets/weapons/sword_6.png",  55);
        // Wands
        createWeapon(itemRepository, "Паличка Учня",      500,  Item.ItemRarity.COMMON,    "/assets/weapons/wand_1.png",   5);
        createWeapon(itemRepository, "Бойова Паличка",    1000, Item.ItemRarity.UNCOMMON,  "/assets/weapons/wand_2.png",  10);
        createWeapon(itemRepository, "Паличка Мага",      1800, Item.ItemRarity.RARE,      "/assets/weapons/wand_3.png",  20);
        createWeapon(itemRepository, "Рунічна Паличка",   3000, Item.ItemRarity.EPIC,      "/assets/weapons/wand_4.png",  35);
        createWeapon(itemRepository, "Паличка Архімага",  4500, Item.ItemRarity.LEGENDARY, "/assets/weapons/wand_5.png",  50);
        // Shields (3 tiers; placed in HANDS slot)
        createShield(itemRepository, "Щит Новачка",       500,  Item.ItemRarity.COMMON,    "/assets/weapons/shield_1.png",  5);
        createShield(itemRepository, "Лицарський Щит",    1000, Item.ItemRarity.UNCOMMON,  "/assets/weapons/shield_2.png", 10);
        createShield(itemRepository, "Щит Короля",        1800, Item.ItemRarity.RARE,      "/assets/weapons/shield_3.png", 20);
    }

    // ==========================================
    // Shop item creation helpers
    // ==========================================

    private void createEquipment(ItemRepository repo, String name, int price, Item.ItemSlot slot,
            Item.ItemRarity rarity, String assetUrl) {
        if (repo.existsByName(name))
            return; // Skip if already seeded
        Item item = new Item();
        item.setName(name);
        item.setDescription("Елемент екіпірування героя.");
        item.setPrice(price);
        item.setCurrencyType(Item.CurrencyType.GOLD);
        item.setCategory(Item.ItemCategory.COSMETIC);
        item.setEffect(Item.EffectType.NONE);
        item.setSlot(slot);
        item.setRarity(rarity);
        item.setAssetUrl(assetUrl);
        item.setAttributeBonus(0);
        repo.save(item);
    }

    /** Armor item (HEAD/BODY/HANDS/LEGS): gives DEF bonus */
    private void createArmor(ItemRepository repo, String name, int price,
            Item.ItemSlot slot, Item.ItemRarity rarity, String assetUrl, int def) {
        if (repo.existsByName(name)) return;
        Item item = new Item();
        item.setName(name);
        item.setDescription("🛡️ +" + def + " DEF");
        item.setPrice(price);
        item.setCurrencyType(Item.CurrencyType.GOLD);
        item.setCategory(Item.ItemCategory.COSMETIC);
        item.setEffect(Item.EffectType.NONE);
        item.setSlot(slot);
        item.setRarity(rarity);
        item.setAssetUrl(assetUrl);
        item.setAttributeBonus(def);
        repo.save(item);
    }

    /** Weapon item: sets slot=WEAPON, category=COSMETIC, attributeBonus=atk */
    private void createWeapon(ItemRepository repo, String name, int price,
            Item.ItemRarity rarity, String assetUrl, int atk) {
        if (repo.existsByName(name)) return;
        Item item = new Item();
        item.setName(name);
        item.setDescription("Зброя героя. ⚔2️ +" + atk + " ATK");
        item.setPrice(price);
        item.setCurrencyType(Item.CurrencyType.GOLD);
        item.setCategory(Item.ItemCategory.COSMETIC);
        item.setEffect(Item.EffectType.NONE);
        item.setSlot(Item.ItemSlot.WEAPON);
        item.setRarity(rarity);
        item.setAssetUrl(assetUrl);
        item.setAttributeBonus(atk);
        repo.save(item);
    }

    /** Shield item: slots into HANDS, but carries an ATK/DEF bonus like a weapon */
    private void createShield(ItemRepository repo, String name, int price,
            Item.ItemRarity rarity, String assetUrl, int def) {
        if (repo.existsByName(name)) return;
        Item item = new Item();
        item.setName(name);
        item.setDescription("🛡️ +" + def + " DEF");
        item.setPrice(price);
        item.setCurrencyType(Item.CurrencyType.GOLD);
        item.setCategory(Item.ItemCategory.COSMETIC);
        item.setEffect(Item.EffectType.NONE);
        item.setSlot(Item.ItemSlot.HANDS);
        item.setRarity(rarity);
        item.setAssetUrl(assetUrl);
        item.setAttributeBonus(def);
        repo.save(item);
    }

    private void createConsumable(ItemRepository repo, String name, String desc, int price, Item.CurrencyType currency,
            Item.EffectType effect, String assetUrl) {
        if (repo.existsByName(name))
            return; // Skip if already seeded
        Item item = new Item();
        item.setName(name);
        item.setDescription(desc);
        item.setPrice(price);
        item.setCurrencyType(currency);
        item.setCategory(Item.ItemCategory.CONSUMABLE);
        item.setEffect(effect);
        item.setSlot(Item.ItemSlot.NONE);
        item.setRarity(Item.ItemRarity.COMMON);
        item.setAssetUrl(assetUrl);
        repo.save(item);
    }

    // Loads tasks from JSON using a two-pass save to correctly remap JSON IDs to DB-generated IDs.
    private void loadTasksFromJson(Course course, TaskRepository taskRepository) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try (InputStream inputStream = getClass().getResourceAsStream("/data/networks.json")) {
            if (inputStream == null) {
                System.err.println("❌ Файл /data/networks.json не знайдено!");
                return;
            }

            List<Task> tasks = mapper.readValue(inputStream, new TypeReference<List<Task>>() {
            });

            List<Task> existingTasks = taskRepository.findByCourseId(course.getId());
            Map<String, Task> existingTaskByTitle = existingTasks.stream()
                    .collect(Collectors.toMap(Task::getTitle, t -> t, (t1, t2) -> t1));

            // Pass 1: Record the old JSON ID → prerequisiteTaskIds mapping and null out IDs
            // so Hibernate assigns new DB IDs. Preserve insertion order for later index-based remapping.
            Map<Long, List<Long>> oldPrerequisites = new HashMap<>();
            List<Long> indexedOldIds = new ArrayList<>(); // Parallel list: indexedOldIds[i] matches tasks[i]
            List<Task> tasksToSave = new ArrayList<>();

            for (Task jsonTask : tasks) {
                Long oldId = jsonTask.getId(); // Capture JSON-defined ID before it is reset
                indexedOldIds.add(oldId); // Preserve order before ID is cleared
                oldPrerequisites.put(oldId, new ArrayList<>(jsonTask.getPrerequisiteTaskIds()));

                Task existingTask = existingTaskByTitle.get(jsonTask.getTitle());
                Task taskToPersist;

                if (existingTask != null) {
                    // Upsert: update existing entity fields
                    taskToPersist = existingTask;
                    taskToPersist.setTheoryContent(jsonTask.getTheoryContent());
                    taskToPersist.setBranchName(jsonTask.getBranchName());
                    taskToPersist.setOrderIndex(jsonTask.getOrderIndex());
                    taskToPersist.setIsTheoryHidden(jsonTask.getIsTheoryHidden());
                    taskToPersist.setRewardXp(jsonTask.getRewardXp());
                    taskToPersist.setRewardGold(jsonTask.getRewardGold());
                    taskToPersist.setDynamicQuestionCount(jsonTask.getDynamicQuestionCount());
                    taskToPersist.setType(jsonTask.getType());
                    taskToPersist.setBossName(jsonTask.getBossName());
                    taskToPersist.setBossAvatarUrl(jsonTask.getBossAvatarUrl());
                    taskToPersist.setTimeLimitSeconds(jsonTask.getTimeLimitSeconds());

                    // Upsert questions
                    if (jsonTask.getQuestions() != null) {
                        Map<String, Question> existingQuestionByText = taskToPersist.getQuestions().stream()
                                .collect(Collectors.toMap(Question::getQuestionText, q -> q, (q1, q2) -> q1));
                        
                        List<Question> updatedQuestions = new ArrayList<>();
                        for (Question jsonQ : jsonTask.getQuestions()) {
                            Question qToPersist;
                            Question existingQ = existingQuestionByText.get(jsonQ.getQuestionText());
                            if (existingQ != null) {
                                qToPersist = existingQ;
                                qToPersist.setType(jsonQ.getType());
                                qToPersist.setOptions(jsonQ.getOptions());
                                qToPersist.setCorrectAnswers(jsonQ.getCorrectAnswers());
                                qToPersist.setExplanation(jsonQ.getExplanation());
                            } else {
                                qToPersist = jsonQ;
                                qToPersist.setTask(taskToPersist);
                            }
                            updatedQuestions.add(qToPersist);
                        }
                        taskToPersist.getQuestions().clear();
                        taskToPersist.getQuestions().addAll(updatedQuestions);
                    }
                } else {
                    // New task
                    taskToPersist = jsonTask;
                    taskToPersist.setId(null);
                    taskToPersist.setCourse(course);
                    if (taskToPersist.getQuestions() != null) {
                        taskToPersist.getQuestions().forEach(q -> {
                            q.setId(null);
                            q.setTask(taskToPersist);
                        });
                    }
                }

                // Clear prerequisites temporarily; they will be remapped after the first saveAll
                taskToPersist.setPrerequisiteTaskIds(new ArrayList<>());
                tasksToSave.add(taskToPersist);
            }

            // Pass 2: First saveAll — Hibernate assigns real DB IDs.
            // JPA guarantees savedTasks is returned in the same order as tasksToSave.
            List<Task> savedTasks = taskRepository.saveAll(tasksToSave);

            // Pass 3: Build oldJsonId → newDbId map using the preserved index order.
            Map<Long, Long> oldIdToNewId = new HashMap<>();
            for (int i = 0; i < indexedOldIds.size(); i++) {
                oldIdToNewId.put(indexedOldIds.get(i), savedTasks.get(i).getId());
            }

            // Pass 4: Remap each task's prerequisiteTaskIds from JSON IDs to DB IDs.
            for (int i = 0; i < savedTasks.size(); i++) {
                Long oldId = indexedOldIds.get(i);
                List<Long> oldPrereqs = oldPrerequisites.get(oldId);
                if (oldPrereqs != null && !oldPrereqs.isEmpty()) {
                    List<Long> newPrereqs = new ArrayList<>();
                    for (Long oldPrereqId : oldPrereqs) {
                        Long newPrereqId = oldIdToNewId.get(oldPrereqId);
                        if (newPrereqId != null) {
                            newPrereqs.add(newPrereqId);
                        }
                    }
                    savedTasks.get(i).setPrerequisiteTaskIds(newPrereqs);
                }
            }

            // Pass 5: Second saveAll — persist the remapped prerequisiteTaskIds.
            taskRepository.saveAll(savedTasks);
            System.out.println("✅ Завдання для курсу '" + course.getTitle() + "' успішно завантажено з JSON!");

        } catch (Exception e) {
            System.err.println("❌ Помилка читання JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
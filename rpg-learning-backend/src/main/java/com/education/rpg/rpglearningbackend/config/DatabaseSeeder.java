package com.education.rpg.rpglearningbackend.config;

import com.education.rpg.rpglearningbackend.model.*;
import com.education.rpg.rpglearningbackend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
public class DatabaseSeeder {

    @Bean
    public CommandLineRunner seedDatabase(
            UserRepository userRepository,
            CourseRepository courseRepository,
            TaskRepository taskRepository,
            ItemRepository itemRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            System.out.println("🌱 Запуск модульної перевірки бази даних...");

            // 1. БЛОК КОРИСТУВАЧІВ
            if (userRepository.count() == 0) {
                System.out.println("Створення користувачів...");
                generateUsers(userRepository, passwordEncoder);
            }

            // 2. БЛОК КУРСІВ ТА ЗАВДАНЬ
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

            // 3. БЛОК МАГАЗИНУ (запускається завжди, але хелпери не дадуть створити дублікати)
            System.out.println("Перевірка та оновлення асортименту магазину...");
            generateShopItems(itemRepository);

            System.out.println("✅ База даних успішно синхронізована!");
        };
    }

    // ==========================================
    // ХЕЛПЕРИ ДЛЯ ПРОЦЕДУРНОЇ ГЕНЕРАЦІЇ
    // ==========================================

    private void generateLinearCourse(Course course, int taskCount, String prefix, TaskRepository taskRepository) {
        Task prevTask = null;
        for (int i = 1; i <= taskCount; i++) {
            boolean isBoss = (i % 5 == 0); // Кожне 5-те завдання - Бос
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
            if (prevTask != null) task.setPrerequisiteTaskIds(List.of(prevTask.getId()));

            Question q = createTestQuestion(task, "Тестове питання для " + task.getTitle(), List.of("Варіант 1", "Варіант 2", "Варіант 3"), "Варіант 1");
            task.setQuestions(List.of(q));
            taskRepository.save(task);
            prevTask = task;
        }
    }

    private void generateBranchedCourse(Course course, int totalTasks, TaskRepository taskRepository) {
        // Генерує деревоподібну структуру (схоже на те, що було для Курсу 1)
        Task root = new Task();
        root.setTitle("Вступ до " + course.getTitle());
        root.setTheoryContent("Основи основ.");
        root.setRewardXp(50); root.setRewardGold(20);
        root.setCourse(course); root.setBranchName("Старт");
        root.setOrderIndex(1); root.setType(Task.TaskType.REGULAR);
        root.setQuestions(List.of(createTestQuestion(root, "Готові?", List.of("Так", "Ні"), "Так")));
        taskRepository.save(root);

        Task currentDivergence = root;
        int order = 2;
        int remaining = totalTasks - 1;

        while (remaining > 0) {
            int leftLen = Math.min(3, remaining / 2 + 1);
            int rightLen = Math.min(2, remaining / 2);
            if (leftLen == 0) break;

            Task leftLast = currentDivergence;
            for(int i=0; i<leftLen; i++) {
                Task t = new Task(); t.setTitle("Гілка Практики " + order); t.setCourse(course); t.setBranchName("Практика");
                t.setOrderIndex(order++); t.setType(Task.TaskType.REGULAR); t.setRewardXp(60); t.setRewardGold(20);
                t.setPrerequisiteTaskIds(List.of(leftLast.getId()));
                t.setQuestions(List.of(createTestQuestion(t, "Питання", List.of("1", "2"), "1")));
                taskRepository.save(t); leftLast = t; remaining--;
            }

            Task rightLast = currentDivergence;
            for(int i=0; i<rightLen; i++) {
                Task t = new Task(); t.setTitle("Гілка Теорії " + order); t.setCourse(course); t.setBranchName("Теорія");
                t.setOrderIndex(order++); t.setType(Task.TaskType.REGULAR); t.setRewardXp(60); t.setRewardGold(20);
                t.setPrerequisiteTaskIds(List.of(rightLast.getId()));
                t.setQuestions(List.of(createTestQuestion(t, "Питання", List.of("1", "2"), "1")));
                taskRepository.save(t); rightLast = t; remaining--;
            }

            // Бос Злиття
            if (remaining > 0) {
                Task boss = new Task(); boss.setTitle("Бос Злиття " + order); boss.setCourse(course); boss.setBranchName("Арена");
                boss.setOrderIndex(order++); boss.setType(Task.TaskType.BOSS); boss.setRewardXp(300); boss.setRewardGold(150);
                boss.setBossName("Хранитель Гілок"); boss.setBossAvatarUrl("/assets/bosses/golem.png");
                boss.setPrerequisiteTaskIds(List.of(leftLast.getId(), rightLast.getId()));
                boss.setQuestions(List.of(createTestQuestion(boss, "Тест", List.of("Так", "Ні"), "Так")));
                taskRepository.save(boss); currentDivergence = boss; remaining--;
            }
        }
    }

    private Course createCourse(String title, String desc, String code, User author) {
        Course c = new Course(); c.setTitle(title); c.setDescription(desc); c.setAccessCode(code); c.setAuthor(author); return c;
    }

    private Question createTestQuestion(Task task, String text, List<String> options, String correctAnswer) {
        Question q = new Question(); q.setTask(task); q.setQuestionText(text); q.setType(Question.QuestionType.TEST);
        q.setOptions(options); q.setCorrectAnswers(List.of(correctAnswer)); q.setExplanation("Уважно перегляньте теорію."); return q;
    }

    private void generateUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        User teacher = new User(); teacher.setUsername("Гендальф (Вчитель)"); teacher.setEmail("teacher@rpg.com");
        teacher.setPassword(passwordEncoder.encode("12345678")); teacher.setRole(Role.TEACHER); teacher.setLevel(50);
        userRepository.save(teacher);

        User student = new User(); student.setUsername("Фродо Багінс"); student.setEmail("student@rpg.com");
        student.setPassword(passwordEncoder.encode("12345678")); student.setRole(Role.STUDENT); student.setLevel(2);
        student.setCampfireLevel(1); student.setEnergy(100); student.setLastLoginDate(LocalDateTime.now());
        userRepository.save(student);
        
        // Фіктивні студенти для лідерборду
        String[] heroNames = {
            "Сем Гемджі", "Піппін Тук", "Меррі Брендібак",
            "Арагорн", "Леголас", "Гімлі",
            "Боромір", "Фарамір", "Еовін",
            "Галадріель", "Елронд", "Саруман"
        };
        int[] privateIndexes = {3, 7};
        Random seedRandom = new Random(42);

        for (int i = 0; i < heroNames.length; i++) {
            int xp    = 100 + seedRandom.nextInt(4901);
            int level = (xp / 1000) + 1;
            int gold  = 50  + seedRandom.nextInt(751);
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
            userRepository.save(hero);
        }
    }

    private void generateShopItems(ItemRepository itemRepository) {
        // --- Розхідники (Consumables) ---
        createConsumable(itemRepository, "Зілля Мудрості", "+50% XP на 30 хвилин.", 15, Item.CurrencyType.CRYSTAL, Item.EffectType.XP_BOOST, "/assets/items/potion_wisdom.png");
        createConsumable(itemRepository, "Магніт Гобліна", "Подвійне золото на 60 хвилин.", 15, Item.CurrencyType.CRYSTAL, Item.EffectType.GOLD_BOOST, "/assets/items/goblin_magnet.png");
        createConsumable(itemRepository, "Еліксир Бадьорості", "Миттєво відновлює 100 Енергії.", 20, Item.CurrencyType.CRYSTAL, Item.EffectType.ENERGY_REFILL, "/assets/items/elixir_vigor.png");
        createConsumable(itemRepository, "Руна Захисту", "Поглинає одну поразку.", 100, Item.CurrencyType.CRYSTAL, Item.EffectType.SHIELD, "/assets/items/rune_protection.png");

        // --- Аватари (AVATAR) ---
        createEquipment(itemRepository, "Елронд", 1000, Item.ItemSlot.AVATAR, Item.ItemRarity.RARE, "/assets/avatars/elrond.png");
        createEquipment(itemRepository, "Гімлі", 1000, Item.ItemSlot.AVATAR, Item.ItemRarity.RARE, "/assets/avatars/gimli.png");

        // --- Голова (HEAD) ---
        createEquipment(itemRepository, "Шолом Новачка", 200, Item.ItemSlot.HEAD, Item.ItemRarity.COMMON, "/assets/cosmetics/Head/head1.png");

        // --- Тулуб (BODY) ---
        createEquipment(itemRepository, "Мантія Учня", 300, Item.ItemSlot.BODY, Item.ItemRarity.COMMON, "/assets/cosmetics/Chest/chest1.png");

        // --- Руки (HANDS) ---
        createEquipment(itemRepository, "Шкіряні Рукавиці", 150, Item.ItemSlot.HANDS, Item.ItemRarity.COMMON, "/assets/cosmetics/Hands/hands1.png");

        // --- Ноги (LEGS) ---
        createEquipment(itemRepository, "Чоботи Мандрівника", 150, Item.ItemSlot.LEGS, Item.ItemRarity.COMMON, "/assets/cosmetics/Legs/legs1.png");

        // --- Зброя (WEAPON) ---
        createEquipment(itemRepository, "Гостра Сокира", 500, Item.ItemSlot.WEAPON, Item.ItemRarity.RARE, "/assets/weapons/axe_1.png");
        createEquipment(itemRepository, "Лук Лісника", 500, Item.ItemSlot.WEAPON, Item.ItemRarity.RARE, "/assets/weapons/bow_1.png");
        createEquipment(itemRepository, "Сталевий Меч", 600, Item.ItemSlot.WEAPON, Item.ItemRarity.EPIC, "/assets/weapons/sword_1.png");
    }

    // ==========================================
    // ХЕЛПЕРИ ДЛЯ ПРЕДМЕТІВ МАГАЗИНУ
    // ==========================================

    private void createEquipment(ItemRepository repo, String name, int price, Item.ItemSlot slot, Item.ItemRarity rarity, String assetUrl) {
        if (repo.existsByName(name)) return; // Захист від дублікатів
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
        repo.save(item);
    }

    private void createConsumable(ItemRepository repo, String name, String desc, int price, Item.CurrencyType currency, Item.EffectType effect, String assetUrl) {
        if (repo.existsByName(name)) return; // Захист від дублікатів
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
}
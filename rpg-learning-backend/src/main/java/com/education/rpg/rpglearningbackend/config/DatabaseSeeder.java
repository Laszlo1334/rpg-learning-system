package com.education.rpg.rpglearningbackend.config;

import com.education.rpg.rpglearningbackend.model.*;
import com.education.rpg.rpglearningbackend.repository.CourseRepository;
import com.education.rpg.rpglearningbackend.repository.ItemRepository;
import com.education.rpg.rpglearningbackend.repository.TaskRepository;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
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
            if (userRepository.count() == 0) {
                System.out.println("🌱 База даних порожня. Починаємо епічну генерацію світу...");

                // ==========================================
                // 1. КОРИСТУВАЧІ
                // ==========================================
                User teacher = new User();
                teacher.setUsername("Гендальф (Вчитель)");
                teacher.setEmail("teacher@rpg.com");
                teacher.setPassword(passwordEncoder.encode("12345678"));
                teacher.setRole(Role.TEACHER);
                teacher.setLevel(50);
                teacher.setCurrentXp(50000);
                teacher.setGold(10000);
                teacher.setCrystals(1000);
                userRepository.save(teacher);

                User student = new User();
                student.setUsername("Фродо Багінс");
                student.setEmail("student@rpg.com");
                student.setPassword(passwordEncoder.encode("12345678"));
                student.setRole(Role.STUDENT);
                student.setLevel(2);
                student.setCurrentXp(250);
                student.setGold(300);
                student.setCrystals(25);
                student.setCampfireLevel(1);
                student.setEnergy(100);
                student.setLastLoginDate(LocalDateTime.now());
                userRepository.save(student);

                // ==========================================
                // 1b. ФІКТИВНІ СТУДЕНТИ ДЛЯ ЛІДЕРБОРДУ (12 гравців)
                // ==========================================
                String[] heroNames = {
                    "Сем Гемджі", "Піппін Тук", "Меррі Брендібак",
                    "Арагорн", "Леголас", "Гімлі",
                    "Боромір", "Фарамір", "Еовін",
                    "Галадріель", "Елронд", "Саруман"
                };
                // Індекси 3 та 7 — прихований профіль (перевірка SQL-фільтра isPublicProfile)
                int[] privateIndexes = {3, 7};

                Random seedRandom = new Random(42); // Фіксований seed для відтворюваності

                for (int i = 0; i < heroNames.length; i++) {
                    int xp    = 100 + seedRandom.nextInt(4901);   // 100–5000
                    int level = (xp / 1000) + 1;
                    int gold  = 50  + seedRandom.nextInt(751);    // 50–800

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
                    hero.setCampfireLevel(1 + seedRandom.nextInt(5));  // 1–5
                    hero.setEnergy(50 + seedRandom.nextInt(51));       // 50–100
                    hero.setIsPublicProfile(!isPrivate);
                    hero.setLastLoginDate(LocalDateTime.now().minusDays(seedRandom.nextInt(7)));
                    hero.setTotalTasksCompleted(seedRandom.nextInt(15));
                    userRepository.save(hero);

                    System.out.println("  👤 " + heroNames[i]
                        + " | XP=" + xp + " | Рівень=" + level
                        + " | Публічний=" + !isPrivate);
                }
                System.out.println("✅ Згенеровано 12 тестових гравців для Лідерборду.");

                // ==========================================
                Course course1 = new Course();
                course1.setTitle("Основи магії (Java Spring Boot)");
                course1.setDescription("Вступний курс для юних чарівників-програмістів. Від змінних до об'єктів.");
                course1.setAccessCode("MAGIC101");
                course1.setAuthor(teacher);
                courseRepository.save(course1);

                Course course2 = new Course();
                course2.setTitle("Некромантія Баз Даних (SQL)");
                course2.setDescription("Навчіться піднімати дані з мертвих за допомогою складних JOIN'ів.");
                course2.setAccessCode("SQL666");
                course2.setAuthor(teacher);
                courseRepository.save(course2);

                // ==========================================
                // 3. ЗАВДАННЯ ТА ДЕРЕВО (Курс 1)
                // ==========================================

                // --- Вузол 1: Корінь ---
                Task task1 = new Task();
                task1.setTitle("Перше заклинання: Змінні");
                task1.setTheoryContent("Змінні — це магічні скриньки. Уявіть, що ви кладете туди ману.");
                task1.setRewardXp(100);
                task1.setRewardGold(20);
                task1.setCourse(course1);
                task1.setBranchName("Базова Алхімія");
                task1.setOrderIndex(1);
                task1.setType(Task.TaskType.REGULAR);
                task1.setDynamicQuestionCount(2);

                Question t1q1 = createTestQuestion(task1, "Як оголосити ціле число?", List.of("int x;", "String x;", "boolean x;", "double x;"), "int x;");
                Question t1q2 = createTestQuestion(task1, "Який тип даних для тексту?", List.of("String", "text", "char", "word"), "String");
                Question t1q3 = createTestQuestion(task1, "Чи можна змінити значення константи?", List.of("Так", "Ні"), "Ні");
                Question t1q4 = createTestQuestion(task1, "Який тип займає 8 байт?", List.of("int", "long", "byte", "short"), "long");
                task1.setQuestions(List.of(t1q1, t1q2, t1q3, t1q4));
                taskRepository.save(task1);

                // --- Вузол 2: Ліва гілка ---
                Task task2 = new Task();
                task2.setTitle("Магія Умов (If/Else)");
                task2.setTheoryContent("Роздоріжжя магії: якщо умова істинна, йдемо ліворуч, інакше — праворуч.");
                task2.setRewardXp(150);
                task2.setRewardGold(30);
                task2.setCourse(course1);
                task2.setBranchName("Логічні Шляхи");
                task2.setOrderIndex(2);
                task2.setType(Task.TaskType.REGULAR);
                task2.setDynamicQuestionCount(2);
                task2.setPrerequisiteTaskIds(List.of(task1.getId()));

                Question t2q1 = createTestQuestion(task2, "Що повертає оператор == ?", List.of("int", "boolean", "String"), "boolean");
                Question t2q2 = createTestQuestion(task2, "Який оператор означає 'АБО'?", List.of("&&", "||", "!", "!="), "||");
                Question t2q3 = createTestQuestion(task2, "Який оператор означає 'ТА'?", List.of("&&", "||", "!", "!="), "&&");
                task2.setQuestions(List.of(t2q1, t2q2, t2q3));
                taskRepository.save(task2);

                // --- Вузол 3: Права гілка ---
                Task task3 = new Task();
                task3.setTitle("Цикли Часу (For/While)");
                task3.setTheoryContent("Змусьте всесвіт повторювати ваші дії, поки у вас не скінчиться мана.");
                task3.setRewardXp(150);
                task3.setRewardGold(30);
                task3.setCourse(course1);
                task3.setBranchName("Керування Часом");
                task3.setOrderIndex(2);
                task3.setType(Task.TaskType.REGULAR);
                task3.setDynamicQuestionCount(2);
                task3.setIsTheoryHidden(true);
                task3.setPrerequisiteTaskIds(List.of(task1.getId()));

                Question t3q1 = createTestQuestion(task3, "Який цикл гарантовано виконається хоча б один раз?", List.of("for", "while", "do-while"), "do-while");
                Question t3q2 = createTestQuestion(task3, "Що робить оператор break?", List.of("Пропускає ітерацію", "Зупиняє цикл", "Видає помилку"), "Зупиняє цикл");
                Question t3q3 = createTestQuestion(task3, "Що робить оператор continue?", List.of("Пропускає ітерацію", "Зупиняє цикл", "Ламає ПК"), "Пропускає ітерацію");
                task3.setQuestions(List.of(t3q1, t3q2, t3q3));
                taskRepository.save(task3);

                // --- Вузол 4: ПРОМІЖНИЙ БОС (Потребує обидві гілки) ---
                Task boss1 = new Task();
                boss1.setTitle("Випробування Голема");
                boss1.setTheoryContent("Шлях перекриває величезний кам'яний голем. Він вимагає від вас знання логіки та циклів одночасно!");
                boss1.setRewardXp(500);
                boss1.setRewardGold(200);
                boss1.setCourse(course1);
                boss1.setBranchName("Арена Хранителів");
                boss1.setOrderIndex(3);
                boss1.setType(Task.TaskType.BOSS);
                boss1.setBossName("Голем Синтаксису");
                boss1.setBossAvatarUrl("/assets/bosses/golem.png");
                boss1.setTimeLimitSeconds(120);
                boss1.setDynamicQuestionCount(4);
                boss1.setPrerequisiteTaskIds(List.of(task2.getId(), task3.getId()));

                Question b1q1 = createTestQuestion(boss1, "Чи можна використовувати if всередині while?", List.of("Так", "Ні"), "Так");
                Question b1q2 = createTestQuestion(boss1, "Що буде, якщо умова while завжди true?", List.of("Помилка компіляції", "Нескінченний цикл", "Програма завершиться"), "Нескінченний цикл");
                Question b1q3 = createTestQuestion(boss1, "Який оператор заперечує логічне значення?", List.of("!", "NOT", "~", "-"), "!");
                Question b1q4 = createTestQuestion(boss1, "Яке ключове слово використовується для виходу з циклу?", List.of("stop", "exit", "break", "return"), "break");
                Question b1q5 = createTestQuestion(boss1, "Як називається цикл всередині іншого циклу?", List.of("Подвійний", "Вкладений", "Складний"), "Вкладений");
                boss1.setQuestions(List.of(b1q1, b1q2, b1q3, b1q4, b1q5));
                taskRepository.save(boss1);

                // --- ПРОЦЕДУРНА ГЕНЕРАЦІЯ: 3 ЕПОХИ (Курс 1) ---
                Task currentDivergence = boss1; // Точка роздоріжжя
                Random random = new Random();
                int taskCounter = 4;

                for (int epoch = 1; epoch <= 3; epoch++) {
                    System.out.println("Будуємо Епоху " + epoch + "...");

                    // 🌿 1. Ліва гілка (випадкова довжина від 2 до 5)
                    int leftLength = random.nextInt(4) + 2;
                    Task leftLastTask = currentDivergence;

                    for (int i = 0; i < leftLength; i++) {
                        Task leftTask = new Task();
                        leftTask.setTitle("Епоха " + epoch + " - Ліва стежка " + (i + 1));
                        leftTask.setTheoryContent("Таємні знання лівого шляху.");
                        leftTask.setRewardXp(50 + (epoch * 10));
                        leftTask.setRewardGold(15);
                        leftTask.setCourse(course1);
                        leftTask.setBranchName("Шлях Тіні");
                        leftTask.setOrderIndex(taskCounter++);
                        leftTask.setType(Task.TaskType.REGULAR);
                        leftTask.setDynamicQuestionCount(1);
                        leftTask.setPrerequisiteTaskIds(List.of(leftLastTask.getId()));

                        Question q = createTestQuestion(leftTask, "Питання лівої стежки", List.of("А", "Б"), "А");
                        leftTask.setQuestions(List.of(q));
                        taskRepository.save(leftTask);
                        leftLastTask = leftTask;
                    }

                    // 🌿 2. Права гілка (випадкова довжина від 2 до 4)
                    int rightLength = random.nextInt(3) + 2;
                    Task rightLastTask = currentDivergence;

                    for (int i = 0; i < rightLength; i++) {
                        Task rightTask = new Task();
                        rightTask.setTitle("Епоха " + epoch + " - Права стежка " + (i + 1));
                        rightTask.setTheoryContent("Світлі знання правого шляху.");
                        rightTask.setRewardXp(50 + (epoch * 10));
                        rightTask.setRewardGold(15);
                        rightTask.setCourse(course1);
                        rightTask.setBranchName("Шлях Світла");
                        rightTask.setOrderIndex(taskCounter++);
                        rightTask.setType(Task.TaskType.REGULAR);
                        rightTask.setDynamicQuestionCount(1);
                        rightTask.setPrerequisiteTaskIds(List.of(rightLastTask.getId()));

                        Question q = createTestQuestion(rightTask, "Питання правої стежки", List.of("1", "2"), "1");
                        rightTask.setQuestions(List.of(q));
                        taskRepository.save(rightTask);
                        rightLastTask = rightTask;
                    }

                    // ⚔️ 3. Злиття: Бос Епохи
                    boolean isFinalBoss = (epoch == 3);
                    Task epochBoss = new Task();
                    epochBoss.setTitle(isFinalBoss ? "Володар Архітектури" : "Вартовий Епохи " + epoch);
                    epochBoss.setTheoryContent("Здолайте боса, щоб пройти далі.");
                    epochBoss.setRewardXp(isFinalBoss ? 2000 : 800);
                    epochBoss.setRewardGold(isFinalBoss ? 500 : 150);
                    epochBoss.setCourse(course1);
                    epochBoss.setBranchName("Арена");
                    epochBoss.setOrderIndex(taskCounter++);
                    epochBoss.setType(Task.TaskType.BOSS);
                    epochBoss.setBossName(isFinalBoss ? "Архітектор Систем" : "Вартовий");
                    epochBoss.setBossAvatarUrl("/assets/bosses/golem.png");
                    epochBoss.setDynamicQuestionCount(2);

                    epochBoss.setPrerequisiteTaskIds(List.of(leftLastTask.getId(), rightLastTask.getId()));

                    Question bq1 = createTestQuestion(epochBoss, "Тест боса 1", List.of("Так", "Ні"), "Так");
                    Question bq2 = createTestQuestion(epochBoss, "Тест боса 2", List.of("Так", "Ні"), "Так");
                    epochBoss.setQuestions(List.of(bq1, bq2));
                    taskRepository.save(epochBoss);

                    currentDivergence = epochBoss;
                }

                // --- Генерація 10 вузлів для Курсу 2 ---
                Task prevCourse2Task = null;
                for (int i = 1; i <= 10; i++) {
                    Task c2Task = new Task();
                    c2Task.setTitle("SQL Запит " + i);
                    c2Task.setTheoryContent("Теорія баз даних " + i);
                    c2Task.setRewardXp(40);
                    c2Task.setCourse(course2);
                    c2Task.setOrderIndex(i);
                    c2Task.setType(Task.TaskType.REGULAR);
                    c2Task.setDynamicQuestionCount(1);

                    if (prevCourse2Task != null) {
                        c2Task.setPrerequisiteTaskIds(List.of(prevCourse2Task.getId()));
                    }

                    Question q = createTestQuestion(c2Task, "Якою командою дістати всі колонки з таблиці?", List.of("GET *", "SELECT *", "FETCH ALL", "PULL *"), "SELECT *");
                    c2Task.setQuestions(List.of(q));

                    taskRepository.save(c2Task);
                    prevCourse2Task = c2Task;
                }

                // ==========================================
                // 4. GUILD SHOP ITEMS
                // ==========================================

                // ── Consumables ──────────────────────────────────────────────
                Item potionOfWisdom = new Item();
                potionOfWisdom.setName("Potion of Wisdom");
                potionOfWisdom.setDescription("Grants +50% XP for 30 minutes. Perfect before a boss run.");
                potionOfWisdom.setPrice(15);
                potionOfWisdom.setCurrencyType(Item.CurrencyType.CRYSTAL);
                potionOfWisdom.setCategory(Item.ItemCategory.CONSUMABLE);
                potionOfWisdom.setEffect(Item.EffectType.XP_BOOST);
                potionOfWisdom.setSlot(Item.ItemSlot.NONE);
                potionOfWisdom.setAssetUrl("/assets/items/potion_wisdom.png");

                Item goblinMagnet = new Item();
                goblinMagnet.setName("Goblin's Magnet");
                goblinMagnet.setDescription("Doubles all Gold earned for 60 minutes. The goblins weep.");
                goblinMagnet.setPrice(15);
                goblinMagnet.setCurrencyType(Item.CurrencyType.CRYSTAL);
                goblinMagnet.setCategory(Item.ItemCategory.CONSUMABLE);
                goblinMagnet.setEffect(Item.EffectType.GOLD_BOOST);
                goblinMagnet.setSlot(Item.ItemSlot.NONE);
                goblinMagnet.setAssetUrl("/assets/items/goblin_magnet.png");

                Item elixirOfVigor = new Item();
                elixirOfVigor.setName("Elixir of Vigor");
                elixirOfVigor.setDescription("Instantly restores your Energy to 100. Go again, hero.");
                elixirOfVigor.setPrice(20);
                elixirOfVigor.setCurrencyType(Item.CurrencyType.CRYSTAL);
                elixirOfVigor.setCategory(Item.ItemCategory.CONSUMABLE);
                elixirOfVigor.setEffect(Item.EffectType.ENERGY_REFILL);
                elixirOfVigor.setSlot(Item.ItemSlot.NONE);
                elixirOfVigor.setAssetUrl("/assets/items/elixir_vigor.png");

                Item runeOfProtection = new Item();
                runeOfProtection.setName("Rune of Protection");
                runeOfProtection.setDescription("Elite rune. Absorbs one defeat on the Arena. Does not stack.");
                runeOfProtection.setPrice(100);
                runeOfProtection.setCurrencyType(Item.CurrencyType.CRYSTAL);
                runeOfProtection.setCategory(Item.ItemCategory.CONSUMABLE);
                runeOfProtection.setEffect(Item.EffectType.SHIELD);
                runeOfProtection.setSlot(Item.ItemSlot.NONE);
                runeOfProtection.setAssetUrl("/assets/items/rune_protection.png");

                // ── Cosmetics ────────────────────────────────────────────────
                Item wizardHat = new Item();
                wizardHat.setName("Wizard Hat");
                wizardHat.setDescription("A tall pointed hat that radiates ancient power. +0 stats, maximum respect.");
                wizardHat.setPrice(1500);
                wizardHat.setCurrencyType(Item.CurrencyType.GOLD);
                wizardHat.setCategory(Item.ItemCategory.COSMETIC);
                wizardHat.setEffect(Item.EffectType.NONE);
                wizardHat.setSlot(Item.ItemSlot.HEAD);
                wizardHat.setAssetUrl("/assets/cosmetics/wizard_hat.png");

                Item apprenticeRobe = new Item();
                apprenticeRobe.setName("Apprentice Robe");
                apprenticeRobe.setDescription("A fine robe worn by the most dedicated students of the Academy.");
                apprenticeRobe.setPrice(2500);
                apprenticeRobe.setCurrencyType(Item.CurrencyType.GOLD);
                apprenticeRobe.setCategory(Item.ItemCategory.COSMETIC);
                apprenticeRobe.setEffect(Item.EffectType.NONE);
                apprenticeRobe.setSlot(Item.ItemSlot.BODY);
                apprenticeRobe.setAssetUrl("/assets/cosmetics/apprentice_robe.png");

                Item mysticForest = new Item();
                mysticForest.setName("Mystic Forest");
                mysticForest.setDescription("A legendary background. The forest breathes with you.");
                mysticForest.setPrice(5000);
                mysticForest.setCurrencyType(Item.CurrencyType.GOLD);
                mysticForest.setCategory(Item.ItemCategory.COSMETIC);
                mysticForest.setEffect(Item.EffectType.NONE);
                mysticForest.setSlot(Item.ItemSlot.BACKGROUND);
                mysticForest.setAssetUrl("/assets/cosmetics/mystic_forest.png");

                itemRepository.saveAll(List.of(
                    potionOfWisdom, goblinMagnet, elixirOfVigor, runeOfProtection,
                    wizardHat, apprenticeRobe, mysticForest
                ));

                System.out.println("✅ World generated! Tasks, bosses, heroes and Guild Shop are ready.");

            } else {
                System.out.println("⚡ База даних вже містить інформацію. Генерація пропущена.");
            }
        };
    }

    private Question createTestQuestion(Task task, String text, List<String> options, String correctAnswer) {
        Question q = new Question();
        q.setTask(task);
        q.setQuestionText(text);
        q.setType(Question.QuestionType.TEST);
        q.setOptions(options);
        q.setCorrectAnswers(List.of(correctAnswer));
        q.setExplanation("Уважно перегляньте теорію щодо " + correctAnswer);
        return q;
    }

    private Question createTextQuestion(Task task, String text, List<String> correctAnswers) {
        Question q = new Question();
        q.setTask(task);
        q.setQuestionText(text);
        q.setType(Question.QuestionType.TEXT);
        q.setCorrectAnswers(correctAnswers);
        q.setExplanation("Точність — ввічливість магів. Перевір синтаксис.");
        return q;
    }
}
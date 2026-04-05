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
                // 2. КУРСИ
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
                task1.setDynamicQuestionCount(2); // Видасть 2 випадкових з 4

                Question t1q1 = createTextQuestion(task1, "Як оголосити ціле число?", List.of("int x;", "int x"));
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
                task2.setPrerequisiteTaskIds(List.of(task1.getId())); // Потрібно пройти Завдання 1

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
                task3.setIsTheoryHidden(true); // Ховаємо теорію для тестування
                task3.setPrerequisiteTaskIds(List.of(task1.getId())); // Потрібно пройти Завдання 1

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
                boss1.setTimeLimitSeconds(120); // 2 хвилини на проходження
                boss1.setDynamicQuestionCount(4); // 4 питання
                boss1.setPrerequisiteTaskIds(List.of(task2.getId(), task3.getId())); // Треба пройти і 2, і 3!

                // Поки немає динамічного агрегатора питань, додаємо вручну для тесту Боса
                Question b1q1 = createTestQuestion(boss1, "Чи можна використовувати if всередині while?", List.of("Так", "Ні"), "Так");
                Question b1q2 = createTestQuestion(boss1, "Що буде, якщо умова while завжди true?", List.of("Помилка компіляції", "Нескінченний цикл", "Програма завершиться"), "Нескінченний цикл");
                Question b1q3 = createTestQuestion(boss1, "Який оператор заперечує логічне значення?", List.of("!", "NOT", "~", "-"), "!");
                Question b1q4 = createTextQuestion(boss1, "Напишіть ключове слово для виходу з циклу", List.of("break", "break;"));
                Question b1q5 = createTestQuestion(boss1, "Як називається цикл всередині іншого циклу?", List.of("Подвійний", "Вкладений", "Складний"), "Вкладений");
                boss1.setQuestions(List.of(b1q1, b1q2, b1q3, b1q4, b1q5));
                taskRepository.save(boss1);

                // ==========================================
                // 4. ТОВАРИ У МАГАЗИНІ
                // ==========================================
                Item hintScroll = new Item();
                hintScroll.setName("Сувій Ясновидіння");
                hintScroll.setDescription("Знімає 'Туман війни' з теорії. Показує приховані знання.");
                hintScroll.setPrice(15);
                hintScroll.setCurrencyType(Item.CurrencyType.CRYSTAL);
                hintScroll.setCategory(Item.ItemCategory.CONSUMABLE);
                hintScroll.setAssetUrl("/assets/items/scroll.png");

                Item hpPotion = new Item();
                hpPotion.setName("Зілля Життя");
                hpPotion.setDescription("Додає +1 серденько на Арені (максимум 5).");
                hpPotion.setPrice(50);
                hpPotion.setCurrencyType(Item.CurrencyType.GOLD);
                hpPotion.setCategory(Item.ItemCategory.CONSUMABLE);
                hpPotion.setAssetUrl("/assets/items/health_potion.png");

                Item epicFrame = new Item();
                epicFrame.setName("Золота Рамка Ачівера");
                epicFrame.setDescription("Епічна рамка для аватара, що показує ваш статус.");
                epicFrame.setPrice(500);
                epicFrame.setCurrencyType(Item.CurrencyType.GOLD);
                epicFrame.setCategory(Item.ItemCategory.COSMETIC);
                epicFrame.setAssetUrl("/assets/frames/gold-frame.png");

                Item fireSword = new Item();
                fireSword.setName("Палаючий Меч Дебагу");
                fireSword.setDescription("Косметична зброя. Показує всім, що ви винищувач багів.");
                fireSword.setPrice(100);
                fireSword.setCurrencyType(Item.CurrencyType.CRYSTAL);
                fireSword.setCategory(Item.ItemCategory.COSMETIC);
                fireSword.setAssetUrl("/assets/items/fire_sword.png");

                itemRepository.saveAll(List.of(hintScroll, hpPotion, epicFrame, fireSword));

                System.out.println("✅ Світ успішно згенеровано! Завантажено дерева завдань, босів та магазин.");
            } else {
                System.out.println("⚡ База даних вже містить інформацію. Генерація пропущена.");
            }
        };
    }

    // --- Допоміжні методи для швидкого створення питань ---

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
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
            ItemRepository itemRepository, // ДОДАНО: Репозиторій для магазину
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // Перевіряємо, чи база порожня
            if (userRepository.count() == 0) {
                System.out.println("🌱 База даних порожня. Починаємо магічну генерацію світу...");

                // 1. Створюємо Вчителя (Адміна курсу)
                User teacher = new User();
                teacher.setUsername("Гендальф (Вчитель)");
                teacher.setEmail("teacher@rpg.com");
                teacher.setPassword(passwordEncoder.encode("12345678"));
                teacher.setRole(Role.TEACHER);
                teacher.setLevel(50);
                teacher.setCurrentXp(50000);
                teacher.setGold(10000);
                teacher.setCrystals(0);
                teacher.setCampfireLevel(5);
                teacher.setEnergy(100);
                teacher.setIsPublicProfile(true);
                teacher.setLifetimeGold(10000);
                teacher.setLifetimeCrystals(0);
                teacher.setTotalTasksCompleted(500);
                teacher.setTotalFailures(50);
                userRepository.save(teacher);

                // 2. Створюємо Студента
                User student = new User();
                student.setUsername("Фродо Багінс");
                student.setEmail("student@rpg.com");
                student.setPassword(passwordEncoder.encode("12345678"));
                student.setRole(Role.STUDENT);
                student.setLevel(1);
                student.setCurrentXp(0);
                student.setGold(150);        // Даємо 150 монет для магазину (для косметики)
                student.setCrystals(10);     // Дамо 10 кристалів на старті
                student.setCampfireLevel(1);
                student.setEnergy(100);
                student.setIsPublicProfile(true);
                student.setLifetimeGold(150);
                student.setLifetimeCrystals(10);
                student.setTotalTasksCompleted(0);
                student.setTotalFailures(0);
                student.setLastLoginDate(LocalDateTime.now());
                userRepository.save(student);

                // 3. Створюємо Курс
                Course course = new Course();
                course.setTitle("Основи магії (Java Spring Boot)");
                course.setDescription("Вступний курс для юних чарівників-програмістів. Тут ви навчитеся керувати потоками мані (даних).");
                course.setAccessCode("MAGIC101");
                course.setAuthor(teacher);
                courseRepository.save(course);

                // 4. Створюємо Квести (Tasks) та Запитання (Questions)
                Task task1 = new Task();
                task1.setTitle("Перше заклинання: Hello World");
                task1.setTheoryContent("Напишіть програму, яка виводить привітання у консоль.");
                task1.setRewardXp(100);
                task1.setRewardGold(20);
                task1.setCourse(course);
                task1.setBranchName("Магія Інтерфейсів");
                task1.setOrderIndex(1);
                task1.setIsTheoryHidden(false);

                // Створюємо запитання для першого завдання
                Question q1 = new Question();
                q1.setTask(task1);
                q1.setQuestionText("Напишіть команду виводу 'Hello World' в консоль Java.");
                q1.setType(Question.QuestionType.TEXT);
                q1.setCorrectAnswers(List.of("System.out.println(\"Hello World\");", "System.out.print(\"Hello World\");"));
                q1.setExplanation("Використовуйте System.out.println() для виводу тексту.");

                task1.setQuestions(List.of(q1));

                Task task2 = new Task();
                task2.setTitle("Епічний бос: Типи даних");
                task2.setTheoryContent("Змінні зберігають дані. Уявіть їх як магічні скриньки.");
                task2.setRewardXp(500);
                task2.setRewardGold(150);
                task2.setCourse(course);
                task2.setBranchName("Магія Інтерфейсів");
                task2.setOrderIndex(2);
                task2.setIsTheoryHidden(true); // Режим Б

                // Створюємо запитання для боса
                Question q2 = new Question();
                q2.setTask(task2);
                q2.setQuestionText("Який тип даних використовується для цілих чисел у Java?");
                q2.setType(Question.QuestionType.TEST);
                q2.setOptions(List.of("String", "int", "boolean", "double"));
                q2.setCorrectAnswers(List.of("int"));
                q2.setExplanation("Для цілих чисел використовується int (integer).");

                task2.setQuestions(List.of(q2));

                // Зберігаємо все разом
                taskRepository.saveAll(List.of(task1, task2));

                // 5. НОВЕ: Створюємо товари для Крамниці Гільдії
                Item hintScroll = new Item();
                hintScroll.setName("Сувій Підказки");
                hintScroll.setDescription("Знімає 'Туман війни' з теорії складного завдання (Режим Б).");
                hintScroll.setPrice(15); // Коштує 15 кристалів (треба зробити 3 помилки, щоб купити)
                hintScroll.setCurrencyType(Item.CurrencyType.CRYSTAL);
                hintScroll.setCategory(Item.ItemCategory.CONSUMABLE);
                hintScroll.setAssetUrl("/assets/items/scroll.png");

                Item focusPotion = new Item();
                focusPotion.setName("Зілля Концентрації");
                focusPotion.setDescription("Дає множник x2 до XP за наступні 2 завдання.");
                focusPotion.setPrice(25); // Коштує 25 кристалів
                focusPotion.setCurrencyType(Item.CurrencyType.CRYSTAL);
                focusPotion.setCategory(Item.ItemCategory.CONSUMABLE);
                focusPotion.setAssetUrl("/assets/items/potion.png");

                Item epicFrame = new Item();
                epicFrame.setName("Золота Рамка Ачівера");
                epicFrame.setDescription("Епічна рамка для аватара, що показує ваш статус.");
                epicFrame.setPrice(500); // Коштує 500 золота (дорого, купується за золото)
                epicFrame.setCurrencyType(Item.CurrencyType.GOLD);
                epicFrame.setCategory(Item.ItemCategory.COSMETIC);
                epicFrame.setAssetUrl("/assets/frames/gold-frame.png");

                // Зберігаємо товари в базу
                itemRepository.saveAll(List.of(hintScroll, focusPotion, epicFrame));

                System.out.println("✅ Світ успішно згенеровано! Тестові дані та Магазин завантажено.");
            } else {
                System.out.println("⚡ База даних вже містить інформацію. Генерація пропущена.");
            }
        };
    }
}
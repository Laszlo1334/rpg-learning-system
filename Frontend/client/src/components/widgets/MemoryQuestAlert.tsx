import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { taskService } from '@/services/taskService';
import type { TaskDto } from '@/types';
import { Brain, Sparkles } from 'lucide-react';

export const MemoryQuestAlert = () => {
    // Стан для збереження квесту. Початково null.
    const [task, setTask] = useState<TaskDto | null>(null);
    const navigate = useNavigate();

    // Запит до сервера при появі компонента на екрані
    useEffect(() => {
        const fetchTask = async () => {
            try {
                const data = await taskService.getMemoryTask();
                setTask(data); // Зберігаємо завдання, якщо воно є
            } catch (error) {
                console.error('Помилка завантаження квесту-спогаду:', error);
            }
        };
        fetchTask();
    }, []);

    // Якщо завдання немає, ми не рендеримо ЖОДНОГО HTML-коду
    if (!task) return null;

    return (
        <div
            // При кліку переходимо на сторінку конкретного квесту
            onClick={() => navigate(`/tasks/${task.id}`)}
            // fixed, bottom-8, right-8 закріплюють кнопку в нижньому правому кутку екрана
            className="fixed bottom-8 right-8 bg-purple-600 hover:bg-purple-500 text-white p-4 rounded-full shadow-[0_0_20px_rgba(168,85,247,0.6)] cursor-pointer flex items-center gap-3 transition-transform hover:scale-110 z-50 animate-bounce"
        >
            <div className="relative">
                <Brain size={28} />
                {/* Маленька мигаюча жовта крапка для привернення уваги */}
                <span className="absolute -top-1 -right-1 flex h-3 w-3">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-yellow-400 opacity-75"></span>
                    <span className="relative inline-flex rounded-full h-3 w-3 bg-yellow-500"></span>
                </span>
            </div>

            {/* Текст показуємо лише на великих екранах (від md і вище) */}
            <div className="hidden md:block pr-2">
                <p className="text-sm font-bold">Квест-Спогад</p>
                <p className="text-[10px] text-purple-200 uppercase tracking-wider">Подвійна нагорода!</p>
            </div>
            <Sparkles className="absolute top-1 right-1 text-yellow-300 opacity-50" size={12} />
        </div>
    );
};
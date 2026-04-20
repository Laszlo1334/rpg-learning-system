import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { courseService } from '@/services/courseService';
import type { CourseProgressDto } from '@/types';
import { Book, Compass, Sparkles, Award, Hammer, Lock } from 'lucide-react';

export const CoursesPage = () => {
  const { user } = useAuthStore();
  const navigate = useNavigate();

  const [courses, setCourses] = useState<CourseProgressDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [filter, setFilter] = useState<'all' | 'active' | 'completed'>('all');

  useEffect(() => {
    const fetchCourses = async () => {
      try {
        const data = await courseService.getAllCourses();
        setCourses(data);
      } catch (error) {
        console.error('Помилка завантаження курсів:', error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchCourses();
  }, []);

  if (isLoading) {
    return <div className="p-8 text-center text-zinc-400">Відкриття мапи світу...</div>;
  }

  const filteredCourses = courses.filter(c => {
    if (filter === 'active') return c.status === 'in_progress';
    if (filter === 'completed') return c.status === 'completed';
    return true;
  });

  const totalCourses = courses.length;
  const exploredCourses = courses.filter(c => c.status === 'completed' || c.status === 'in_progress').length;

  return (
    <div className="max-w-7xl mx-auto space-y-8">

      {/* 🏛️ Шапка Архітектури */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-3xl p-8 flex flex-col md:flex-row items-center justify-between gap-6 shadow-xl relative overflow-hidden">
        <div className="absolute -top-10 -right-10 text-purple-500/10">
          <Compass size={200} />
        </div>

        <div className="relative z-10 text-center md:text-left">
          <h1 className="text-3xl font-black text-white mb-2 flex items-center justify-center md:justify-start gap-3">
            Глобальна Карта <Sparkles className="text-yellow-400" />
          </h1>
          <p className="text-zinc-400">
            Вітаємо, {user?.username}. Обирай свій шлях мудро. Світ магії розширюється з кожним твоїм кроком.
          </p>
        </div>

        <div className="relative z-10 flex flex-col items-center bg-zinc-950 px-6 py-4 rounded-2xl border border-zinc-800">
          <span className="text-sm font-bold text-zinc-500 uppercase tracking-widest mb-1">Досліджено</span>
          <div className="text-2xl font-black text-white">
            <span className="text-purple-400">{exploredCourses}</span> / {totalCourses}
          </div>
          <span className="text-xs text-zinc-600 mt-1">дисциплін</span>
        </div>
      </div>

      {/* 🎛️ Фільтрація */}
      <div className="flex justify-center md:justify-start gap-3">
        {(['all', 'active', 'completed'] as const).map((f) => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            className={`px-6 py-2.5 rounded-xl font-bold transition-all border ${filter === f
              ? 'bg-zinc-800 border-purple-500 text-purple-400 shadow-[0_0_15px_rgba(168,85,247,0.15)]'
              : 'bg-zinc-900 border-zinc-800 text-zinc-500 hover:text-zinc-300 hover:border-zinc-700'
              }`}
          >
            {f === 'all' ? 'Всі портали' : f === 'active' ? 'В процесі' : 'Завершені'}
          </button>
        ))}
      </div>

      {/* 🗂️ Сітка Карток */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredCourses.map(course => {
            const isLocked = course.status === 'locked'; // На випадок, якщо статус повернувся
            const isEmpty = course.totalTasks === 0;
            
            // Кастомні попередження для конкретних курсів
            let warningMessage = null;
            if (course.title.includes('Архітектура')) warningMessage = "⚠️ Вимагає розуміння ООП";
            else if (course.title.includes('Захист від темних')) warningMessage = "⚠️ Рекомендовано знання Java";
            else if (course.title.includes('Магія Інтерфейсів')) warningMessage = "⚠️ Вимагає базового JavaScript";

            return (
                <div key={course.id} 
                     onClick={() => !isLocked && !isEmpty && navigate(`/courses/${course.id}/foyer`)}
                     className={`p-6 rounded-3xl border transition-all flex flex-col ${
                         isLocked 
                         ? 'bg-zinc-950/50 border-zinc-800/50 grayscale opacity-60 cursor-not-allowed' 
                         : isEmpty
                         ? 'bg-zinc-900/50 border-dashed border-zinc-700 cursor-not-allowed'
                         : 'bg-zinc-900 border-zinc-800 hover:border-purple-500 cursor-pointer hover:shadow-[0_0_20px_rgba(168,85,247,0.15)]'
                     }`}>
                    <h3 className="text-xl font-bold mb-2">{course.title}</h3>
                    <p className="text-zinc-400 text-sm mb-6 flex-1">{course.description}</p>
                    
                    {warningMessage && !isEmpty && (
                        <div className="mb-6 inline-flex w-fit items-center gap-2 bg-orange-500/10 text-orange-400 text-xs font-bold px-3 py-1.5 rounded-lg border border-orange-500/20">
                            {warningMessage}
                        </div>
                    )}

                    {isLocked ? (
                        <div className="flex items-center gap-2 text-zinc-500 font-bold mt-auto">
                            <Lock size={18} /> Запечатано магією
                        </div>
                    ) : isEmpty ? (
                        <div className="flex items-center gap-2 text-zinc-500 font-bold mt-auto bg-zinc-800/50 w-fit px-4 py-2 rounded-xl border border-zinc-700">
                            <Hammer size={18} /> У розробці
                        </div>
                    ) : (
                        <div className="mt-auto">
                            <div className="flex justify-between text-xs font-bold mb-2 uppercase text-zinc-500">
                                <span>Прогрес</span>
                                <span>{course.completedTasks} / {course.totalTasks} завдань</span>
                            </div>
                            <div className="h-2 bg-zinc-950 rounded-full overflow-hidden">
                                <div className="h-full bg-purple-500 transition-all duration-500" style={{ width: `${(course.completedTasks / Math.max(course.totalTasks, 1)) * 100}%` }} />
                            </div>
                        </div>
                    )}
                </div>
            );
        })}
      </div>

    </div>
  );
};
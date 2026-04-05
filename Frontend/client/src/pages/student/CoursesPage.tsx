import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { courseService } from '@/services/courseService';
import type { CourseDto } from '@/types';
import { Book, Lock, Unlock, Compass, Sparkles, Award, ChevronDown, ChevronUp } from 'lucide-react';

export const CoursesPage = () => {
  const { user } = useAuthStore();
  const navigate = useNavigate();

  const [courses, setCourses] = useState<CourseDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [filter, setFilter] = useState<'all' | 'active' | 'completed'>('all');
  const [showLocked, setShowLocked] = useState(false);

  useEffect(() => {
    const fetchCourses = async () => {
      try {
        // Тимчасовий mock-даних
        const mockData: CourseDto[] = [
          { id: 1, title: 'Основи магії даних: SQL', description: 'Опануй мистецтво виклику та сортування інформації з баз даних.', totalTasks: 50, completedTasks: 15, status: 'in_progress' },
          { id: 2, title: 'Алгоритмічні закляття', description: 'Вивчи структури даних, щоб оптимізувати свої магічні потоки.', totalTasks: 30, completedTasks: 30, status: 'completed' },
          { id: 3, title: 'Захист від темних багів', description: 'Основи тестування та відловлювання помилок у коді.', totalTasks: 25, completedTasks: 0, status: 'new' },
          { id: 4, title: 'Некромантія Legacy коду', description: 'Робота зі старим кодом та рефакторинг систем.', totalTasks: 40, completedTasks: 0, status: 'locked' },
        ];
        setCourses(mockData);
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

  const availableCourses = courses.filter(c => c.status !== 'locked');
  const lockedCourses = courses.filter(c => c.status === 'locked');

  const filteredCourses = availableCourses.filter(c => {
    if (filter === 'active') return c.status === 'in_progress';
    if (filter === 'completed') return c.status === 'completed';
    return true;
  });

  const unlockedCount = availableCourses.length;
  const totalCount = courses.length;

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
            <span className="text-purple-400">{unlockedCount}</span> / {totalCount}
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
        {filteredCourses.map((course) => {
          const progressPercent = Math.round((course.completedTasks / course.totalTasks) * 100);
          const isCompleted = course.status === 'completed';
          const isInProgress = course.status === 'in_progress';

          let cardClasses = "bg-zinc-900 border border-zinc-800 hover:border-zinc-600 bg-gradient-to-br from-zinc-800/10 to-transparent";
          if (isCompleted) cardClasses = "bg-zinc-900 border-yellow-500/50 hover:border-yellow-400 shadow-[0_0_15px_rgba(234,179,8,0.05)] bg-gradient-to-br from-yellow-500/10 to-transparent";
          if (isInProgress) cardClasses = "bg-zinc-900 border-purple-500/50 hover:border-purple-400 shadow-[0_0_20px_rgba(168,85,247,0.1)] bg-gradient-to-br from-purple-500/10 to-transparent";

          return (
            <div
              key={course.id}
              onClick={() => navigate(`/courses/${course.id}/foyer`)}
              className={`relative rounded-3xl px-6 pt-6 pb-8 flex flex-col cursor-pointer transition-all duration-300 group ${cardClasses}`}
            >
              <div className="absolute top-6 right-6">
                {isCompleted ? (
                  <Award className="text-yellow-400 drop-shadow-[0_0_8px_rgba(234,179,8,0.5)]" size={28} />
                ) : (
                  <Book className={isInProgress ? 'text-purple-400 drop-shadow-[0_0_8px_rgba(168,85,247,0.3)]' : 'text-zinc-600'} size={24} />
                )}
              </div>

              <h3 className="text-xl font-black text-white pr-10 mb-3 group-hover:text-purple-300 transition-colors">
                {course.title}
              </h3>
              <p className="text-sm text-zinc-400 mb-8 line-clamp-2 min-h-[40px] leading-relaxed">
                {course.description}
              </p>

              <div className="mt-auto pt-2">
                <div className="flex justify-between text-xs font-bold mb-3">
                  <span className={isCompleted ? 'text-yellow-400 tracking-widest' : 'text-zinc-500 uppercase tracking-widest'}>
                    {isCompleted ? 'МАЙСТЕР' : 'Прогрес'}
                  </span>
                  <span className="text-zinc-400">
                    {course.completedTasks} / {course.totalTasks} завдань
                  </span>
                </div>
                <div className="w-full bg-zinc-950/50 rounded-full h-2.5 border border-zinc-800/50 overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-1000 ${isCompleted ? 'bg-yellow-400' : isInProgress ? 'bg-purple-500 shadow-[0_0_10px_rgba(168,85,247,0.5)]' : 'bg-blue-500'
                      }`}
                    style={{ width: `${progressPercent}%` }}
                  />
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* 🗂️ Запечатані Архіви */}
      {lockedCourses.length > 0 && (
        <div className="mt-12 pt-8 border-t border-zinc-800/30">
          <button
            onClick={() => setShowLocked(!showLocked)}
            className="flex items-center gap-3 text-zinc-500 hover:text-zinc-300 font-bold mx-auto transition-colors bg-zinc-900/50 px-6 py-3 rounded-2xl border border-zinc-800/50"
          >
            <Lock size={18} /> Запечатані Архіви ({lockedCourses.length})
            {showLocked ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
          </button>

          {showLocked && (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mt-8 opacity-60 grayscale hover:opacity-100 hover:grayscale-0 transition-all duration-500">
              {lockedCourses.map(course => (
                <div key={course.id} className="bg-zinc-950 border border-zinc-800 rounded-3xl p-6 relative">
                  <div className="absolute inset-0 bg-zinc-950/50 rounded-3xl z-10 flex items-center justify-center">
                    <Lock size={40} className="text-zinc-700" />
                  </div>
                  <h3 className="text-lg font-bold text-zinc-500 mb-2">{course.title}</h3>
                  <p className="text-sm text-zinc-600 mb-6 line-clamp-2 leading-relaxed">{course.description}</p>
                  <div className="w-full bg-zinc-900 rounded-full h-2 border border-zinc-800"></div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

    </div>
  );
};
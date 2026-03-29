// src/pages/student/CoursesPage.tsx
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { courseService } from '@/services/courseService';
import type { Course } from '@/types';
import { BookOpen, Users, ArrowRight, Sparkles } from 'lucide-react';

export const CoursesPage = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCourses = async () => {
      try {
        const data = await courseService.getAllCourses();
        // Захист від пагінації Spring Boot, як ми робили раніше
        if (Array.isArray(data)) setCourses(data);
        else if (data && typeof data === 'object' && Array.isArray((data as any).content)) setCourses((data as any).content);
        else setCourses([]);
      } catch (error) {
        console.error("Помилка завантаження курсів:", error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchCourses();
  }, []);

  return (
    <div className="space-y-8">
      {/* Шапка сторінки */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-3xl p-8 relative overflow-hidden shadow-lg">
        <div className="absolute top-0 right-0 w-64 h-64 bg-purple-500/10 rounded-full blur-3xl -mr-20 -mt-20 pointer-events-none"></div>
        <div className="relative z-10 flex items-center gap-4">
          <div className="p-4 bg-purple-500/20 rounded-2xl text-purple-400">
            <BookOpen size={40} />
          </div>
          <div>
            <h1 className="text-3xl font-black text-white mb-2">Академія Магії</h1>
            <p className="text-zinc-400 max-w-2xl">
              Виберіть свій шлях розвитку. Приєднуйтесь до курсів, вивчайте нові заклинання та отримуйте досвід від найкращих Майстрів гільдії.
            </p>
          </div>
        </div>
      </div>

      {/* Список курсів */}
      {isLoading ? (
        <div className="text-center py-12 text-zinc-500 font-bold animate-pulse">
          Відкриваємо архіви бібліотеки...
        </div>
      ) : courses.length === 0 ? (
        <div className="text-center py-12 bg-zinc-900 border border-zinc-800 border-dashed rounded-3xl">
          <Sparkles className="mx-auto mb-4 text-zinc-600" size={48} />
          <h3 className="text-xl font-bold text-white mb-2">Немає доступних курсів</h3>
          <p className="text-zinc-500">Магістри ще не підготували нові програми. Повертайтеся пізніше!</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {courses.map((course) => (
            <div 
              key={course.id} 
              className="group bg-zinc-900 border border-zinc-800 rounded-3xl p-6 hover:border-purple-500/50 hover:shadow-[0_0_30px_rgba(168,85,247,0.15)] transition-all cursor-pointer flex flex-col h-full relative overflow-hidden"
              onClick={() => navigate(`/courses/${course.id}`)}
            >
              {/* Декоративний градієнт при наведенні */}
              <div className="absolute inset-0 bg-gradient-to-br from-purple-500/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none"></div>
              
              <div className="relative z-10">
                <div className="flex justify-between items-start mb-4">
                  <div className="bg-zinc-950 border border-zinc-800 p-3 rounded-xl text-zinc-300 group-hover:text-purple-400 transition-colors">
                    <BookOpen size={24} />
                  </div>
                  <span className="flex items-center gap-1.5 text-xs font-bold text-zinc-500 bg-zinc-950 px-3 py-1.5 rounded-lg border border-zinc-800">
                    <Users size={14} /> ID: {course.accessCode || course.id}
                  </span>
                </div>

                <h3 className="text-xl font-bold text-white mb-3 group-hover:text-purple-300 transition-colors line-clamp-2">
                  {course.title}
                </h3>
                
                <p className="text-zinc-400 text-sm mb-6 line-clamp-3 flex-grow">
                  {course.description}
                </p>
              </div>

              <div className="mt-auto pt-4 border-t border-zinc-800/50 relative z-10 flex items-center justify-between">
                <span className="text-sm font-bold text-zinc-500 group-hover:text-zinc-300 transition-colors">
                  Увійти в підземелля
                </span>
                <ArrowRight className="text-zinc-600 group-hover:text-purple-400 transform group-hover:translate-x-1 transition-all" size={20} />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
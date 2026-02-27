// src/pages/student/StudentDashboard.tsx
import { useEffect, useState } from 'react';
import { useAuthStore } from '@/store/authStore';
import { questService } from '@/services/questService';
import type { Task } from '@/types';
import { Swords, Coins, Shield, Star, CheckCircle } from 'lucide-react';

export const StudentDashboard = () => {
  const { user } = useAuthStore();
  
  // Стан для зберігання квестів з бекенду
  const [tasks, setTasks] = useState<Task[]>([]);
  const [isLoadingTasks, setIsLoadingTasks] = useState(true);

  // При завантаженні сторінки йдемо на бекенд за квестами
  useEffect(() => {
    const fetchTasks = async () => {
      try {
        const data = await questService.getAllTasks();
        setTasks(data);
      } catch (error) {
        console.error("Помилка завантаження квестів:", error);
      } finally {
        setIsLoadingTasks(false);
      }
    };

    fetchTasks();
  }, []);

  // Розрахунок для смуги прогресу (уявимо, що для наступного рівня треба (поточний рівень * 1000) XP)
  const xpForNextLevel = (user?.level || 1) * 1000;
  const progressPercentage = Math.min(100, Math.round(((user?.xp || 0) / xpForNextLevel) * 100));

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
      
      {/* ЛІВА КОЛОНКА: Профіль гравця (Займає 1/3 екрану) */}
      <div className="lg:col-span-1 space-y-6">
        <div className="bg-zinc-900 border border-zinc-800 rounded-3xl p-6 shadow-xl relative overflow-hidden">
          {/* Декоративний фон */}
          <div className="absolute top-0 left-0 w-full h-24 bg-gradient-to-r from-purple-600/20 to-blue-600/20"></div>
          
          <div className="relative flex flex-col items-center mt-4">
            {/* Аватар (поки плейсхолдер) */}
            <div className="w-24 h-24 rounded-2xl bg-zinc-800 border-2 border-zinc-700 flex items-center justify-center mb-4 shadow-lg rotate-3 hover:rotate-0 transition-all cursor-pointer">
              <span className="text-4xl">🧙‍♂️</span>
            </div>
            
            <h2 className="text-2xl font-black mb-1">{user?.username}</h2>
            <p className="text-zinc-400 font-medium mb-6">Студент-Маг</p>

            {/* Смуга прогресу рівня */}
            <div className="w-full bg-zinc-950 rounded-full h-4 mb-2 border border-zinc-800 relative overflow-hidden">
              <div 
                className="bg-gradient-to-r from-blue-500 to-purple-500 h-full rounded-full transition-all duration-1000"
                style={{ width: `${progressPercentage}%` }}
              ></div>
            </div>
            <div className="w-full flex justify-between text-xs font-bold text-zinc-500 mb-6">
              <span>{user?.xp} XP</span>
              <span>{xpForNextLevel} XP</span>
            </div>

            {/* Статистика */}
            <div className="grid grid-cols-2 gap-3 w-full">
              <div className="bg-zinc-950/50 border border-zinc-800 rounded-xl p-3 flex flex-col items-center">
                <Shield className="text-blue-400 mb-1" size={24} />
                <span className="text-xs text-zinc-500 uppercase font-black">Рівень</span>
                <span className="text-xl font-bold">{user?.level}</span>
              </div>
              <div className="bg-zinc-950/50 border border-zinc-800 rounded-xl p-3 flex flex-col items-center">
                <Coins className="text-yellow-400 mb-1" size={24} />
                <span className="text-xs text-zinc-500 uppercase font-black">Монети</span>
                <span className="text-xl font-bold">{user?.coins}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* ПРАВА КОЛОНКА: Дошка квестів (Займає 2/3 екрану) */}
      <div className="lg:col-span-2">
        <div className="flex items-center gap-3 mb-6">
          <Swords className="text-purple-500" size={28} />
          <h2 className="text-2xl font-black">Доступні Квести</h2>
        </div>

        {isLoadingTasks ? (
          <div className="text-center py-10 text-zinc-500 animate-pulse">
            Шукаємо квести у гільдії...
          </div>
        ) : tasks.length === 0 ? (
          <div className="bg-zinc-900 border border-zinc-800 border-dashed rounded-3xl p-10 text-center">
            <span className="text-4xl mb-4 block">📜</span>
            <h3 className="text-xl font-bold mb-2">Немає доступних завдань</h3>
            <p className="text-zinc-500">Вчитель ще не додав нові квести. Відпочинь, воїне!</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Рендеримо картки квестів з бекенду */}
            {tasks.map((task) => (
              <div key={task.id} className="bg-zinc-900 border border-zinc-800 rounded-2xl p-5 hover:border-zinc-700 transition-colors group">
                <div className="flex justify-between items-start mb-3">
                  <h3 className="text-lg font-bold group-hover:text-purple-400 transition-colors">
                    {task.title}
                  </h3>
                  {task.verificationType === 'AUTO' && (
                    <span className="bg-blue-500/10 text-blue-400 text-[10px] uppercase font-black px-2 py-1 rounded-lg">
                      Авто-тест
                    </span>
                  )}
                </div>
                
                <p className="text-zinc-400 text-sm mb-5 line-clamp-2">
                  {task.description}
                </p>
                
                <div className="flex items-center justify-between mt-auto">
                  <div className="flex items-center gap-3">
                    <span className="flex items-center gap-1 text-sm font-bold text-green-400">
                      <Star size={14} /> {task.rewardXp}
                    </span>
                    <span className="flex items-center gap-1 text-sm font-bold text-yellow-400">
                      <Coins size={14} /> {task.rewardCoins}
                    </span>
                  </div>
                  <button className="bg-white text-black hover:bg-zinc-200 px-4 py-2 rounded-xl text-sm font-bold transition-colors">
                    Почати
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

    </div>
  );
};
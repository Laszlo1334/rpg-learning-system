// src/pages/student/StudentDashboard.tsx
import { useEffect, useState } from "react";
import { useAuthStore } from "@/store/authStore";
import { questService } from "@/services/questService";
import type { Task } from "@/types";
import { Swords, Coins, Shield, Star, X, Send } from "lucide-react";

export const StudentDashboard = () => {
  const { user } = useAuthStore();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [isLoadingTasks, setIsLoadingTasks] = useState(true);

  // --- СТАНИ ДЛЯ МОДАЛЬНОГО ВІКНА ---
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [answer, setAnswer] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const fetchTasks = async () => {
      try {
        const data = await questService.getAllTasks();
        if (Array.isArray(data)) setTasks(data);
        else if (
          data &&
          typeof data === "object" &&
          Array.isArray((data as any).content)
        )
          setTasks((data as any).content);
        else setTasks([]);
      } catch (error) {
        console.error("Помилка:", error);
        setTasks([]);
      } finally {
        setIsLoadingTasks(false);
      }
    };
    fetchTasks();
  }, []);

  // --- ФУНКЦІЯ ВІДПРАВКИ ВІДПОВІДІ ---
  const handleSubmitTask = async () => {
    if (!selectedTask || !answer.trim()) return;

    try {
      setIsSubmitting(true);
      await questService.submitTask(selectedTask.id, answer);
      alert("Відповідь успішно відправлена! 🎉"); // Тимчасовий алерт
      // Закриваємо модалку і очищаємо поле
      setSelectedTask(null);
      setAnswer("");
    } catch (error) {
      console.error("Помилка відправки:", error);
      alert("Сталася помилка при відправці :(");
    } finally {
      setIsSubmitting(false);
    }
  };

  const xpForNextLevel = (user?.level || 1) * 1000;
  const progressPercentage = Math.min(
    100,
    Math.round(((user?.xp || 0) / xpForNextLevel) * 100),
  );

  return (
    <div className="relative">
      {/* --- ОСНОВНИЙ КОНТЕНТ (Той самий, що й був) --- */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* ЛІВА КОЛОНКА: Профіль */}
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-zinc-900 border border-zinc-800 rounded-3xl p-6 shadow-xl relative overflow-hidden">
            <div className="absolute top-0 left-0 w-full h-24 bg-gradient-to-r from-purple-600/20 to-blue-600/20"></div>
            <div className="relative flex flex-col items-center mt-4">
              <div className="w-24 h-24 rounded-2xl bg-zinc-800 border-2 border-zinc-700 flex items-center justify-center mb-4 shadow-lg rotate-3 hover:rotate-0 transition-all cursor-pointer">
                <span className="text-4xl">🧙‍♂️</span>
              </div>
              <h2 className="text-2xl font-black mb-1">{user?.username}</h2>
              <p className="text-zinc-400 font-medium mb-6">Студент-Маг</p>
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
              <div className="grid grid-cols-2 gap-3 w-full">
                <div className="bg-zinc-950/50 border border-zinc-800 rounded-xl p-3 flex flex-col items-center">
                  <Shield className="text-blue-400 mb-1" size={24} />
                  <span className="text-xs text-zinc-500 uppercase font-black">
                    Рівень
                  </span>
                  <span className="text-xl font-bold">{user?.level}</span>
                </div>
                <div className="bg-zinc-950/50 border border-zinc-800 rounded-xl p-3 flex flex-col items-center">
                  <Coins className="text-yellow-400 mb-1" size={24} />
                  <span className="text-xs text-zinc-500 uppercase font-black">
                    Монети
                  </span>
                  <span className="text-xl font-bold">{user?.coins}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* ПРАВА КОЛОНКА: Квести */}
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
              <h3 className="text-xl font-bold mb-2">
                Немає доступних завдань
              </h3>
              <p className="text-zinc-500">
                Вчитель ще не додав нові квести. Відпочинь, воїне!
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {tasks.map((task) => (
                <div
                  key={task.id}
                  className="bg-zinc-900 border border-zinc-800 rounded-2xl p-5 hover:border-zinc-700 transition-colors flex flex-col"
                >
                  <div className="flex justify-between items-start mb-3">
                    <h3 className="text-lg font-bold text-white">
                      {task.title}
                    </h3>
                    {task.verificationType === "AUTO" && (
                      <span className="bg-blue-500/10 text-blue-400 text-[10px] uppercase font-black px-2 py-1 rounded-lg shrink-0 ml-2">
                        Авто
                      </span>
                    )}
                  </div>
                  <p className="text-zinc-400 text-sm mb-5 line-clamp-2 flex-grow">
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
                    <button
                      onClick={() => setSelectedTask(task)} // Відкриваємо модалку з цим квестом
                      className="bg-white text-black hover:bg-zinc-200 px-4 py-2 rounded-xl text-sm font-bold transition-colors"
                    >
                      Почати
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* --- УНІВЕРСАЛЬНЕ МОДАЛЬНЕ ВІКНО --- */}
      {selectedTask && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-900 border border-zinc-800 rounded-3xl w-full max-w-lg overflow-hidden shadow-2xl animate-in fade-in zoom-in duration-200">
            {/* Шапка модалки */}
            <div className="flex justify-between items-center p-6 border-b border-zinc-800 bg-zinc-950/50">
              <h3 className="text-xl font-black text-white pr-4">
                {selectedTask.title}
              </h3>
              <button
                onClick={() => setSelectedTask(null)}
                className="text-zinc-500 hover:text-white transition-colors p-1"
              >
                <X size={24} />
              </button>
            </div>

            {/* Тіло модалки */}
            <div className="p-6 space-y-6">
              <div className="bg-zinc-950 rounded-xl p-4 text-zinc-300 text-sm border border-zinc-800/50">
                {selectedTask.description}
              </div>

              <div>
                <label className="block text-sm font-bold text-zinc-400 mb-2">
                  {selectedTask.verificationType === "AUTO"
                    ? "Ваша відповідь (точний текст або код):"
                    : "Ваше рішення (текст або посилання):"}
                </label>
                <textarea
                  value={answer}
                  onChange={(e) => setAnswer(e.target.value)}
                  placeholder={
                    selectedTask.verificationType === "AUTO"
                      ? 'Наприклад: System.out.println("Hello");'
                      : "Опишіть, як ви вирішили завдання..."
                  }
                  className="w-full bg-zinc-950 border border-zinc-800 rounded-xl p-4 text-white placeholder:text-zinc-600 focus:outline-none focus:border-purple-500 focus:ring-1 focus:ring-purple-500 transition-all min-h-[120px] resize-none [&::-webkit-scrollbar]:w-2 [&::-webkit-scrollbar-track]:bg-zinc-950 [&::-webkit-scrollbar-thumb]:bg-zinc-700 [&::-webkit-scrollbar-thumb]:rounded-full hover:[&::-webkit-scrollbar-thumb]:bg-zinc-600"
                />
              </div>
            </div>

            {/* Підвал модалки з кнопкою */}
            <div className="p-6 border-t border-zinc-800 bg-zinc-950/50 flex justify-end gap-3">
              <button
                onClick={() => setSelectedTask(null)}
                className="px-5 py-2.5 rounded-xl font-bold text-zinc-400 hover:text-white hover:bg-zinc-800 transition-colors"
              >
                Скасувати
              </button>
              <button
                onClick={handleSubmitTask}
                disabled={isSubmitting || !answer.trim()}
                className="flex items-center gap-2 bg-purple-600 hover:bg-purple-500 disabled:opacity-50 disabled:cursor-not-allowed text-white px-6 py-2.5 rounded-xl font-bold transition-colors"
              >
                {isSubmitting ? "Відправка..." : "Відправити рішення"}{" "}
                <Send size={18} />
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

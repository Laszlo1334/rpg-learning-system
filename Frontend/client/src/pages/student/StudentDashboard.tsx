import { useState } from 'react';
import { useAuthStore } from '@/store/authStore';
import { Map, ShoppingBag, ScrollText, Coins, Gem } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

import { CampfireWidget } from '@/components/widgets/CampfireWidget';
import { EnergyWidget } from '@/components/widgets/EnergyWidget';
import { MemoryQuestAlert } from '@/components/widgets/MemoryQuestAlert';
import { PersonalFileModal } from '@/components/widgets/PersonalFileModal';

export const StudentDashboard = () => {
  const { user, isLoading } = useAuthStore();
  const [isPersonalFileOpen, setIsPersonalFileOpen] = useState(false);
  const navigate = useNavigate(); // Ініціалізуємо хук навігації

  if (isLoading) {
    return <div className="p-8 text-center text-zinc-400">Завантаження табору...</div>;
  }

  if (!user) {
    return <div className="p-8 text-center text-red-400">Помилка: Користувача не знайдено</div>;
  }

  const xpForNextLevel = user.level * 1000;
  const progressPercentage = Math.min(100, Math.round((user.currentXp / xpForNextLevel) * 100));

  return (
    <div className="p-6 max-w-6xl mx-auto space-y-6 relative">

      {/* --- БЛОК 1: Профіль Гравця --- */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-3xl p-6 shadow-xl flex flex-col md:flex-row items-center gap-6">
        <div className="w-24 h-24 bg-zinc-800 rounded-2xl flex items-center justify-center text-5xl border-2 border-zinc-700 shadow-lg overflow-hidden">
          {user.avatarUrl ? <img src={user.avatarUrl} alt="Avatar" className="w-full h-full object-cover" /> : '🧙‍♂️'}
        </div>

        <div className="flex-1 w-full text-center md:text-left">
          <h2 className="text-2xl font-black text-white mb-1">
            {user.username} <span className="text-zinc-500 text-lg font-bold ml-2">Рівень {user.level}</span>
          </h2>

          <div className="w-full bg-zinc-950 rounded-full h-4 mt-3 border border-zinc-800 relative overflow-hidden">
            <div
              className="bg-gradient-to-r from-blue-500 to-purple-500 h-full rounded-full transition-all duration-1000"
              style={{ width: `${progressPercentage}%` }}
            ></div>
          </div>
          <div className="flex justify-between text-xs font-bold text-zinc-500 mt-2">
            <span>{user.currentXp} XP</span>
            <span>{xpForNextLevel} XP</span>
          </div>
        </div>

        <div className="flex gap-3 w-full md:w-auto justify-center">
          {/* Використовуємо іконки Lucide замість емодзі */}
          <div className="flex flex-col items-center justify-center bg-zinc-950 px-5 py-3 rounded-xl border border-zinc-800 min-w-[90px]">
            <Coins size={28} className="text-yellow-400 mb-1" />
            <span className="font-black text-white">{user.gold}</span>
          </div>
          <div className="flex flex-col items-center justify-center bg-zinc-950 px-5 py-3 rounded-xl border border-zinc-800 min-w-[90px]">
            <Gem size={28} className="text-purple-400 mb-1" />
            <span className="font-black text-white">{user.crystals}</span>
          </div>
        </div>
      </div>

      {/* --- БЛОК 2: Головна навігація --- */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Додаємо перехід на сторінку вибору квестів */}
        <button
          onClick={() => navigate('/courses')}
          className="flex items-center justify-center gap-3 bg-blue-600 hover:bg-blue-500 text-white p-5 rounded-2xl font-bold text-lg transition-colors shadow-lg"
        >
          <Map size={24} /> Продовжити пригоду
        </button>
        {/* Додаємо перехід в магазин */}
        <button
          onClick={() => navigate('/shop')}
          className="flex items-center justify-center gap-3 bg-zinc-900 hover:bg-zinc-800 border border-zinc-700 text-white p-5 rounded-2xl font-bold text-lg transition-colors shadow-lg"
        >
          <ShoppingBag size={24} /> Крамниця Гільдії
        </button>
      </div>

      {/* --- БЛОК 3: Сітка віджетів --- */}
      {/* Додали auto-rows-fr, щоб усі картки в сітці тягнулися до однакової висоти */}
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4 auto-rows-fr">
        <CampfireWidget level={user.campfireLevel} />
        <EnergyWidget energy={user.energy} />

        <div
          onClick={() => setIsPersonalFileOpen(true)}
          className="h-full bg-zinc-900 border border-zinc-800 rounded-2xl p-6 flex flex-col items-center justify-center min-h-[160px] text-center transition-all duration-300 hover:border-blue-500/50 hover:shadow-[0_0_20px_rgba(59,130,246,0.15)] cursor-pointer group"
        >
          <ScrollText
            size={40}
            className="mb-3 text-blue-400 drop-shadow-[0_0_8px_rgba(59,130,246,0.5)] transition-transform duration-300 group-hover:scale-110"
          />
          <span className="font-bold text-white mb-1">Особиста справа</span>
          <span className="text-[10px] text-zinc-500 uppercase tracking-widest">
            Відкрити літопис
          </span>
        </div>
      </div>

      <MemoryQuestAlert />
      <PersonalFileModal isOpen={isPersonalFileOpen} onClose={() => setIsPersonalFileOpen(false)} />

    </div>
  );
};
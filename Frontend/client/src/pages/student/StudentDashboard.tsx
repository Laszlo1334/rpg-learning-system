import { useState, useEffect, useCallback } from 'react';
import { useAuthStore } from '@/store/authStore';
import { Map, ShoppingBag, Coins, Gem, BookOpen } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import type { InventoryEntry } from '@/types';

import { inventoryService } from '@/services/inventoryService';
import { courseService } from '@/services/courseService';

import { CampfireWidget } from '@/components/widgets/CampfireWidget';
import { EnergyWidget } from '@/components/widgets/EnergyWidget';
import { BackpackWidget } from '@/components/widgets/BackpackWidget';
import { MemoryQuestAlert } from '@/components/widgets/MemoryQuestAlert';
import { PlayerChronicleModal } from '@/components/modals/PlayerChronicleModal';

export const StudentDashboard = () => {
  const { user, isLoading, refreshUser } = useAuthStore();
  const [isChronicleOpen, setIsChronicleOpen] = useState(false);
  const [inventory, setInventory] = useState<InventoryEntry[]>([]);
  const [isInventoryLoading, setIsInventoryLoading] = useState(true);
  const [toast, setToast] = useState<string | null>(null);
  const [activeCourseId, setActiveCourseId] = useState<number | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
      const fetchActiveCourse = async () => {
          try {
              if (user && user.totalTasksCompleted > 0) {
                  const courses = await courseService.getAllCourses();
                  let targetCourse = courses.find((c: any) => c.status === 'in_progress');
                  if (!targetCourse) {
                      targetCourse = [...courses].reverse().find((c: any) => c.completedTasks > 0);
                  }
                  if (targetCourse) setActiveCourseId(targetCourse.id);
              }
          } catch (error) {
              console.error("Не вдалося завантажити активний курс", error);
          }
      };
      fetchActiveCourse();
  }, [user]);

  // Load inventory on mount
  useEffect(() => {
    const loadInventory = async () => {
      try {
        const data = await inventoryService.getInventory();
        setInventory(data);
      } catch (err) {
        console.error('[Dashboard] Failed to load inventory:', err);
      } finally {
        setIsInventoryLoading(false);
      }
    };
    loadInventory();
  }, []);

  // Auto-dismiss toast after 3 s
  useEffect(() => {
    if (!toast) return;
    const timer = setTimeout(() => setToast(null), 3000);
    return () => clearTimeout(timer);
  }, [toast]);

  // Use a consumable item
  const handleUseItem = useCallback(async (inventoryId: number) => {
    try {
      await inventoryService.useItem(inventoryId);
      // Sync buff timers in Header + Chronicle modal
      await refreshUser();
      // Refresh backpack quantity
      const updated = await inventoryService.getInventory();
      setInventory(updated);
      setToast('✨ Магічний ефект активовано!');
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Помилка при використанні';
      setToast(`❌ ${message}`);
    }
  }, [refreshUser]);

  if (isLoading) {
    return <div className="p-8 text-center text-zinc-400">Завантаження табору...</div>;
  }

  if (!user) {
    return <div className="p-8 text-center text-red-400">Помилка: Користувача не знайдено</div>;
  }

  const xpPerLevel = 1000;
  const xpInCurrentLevel = user.currentXp - (user.level - 1) * xpPerLevel;
  const progressPercentage = Math.min(100, Math.max(0, Math.round((xpInCurrentLevel / xpPerLevel) * 100)));

  const isNewbie = !user || user.totalTasksCompleted === 0;
  const buttonText = isNewbie ? "Розпочати пригоду" : "Продовжити пригоду";
  const targetUrl = isNewbie || !activeCourseId 
      ? '/courses' 
      : `/courses/${activeCourseId}/foyer`;

  return (
    <div className="p-6 max-w-6xl mx-auto space-y-6 relative">

      {/* ── Toast notification ─────────────────────────────── */}
      {toast && (
        <div className="fixed top-6 left-1/2 -translate-x-1/2 z-50 bg-zinc-900 border border-zinc-700 text-white text-sm font-bold px-5 py-3 rounded-2xl shadow-2xl">
          {toast}
        </div>
      )}

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
            <span>{xpInCurrentLevel} / {xpPerLevel} XP</span>
            <span>Рівень {user.level + 1}</span>
          </div>
        </div>

        <div className="flex gap-3 w-full md:w-auto justify-center">
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
        <button 
            onClick={() => navigate(targetUrl)} 
            className="w-full md:w-auto flex-1 py-4 bg-blue-600 hover:bg-blue-500 rounded-2xl font-black text-xl text-white transition-all shadow-[0_0_20px_rgba(59,130,246,0.4)] flex items-center justify-center gap-3"
        >
            <Map size={24} /> {buttonText}
        </button>
        <button
          onClick={() => navigate('/shop')}
          className="flex items-center justify-center gap-3 bg-zinc-900 hover:bg-zinc-800 border border-zinc-700 text-white p-5 rounded-2xl font-bold text-lg transition-colors shadow-lg"
        >
          <ShoppingBag size={24} /> Крамниця Гільдії
        </button>
      </div>

      {/* --- БЛОК 3: Сітка віджетів --- */}
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4 auto-rows-fr">
        <CampfireWidget level={user.campfireLevel} />
        <EnergyWidget energy={user.energy} />

        {/* Мікро-віджет: Літопис Героя */}
        <div
          onClick={() => setIsChronicleOpen(true)}
          className="h-full bg-zinc-900 border border-zinc-800 rounded-2xl p-6 flex flex-col items-center justify-center min-h-[160px] text-center transition-all duration-300 hover:border-blue-500/50 hover:shadow-[0_0_20px_rgba(59,130,246,0.15)] cursor-pointer group"
        >
          <BookOpen
            size={40}
            className="mb-3 text-blue-400 drop-shadow-[0_0_8px_rgba(59,130,246,0.5)] transition-transform duration-300 group-hover:scale-110"
          />
          <span className="font-bold text-white mb-1">Літопис Героя</span>
          <span className="text-xl font-black text-blue-400">{user.totalTasksCompleted}</span>
          <span className="text-[10px] text-zinc-500 uppercase tracking-widest mt-1">
            Пройдено випробувань
          </span>
        </div>

        {/* Рюкзак — займає повну ширину другого ряду на xl */}
        <div className="xl:col-span-3">
          <BackpackWidget
            inventory={inventory}
            onUseItem={handleUseItem}
            isLoading={isInventoryLoading}
          />
        </div>
      </div>

      <MemoryQuestAlert />
      <PlayerChronicleModal isOpen={isChronicleOpen} onClose={() => setIsChronicleOpen(false)} />

    </div>
  );
};
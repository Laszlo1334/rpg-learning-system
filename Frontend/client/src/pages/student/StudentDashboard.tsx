import { useState, useEffect, useCallback } from 'react';
import { useAuthStore } from '@/store/authStore';
import { Map, ShoppingBag, Coins, Gem, BookOpen, Sparkles, Shield, FlaskConical } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import type { InventoryEntry, ItemSlot } from '@/types';

import { inventoryService } from '@/services/inventoryService';

import { CampfireWidget } from '@/components/widgets/CampfireWidget';
import { EnergyWidget } from '@/components/widgets/EnergyWidget';
import { BackpackWidget } from '@/components/widgets/BackpackWidget';
import { MemoryQuestAlert } from '@/components/widgets/MemoryQuestAlert';
import { PlayerChronicleModal } from '@/components/modals/PlayerChronicleModal';

// ─── RPG Equipment Layout ──────────────────────────────────────────────────

type SlotConfig = {
  slot: ItemSlot;
  label: string;
  icon: string;
  gridArea: string;
};

const SLOT_CONFIGS: SlotConfig[] = [
  { slot: 'HEAD',   label: 'Голова',       icon: '⛑️',  gridArea: 'head'   },
  { slot: 'BODY',   label: 'Тулуб',        icon: '🥋',  gridArea: 'body'   },
  { slot: 'HANDS',  label: 'Руки',         icon: '🧤',  gridArea: 'hands'  },
  { slot: 'LEGS',   label: 'Ноги',         icon: '👢',  gridArea: 'legs'   },
  { slot: 'WEAPON', label: 'Зброя (гол.)', icon: '⚔️',  gridArea: 'main'   },
  { slot: 'WEAPON', label: 'Зброя (доп.)', icon: '🛡️',  gridArea: 'off'    },
];

interface EquipSlotProps {
  config: SlotConfig;
  equippedEntry: InventoryEntry | null;
}

const EquipSlot = ({ config, equippedEntry }: EquipSlotProps) => {
  const item = equippedEntry?.item;
  return (
    <div
      title={config.label}
      className="w-12 h-12 rounded-xl border-2 border-dashed border-zinc-700 bg-zinc-900/80 flex items-center justify-center overflow-hidden relative group transition-all hover:border-zinc-500"
    >
      {item?.assetUrl ? (
        <img
          src={item.assetUrl}
          alt={item.name}
          className="w-full h-full object-contain"
          style={{ imageRendering: 'pixelated' }}
          title={item.name}
        />
      ) : (
        <span className="text-xl opacity-30 select-none">{config.icon}</span>
      )}
      {/* Tooltip */}
      <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-1 px-2 py-1 bg-zinc-800 border border-zinc-700 rounded-lg text-[10px] text-zinc-300 font-bold whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-10">
        {item ? item.name : config.label}
      </div>
    </div>
  );
};

interface AvatarSlotProps {
  avatarUrl: string | null;
  equippedAvatar: InventoryEntry | null;
}

const AvatarSlot = ({ avatarUrl, equippedAvatar }: AvatarSlotProps) => {
  const src = equippedAvatar?.item.assetUrl ?? avatarUrl;
  return (
    <div className="w-24 h-24 rounded-2xl border-2 border-zinc-600 bg-zinc-800 shadow-lg overflow-hidden flex items-center justify-center relative">
      {src ? (
        <img
          src={src}
          alt="Avatar"
          className="w-full h-full object-contain"
          style={{ imageRendering: 'pixelated' }}
        />
      ) : (
        <span className="text-5xl select-none">🧙‍♂️</span>
      )}
    </div>
  );
};

// Helper: find first equipped item for a given slot
const findEquipped = (inventory: InventoryEntry[], slot: ItemSlot, skipFirst?: boolean): InventoryEntry | null => {
  const matches = inventory.filter(e => e.isEquipped && e.item.slot === slot);
  if (skipFirst) return matches[1] ?? null;
  return matches[0] ?? null;
};

// ─── Main Component ─────────────────────────────────────────────────────────

export const StudentDashboard = () => {
  const navigate = useNavigate();
  const { user, isLoading, refreshUser } = useAuthStore();
  const [isChronicleOpen, setIsChronicleOpen] = useState(false);
  const [inventory, setInventory] = useState<InventoryEntry[]>([]);
  const [isInventoryLoading, setIsInventoryLoading] = useState(true);
  const [toast, setToast] = useState<string | null>(null);
  const [itemToUse, setItemToUse] = useState<{id: number, name: string, description: string} | null>(null);


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

  const handleUseItem = useCallback(async (inventoryId: number, itemName: string, itemDescription: string) => {
    setItemToUse({ id: inventoryId, name: itemName, description: itemDescription });
  }, []);

  if (isLoading) {
    return <div className="p-8 text-center text-zinc-400">Завантаження табору...</div>;
  }

  if (!user) {
    return <div className="p-8 text-center text-red-400">Помилка: Користувача не знайдено</div>;
  }

  const xpPerLevel = 1000;
  const xpInCurrentLevel = user.currentXp % xpPerLevel;
  const progressPercentage = (user.currentXp % 1000) / 10;

  const parseBuffDate = (dateStr?: string) => {
      if (!dateStr) return new Date(0);
      return new Date(dateStr.endsWith('Z') ? dateStr : dateStr + 'Z');
  };

  const now = new Date();
  const isXpActive = user?.xpBuffEndsAt && parseBuffDate(user.xpBuffEndsAt) > now;
  const isGoldActive = user?.goldBuffEndsAt && parseBuffDate(user.goldBuffEndsAt) > now;
  const isShieldActive = user?.hasActiveShield;

  const isNewbie = !user || user.totalTasksCompleted === 0;
  const lastCourseId = localStorage.getItem('lastActiveCourseId');
  const targetUrl = isNewbie ? '/courses' : (lastCourseId ? `/courses/${lastCourseId}/foyer` : '/courses');
  const buttonText = isNewbie ? "Розпочати пригоду" : "Продовжити пригоду";

  // Equipped items
  const equippedAvatar  = findEquipped(inventory, 'AVATAR');
  const equippedHead    = findEquipped(inventory, 'HEAD');
  const equippedBody    = findEquipped(inventory, 'BODY');
  const equippedHands   = findEquipped(inventory, 'HANDS');
  const equippedLegs    = findEquipped(inventory, 'LEGS');
  const equippedMainWpn = findEquipped(inventory, 'WEAPON');
  const equippedOffWpn  = findEquipped(inventory, 'WEAPON', true);

  return (
    <div className="p-6 max-w-6xl mx-auto space-y-6 relative">

      {/* ── Toast notification ─────────────────────────────── */}
      {toast && (
        <div className="fixed top-6 left-1/2 -translate-x-1/2 z-50 bg-zinc-900 border border-zinc-700 text-white text-sm font-bold px-5 py-3 rounded-2xl shadow-2xl">
          {toast}
        </div>
      )}

      {/* --- БЛОК 1: Профіль Гравця --- */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-3xl p-6 shadow-xl flex flex-col md:flex-row flex-wrap items-center gap-6">

        {/* ── RPG Equipment Grid ────────────────────────────── */}
        <div className="flex items-center gap-3 flex-shrink-0">
          {/* Left column: HEAD / BODY / HANDS */}
          <div className="flex flex-col gap-2">
            <EquipSlot config={{ slot: 'HEAD',  label: 'Голова', icon: '⛑️', gridArea: 'head'  }} equippedEntry={equippedHead}  />
            <EquipSlot config={{ slot: 'BODY',  label: 'Тулуб',  icon: '🥋', gridArea: 'body'  }} equippedEntry={equippedBody}  />
            <EquipSlot config={{ slot: 'HANDS', label: 'Руки',   icon: '🧤', gridArea: 'hands' }} equippedEntry={equippedHands} />
          </div>

          {/* Center: AVATAR */}
          <AvatarSlot avatarUrl={user.avatarUrl} equippedAvatar={equippedAvatar} />

          {/* Right column: LEGS / WEAPON / WEAPON OFF */}
          <div className="flex flex-col gap-2">
            <EquipSlot config={{ slot: 'LEGS',   label: 'Ноги',         icon: '👢', gridArea: 'legs' }} equippedEntry={equippedLegs}    />
            <EquipSlot config={{ slot: 'WEAPON', label: 'Зброя (гол.)', icon: '⚔️', gridArea: 'main' }} equippedEntry={equippedMainWpn} />
            <EquipSlot config={{ slot: 'WEAPON', label: 'Зброя (доп.)', icon: '🛡️', gridArea: 'off'  }} equippedEntry={equippedOffWpn}  />
          </div>
        </div>

        {/* ── Name + XP bar ─────────────────────────────────── */}
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

        {/* ── Gold / Crystals ───────────────────────────────── */}
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

        {/* --- БАФИ --- */}
        {(isXpActive || isGoldActive || isShieldActive) && (
          <div className="flex flex-wrap gap-2 justify-center w-full mt-4 justify-start">
            {isXpActive && (
                <span className="bg-blue-500/20 text-blue-400 text-sm font-bold px-3 py-1.5 rounded-lg border border-blue-500/30 flex items-center gap-2">
                    <FlaskConical size={16} /> XP Бонус
                </span>
            )}
            {isGoldActive && (
                <span className="bg-yellow-500/20 text-yellow-400 text-sm font-bold px-3 py-1.5 rounded-lg border border-yellow-500/30 flex items-center gap-2">
                    <Coins size={16} /> Золото x2
                </span>
            )}
            {isShieldActive && (
                <span className="bg-purple-500/20 text-purple-400 text-sm font-bold px-3 py-1.5 rounded-lg border border-purple-500/30 flex items-center gap-2">
                    <Shield size={16} /> Руна Захисту
                </span>
            )}
          </div>
        )}
      </div>

      {/* --- БЛОК 2: Головна навігація --- */}
      <div className="relative z-10 grid grid-cols-1 md:grid-cols-2 gap-4">
        <button 
            onClick={() => navigate(targetUrl)} 
            className="w-full md:w-auto flex-1 py-4 bg-blue-600 hover:bg-blue-500 rounded-2xl font-black text-xl text-white transition-all shadow-[0_0_20px_rgba(59,130,246,0.4)] flex items-center justify-center gap-3 cursor-pointer"
        >
            <Map size={24} /> {buttonText}
        </button>
        <button
          onClick={() => navigate('/shop')}
          className="flex items-center justify-center gap-3 bg-zinc-900 hover:bg-zinc-800 border border-zinc-700 text-white p-5 rounded-2xl font-bold text-lg transition-colors shadow-lg cursor-pointer"
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
      <PlayerChronicleModal isOpen={isChronicleOpen} onClose={() => { setIsChronicleOpen(false); refreshUser(); }} />

      {/* Кастомна модалка для Зілля */}
      {itemToUse && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
              <div className="bg-zinc-900 border border-zinc-700 p-8 rounded-3xl max-w-sm w-full text-center shadow-2xl">
                  <div className="w-16 h-16 bg-purple-500/20 text-purple-400 rounded-2xl flex items-center justify-center mx-auto mb-4 border border-purple-500/30">
                      <Sparkles size={32} />
                  </div>
                  <h3 className="text-2xl font-black text-white mb-2">Використати предмет?</h3>
                  <p className="text-zinc-400 mb-2">Ти збираєшся застосувати магію: <span className="text-purple-400 font-bold">{itemToUse.name}</span>. Цю дію неможливо скасувати.</p>
                  <p className="text-sm font-bold text-zinc-500 mb-8 max-w-xs mx-auto italic">{itemToUse.description}</p>
                  <div className="flex gap-4">
                      <button onClick={() => setItemToUse(null)} className="flex-1 py-3 rounded-xl font-bold text-zinc-400 hover:text-white hover:bg-zinc-800 transition-colors">
                          Сховати
                      </button>
                      <button onClick={async () => {
                          try {
                              await inventoryService.useItem(itemToUse.id);
                              await refreshUser();
                              setItemToUse(null);
                              window.location.reload();
                          } catch (e) { console.error(e); }
                      }} className="flex-1 py-3 rounded-xl font-bold text-white bg-purple-600 hover:bg-purple-500 shadow-[0_0_15px_rgba(168,85,247,0.4)] transition-all">
                          Випити
                      </button>
                  </div>
              </div>
          </div>
      )}

    </div>
  );
};
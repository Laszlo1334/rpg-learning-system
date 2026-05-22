import { useState, useEffect, useCallback } from 'react';
import { useAuthStore } from '@/store/authStore';
import { Map, ShoppingBag, Coins, Gem, BookOpen, Sparkles, Shield } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import type { InventoryEntry, ItemSlot } from '@/types';

import { inventoryService } from '@/services/inventoryService';

import { CampfireWidget } from '@/components/widgets/CampfireWidget';
import { EnergyWidget } from '@/components/widgets/EnergyWidget';
import { BackpackWidget } from '@/components/widgets/BackpackWidget';

import { PlayerChronicleModal } from '@/components/modals/PlayerChronicleModal';
import { EquipModal } from '@/components/modals/EquipModal';

type SlotConfig = {
  slot: ItemSlot;
  label: string;
  icon: string;
  gridArea: string;
};

const SLOT_CONFIGS: SlotConfig[] = [
  { slot: 'HEAD', label: 'Голова', icon: '⛑️', gridArea: 'head' },
  { slot: 'BODY', label: 'Тулуб', icon: '🥋', gridArea: 'body' },
  { slot: 'HANDS', label: 'Руки', icon: '🧤', gridArea: 'hands' },
  { slot: 'LEGS', label: 'Ноги', icon: '👢', gridArea: 'legs' },
  { slot: 'WEAPON', label: 'Зброя (гол.)', icon: '⚔️', gridArea: 'main' },
  { slot: 'WEAPON', label: 'Зброя (доп.)', icon: '🛡️', gridArea: 'off' },
];

const getRarityBorder = (rarity?: string): string => {
  switch (rarity) {
    case 'COMMON': return 'border-solid border-zinc-400';
    case 'RARE': return 'border-solid border-blue-400 shadow-[0_0_8px_rgba(96,165,250,0.4)]';
    case 'EPIC': return 'border-solid border-purple-400 shadow-[0_0_8px_rgba(192,132,252,0.4)]';
    case 'LEGENDARY': return 'border-solid border-yellow-400 shadow-[0_0_10px_rgba(250,204,21,0.5)]';
    default: return 'border-dashed border-zinc-700';
  }
};

interface EquipSlotProps {
  config: SlotConfig;
  equippedEntry: InventoryEntry | null;
  onClick: () => void;
}

const EquipSlot = ({ config, equippedEntry, onClick }: EquipSlotProps) => {
  const item = equippedEntry?.item;
  const borderClass = item ? getRarityBorder(item.rarity) : 'border-dashed border-zinc-700';
  return (
    <div
      title={config.label}
      onClick={onClick}
      className={`w-12 h-12 rounded-xl border-2 ${borderClass} bg-zinc-900/80 flex items-center justify-center overflow-hidden relative group transition-all hover:border-blue-500/60 hover:shadow-[0_0_10px_rgba(59,130,246,0.2)] cursor-pointer`}
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
      <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-1 px-2 py-1 bg-zinc-800 border border-zinc-700 rounded-lg text-[10px] text-zinc-300 font-bold whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-10">
        {item ? item.name : config.label}
      </div>
    </div>
  );
};

const findEquipped = (inventory: InventoryEntry[], slot: ItemSlot, skipFirst?: boolean): InventoryEntry | null => {
  const matches = inventory.filter(e => e.isEquipped && e.item.slot === slot);
  if (skipFirst) return matches[1] ?? null;
  return matches[0] ?? null;
};

export const StudentDashboard = () => {
  const navigate = useNavigate();
  const { user, isLoading, refreshUser } = useAuthStore();
  const [isChronicleOpen, setIsChronicleOpen] = useState(false);
  const [inventory, setInventory] = useState<InventoryEntry[]>([]);
  const [isInventoryLoading, setIsInventoryLoading] = useState(true);
  const [toast, setToast] = useState<string | null>(null);
  const [itemToUse, setItemToUse] = useState<{ id: number; name: string; description: string; assetUrl: string | null } | null>(null);
  const [selectedSlot, setSelectedSlot] = useState<ItemSlot | null>(null);
  const [replaceItemId, setReplaceItemId] = useState<number | null>(null);

  const loadInventory = useCallback(async () => {
    try {
      const data = await inventoryService.getInventory();
      setInventory(data);
    } catch (err) {
      console.error('[Dashboard] Failed to load inventory:', err);
    } finally {
      setIsInventoryLoading(false);
    }
  }, []);

  useEffect(() => {
    loadInventory();
  }, [loadInventory]);

  useEffect(() => {
    if (!toast) return;
    const timer = setTimeout(() => setToast(null), 3000);
    return () => clearTimeout(timer);
  }, [toast]);

  const handleUseItem = useCallback(async (inventoryId: number, itemName: string, itemDescription: string, assetUrl: string | null) => {
    setItemToUse({ id: inventoryId, name: itemName, description: itemDescription, assetUrl });
  }, []);

  if (isLoading) {
    return <div className="p-8 text-center text-zinc-400">Loading camp...</div>;
  }

  if (!user) {
    return <div className="p-8 text-center text-red-400">Error: User not found</div>;
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

  const equippedAvatar = findEquipped(inventory, 'AVATAR');
  const equippedHead = findEquipped(inventory, 'HEAD');
  const equippedBody = findEquipped(inventory, 'BODY');
  const equippedHands = findEquipped(inventory, 'HANDS');
  const equippedLegs = findEquipped(inventory, 'LEGS');
  const equippedWeapons = inventory.filter(e => e.isEquipped && e.item.slot === 'WEAPON');
  const equippedMainWpn = equippedWeapons[0] ?? null;
  const equippedOffWpn = equippedWeapons[1] ?? null;

  return (
    <div className="p-6 max-w-6xl mx-auto space-y-6 relative">

      {toast && (
        <div className="fixed top-6 left-1/2 -translate-x-1/2 z-50 bg-zinc-900 border border-zinc-700 text-white text-sm font-bold px-5 py-3 rounded-2xl shadow-2xl">
          {toast}
        </div>
      )}

      <div className="bg-zinc-900 border border-zinc-800 rounded-3xl p-6 shadow-xl flex flex-col md:flex-row flex-wrap items-center gap-6">

        <div className="flex items-center gap-3 flex-shrink-0">
          <div className="flex flex-col gap-2">
            <EquipSlot config={{ slot: 'HEAD', label: 'Head', icon: '⛑️', gridArea: 'head' }} equippedEntry={equippedHead} onClick={() => { setSelectedSlot('HEAD'); setReplaceItemId(equippedHead?.id ?? null); }} />
            <EquipSlot config={{ slot: 'BODY', label: 'Body', icon: '🥋', gridArea: 'body' }} equippedEntry={equippedBody} onClick={() => { setSelectedSlot('BODY'); setReplaceItemId(equippedBody?.id ?? null); }} />
            <EquipSlot config={{ slot: 'HANDS', label: 'Hands', icon: '🧤', gridArea: 'hands' }} equippedEntry={equippedHands} onClick={() => { setSelectedSlot('HANDS'); setReplaceItemId(equippedHands?.id ?? null); }} />
          </div>

          <div
            className={`w-24 h-24 rounded-2xl border-2 ${equippedAvatar ? getRarityBorder(equippedAvatar.item.rarity) : 'border-zinc-600'
              } bg-zinc-800 shadow-lg overflow-hidden flex items-center justify-center relative cursor-pointer hover:border-blue-500/60 hover:shadow-[0_0_15px_rgba(59,130,246,0.2)] transition-all group`}
            onClick={() => { setSelectedSlot('AVATAR'); setReplaceItemId(equippedAvatar?.id ?? null); }}
            title="Avatar"
          >
            {(equippedAvatar?.item.assetUrl ?? user.avatarUrl) ? (
              <img
                src={equippedAvatar?.item.assetUrl ?? user.avatarUrl!}
                alt="Avatar"
                className="w-full h-full object-contain"
                style={{ imageRendering: 'pixelated' }}
              />
            ) : (
              <span className="text-5xl select-none">🧙‍♂️</span>
            )}
            <div className="absolute inset-0 bg-blue-500/10 opacity-0 group-hover:opacity-100 transition-opacity rounded-2xl" />
          </div>

          <div className="flex flex-col gap-2">
            <EquipSlot config={{ slot: 'LEGS', label: 'Legs', icon: '👢', gridArea: 'legs' }} equippedEntry={equippedLegs} onClick={() => { setSelectedSlot('LEGS'); setReplaceItemId(equippedLegs?.id ?? null); }} />
            <EquipSlot config={{ slot: 'WEAPON', label: 'Weapon (main)', icon: '⚔️', gridArea: 'main' }} equippedEntry={equippedMainWpn} onClick={() => { setSelectedSlot('WEAPON'); setReplaceItemId(equippedMainWpn?.id ?? null); }} />
            <EquipSlot config={{ slot: 'WEAPON', label: 'Weapon (off)', icon: '🛡️', gridArea: 'off' }} equippedEntry={equippedOffWpn} onClick={() => { setSelectedSlot('WEAPON'); setReplaceItemId(equippedOffWpn?.id ?? null); }} />
          </div>
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

        {(isXpActive || isGoldActive || isShieldActive) && (
          <div className="flex flex-wrap gap-2 w-full mt-4">
            {isXpActive && (
              <span className="bg-blue-500/20 text-blue-400 text-sm font-bold px-3 py-1.5 rounded-lg border border-blue-500/30 flex items-center gap-2">
                <img src="/assets/items/potion_wisdom.png" alt="XP Buff" className="w-5 h-5 object-contain" style={{ imageRendering: 'pixelated' }} onError={e => { e.currentTarget.style.display = 'none'; }} />
                XP Bonus
              </span>
            )}
            {isGoldActive && (
              <span className="bg-yellow-500/20 text-yellow-400 text-sm font-bold px-3 py-1.5 rounded-lg border border-yellow-500/30 flex items-center gap-2">
                <img src="/assets/items/goblin_magnet.png" alt="Gold Buff" className="w-5 h-5 object-contain" style={{ imageRendering: 'pixelated' }} onError={e => { e.currentTarget.style.display = 'none'; }} />
                Gold x2
              </span>
            )}
            {isShieldActive && (
              <span className="bg-purple-500/20 text-purple-400 text-sm font-bold px-3 py-1.5 rounded-lg border border-purple-500/30 flex items-center gap-2">
                <Shield size={16} /> Rune of Protection
              </span>
            )}
          </div>
        )}
      </div>

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

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4 auto-rows-fr">
        <CampfireWidget level={user.campfireLevel} />
        <EnergyWidget energy={user.energy} />

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

        {/* BackpackWidget spans full width of the xl row */}
        <div className="xl:col-span-3">
          <BackpackWidget
            inventory={inventory}
            onUseItem={handleUseItem}
            isLoading={isInventoryLoading}
          />
        </div>
      </div>


      <PlayerChronicleModal isOpen={isChronicleOpen} onClose={() => { setIsChronicleOpen(false); refreshUser(); }} />

      {/* Equip Modal — opens when a slot is clicked */}
      {selectedSlot && (
        <EquipModal
          selectedSlot={selectedSlot}
          inventory={inventory}
          replaceItemId={replaceItemId}
          onClose={() => { setSelectedSlot(null); setReplaceItemId(null); }}
          onEquipped={() => { loadInventory(); }}
        />
      )}

      {/* Confirmation modal for consumable use */}
      {itemToUse && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-900 border border-zinc-700 p-8 rounded-3xl max-w-sm w-full text-center shadow-2xl">
            <div className="w-16 h-16 bg-purple-500/20 text-purple-400 rounded-2xl flex items-center justify-center mx-auto mb-4 border border-purple-500/30 overflow-hidden">
              {itemToUse.assetUrl ? (
                <img
                  src={itemToUse.assetUrl}
                  alt={itemToUse.name}
                  className="w-10 h-10 object-contain"
                  style={{ imageRendering: 'pixelated' }}
                />
              ) : (
                <Sparkles size={32} />
              )}
            </div>
            <h3 className="text-2xl font-black text-white mb-2">Використати предмет?</h3>
            <p className="text-zinc-400 mb-2">
              Ви збираєтесь використати: <span className="text-purple-400 font-bold">{itemToUse.name}</span>.
            </p>
            <p className="text-sm text-zinc-500 mb-2">Цю дію неможливо скасувати.</p>
            <p className="text-sm font-bold text-zinc-500 mb-8 max-w-xs mx-auto italic">{itemToUse.description}</p>
            <div className="flex gap-4">
              <button onClick={() => setItemToUse(null)} className="flex-1 py-3 rounded-xl font-bold text-zinc-400 hover:text-white hover:bg-zinc-800 transition-colors">
                Скасувати
              </button>
              <button onClick={async () => {
                try {
                  await inventoryService.useItem(itemToUse.id);
                  await refreshUser();
                  setItemToUse(null);
                  window.location.reload();
                } catch (e) { console.error(e); }
              }} className="flex-1 py-3 rounded-xl font-bold text-white bg-purple-600 hover:bg-purple-500 shadow-[0_0_15px_rgba(168,85,247,0.4)] transition-all">
                Використати
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};
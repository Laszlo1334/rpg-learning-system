// src/components/modals/EquipModal.tsx
import { useState } from 'react';
import { X, CheckCircle2, Loader2 } from 'lucide-react';
import type { InventoryEntry, ItemSlot } from '@/types';
import { inventoryService } from '@/services/inventoryService';

interface EquipModalProps {
  selectedSlot: ItemSlot;
  inventory: InventoryEntry[];
  onClose: () => void;
  onEquipped: () => void; // callback to trigger inventory refetch
  replaceItemId?: number | null; // ID of the currently-equipped item to displace (for dual-wield)
}

const SLOT_LABEL: Record<ItemSlot, string> = {
  HEAD:   'Head',
  BODY:   'Body',
  HANDS:  'Hands',
  LEGS:   'Legs',
  WEAPON: 'Weapon',
  AVATAR: 'Avatar',
  NONE:   'None',
};

const RARITY_COLOR: Record<string, string> = {
  COMMON:    'border-zinc-600 text-zinc-400',
  RARE:      'border-blue-500/60 text-blue-400',
  EPIC:      'border-purple-500/60 text-purple-400',
  LEGENDARY: 'border-yellow-500/60 text-yellow-400',
};

export const EquipModal = ({ selectedSlot, inventory, onClose, onEquipped, replaceItemId }: EquipModalProps) => {
  const [loadingId, setLoadingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Items matching the slot (both equipped and unequipped shown for toggle UX)
  const slotItems = inventory.filter(entry => entry.item.slot === selectedSlot);

  const handleEquip = async (entry: InventoryEntry) => {
    if (entry.isEquipped && entry.id === replaceItemId) return; // already the target — skip
    if (entry.isEquipped && replaceItemId == null) return;      // already equipped, nothing to replace
    setLoadingId(entry.id);
    setError(null);
    try {
      // Pass replaceItemId so the backend knows exactly which weapon slot to free
      await inventoryService.equipItem(entry.id, replaceItemId);
      onEquipped(); // trigger refetch
      onClose();
    } catch (err) {
      console.error('[EquipModal] equip failed:', err);
      setError('Failed to equip item. Please try again.');
    } finally {
      setLoadingId(null);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        className="bg-zinc-900 border border-zinc-700 rounded-3xl p-6 max-w-md w-full shadow-2xl"
        onClick={e => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between mb-5">
          <h3 className="text-lg font-black text-white">
            Equip — {SLOT_LABEL[selectedSlot]} Slot
          </h3>
          <button
            onClick={onClose}
            className="text-zinc-500 hover:text-white transition-colors"
          >
            <X size={20} />
          </button>
        </div>

        {/* Error */}
        {error && (
          <p className="text-red-400 text-sm font-bold mb-4 bg-red-500/10 border border-red-500/20 rounded-xl px-3 py-2">
            {error}
          </p>
        )}

        {/* Item list */}
        {slotItems.length === 0 ? (
          <div className="text-center py-10 text-zinc-500">
            <span className="text-4xl opacity-30 block mb-3">🎒</span>
            <p className="text-sm font-bold">No items for this slot.</p>
            <p className="text-xs mt-1">Visit the Guild Shop to purchase gear.</p>
          </div>
        ) : (
          <ul className="flex flex-col gap-3 max-h-80 overflow-y-auto pr-1">
            {slotItems.map(entry => {
              const rarityClass = RARITY_COLOR[entry.item.rarity] ?? RARITY_COLOR.COMMON;
              const isEquipped = entry.isEquipped;
              const isLoading = loadingId === entry.id;

              return (
                <li
                  key={entry.id}
                  className={`flex items-center gap-3 rounded-2xl border px-4 py-3 transition-all duration-200 ${
                    isEquipped
                      ? 'bg-zinc-800 border-emerald-500/50'
                      : `bg-zinc-950 ${rarityClass} hover:bg-zinc-800 cursor-pointer`
                  }`}
                  onClick={() => handleEquip(entry)}
                >
                  {/* Sprite */}
                  <div className="w-10 h-10 rounded-xl bg-zinc-800 border border-zinc-700 flex items-center justify-center flex-shrink-0 overflow-hidden">
                    {entry.item.assetUrl ? (
                      <img
                        src={entry.item.assetUrl}
                        alt={entry.item.name}
                        className="w-full h-full object-contain"
                        style={{ imageRendering: 'pixelated' }}
                        onError={e => { e.currentTarget.style.display = 'none'; }}
                      />
                    ) : (
                      <span className="text-xl">🗡️</span>
                    )}
                  </div>

                  {/* Info */}
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-black text-white truncate">{entry.item.name}</p>
                    <p className="text-[11px] text-zinc-500 truncate">{entry.item.description}</p>
                    <span className={`text-[10px] font-bold uppercase tracking-wider ${rarityClass.split(' ')[1]}`}>
                      {entry.item.rarity}
                    </span>
                  </div>

                  {/* Status */}
                  {isLoading ? (
                    <Loader2 size={18} className="animate-spin text-zinc-400 flex-shrink-0" />
                  ) : isEquipped ? (
                    <CheckCircle2 size={18} className="text-emerald-400 flex-shrink-0" />
                  ) : (
                    <button
                      className="flex-shrink-0 bg-blue-600 hover:bg-blue-500 text-white text-[11px] font-black px-3 py-1.5 rounded-lg transition-all shadow-[0_0_10px_rgba(59,130,246,0.3)]"
                      onClick={e => { e.stopPropagation(); handleEquip(entry); }}
                    >
                      Equip
                    </button>
                  )}
                </li>
              );
            })}
          </ul>
        )}
      </div>
    </div>
  );
};

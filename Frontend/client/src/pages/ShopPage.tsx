// src/pages/ShopPage.tsx
import { useState, useEffect, useCallback, useMemo } from 'react';
import { createPortal } from 'react-dom';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { shopService } from '@/services/shopService';
import { inventoryService } from '@/services/inventoryService';
import type { Item, InventoryEntry, ItemSlot } from '@/types';
import {
    ArrowLeft, Coins, Gem, ShoppingCart, Loader2,
    Shirt, FlaskConical, AlertCircle, X, CheckCircle
} from 'lucide-react';

type ActiveTab = 'COSMETIC' | 'CONSUMABLE';
type SlotFilter = ItemSlot | 'ALL';
type SortOption = 'RARITY' | 'PRICE_ASC' | 'PRICE_DESC' | 'ALPHABETICAL';

const rarityWeights: Record<string, number> = {
    COMMON: 1,
    UNCOMMON: 2,
    RARE: 3,
    EPIC: 4,
    LEGENDARY: 5,
};

const SORT_OPTIONS: { value: SortOption; label: string }[] = [
    { value: 'RARITY', label: 'Рідкість (Common → Legendary)' },
    { value: 'PRICE_ASC', label: 'Ціна: Найменша → Найбільша' },
    { value: 'PRICE_DESC', label: 'Ціна: Найбільша → Найменша' },
    { value: 'ALPHABETICAL', label: 'Алфавітний (A → Я)' },
];

function sortShopItems(items: Item[], sortBy: SortOption): Item[] {
    const sorted = [...items];
    switch (sortBy) {
        case 'RARITY':
            return sorted.sort(
                (a, b) =>
                    (rarityWeights[a.rarity ?? 'COMMON'] ?? 0) -
                    (rarityWeights[b.rarity ?? 'COMMON'] ?? 0),
            );
        case 'PRICE_ASC':
            return sorted.sort((a, b) => a.price - b.price);
        case 'PRICE_DESC':
            return sorted.sort((a, b) => b.price - a.price);
        case 'ALPHABETICAL':
            return sorted.sort((a, b) =>
                a.name.localeCompare(b.name, undefined, { sensitivity: 'base' }),
            );
        default:
            return sorted;
    }
}

// Backend effect → readable label
const EFFECT_LABELS: Record<string, string> = {
    XP_BOOST: '🧪 Досвід ×1.5 на 30 хв',
    GOLD_BOOST: '🧲 Золото ×2 на 60 хв',
    ENERGY_REFILL: '☕ Енергія до 100%',
    SHIELD: '🛡️ Щит на 1 проходження',
    NONE: '',
};

// Rarity color map
const RARITY_BORDER: Record<string, string> = {
    COMMON: 'border-zinc-600',
    UNCOMMON: 'border-green-500',
    RARE: 'border-blue-500',
    EPIC: 'border-purple-500',
    LEGENDARY: 'border-yellow-400',
};
const RARITY_GLOW: Record<string, string> = {
    COMMON: '',
    UNCOMMON: 'shadow-[0_0_10px_rgba(34,197,94,0.30)]',
    RARE: 'shadow-[0_0_12px_rgba(59,130,246,0.35)]',
    EPIC: 'shadow-[0_0_12px_rgba(168,85,247,0.35)]',
    LEGENDARY: 'shadow-[0_0_16px_rgba(234,179,8,0.45)]',
};
const RARITY_LABEL: Record<string, string> = {
    COMMON: 'Common',
    UNCOMMON: 'Uncommon',
    RARE: 'Rare',
    EPIC: 'Epic',
    LEGENDARY: 'Legendary',
};
const RARITY_TEXT: Record<string, string> = {
    COMMON: 'text-zinc-400',
    UNCOMMON: 'text-green-400',
    RARE: 'text-blue-400',
    EPIC: 'text-purple-400',
    LEGENDARY: 'text-yellow-400',
};

const SLOT_FILTERS: { label: string; value: SlotFilter }[] = [
    { label: 'Усе', value: 'ALL' },
    { label: 'Аватар', value: 'AVATAR' },
    { label: 'Голова', value: 'HEAD' },
    { label: 'Тіло', value: 'BODY' },
    { label: 'Ноги', value: 'LEGS' },
    { label: 'Руки', value: 'HANDS' },
    { label: 'Зброя', value: 'WEAPON' },
];

export const ShopPage = () => {
    const navigate = useNavigate();
    const { user, refreshUser } = useAuthStore();

    // ─── Data state ────────────────────────────────────────────────
    const [items, setItems] = useState<Item[]>([]);
    const [inventory, setInventory] = useState<InventoryEntry[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    // ─── UI state ──────────────────────────────────────────────────
    const [activeTab, setActiveTab] = useState<ActiveTab>('CONSUMABLE');
    const [activeFilter, setActiveFilter] = useState<SlotFilter>('ALL');
    const [sortBy, setSortBy] = useState<SortOption>('RARITY');
    const [buyingId, setBuyingId] = useState<number | null>(null);
    const [itemToBuy, setItemToBuy] = useState<Item | null>(null);
    const [notification, setNotification] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

    // ─── Load items + inventory on mount ───────────────────────────
    useEffect(() => {
        const load = async () => {
            try {
                const [shopData, inventoryData] = await Promise.all([
                    shopService.getItems(),
                    inventoryService.getInventory(),
                ]);
                setItems(shopData);
                setInventory(inventoryData);
            } catch (err) {
                console.error('[ShopPage] Load error:', err);
            } finally {
                setIsLoading(false);
            }
        };
        load();
    }, []);

    // ─── Auto-dismiss notification after 3 s ───────────────────────
    useEffect(() => {
        if (!notification) return;
        const timer = setTimeout(() => setNotification(null), 3000);
        return () => clearTimeout(timer);
    }, [notification]);

    // ─── Purchase (called from confirm modal) ──────────────────────
    const handleConfirmBuy = useCallback(async () => {
        if (!itemToBuy) return;
        setBuyingId(itemToBuy.id);
        const boughtItem = itemToBuy;
        setItemToBuy(null);

        try {
            await shopService.buyItem(boughtItem.id);
            const [, newInventory] = await Promise.all([
                refreshUser(),
                inventoryService.getInventory(),
            ]);
            setInventory(newInventory);
            setNotification({ type: 'success', message: `"${boughtItem.name}" додано до інвентарю! ✅` });
        } catch (err: unknown) {
            const raw = err instanceof Error ? err.message : '';
            setNotification({ type: 'error', message: raw || 'Помилка покупки. Спробуйте ще раз.' });
        } finally {
            setBuyingId(null);
        }
    }, [itemToBuy, refreshUser]);

    // ─── Helpers ───────────────────────────────────────────────────
    const canAfford = (item: Item): boolean => {
        if (!user) return false;
        return item.currencyType === 'GOLD' ? user.gold >= item.price : user.crystals >= item.price;
    };

    const getOwnedQuantity = (item: Item): number => {
        const entry = inventory.find(inv => inv.item.id === item.id);
        return entry ? entry.quantity : 0;
    };

    // Filter by tab first, then by slot
    const tabFiltered = items.filter(i => i.category === activeTab);
    const filteredItems = activeTab === 'COSMETIC' && activeFilter !== 'ALL'
        ? tabFiltered.filter(i => i.slot === activeFilter)
        : tabFiltered;

    const sortedItems = useMemo(
        () => sortShopItems(filteredItems, sortBy),
        [filteredItems, sortBy],
    );

    const tabClass = (tab: ActiveTab) =>
        `flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl font-bold text-sm transition-all ${activeTab === tab ? 'bg-zinc-800 text-white shadow-sm' : 'text-zinc-500 hover:text-zinc-300'
        }`;

    const filterBtnClass = (val: SlotFilter) =>
        `px-3 py-1.5 rounded-xl text-xs font-bold transition-all border ${activeFilter === val
            ? 'bg-purple-600 border-purple-500 text-white'
            : 'bg-zinc-900 border-zinc-700 text-zinc-400 hover:border-zinc-500 hover:text-zinc-200'
        }`;

    // ─── Item Card ─────────────────────────────────────────────────
    const ItemCard = ({ item }: { item: Item }) => {
        const affordable = canAfford(item);
        const isBuying = buyingId === item.id;
        const ownedQty = getOwnedQuantity(item);
        const isOwned = inventory.some(inv => inv.item.id === item.id);
        const rarity = item.rarity ?? 'COMMON';

        // Cosmetics that are already owned cannot be repurchased
        const isCosmeticOwned = item.category === 'COSMETIC' && isOwned;
        const cardAffordable = !isCosmeticOwned && affordable;

        return (
            <div
                className={`bg-zinc-900 border-2 rounded-2xl p-5 flex flex-col gap-3 transition-all duration-200 ${isCosmeticOwned
                    ? 'border-emerald-700/60 opacity-75'
                    : item.category === 'COSMETIC'
                        ? `${RARITY_BORDER[rarity]} ${RARITY_GLOW[rarity]} ${!cardAffordable ? 'opacity-60' : 'hover:brightness-110'
                        }`
                        : cardAffordable
                            ? 'border-zinc-800 hover:border-zinc-600 hover:shadow-lg'
                            : 'border-zinc-800/50 opacity-60'
                    }`}
            >
                {/* Icon with rarity border */}
                <div className={`w-14 h-14 rounded-xl bg-zinc-800 border-2 ${RARITY_BORDER[rarity]} ${RARITY_GLOW[rarity]} flex items-center justify-center overflow-hidden`}>
                    {item.assetUrl ? (
                        <img
                            src={item.assetUrl}
                            alt={item.name}
                            className="w-full h-full object-contain"
                            style={{ imageRendering: 'pixelated' }}
                        />
                    ) : (
                        <span className="text-2xl">{item.category === 'COSMETIC' ? '✨' : '🧪'}</span>
                    )}
                </div>

                {/* Rarity label */}
                <span className={`text-[10px] font-black uppercase tracking-wider ${RARITY_TEXT[rarity]}`}>
                    {RARITY_LABEL[rarity]}
                </span>

                {/* Info */}
                <div className="flex-1">
                    <h3 className="font-black text-white text-base leading-tight">{item.name}</h3>
                    <p className="text-zinc-500 text-xs mt-1 leading-relaxed">{item.description}</p>
                    {EFFECT_LABELS[item.effect] && (
                        <p className="text-xs text-blue-400 mt-2 font-bold">{EFFECT_LABELS[item.effect]}</p>
                    )}

                </div>

                {/* Owned quantity (consumables only) */}
                {item.category === 'CONSUMABLE' && (
                    <p className="text-xs text-zinc-500">
                        В інвентарі: <span className={ownedQty > 0 ? 'text-green-400 font-bold' : ''}>{ownedQty} шт.</span>
                    </p>
                )}

                {/* Price + Buy */}
                <div className="flex items-center justify-between gap-2 mt-auto pt-3 border-t border-zinc-800">
                    <div className="flex items-center gap-1.5 font-black text-base">
                        {item.currencyType === 'GOLD'
                            ? <Coins size={14} className="text-yellow-400" />
                            : <Gem size={14} className="text-purple-400" />
                        }
                        <span className={item.currencyType === 'GOLD' ? 'text-yellow-400' : 'text-purple-400'}>
                            {item.price}
                        </span>
                    </div>

                    {/* Owned badge for cosmetics already in inventory */}
                    {isCosmeticOwned ? (
                        <span className="flex items-center gap-1.5 px-4 py-2 rounded-xl font-bold text-sm bg-emerald-900/50 border border-emerald-700/50 text-emerald-400 cursor-not-allowed">
                            <CheckCircle size={14} />
                            Вже придбано
                        </span>
                    ) : (
                        <button
                            type="button"
                            onClick={(e) => {
                                e.preventDefault();
                                if (cardAffordable && !isBuying) setItemToBuy(item);
                            }}
                            disabled={!cardAffordable || isBuying}
                            className={`flex items-center gap-1.5 px-4 py-2 rounded-xl font-bold text-sm transition-all disabled:cursor-not-allowed ${cardAffordable ? 'bg-blue-600 hover:bg-blue-500 text-white' : 'bg-zinc-800 text-zinc-500'
                                }`}
                        >
                            {isBuying
                                ? <Loader2 size={16} className="animate-spin" />
                                : <ShoppingCart size={16} />
                            }
                            {isBuying ? 'Виконується покупка...' : cardAffordable ? 'Придбати' : 'Недостатньо коштів'}
                        </button>
                    )}
                </div>
            </div>
        );
    };

    // ──────────────────────────────────────────────────────────────────
    return (
        <div className="p-6 max-w-5xl mx-auto space-y-6">

            {/* ── Toast ─────────────────────────────────────────────── */}
            {notification && (
                <div className={`fixed top-6 left-1/2 -translate-x-1/2 z-50 flex items-center gap-3 px-5 py-3 rounded-2xl shadow-2xl font-bold text-sm ${notification.type === 'success'
                    ? 'bg-emerald-900 border border-emerald-700 text-emerald-200'
                    : 'bg-red-900 border border-red-700 text-red-200'
                    }`}>
                    <AlertCircle size={18} />
                    {notification.message}
                </div>
            )}

            {/* ── Confirmation Modal (portal → document.body) ─────── */}
            {itemToBuy && createPortal(
                <div
                    className="fixed top-0 left-0 w-screen h-[100dvh] z-[100] bg-black/80 flex items-center justify-center p-4 overscroll-none"
                    style={{ pointerEvents: 'auto' }}
                    onClick={() => setItemToBuy(null)}
                    onWheel={(e) => { e.preventDefault(); e.stopPropagation(); }}
                    onTouchMove={(e) => e.stopPropagation()}
                >
                    <div
                        className="bg-zinc-900 border border-zinc-800 rounded-2xl p-6 max-w-sm w-full shadow-2xl"
                        onClick={e => e.stopPropagation()}
                    >
                        <div className="flex items-center justify-between mb-4">
                            <h3 className="text-lg font-black text-white">Підтвердження покупки</h3>
                            <button onClick={() => setItemToBuy(null)} className="text-zinc-500 hover:text-white transition-colors">
                                <X size={20} />
                            </button>
                        </div>

                        <div className="bg-zinc-950 border border-zinc-800 rounded-xl p-4 mb-5 flex items-center gap-3">
                            {itemToBuy.assetUrl ? (
                                <img
                                    src={itemToBuy.assetUrl}
                                    alt={itemToBuy.name}
                                    className="w-8 h-8 object-contain shrink-0"
                                    style={{ imageRendering: 'pixelated' }}
                                />
                            ) : (
                                <span className="text-3xl shrink-0">{itemToBuy.category === 'COSMETIC' ? '✨' : '🧪'}</span>
                            )}
                            <div>
                                <p className="font-black text-white">{itemToBuy.name}</p>
                                <p className="text-sm text-zinc-500 flex items-center gap-1 mt-0.5">
                                    {itemToBuy.currencyType === 'GOLD'
                                        ? (
                                            <>
                                                Витратити&nbsp;
                                                <Coins size={14} className="text-yellow-400" />
                                                <span className="text-yellow-400 font-bold">{itemToBuy.price} золота</span>
                                            </>
                                        )
                                        : (
                                            <>
                                                Витратити&nbsp;
                                                <Gem size={14} className="text-purple-400" />
                                                <span className="text-purple-400 font-bold">{itemToBuy.price} кристалів</span>
                                            </>
                                        )
                                    }
                                </p>
                            </div>
                        </div>

                        <div className="flex gap-3">
                            <button
                                onClick={() => setItemToBuy(null)}
                                className="flex-1 py-2.5 rounded-xl font-bold bg-zinc-800 text-zinc-300 hover:bg-zinc-700 transition-colors"
                            >
                                Скасувати
                            </button>
                            <button
                                onClick={handleConfirmBuy}
                                className="flex-1 py-2.5 rounded-xl font-bold bg-blue-600 hover:bg-blue-500 text-white transition-colors flex items-center justify-center gap-2"
                            >
                                <CheckCircle size={16} /> Підтвердити
                            </button>
                        </div>
                    </div>
                </div>,
                document.body,
            )}

            {/* ── Page Header ───────────────────────────────────────── */}
            <div className="flex items-center justify-between gap-4 flex-wrap">
                <div className="flex items-center gap-3">
                    <button
                        onClick={() => navigate('/dashboard')}
                        className="p-2 text-zinc-400 hover:text-white hover:bg-zinc-800 rounded-xl transition-colors"
                        title="Back to Camp"
                    >
                        <ArrowLeft size={22} />
                    </button>
                    <div>
                        <h1 className="text-2xl font-black text-white">Магазин</h1>
                        <p className="text-zinc-500 text-sm">Витрачай золото та кристали з розумом</p>
                    </div>
                </div>

                <div className="flex items-center gap-3">
                    <div className="flex items-center gap-2 bg-zinc-900 border border-zinc-800 px-4 py-2 rounded-xl">
                        <Coins size={18} className="text-yellow-400" />
                        <span className="font-black text-white">{user?.gold ?? 0}</span>
                    </div>
                    <div className="flex items-center gap-2 bg-zinc-900 border border-zinc-800 px-4 py-2 rounded-xl">
                        <Gem size={18} className="text-purple-400" />
                        <span className="font-black text-white">{user?.crystals ?? 0}</span>
                    </div>
                </div>
            </div>

            {/* ── Tabs ──────────────────────────────────────────────── */}
            <div className="flex bg-zinc-900 border border-zinc-800 rounded-2xl p-1 gap-1">
                <button
                    onClick={() => { setActiveTab('CONSUMABLE'); setActiveFilter('ALL'); }}
                    className={tabClass('CONSUMABLE')}
                >
                    <FlaskConical size={16} /> Витратні предмети
                </button>
                <button
                    onClick={() => { setActiveTab('COSMETIC'); setActiveFilter('ALL'); }}
                    className={tabClass('COSMETIC')}
                >
                    <Shirt size={16} /> Косметика
                </button>
            </div>

            {/* ── Slot filter + sort ─────────────────────────────────── */}
            <div className="flex flex-wrap items-center justify-between gap-3">
                {activeTab === 'COSMETIC' ? (
                    <div className="flex flex-wrap gap-2">
                        {SLOT_FILTERS.map(f => (
                            <button
                                key={f.value}
                                onClick={() => setActiveFilter(f.value)}
                                className={filterBtnClass(f.value)}
                            >
                                {f.label}
                            </button>
                        ))}
                    </div>
                ) : (
                    <span className="text-xs text-zinc-600 font-bold uppercase tracking-wider">
                        Сортувати
                    </span>
                )}

                <label className="flex items-center gap-2 shrink-0">
                    <span className="text-xs font-bold text-zinc-500 hidden sm:inline">Сортувати</span>
                    <select
                        value={sortBy}
                        onChange={e => setSortBy(e.target.value as SortOption)}
                        aria-label="Sort shop items"
                        className="px-3 py-1.5 rounded-xl text-xs font-bold bg-zinc-900 border border-zinc-700 text-zinc-300 hover:border-zinc-500 hover:text-zinc-200 focus:outline-none focus:border-purple-500 focus:ring-1 focus:ring-purple-500/40 transition-all cursor-pointer min-w-[11rem] sm:min-w-[13rem]"
                    >
                        {SORT_OPTIONS.map(opt => (
                            <option key={opt.value} value={opt.value} className="bg-zinc-900 text-zinc-200">
                                {opt.label}
                            </option>
                        ))}
                    </select>
                </label>
            </div>

            {/* ── Content ───────────────────────────────────────────── */}
            {isLoading ? (
                <div className="flex items-center justify-center py-20 gap-3 text-zinc-500">
                    <Loader2 size={24} className="animate-spin" />
                    <span className="font-bold">Завантаження...</span>
                </div>
            ) : sortedItems.length === 0 ? (
                <div className="py-20 text-center text-zinc-500 font-bold">Нічого немає 🧹</div>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                    {sortedItems.map(item => <ItemCard key={item.id} item={item} />)}
                </div>
            )}
        </div>
    );
};

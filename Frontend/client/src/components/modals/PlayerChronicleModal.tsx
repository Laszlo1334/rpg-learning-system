import { useAuthStore } from '@/store/authStore';
import {
    X, Target, Skull, Coins, Gem, Crosshair,
    BookOpen, Shield, FlaskConical, Magnet, Coffee, Sparkles, CalendarDays,
    Flame, LogIn, Clock, Trophy
} from 'lucide-react';

interface PlayerChronicleModalProps {
    isOpen: boolean;
    onClose: () => void;
}


const parseBuffDate = (dateStr?: string) => {
    if (!dateStr) return new Date(0);
    return new Date(dateStr.endsWith('Z') ? dateStr : dateStr + 'Z');
};

/** Returns true if the buff expiry date is still in the future */
const isBuffActive = (dateString: string | null): boolean => {
    if (!dateString) return false;
    return parseBuffDate(dateString) > new Date();
};

/** Formats total seconds into a readable playtime string */
const formatPlayTime = (totalSeconds: number): string => {
    if (totalSeconds <= 0) return '0m';
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    if (hours > 0) return `${hours}h ${minutes}m`;
    if (minutes > 0) return `${minutes}m`;
    return `${totalSeconds}s`;
};

/** Formats an ISO date string into a localised display string, or 'Немає даних' if absent */
const formatDate = (dateString: string | null): string => {
    if (!dateString) return 'Немає даних';
    return new Date(dateString).toLocaleString('uk-UA', {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
};


export const PlayerChronicleModal = ({ isOpen, onClose }: PlayerChronicleModalProps) => {
    const user = useAuthStore(state => state.user);

    if (!isOpen || !user) return null;

    // task completion accuracy (0–100 %)
    const totalAttempts = user.totalTasksCompleted + user.totalFailures;
    const accuracy = totalAttempts > 0
        ? Math.round((user.totalTasksCompleted / totalAttempts) * 100)
        : 0;

    const accuracyColor =
        accuracy >= 75 ? 'from-green-500 to-emerald-400' :
            accuracy >= 50 ? 'from-yellow-500 to-amber-400' :
                'from-red-500 to-orange-400';

    // Active buffs
    const buffs = [
        {
            active: user.hasActiveShield === true,
            icon: <Shield size={18} className="text-blue-400" />,
            label: 'Аура безстрашності',
            sublabel: 'Щит від поразки',
            color: 'border-blue-500/30 bg-blue-500/5',
        },
        {
            active: isBuffActive(user.xpBuffEndsAt),
            icon: <FlaskConical size={18} className="text-purple-400" />,
            label: 'Еліксир Мудрості',
            sublabel: `XP ×2 до ${formatDate(user.xpBuffEndsAt)}`,
            color: 'border-purple-500/30 bg-purple-500/5',
        },
        {
            active: isBuffActive(user.goldBuffEndsAt),
            icon: <Magnet size={18} className="text-yellow-400" />,
            label: 'Магніт Гобліна',
            sublabel: `Gold ×2 до ${formatDate(user.goldBuffEndsAt)}`,
            color: 'border-yellow-500/30 bg-yellow-500/5',
        },
        {
            active: isBuffActive(user.energyStasisEndsAt),
            icon: <Coffee size={18} className="text-amber-400" />,
            label: 'Кава Магістра',
            sublabel: `Безкінечна енергія до ${formatDate(user.energyStasisEndsAt)}`,
            color: 'border-amber-500/30 bg-amber-500/5',
        },
    ].filter(b => b.active);

    const statCards = [
        {
            icon: <Target size={26} className="text-blue-400" />,
            label: 'Пройдено завдань',
            value: user.totalTasksCompleted,
            glow: 'hover:border-blue-500/40',
        },
        {
            icon: <Skull size={26} className="text-red-400" />,
            label: 'Продуктивних невдач',
            value: user.totalFailures,
            glow: 'hover:border-red-500/40',
        },
        {
            icon: <Coins size={26} className="text-yellow-400" />,
            label: 'Золота за весь час',
            value: user.lifetimeGold.toLocaleString(),
            glow: 'hover:border-yellow-500/40',
        },
        {
            icon: <Gem size={26} className="text-purple-400" />,
            label: 'Кристалів за весь час',
            value: user.lifetimeCrystals.toLocaleString(),
            glow: 'hover:border-purple-500/40',
        },
    ];

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm"
            onClick={e => { if (e.target === e.currentTarget) onClose(); }}
        >
            <div className="bg-zinc-900 border border-zinc-800 rounded-3xl w-full max-w-md shadow-2xl overflow-hidden max-h-[90vh] flex flex-col">
<div className="flex items-center justify-between px-6 py-5 border-b border-zinc-800 bg-zinc-950/60 flex-shrink-0">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-blue-500/10 rounded-xl flex items-center justify-center">
                            <BookOpen size={22} className="text-blue-400" />
                        </div>
                        <div>
                            <h3 className="text-lg font-black text-white leading-tight">Літопис Гравця</h3>
                            <p className="text-xs text-zinc-500">{user.username} · Рівень {user.level}</p>
                        </div>
                    </div>
                    <button
                        onClick={onClose}
                        className="text-zinc-500 hover:text-white transition-colors p-1 rounded-lg hover:bg-zinc-800"
                    >
                        <X size={22} />
                    </button>
                </div>

                {/* Scrollable body */}
                <div className="overflow-y-auto flex-1 p-5 space-y-4">

                {/* Section: combat stats */}
                <div>
                        <p className="text-[10px] font-black text-zinc-500 uppercase tracking-widest mb-3 flex items-center gap-1.5">
                            <Target size={11} /> Бойова статистика
                        </p>

                        <div className="grid grid-cols-2 gap-2.5">
                            {statCards.map((card, idx) => (
                                <div
                                    key={idx}
                                    className={`bg-zinc-950 border border-zinc-800 rounded-2xl p-4 text-center transition-all duration-200 ${card.glow}`}
                                >
                                    <div className="flex justify-center mb-1.5">{card.icon}</div>
                                    <p className="text-[10px] text-zinc-500 font-bold uppercase tracking-wide leading-tight mb-1">
                                        {card.label}
                                    </p>
                                    <p className="text-2xl font-black text-white">{card.value}</p>
                                </div>
                            ))}
                        </div>

                        {/* Точність маг-касту */}
                        <div className="mt-2.5 bg-zinc-950 border border-zinc-800 rounded-2xl p-4">
                            <div className="flex items-center justify-between mb-2.5">
                                <div className="flex items-center gap-2">
                                    <Crosshair size={16} className="text-zinc-400" />
                                    <span className="text-sm font-bold text-zinc-300">Точність маг-касту</span>
                                </div>
                                <span className="text-xl font-black text-white">{accuracy}%</span>
                            </div>
                            <div className="w-full bg-zinc-800 rounded-full h-2 overflow-hidden">
                                <div
                                    className={`h-full rounded-full bg-gradient-to-r ${accuracyColor} transition-all duration-1000`}
                                    style={{ width: `${accuracy}%` }}
                                />
                            </div>
                            <p className="text-[10px] text-zinc-600 mt-1.5 text-right">
                                {user.totalTasksCompleted} перемог / {totalAttempts} спроб
                            </p>
                        </div>
                    </div>

                {/* Section: active buffs */}
                <div>
                        <p className="text-[10px] font-black text-zinc-500 uppercase tracking-widest mb-3 flex items-center gap-1.5">
                            <Sparkles size={11} /> Активні чари
                        </p>

                        {(!user?.hasActiveShield && !(user?.xpBuffEndsAt && parseBuffDate(user.xpBuffEndsAt) > new Date()) && !(user?.goldBuffEndsAt && parseBuffDate(user.goldBuffEndsAt) > new Date()) && !(user?.energyStasisEndsAt && parseBuffDate(user.energyStasisEndsAt) > new Date())) ? (
                            <div className="p-4 rounded-2xl bg-zinc-950 border border-zinc-800 text-center text-zinc-500 font-bold">
                                Зараз на вас не діють жодні чари
                            </div>
                        ) : (
                            <div className="flex flex-col gap-2">
                                {user?.hasActiveShield && (
                                    <div className="flex items-center gap-3 border rounded-xl px-4 py-3 border-blue-500/30 bg-blue-500/5">
                                        <div className="w-8 h-8 rounded-lg bg-zinc-900/60 flex items-center justify-center flex-shrink-0">
                                            <Shield size={18} className="text-blue-400" />
                                        </div>
                                        <div className="min-w-0">
                                            <p className="text-sm font-bold text-white leading-tight">Аура безстрашності</p>
                                            <p className="text-[11px] text-zinc-500 truncate">Щит від поразки</p>
                                        </div>
                                    </div>
                                )}
                                {user?.xpBuffEndsAt && parseBuffDate(user.xpBuffEndsAt) > new Date() && (
                                    <div className="flex items-center gap-3 border rounded-xl px-4 py-3 border-purple-500/30 bg-purple-500/5">
                                        <div className="w-8 h-8 rounded-lg bg-zinc-900/60 flex items-center justify-center flex-shrink-0">
                                            <FlaskConical size={18} className="text-purple-400" />
                                        </div>
                                        <div className="min-w-0">
                                            <p className="text-sm font-bold text-white leading-tight">Еліксир Мудрості</p>
                                            <p className="text-[11px] text-zinc-500 truncate">XP x1.5 до {formatDate(user.xpBuffEndsAt)}</p>
                                        </div>
                                    </div>
                                )}
                                {user?.goldBuffEndsAt && parseBuffDate(user.goldBuffEndsAt) > new Date() && (
                                    <div className="flex items-center gap-3 border rounded-xl px-4 py-3 border-yellow-500/30 bg-yellow-500/5">
                                        <div className="w-8 h-8 rounded-lg bg-zinc-900/60 flex items-center justify-center flex-shrink-0">
                                            <Magnet size={18} className="text-yellow-400" />
                                        </div>
                                        <div className="min-w-0">
                                            <p className="text-sm font-bold text-white leading-tight">Магніт Гобліна</p>
                                            <p className="text-[11px] text-zinc-500 truncate">Золото x2 до {formatDate(user.goldBuffEndsAt)}</p>
                                        </div>
                                    </div>
                                )}
                                {user?.energyStasisEndsAt && parseBuffDate(user.energyStasisEndsAt) > new Date() && (
                                    <div className="flex items-center gap-3 border rounded-xl px-4 py-3 border-amber-500/30 bg-amber-500/5">
                                        <div className="w-8 h-8 rounded-lg bg-zinc-900/60 flex items-center justify-center flex-shrink-0">
                                            <Coffee size={18} className="text-amber-400" />
                                        </div>
                                        <div className="min-w-0">
                                            <p className="text-sm font-bold text-white leading-tight">Кава Магістра</p>
                                            <p className="text-[11px] text-zinc-500 truncate">Енергія не витрачається до {formatDate(user.energyStasisEndsAt)}</p>
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>

                {/* Section: engagement & streaks */}
                <div>
                        <p className="text-[10px] font-black text-zinc-500 uppercase tracking-widest mb-3 flex items-center gap-1.5">
                            <Trophy size={11} /> Залученість та Серії
                        </p>

                        <div className="grid grid-cols-2 gap-2.5">
                            <div className="bg-zinc-950 border border-amber-500/20 rounded-2xl p-4 col-span-2 hover:border-amber-500/40 transition-colors">
                                <div className="flex items-center gap-2 mb-2">
                                    <Flame size={18} className="text-amber-400" />
                                    <span className="text-[10px] font-bold text-zinc-500 uppercase tracking-wide">Бездоганна серія</span>
                                </div>
                                <p className="text-2xl font-black text-white">
                                    {user.currentFlawlessStreak ?? 0}
                                    <span className="text-sm font-bold text-zinc-500 ml-2">/ {user.longestFlawlessStreak ?? 0} рекорд</span>
                                </p>
                                <p className="text-[11px] text-zinc-500 mt-1">Поточна безперервна серія правильних відповідей · кращий результат за весь час</p>
                            </div>

                            <div className="bg-zinc-950 border border-zinc-800 rounded-2xl p-4 text-center hover:border-blue-500/40 transition-colors">
                                <div className="flex justify-center mb-1.5"><LogIn size={22} className="text-blue-400" /></div>
                                <p className="text-[10px] text-zinc-500 font-bold uppercase tracking-wide mb-1">Серія входів</p>
                                <p className="text-2xl font-black text-white">{user.longestLoginStreak ?? 0}</p>
                                <p className="text-[10px] text-zinc-600 mt-0.5">днів поспіль</p>
                            </div>

                            <div className="bg-zinc-950 border border-zinc-800 rounded-2xl p-4 text-center hover:border-emerald-500/40 transition-colors">
                                <div className="flex justify-center mb-1.5"><CalendarDays size={22} className="text-emerald-400" /></div>
                                <p className="text-[10px] text-zinc-500 font-bold uppercase tracking-wide mb-1">Активних днів</p>
                                <p className="text-2xl font-black text-white">{user.totalLoginDays ?? 0}</p>
                                <p className="text-[10px] text-zinc-600 mt-0.5">унікальних входів</p>
                            </div>

                            <div className="bg-zinc-950 border border-zinc-800 rounded-2xl p-4 col-span-2 hover:border-purple-500/40 transition-colors">
                                <div className="flex items-center justify-between">
                                    <div className="flex items-center gap-2">
                                        <Clock size={18} className="text-purple-400" />
                                        <span className="text-[10px] font-bold text-zinc-500 uppercase tracking-wide">Загальний час гри</span>
                                    </div>
                                    <p className="text-xl font-black text-white">{formatPlayTime(user.totalPlayTimeSeconds ?? 0)}</p>
                                </div>
                            </div>
                        </div>
                    </div>

                {/* Section: activity log */}
                <div>
                        <p className="text-[10px] font-black text-zinc-500 uppercase tracking-widest mb-3 flex items-center gap-1.5">
                            <CalendarDays size={11} /> Журнал активності
                        </p>

                        <div className="bg-zinc-950 border border-zinc-800 rounded-2xl px-4 py-4 space-y-2.5">
                            <div className="flex items-start justify-between gap-2">
                                <span className="text-xs text-zinc-500 flex-shrink-0">Останній візит до Табору</span>
                                <span className="text-xs font-bold text-zinc-300 text-right">
                                    {formatDate(user.lastLoginDate)}
                                </span>
                            </div>
                            <div className="w-full h-px bg-zinc-800" />
                            <div className="flex items-start justify-between gap-2">
                                <span className="text-xs text-zinc-500 flex-shrink-0">Останнє завершене випробування</span>
                                <span className="text-xs font-bold text-zinc-300 text-right">
                                    {formatDate(user.lastTaskCompletionDate)}
                                </span>
                            </div>
                        </div>
                    </div>

                </div>

                {/* --- Кнопка закриття --- */}
                <div className="px-5 pb-5 pt-2 flex-shrink-0 border-t border-zinc-800 bg-zinc-900">
                    <button
                        onClick={onClose}
                        className="w-full py-3 rounded-2xl font-black text-base bg-zinc-800 text-zinc-200 hover:bg-zinc-700 transition-colors mt-3"
                    >
                        Закрити літопис
                    </button>
                </div>

            </div>
        </div>
    );
};

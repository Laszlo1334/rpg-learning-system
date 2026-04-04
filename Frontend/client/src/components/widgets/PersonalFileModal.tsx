import { useEffect, useState } from 'react';
import { authService } from '@/services/authService';
import type { UserStatsDto } from '@/types';
import { X, Target, Skull, Coins, Gem } from 'lucide-react';

interface PersonalFileModalProps {
    isOpen: boolean;
    onClose: () => void;
}

export const PersonalFileModal = ({ isOpen, onClose }: PersonalFileModalProps) => {
    // Стан для збереження статистики та статусу завантаження
    const [stats, setStats] = useState<UserStatsDto | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    // Цей код спрацьовує щоразу, коли isOpen стає true (вікно відкривається)
    useEffect(() => {
        if (isOpen) {
            setIsLoading(true);
            const fetchStats = async () => {
                try {
                    const data = await authService.getUserStats();
                    setStats(data);
                } catch (error) {
                    console.error('Помилка завантаження статистики:', error);
                } finally {
                    setIsLoading(false);
                }
            };
            fetchStats();
        }
    }, [isOpen]);

    // Якщо вікно закрите, взагалі нічого не малюємо на екрані
    if (!isOpen) return null;

    return (
        // Темний фон, який перекриває весь екран
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
            {/* Саме вікно */}
            <div className="bg-zinc-900 border border-zinc-800 rounded-3xl w-full max-w-md overflow-hidden shadow-2xl relative">

                {/* Шапка вікна з кнопкою закриття */}
                <div className="flex justify-between items-center p-6 border-b border-zinc-800 bg-zinc-950/50">
                    <h3 className="text-xl font-black text-white">Літопис Гравця</h3>
                    <button onClick={onClose} className="text-zinc-500 hover:text-white transition-colors">
                        <X size={24} />
                    </button>
                </div>

                {/* Тіло вікна */}
                <div className="p-6">
                    {isLoading ? (
                        <div className="text-center text-zinc-500 py-10 animate-pulse">
                            Гортаємо сторінки літопису...
                        </div>
                    ) : stats ? (
                        <div className="grid grid-cols-2 gap-4">
                            {/* Картка 1: Пройдені квести */}
                            <div className="bg-zinc-950 border border-zinc-800 rounded-2xl p-4 text-center">
                                <Target size={28} className="text-blue-400 mx-auto mb-2" />
                                <p className="text-xs text-zinc-500 font-bold uppercase">Пройдено завдань</p>
                                <p className="text-2xl font-black text-white">{stats.totalTasksCompleted}</p>
                            </div>

                            {/* Картка 2: Помилки (Продуктивна невдача) */}
                            <div className="bg-zinc-950 border border-zinc-800 rounded-2xl p-4 text-center">
                                <Skull size={28} className="text-red-400 mx-auto mb-2" />
                                <p className="text-xs text-zinc-500 font-bold uppercase">Продуктивних помилок</p>
                                <p className="text-2xl font-black text-white">{stats.totalFailures}</p>
                            </div>

                            {/* Картка 3: Загальне золото */}
                            <div className="bg-zinc-950 border border-zinc-800 rounded-2xl p-4 text-center">
                                <Coins size={28} className="text-yellow-400 mx-auto mb-2" />
                                <p className="text-xs text-zinc-500 font-bold uppercase">Всього золота</p>
                                <p className="text-2xl font-black text-white">{stats.lifetimeGold}</p>
                            </div>

                            {/* Картка 4: Загальні кристали */}
                            <div className="bg-zinc-950 border border-zinc-800 rounded-2xl p-4 text-center">
                                <Gem size={28} className="text-purple-400 mx-auto mb-2" />
                                <p className="text-xs text-zinc-500 font-bold uppercase">Всього кристалів</p>
                                <p className="text-2xl font-black text-white">{stats.lifetimeCrystals}</p>
                            </div>
                        </div>
                    ) : (
                        <div className="text-center text-red-400 py-10">Не вдалося завантажити дані.</div>
                    )}
                </div>
            </div>
        </div>
    );
};
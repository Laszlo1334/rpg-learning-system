// src/pages/LeaderboardPage.tsx
import { useState, useEffect } from 'react';
import { useAuthStore } from '@/store/authStore';
import { leaderboardService } from '@/services/leaderboardService';
import { courseService } from '@/services/courseService';
import type { LeaderboardDto, CourseLeaderboardDto, CourseProgressDto } from '@/types';
import { Trophy, Medal, Eye, EyeOff, Crown, Gem, Star, Loader2, Globe, BookOpen, Coins } from 'lucide-react';

type Tab = 'global' | 'course';

export const LeaderboardPage = () => {
    const { user, refreshUser } = useAuthStore();

    // Privacy toggle — initialise from the store (default: public)
    const [isPublic, setIsPublic] = useState<boolean>(user?.isPublicProfile ?? true);
    const [isPrivacyUpdating, setIsPrivacyUpdating] = useState(false);

    // Tabs and data
    const [activeTab, setActiveTab] = useState<Tab>('global');
    const [sortBy, setSortBy] = useState<'xp' | 'gold'>('xp');
    const [globalData, setGlobalData] = useState<LeaderboardDto[]>([]);
    const [courseData, setCourseData] = useState<CourseLeaderboardDto[]>([]);
    const [isLoading, setIsLoading] = useState(false);

    // Dynamic course list
    const [courses, setCourses] = useState<CourseProgressDto[]>([]);
    const [selectedCourseId, setSelectedCourseId] = useState<number | null>(null);

    // Load global leaderboard and course list on mount
    useEffect(() => {
        if (!isPublic) return;

        const load = async () => {
            setIsLoading(true);
            try {
                const [globalResult, coursesResult] = await Promise.all([
                    leaderboardService.getGlobalLeaderboard(sortBy),
                    courseService.getAllCourses(),
                ]);
                setGlobalData(globalResult);
                setCourses(coursesResult);
                if (coursesResult.length > 0 && selectedCourseId === null) {
                    setSelectedCourseId(coursesResult[0].id);
                }
            } catch (err) {
                console.error('[LeaderboardPage] Failed to load leaderboard:', err);
            } finally {
                setIsLoading(false);
            }
        };

        load();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isPublic, sortBy]);

    // Load course leaderboard when tab or selected course changes
    useEffect(() => {
        if (!isPublic || activeTab !== 'course' || selectedCourseId === null) return;

        const loadCourse = async () => {
            setIsLoading(true);
            try {
                const data = await leaderboardService.getCourseLeaderboard(selectedCourseId);
                setCourseData(data);
            } catch (err) {
                console.error('[LeaderboardPage] Failed to load course leaderboard:', err);
            } finally {
                setIsLoading(false);
            }
        };

        loadCourse();
    }, [activeTab, selectedCourseId, isPublic]);

    // Privacy toggle handler
    const handlePrivacyToggle = async () => {
        const newValue = !isPublic;
        setIsPrivacyUpdating(true);
        try {
            await leaderboardService.updatePrivacy(newValue);
            setIsPublic(newValue);
            await refreshUser(); // Sync global auth store
        } catch (err) {
            console.error('[LeaderboardPage] Failed to update privacy setting:', err);
        } finally {
            setIsPrivacyUpdating(false);
        }
    };

    // Returns a medal icon for positions 1–3, or a plain rank number for the rest
    const getMedalIcon = (index: number) => {
        if (index === 0) return <Crown size={20} className="text-yellow-400" />;
        if (index === 1) return <Medal size={20} className="text-zinc-400" />;
        if (index === 2) return <Medal size={20} className="text-amber-600" />;
        return <span className="text-zinc-500 font-bold text-sm w-5 text-center">{index + 1}</span>;
    };

    return (
        <div className="p-6 max-w-3xl mx-auto space-y-6">

            {/* --- Page header --- */}
            <div className="flex items-center gap-3">
                <div className="w-12 h-12 bg-yellow-500/10 rounded-2xl flex items-center justify-center">
                    <Trophy size={28} className="text-yellow-400" />
                </div>
                <div>
                    <h1 className="text-2xl font-black text-white">Зал Слави</h1>
                    <p className="text-zinc-500 text-sm">Рейтинг найкращих гравців Академії</p>
                </div>
            </div>

            {/* --- Privacy toggle --- */}
            <div className="bg-zinc-900 border border-zinc-800 rounded-2xl p-4 flex items-center justify-between gap-4">
                <div className="flex items-center gap-3">
                    <div className={`w-10 h-10 rounded-xl flex items-center justify-center transition-colors ${isPublic ? 'bg-purple-500/10' : 'bg-zinc-800'}`}>
                        {isPublic ? <Eye size={20} className="text-purple-400" /> : <EyeOff size={20} className="text-zinc-500" />}
                    </div>
                    <div>
                        <p className="font-bold text-white text-sm">Участь у таблиці лідерів</p>
                        <p className="text-zinc-500 text-xs">
                            {isPublic ? 'Ваш профіль видно в рейтингу' : 'Ваш профіль приховано від інших гравців'}
                        </p>
                    </div>
                </div>

                {/* Custom toggle */}
                <button
                    onClick={handlePrivacyToggle}
                    disabled={isPrivacyUpdating}
                    className={`relative w-14 h-7 rounded-full transition-all duration-300 focus:outline-none disabled:opacity-50 ${isPublic ? 'bg-purple-600' : 'bg-zinc-700'
                        }`}
                    title={isPublic ? 'Hide profile' : 'Show profile'}
                >
                    <span className={`absolute top-0.5 w-6 h-6 bg-white rounded-full shadow-md transition-all duration-300 ${isPublic ? 'left-7' : 'left-0.5'
                        }`} />
                    {isPrivacyUpdating && (
                        <Loader2 size={14} className="absolute inset-0 m-auto text-white animate-spin" />
                    )}
                </button>
            </div>

            {/* --- Hidden profile mode --- */}
            {!isPublic ? (
                <div className="bg-zinc-900 border border-zinc-800 rounded-2xl p-12 flex flex-col items-center justify-center text-center gap-4">
                    <div className="w-20 h-20 bg-zinc-800 rounded-full flex items-center justify-center">
                        <EyeOff size={36} className="text-zinc-600" />
                    </div>
                    <h2 className="text-xl font-black text-white">Ви навчаєтесь у власному темпі</h2>
                    <p className="text-zinc-400 text-sm max-w-sm">
                        Ваш профіль приховано від інших гравців. Увімкніть участь у таблиці лідерів, щоб побачити Зал Слави.
                    </p>
                </div>
            ) : (
                <>
                    {/* --- Tabs --- */}
                    <div className="flex bg-zinc-900 border border-zinc-800 rounded-2xl p-1 gap-1">
                        <button
                            onClick={() => setActiveTab('global')}
                            className={`flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl font-bold text-sm transition-all ${activeTab === 'global'
                                    ? 'bg-zinc-800 text-white shadow-sm'
                                    : 'text-zinc-500 hover:text-zinc-300'
                                }`}
                        >
                            <Globe size={16} />
                            Загальний рейтинг
                        </button>
                        <button
                            onClick={() => setActiveTab('course')}
                            className={`flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl font-bold text-sm transition-all ${activeTab === 'course'
                                    ? 'bg-zinc-800 text-white shadow-sm'
                                    : 'text-zinc-500 hover:text-zinc-300'
                                }`}
                        >
                            <BookOpen size={16} />
                            Рейтинг курсів
                        </button>
                    </div>

                    {/* --- Sort by toggle (only for global) --- */}
                    {activeTab === 'global' && (
                        <div className="flex bg-zinc-900 border border-zinc-800 rounded-xl p-1 gap-1 w-full max-w-xs ml-auto mb-2">
                            <button
                                onClick={() => setSortBy('xp')}
                                className={`flex-1 py-1.5 rounded-lg font-bold text-xs transition-all ${sortBy === 'xp' ? 'bg-zinc-800 text-white shadow-sm' : 'text-zinc-500 hover:text-zinc-300'
                                    }`}
                            >
                                За досвідом
                            </button>
                            <button
                                onClick={() => setSortBy('gold')}
                                className={`flex-1 py-1.5 rounded-lg font-bold text-xs transition-all ${sortBy === 'gold' ? 'bg-zinc-800 text-white shadow-sm' : 'text-zinc-500 hover:text-zinc-300'
                                    }`}
                            >
                                За золотом
                            </button>
                        </div>
                    )}

                    {/* --- Course selector --- */}
                    {activeTab === 'course' && courses.length > 0 && (
                        <div className="mb-6 flex justify-end">
                            <select
                                value={selectedCourseId || ''}
                                onChange={(e) => setSelectedCourseId(Number(e.target.value))}
                                className="bg-zinc-900 border border-zinc-700 text-zinc-300 rounded-xl px-4 py-2 outline-none focus:border-purple-500 font-bold"
                            >
                                {courses.map(course => (
                                    <option key={course.id} value={course.id}>{course.title}</option>
                                ))}
                            </select>
                        </div>
                    )}

                    {/* --- Leaderboard table --- */}
                    <div className="bg-zinc-900 border border-zinc-800 rounded-2xl overflow-hidden">
                        {isLoading ? (
                            <div className="flex items-center justify-center py-16 gap-3 text-zinc-500">
                                <Loader2 size={24} className="animate-spin" />
                                <span className="font-bold">Завантаження рейтингу...</span>
                            </div>
                        ) : activeTab === 'global' ? (
                            globalData.length === 0 ? (
                                <div className="py-16 text-center text-zinc-500 font-bold">Ще немає гравців 👤</div>
                            ) : (
                                <ul className="divide-y divide-zinc-800">
                                    {globalData.map((player, index) => (
                                        <li
                                            key={player.id}
                                            className={`flex items-center gap-4 px-5 py-4 transition-colors hover:bg-zinc-800/50 ${player.username === user?.username ? 'bg-purple-500/5 border-l-2 border-purple-500' : ''
                                                }`}
                                        >
                                            {/* Medal / rank number */}
                                            <div className="w-7 flex justify-center flex-shrink-0">
                                                {getMedalIcon(index)}
                                            </div>

                                            <img
                                                src={player.avatarUrl || '/assets/default_avatar.png'}
                                                alt={`${player.username} avatar`}
                                                className="w-10 h-10 rounded-xl object-contain [image-rendering:pixelated] bg-zinc-800 border border-zinc-700 flex-shrink-0"
                                                onError={(e) => { e.currentTarget.src = '/assets/default_avatar.png'; }}
                                            />

                                            {/* Name + level */}
                                            <div className="flex-1 min-w-0">
                                                <p className={`font-bold truncate ${player.username === user?.username ? 'text-purple-400' : 'text-white'}`}>
                                                    {player.username}
                                                    {player.username === user?.username && (
                                                        <span className="ml-2 text-xs text-purple-500 font-normal">(you)</span>
                                                    )}
                                                </p>
                                                <p className="text-xs text-zinc-500 flex items-center gap-1 mt-0.5">
                                                    <Star size={11} /> Level {player.level}
                                                </p>
                                            </div>

                                            {/* Score */}
                                            {sortBy === 'xp' ? (
                                                <div className="flex items-center gap-1.5 text-blue-400 font-black text-sm bg-blue-500/10 px-3 py-1.5 rounded-lg whitespace-nowrap">
                                                    <Gem size={14} />
                                                    {player.xp.toLocaleString()} XP
                                                </div>
                                            ) : (
                                                <div className="flex items-center gap-1.5 text-yellow-500 font-black text-sm bg-yellow-500/10 px-3 py-1.5 rounded-lg whitespace-nowrap">
                                                    <Coins size={14} />
                                                    {(player.lifetimeGold || 0).toLocaleString()} Золота
                                                </div>
                                            )}
                                        </li>
                                    ))}
                                </ul>
                            )
                        ) : (
                            courseData.length === 0 ? (
                                <div className="py-16 text-center text-zinc-500 font-bold">Ще немає гравців 👤</div>
                            ) : (
                                <ul className="divide-y divide-zinc-800">
                                    {courseData.map((player, index) => (
                                        <li
                                            key={player.userId}
                                            className={`flex items-center gap-4 px-5 py-4 transition-colors hover:bg-zinc-800/50 ${player.username === user?.username ? 'bg-purple-500/5 border-l-2 border-purple-500' : ''
                                                }`}
                                        >
                                            <div className="w-7 flex justify-center flex-shrink-0">
                                                {getMedalIcon(index)}
                                            </div>
                                            <img
                                                src={player.avatarUrl || '/assets/default_avatar.png'}
                                                alt={`${player.username} avatar`}
                                                className="w-10 h-10 rounded-xl object-contain [image-rendering:pixelated] bg-zinc-800 border border-zinc-700 flex-shrink-0"
                                                onError={(e) => { e.currentTarget.src = '/assets/default_avatar.png'; }}
                                            />
                                            <div className="flex-1 min-w-0">
                                                <p className={`font-bold truncate ${player.username === user?.username ? 'text-purple-400' : 'text-white'}`}>
                                                    {player.username}
                                                    {player.username === user?.username && (
                                                        <span className="ml-2 text-xs text-purple-500 font-normal">Ти</span>
                                                    )}
                                                </p>
                                            </div>
                                            <div className="flex items-center gap-1.5 text-yellow-400 font-black text-sm bg-yellow-500/10 px-3 py-1.5 rounded-lg">
                                                <Trophy size={14} />
                                                {player.courseXp.toLocaleString()} Досвід за курс
                                            </div>
                                        </li>
                                    ))}
                                </ul>
                            )
                        )}
                    </div>
                </>
            )}
        </div>
    );
};

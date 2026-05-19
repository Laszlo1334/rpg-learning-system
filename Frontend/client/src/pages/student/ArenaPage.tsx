import { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import confetti from 'canvas-confetti';
import { taskService } from '@/services/taskService';
import { arenaService } from '@/services/arenaService';
import { authService } from '@/services/authService';
import { useAuthStore } from '@/store/authStore';
import type { TaskDto, AnswerResponse, RunCompletionRequest, RunCompletionResponse } from '@/types';
import { Heart, Gem, Flag, ChevronRight, ShieldAlert, Sparkles, Skull, Clock, Flame, EyeOff, Shield, BookOpen, Coins } from 'lucide-react';

export const ArenaPage = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { user, refreshUser } = useAuthStore();

    const [task, setTask] = useState<TaskDto | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    // Controls whether the theory panel is visible; synced after the task loads
    const [isTheoryVisible, setIsTheoryVisible] = useState(true);

    const [currentIndex, setCurrentIndex] = useState(0);
    const [hearts, setHearts] = useState(3);
    const [earnedCrystals, setEarnedCrystals] = useState(0);
    const [failedQuestionIds, setFailedQuestionIds] = useState<number[]>([]);
    const [attemptsTaken, setAttemptsTaken] = useState(1);
    const [hintsUsed, setHintsUsed] = useState(false);
    const runStartTimeRef = useRef<number | null>(null);

    const [isChecking, setIsChecking] = useState(false);
    const [feedback, setFeedback] = useState<AnswerResponse | null>(null);
    const [runStatus, setRunStatus] = useState<'playing' | 'victory' | 'defeat' | 'timeout' | 'cheated'>('playing');
    const [selectedOption, setSelectedOption] = useState<string | null>(null);
    const [showNextButton, setShowNextButton] = useState(false);

    const [timeLeft, setTimeLeft] = useState<number | null>(null);
    const [isShaking, setIsShaking] = useState(false);
    const [isSuccess, setIsSuccess] = useState(false);
    const [runResult, setRunResult] = useState<RunCompletionResponse | null>(null);

    // Inline arena toast (avoids external library dependency)
    const [arenaToast, setArenaToast] = useState<{ message: string; type: 'shield' | 'crystal' | 'error' } | null>(null);

    const showArenaToast = useCallback((message: string, type: 'shield' | 'crystal' | 'error') => {
        setArenaToast({ message, type });
        setTimeout(() => setArenaToast(null), 3500);
    }, []);

    useEffect(() => {
        const fetchTask = async () => {
            try {
                if (id) {
                    const data = await taskService.getTaskById(Number(id));
                    setTask(data);
                    // Hide theory by default when the task explicitly hides it
                    setIsTheoryVisible(!data.isTheoryHidden);

                    if (data.type === 'BOSS' && data.bossMetadata?.timeLimitSeconds) {
                        setTimeLeft(data.bossMetadata.timeLimitSeconds);
                    }
                    runStartTimeRef.current = Date.now();
                }
            } catch (error) {
                navigate('/courses', { state: { error: 'Завдання заблоковано' } });
            } finally {
                setIsLoading(false);
            }
        };
        fetchTask();
    }, [id, navigate]);

    const isBoss = task?.type === 'BOSS';

    useEffect(() => {
        if (!isBoss || runStatus !== 'playing') return;
        const handleVisibilityChange = () => {
            if (document.hidden) handleFinishRun(false, 'cheated');
        };
        document.addEventListener('visibilitychange', handleVisibilityChange);
        return () => document.removeEventListener('visibilitychange', handleVisibilityChange);
    }, [isBoss, runStatus]);

    useEffect(() => {
        if (timeLeft === null || runStatus !== 'playing') return;
        if (timeLeft <= 0) {
            handleFinishRun(false, 'timeout');
            return;
        }
        const timerId = setInterval(() => {
            setTimeLeft(prev => (prev !== null ? prev - 1 : null));
        }, 1000);
        return () => clearInterval(timerId);
    }, [timeLeft, runStatus]);

    const currentQuestion = task?.questions?.[currentIndex];

    const formatTime = (seconds: number) => {
        const m = Math.floor(seconds / 60);
        const s = seconds % 60;
        return `${m}:${s.toString().padStart(2, '0')}`;
    };

    const handleAnswer = async (answer: string) => {
        if (!currentQuestion || isChecking || runStatus !== 'playing' || showNextButton) return;

        setIsChecking(true);
        setSelectedOption(answer);
        setFeedback(null);

        try {
            const result = await arenaService.checkAnswer({
                questionId: currentQuestion.id,
                userAnswer: answer,
            });

            setFeedback(result);
            setShowNextButton(true);

            if (result.isCorrect) {
                // ── Correct answer ────────────────────────────────────────
                setIsSuccess(true);
            } else {
                // ── Wrong answer ──────────────────────────────────────────
                setAttemptsTaken(prev => prev + 1);
                setIsShaking(true);
                setTimeout(() => setIsShaking(false), 500);

                // STEP 1: Check if Rune of Protection (shield) is active
                if (user?.hasActiveShield) {
                    showArenaToast('🛡️ Rune of Protection absorbed the blow! No life lost.', 'shield');
                    // Burn the shield on the backend in background (non-blocking UX)
                    authService.consumeShield()
                        .then(() => refreshUser())
                        .catch(err => console.error('[Shield] consume failed:', err));
                    // Hearts stay the same — skip setHearts
                } else {
                    // STEP 2: No shield — deduct a heart
                    setHearts(prev => prev - 1);
                }

                // STEP 3: Productive failure crystal reward (first miss only)
                if (!failedQuestionIds.includes(currentQuestion.id)) {
                    setFailedQuestionIds(prev => [...prev, currentQuestion.id]);
                    // Backend already awards crystals via SubmissionService,
                    // but we reflect the response amount in the local counter
                    if (result.crystalsAwarded && result.crystalsAwarded > 0) {
                        setEarnedCrystals(prev => prev + result.crystalsAwarded!);
                        showArenaToast(`Wrong answer — but you earned +${result.crystalsAwarded} 💎 (Productive Failure)!`, 'crystal');
                    } else {
                        showArenaToast('Wrong answer. Keep trying!', 'error');
                    }
                } else {
                    // Repeat mistake — no crystals, no extra toast
                    showArenaToast('Wrong again. Study the theory carefully!', 'error');
                }
            }
        } catch (error) {
            console.error('[ArenaPage] checkAnswer error:', error);
        } finally {
            setIsChecking(false);
        }
    };

    const handleNextStep = () => {
        setShowNextButton(false);
        setFeedback(null);
        setSelectedOption(null);
        setIsSuccess(false);

        // Якщо втрачено останнє серце, завершуємо гру поразкою
        if (hearts <= 0) {
            handleFinishRun(false, 'defeat');
            return;
        }

        // Інакше йдемо до наступного питання або святкуємо перемогу
        if (task?.questions && currentIndex + 1 < task.questions.length) {
            setCurrentIndex(prev => prev + 1);
        } else {
            handleFinishRun(true, 'victory');
        }
    };

    const handleFinishRun = useCallback(async (isVictory: boolean, reason: 'victory' | 'defeat' | 'timeout' | 'cheated') => {
        setRunStatus(reason);

        // 🎉 Confetti — only on victory, right after showing the screen
        if (isVictory) {
            // Left burst
            confetti({
                particleCount: 80,
                angle: 60,
                spread: 55,
                origin: { x: 0, y: 0.65 },
                colors: ['#a855f7', '#3b82f6', '#facc15', '#34d399'],
            });
            // Right burst
            confetti({
                particleCount: 80,
                angle: 120,
                spread: 55,
                origin: { x: 1, y: 0.65 },
                colors: ['#a855f7', '#3b82f6', '#facc15', '#34d399'],
            });
        }

        if (task) {
            try {
                const timeSpentSeconds = runStartTimeRef.current
                    ? Math.floor((Date.now() - runStartTimeRef.current) / 1000)
                    : 0;
                const payload: RunCompletionRequest = {
                    taskId: task.id,
                    isVictory,
                    failedQuestionIds,
                    attemptsTaken,
                    hintsUsed,
                    timeSpentSeconds,
                };
                console.log('[ArenaPage] Sending finishRun:', payload);
                const response = await arenaService.finishRun(payload);
                if (response) {
                    setRunResult(response);
                }
            } catch (error) {
                console.error('[ArenaPage] finishRun error:', error);
            }
        }
        // Update player profile (XP, Gold, Crystals) after run completion
        await refreshUser();
    }, [task, failedQuestionIds, attemptsTaken, hintsUsed, refreshUser]);

    if (isLoading) return <div className="min-h-screen bg-zinc-950 flex items-center justify-center text-zinc-500 font-bold">Preparing Arena...</div>;
    if (!task || !currentQuestion) return <div className="min-h-screen bg-zinc-950 p-8 text-center text-red-400">Task not found.</div>;

    const bgClasses = isBoss
        ? "bg-gradient-to-b from-red-950/40 via-zinc-950 to-zinc-950 border-red-900/30"
        : "bg-zinc-950 border-zinc-800";

    return (
        <>

            {/* ── Arena toast notification ──────────────────────────────── */}
            {arenaToast && (
                <div className={`fixed top-20 left-1/2 -translate-x-1/2 z-50 flex items-center gap-3 px-5 py-3 rounded-2xl font-bold text-sm shadow-2xl transition-all ${arenaToast.type === 'shield'
                    ? 'bg-blue-900 border border-blue-600 text-blue-200'
                    : arenaToast.type === 'crystal'
                        ? 'bg-purple-900 border border-purple-600 text-purple-200'
                        : 'bg-red-900 border border-red-700 text-red-200'
                    }`}>
                    {arenaToast.type === 'shield' && <Shield size={18} />}
                    {arenaToast.type === 'crystal' && <Gem size={18} />}
                    {arenaToast.message}
                </div>
            )}

            <div className={`min-h-screen text-white flex flex-col transition-transform ${isShaking ? 'animate-shake' : ''} ${bgClasses} ${isBoss ? 'animate-boss-aura relative overflow-hidden' : ''}`}>

                {isBoss && (
                    <>
                        <div className="absolute top-0 left-1/4 w-96 h-96 bg-red-600/10 rounded-full blur-[120px] pointer-events-none" />
                        <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-orange-600/5 rounded-full blur-[100px] pointer-events-none" />
                    </>
                )}

                <header className={`p-4 flex items-center justify-between sticky top-0 z-10 border-b backdrop-blur-md ${isBoss ? 'bg-red-950/20 border-red-900/30' : 'bg-zinc-900/90 border-zinc-800'}`}>
                    <button onClick={() => navigate(task?.courseId ? `/courses/${task.courseId}/foyer` : '/courses')} className="flex items-center gap-2 text-zinc-400 hover:text-white transition-colors font-bold">
                        <Flag size={20} /> Retreat to Map
                    </button>
                    <div className="flex items-center gap-6 md:gap-8">

                        {/* Theory toggle button — hidden during boss runs */}
                        {!isBoss && (
                            <button
                                onClick={() => {
                                    if (!isTheoryVisible) setHintsUsed(true);
                                    setIsTheoryVisible(v => !v);
                                }}
                                className="flex items-center gap-2 px-3 py-1.5 bg-zinc-800 hover:bg-zinc-700 text-zinc-300 rounded-lg transition-colors border border-zinc-700 text-sm font-bold"
                            >
                                {isTheoryVisible ? <EyeOff size={16} /> : <BookOpen size={16} />}
                                <span>{isTheoryVisible ? 'Hide Theory' : 'Show Theory'}</span>
                            </button>
                        )}

                        {timeLeft !== null && (
                            <div className={`flex items-center gap-2 font-black text-lg ${timeLeft <= 10 ? 'text-red-500 animate-pulse' : 'text-orange-400'}`}>
                                <Clock size={20} /> {formatTime(timeLeft)}
                            </div>
                        )}

                        <div className="flex items-center gap-2">
                            {[1, 2, 3].map((h) => {
                                const isLost = h > hearts;
                                const heartClass = isLost
                                    ? 'animate-heart-break fill-zinc-800 text-zinc-700'
                                    : (isBoss ? 'fill-red-600 text-red-600 drop-shadow-[0_0_8px_rgba(220,38,38,0.8)]' : 'fill-red-500 text-red-500');
                                return (
                                    <Heart
                                        key={h}
                                        size={24}
                                        className={`transition-all duration-300 ${heartClass}`}
                                    />
                                );
                            })}
                        </div>
                        <div className="flex items-center gap-2 text-purple-400 font-bold bg-purple-500/10 px-3 py-1 rounded-lg">
                            <Gem size={18} /> {earnedCrystals}
                        </div>
                    </div>
                </header>

                <main className="flex-1 flex flex-col w-full p-4 lg:p-6 relative z-10">

                    {/* 1. Full-width progress bar above the panels */}
                    <div className="w-full max-w-7xl mx-auto mb-6">
                        <div className={`flex items-center gap-4 text-sm font-bold tracking-wider uppercase ${isBoss ? 'text-red-500/70' : 'text-zinc-400'}`}>
                            <span className="whitespace-nowrap">Step {currentIndex + 1} of {task.questions?.length || 1}</span>
                            <div className={`flex-1 h-1.5 rounded-full overflow-hidden ${isBoss ? 'bg-red-950/50' : 'bg-zinc-800'}`}>
                                <div
                                    className={`h-full transition-all duration-500 ease-out ${isBoss ? 'bg-gradient-to-r from-orange-500 to-red-600 shadow-[0_0_10px_rgba(220,38,38,0.5)]' : 'bg-purple-600'}`}
                                    style={{ width: `${((currentIndex) / (task.questions?.length || 1)) * 100}%` }}
                                />
                            </div>
                        </div>
                    </div>

                    {/* 2. Split panels container — Theory (left) + Questions (right) */}
                    <div className="flex flex-col lg:flex-row gap-6 w-full max-w-7xl mx-auto transition-all duration-500">

                        {/* Theory panel — collapses smoothly when hidden */}
                        <div className={`transition-all duration-500 overflow-hidden ${isTheoryVisible && !isBoss ? 'lg:w-1/2 opacity-100' : 'w-0 opacity-0 p-0'}`}>
                            <div className={`border rounded-3xl p-6 overflow-y-auto max-h-[80vh] h-full ${isBoss ? 'bg-zinc-950/60 border-red-900/30' : 'bg-zinc-900 border-zinc-800'}`}>
                                {isBoss ? (
                                    <div className="flex items-center gap-4 mb-6 border-b border-red-900/30 pb-4">
                                        <div className="p-3 bg-red-950 border border-red-900/50 rounded-2xl">
                                            <Skull className="text-red-500" size={32} />
                                        </div>
                                        <div>
                                            <h2 className="text-2xl font-black text-red-500">{task.bossMetadata?.bossName || 'Final Challenge'}</h2>
                                            <span className="text-xs font-bold text-orange-500 uppercase tracking-widest flex items-center gap-1 mt-1"><Flame size={14} /> {task.title}</span>
                                        </div>
                                    </div>
                                ) : (
                                    <h2 className="text-2xl font-black mb-4 text-purple-400">{task.title}</h2>
                                )}
                                <div className="prose prose-invert max-w-none text-zinc-300 leading-relaxed" dangerouslySetInnerHTML={{ __html: task.theoryContent || '' }} />
                            </div>
                        </div>

                        {/* Questions panel — expands to full width when theory is hidden */}
                        <div className={`transition-all duration-500 flex flex-col ${isBoss || !isTheoryVisible ? 'w-full max-w-3xl mx-auto' : 'lg:w-1/2'}`}>

                            <div className={`border rounded-3xl p-6 md:p-8 flex-1 flex flex-col justify-center relative backdrop-blur-sm ${isBoss ? 'bg-zinc-950/80 border-red-900/30 shadow-[0_0_30px_rgba(220,38,38,0.05)]' : 'bg-zinc-900 border-zinc-800'}`}>
                                <h3 className="text-xl md:text-2xl font-bold mb-8 text-center">{currentQuestion.questionText}</h3>

                                <div className="grid grid-cols-1 gap-4">
                                    {currentQuestion.options.map((option, idx) => {
                                        const isSelected = selectedOption === option;
                                        let btnClass = isBoss
                                            ? "bg-zinc-950 border-red-900/30 hover:border-red-500 hover:bg-red-500/10 text-zinc-300"
                                            : "bg-zinc-950 border-zinc-800 hover:border-purple-500 hover:bg-purple-500/5 text-zinc-300";

                                        if (isSelected && feedback) {
                                            if (feedback.isCorrect) btnClass = "bg-green-500/20 border-green-500 text-green-400 scale-105 shadow-[0_0_20px_rgba(34,197,94,0.3)] z-10";
                                            else btnClass = "bg-red-500/20 border-red-500 text-red-400 opacity-50";
                                        }

                                        return (
                                            <button
                                                key={idx}
                                                disabled={isChecking || runStatus !== 'playing' || showNextButton}
                                                onClick={() => handleAnswer(option)}
                                                className={`border p-5 rounded-2xl font-bold transition-all duration-300 disabled:cursor-not-allowed flex items-center justify-center gap-3 text-lg ${btnClass}`}
                                            >
                                                {isSelected && isSuccess && <Sparkles size={20} className="animate-pulse" />}
                                                {option}
                                                {isSelected && isSuccess && <Sparkles size={20} className="animate-pulse" />}
                                            </button>
                                        );
                                    })}
                                </div>

                                {feedback && !feedback.isCorrect && feedback.explanation && (
                                    <div className="mt-6 p-4 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-400 animate-in fade-in slide-in-from-bottom-4">
                                        <p className="font-bold mb-1 flex items-center gap-2"><Flame size={18} /> Miss! Try again</p>
                                        <p className="text-sm text-zinc-300">{feedback.explanation}</p>
                                    </div>
                                )}

                                {showNextButton && (
                                    <button
                                        onClick={handleNextStep}
                                        className="mt-8 w-full py-4 rounded-2xl font-black text-xl bg-white text-black hover:bg-zinc-200 transition-all flex items-center justify-center gap-2 animate-in fade-in zoom-in duration-300"
                                    >
                                        Continue <ChevronRight size={24} />
                                    </button>
                                )}
                            </div>
                        </div>

                    </div> {/* end: split panels container */}
                </main>

                {runStatus !== 'playing' && (
                    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/90 backdrop-blur-md animate-in fade-in duration-500">
                        <div className={`border rounded-3xl p-8 max-w-sm w-full text-center text-white scale-in-center ${isBoss && runStatus !== 'victory' ? 'bg-red-950/40 border-red-900/50' : 'bg-zinc-900 border-zinc-800'}`}>
                            {runStatus === 'victory' ? (
                                <>
                                    <div className="w-20 h-20 bg-green-500/20 text-green-400 rounded-full flex items-center justify-center mx-auto mb-6">
                                        {isBoss ? <Skull size={40} /> : <Flag size={40} />}
                                    </div>
                                    <h2 className="text-3xl font-black mb-2">{isBoss ? 'Boss Defeated!' : 'Перемога!'}</h2>
                                    <p className="text-zinc-400 mb-6">Ви успішно пройшли завдання.</p>

                                    {runResult && (
                                        <div className="bg-zinc-950/50 border border-zinc-800 rounded-2xl p-4 mb-6 text-sm text-left">
                                            <div className="flex justify-between items-center py-1.5 border-b border-zinc-800/50">
                                                <span className="text-zinc-400">Базова нагорода:</span>
                                                <span className="flex items-center gap-1.5">
                                                    <span className="text-blue-400 font-bold">+{runResult.baseXp} XP</span>
                                                    <span className="text-yellow-400 font-bold flex items-center gap-0.5">+{runResult.baseGold} <Coins size={16} /></span>
                                                </span>
                                            </div>
                                            {runResult.flawlessMultiplier > 1 && (
                                                <div className="flex justify-between items-center py-1.5 border-b border-zinc-800/50">
                                                    <span className="text-purple-400">Бонус без помилок:</span>
                                                    <span className="font-bold text-purple-400">x{runResult.flawlessMultiplier.toFixed(2)}</span>
                                                </div>
                                            )}
                                            {runResult.campfireMultiplier > 1 && (
                                                <div className="flex justify-between items-center py-1.5 border-b border-zinc-800/50">
                                                    <span className="text-orange-400">Бонус багаття:</span>
                                                    <span className="font-bold text-orange-400">x{runResult.campfireMultiplier.toFixed(2)}</span>
                                                </div>
                                            )}
                                            {runResult.xpBuffMultiplier > 1 && (
                                                <div className="flex justify-between items-center py-1.5 border-b border-zinc-800/50">
                                                    <span className="text-blue-400">Баф досвіду:</span>
                                                    <span className="font-bold text-blue-400">x{runResult.xpBuffMultiplier.toFixed(2)}</span>
                                                </div>
                                            )}
                                            {runResult.goldBuffMultiplier > 1 && (
                                                <div className="flex justify-between items-center py-1.5 border-b border-zinc-800/50">
                                                    <span className="text-yellow-400">Баф золота:</span>
                                                    <span className="font-bold text-yellow-400">x{runResult.goldBuffMultiplier.toFixed(2)}</span>
                                                </div>
                                            )}
                                            {runResult.energyMultiplier > 1 && (
                                                <div className="flex justify-between items-center py-1.5">
                                                    <span className="text-green-400">Бонус відпочинку:</span>
                                                    <span className="font-bold text-green-400">x{runResult.energyMultiplier.toFixed(2)}</span>
                                                </div>
                                            )}
                                        </div>
                                    )}

                                    <div className="flex justify-center gap-4 mb-8">
                                        <div className="bg-zinc-950 px-4 py-2 rounded-xl border border-zinc-800 font-bold text-yellow-400">+{runResult ? runResult.earnedGold : task.rewardGold} <Coins size={20} className="inline ml-1 mb-1" /></div>
                                        <div className="bg-zinc-950 px-4 py-2 rounded-xl border border-zinc-800 font-bold text-blue-400">+{runResult ? runResult.earnedXp : task.rewardXp} XP</div>
                                    </div>
                                </>
                            ) : (
                                <>
                                    <div className="w-20 h-20 bg-red-500/20 text-red-400 rounded-full flex items-center justify-center mx-auto mb-6">
                                        {runStatus === 'timeout' && <Clock size={40} />}
                                        {runStatus === 'cheated' && <EyeOff size={40} />}
                                        {runStatus === 'defeat' && <ShieldAlert size={40} />}
                                    </div>
                                    <h2 className="text-3xl font-black mb-2">
                                        {runStatus === 'timeout' && 'Час вичерпано'}
                                        {runStatus === 'cheated' && 'Втрата фокусу'}
                                        {runStatus === 'defeat' && 'Поразка'}
                                    </h2>
                                    <p className="text-zinc-400 mb-6">
                                        {runStatus === 'timeout' && 'Ви не встигли вчасно.'}
                                        {runStatus === 'cheated' && 'Ви втратили концентрацію. Не перемикайтеся під час битви з Босом!'}
                                        {runStatus === 'defeat' && 'Ти припустився помилок, але це шлях до знань!'}
                                    </p>
                                    <div className="inline-flex items-center gap-2 bg-purple-500/20 px-6 py-3 rounded-xl border border-purple-500/30 font-bold text-purple-400 mb-8">
                                        Отримано: {earnedCrystals} <Gem size={20} />
                                    </div>
                                </>
                            )}
                            <button onClick={() => navigate(task?.courseId ? `/courses/${task.courseId}/foyer` : '/courses')} className={`w-full py-4 rounded-2xl font-black text-lg transition-colors flex items-center justify-center gap-2 ${isBoss && runStatus !== 'victory' ? 'bg-red-600 hover:bg-red-500 text-white' : 'bg-white text-black hover:bg-zinc-200'}`}>
                                {runStatus === 'victory' ? 'Продовжити' : 'Повернутися на карту'} <ChevronRight size={24} />
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </>
    );
};
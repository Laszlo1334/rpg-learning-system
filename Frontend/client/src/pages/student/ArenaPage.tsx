import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { taskService } from '@/services/taskService';
import { arenaService } from '@/services/arenaService';
import type { TaskDto, AnswerResponse } from '@/types';
import { Heart, Gem, Flag, ChevronRight, ShieldAlert, Sparkles, Skull, Clock, Flame, EyeOff } from 'lucide-react';

export const ArenaPage = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const [task, setTask] = useState<TaskDto | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const [currentIndex, setCurrentIndex] = useState(0);
    const [hearts, setHearts] = useState(3);
    const [earnedCrystals, setEarnedCrystals] = useState(0);
    const [failedQuestionIds, setFailedQuestionIds] = useState<number[]>([]);

    const [isChecking, setIsChecking] = useState(false);
    const [feedback, setFeedback] = useState<AnswerResponse | null>(null);
    // Додано статус 'cheated'
    const [runStatus, setRunStatus] = useState<'playing' | 'victory' | 'defeat' | 'timeout' | 'cheated'>('playing');
    const [selectedOption, setSelectedOption] = useState<string | null>(null);

    // Таймер для боса
    const [timeLeft, setTimeLeft] = useState<number | null>(null);

    // Стани для анімацій
    const [isShaking, setIsShaking] = useState(false);
    const [isSuccess, setIsSuccess] = useState(false);

    useEffect(() => {
        const fetchTask = async () => {
            try {
                if (id) {
                    const data = await taskService.getTaskById(Number(id));
                    setTask(data);

                    if (data.type === 'BOSS' && data.bossMetadata?.timeLimitSeconds) {
                        setTimeLeft(data.bossMetadata.timeLimitSeconds);
                    }
                }
            } catch (error) {
                navigate('/courses');
            } finally {
                setIsLoading(false);
            }
        };
        fetchTask();
    }, [id, navigate]);

    const isBoss = task?.type === 'BOSS';

    // Античіт: відстежуємо згортання вкладки тільки для Босів
    useEffect(() => {
        if (!isBoss || runStatus !== 'playing') return;

        const handleVisibilityChange = () => {
            if (document.hidden) {
                // Вкладку згорнуто або перемкнуто — миттєва поразка
                handleFinishRun(false, 'cheated');
            }
        };

        document.addEventListener('visibilitychange', handleVisibilityChange);
        return () => document.removeEventListener('visibilitychange', handleVisibilityChange);
    }, [isBoss, runStatus]);

    // Логіка таймера
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
        if (!currentQuestion || isChecking || runStatus !== 'playing') return;

        setIsChecking(true);
        setSelectedOption(answer);
        setFeedback(null);

        try {
            const result = await arenaService.checkAnswer({
                questionId: currentQuestion.id,
                userAnswer: answer,
            });

            setFeedback(result);

            if (result.isCorrect) {
                setIsSuccess(true);
                setTimeout(() => {
                    setIsSuccess(false);
                    setFeedback(null);
                    setSelectedOption(null);
                    if (task.questions && currentIndex + 1 < task.questions.length) {
                        setCurrentIndex(prev => prev + 1);
                    } else {
                        handleFinishRun(true, 'victory');
                    }
                    setIsChecking(false);
                }, 1500);
            } else {
                setIsShaking(true);
                setTimeout(() => setIsShaking(false), 500);

                setHearts(prev => prev - 1);
                if (result.crystalsAwarded) setEarnedCrystals(prev => prev + result.crystalsAwarded!);
                if (!failedQuestionIds.includes(currentQuestion.id)) setFailedQuestionIds(prev => [...prev, currentQuestion.id]);

                if (hearts - 1 <= 0) {
                    setTimeout(() => handleFinishRun(false, 'defeat'), 1000);
                }
                setIsChecking(false);
            }
        } catch (error) {
            setIsChecking(false);
        }
    };

    const handleFinishRun = async (isVictory: boolean, reason: 'victory' | 'defeat' | 'timeout' | 'cheated') => {
        setRunStatus(reason);
        if (task) {
            try {
                await arenaService.finishRun({ taskId: task.id, isVictory, failedQuestionIds });
            } catch (error) { }
        }
    };

    if (isLoading) return <div className="min-h-screen bg-zinc-950 flex items-center justify-center text-zinc-500 font-bold">Підготовка Арени...</div>;
    if (!task || !currentQuestion) return <div className="min-h-screen bg-zinc-950 p-8 text-center text-red-400">Завдання не знайдено.</div>;

    const bgClasses = isBoss
        ? "bg-gradient-to-b from-red-950/40 via-zinc-950 to-zinc-950 border-red-900/30"
        : "bg-zinc-950 border-zinc-800";

    return (
        <>
            <style>{`
        @keyframes shake {
          0%, 100% { transform: translateX(0); }
          25% { transform: translateX(-10px); }
          50% { transform: translateX(10px); }
          75% { transform: translateX(-10px); }
        }
        .animate-shake { animation: shake 0.4s ease-in-out; }

        @keyframes pulse-red {
          0%, 100% { box-shadow: inset 0 0 20px rgba(220,38,38,0.05); }
          50% { box-shadow: inset 0 0 60px rgba(220,38,38,0.2); }
        }
        .animate-boss-aura { animation: pulse-red 3s infinite ease-in-out; }
      `}</style>

            <div className={`min-h-screen text-white flex flex-col transition-transform ${isShaking ? 'animate-shake' : ''} ${bgClasses} ${isBoss ? 'animate-boss-aura relative overflow-hidden' : ''}`}>

                {isBoss && (
                    <>
                        <div className="absolute top-0 left-1/4 w-96 h-96 bg-red-600/10 rounded-full blur-[120px] pointer-events-none" />
                        <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-orange-600/5 rounded-full blur-[100px] pointer-events-none" />
                    </>
                )}

                <header className={`p-4 flex items-center justify-between sticky top-0 z-10 border-b backdrop-blur-md ${isBoss ? 'bg-red-950/20 border-red-900/30' : 'bg-zinc-900/90 border-zinc-800'}`}>
                    <button onClick={() => navigate('/dashboard')} className="flex items-center gap-2 text-zinc-400 hover:text-white transition-colors font-bold">
                        <Flag size={20} /> Відступити до Табору
                    </button>
                    <div className="flex items-center gap-6 md:gap-8">

                        {timeLeft !== null && (
                            <div className={`flex items-center gap-2 font-black text-lg ${timeLeft <= 10 ? 'text-red-500 animate-pulse' : 'text-orange-400'}`}>
                                <Clock size={20} /> {formatTime(timeLeft)}
                            </div>
                        )}

                        <div className="flex items-center gap-2">
                            {[1, 2, 3].map((h) => (
                                <Heart key={h} size={24} className={`transition-all duration-300 ${h <= hearts ? (isBoss ? 'fill-red-600 text-red-600 drop-shadow-[0_0_8px_rgba(220,38,38,0.8)]' : 'fill-red-500 text-red-500') : 'fill-zinc-800 text-zinc-700'}`} />
                            ))}
                        </div>
                        <div className="flex items-center gap-2 text-purple-400 font-bold bg-purple-500/10 px-3 py-1 rounded-lg">
                            <Gem size={18} /> {earnedCrystals}
                        </div>
                    </div>
                </header>

                <main className="flex-1 flex flex-col lg:flex-row max-w-7xl mx-auto w-full p-4 lg:p-6 gap-6 relative z-10">
                    {!task.isTheoryHidden && (
                        <div className={`lg:w-1/2 border rounded-3xl p-6 overflow-y-auto max-h-[80vh] ${isBoss ? 'bg-zinc-950/60 border-red-900/30' : 'bg-zinc-900 border-zinc-800'}`}>
                            {isBoss ? (
                                <div className="flex items-center gap-4 mb-6 border-b border-red-900/30 pb-4">
                                    <div className="p-3 bg-red-950 border border-red-900/50 rounded-2xl">
                                        <Skull className="text-red-500" size={32} />
                                    </div>
                                    <div>
                                        <h2 className="text-2xl font-black text-red-500">{task.bossMetadata?.bossName || 'Фінальне Випробування'}</h2>
                                        <span className="text-xs font-bold text-orange-500 uppercase tracking-widest flex items-center gap-1 mt-1"><Flame size={14} /> {task.title}</span>
                                    </div>
                                </div>
                            ) : (
                                <h2 className="text-2xl font-black mb-4 text-purple-400">{task.title}</h2>
                            )}
                            <div className="prose prose-invert max-w-none text-zinc-300 leading-relaxed" dangerouslySetInnerHTML={{ __html: task.theoryContent || '' }} />
                        </div>
                    )}

                    <div className={`flex flex-col ${task.isTheoryHidden ? 'w-full max-w-3xl mx-auto' : 'lg:w-1/2'}`}>
                        <div className="flex items-center justify-between mb-6">
                            <span className={`text-sm font-bold uppercase tracking-widest ${isBoss ? 'text-red-500/70' : 'text-zinc-500'}`}>Крок {currentIndex + 1} з {task.questions?.length}</span>
                            <div className={`h-2 flex-1 mx-4 rounded-full overflow-hidden ${isBoss ? 'bg-red-950/50' : 'bg-zinc-900'}`}>
                                <div className={`h-full transition-all duration-500 ${isBoss ? 'bg-gradient-to-r from-orange-500 to-red-600 shadow-[0_0_10px_rgba(220,38,38,0.5)]' : 'bg-blue-500'}`} style={{ width: `${((currentIndex) / (task.questions?.length || 1)) * 100}%` }} />
                            </div>
                        </div>

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
                                            disabled={isChecking || runStatus !== 'playing'}
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
                                    <p className="font-bold mb-1 flex items-center gap-2"><Flame size={18} /> Промах! Спробуй ще раз</p>
                                    <p className="text-sm text-zinc-300">{feedback.explanation}</p>
                                </div>
                            )}
                        </div>
                    </div>
                </main>

                {runStatus !== 'playing' && (
                    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/90 backdrop-blur-md animate-in fade-in duration-500">
                        <div className={`border rounded-3xl p-8 max-w-sm w-full text-center text-white scale-in-center ${isBoss && runStatus !== 'victory' ? 'bg-red-950/40 border-red-900/50' : 'bg-zinc-900 border-zinc-800'}`}>
                            {runStatus === 'victory' ? (
                                <>
                                    <div className="w-20 h-20 bg-green-500/20 text-green-400 rounded-full flex items-center justify-center mx-auto mb-6">
                                        {isBoss ? <Skull size={40} /> : <Flag size={40} />}
                                    </div>
                                    <h2 className="text-3xl font-black mb-2">{isBoss ? 'Боса Подолано!' : 'Перемога!'}</h2>
                                    <p className="text-zinc-400 mb-6">Ти успішно пройшов підземелля.</p>
                                    <div className="flex justify-center gap-4 mb-8">
                                        <div className="bg-zinc-950 px-4 py-2 rounded-xl border border-zinc-800 font-bold text-yellow-400">+{task.rewardGold} 🪙</div>
                                        <div className="bg-zinc-950 px-4 py-2 rounded-xl border border-zinc-800 font-bold text-blue-400">+{task.rewardXp} XP</div>
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
                                        {runStatus === 'timeout' && 'Час вийшов'}
                                        {runStatus === 'cheated' && 'Магія розсіялася'}
                                        {runStatus === 'defeat' && 'Сили вичерпано'}
                                    </h2>
                                    <p className="text-zinc-400 mb-6">
                                        {runStatus === 'timeout' && 'Ти не встиг розвіяти закляття.'}
                                        {runStatus === 'cheated' && 'Ти втратив концентрацію. Під час битви з Босом не можна відводити погляд!'}
                                        {runStatus === 'defeat' && 'Ти помилявся, але це шлях до знань!'}
                                    </p>
                                    <div className="inline-flex items-center gap-2 bg-purple-500/20 px-6 py-3 rounded-xl border border-purple-500/30 font-bold text-purple-400 mb-8">
                                        Здобуто: {earnedCrystals} <Gem size={20} />
                                    </div>
                                </>
                            )}
                            <button onClick={() => navigate('/dashboard')} className={`w-full py-4 rounded-2xl font-black text-lg transition-colors flex items-center justify-center gap-2 ${isBoss && runStatus !== 'victory' ? 'bg-red-600 hover:bg-red-500 text-white' : 'bg-white text-black hover:bg-zinc-200'}`}>
                                Повернутися в Табір <ChevronRight size={24} />
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </>
    );
};
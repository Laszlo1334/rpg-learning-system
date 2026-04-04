// src/services/arenaService.ts
import { api } from './api';
import type { AnswerResponse } from '@/types';

export const arenaService = {
    // Відправляє відповідь на бекенд для перевірки
    checkAnswer: async (data: { questionId: number; userAnswer: string }): Promise<AnswerResponse> => {
        const response = await api.post<AnswerResponse>('/arena/check', data);
        return response.data;
    },

    // Відправляє результати проходження (перемога/поразка та помилки)
    finishRun: async (data: { taskId: number; isVictory: boolean; failedQuestionIds: number[] }): Promise<void> => {
        await api.post('/arena/finish', data);
    }
};
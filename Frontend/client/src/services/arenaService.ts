// src/services/arenaService.ts
import { api } from './api';
import type { AnswerResponse, RunCompletionRequest } from '@/types';

export const arenaService = {
    // Відправляє відповідь на бекенд для перевірки
    checkAnswer: async (data: { questionId: number; userAnswer: string }): Promise<AnswerResponse> => {
        const response = await api.post<AnswerResponse>('/arena/check-answer', data);
        return response.data;
    },

    // Відправляє результати проходження (перемога/поразка та помилки)
    finishRun: async (data: RunCompletionRequest): Promise<void> => {
        await api.post('/arena/finish-run', data);
    }
};
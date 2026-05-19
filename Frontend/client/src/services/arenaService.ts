// src/services/arenaService.ts
import { api } from './api';
import type { AnswerResponse, RunCompletionRequest, RunCompletionResponse } from '@/types';

export const arenaService = {
    // Відправляє відповідь на бекенд для перевірки
    checkAnswer: async (data: { questionId: number; userAnswer: string }): Promise<AnswerResponse> => {
        const response = await api.post<AnswerResponse>('/arena/check-answer', data);
        return response.data;
    },

    // Відправляє результати проходження (перемога/поразка та помилки)
    finishRun: async (data: RunCompletionRequest): Promise<RunCompletionResponse> => {
        const response = await api.post<RunCompletionResponse>('/arena/finish-run', data);
        return response.data;
    }
};
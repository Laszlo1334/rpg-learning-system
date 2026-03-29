// src/services/questService.ts
import { api } from './api';
import type { Task } from '@/types';

export const questService = {
  // Отримання всіх квестів
  getAllTasks: async (): Promise<Task[]> => {
    const response = await api.get<Task[]>('/tasks');
    return response.data;
  },

  // Відправка відповіді на перевірку (САМЕ ЦЮ ФУНКЦІЮ ШУКАЄ ТИПІЗАЦІЯ)
  submitTask: async (taskId: number, answer: string) => {
    const response = await api.post('/submissions', { 
      taskId: taskId, 
      answer: answer 
    });
    return response.data;
  }
};
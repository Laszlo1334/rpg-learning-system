// src/services/questService.ts
import { api } from './api';
import type { Task } from '@/types';

export const questService = {
  // Функція робить GET запит на /api/tasks (як у твоєму Swagger)
  getAllTasks: async (): Promise<Task[]> => {
    const response = await api.get<Task[]>('/tasks');
    return response.data;
  },
};
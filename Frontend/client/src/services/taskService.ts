import { api } from './api';
import type { TaskDto } from '@/types';

export const taskService = {
    // Отримуємо всі завдання для конкретного курсу
    getTasksByCourse: async (courseId: number): Promise<TaskDto[]> => {
        const response = await api.get<TaskDto[]>(`/tasks/course/${courseId}`);
        return response.data;
    },

    // Отримуємо одне завдання за його ID (для Арени)
    getTaskById: async (id: number): Promise<TaskDto> => {
        const response = await api.get<TaskDto>(`/tasks/${id}`);
        return response.data;
    },

    // Запит на отримання квесту для інтервального повторення
    getMemoryTask: async (): Promise<TaskDto | null> => {
        // validateStatus дозволяє нам не викликати помилку при статусі 204 (Немає контенту)
        const response = await api.get<TaskDto>('/tasks/memory', { validateStatus: (s) => s < 500 });
        // Якщо бекенд каже, що завдань для повторення немає, повертаємо null
        if (response.status === 204) return null;
        return response.data;
    },
};
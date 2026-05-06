import { api } from './api';
import type { TaskDto } from '@/types';

export const taskService = {
    // Fetch all tasks for a specific course
    getTasksByCourse: async (courseId: number): Promise<TaskDto[]> => {
        const response = await api.get<TaskDto[]>(`/tasks/course/${courseId}`);
        return response.data;
    },

    // Fetch a single task by ID (used by the Arena)
    getTaskById: async (id: number): Promise<TaskDto> => {
        const response = await api.get<TaskDto>(`/tasks/${id}`);
        return response.data;
    },


};
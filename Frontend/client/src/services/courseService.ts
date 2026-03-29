// src/services/courseService.ts
import { api } from './api';
import type { Course } from '@/types';

export const courseService = {
  // Отримати всі доступні курси
  getAllCourses: async (): Promise<Course[]> => {
    const response = await api.get<Course[]>('/courses');
    return response.data;
  },
};
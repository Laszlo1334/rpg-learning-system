
import { api } from './api';
import type { CourseProgressDto } from '@/types';

export const courseService = {

  getAllCourses: async (): Promise<CourseProgressDto[]> => {
    const response = await api.get<CourseProgressDto[]>('/courses');
    return response.data;
  },
};
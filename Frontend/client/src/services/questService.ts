import { api } from './api';
import type { CourseDto } from '@/types';

export const courseService = {
    getAllCourses: async (): Promise<CourseDto[]> => {
        const response = await api.get<CourseDto[]>('/courses');
        return response.data;
    }
};
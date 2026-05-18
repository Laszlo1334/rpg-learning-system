// src/services/leaderboardService.ts
import { api } from './api';
import type { LeaderboardDto, CourseLeaderboardDto } from '@/types';

export const leaderboardService = {
    // Топ-10 гравців за загальним XP або золотом (тільки isPublicProfile = true)
    getGlobalLeaderboard: async (sortBy: 'xp' | 'gold' = 'xp'): Promise<LeaderboardDto[]> => {
        const response = await api.get<LeaderboardDto[]>(`/leaderboard/global?sortBy=${sortBy}`);
        return response.data;
    },

    // Топ гравців у межах конкретного курсу
    getCourseLeaderboard: async (courseId: number): Promise<CourseLeaderboardDto[]> => {
        const response = await api.get<CourseLeaderboardDto[]>(`/leaderboard/course/${courseId}`);
        return response.data;
    },

    // Оновлення видимості профілю у рейтингу (opt-out система)
    updatePrivacy: async (isPublic: boolean): Promise<void> => {
        await api.put(`/users/me/privacy?isPublic=${isPublic}`);
    },
};

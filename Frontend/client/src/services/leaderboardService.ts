import { api } from './api';
import type { LeaderboardDto, CourseLeaderboardDto } from '@/types';

export const leaderboardService = {
    // Only players with isPublicProfile=true appear; sortBy defaults to 'xp'
    getGlobalLeaderboard: async (sortBy: 'xp' | 'gold' = 'xp'): Promise<LeaderboardDto[]> => {
        const response = await api.get<LeaderboardDto[]>(`/leaderboard/global?sortBy=${sortBy}`);
        return response.data;
    },

    getCourseLeaderboard: async (courseId: number): Promise<CourseLeaderboardDto[]> => {
        const response = await api.get<CourseLeaderboardDto[]>(`/leaderboard/course/${courseId}`);
        return response.data;
    },

    // Opt-out privacy toggle: sets whether the player appears on the global leaderboard
    updatePrivacy: async (isPublic: boolean): Promise<void> => {
        await api.put(`/users/me/privacy?isPublic=${isPublic}`);
    },
};

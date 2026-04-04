// src/services/authService.ts
import { api } from './api';
import type { User, UserStatsDto } from '../types';

export const authService = {
  // Отримання поточного юзера
  getCurrentUser: async (): Promise<User> => {
    const response = await api.get<User>('/users/me');
    return response.data;
  },

  // Отримання детальної статистики для Літопису
  getUserStats: async (): Promise<UserStatsDto> => {
    const response = await api.get<UserStatsDto>('/users/me/stats');
    return response.data;
  },

  // Логін через Google OAuth2
  loginWithGoogle: () => {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  },

  // Вихід з акаунту
  logout: async () => {
    await api.post('/logout');
  }
};
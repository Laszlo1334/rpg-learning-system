// src/services/authService.ts
import { api } from './api';
import type { User } from '../types';

export const authService = {
  // Функція для отримання поточного юзера
  getCurrentUser: async (): Promise<User> => {
    // Звертаємося до ендпоінту бекенда. Axios автоматично підставить baseURL
    const response = await api.get<User>('/users/me'); 
    return response.data;
  },

  // Функція для логіну (оскільки у нас Google OAuth2, ми просто робимо редірект на бекенд)
  loginWithGoogle: () => {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  },
  
  // Функція для виходу (якщо на бекенді є такий ендпоінт, зазвичай /logout)
  logout: async () => {
    await api.post('/logout');
  }
};
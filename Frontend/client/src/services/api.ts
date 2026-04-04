// src/services/api.ts
import axios from 'axios';

// Створюємо базовий екземпляр axios
export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  // КРИТИЧНО ВАЖЛИВО: дозволяє браузеру відправляти cookie (JSESSIONID)
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Глобальний перехоплювач відповідей
api.interceptors.response.use(
  (response) => {
    // Якщо запит успішний, просто повертаємо дані
    return response;
  },
  (error) => {
    // Якщо отримуємо 401 (Неавторизовано), сесія закінчилась
    if (error.response?.status === 401) {
      // Перенаправляємо на сторінку логіну
      // Використовуємо window.location для повного перезавантаження сторінки
      window.location.href = '/login';
    }

    // Повертаємо помилку далі, щоб її можна було обробити в компоненті (наприклад, показати тост)
    return Promise.reject(error);
  }
);
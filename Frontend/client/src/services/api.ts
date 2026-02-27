// src/services/api.ts
import axios from 'axios';

// Створюємо базовий екземпляр axios
export const api = axios.create({
  // Заміни '/api' на те, що в тебе реально на бекенді, якщо у тебе просто http://localhost:8080/users/me, то '/api' не треба
  baseURL: 'http://localhost:8080/api', 
  
  // ЦЕ НАЙГОЛОВНІШИЙ РЯДОК! 
  // Без нього браузер не передасть кукі (JSESSIONID) від Spring Security, і бекенд завжди повертатиме 401 Unauthorized
  withCredentials: true, 
});
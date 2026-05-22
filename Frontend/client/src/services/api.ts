import axios from 'axios';

// Shared axios instance; withCredentials is required to send JSESSIONID cookies
export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Global response interceptor: handles session expiry
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // 401 = session expired; force a full page reload to /login to clear React state
    if (error.response?.status === 401) {
      window.location.href = '/login';
    }

    // Propagate so callers can still show error toasts or handle locally
    return Promise.reject(error);
  }
);
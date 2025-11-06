import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { API_BASE_URL, ERROR_MESSAGES } from '../utils/constants';
import { getAuth } from 'firebase/auth';
import { toast } from 'react-toastify';

// Extend AxiosRequestConfig to include custom metadata
declare module 'axios' {
  export interface InternalAxiosRequestConfig {
    metadata?: {
      skipErrorToast?: boolean;
      silent404?: boolean;
    };
  }
}

// Create Axios instance
const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - Add auth token
api.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    try {
      const auth = getAuth();
      const user = auth.currentUser;

      if (user) {
        const token = await user.getIdToken();
        config.headers.Authorization = `Bearer ${token}`;
      }
    } catch (error) {
      console.error('Error getting auth token:', error);
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Handle errors
api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    // Handle specific error cases
    if (error.response) {
      const status = error.response.status;
      const data: any = error.response.data;

      switch (status) {
        case 401:
          // Unauthorized - Try to refresh token
          if (!originalRequest._retry) {
            originalRequest._retry = true;

            try {
              const auth = getAuth();
              const user = auth.currentUser;

              if (user) {
                const token = await user.getIdToken(true); // Force refresh
                originalRequest.headers.Authorization = `Bearer ${token}`;
                return api(originalRequest);
              } else {
                // No user, redirect to login
                window.location.href = '/login';
                toast.error(ERROR_MESSAGES.SESSION_EXPIRED, { toastId: 'session-expired' });
              }
            } catch (refreshError) {
              window.location.href = '/login';
              toast.error(ERROR_MESSAGES.SESSION_EXPIRED, { toastId: 'session-expired' });
              return Promise.reject(refreshError);
            }
          }
          break;

        case 403:
          toast.error(ERROR_MESSAGES.UNAUTHORIZED, { toastId: 'unauthorized' });
          break;

        case 404:
          // Check metadata to skip toast for expected 404s (e.g., during user sync)
          if (!originalRequest.metadata?.silent404) {
            toast.error(data?.message || ERROR_MESSAGES.NOT_FOUND, { toastId: 'not-found' });
          }
          break;

        case 422:
          toast.error(data?.message || ERROR_MESSAGES.VALIDATION_ERROR, { toastId: 'validation-error' });
          break;

        case 500:
        case 502:
        case 503:
        case 504:
          // Don't spam toasts for backend unavailability - handled by useAuth
          console.warn(`[API] Server error ${status}:`, data?.message || ERROR_MESSAGES.SERVER_ERROR);
          break;

        default:
          // Only show toast for unexpected errors
          if (status >= 400) {
            toast.error(data?.message || 'An error occurred', { toastId: `error-${status}` });
          }
      }
    } else if (error.request) {
      // Network error - Don't spam toasts, just log
      console.warn('[API] Network error - backend may be unavailable');
    } else {
      console.error('[API] Unexpected error:', error.message);
    }

    return Promise.reject(error);
  }
);

export default api;

// Helper functions for making requests
export const apiClient = {
  get: <T>(url: string, params?: any) =>
    api.get<T>(url, { 
      params,
      metadata: { silent404: url.includes('/auth/me') } // Suppress 404 toasts for user fetch
    }).then((res) => res.data),

  post: <T>(url: string, data?: any) =>
    api.post<T>(url, data).then((res) => res.data),

  put: <T>(url: string, data?: any) =>
    api.put<T>(url, data).then((res) => res.data),

  patch: <T>(url: string, data?: any) =>
    api.patch<T>(url, data).then((res) => res.data),

  delete: <T>(url: string) =>
    api.delete<T>(url).then((res) => res.data),
};

import { apiClient } from './api';
import { User, RegisterData, UpdateProfileData } from '../types';

export const authService = {
  // Register new user
  register: async (data: RegisterData): Promise<{ user: User; token: string }> => {
    return apiClient.post('/auth/register', data);
  },

  // Get current user profile
  getCurrentUser: async (): Promise<User> => {
    return apiClient.get('/auth/me', undefined, { silent404: true });
  },

  // Update user profile
  updateProfile: async (data: UpdateProfileData): Promise<User> => {
    return apiClient.patch('/auth/profile', data);
  },

  // Sync Firebase user with backend
  syncUser: async (firebaseUser: any): Promise<User> => {
    const response: any = await apiClient.post('/auth/sync', {
      firebaseUid: firebaseUser.uid,
      email: firebaseUser.email,
      name: firebaseUser.displayName || firebaseUser.email?.split('@')[0] || 'User',
      phone: firebaseUser.phoneNumber || null,
      companyId: null,
      companyName: null,
    });
    // Backend returns AuthResponse { success, message, user }, extract user
    return response.user || response;
  },
};

export default authService;

export enum UserRole {
  SUPER_ADMIN = 'SUPER_ADMIN',
  FLEET_ADMIN = 'FLEET_ADMIN',
  FLEET_MANAGER = 'FLEET_MANAGER',
  DRIVER = 'DRIVER',
  VIEWER = 'VIEWER',
}

export interface User {
  id: string;
  firebaseUid: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  companyId?: number;
  companyName?: string;
  fleetId?: string;
  fleetName?: string;
  phone?: string;
  profileImageUrl?: string;
  isActive: boolean;
  emailVerified: boolean;
  lastLogin?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AuthState {
  user: User | null;
  firebaseUser: any | null;
  token: string | null;
  loading: boolean;
  error: string | null;
  isAuthenticated: boolean;
}

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterData {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
  companyName?: string;
  fleetSize?: number;
  firebaseUid?: string;
  name?: string;
}

export interface ResetPasswordData {
  email: string;
}

export interface UpdateProfileData {
  firstName?: string;
  lastName?: string;
  phone?: string;
  profileImageUrl?: string;
}

export interface ChangePasswordData {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

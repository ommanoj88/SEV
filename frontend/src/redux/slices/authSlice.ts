import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { AuthState, RegisterData, UpdateProfileData } from '../../types';
import { firebaseAuth } from '../../services/firebase';
import authService from '../../services/authService';
import { RootState } from '../store';

// Extended AuthState to track initialization
interface ExtendedAuthState extends AuthState {
  initialized: boolean; // True when initial auth check is complete
}

const initialState: ExtendedAuthState = {
  user: null,
  firebaseUser: null,
  token: null,
  loading: true, // Start with loading true until auth is initialized
  error: null,
  isAuthenticated: false,
  initialized: false,
};

// Async thunks
export const loginWithEmail = createAsyncThunk(
  'auth/loginWithEmail',
  async ({ email, password }: { email: string; password: string }, { rejectWithValue }) => {
    try {
      const firebaseUser = await firebaseAuth.signIn(email, password);
      const token = await firebaseUser.getIdToken();
      const user = await authService.syncUser(firebaseUser);
      return { user, firebaseUser, token };
    } catch (error: any) {
      return rejectWithValue(error.message);
    }
  }
);

export const loginWithGoogle = createAsyncThunk(
  'auth/loginWithGoogle',
  async (_, { rejectWithValue }) => {
    try {
      const firebaseUser = await firebaseAuth.signInWithGoogle();
      const token = await firebaseUser.getIdToken();
      const user = await authService.syncUser(firebaseUser);
      return { user, firebaseUser, token };
    } catch (error: any) {
      return rejectWithValue(error.message);
    }
  }
);

export const register = createAsyncThunk(
  'auth/register',
  async (data: RegisterData, { rejectWithValue }) => {
    try {
      const displayName = `${data.firstName} ${data.lastName}`;
      const firebaseUser = await firebaseAuth.signUp(data.email, data.password, displayName);
      const token = await firebaseUser.getIdToken();
      const user = await authService.register({
        ...data,
        firebaseUid: firebaseUser.uid,
        name: displayName
      });
      return { user, firebaseUser, token };
    } catch (error: any) {
      return rejectWithValue(error.message);
    }
  }
);

export const logout = createAsyncThunk(
  'auth/logout',
  async (_, { rejectWithValue }) => {
    try {
      await firebaseAuth.signOut();
      return undefined;
    } catch (error: any) {
      return rejectWithValue(error.message);
    }
  }
);

export const fetchCurrentUser = createAsyncThunk(
  'auth/fetchCurrentUser',
  async (_, { rejectWithValue }) => {
    try {
      const user = await authService.getCurrentUser();
      return user;
    } catch (error: any) {
      return rejectWithValue(error.message);
    }
  }
);

export const updateProfile = createAsyncThunk(
  'auth/updateProfile',
  async (data: UpdateProfileData, { rejectWithValue }) => {
    try {
      const user = await authService.updateProfile(data);
      return user;
    } catch (error: any) {
      return rejectWithValue(error.message);
    }
  }
);

export const resetPassword = createAsyncThunk(
  'auth/resetPassword',
  async (email: string, { rejectWithValue }) => {
    try {
      await firebaseAuth.resetPassword(email);
      return undefined;
    } catch (error: any) {
      return rejectWithValue(error.message);
    }
  }
);

// Slice
const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setFirebaseUser: (state, action: PayloadAction<any>) => {
      state.firebaseUser = action.payload;
      // Don't set isAuthenticated here - wait for user to be fetched
      // This prevents components from rendering before user data is available
      if (!action.payload) {
        // User logged out
        state.isAuthenticated = false;
        state.user = null;
        state.loading = false;
        state.initialized = true;
      }
    },
    setToken: (state, action: PayloadAction<string | null>) => {
      state.token = action.payload;
    },
    clearError: (state) => {
      state.error = null;
    },
    setAuthInitialized: (state) => {
      state.initialized = true;
      state.loading = false;
    },
  },
  extraReducers: (builder) => {
    builder
      // Login with email
      .addCase(loginWithEmail.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loginWithEmail.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload.user;
        state.firebaseUser = action.payload.firebaseUser;
        state.token = action.payload.token;
        state.isAuthenticated = true;
        state.error = null;
      })
      .addCase(loginWithEmail.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      // Login with Google
      .addCase(loginWithGoogle.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loginWithGoogle.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload.user;
        state.firebaseUser = action.payload.firebaseUser;
        state.token = action.payload.token;
        state.isAuthenticated = true;
        state.error = null;
      })
      .addCase(loginWithGoogle.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      // Register
      .addCase(register.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(register.fulfilled, (state, action: any) => {
        state.loading = false;
        state.user = action.payload.user;
        state.firebaseUser = action.payload.firebaseUser;
        state.token = action.payload.token;
        state.isAuthenticated = true;
        state.error = null;
      })
      .addCase(register.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      // Logout
      .addCase(logout.fulfilled, (state) => {
        state.user = null;
        state.firebaseUser = null;
        state.token = null;
        state.isAuthenticated = false;
        state.error = null;
        state.initialized = true;
      })
      // Fetch current user
      .addCase(fetchCurrentUser.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchCurrentUser.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload;
        state.isAuthenticated = true;
        state.initialized = true; // Auth is now fully initialized
      })
      .addCase(fetchCurrentUser.rejected, (state) => {
        state.loading = false;
        state.isAuthenticated = false;
        state.initialized = true; // Auth check complete, but failed
      })
      // Update profile
      .addCase(updateProfile.fulfilled, (state, action) => {
        state.user = action.payload;
      })
      // Reset password
      .addCase(resetPassword.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(resetPassword.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(resetPassword.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });
  },
});

export const { setFirebaseUser, setToken, clearError, setAuthInitialized } = authSlice.actions;

// Selectors
export const selectAuth = (state: RootState) => state.auth;
export const selectUser = (state: RootState) => state.auth.user;
export const selectIsAuthenticated = (state: RootState) => state.auth.isAuthenticated;
export const selectAuthLoading = (state: RootState) => state.auth.loading;
export const selectAuthError = (state: RootState) => state.auth.error;
export const selectAuthInitialized = (state: RootState) => (state.auth as any).initialized;

export default authSlice.reducer;

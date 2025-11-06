import { initializeApp, FirebaseApp } from 'firebase/app';
import {
  getAuth,
  Auth,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signOut,
  sendPasswordResetEmail,
  updateProfile,
  GoogleAuthProvider,
  signInWithPopup,
  User,
} from 'firebase/auth';
import { FIREBASE_CONFIG } from '../utils/constants';

// Initialize Firebase
let app: FirebaseApp;
let auth: Auth;

try {
  app = initializeApp(FIREBASE_CONFIG);
  auth = getAuth(app);
} catch (error) {
  console.error('Error initializing Firebase:', error);
  throw new Error('Failed to initialize Firebase');
}

// Google Auth Provider
const googleProvider = new GoogleAuthProvider();
// Configure provider to reduce popup-related issues
// The 'select_account' prompt ensures user actively selects account,
// reducing issues with popup blocking and COOP policy enforcement
//
// Note: You may see COOP warnings in the console during popup auth:
// "Cross-Origin-Opener-Policy policy would block the window.closed call"
// These are non-functional warnings from Firebase SDK's internal checks
// and can be safely ignored. See: frontend/COOP_WARNINGS.md for details.
googleProvider.setCustomParameters({
  prompt: 'select_account'
});

// Authentication Functions
export const firebaseAuth = {
  // Sign in with email and password
  signIn: async (email: string, password: string) => {
    try {
      const userCredential = await signInWithEmailAndPassword(auth, email, password);
      return userCredential.user;
    } catch (error: any) {
      throw new Error(getAuthErrorMessage(error.code));
    }
  },

  // Sign up with email and password
  signUp: async (email: string, password: string, displayName?: string) => {
    try {
      const userCredential = await createUserWithEmailAndPassword(auth, email, password);
      const user = userCredential.user;

      // Update profile with display name
      if (displayName) {
        await updateProfile(user, { displayName });
      }

      return user;
    } catch (error: any) {
      throw new Error(getAuthErrorMessage(error.code));
    }
  },

  // Sign in with Google
  signInWithGoogle: async () => {
    try {
      // Use popup for better COOP compatibility
      const result = await signInWithPopup(auth, googleProvider);
      return result.user;
    } catch (error: any) {
      // Handle specific COOP-related errors gracefully
      if (error.code === 'auth/popup-closed-by-user') {
        throw new Error('Sign-in popup was closed. Please try again.');
      } else if (error.code === 'auth/cancelled-popup-request') {
        throw new Error('Another sign-in is in progress. Please wait.');
      }
      throw new Error(getAuthErrorMessage(error.code));
    }
  },

  // Sign out
  signOut: async () => {
    try {
      await signOut(auth);
    } catch (error: any) {
      throw new Error('Failed to sign out');
    }
  },

  // Send password reset email
  resetPassword: async (email: string) => {
    try {
      await sendPasswordResetEmail(auth, email);
    } catch (error: any) {
      throw new Error(getAuthErrorMessage(error.code));
    }
  },

  // Update user profile
  updateUserProfile: async (user: User, displayName?: string, photoURL?: string) => {
    try {
      await updateProfile(user, { displayName, photoURL });
    } catch (error: any) {
      throw new Error('Failed to update profile');
    }
  },

  // Get current user
  getCurrentUser: () => {
    return auth.currentUser;
  },

  // Get ID token
  getIdToken: async (forceRefresh: boolean = false) => {
    const user = auth.currentUser;
    if (user) {
      return await user.getIdToken(forceRefresh);
    }
    return null;
  },

  // Listen to auth state changes
  onAuthStateChanged: (callback: (user: User | null) => void) => {
    return auth.onAuthStateChanged(callback);
  },
};

// Helper function to get user-friendly error messages
const getAuthErrorMessage = (errorCode: string): string => {
  switch (errorCode) {
    case 'auth/user-not-found':
      return 'No account found with this email';
    case 'auth/wrong-password':
      return 'Incorrect password';
    case 'auth/email-already-in-use':
      return 'Email already in use';
    case 'auth/weak-password':
      return 'Password is too weak';
    case 'auth/invalid-email':
      return 'Invalid email address';
    case 'auth/operation-not-allowed':
      return 'Operation not allowed';
    case 'auth/user-disabled':
      return 'This account has been disabled';
    case 'auth/too-many-requests':
      return 'Too many attempts. Please try again later';
    case 'auth/network-request-failed':
      return 'Network error. Please check your connection';
    case 'auth/popup-closed-by-user':
      return 'Sign-in popup was closed';
    case 'auth/cancelled-popup-request':
      return 'Only one popup request is allowed at a time';
    default:
      return 'Authentication failed. Please try again';
  }
};

export { auth };
export default firebaseAuth;

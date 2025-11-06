import { useEffect, useRef } from 'react';
import { useAppDispatch, useAppSelector } from '../redux/hooks';
import { setFirebaseUser, fetchCurrentUser, selectAuth, logout } from '../redux/slices/authSlice';
import { firebaseAuth } from '../services/firebase';
import authService from '../services/authService';
import { toast } from 'react-toastify';

const sleep = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

export const useAuth = () => {
  const dispatch = useAppDispatch();
  const auth = useAppSelector(selectAuth);
  const syncingRef = useRef(false);
  const lastFirebaseUid = useRef<string | null>(null);
  const retryCountRef = useRef(0);
  const maxRetries = 3;
  const hasInitializedRef = useRef(false);

  useEffect(() => {
    // Ensure we only set up the listener once
    if (hasInitializedRef.current) {
      return;
    }
    hasInitializedRef.current = true;

    const unsubscribe = firebaseAuth.onAuthStateChanged(async (firebaseUser) => {
      // Prevent infinite loops - only process if UID changed
      if (firebaseUser && firebaseUser.uid === lastFirebaseUid.current) {
        console.log('[useAuth] Skipping duplicate auth state change');
        return;
      }

      lastFirebaseUid.current = firebaseUser?.uid || null;
      dispatch(setFirebaseUser(firebaseUser));

      if (firebaseUser) {
        // Prevent concurrent sync operations
        if (syncingRef.current) {
          console.log('[useAuth] Sync already in progress, skipping...');
          return;
        }

        // Check retry limit to prevent infinite loops
        if (retryCountRef.current >= maxRetries) {
          console.error('[useAuth] Max retries reached. Backend sync required but unavailable.');
          toast.error('Unable to connect to backend services. Please ensure the backend is running and try again.', {
            autoClose: false,
            toastId: 'backend-sync-required'
          });
          // Log out user from Firebase since backend sync is required
          await dispatch(logout());
          return;
        }

        try {
          syncingRef.current = true;
          retryCountRef.current += 1;

          await dispatch(fetchCurrentUser()).unwrap();
          console.log('[useAuth] User fetched successfully from backend');
          retryCountRef.current = 0; // Reset on success
          toast.dismiss('backend-unavailable');
          toast.dismiss('backend-sync-required');
        } catch (error: any) {
          // Check if it's a 503 Service Unavailable error
          if (error?.response?.status === 503 || error?.code === 'ERR_BAD_RESPONSE') {
            console.warn(`[useAuth] Backend unavailable (503). Retry ${retryCountRef.current}/${maxRetries}. Waiting before retry...`);

            // Exponential backoff: 2s, 4s, 8s
            const backoffMs = Math.pow(2, retryCountRef.current) * 1000;
            await sleep(backoffMs);

            // Try one more time after backoff
            try {
              await dispatch(fetchCurrentUser()).unwrap();
              console.log('[useAuth] User fetched after retry');
              retryCountRef.current = 0; // Reset on success
              toast.dismiss('backend-unavailable');
              toast.dismiss('backend-sync-required');
            } catch (retryError) {
              console.error('[useAuth] Retry failed. Backend sync is required but unavailable.');
              toast.error('Backend services are required for authentication. Please ensure services are running.', {
                autoClose: false,
                toastId: 'backend-sync-required'
              });
              // Log out user from Firebase since backend sync is required
              await dispatch(logout());
            }
          } else if (error?.response?.status === 404) {
            // User doesn't exist in backend, try to sync from Firebase
            console.log('[useAuth] User not found in backend, syncing from Firebase...');
            try {
              await authService.syncUser(firebaseUser);
              console.log('[useAuth] User synced to backend, retrying fetch...');
              // Retry fetching user after sync
              await dispatch(fetchCurrentUser()).unwrap();
              console.log('[useAuth] User fetched after sync - authentication complete');
              retryCountRef.current = 0; // Reset on success
              toast.dismiss('backend-unavailable');
              toast.dismiss('backend-sync-required');
              toast.success('Account synced successfully!', {
                autoClose: 3000,
                toastId: 'account-synced',
              });
            } catch (syncError: any) {
              if (syncError?.response?.status === 503) {
                console.error('[useAuth] Cannot sync - backend unavailable (503).');
                toast.error('Backend services are required but unavailable. Please ensure services are running.', {
                  autoClose: false,
                  toastId: 'backend-sync-required'
                });
              } else {
                console.error('[useAuth] Error syncing user to backend:', syncError);
                toast.error('Failed to sync your account with backend database. Authentication requires backend sync.', {
                  autoClose: false,
                  toastId: 'backend-sync-failed'
                });
              }
              // Log out user from Firebase since backend sync is required
              await dispatch(logout());
            }
          } else {
            // Other errors - still require backend sync
            console.error('[useAuth] Unexpected error during backend sync:', error);
            toast.error('Authentication requires backend database sync. Please contact support.', {
              autoClose: false,
              toastId: 'backend-sync-error'
            });
            await dispatch(logout());
          }
        } finally {
          syncingRef.current = false;
        }
      } else {
        // User logged out, reset retry counter
        retryCountRef.current = 0;
        toast.dismiss('backend-unavailable');
        toast.dismiss('backend-sync-required');
        toast.dismiss('backend-sync-failed');
        toast.dismiss('backend-sync-error');
      }
    });

    return () => {
      hasInitializedRef.current = false;
      unsubscribe();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Only run once on mount

  return auth;
};

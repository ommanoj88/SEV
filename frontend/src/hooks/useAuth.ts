import { useEffect, useRef } from 'react';
import { useAppDispatch, useAppSelector } from '../redux/hooks';
import { setFirebaseUser, fetchCurrentUser, selectAuth } from '../redux/slices/authSlice';
import { firebaseAuth } from '../services/firebase';
import authService from '../services/authService';

const sleep = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

export const useAuth = () => {
  const dispatch = useAppDispatch();
  const auth = useAppSelector(selectAuth);
  const syncingRef = useRef(false);
  const lastFirebaseUid = useRef<string | null>(null);
  const retryCountRef = useRef(0);
  const maxRetries = 3;

  useEffect(() => {
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
          console.error('[useAuth] Max retries reached. Backend may be down. Please refresh the page.');
          return;
        }

        try {
          syncingRef.current = true;
          retryCountRef.current += 1;

          await dispatch(fetchCurrentUser()).unwrap();
          console.log('[useAuth] User fetched successfully');
          retryCountRef.current = 0; // Reset on success
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
            } catch (retryError) {
              console.error('[useAuth] Retry failed. Backend still unavailable.');
            }
          } else {
            // User doesn't exist in backend, try to sync from Firebase
            console.log('[useAuth] User not found in backend, syncing from Firebase...');
            try {
              await authService.syncUser(firebaseUser);
              console.log('[useAuth] Sync completed, retrying fetch...');
              // Retry fetching user after sync
              await dispatch(fetchCurrentUser()).unwrap();
              console.log('[useAuth] User fetched after sync');
              retryCountRef.current = 0; // Reset on success
            } catch (syncError: any) {
              if (syncError?.response?.status === 503) {
                console.error('[useAuth] Cannot sync - backend unavailable (503). Please wait for services to start.');
              } else {
                console.error('[useAuth] Error syncing user:', syncError);
              }
            }
          }
        } finally {
          syncingRef.current = false;
        }
      } else {
        // User logged out, reset retry counter
        retryCountRef.current = 0;
      }
    });

    return () => unsubscribe();
  }, [dispatch]);

  return auth;
};

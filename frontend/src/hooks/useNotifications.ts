import { useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '../redux/hooks';
import { fetchNotifications, addNotification, addAlert, selectNotifications, selectUnreadCount } from '../redux/slices/notificationSlice';
import websocketService from '../services/websocket';
import { toast } from 'react-toastify';

export const useNotifications = () => {
  const dispatch = useAppDispatch();
  const notifications = useAppSelector(selectNotifications);
  const unreadCount = useAppSelector(selectUnreadCount);

  useEffect(() => {
    dispatch(fetchNotifications());

    websocketService.connect()
      .then(() => {
        websocketService.onNotification((notification) => {
          dispatch(addNotification(notification));
          toast.info(notification.title);
        });

        websocketService.onAlert((alert) => {
          dispatch(addAlert(alert));
          if (alert.severity === 'CRITICAL' || alert.severity === 'HIGH') {
            toast.error(alert.title);
          } else {
            toast.warning(alert.title);
          }
        });
      })
      .catch((error) => {
        // Silently handle WebSocket connection failure
        // Real-time notifications will be unavailable but app continues to function
      });
  }, [dispatch]);

  return { notifications, unreadCount };
};

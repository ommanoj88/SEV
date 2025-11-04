import { useEffect } from 'react';
import { useAppDispatch } from '../redux/hooks';
import { updateVehicleRealtime } from '../redux/slices/vehicleSlice';
import websocketService from '../services/websocket';

export const useRealTimeLocation = (vehicleId?: string, fleetId?: string) => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    websocketService.connect()
      .then(() => {
        if (vehicleId) {
          websocketService.subscribeToVehicle(vehicleId);
        } else if (fleetId) {
          websocketService.subscribeToFleet(fleetId);
        }

        websocketService.onLocationUpdate((data) => {
          dispatch(updateVehicleRealtime(data));
        });

        websocketService.onBatteryUpdate((data) => {
          dispatch(updateVehicleRealtime(data));
        });

        websocketService.onVehicleStatusUpdate((data) => {
          dispatch(updateVehicleRealtime(data));
        });
      })
      .catch((error) => {
        // Silently handle WebSocket connection failure
        // Real-time features will be unavailable but app continues to function
      });

    return () => {
      if (vehicleId) {
        websocketService.unsubscribeFromVehicle(vehicleId);
      } else if (fleetId) {
        websocketService.unsubscribeFromFleet(fleetId);
      }
    };
  }, [vehicleId, fleetId, dispatch]);
};

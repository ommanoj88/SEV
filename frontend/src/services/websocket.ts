import { io, Socket } from 'socket.io-client';
import { WS_URL, WS_EVENTS } from '../utils/constants';
import { firebaseAuth } from './firebase';

class WebSocketService {
  private socket: Socket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 2; // Reduced to 2 attempts
  private reconnectDelay = 3000; // Increased delay
  private connectionFailed = false; // Track if connection has permanently failed

  connect(): Promise<Socket> {
    return new Promise((resolve, reject) => {
      // If connection permanently failed, reject immediately
      if (this.connectionFailed) {
        reject(new Error('WebSocket backend not available'));
        return;
      }

      if (this.socket?.connected) {
        resolve(this.socket);
        return;
      }

      // Get auth token
      firebaseAuth.getIdToken().then((token) => {
        this.socket = io(WS_URL, {
          auth: {
            token,
          },
          transports: ['websocket', 'polling'],
          reconnection: false, // Disable auto-reconnection
          timeout: 5000, // 5 second timeout
        });

        this.socket.on(WS_EVENTS.CONNECT, () => {
          console.log('[WebSocket] Connected successfully');
          this.reconnectAttempts = 0;
          this.connectionFailed = false;
          resolve(this.socket!);
        });

        this.socket.on('connect_error', (error) => {
          this.reconnectAttempts++;

          if (this.reconnectAttempts === 1) {
            console.warn('[WebSocket] Connection failed - WebSocket backend not available. Real-time features will be disabled.');
          }

          if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            this.connectionFailed = true;
            this.disconnect();
            reject(new Error('WebSocket backend not available'));
          }
        });

        this.socket.on(WS_EVENTS.DISCONNECT, (reason) => {
          console.log('[WebSocket] Disconnected:', reason);
        });

        this.socket.on('error', (error) => {
          console.warn('[WebSocket] Error:', error.message || error);
        });
      }).catch(reject);
    });
  }

  disconnect() {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
    }
  }

  emit(event: string, data: any) {
    if (this.socket?.connected) {
      this.socket.emit(event, data);
    } else {
      console.warn('Cannot emit event: WebSocket not connected');
    }
  }

  on(event: string, callback: (data: any) => void) {
    if (this.socket) {
      this.socket.on(event, callback);
    }
  }

  off(event: string, callback?: (data: any) => void) {
    if (this.socket) {
      this.socket.off(event, callback);
    }
  }

  // Specific event listeners
  onLocationUpdate(callback: (data: any) => void) {
    this.on(WS_EVENTS.LOCATION_UPDATE, callback);
  }

  onBatteryUpdate(callback: (data: any) => void) {
    this.on(WS_EVENTS.BATTERY_UPDATE, callback);
  }

  onVehicleStatusUpdate(callback: (data: any) => void) {
    this.on(WS_EVENTS.VEHICLE_STATUS_UPDATE, callback);
  }

  onAlert(callback: (data: any) => void) {
    this.on(WS_EVENTS.ALERT, callback);
  }

  onNotification(callback: (data: any) => void) {
    this.on(WS_EVENTS.NOTIFICATION, callback);
  }

  onChargingUpdate(callback: (data: any) => void) {
    this.on(WS_EVENTS.CHARGING_UPDATE, callback);
  }

  // Subscribe to vehicle updates
  subscribeToVehicle(vehicleId: string) {
    this.emit('subscribe_vehicle', { vehicleId });
  }

  unsubscribeFromVehicle(vehicleId: string) {
    this.emit('unsubscribe_vehicle', { vehicleId });
  }

  // Subscribe to fleet updates
  subscribeToFleet(fleetId: string) {
    this.emit('subscribe_fleet', { fleetId });
  }

  unsubscribeFromFleet(fleetId: string) {
    this.emit('unsubscribe_fleet', { fleetId });
  }

  isConnected(): boolean {
    return this.socket?.connected || false;
  }
}

export const websocketService = new WebSocketService();
export default websocketService;

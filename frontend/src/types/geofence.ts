export enum GeofenceType {
  CHARGING_ZONE = 'CHARGING_ZONE',
  DEPOT = 'DEPOT',
  RESTRICTED = 'RESTRICTED',
  CUSTOMER_LOCATION = 'CUSTOMER_LOCATION',
  SERVICE_AREA = 'SERVICE_AREA',
  NO_GO_ZONE = 'NO_GO_ZONE',
  PARKING_AREA = 'PARKING_AREA',
}

export enum GeofenceShape {
  CIRCLE = 'CIRCLE',
  POLYGON = 'POLYGON',
  RECTANGLE = 'RECTANGLE',
}

export interface Coordinates {
  latitude: number;
  longitude: number;
}

export interface GeofenceFormData {
  name: string;
  type: GeofenceType;
  shape: GeofenceShape;
  coordinates: Coordinates[]; // For polygon/rectangle points or center
  radius?: number; // For circles in meters
  speedLimit?: number; // km/h
  alertOnEntry: boolean;
  alertOnExit: boolean;
  scheduleStartTime?: string; // HH:MM format
  scheduleEndTime?: string; // HH:MM format
  scheduleDays?: string[]; // MON, TUE, WED, etc.
  allowedVehicles?: number[]; // Vehicle IDs
  restrictedVehicles?: number[]; // Vehicle IDs
  companyId: number;
}

export interface Geofence extends GeofenceFormData {
  id: number;
  createdAt: string;
  updatedAt: string;
}

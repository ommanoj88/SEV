import * as yup from 'yup';
import { EMAIL_REGEX, PHONE_REGEX, VIN_LENGTH, LICENSE_PLATE_REGEX } from './constants';

// Authentication Schemas
export const loginSchema = yup.object({
  email: yup
    .string()
    .required('Email is required')
    .matches(EMAIL_REGEX, 'Invalid email format'),
  password: yup
    .string()
    .required('Password is required')
    .min(6, 'Password must be at least 6 characters'),
});

export const registerSchema = yup.object({
  email: yup
    .string()
    .required('Email is required')
    .matches(EMAIL_REGEX, 'Invalid email format'),
  password: yup
    .string()
    .required('Password is required')
    .min(8, 'Password must be at least 8 characters')
    .matches(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/,
      'Password must contain uppercase, lowercase, number and special character'
    ),
  confirmPassword: yup
    .string()
    .required('Please confirm your password')
    .oneOf([yup.ref('password')], 'Passwords must match'),
  firstName: yup
    .string()
    .required('First name is required')
    .min(2, 'First name must be at least 2 characters'),
  lastName: yup
    .string()
    .required('Last name is required')
    .min(2, 'Last name must be at least 2 characters'),
  phone: yup
    .string()
    .transform((value) => (value === '' ? undefined : value))
    .matches(PHONE_REGEX, { message: 'Invalid phone number format', excludeEmptyString: true })
    .optional(),
  companyName: yup
    .string()
    .required('Company name is required')
    .min(2, 'Company name must be at least 2 characters'),
  fleetSize: yup
    .number()
    .transform((value) => (isNaN(value) ? undefined : value))
    .positive().integer()
    .optional(),
});

export const forgotPasswordSchema = yup.object({
  email: yup
    .string()
    .required('Email is required')
    .matches(EMAIL_REGEX, 'Invalid email format'),
});

export const changePasswordSchema = yup.object({
  currentPassword: yup.string().required('Current password is required'),
  newPassword: yup
    .string()
    .required('New password is required')
    .min(8, 'Password must be at least 8 characters')
    .matches(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/,
      'Password must contain uppercase, lowercase, number and special character'
    ),
  confirmPassword: yup
    .string()
    .required('Please confirm your password')
    .oneOf([yup.ref('newPassword')], 'Passwords must match'),
});

// Vehicle Schemas
export const vehicleSchema = yup.object({
  vin: yup
    .string()
    .required('VIN is required')
    .length(VIN_LENGTH, `VIN must be exactly ${VIN_LENGTH} characters`),
  make: yup
    .string()
    .required('Make is required')
    .min(2, 'Make must be at least 2 characters'),
  model: yup
    .string()
    .required('Model is required')
    .min(1, 'Model is required'),
  year: yup
    .number()
    .required('Year is required')
    .min(2000, 'Year must be 2000 or later')
    .max(new Date().getFullYear() + 1, 'Invalid year'),
  type: yup.string().required('Vehicle type is required'),
  licensePlate: yup
    .string()
    .required('License plate is required')
    .matches(LICENSE_PLATE_REGEX, 'Invalid license plate format'),
  color: yup.string().optional(),
  batteryCapacity: yup
    .number()
    .required('Battery capacity is required')
    .positive('Battery capacity must be positive'),
  imageUrl: yup.string().url('Must be a valid URL').optional(),
});

// Driver Schemas
export const driverSchema = yup.object({
  firstName: yup
    .string()
    .required('First name is required')
    .min(2, 'First name must be at least 2 characters'),
  lastName: yup
    .string()
    .required('Last name is required')
    .min(2, 'Last name must be at least 2 characters'),
  email: yup
    .string()
    .required('Email is required')
    .matches(EMAIL_REGEX, 'Invalid email format'),
  phone: yup
    .string()
    .required('Phone is required')
    .matches(PHONE_REGEX, 'Invalid phone number format'),
  licenseNumber: yup
    .string()
    .required('License number is required')
    .min(5, 'License number must be at least 5 characters'),
  licenseExpiry: yup
    .date()
    .required('License expiry is required')
    .min(new Date(), 'License has expired'),
  dateOfBirth: yup
    .date()
    .max(new Date(), 'Date of birth cannot be in the future')
    .optional(),
  address: yup.string().optional(),
  emergencyContactName: yup.string().optional(),
  emergencyContactPhone: yup
    .string()
    .matches(PHONE_REGEX, 'Invalid phone number format')
    .optional(),
  emergencyContactRelationship: yup.string().optional(),
});

// Charging Session Schema
export const chargingSessionSchema = yup.object({
  vehicleId: yup.string().required('Vehicle is required'),
  stationId: yup.string().required('Charging station is required'),
  targetBatteryLevel: yup
    .number()
    .required('Target battery level is required')
    .min(1, 'Must be at least 1%')
    .max(100, 'Cannot exceed 100%'),
});

// Maintenance Schema
export const maintenanceSchema = yup.object({
  vehicleId: yup.string().required('Vehicle is required'),
  type: yup.string().required('Maintenance type is required'),
  priority: yup.string().required('Priority is required'),
  scheduledDate: yup
    .date()
    .required('Scheduled date is required')
    .min(new Date(), 'Scheduled date cannot be in the past'),
  description: yup
    .string()
    .required('Description is required')
    .min(10, 'Description must be at least 10 characters'),
  estimatedCost: yup.number().positive('Cost must be positive').optional(),
  serviceProvider: yup.string().optional(),
  notes: yup.string().optional(),
});

// Route Optimization Schema
export const routeOptimizationSchema = yup.object({
  origin: yup.object({
    latitude: yup.number().required().min(-90).max(90),
    longitude: yup.number().required().min(-180).max(180),
  }).required('Origin is required'),
  destination: yup.object({
    latitude: yup.number().required().min(-90).max(90),
    longitude: yup.number().required().min(-180).max(180),
  }).required('Destination is required'),
  vehicleId: yup.string().required('Vehicle is required'),
});

// Profile Update Schema
export const profileUpdateSchema = yup.object({
  firstName: yup
    .string()
    .min(2, 'First name must be at least 2 characters')
    .optional(),
  lastName: yup
    .string()
    .min(2, 'Last name must be at least 2 characters')
    .optional(),
  phone: yup
    .string()
    .matches(PHONE_REGEX, 'Invalid phone number format')
    .optional(),
  profileImageUrl: yup.string().url('Must be a valid URL').optional(),
});

// Helper validation functions
export const isValidEmail = (email: string): boolean => {
  return EMAIL_REGEX.test(email);
};

export const isValidPhone = (phone: string): boolean => {
  return PHONE_REGEX.test(phone);
};

export const isValidVIN = (vin: string): boolean => {
  return vin.length === VIN_LENGTH;
};

export const isValidLicensePlate = (plate: string): boolean => {
  return LICENSE_PLATE_REGEX.test(plate);
};

export const isValidBatteryLevel = (level: number): boolean => {
  return level >= 0 && level <= 100;
};

export const isValidCoordinate = (lat: number, lng: number): boolean => {
  return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
};

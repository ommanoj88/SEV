import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Grid,
  Alert,
  CircularProgress,
} from '@mui/material';
import { LocationOn as LocationIcon } from '@mui/icons-material';
import { Vehicle, VehicleFormData, VehicleType, VehicleStatus, FuelType } from '../../types';

interface VehicleFormDialogProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: VehicleFormData) => Promise<void>;
  vehicle?: Vehicle;
  companyId: number;
  loading?: boolean;
}

const VehicleFormDialog: React.FC<VehicleFormDialogProps> = ({
  open,
  onClose,
  onSubmit,
  vehicle,
  companyId,
  loading = false,
}) => {
  const [formData, setFormData] = useState<VehicleFormData>({
    vehicleNumber: '',
    vin: '',
    make: '',
    model: '',
    year: new Date().getFullYear(),
    type: VehicleType.TWO_WHEELER,
    fuelType: FuelType.EV,
    licensePlate: '',
    color: '',
    batteryCapacity: 0,
    status: VehicleStatus.ACTIVE,
    currentBatterySoc: 100,
    companyId: companyId,
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitError, setSubmitError] = useState<string | null>(null);

  useEffect(() => {
    if (vehicle) {
      setFormData({
        vehicleNumber: vehicle.vin || '',
        vin: vehicle.vin || '',
        make: vehicle.make,
        model: vehicle.model,
        year: vehicle.year,
        type: vehicle.type,
        fuelType: vehicle.fuelType || FuelType.EV,
        licensePlate: vehicle.licensePlate || '',
        color: vehicle.color || '',
        batteryCapacity: vehicle.battery?.capacity || 0,
        status: vehicle.status,
        currentBatterySoc: vehicle.battery?.stateOfCharge || 100,
        companyId: companyId,
      });
    } else {
      setFormData({
        vehicleNumber: '',
        vin: '',
        make: '',
        model: '',
        year: new Date().getFullYear(),
        type: VehicleType.TWO_WHEELER,
        fuelType: FuelType.EV,
        licensePlate: '',
        color: '',
        batteryCapacity: 0,
        status: VehicleStatus.ACTIVE,
        currentBatterySoc: 100,
        companyId: companyId,
      });
    }
    setErrors({});
    setSubmitError(null);
  }, [vehicle, companyId, open]);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.vehicleNumber.trim()) {
      newErrors.vehicleNumber = 'Vehicle number is required';
    } else if (formData.vehicleNumber.length > 50) {
      newErrors.vehicleNumber = 'Vehicle number must not exceed 50 characters';
    }

    if (!formData.make.trim()) {
      newErrors.make = 'Make is required';
    } else if (formData.make.length > 100) {
      newErrors.make = 'Make must not exceed 100 characters';
    }

    if (!formData.model.trim()) {
      newErrors.model = 'Model is required';
    } else if (formData.model.length > 100) {
      newErrors.model = 'Model must not exceed 100 characters';
    }

    if (!formData.year || formData.year < 2000 || formData.year > 2100) {
      newErrors.year = 'Year must be between 2000 and 2100';
    }

    if (!formData.type) {
      newErrors.type = 'Vehicle type is required';
    }

    if (!formData.fuelType) {
      newErrors.fuelType = 'Fuel type is required';
    }

    // Validate battery capacity for EV and HYBRID
    if (formData.fuelType === FuelType.EV || formData.fuelType === FuelType.HYBRID) {
      if (!formData.batteryCapacity || formData.batteryCapacity <= 0) {
        newErrors.batteryCapacity = 'Battery capacity is required for EV/Hybrid vehicles';
      }
    }

    // Validate fuel tank capacity for ICE and HYBRID
    if (formData.fuelType === FuelType.ICE || formData.fuelType === FuelType.HYBRID) {
      if (!formData.fuelTankCapacity || formData.fuelTankCapacity <= 0) {
        newErrors.fuelTankCapacity = 'Fuel tank capacity is required for ICE/Hybrid vehicles';
      }
    }

    if (!formData.status) {
      newErrors.status = 'Status is required';
    }

    if (formData.vin && formData.vin.length > 17) {
      newErrors.vin = 'VIN must not exceed 17 characters';
    }

    if (formData.licensePlate && formData.licensePlate.length > 20) {
      newErrors.licensePlate = 'License plate must not exceed 20 characters';
    }

    if (formData.color && formData.color.length > 50) {
      newErrors.color = 'Color must not exceed 50 characters';
    }

    if (
      formData.currentBatterySoc !== undefined &&
      (formData.currentBatterySoc < 0 || formData.currentBatterySoc > 100)
    ) {
      newErrors.currentBatterySoc = 'Battery SOC must be between 0 and 100';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | { name?: string; value: unknown }>
  ) => {
    const { name, value } = e.target as any;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Clear error for this field when user starts editing
    if (errors[name]) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[name];
        return newErrors;
      });
    }
  };

  const handleSelectChange = (name: string) => (e: any) => {
    const value = e.target.value;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Clear error for this field when user starts editing
    if (errors[name]) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[name];
        return newErrors;
      });
    }
  };

  const handleSubmit = async () => {
    if (!validateForm()) {
      setSubmitError('Please fix the errors above');
      return;
    }

    try {
      setSubmitError(null);
      await onSubmit(formData);
      onClose();
    } catch (error: any) {
      setSubmitError(error.message || 'Failed to save vehicle');
    }
  };

  const isEditing = !!vehicle;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEditing ? 'Edit Vehicle' : 'Add Vehicle'}</DialogTitle>
      <DialogContent sx={{ pt: 2 }}>
        {submitError && <Alert severity="error" sx={{ mb: 2 }}>{submitError}</Alert>}

        <Grid container spacing={2}>
          {/* Vehicle Number */}
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Vehicle Number"
              name="vehicleNumber"
              value={formData.vehicleNumber}
              onChange={handleChange}
              error={!!errors.vehicleNumber}
              helperText={errors.vehicleNumber}
              disabled={loading}
              placeholder="e.g., EV-001"
            />
          </Grid>

          {/* Make */}
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Make"
              name="make"
              value={formData.make}
              onChange={handleChange}
              error={!!errors.make}
              helperText={errors.make}
              disabled={loading}
              placeholder="e.g., Tata"
            />
          </Grid>

          {/* Model */}
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Model"
              name="model"
              value={formData.model}
              onChange={handleChange}
              error={!!errors.model}
              helperText={errors.model}
              disabled={loading}
              placeholder="e.g., Nexon EV"
            />
          </Grid>

          {/* Year */}
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Year"
              name="year"
              type="number"
              value={formData.year}
              onChange={handleChange}
              error={!!errors.year}
              helperText={errors.year}
              disabled={loading}
              inputProps={{ min: 2000, max: 2100 }}
            />
          </Grid>

          {/* Vehicle Type */}
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth error={!!errors.type} disabled={loading}>
              <InputLabel>Vehicle Type</InputLabel>
              <Select
                name="type"
                value={formData.type}
                onChange={handleSelectChange('type')}
                label="Vehicle Type"
              >
                <MenuItem value={VehicleType.TWO_WHEELER}>Two Wheeler</MenuItem>
                <MenuItem value={VehicleType.THREE_WHEELER}>Three Wheeler</MenuItem>
                <MenuItem value={VehicleType.LCV}>LCV (Light Commercial)</MenuItem>
              </Select>
              {errors.type && <p style={{ color: '#d32f2f', fontSize: '0.75rem' }}>{errors.type}</p>}
            </FormControl>
          </Grid>

          {/* Fuel Type */}
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth error={!!errors.fuelType} disabled={loading}>
              <InputLabel>Fuel Type</InputLabel>
              <Select
                name="fuelType"
                value={formData.fuelType}
                onChange={handleSelectChange('fuelType')}
                label="Fuel Type"
              >
                <MenuItem value={FuelType.EV}>Electric (EV)</MenuItem>
                <MenuItem value={FuelType.ICE}>Petrol/Diesel (ICE)</MenuItem>
                <MenuItem value={FuelType.HYBRID}>Hybrid (EV + ICE)</MenuItem>
              </Select>
              {errors.fuelType && <p style={{ color: '#d32f2f', fontSize: '0.75rem' }}>{errors.fuelType}</p>}
            </FormControl>
          </Grid>

          {/* GPS-Only tracking notice for 2-wheelers and 3-wheelers with EV */}
          {(formData.type === VehicleType.TWO_WHEELER || formData.type === VehicleType.THREE_WHEELER) && 
           formData.fuelType === FuelType.EV && (
            <Grid item xs={12}>
              <Alert severity="info" icon={<LocationIcon />}>
                <strong>GPS-Only Tracking</strong> - Battery monitoring is not available for {formData.type.replace('_', '-').toLowerCase()}s. 
                Real-time GPS location, speed, and odometer will be tracked. Battery data can be manually reported by drivers.
              </Alert>
            </Grid>
          )}

          {/* Battery Capacity - Only for EV and HYBRID */}
          {(formData.fuelType === FuelType.EV || formData.fuelType === FuelType.HYBRID) && (
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Battery Capacity (kWh)"
                name="batteryCapacity"
                type="number"
                value={formData.batteryCapacity}
                onChange={handleChange}
                error={!!errors.batteryCapacity}
                helperText={errors.batteryCapacity}
                disabled={loading}
                inputProps={{ step: 0.1, min: 0 }}
              />
            </Grid>
          )}

          {/* Fuel Tank Capacity - Only for ICE and HYBRID */}
          {(formData.fuelType === FuelType.ICE || formData.fuelType === FuelType.HYBRID) && (
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Fuel Tank Capacity (L)"
                name="fuelTankCapacity"
                type="number"
                value={formData.fuelTankCapacity || ''}
                onChange={handleChange}
                error={!!errors.fuelTankCapacity}
                helperText={errors.fuelTankCapacity}
                disabled={loading}
                inputProps={{ step: 0.1, min: 0 }}
              />
            </Grid>
          )}

          {/* Status */}
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth error={!!errors.status} disabled={loading}>
              <InputLabel>Status</InputLabel>
              <Select
                name="status"
                value={formData.status}
                onChange={handleSelectChange('status')}
                label="Status"
              >
                <MenuItem value={VehicleStatus.ACTIVE}>Active</MenuItem>
                <MenuItem value={VehicleStatus.INACTIVE}>Inactive</MenuItem>
                <MenuItem value={VehicleStatus.MAINTENANCE}>Maintenance</MenuItem>
                <MenuItem value={VehicleStatus.IN_TRIP}>In Trip</MenuItem>
                <MenuItem value={VehicleStatus.CHARGING}>Charging</MenuItem>
              </Select>
              {errors.status && <p style={{ color: '#d32f2f', fontSize: '0.75rem' }}>{errors.status}</p>}
            </FormControl>
          </Grid>

          {/* VIN */}
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="VIN (Optional)"
              name="vin"
              value={formData.vin}
              onChange={handleChange}
              error={!!errors.vin}
              helperText={errors.vin}
              disabled={loading}
              placeholder="17-character VIN"
            />
          </Grid>

          {/* License Plate */}
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="License Plate (Optional)"
              name="licensePlate"
              value={formData.licensePlate}
              onChange={handleChange}
              error={!!errors.licensePlate}
              helperText={errors.licensePlate}
              disabled={loading}
              placeholder="e.g., MH02AB1234"
            />
          </Grid>

          {/* Color */}
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Color (Optional)"
              name="color"
              value={formData.color}
              onChange={handleChange}
              error={!!errors.color}
              helperText={errors.color}
              disabled={loading}
              placeholder="e.g., White"
            />
          </Grid>

          {/* Current Battery SOC - Only for EV and HYBRID */}
          {(formData.fuelType === FuelType.EV || formData.fuelType === FuelType.HYBRID) && (
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Current Battery Level (%)"
                name="currentBatterySoc"
                type="number"
                value={formData.currentBatterySoc}
                onChange={handleChange}
                error={!!errors.currentBatterySoc}
                helperText={errors.currentBatterySoc}
                disabled={loading}
                inputProps={{ min: 0, max: 100 }}
              />
            </Grid>
          )}

          {/* Current Fuel Level - Only for ICE and HYBRID */}
          {(formData.fuelType === FuelType.ICE || formData.fuelType === FuelType.HYBRID) && (
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Current Fuel Level (L)"
                name="fuelLevel"
                type="number"
                value={formData.fuelLevel || ''}
                onChange={handleChange}
                error={!!errors.fuelLevel}
                helperText={errors.fuelLevel}
                disabled={loading}
                inputProps={{ step: 0.1, min: 0 }}
              />
            </Grid>
          )}
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          Cancel
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={loading}
          startIcon={loading ? <CircularProgress size={20} /> : undefined}
        >
          {loading ? 'Saving...' : isEditing ? 'Update Vehicle' : 'Create Vehicle'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default VehicleFormDialog;

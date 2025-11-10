import React, { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  TextField,
  Button,
  MenuItem,
  Divider,
} from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch } from '@redux/hooks';
import { createVehicle } from '@redux/slices/vehicleSlice';
import { VehicleType, FuelType } from '../../types/vehicle';
import { toast } from 'react-toastify';
import FuelTypeSelector from './FuelTypeSelector';

// Dynamic schema based on fuel type
const getValidationSchema = (fuelType: FuelType) => {
  const baseSchema = {
    vin: yup.string().required('VIN is required').length(17, 'VIN must be 17 characters'),
    make: yup.string().required('Make is required'),
    model: yup.string().required('Model is required'),
    year: yup.number().required('Year is required').min(2010).max(new Date().getFullYear() + 1),
    type: yup.string().required('Type is required'),
    licensePlate: yup.string().required('License plate is required'),
    color: yup.string(),
    fuelType: yup.string().required('Fuel type is required'),
  };

  // Add conditional validation based on fuel type
  if (fuelType === FuelType.EV || fuelType === FuelType.HYBRID) {
    Object.assign(baseSchema, {
      batteryCapacity: yup.number().required('Battery capacity is required for EV/Hybrid vehicles').min(10).max(200),
      range: yup.number().required('Range is required for EV/Hybrid vehicles').min(50).max(500),
    });
  }

  if (fuelType === FuelType.ICE || fuelType === FuelType.HYBRID) {
    Object.assign(baseSchema, {
      fuelTankCapacity: yup.number().required('Fuel tank capacity is required for ICE/Hybrid vehicles').min(10).max(200),
    });
  }

  return yup.object(baseSchema).required();
};

type FormData = {
  vin: string;
  make: string;
  model: string;
  year: number;
  type: string;
  licensePlate: string;
  color?: string;
  fuelType: FuelType;
  batteryCapacity?: number;
  range?: number;
  fuelTankCapacity?: number;
};

const AddVehicle: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const [selectedFuelType, setSelectedFuelType] = useState<FuelType>(FuelType.EV);

  const { control, handleSubmit, formState: { errors, isSubmitting }, setValue, watch } = useForm<FormData>({
    resolver: yupResolver(getValidationSchema(selectedFuelType)) as any,
    defaultValues: {
      year: new Date().getFullYear(),
      fuelType: FuelType.EV,
      batteryCapacity: 75,
      range: 250,
      fuelTankCapacity: 50,
    },
  });

  // Watch fuel type changes
  const fuelType = watch('fuelType');

  const onSubmit = async (data: FormData) => {
    try {
      await dispatch(createVehicle(data as any)).unwrap();
      toast.success('Vehicle added successfully!');
      navigate('/fleet');
    } catch (error: any) {
      toast.error(error.message || 'Failed to add vehicle');
    }
  };

  const handleFuelTypeChange = (newFuelType: FuelType) => {
    setSelectedFuelType(newFuelType);
    setValue('fuelType', newFuelType);
  };

  const showBatteryFields = fuelType === FuelType.EV || fuelType === FuelType.HYBRID;
  const showFuelFields = fuelType === FuelType.ICE || fuelType === FuelType.HYBRID;

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Add New Vehicle
      </Typography>

      <Paper sx={{ p: 3, mt: 3 }}>
        <form onSubmit={handleSubmit(onSubmit)}>
          <Grid container spacing={3}>
            {/* Fuel Type Selector */}
            <Grid item xs={12}>
              <Controller
                name="fuelType"
                control={control}
                render={({ field }) => (
                  <FuelTypeSelector
                    value={field.value}
                    onChange={handleFuelTypeChange}
                    error={!!errors.fuelType}
                    helperText={errors.fuelType?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Divider sx={{ my: 2 }} />
              <Typography variant="h6" gutterBottom>
                Basic Information
              </Typography>
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="vin"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="VIN"
                    fullWidth
                    error={!!errors.vin}
                    helperText={errors.vin?.message}
                    inputProps={{ maxLength: 17 }}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <Controller
                name="licensePlate"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="License Plate"
                    fullWidth
                    error={!!errors.licensePlate}
                    helperText={errors.licensePlate?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <Controller
                name="make"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Make"
                    fullWidth
                    error={!!errors.make}
                    helperText={errors.make?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <Controller
                name="model"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Model"
                    fullWidth
                    error={!!errors.model}
                    helperText={errors.model?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <Controller
                name="year"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Year"
                    type="number"
                    fullWidth
                    error={!!errors.year}
                    helperText={errors.year?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <Controller
                name="type"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    select
                    label="Type"
                    fullWidth
                    error={!!errors.type}
                    helperText={errors.type?.message}
                  >
                    <MenuItem value="sedan">Sedan</MenuItem>
                    <MenuItem value="suv">SUV</MenuItem>
                    <MenuItem value="van">Van</MenuItem>
                    <MenuItem value="truck">Truck</MenuItem>
                    <MenuItem value="bus">Bus</MenuItem>
                  </TextField>
                )}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <Controller
                name="color"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Color (Optional)"
                    fullWidth
                  />
                )}
              />
            </Grid>

            {/* Conditional EV/Hybrid Fields */}
            {showBatteryFields && (
              <>
                <Grid item xs={12}>
                  <Divider sx={{ my: 2 }} />
                  <Typography variant="h6" gutterBottom>
                    Battery Information
                  </Typography>
                </Grid>

                <Grid item xs={12} md={6}>
                  <Controller
                    name="batteryCapacity"
                    control={control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Battery Capacity (kWh)"
                        type="number"
                        fullWidth
                        error={!!errors.batteryCapacity}
                        helperText={errors.batteryCapacity?.message}
                      />
                    )}
                  />
                </Grid>

                <Grid item xs={12} md={6}>
                  <Controller
                    name="range"
                    control={control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Range (miles)"
                        type="number"
                        fullWidth
                        error={!!errors.range}
                        helperText={errors.range?.message}
                      />
                    )}
                  />
                </Grid>
              </>
            )}

            {/* Conditional ICE/Hybrid Fields */}
            {showFuelFields && (
              <>
                <Grid item xs={12}>
                  <Divider sx={{ my: 2 }} />
                  <Typography variant="h6" gutterBottom>
                    Fuel Information
                  </Typography>
                </Grid>

                <Grid item xs={12} md={6}>
                  <Controller
                    name="fuelTankCapacity"
                    control={control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Fuel Tank Capacity (liters)"
                        type="number"
                        fullWidth
                        error={!!errors.fuelTankCapacity}
                        helperText={errors.fuelTankCapacity?.message}
                      />
                    )}
                  />
                </Grid>
              </>
            )}

            <Grid item xs={12}>
              <Box display="flex" gap={2} justifyContent="flex-end">
                <Button
                  variant="outlined"
                  onClick={() => navigate('/fleet')}
                  disabled={isSubmitting}
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? 'Adding...' : 'Add Vehicle'}
                </Button>
              </Box>
            </Grid>
          </Grid>
        </form>
      </Paper>
    </Box>
  );
};

export default AddVehicle;

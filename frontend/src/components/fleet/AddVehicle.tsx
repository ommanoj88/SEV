import React from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  TextField,
  Button,
  MenuItem,
} from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch } from '@redux/hooks';
import { createVehicle } from '@redux/slices/vehicleSlice';
import { VehicleType } from '../../types/vehicle';
import { toast } from 'react-toastify';

const schema = yup.object({
  vin: yup.string().required('VIN is required').length(17, 'VIN must be 17 characters'),
  make: yup.string().required('Make is required'),
  model: yup.string().required('Model is required'),
  year: yup.number().required('Year is required').min(2010).max(new Date().getFullYear() + 1),
  type: yup.string().required('Type is required'),
  licensePlate: yup.string().required('License plate is required'),
  color: yup.string(),
  batteryCapacity: yup.number().required('Battery capacity is required').min(10).max(200),
  range: yup.number().required('Range is required').min(50).max(500),
}).required();

type FormData = yup.InferType<typeof schema>;

const AddVehicle: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();

  const { control, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: yupResolver(schema) as any,
    defaultValues: {
      year: new Date().getFullYear(),
      batteryCapacity: 75,
      range: 250,
    },
  });

  const onSubmit = async (data: FormData) => {
    try {
      await dispatch(createVehicle(data as any)).unwrap();
      toast.success('Vehicle added successfully!');
      navigate('/fleet');
    } catch (error: any) {
      toast.error(error.message || 'Failed to add vehicle');
    }
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Add New Vehicle
      </Typography>

      <Paper sx={{ p: 3, mt: 3 }}>
        <form onSubmit={handleSubmit(onSubmit)}>
          <Grid container spacing={3}>
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

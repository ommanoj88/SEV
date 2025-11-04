import React from 'react';
import { Paper, Typography, Grid, TextField, Button, MenuItem } from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { useAppDispatch, useAppSelector } from '../../redux/hooks';
import { assignDriverToVehicle } from '../../redux/slices/driverSlice';
import { toast } from 'react-toastify';

const AssignDriver: React.FC = () => {
  const dispatch = useAppDispatch();
  const { drivers } = useAppSelector((state) => state.drivers);
  const { vehicles } = useAppSelector((state) => state.vehicles);
  const { control, handleSubmit } = useForm();

  const onSubmit = async (data: any) => {
    try {
      await dispatch(assignDriverToVehicle(data)).unwrap();
      toast.success('Driver assigned successfully!');
    } catch (error: any) {
      toast.error(error.message || 'Failed to assign driver');
    }
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h6" gutterBottom>Assign Driver to Vehicle</Typography>
      <form onSubmit={handleSubmit(onSubmit)}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={6}>
            <Controller name="driverId" control={control} defaultValue="" render={({ field }) => (
              <TextField {...field} select label="Driver" fullWidth required>
                {drivers.map((d) => <MenuItem key={d.id} value={d.id}>{d.firstName} {d.lastName}</MenuItem>)}
              </TextField>
            )} />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller name="vehicleId" control={control} defaultValue="" render={({ field }) => (
              <TextField {...field} select label="Vehicle" fullWidth required>
                {vehicles.filter(v => !v.assignedDriverId).map((v) => <MenuItem key={v.id} value={v.id}>{v.make} {v.model} - {v.licensePlate}</MenuItem>)}
              </TextField>
            )} />
          </Grid>
          <Grid item xs={12}>
            <Button type="submit" variant="contained" fullWidth>Assign Driver</Button>
          </Grid>
        </Grid>
      </form>
    </Paper>
  );
};

export default AssignDriver;

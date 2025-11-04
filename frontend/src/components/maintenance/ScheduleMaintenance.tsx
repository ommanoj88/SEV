import React from 'react';
import { Paper, Typography, Grid, TextField, Button, MenuItem } from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { useAppDispatch, useAppSelector } from '../../redux/hooks';
import { scheduleMaintenance } from '../../redux/slices/maintenanceSlice';
import { toast } from 'react-toastify';

const ScheduleMaintenance: React.FC = () => {
  const dispatch = useAppDispatch();
  const { vehicles } = useAppSelector((state) => state.vehicles);
  const { control, handleSubmit } = useForm();

  const onSubmit = async (data: any) => {
    try {
      await dispatch(scheduleMaintenance(data)).unwrap();
      toast.success('Maintenance scheduled successfully!');
    } catch (error: any) {
      toast.error(error.message || 'Failed to schedule maintenance');
    }
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h6" gutterBottom>Schedule Maintenance</Typography>
      <form onSubmit={handleSubmit(onSubmit)}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={6}>
            <Controller name="vehicleId" control={control} defaultValue="" render={({ field }) => (
              <TextField {...field} select label="Vehicle" fullWidth required>
                {vehicles.map((v) => <MenuItem key={v.id} value={v.id}>{v.make} {v.model} - {v.licensePlate}</MenuItem>)}
              </TextField>
            )} />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller name="type" control={control} defaultValue="" render={({ field }) => (
              <TextField {...field} select label="Service Type" fullWidth required>
                <MenuItem value="scheduled">Scheduled</MenuItem>
                <MenuItem value="preventive">Preventive</MenuItem>
                <MenuItem value="repair">Repair</MenuItem>
                <MenuItem value="inspection">Inspection</MenuItem>
              </TextField>
            )} />
          </Grid>
          <Grid item xs={12}>
            <Controller name="title" control={control} defaultValue="" render={({ field }) => (
              <TextField {...field} label="Title" fullWidth required />
            )} />
          </Grid>
          <Grid item xs={12}>
            <Controller name="description" control={control} defaultValue="" render={({ field }) => (
              <TextField {...field} label="Description" fullWidth multiline rows={3} required />
            )} />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller name="scheduledDate" control={control} defaultValue="" render={({ field }) => (
              <TextField {...field} label="Scheduled Date" type="date" fullWidth required InputLabelProps={{ shrink: true }} />
            )} />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller name="serviceProvider" control={control} defaultValue="" render={({ field }) => (
              <TextField {...field} label="Service Provider" fullWidth />
            )} />
          </Grid>
          <Grid item xs={12}>
            <Button type="submit" variant="contained" fullWidth>Schedule Maintenance</Button>
          </Grid>
        </Grid>
      </form>
    </Paper>
  );
};

export default ScheduleMaintenance;

import React from 'react';
import { Paper, Typography, Grid, TextField, Button, MenuItem } from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { useAppDispatch, useAppSelector } from '@redux/hooks';
import { startChargingSession } from '@redux/slices/chargingSlice';
import { toast } from 'react-toastify';

const StartChargingSession: React.FC = () => {
  const dispatch = useAppDispatch();
  const { vehicles } = useAppSelector((state) => state.vehicles);
  const { stations } = useAppSelector((state) => state.charging);
  const { control, handleSubmit } = useForm();

  const onSubmit = async (data: any) => {
    try {
      await dispatch(startChargingSession(data)).unwrap();
      toast.success('Charging session started!');
    } catch (error: any) {
      toast.error(error.message || 'Failed to start session');
    }
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h6" gutterBottom>Start Charging Session</Typography>
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
            <Controller name="stationId" control={control} defaultValue="" render={({ field }) => (
              <TextField {...field} select label="Charging Station" fullWidth required>
                {stations.map((s) => <MenuItem key={s.id} value={s.id}>{s.name}</MenuItem>)}
              </TextField>
            )} />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller name="targetBatteryLevel" control={control} defaultValue={80} render={({ field }) => (
              <TextField {...field} label="Target Battery %" type="number" fullWidth inputProps={{ min: 20, max: 100 }} />
            )} />
          </Grid>
          <Grid item xs={12}>
            <Button type="submit" variant="contained" fullWidth>Start Charging</Button>
          </Grid>
        </Grid>
      </form>
    </Paper>
  );
};

export default StartChargingSession;

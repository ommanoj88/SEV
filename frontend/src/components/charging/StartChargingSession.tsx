import React from 'react';
import { Paper, Typography, Grid, TextField, Button, MenuItem, Alert, AlertTitle } from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { useAppDispatch, useAppSelector } from '@redux/hooks';
import { startChargingSession } from '@redux/slices/chargingSlice';
import { toast } from 'react-toastify';
import { VehicleType, FuelType } from '../../types/vehicle';

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

  // Filter to show only 4-wheeler EVs (LCV type + EV fuel)
  const chargingEligibleVehicles = vehicles.filter(v =>
    v.type === VehicleType.LCV && v.fuelType === FuelType.EV
  );

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h6" gutterBottom>Start Charging Session</Typography>

      <Alert severity="info" sx={{ mb: 3 }}>
        <AlertTitle>Charging Management</AlertTitle>
        Available for 4-wheeler EVs with battery tracking (Teltonika FMC003 OBD-II devices).
        For 2-wheelers and 3-wheelers, we track GPS location only.
      </Alert>

      <form onSubmit={handleSubmit(onSubmit)}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={6}>
            <Controller name="vehicleId" control={control} defaultValue="" render={({ field }) => (
              <TextField
                {...field}
                select
                label="Vehicle"
                fullWidth
                required
                helperText="Only 4-wheeler EVs with battery tracking"
              >
                {chargingEligibleVehicles.length > 0 ? (
                  chargingEligibleVehicles.map((v) => (
                    <MenuItem key={v.id} value={v.id}>
                      {v.make} {v.model} - {v.licensePlate} ({v.battery?.stateOfCharge || 0}%)
                    </MenuItem>
                  ))
                ) : (
                  <MenuItem disabled>No eligible vehicles available</MenuItem>
                )}
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

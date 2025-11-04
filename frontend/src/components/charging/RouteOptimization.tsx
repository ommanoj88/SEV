import React, { useState } from 'react';
import { Box, Paper, Typography, Grid, TextField, Button, Card, CardContent } from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { useAppDispatch } from '@redux/hooks';
import { optimizeChargingRoute } from '@redux/slices/chargingSlice';
import { formatDuration } from '@utils/formatters';

const RouteOptimization: React.FC = () => {
  const dispatch = useAppDispatch();
  const { control, handleSubmit } = useForm();
  const [result, setResult] = useState<any>(null);

  const onSubmit = async (data: any) => {
    try {
      const res = await dispatch(optimizeChargingRoute(data)).unwrap();
      setResult(res);
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <Box>
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>Plan Optimal Route</Typography>
        <form onSubmit={handleSubmit(onSubmit)}>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <Controller name="origin" control={control} defaultValue="" render={({ field }) => (
                <TextField {...field} label="Origin Address" fullWidth required />
              )} />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller name="destination" control={control} defaultValue="" render={({ field }) => (
                <TextField {...field} label="Destination Address" fullWidth required />
              )} />
            </Grid>
            <Grid item xs={12}>
              <Button type="submit" variant="contained">Optimize Route</Button>
            </Grid>
          </Grid>
        </form>
      </Paper>

      {result && (
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>Optimized Route</Typography>
          <Grid container spacing={2}>
            <Grid item xs={12} md={3}>
              <Card><CardContent>
                <Typography variant="caption" color="text.secondary">Distance</Typography>
                <Typography variant="h6">{result.distance} mi</Typography>
              </CardContent></Card>
            </Grid>
            <Grid item xs={12} md={3}>
              <Card><CardContent>
                <Typography variant="caption" color="text.secondary">Duration</Typography>
                <Typography variant="h6">{formatDuration(result.duration)}</Typography>
              </CardContent></Card>
            </Grid>
            <Grid item xs={12} md={3}>
              <Card><CardContent>
                <Typography variant="caption" color="text.secondary">Charging Stops</Typography>
                <Typography variant="h6">{result.chargingStops?.length || 0}</Typography>
              </CardContent></Card>
            </Grid>
            <Grid item xs={12} md={3}>
              <Card><CardContent>
                <Typography variant="caption" color="text.secondary">Total Cost</Typography>
                <Typography variant="h6">${result.totalCost?.toFixed(2)}</Typography>
              </CardContent></Card>
            </Grid>
          </Grid>
        </Paper>
      )}
    </Box>
  );
};

export default RouteOptimization;

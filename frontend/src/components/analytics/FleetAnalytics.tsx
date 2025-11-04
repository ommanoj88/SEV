import React, { useEffect } from 'react';
import { Grid, Paper, Typography } from '@mui/material';
import { useAppDispatch, useAppSelector } from '../../redux/hooks';
import { fetchFleetSummary } from '../../redux/slices/analyticsSlice';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { useTheme } from '@mui/material/styles';

const FleetAnalytics: React.FC = () => {
  const theme = useTheme();
  const dispatch = useAppDispatch();
  const { fleetAnalytics } = useAppSelector((state) => state.analytics);

  useEffect(() => {
    dispatch(fetchFleetSummary(undefined));
  }, [dispatch]);

  return (
    <Grid container spacing={3}>
      <Grid item xs={12} md={3}>
        <Paper sx={{ p: 3 }}>
          <Typography variant="h4">
            {(fleetAnalytics?.summary?.totalVehicles ?? 0)}
          </Typography>
          <Typography color="text.secondary">Total Vehicles</Typography>
        </Paper>
      </Grid>
      <Grid item xs={12} md={3}>
        <Paper sx={{ p: 3 }}>
          <Typography variant="h4">
            {(fleetAnalytics?.summary?.totalTrips ?? 0)}
          </Typography>
          <Typography color="text.secondary">Total Trips</Typography>
        </Paper>
      </Grid>
      <Grid item xs={12} md={3}>
        <Paper sx={{ p: 3 }}>
          <Typography variant="h4">
            {(fleetAnalytics?.summary?.totalDistance?.toLocaleString() ?? 0)} mi
          </Typography>
          <Typography color="text.secondary">Total Distance</Typography>
        </Paper>
      </Grid>
      <Grid item xs={12} md={3}>
        <Paper sx={{ p: 3 }}>
          <Typography variant="h4">
            {(fleetAnalytics?.summary?.averageUtilization?.toFixed(1) ?? 0)}%
          </Typography>
          <Typography color="text.secondary">Avg Utilization</Typography>
        </Paper>
      </Grid>
      <Grid item xs={12}>
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>Fleet Utilization Trend</Typography>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={(fleetAnalytics?.trends?.utilization ?? [])}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Line type="monotone" dataKey="value" stroke={theme.palette.primary.main} />
            </LineChart>
          </ResponsiveContainer>
        </Paper>
      </Grid>
    </Grid>
  );
};

export default FleetAnalytics;

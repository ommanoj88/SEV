import React, { useEffect } from 'react';
import { Box, Grid, Typography, Card, CardContent, LinearProgress } from '@mui/material';
import { useAppDispatch, useAppSelector } from '../redux/hooks';
import { fetchFleetSummary, selectFleetAnalytics } from '../redux/slices/analyticsSlice';
import { fetchAlerts, selectAlerts } from '../redux/slices/notificationSlice';
import FleetSummaryCard from '../components/dashboard/FleetSummaryCard';
import { Battery80, TrendingUp, Warning } from '@mui/icons-material';
import { formatNumber, formatPercentage } from '../utils/formatters';
import { FleetAnalytics } from '../types';

const DashboardPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const analytics = useAppSelector(selectFleetAnalytics);
  const alerts = useAppSelector(selectAlerts);
  const { loading: analyticsLoading, error: analyticsError } = useAppSelector((state) => state.analytics);
  const { loading: notificationsLoading, error: notificationsError } = useAppSelector((state) => state.notifications);

  useEffect(() => {
    dispatch(fetchFleetSummary(undefined));
    dispatch(fetchAlerts());
  }, [dispatch]);

  // Show loading only while actively fetching (not on error)
  if ((analyticsLoading || notificationsLoading) && !analytics) {
    return <LinearProgress />;
  }

  // Use default values if analytics failed to load
  const safeAnalytics: FleetAnalytics = analytics || {
    totalVehicles: 0,
    activeVehicles: 0,
    inactiveVehicles: 0,
    chargingVehicles: 0,
    maintenanceVehicles: 0,
    inTripVehicles: 0,
    averageBatteryLevel: 0,
    averageBatteryHealth: 0,
    totalDistance: 0,
    totalTrips: 0,
    totalEnergyConsumed: 0,
    utilizationRate: 0,
    averageUtilization: 0,
  };

  const unresolvedAlerts = alerts.filter(a => !a.resolved);
  const criticalAlerts = unresolvedAlerts.filter(a => a.severity === 'CRITICAL' || a.severity === 'HIGH');

  return (
    <Box>
      <Typography variant="h4" fontWeight={600} gutterBottom>
        Dashboard
      </Typography>
      <Typography variant="body2" color="text.secondary" paragraph>
        Welcome back! Here's an overview of your fleet.
      </Typography>

      <Grid container spacing={3}>
        {/* Fleet Summary */}
        <Grid item xs={12} lg={8}>
          <FleetSummaryCard analytics={safeAnalytics} />
        </Grid>

        {/* Quick Stats */}
        <Grid item xs={12} lg={4}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1} mb={1}>
                    <Battery80 color="success" />
                    <Typography variant="subtitle2" color="text.secondary">
                      Avg Battery Level
                    </Typography>
                  </Box>
                  <Typography variant="h4" fontWeight={700} color="success.main">
                    {formatPercentage(safeAnalytics.averageBatteryLevel)}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1} mb={1}>
                    <TrendingUp color="primary" />
                    <Typography variant="subtitle2" color="text.secondary">
                      Utilization Rate
                    </Typography>
                  </Box>
                  <Typography variant="h4" fontWeight={700} color="primary">
                    {formatPercentage(safeAnalytics.utilizationRate)}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12}>
              <Card sx={{ bgcolor: criticalAlerts.length > 0 ? 'error.light' : 'background.paper' }}>
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1} mb={1}>
                    <Warning color="error" />
                    <Typography variant="subtitle2" color="text.secondary">
                      Active Alerts
                    </Typography>
                  </Box>
                  <Typography variant="h4" fontWeight={700} color="error.main">
                    {unresolvedAlerts.length}
                  </Typography>
                  {criticalAlerts.length > 0 && (
                    <Typography variant="caption" color="error.dark">
                      {criticalAlerts.length} critical
                    </Typography>
                  )}
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </Grid>

        {/* Recent Stats */}
        <Grid item xs={12} md={6} lg={3}>
          <Card>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                Total Distance
              </Typography>
              <Typography variant="h5" fontWeight={600}>
                {formatNumber(safeAnalytics.totalDistance)} km
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6} lg={3}>
          <Card>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                Total Trips
              </Typography>
              <Typography variant="h5" fontWeight={600}>
                {formatNumber(safeAnalytics.totalTrips)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6} lg={3}>
          <Card>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                Energy Consumed
              </Typography>
              <Typography variant="h5" fontWeight={600}>
                {formatNumber(safeAnalytics.totalEnergyConsumed)} kWh
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6} lg={3}>
          <Card>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                Battery Health
              </Typography>
              <Typography variant="h5" fontWeight={600}>
                {formatPercentage(safeAnalytics.averageBatteryHealth)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default DashboardPage;

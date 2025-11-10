import React, { useEffect } from 'react';
import { Grid, Container, Typography, Box, Paper, Button } from '@mui/material';
import { Refresh as RefreshIcon } from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '@redux/hooks';
import { fetchFleetAnalyticsByCompany } from '@redux/slices/analyticsSlice';
import { fetchVehicles } from '@redux/slices/vehicleSlice';
import { fetchAlerts } from '@redux/slices/notificationSlice';
import FleetSummaryCard from './FleetSummaryCard';
import BatterySummaryCard from './BatterySummaryCard';
import AlertsCard from './AlertsCard';
import UtilizationChart from './UtilizationChart';
import FleetCompositionCard from './FleetCompositionCard';
import CostBreakdownCard from './CostBreakdownCard';
import MaintenanceAlertsCard from './MaintenanceAlertsCard';
import LoadingSpinner from '@components/common/LoadingSpinner';

const Dashboard: React.FC = () => {
  const dispatch = useAppDispatch();
  const { fleetAnalytics: metrics, loading } = useAppSelector((state) => state.analytics);
  const { user } = useAppSelector((state) => state.auth);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = () => {
    // Default to company ID 1 for now - in production this should come from user context
    dispatch(fetchFleetAnalyticsByCompany(1));
    dispatch(fetchVehicles());
    dispatch(fetchAlerts());
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" gutterBottom>
            Dashboard
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Welcome back, {user?.firstName || 'User'}! Here's your fleet overview.
          </Typography>
        </Box>
        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          onClick={loadDashboardData}
        >
          Refresh
        </Button>
      </Box>

      <Grid container spacing={3}>
        {/* Fleet Summary */}
        <Grid item xs={12}>
          <FleetSummaryCard analytics={metrics} />
        </Grid>

        {/* Fleet Composition - NEW PR 16 */}
        <Grid item xs={12} md={6} lg={4}>
          <FleetCompositionCard />
        </Grid>

        {/* Cost Breakdown - NEW PR 16 */}
        <Grid item xs={12} md={6} lg={4}>
          <CostBreakdownCard />
        </Grid>

        {/* Maintenance Alerts - NEW PR 16 */}
        <Grid item xs={12} md={12} lg={4}>
          <MaintenanceAlertsCard />
        </Grid>

        {/* Battery Summary */}
        <Grid item xs={12} md={6}>
          <BatterySummaryCard />
        </Grid>

        {/* Alerts */}
        <Grid item xs={12} md={6}>
          <AlertsCard />
        </Grid>

        {/* Fleet Utilization Chart */}
        <Grid item xs={12} lg={8}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Typography variant="h6" gutterBottom>
              Fleet Utilization
            </Typography>
            <UtilizationChart />
          </Paper>
        </Grid>

        {/* Quick Stats */}
        <Grid item xs={12} lg={4}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Typography variant="h6" gutterBottom>
              Quick Stats
            </Typography>
            <Box sx={{ mt: 2 }}>
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Average Battery Level
                </Typography>
                <Typography variant="h5">
                  {metrics?.averageBatteryLevel?.toFixed(1) || 0}%
                </Typography>
              </Box>
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Total Distance Today
                </Typography>
                <Typography variant="h5">
                  {metrics?.totalDistance?.toLocaleString() || 0} miles
                </Typography>
              </Box>
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Fleet Utilization
                </Typography>
                <Typography variant="h5">
                  {metrics?.utilizationRate?.toFixed(1) || 0}%
                </Typography>
              </Box>
              <Box>
                <Typography variant="body2" color="text.secondary">
                  Active Drivers
                </Typography>
                <Typography variant="h5">
                  {0}
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Dashboard;

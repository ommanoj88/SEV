import React, { useEffect } from 'react';
import { Box, Grid, Typography, Card, CardContent, LinearProgress, alpha } from '@mui/material';
import { useAppDispatch, useAppSelector } from '../redux/hooks';
import { fetchFleetSummary, selectFleetAnalytics } from '../redux/slices/analyticsSlice';
import { fetchAlerts, selectAlerts } from '../redux/slices/notificationSlice';
import FleetSummaryCard from '../components/dashboard/FleetSummaryCard';
import { Battery80, TrendingUp, Warning, DirectionsCar } from '@mui/icons-material';
import { formatNumber, formatPercentage } from '../utils/formatters';
import { FleetAnalytics } from '../types';

const DashboardPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const analytics = useAppSelector(selectFleetAnalytics);
  const alerts = useAppSelector(selectAlerts);
  const { loading: analyticsLoading } = useAppSelector((state) => state.analytics);
  const { loading: notificationsLoading } = useAppSelector((state) => state.notifications);

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

  const StatCard: React.FC<{
    title: string;
    value: string | number;
    icon: React.ReactNode;
    color: string;
    subtitle?: string;
  }> = ({ title, value, icon, color, subtitle }) => (
    <Card 
      className="fade-in"
      sx={{
        position: 'relative',
        overflow: 'hidden',
        '&::before': {
          content: '""',
          position: 'absolute',
          top: 0,
          right: 0,
          width: '100px',
          height: '100px',
          borderRadius: '50%',
          background: (theme) => alpha(color, 0.1),
          transform: 'translate(30%, -30%)',
        },
      }}
    >
      <CardContent>
        <Box display="flex" alignItems="center" gap={1.5} mb={2}>
          <Box 
            sx={{ 
              color,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: 48,
              height: 48,
              borderRadius: 2,
              background: (theme) => alpha(color, 0.1),
            }}
          >
            {icon}
          </Box>
          <Typography variant="subtitle2" color="text.secondary" fontWeight={600}>
            {title}
          </Typography>
        </Box>
        <Typography 
          variant="h3" 
          fontWeight={800} 
          sx={{ 
            color,
            mb: subtitle ? 1 : 0,
          }}
        >
          {value}
        </Typography>
        {subtitle && (
          <Typography variant="caption" color="text.secondary" fontWeight={500}>
            {subtitle}
          </Typography>
        )}
      </CardContent>
    </Card>
  );

  return (
    <Box className="fade-in">
      <Box mb={4}>
        <Typography 
          variant="h3" 
          fontWeight={800}
          sx={{
            background: (theme) => 
              `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.secondary.main} 100%)`,
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            backgroundClip: 'text',
            mb: 1,
          }}
        >
          Dashboard
        </Typography>
        <Typography variant="body1" color="text.secondary" fontWeight={500}>
          Welcome back! Here's an overview of your fleet.
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Fleet Summary */}
        <Grid item xs={12} lg={8}>
          <FleetSummaryCard analytics={safeAnalytics} />
        </Grid>

        {/* Quick Stats */}
        <Grid item xs={12} lg={4}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <StatCard
                title="Avg Battery Level"
                value={formatPercentage(safeAnalytics.averageBatteryLevel)}
                icon={<Battery80 sx={{ fontSize: 28 }} />}
                color="#10B981"
              />
            </Grid>

            <Grid item xs={12}>
              <StatCard
                title="Utilization Rate"
                value={formatPercentage(safeAnalytics.utilizationRate)}
                icon={<TrendingUp sx={{ fontSize: 28 }} />}
                color="#3B82F6"
              />
            </Grid>

            <Grid item xs={12}>
              <Card 
                className="fade-in"
                sx={{
                  position: 'relative',
                  overflow: 'hidden',
                  background: criticalAlerts.length > 0 
                    ? (theme) => alpha(theme.palette.error.main, 0.05)
                    : 'background.paper',
                  '&::before': {
                    content: '""',
                    position: 'absolute',
                    top: 0,
                    right: 0,
                    width: '100px',
                    height: '100px',
                    borderRadius: '50%',
                    background: (theme) => alpha(theme.palette.error.main, 0.1),
                    transform: 'translate(30%, -30%)',
                  },
                }}
              >
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1.5} mb={2}>
                    <Box 
                      sx={{ 
                        color: 'error.main',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        width: 48,
                        height: 48,
                        borderRadius: 2,
                        background: (theme) => alpha(theme.palette.error.main, 0.1),
                      }}
                      className={criticalAlerts.length > 0 ? 'pulse' : ''}
                    >
                      <Warning sx={{ fontSize: 28 }} />
                    </Box>
                    <Typography variant="subtitle2" color="text.secondary" fontWeight={600}>
                      Active Alerts
                    </Typography>
                  </Box>
                  <Typography variant="h3" fontWeight={800} color="error.main" mb={1}>
                    {unresolvedAlerts.length}
                  </Typography>
                  {criticalAlerts.length > 0 && (
                    <Typography variant="caption" color="error.dark" fontWeight={600}>
                      {criticalAlerts.length} critical alerts
                    </Typography>
                  )}
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </Grid>

        {/* Recent Stats - Enhanced Cards */}
        <Grid item xs={12} md={6} lg={3}>
          <StatCard
            title="Total Distance"
            value={`${formatNumber(safeAnalytics.totalDistance)} km`}
            icon={<DirectionsCar sx={{ fontSize: 28 }} />}
            color="#8B5CF6"
          />
        </Grid>

        <Grid item xs={12} md={6} lg={3}>
          <StatCard
            title="Total Trips"
            value={formatNumber(safeAnalytics.totalTrips)}
            icon={<TrendingUp sx={{ fontSize: 28 }} />}
            color="#06B6D4"
          />
        </Grid>

        <Grid item xs={12} md={6} lg={3}>
          <StatCard
            title="Energy Consumed"
            value={`${formatNumber(safeAnalytics.totalEnergyConsumed)} kWh`}
            icon={<Battery80 sx={{ fontSize: 28 }} />}
            color="#F59E0B"
          />
        </Grid>

        <Grid item xs={12} md={6} lg={3}>
          <StatCard
            title="Battery Health"
            value={formatPercentage(safeAnalytics.averageBatteryHealth)}
            icon={<Battery80 sx={{ fontSize: 28 }} />}
            color="#10B981"
          />
        </Grid>
      </Grid>
    </Box>
  );
};

export default DashboardPage;

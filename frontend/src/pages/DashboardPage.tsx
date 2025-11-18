import React, { useEffect } from 'react';
import { Box, Grid, Typography, Card, CardContent, LinearProgress, alpha } from '@mui/material';
import { useAppDispatch, useAppSelector } from '../redux/hooks';
import { fetchFleetSummary, selectFleetAnalytics } from '../redux/slices/analyticsSlice';
import { fetchAlerts, selectAlerts } from '../redux/slices/notificationSlice';
import FleetSummaryCard from '../components/dashboard/FleetSummaryCard';
import { Battery80, TrendingUp, Warning, DirectionsCar } from '@mui/icons-material';
import { formatNumber, formatPercentage } from '../utils/formatters';
import { pluralizeWithCount } from '../utils/helpers';
import { FleetAnalytics } from '../types';

const DashboardPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const user = useAppSelector((state) => state.auth.user);
  const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);
  const analytics = useAppSelector(selectFleetAnalytics);
  const alerts = useAppSelector(selectAlerts) || [];
  const { loading: analyticsLoading } = useAppSelector((state) => state.analytics);
  const { loading: notificationsLoading } = useAppSelector((state) => state.notifications);

  useEffect(() => {
    // Wait for user to be loaded before fetching data
    if (isAuthenticated && user && user.companyId) {
      dispatch(fetchFleetSummary(undefined));
      dispatch(fetchAlerts());
    }
  }, [dispatch, isAuthenticated, user]);

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
        height: '100%',
        background: (theme) => theme.palette.background.paper,
        border: (theme) => `1px solid ${theme.palette.divider}`,
        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        '&:hover': {
          borderColor: alpha(color, 0.5),
          boxShadow: (theme) => `0 8px 24px ${alpha(color, 0.15)}`,
        },
      }}
    >
      <CardContent sx={{ p: 2.5 }}>
        <Box display="flex" alignItems="flex-start" justifyContent="space-between" mb={2}>
          <Typography variant="subtitle2" color="text.secondary" fontWeight={500} textTransform="uppercase" letterSpacing="0.05em">
            {title}
          </Typography>
          <Box 
            sx={{ 
              color,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: 40,
              height: 40,
              borderRadius: 2,
              background: (theme) => alpha(color, 0.1),
            }}
          >
            {icon}
          </Box>
        </Box>
        <Typography 
          variant="h4" 
          fontWeight={700} 
          sx={{ 
            color: 'text.primary',
            mb: subtitle ? 0.5 : 0,
          }}
        >
          {value}
        </Typography>
        {subtitle && (
          <Typography variant="caption" color="text.secondary" fontWeight={400}>
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
          fontWeight={700}
          sx={{
            color: 'text.primary',
            mb: 0.5,
          }}
        >
          Fleet Overview
        </Typography>
        <Typography variant="body1" color="text.secondary" fontWeight={400}>
          Real-time insights and performance metrics for your multi-fuel fleet (ICE, EV, Hybrid)
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
                title="Fleet Health"
                value={formatPercentage(safeAnalytics.averageBatteryLevel)}
                icon={<Battery80 sx={{ fontSize: 28 }} />}
                color="#10B981"
                subtitle="Avg battery/fuel level"
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
                  height: '100%',
                  background: criticalAlerts.length > 0 
                    ? (theme) => alpha(theme.palette.error.main, 0.03)
                    : 'background.paper',
                  border: (theme) => criticalAlerts.length > 0
                    ? `1px solid ${alpha(theme.palette.error.main, 0.3)}`
                    : `1px solid ${theme.palette.divider}`,
                  transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                  '&:hover': {
                    borderColor: (theme) => alpha(theme.palette.error.main, 0.5),
                    boxShadow: (theme) => `0 8px 24px ${alpha(theme.palette.error.main, 0.15)}`,
                  },
                }}
              >
                <CardContent sx={{ p: 2.5 }}>
                  <Box display="flex" alignItems="flex-start" justifyContent="space-between" mb={2}>
                    <Typography variant="subtitle2" color="text.secondary" fontWeight={500} textTransform="uppercase" letterSpacing="0.05em">
                      Active Alerts
                    </Typography>
                    <Box 
                      sx={{ 
                        color: 'error.main',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        width: 40,
                        height: 40,
                        borderRadius: 2,
                        background: (theme) => alpha(theme.palette.error.main, 0.1),
                      }}
                      className={criticalAlerts.length > 0 ? 'pulse' : ''}
                    >
                      <Warning sx={{ fontSize: 24 }} />
                    </Box>
                  </Box>
                  <Typography variant="h4" fontWeight={700} color="error.main" mb={0.5}>
                    {unresolvedAlerts.length}
                  </Typography>
                  {criticalAlerts.length > 0 && (
                    <Typography variant="caption" color="error.dark" fontWeight={500}>
                      {pluralizeWithCount(criticalAlerts.length, 'critical alert')} require attention
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

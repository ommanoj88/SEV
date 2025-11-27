import React, { useEffect } from 'react';
import { 
  Box, 
  Grid, 
  Typography, 
  Card, 
  CardContent,
  alpha,
  Skeleton,
  Chip,
} from '@mui/material';
import { useAppDispatch, useAppSelector } from '../redux/hooks';
import { fetchFleetSummary, selectFleetAnalytics } from '../redux/slices/analyticsSlice';
import { fetchAlerts, selectAlerts } from '../redux/slices/notificationSlice';
import FleetSummaryCard from '../components/dashboard/FleetSummaryCard';
import { 
  Battery80, 
  TrendingUp, 
  Warning, 
  DirectionsCar,
  Speed,
  Bolt,
  ArrowUpward,
  ArrowDownward,
} from '@mui/icons-material';
import { formatNumber, formatPercentage } from '../utils/formatters';
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
    if (isAuthenticated && user && user.companyId) {
      dispatch(fetchFleetSummary(undefined));
      dispatch(fetchAlerts());
    }
  }, [dispatch, isAuthenticated, user]);

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

  interface MetricCardProps {
    title: string;
    value: string | number;
    icon: React.ReactNode;
    color: string;
    subtitle?: string;
    trend?: { value: number; isPositive: boolean };
    loading?: boolean;
  }

  const MetricCard: React.FC<MetricCardProps> = ({ 
    title, 
    value, 
    icon, 
    color, 
    subtitle,
    trend,
    loading = false,
  }) => (
    <Card 
      className="fade-in"
      sx={{
        height: '100%',
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      <CardContent sx={{ p: 2.5, pb: '20px !important' }}>
        {/* Header */}
        <Box display="flex" alignItems="flex-start" justifyContent="space-between" mb={2}>
          <Typography 
            variant="overline" 
            color="text.secondary" 
            fontWeight={600}
            sx={{ letterSpacing: '0.06em' }}
          >
            {title}
          </Typography>
          <Box 
            sx={{ 
              width: 40,
              height: 40,
              borderRadius: 2,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              background: alpha(color, 0.1),
              color: color,
            }}
          >
            {icon}
          </Box>
        </Box>

        {/* Value */}
        {loading ? (
          <Skeleton variant="text" width="60%" height={44} />
        ) : (
          <Typography 
            variant="h4" 
            fontWeight={700} 
            color="text.primary"
            sx={{ mb: 0.5 }}
          >
            {value}
          </Typography>
        )}

        {/* Subtitle & Trend */}
        <Box display="flex" alignItems="center" justifyContent="space-between" mt={1}>
          {subtitle && (
            <Typography variant="body2" color="text.secondary">
              {subtitle}
            </Typography>
          )}
          {trend && (
            <Chip
              size="small"
              icon={trend.isPositive ? 
                <ArrowUpward sx={{ fontSize: 14 }} /> : 
                <ArrowDownward sx={{ fontSize: 14 }} />
              }
              label={`${Math.abs(trend.value)}%`}
              sx={{
                height: 22,
                fontSize: '0.7rem',
                fontWeight: 600,
                bgcolor: trend.isPositive ? alpha('#00875A', 0.1) : alpha('#DE350B', 0.1),
                color: trend.isPositive ? '#00875A' : '#DE350B',
                '& .MuiChip-icon': {
                  color: 'inherit',
                },
              }}
            />
          )}
        </Box>
      </CardContent>

      {/* Bottom accent line */}
      <Box
        sx={{
          position: 'absolute',
          bottom: 0,
          left: 0,
          right: 0,
          height: 3,
          background: `linear-gradient(90deg, ${color}, ${alpha(color, 0.4)})`,
        }}
      />
    </Card>
  );

  const AlertCard: React.FC = () => (
    <Card 
      className="fade-in"
      sx={{
        height: '100%',
        position: 'relative',
        overflow: 'hidden',
        border: criticalAlerts.length > 0 
          ? (theme) => `1px solid ${alpha(theme.palette.error.main, 0.2)}`
          : undefined,
      }}
    >
      <CardContent sx={{ p: 2.5, pb: '20px !important' }}>
        <Box display="flex" alignItems="flex-start" justifyContent="space-between" mb={2}>
          <Typography 
            variant="overline" 
            color="text.secondary" 
            fontWeight={600}
            sx={{ letterSpacing: '0.06em' }}
          >
            Active Alerts
          </Typography>
          <Box 
            sx={{ 
              width: 40,
              height: 40,
              borderRadius: 2,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              background: (theme) => alpha(theme.palette.error.main, 0.1),
              color: 'error.main',
            }}
            className={criticalAlerts.length > 0 ? 'pulse' : ''}
          >
            <Warning />
          </Box>
        </Box>

        <Typography 
          variant="h4" 
          fontWeight={700} 
          color={unresolvedAlerts.length > 0 ? 'error.main' : 'text.primary'}
          sx={{ mb: 0.5 }}
        >
          {unresolvedAlerts.length}
        </Typography>

        {criticalAlerts.length > 0 ? (
          <Box display="flex" alignItems="center" gap={1} mt={1}>
            <Chip
              size="small"
              label={`${criticalAlerts.length} Critical`}
              sx={{
                height: 22,
                fontSize: '0.7rem',
                fontWeight: 600,
                bgcolor: (theme) => alpha(theme.palette.error.main, 0.1),
                color: 'error.main',
              }}
            />
            <Typography variant="body2" color="text.secondary">
              require attention
            </Typography>
          </Box>
        ) : (
          <Typography variant="body2" color="text.secondary">
            No critical alerts
          </Typography>
        )}
      </CardContent>

      <Box
        sx={{
          position: 'absolute',
          bottom: 0,
          left: 0,
          right: 0,
          height: 3,
          background: (theme) => 
            `linear-gradient(90deg, ${theme.palette.error.main}, ${alpha(theme.palette.error.main, 0.4)})`,
        }}
      />
    </Card>
  );

  // Show loading skeleton while data is loading
  if ((analyticsLoading || notificationsLoading) && !analytics) {
    return (
      <Box className="fade-in">
        <Box mb={4}>
          <Skeleton variant="text" width={280} height={40} />
          <Skeleton variant="text" width={400} height={24} />
        </Box>
        <Grid container spacing={3}>
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <Grid item xs={12} sm={6} lg={4} xl={3} key={i}>
              <Skeleton variant="rectangular" height={160} sx={{ borderRadius: 3 }} />
            </Grid>
          ))}
        </Grid>
      </Box>
    );
  }

  return (
    <Box className="fade-in">
      {/* Page Header */}
      <Box mb={4}>
        <Box display="flex" alignItems="flex-start" justifyContent="space-between">
          <Box>
            <Typography 
              variant="h4" 
              fontWeight={700}
              color="text.primary"
              sx={{ mb: 0.5 }}
            >
              Fleet Overview
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Real-time insights and performance metrics for your multi-fuel fleet
            </Typography>
          </Box>
          <Chip
            label="Live"
            size="small"
            sx={{
              bgcolor: (theme) => alpha(theme.palette.success.main, 0.1),
              color: 'success.main',
              fontWeight: 600,
              '&::before': {
                content: '""',
                width: 6,
                height: 6,
                borderRadius: '50%',
                bgcolor: 'success.main',
                mr: 0.75,
              },
              display: 'flex',
              alignItems: 'center',
            }}
          />
        </Box>
      </Box>

      <Grid container spacing={3}>
        {/* Fleet Summary - Full Width */}
        <Grid item xs={12}>
          <FleetSummaryCard analytics={safeAnalytics} />
        </Grid>

        {/* Key Metrics Row */}
        <Grid item xs={12} sm={6} lg={3}>
          <MetricCard
            title="Fleet Health"
            value={formatPercentage(safeAnalytics.averageBatteryLevel)}
            icon={<Battery80 />}
            color="#00875A"
            subtitle="Avg battery/fuel level"
            trend={{ value: 2.4, isPositive: true }}
            loading={analyticsLoading}
          />
        </Grid>

        <Grid item xs={12} sm={6} lg={3}>
          <MetricCard
            title="Utilization Rate"
            value={formatPercentage(safeAnalytics.utilizationRate)}
            icon={<TrendingUp />}
            color="#0052CC"
            subtitle="Fleet efficiency"
            trend={{ value: 5.2, isPositive: true }}
            loading={analyticsLoading}
          />
        </Grid>

        <Grid item xs={12} sm={6} lg={3}>
          <MetricCard
            title="Total Distance"
            value={`${formatNumber(safeAnalytics.totalDistance)} km`}
            icon={<Speed />}
            color="#6554C0"
            subtitle="This month"
            loading={analyticsLoading}
          />
        </Grid>

        <Grid item xs={12} sm={6} lg={3}>
          <AlertCard />
        </Grid>

        {/* Secondary Metrics Row */}
        <Grid item xs={12} sm={6} lg={3}>
          <MetricCard
            title="Total Trips"
            value={formatNumber(safeAnalytics.totalTrips)}
            icon={<DirectionsCar />}
            color="#0891B2"
            subtitle="Completed trips"
            loading={analyticsLoading}
          />
        </Grid>

        <Grid item xs={12} sm={6} lg={3}>
          <MetricCard
            title="Energy Consumed"
            value={`${formatNumber(safeAnalytics.totalEnergyConsumed)} kWh`}
            icon={<Bolt />}
            color="#FF8B00"
            subtitle="Total consumption"
            loading={analyticsLoading}
          />
        </Grid>

        <Grid item xs={12} sm={6} lg={3}>
          <MetricCard
            title="Battery Health"
            value={formatPercentage(safeAnalytics.averageBatteryHealth)}
            icon={<Battery80 />}
            color="#00875A"
            subtitle="Average across fleet"
            trend={{ value: 1.1, isPositive: true }}
            loading={analyticsLoading}
          />
        </Grid>

        <Grid item xs={12} sm={6} lg={3}>
          <MetricCard
            title="Active Vehicles"
            value={`${safeAnalytics.activeVehicles}/${safeAnalytics.totalVehicles}`}
            icon={<DirectionsCar />}
            color="#0052CC"
            subtitle="Currently on duty"
            loading={analyticsLoading}
          />
        </Grid>
      </Grid>
    </Box>
  );
};

export default DashboardPage;

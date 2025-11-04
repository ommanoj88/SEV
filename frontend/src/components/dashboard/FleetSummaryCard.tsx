import React from 'react';
import { Card, CardContent, Typography, Box, Grid } from '@mui/material';
import { DirectionsCar, LocalParking, EvStation, Build, SignalWifiOff } from '@mui/icons-material';
import { FleetAnalytics } from '../../types';
import { VEHICLE_STATUS_COLORS } from '../../utils/constants';

interface FleetSummaryCardProps {
  analytics: FleetAnalytics | null;
}

const FleetSummaryCard: React.FC<FleetSummaryCardProps> = ({ analytics }) => {
  if (!analytics) return null;

  const statusItems = [
    { label: 'Active', value: analytics.activeVehicles, icon: <DirectionsCar />, color: VEHICLE_STATUS_COLORS.ACTIVE },
    { label: 'Inactive', value: analytics.inactiveVehicles, icon: <LocalParking />, color: VEHICLE_STATUS_COLORS.INACTIVE },
    { label: 'Charging', value: analytics.chargingVehicles, icon: <EvStation />, color: VEHICLE_STATUS_COLORS.CHARGING },
    { label: 'Maintenance', value: analytics.maintenanceVehicles, icon: <Build />, color: VEHICLE_STATUS_COLORS.MAINTENANCE },
    { label: 'In Trip', value: analytics.inTripVehicles, icon: <DirectionsCar />, color: VEHICLE_STATUS_COLORS.IN_TRIP },
  ];

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Fleet Summary
        </Typography>
        <Typography variant="h3" fontWeight={700} color="primary" gutterBottom>
          {analytics.totalVehicles}
        </Typography>
        <Typography variant="body2" color="text.secondary" paragraph>
          Total Vehicles
        </Typography>

        <Grid container spacing={2}>
          {statusItems.map((item) => (
            <Grid item xs={12} sm={6} md={4} key={item.label}>
              <Box display="flex" alignItems="center" gap={1}>
                <Box sx={{ color: item.color }}>{item.icon}</Box>
                <Box>
                  <Typography variant="h6" fontWeight={600}>
                    {item.value}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {item.label}
                  </Typography>
                </Box>
              </Box>
            </Grid>
          ))}
        </Grid>
      </CardContent>
    </Card>
  );
};

export default FleetSummaryCard;

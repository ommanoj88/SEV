import React from 'react';
import { Card, CardContent, Typography, Box, Grid, alpha } from '@mui/material';
import { DirectionsCar, LocalParking, EvStation, Build } from '@mui/icons-material';
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
    <Card 
      className="fade-in"
      sx={{
        height: '100%',
        background: (theme) => theme.palette.background.paper,
        border: (theme) => `1px solid ${theme.palette.divider}`,
      }}
    >
      <CardContent sx={{ p: 3 }}>
        <Box display="flex" alignItems="center" justifyContent="space-between" mb={3}>
          <Typography 
            variant="h6" 
            fontWeight={600}
            color="text.primary"
          >
            Fleet Summary
          </Typography>
          <Box 
            sx={{ 
              px: 2,
              py: 0.75,
              borderRadius: 2,
              background: (theme) => alpha(theme.palette.primary.main, 0.1),
              border: (theme) => `1px solid ${alpha(theme.palette.primary.main, 0.2)}`,
            }}
          >
            <Typography variant="caption" color="text.secondary" fontWeight={600} textTransform="uppercase" letterSpacing="0.05em">
              Total Vehicles
            </Typography>
            <Typography 
              variant="h5" 
              fontWeight={700}
              color="primary.main"
              sx={{ mt: 0.5 }}
            >
              {analytics.totalVehicles}
            </Typography>
          </Box>
        </Box>

        <Grid container spacing={2}>
          {statusItems.map((item) => (
            <Grid item xs={12} sm={6} md={4} lg={2.4} key={item.label}>
              <Box 
                sx={{ 
                  p: 2, 
                  borderRadius: 2,
                  background: (theme) => theme.palette.background.paper,
                  border: (theme) => `1px solid ${alpha(item.color, 0.2)}`,
                  transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                  '&:hover': {
                    transform: 'translateY(-2px)',
                    borderColor: alpha(item.color, 0.5),
                    boxShadow: (theme) => `0px 4px 16px ${alpha(item.color, 0.15)}`,
                  },
                }}
              >
                <Box display="flex" alignItems="center" gap={1.5}>
                  <Box 
                    sx={{ 
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      width: 36,
                      height: 36,
                      borderRadius: 1.5,
                      color: item.color,
                      background: (theme) => alpha(item.color, 0.1),
                    }}
                  >
                    {item.icon}
                  </Box>
                  <Box flex={1}>
                    <Typography 
                      variant="h6" 
                      fontWeight={700}
                      sx={{ color: 'text.primary', lineHeight: 1.2 }}
                    >
                      {item.value}
                    </Typography>
                    <Typography 
                      variant="caption" 
                      color="text.secondary"
                      fontWeight={500}
                    >
                      {item.label}
                    </Typography>
                  </Box>
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

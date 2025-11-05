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
        position: 'relative',
        overflow: 'hidden',
        '&::before': {
          content: '""',
          position: 'absolute',
          top: 0,
          right: 0,
          width: '200px',
          height: '200px',
          borderRadius: '50%',
          background: (theme) => alpha(theme.palette.primary.main, 0.05),
          transform: 'translate(30%, -30%)',
        },
      }}
    >
      <CardContent sx={{ p: 3 }}>
        <Typography 
          variant="h5" 
          gutterBottom 
          fontWeight={700}
          sx={{ mb: 2 }}
        >
          Fleet Summary
        </Typography>
        
        <Box 
          sx={{ 
            mb: 3,
            p: 3,
            borderRadius: 3,
            background: (theme) => 
              `linear-gradient(135deg, ${alpha(theme.palette.primary.main, 0.1)} 0%, ${alpha(theme.palette.secondary.main, 0.1)} 100%)`,
            border: (theme) => `1px solid ${alpha(theme.palette.primary.main, 0.2)}`,
          }}
        >
          <Typography variant="caption" color="text.secondary" fontWeight={700} sx={{ mb: 1, display: 'block' }}>
            TOTAL VEHICLES
          </Typography>
          <Typography 
            variant="h2" 
            fontWeight={900}
            sx={{
              background: (theme) => 
                `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.secondary.main} 100%)`,
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              backgroundClip: 'text',
            }}
          >
            {analytics.totalVehicles}
          </Typography>
        </Box>

        <Grid container spacing={2}>
          {statusItems.map((item) => (
            <Grid item xs={12} sm={6} md={4} key={item.label}>
              <Box 
                sx={{ 
                  p: 2, 
                  borderRadius: 2,
                  background: (theme) => alpha(item.color, 0.05),
                  border: (theme) => `1px solid ${alpha(item.color, 0.2)}`,
                  transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: (theme) => `0px 8px 24px ${alpha(item.color, 0.2)}`,
                  },
                }}
              >
                <Box display="flex" alignItems="center" gap={1.5}>
                  <Box 
                    sx={{ 
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      width: 40,
                      height: 40,
                      borderRadius: 2,
                      color: item.color,
                      background: (theme) => alpha(item.color, 0.1),
                    }}
                  >
                    {item.icon}
                  </Box>
                  <Box>
                    <Typography 
                      variant="h5" 
                      fontWeight={700}
                      sx={{ color: item.color }}
                    >
                      {item.value}
                    </Typography>
                    <Typography 
                      variant="caption" 
                      color="text.secondary"
                      fontWeight={600}
                      sx={{ textTransform: 'uppercase', letterSpacing: '0.05em' }}
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

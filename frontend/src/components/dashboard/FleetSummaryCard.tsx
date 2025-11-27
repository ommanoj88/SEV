import React, { useState } from 'react';
import { Card, CardContent, Typography, Box, Grid, alpha } from '@mui/material';
import { 
  DirectionsCar, 
  LocalParking, 
  EvStation, 
  Build,
  TrendingUp,
} from '@mui/icons-material';
import { FleetAnalytics } from '../../types';
import { VEHICLE_STATUS_COLORS } from '../../utils/constants';
import VehicleDetailsModal from './VehicleDetailsModal';
import vehicleService from '../../services/vehicleService';

interface FleetSummaryCardProps {
  analytics: FleetAnalytics | null;
}

const FleetSummaryCard: React.FC<FleetSummaryCardProps> = ({ analytics }) => {
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [selectedVehicles, setSelectedVehicles] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);

  if (!analytics) return null;

  const statusItems = [
    { 
      label: 'Active', 
      value: analytics.activeVehicles, 
      icon: <DirectionsCar sx={{ fontSize: 20 }} />, 
      color: VEHICLE_STATUS_COLORS.ACTIVE, 
      status: 'ACTIVE',
      description: 'Currently on duty',
    },
    { 
      label: 'Inactive', 
      value: analytics.inactiveVehicles, 
      icon: <LocalParking sx={{ fontSize: 20 }} />, 
      color: VEHICLE_STATUS_COLORS.INACTIVE, 
      status: 'INACTIVE',
      description: 'Parked/Off duty',
    },
    { 
      label: 'Charging', 
      value: analytics.chargingVehicles, 
      icon: <EvStation sx={{ fontSize: 20 }} />, 
      color: VEHICLE_STATUS_COLORS.CHARGING, 
      status: 'CHARGING',
      description: 'At charging station',
    },
    { 
      label: 'Maintenance', 
      value: analytics.maintenanceVehicles, 
      icon: <Build sx={{ fontSize: 20 }} />, 
      color: VEHICLE_STATUS_COLORS.MAINTENANCE, 
      status: 'IN_MAINTENANCE',
      description: 'Under service',
    },
    { 
      label: 'In Trip', 
      value: analytics.inTripVehicles, 
      icon: <TrendingUp sx={{ fontSize: 20 }} />, 
      color: VEHICLE_STATUS_COLORS.IN_TRIP, 
      status: 'IN_TRIP',
      description: 'Active delivery',
    },
  ];

  const handleCategoryClick = async (category: string, status: string) => {
    setSelectedCategory(category);
    setModalOpen(true);
    setLoading(true);

    try {
      const vehicles = await vehicleService.getVehicles();
      const filteredVehicles = vehicles.filter((v: any) => v.status === status);
      setSelectedVehicles(filteredVehicles);
    } catch (error) {
      console.error('Error fetching vehicles:', error);
      setSelectedVehicles([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCloseModal = () => {
    setModalOpen(false);
    setSelectedCategory('');
    setSelectedVehicles([]);
  };

  return (
    <Card className="fade-in">
      <CardContent sx={{ p: 3 }}>
        {/* Header */}
        <Box display="flex" alignItems="center" justifyContent="space-between" mb={3}>
          <Box>
            <Typography variant="h6" fontWeight={600} color="text.primary">
              Fleet Summary
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Real-time vehicle status breakdown
            </Typography>
          </Box>
          <Box 
            sx={{ 
              display: 'flex',
              alignItems: 'center',
              gap: 1.5,
              px: 2,
              py: 1,
              borderRadius: 2,
              background: (theme) => alpha(theme.palette.primary.main, 0.08),
              border: (theme) => `1px solid ${alpha(theme.palette.primary.main, 0.15)}`,
            }}
          >
            <DirectionsCar sx={{ color: 'primary.main', fontSize: 24 }} />
            <Box>
              <Typography variant="caption" color="text.secondary" fontWeight={600}>
                Total Fleet
              </Typography>
              <Typography variant="h5" fontWeight={700} color="primary.main" lineHeight={1}>
                {analytics.totalVehicles}
              </Typography>
            </Box>
          </Box>
        </Box>

        {/* Status Grid */}
        <Grid container spacing={2}>
          {statusItems.map((item) => (
            <Grid item xs={6} sm={4} md={2.4} key={item.label}>
              <Box 
                onClick={() => handleCategoryClick(item.label, item.status)}
                sx={{ 
                  p: 2,
                  borderRadius: 2,
                  border: (theme) => `1px solid ${theme.palette.divider}`,
                  cursor: 'pointer',
                  transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
                  position: 'relative',
                  overflow: 'hidden',
                  '&:hover': {
                    borderColor: alpha(item.color, 0.4),
                    bgcolor: alpha(item.color, 0.04),
                    transform: 'translateY(-2px)',
                    boxShadow: (theme) => `0 4px 12px ${alpha(item.color, 0.15)}`,
                  },
                  '&::before': {
                    content: '""',
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    right: 0,
                    height: 3,
                    background: item.color,
                    borderRadius: '2px 2px 0 0',
                  },
                }}
              >
                <Box display="flex" alignItems="center" gap={1.5} mb={1.5}>
                  <Box 
                    sx={{ 
                      width: 36,
                      height: 36,
                      borderRadius: 1.5,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: item.color,
                      background: alpha(item.color, 0.1),
                    }}
                  >
                    {item.icon}
                  </Box>
                </Box>
                <Typography 
                  variant="h5" 
                  fontWeight={700}
                  color="text.primary"
                  sx={{ mb: 0.25 }}
                >
                  {item.value}
                </Typography>
                <Typography 
                  variant="body2" 
                  color="text.secondary"
                  fontWeight={500}
                >
                  {item.label}
                </Typography>
                <Typography 
                  variant="caption" 
                  color="text.disabled"
                  sx={{ display: 'block', mt: 0.5 }}
                >
                  {item.description}
                </Typography>
              </Box>
            </Grid>
          ))}
        </Grid>
      </CardContent>

      <VehicleDetailsModal
        open={modalOpen}
        onClose={handleCloseModal}
        title={`${selectedCategory} Vehicles`}
        vehicles={selectedVehicles}
        loading={loading}
      />
    </Card>
  );
};

export default FleetSummaryCard;

import React from 'react';
import {
  Paper,
  Typography,
  Box,
  LinearProgress,
  Chip,
  Alert,
} from '@mui/material';
import {
  BatteryFull as BatteryFullIcon,
  Battery80 as Battery80Icon,
  Battery50 as Battery50Icon,
  Battery20 as Battery20Icon,
  Info as InfoIcon,
} from '@mui/icons-material';
import { useAppSelector } from '@redux/hooks';
import { VehicleType, FuelType } from '../../types/vehicle';

interface BatteryLevelGroup {
  level: string;
  count: number;
  percentage: number;
  color: 'success' | 'info' | 'warning' | 'error';
  icon: React.ReactNode;
}

const BatterySummaryCard: React.FC = () => {
  const { vehicles } = useAppSelector((state) => state.vehicles);

  // Filter to only 4-wheelers (LCV) with EV/HYBRID - 2W/3W use GPS-only per strategy
  const batteryTrackedVehicles = vehicles.filter(
    (v) => v.type === VehicleType.LCV && (v.fuelType === FuelType.EV || v.fuelType === FuelType.HYBRID)
  );
  
  // Count GPS-only vehicles (2W/3W)
  const gpsOnlyVehicles = vehicles.filter(
    (v) => v.type === VehicleType.TWO_WHEELER || v.type === VehicleType.THREE_WHEELER
  );

  const getBatteryGroups = (): BatteryLevelGroup[] => {
    const total = batteryTrackedVehicles.length;
    if (total === 0) return [];

    const groups = {
      high: batteryTrackedVehicles.filter((v) => v.battery.stateOfCharge >= 80).length,
      medium: batteryTrackedVehicles.filter((v) => v.battery.stateOfCharge >= 50 && v.battery.stateOfCharge < 80).length,
      low: batteryTrackedVehicles.filter((v) => v.battery.stateOfCharge >= 20 && v.battery.stateOfCharge < 50).length,
      critical: batteryTrackedVehicles.filter((v) => v.battery.stateOfCharge < 20).length,
    };

    return [
      {
        level: 'High (80-100%)',
        count: groups.high,
        percentage: (groups.high / total) * 100,
        color: 'success',
        icon: <BatteryFullIcon />,
      },
      {
        level: 'Medium (50-80%)',
        count: groups.medium,
        percentage: (groups.medium / total) * 100,
        color: 'info',
        icon: <Battery80Icon />,
      },
      {
        level: 'Low (20-50%)',
        count: groups.low,
        percentage: (groups.low / total) * 100,
        color: 'warning',
        icon: <Battery50Icon />,
      },
      {
        level: 'Critical (<20%)',
        count: groups.critical,
        percentage: (groups.critical / total) * 100,
        color: 'error',
        icon: <Battery20Icon />,
      },
    ];
  };

  const batteryGroups = getBatteryGroups();
  const averageBattery = batteryTrackedVehicles.length > 0
    ? batteryTrackedVehicles.reduce((sum, v) => sum + v.battery.stateOfCharge, 0) / batteryTrackedVehicles.length
    : 0;

  return (
    <Paper sx={{ p: 3, height: '100%' }}>
      <Typography variant="h6" gutterBottom>
        Battery Status Overview
      </Typography>
      
      {/* Info about GPS-only vehicles */}
      {gpsOnlyVehicles.length > 0 && (
        <Alert severity="info" icon={<InfoIcon />} sx={{ mb: 2, py: 0.5 }}>
          <Typography variant="caption">
            {gpsOnlyVehicles.length} vehicle(s) use GPS-only tracking (2W/3W)
          </Typography>
        </Alert>
      )}

      {batteryTrackedVehicles.length === 0 ? (
        <Box textAlign="center" py={4}>
          <Typography variant="body2" color="text.secondary">
            No 4-wheeler EVs with battery tracking
          </Typography>
        </Box>
      ) : (
        <>
          <Box sx={{ mb: 3 }}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
              <Typography variant="body2" color="text.secondary">
                Fleet Average ({batteryTrackedVehicles.length} vehicles)
              </Typography>
              <Typography variant="h6">
                {averageBattery.toFixed(1)}%
              </Typography>
            </Box>
            <LinearProgress
              variant="determinate"
              value={averageBattery}
              sx={{ height: 8, borderRadius: 4 }}
              color={
                averageBattery >= 80 ? 'success' :
                averageBattery >= 50 ? 'info' :
                averageBattery >= 20 ? 'warning' : 'error'
              }
            />
          </Box>

          <Box>
            {batteryGroups.map((group, index) => (
              <Box key={index} sx={{ mb: 2 }}>
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                  <Box display="flex" alignItems="center" gap={1}>
                    <Box color={`${group.color}.main`}>
                      {group.icon}
                    </Box>
                    <Typography variant="body2">
                      {group.level}
                    </Typography>
                  </Box>
                  <Chip
                    label={group.count}
                    size="small"
                    color={group.color}
                  />
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={group.percentage}
                  color={group.color}
                  sx={{ height: 6, borderRadius: 3 }}
                />
              </Box>
            ))}
          </Box>
        </>
      )}
    </Paper>
  );
};

export default BatterySummaryCard;

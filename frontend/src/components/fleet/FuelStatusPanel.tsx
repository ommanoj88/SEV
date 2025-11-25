import React from 'react';
import {
  Box,
  Paper,
  Typography,
  LinearProgress,
  Grid,
  Chip,
  Alert,
} from '@mui/material';
import {
  BatteryFull as BatteryIcon,
  LocalGasStation as FuelIcon,
  BatteryChargingFull as ChargingIcon,
  LocationOn as LocationIcon,
} from '@mui/icons-material';
import { Vehicle, FuelType, VehicleType } from '../../types/vehicle';
import { getFuelTypeColor, getFuelTypeOption } from '../../constants/fuelTypes';

interface FuelStatusPanelProps {
  vehicle: Vehicle;
  compact?: boolean;
}

const FuelStatusPanel: React.FC<FuelStatusPanelProps> = ({ vehicle, compact = false }) => {
  const fuelTypeOption = getFuelTypeOption(vehicle.fuelType);
  // Battery tracking only for 4-wheelers (LCV) - 2W/3W use GPS-only per strategy
  const is4Wheeler = vehicle.type === 'LCV';
  const is2WheelerOr3Wheeler = vehicle.type === VehicleType.TWO_WHEELER || vehicle.type === VehicleType.THREE_WHEELER;
  const showBattery = is4Wheeler && (vehicle.fuelType === FuelType.EV || vehicle.fuelType === FuelType.HYBRID);
  const showFuel = vehicle.fuelType === FuelType.ICE || vehicle.fuelType === FuelType.HYBRID;
  const isEV = vehicle.fuelType === FuelType.EV;

  const getBatteryColor = (level: number): 'success' | 'warning' | 'error' => {
    if (level >= 60) return 'success';
    if (level >= 30) return 'warning';
    return 'error';
  };

  const getFuelColor = (level: number, capacity: number): 'success' | 'warning' | 'error' => {
    const percentage = (level / capacity) * 100;
    if (percentage >= 60) return 'success';
    if (percentage >= 30) return 'warning';
    return 'error';
  };

  const calculateFuelPercentage = (): number => {
    if (!vehicle.fuelLevel || !vehicle.fuelTankCapacity) return 0;
    return (vehicle.fuelLevel / vehicle.fuelTankCapacity) * 100;
  };

  if (compact) {
    return (
      <Box display="flex" alignItems="center" gap={2}>
        {showBattery && vehicle.battery && (
          <Chip
            icon={<BatteryIcon />}
            label={`${vehicle.battery.stateOfCharge.toFixed(0)}%`}
            size="small"
            color={getBatteryColor(vehicle.battery.stateOfCharge)}
            variant="outlined"
          />
        )}
        {showFuel && vehicle.fuelLevel && vehicle.fuelTankCapacity && (
          <Chip
            icon={<FuelIcon />}
            label={`${calculateFuelPercentage().toFixed(0)}%`}
            size="small"
            color={getFuelColor(vehicle.fuelLevel, vehicle.fuelTankCapacity)}
            variant="outlined"
          />
        )}
      </Box>
    );
  }

  return (
    <Paper sx={{ p: 3 }}>
      <Box display="flex" alignItems="center" gap={1} mb={2}>
        <Typography variant="h6">
          {fuelTypeOption?.icon} Fuel Status
        </Typography>
        <Chip
          label={fuelTypeOption?.label || vehicle.fuelType}
          size="small"
          sx={{
            backgroundColor: getFuelTypeColor(vehicle.fuelType),
            color: 'white',
          }}
        />
      </Box>

      <Grid container spacing={3}>
        {/* Battery Status - EV and HYBRID */}
        {showBattery && vehicle.battery && (
          <Grid item xs={12} md={vehicle.fuelType === FuelType.HYBRID ? 6 : 12}>
            <Box>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                <Box display="flex" alignItems="center" gap={1}>
                  <BatteryIcon color="primary" />
                  <Typography variant="body2" color="text.secondary">
                    Battery Level
                  </Typography>
                </Box>
                <Typography variant="h6">
                  {vehicle.battery.stateOfCharge.toFixed(1)}%
                </Typography>
              </Box>
              <LinearProgress
                variant="determinate"
                value={vehicle.battery.stateOfCharge}
                sx={{ height: 10, borderRadius: 5, mb: 1 }}
                color={getBatteryColor(vehicle.battery.stateOfCharge)}
              />
              <Box display="flex" justifyContent="space-between">
                <Typography variant="caption" color="text.secondary">
                  {vehicle.battery.stateOfCharge.toFixed(1)}% of {vehicle.battery.capacity} kWh
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Range: {vehicle.battery.range} km
                </Typography>
              </Box>
            </Box>
          </Grid>
        )}

        {/* Fuel Status - ICE and HYBRID */}
        {showFuel && vehicle.fuelLevel !== undefined && vehicle.fuelTankCapacity && (
          <Grid item xs={12} md={vehicle.fuelType === FuelType.HYBRID ? 6 : 12}>
            <Box>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                <Box display="flex" alignItems="center" gap={1}>
                  <FuelIcon color="warning" />
                  <Typography variant="body2" color="text.secondary">
                    Fuel Level
                  </Typography>
                </Box>
                <Typography variant="h6">
                  {calculateFuelPercentage().toFixed(1)}%
                </Typography>
              </Box>
              <LinearProgress
                variant="determinate"
                value={calculateFuelPercentage()}
                sx={{ height: 10, borderRadius: 5, mb: 1 }}
                color={getFuelColor(vehicle.fuelLevel, vehicle.fuelTankCapacity)}
              />
              <Box display="flex" justifyContent="space-between">
                <Typography variant="caption" color="text.secondary">
                  {vehicle.fuelLevel.toFixed(1)}L of {vehicle.fuelTankCapacity}L
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {((vehicle.fuelTankCapacity - vehicle.fuelLevel)).toFixed(1)}L to full
                </Typography>
              </Box>
            </Box>
          </Grid>
        )}
      </Grid>

      {/* Additional info for hybrid vehicles */}
      {vehicle.fuelType === FuelType.HYBRID && (
        <Box mt={2} p={2} bgcolor="info.light" borderRadius={1}>
          <Typography variant="caption" color="text.secondary">
            💡 Hybrid vehicle: Can use both electric power and fuel for optimal efficiency
          </Typography>
        </Box>
      )}

      {/* GPS-only tracking info for 2-wheelers and 3-wheelers */}
      {is2WheelerOr3Wheeler && isEV && (
        <Alert severity="info" icon={<LocationIcon />} sx={{ mt: 2 }}>
          <Typography variant="body2">
            <strong>GPS Tracking Active</strong> - Battery monitoring is not available for {vehicle.type?.replace('_', '-').toLowerCase()}s. 
            Real-time location, speed, and odometer tracking enabled.
          </Typography>
        </Alert>
      )}
    </Paper>
  );
};

export default FuelStatusPanel;

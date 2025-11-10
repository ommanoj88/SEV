import React from 'react';
import {
  Card,
  CardContent,
  Typography,
  Chip,
  Box,
  IconButton,
  Tooltip,
  Rating,
} from '@mui/material';
import {
  Phone as PhoneIcon,
  Schedule as ScheduleIcon,
  LocalGasStation as FuelIcon,
  EvStation as ChargingIcon,
  Navigation as NavigationIcon,
} from '@mui/icons-material';
import { ChargingStation, FuelStation, isChargingStation, isFuelStation } from '../../types';

interface StationCardProps {
  station: ChargingStation | FuelStation;
  onNavigate?: (station: ChargingStation | FuelStation) => void;
}

const StationCard: React.FC<StationCardProps> = ({ station, onNavigate }) => {
  const isCharging = isChargingStation(station);
  const isFuel = isFuelStation(station);

  const getAvailabilityColor = () => {
    if (isCharging) {
      const availableRatio = station.availablePorts / station.totalPorts;
      if (availableRatio >= 0.5) return 'success';
      if (availableRatio > 0) return 'warning';
      return 'error';
    } else if (isFuel) {
      const availableRatio = station.availablePumps / station.totalPumps;
      if (availableRatio >= 0.5) return 'success';
      if (availableRatio > 0) return 'warning';
      return 'error';
    }
    return 'default';
  };

  const getAvailabilityText = () => {
    if (isCharging) {
      return `${station.availablePorts}/${station.totalPorts} ports available`;
    } else if (isFuel) {
      return `${station.availablePumps}/${station.totalPumps} pumps available`;
    }
    return 'N/A';
  };

  const getPriceText = () => {
    if (isCharging) {
      return `₹${station.costPerKwh.toFixed(2)}/kWh`;
    } else if (isFuel) {
      // Display prices for available fuel types
      const prices = Object.entries(station.pricePerLiter)
        .filter(([_, price]) => price !== undefined)
        .map(([type, price]) => `${type}: ₹${price?.toFixed(2)}/L`)
        .join(', ');
      return prices || 'Price not available';
    }
    return 'N/A';
  };

  return (
    <Card sx={{ mb: 2, '&:hover': { boxShadow: 6 }, transition: 'box-shadow 0.3s' }}>
      <CardContent>
        {/* Header with name and type icon */}
        <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={1}>
          <Box display="flex" alignItems="center" gap={1}>
            {isCharging ? (
              <ChargingIcon color="primary" />
            ) : (
              <FuelIcon color="warning" />
            )}
            <Typography variant="h6" component="div">
              {station.name}
            </Typography>
          </Box>
          {onNavigate && (
            <Tooltip title="Get directions">
              <IconButton
                size="small"
                onClick={() => onNavigate(station)}
                color="primary"
              >
                <NavigationIcon />
              </IconButton>
            </Tooltip>
          )}
        </Box>

        {/* Address */}
        <Typography variant="body2" color="text.secondary" mb={1}>
          {station.location.address || `${station.location.latitude}, ${station.location.longitude}`}
        </Typography>

        {/* Distance and Rating */}
        <Box display="flex" alignItems="center" gap={2} mb={1}>
          {station.distance !== undefined && (
            <Typography variant="body2" color="text.secondary">
              {station.distance.toFixed(1)} km away
            </Typography>
          )}
          {station.rating !== undefined && (
            <Box display="flex" alignItems="center" gap={0.5}>
              <Rating value={station.rating} precision={0.5} size="small" readOnly />
              <Typography variant="body2" color="text.secondary">
                ({station.rating})
              </Typography>
            </Box>
          )}
        </Box>

        {/* Type and Availability */}
        <Box display="flex" gap={1} mb={1} flexWrap="wrap">
          <Chip
            label={getAvailabilityText()}
            color={getAvailabilityColor()}
            size="small"
          />
          {isCharging && (
            <Chip label={station.type} size="small" variant="outlined" />
          )}
          {isFuel && station.fuelTypes.map((type) => (
            <Chip key={type} label={type} size="small" variant="outlined" />
          ))}
        </Box>

        {/* Price */}
        <Typography variant="body2" color="primary" fontWeight="bold" mb={1}>
          {getPriceText()}
        </Typography>

        {/* Additional Info */}
        <Box display="flex" gap={2} flexWrap="wrap">
          {station.operatingHours && (
            <Box display="flex" alignItems="center" gap={0.5}>
              <ScheduleIcon fontSize="small" color="action" />
              <Typography variant="caption" color="text.secondary">
                {station.operatingHours}
              </Typography>
            </Box>
          )}
          {station.phoneNumber && (
            <Box display="flex" alignItems="center" gap={0.5}>
              <PhoneIcon fontSize="small" color="action" />
              <Typography variant="caption" color="text.secondary">
                {station.phoneNumber}
              </Typography>
            </Box>
          )}
        </Box>

        {/* Amenities */}
        {station.amenities && station.amenities.length > 0 && (
          <Box mt={1}>
            <Typography variant="caption" color="text.secondary">
              Amenities: {station.amenities.join(', ')}
            </Typography>
          </Box>
        )}

        {/* Wait Time */}
        {station.estimatedWaitTime !== undefined && station.estimatedWaitTime > 0 && (
          <Box mt={1}>
            <Chip
              label={`~${station.estimatedWaitTime} min wait`}
              size="small"
              color="info"
              variant="outlined"
            />
          </Box>
        )}
      </CardContent>
    </Card>
  );
};

export default StationCard;

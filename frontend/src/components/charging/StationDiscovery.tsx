import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  TextField,
  InputAdornment,
  ToggleButtonGroup,
  ToggleButton,
  CircularProgress,
  Alert,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
} from '@mui/material';
import {
  Search as SearchIcon,
  EvStation as ChargingIcon,
  LocalGasStation as FuelIcon,
  MyLocation as MyLocationIcon,
} from '@mui/icons-material';
import StationMap from './StationMap';
import StationCard from './StationCard';
import { ChargingStation, FuelStation, FuelType } from '../../types';
import { chargingService } from '../../services/chargingService';
import { fuelService } from '../../services/fuelService';

type StationType = 'charging' | 'fuel';

interface StationDiscoveryProps {
  vehicleFuelType?: FuelType;
  vehicleId?: string;
  initialLocation?: { latitude: number; longitude: number };
}

const StationDiscovery: React.FC<StationDiscoveryProps> = ({
  vehicleFuelType = FuelType.EV,
  vehicleId,
  initialLocation,
}) => {
  const [stationType, setStationType] = useState<StationType>('charging');
  const [chargingStations, setChargingStations] = useState<ChargingStation[]>([]);
  const [fuelStations, setFuelStations] = useState<FuelStation[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [userLocation, setUserLocation] = useState<{ latitude: number; longitude: number } | null>(
    initialLocation || null
  );
  const [sortBy, setSortBy] = useState<'distance' | 'rating' | 'availability'>('distance');

  // Determine available station types based on vehicle fuel type
  const canShowCharging = vehicleFuelType === FuelType.EV || vehicleFuelType === FuelType.HYBRID;
  const canShowFuel = vehicleFuelType === FuelType.ICE || vehicleFuelType === FuelType.HYBRID;

  // Set initial station type based on vehicle fuel type
  useEffect(() => {
    if (vehicleFuelType === FuelType.EV) {
      setStationType('charging');
    } else if (vehicleFuelType === FuelType.ICE) {
      setStationType('fuel');
    }
    // For HYBRID, keep default 'charging' or let user toggle
  }, [vehicleFuelType]);

  // Get user's current location
  const getCurrentLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setUserLocation({
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
          });
        },
        (error) => {
          console.error('Error getting location:', error);
          setError('Unable to get your current location. Please enable location services.');
        }
      );
    } else {
      setError('Geolocation is not supported by your browser.');
    }
  };

  // Fetch stations when station type or location changes
  useEffect(() => {
    const fetchStations = async () => {
      setLoading(true);
      setError(null);

      try {
        if (stationType === 'charging') {
          if (userLocation) {
            const stations = await chargingService.getNearestStations(
              userLocation.latitude,
              userLocation.longitude,
              20
            );
            setChargingStations(stations);
          } else {
            const stations = await chargingService.getAllStations();
            setChargingStations(stations);
          }
        } else {
          if (userLocation) {
            const stations = await fuelService.getNearestStations(
              userLocation.latitude,
              userLocation.longitude,
              20
            );
            setFuelStations(stations);
          } else {
            const stations = await fuelService.getAllStations();
            setFuelStations(stations);
          }
        }
      } catch (err: any) {
        console.error('Error fetching stations:', err);
        setError(err.message || 'Failed to fetch stations. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchStations();
  }, [stationType, userLocation]);

  // Handle station type toggle
  const handleStationTypeChange = (_: React.MouseEvent<HTMLElement>, newType: StationType | null) => {
    if (newType !== null) {
      setStationType(newType);
    }
  };

  // Get current stations based on type and apply filtering/sorting
  const getProcessedStations = (): (ChargingStation | FuelStation)[] => {
    const currentStations: (ChargingStation | FuelStation)[] = 
      stationType === 'charging' 
        ? (chargingStations as (ChargingStation | FuelStation)[])
        : (fuelStations as (ChargingStation | FuelStation)[]);

    // Filter stations based on search
    const filteredStations = currentStations.filter((station) =>
      station.name.toLowerCase().includes(search.toLowerCase())
    );

    // Sort stations
    return [...filteredStations].sort((a, b) => {
    switch (sortBy) {
      case 'distance':
        return (a.distance || Infinity) - (b.distance || Infinity);
      case 'rating':
        return (b.rating || 0) - (a.rating || 0);
      case 'availability':
        if ('availablePorts' in a && 'availablePorts' in b) {
          return b.availablePorts - a.availablePorts;
        } else if ('availablePumps' in a && 'availablePumps' in b) {
          return b.availablePumps - a.availablePumps;
        }
        return 0;
      default:
        return 0;
    }
  });
  };

  const sortedStations = getProcessedStations();

  // Handle navigation to station
  const handleNavigate = (station: ChargingStation | FuelStation) => {
    const url = `https://www.google.com/maps/dir/?api=1&destination=${station.location.latitude},${station.location.longitude}`;
    window.open(url, '_blank');
  };

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', gap: 2 }}>
      {/* Header with controls */}
      <Paper sx={{ p: 2 }}>
        <Box display="flex" flexDirection="column" gap={2}>
          <Box display="flex" justifyContent="space-between" alignItems="center" flexWrap="wrap" gap={2}>
            <Typography variant="h5" component="h1">
              Station Discovery
            </Typography>

            {/* Station Type Toggle - only show for HYBRID vehicles */}
            {canShowCharging && canShowFuel && (
              <ToggleButtonGroup
                value={stationType}
                exclusive
                onChange={handleStationTypeChange}
                aria-label="station type"
                size="small"
              >
                <ToggleButton value="charging" aria-label="charging stations">
                  <ChargingIcon sx={{ mr: 1 }} />
                  Charging
                </ToggleButton>
                <ToggleButton value="fuel" aria-label="fuel stations">
                  <FuelIcon sx={{ mr: 1 }} />
                  Fuel
                </ToggleButton>
              </ToggleButtonGroup>
            )}

            {/* Show station type info for non-HYBRID vehicles */}
            {!canShowCharging && (
              <Box display="flex" alignItems="center" gap={1}>
                <FuelIcon color="warning" />
                <Typography variant="body2" color="text.secondary">
                  Fuel Stations (ICE Vehicle)
                </Typography>
              </Box>
            )}
            {!canShowFuel && (
              <Box display="flex" alignItems="center" gap={1}>
                <ChargingIcon color="primary" />
                <Typography variant="body2" color="text.secondary">
                  Charging Stations (EV Vehicle)
                </Typography>
              </Box>
            )}
          </Box>

          {/* Search and filters */}
          <Box display="flex" gap={2} flexWrap="wrap">
            <TextField
              size="small"
              placeholder="Search stations..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
              sx={{ flex: 1, minWidth: 200 }}
            />

            <FormControl size="small" sx={{ minWidth: 150 }}>
              <InputLabel>Sort by</InputLabel>
              <Select
                value={sortBy}
                label="Sort by"
                onChange={(e) => setSortBy(e.target.value as typeof sortBy)}
              >
                <MenuItem value="distance">Distance</MenuItem>
                <MenuItem value="rating">Rating</MenuItem>
                <MenuItem value="availability">Availability</MenuItem>
              </Select>
            </FormControl>

            <Button
              variant="outlined"
              startIcon={<MyLocationIcon />}
              onClick={getCurrentLocation}
              size="small"
            >
              Use My Location
            </Button>
          </Box>

          {userLocation && (
            <Alert severity="info" sx={{ py: 0 }}>
              Showing stations near your location
            </Alert>
          )}
        </Box>
      </Paper>

      {/* Error message */}
      {error && (
        <Alert severity="error" onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* Main content area with map and list */}
      <Box display="flex" gap={2} sx={{ flex: 1, minHeight: 0 }}>
        {/* Map */}
        <Paper sx={{ flex: 1, overflow: 'hidden', position: 'relative' }}>
          {loading && (
            <Box
              sx={{
                position: 'absolute',
                top: '50%',
                left: '50%',
                transform: 'translate(-50%, -50%)',
                zIndex: 1,
              }}
            >
              <CircularProgress />
            </Box>
          )}
          <StationMap
            stations={sortedStations}
            center={
              userLocation
                ? [userLocation.longitude, userLocation.latitude]
                : undefined
            }
            onStationClick={handleNavigate}
          />
        </Paper>

        {/* Station list */}
        <Paper sx={{ width: 400, p: 2, overflow: 'auto' }}>
          {loading ? (
            <Box display="flex" justifyContent="center" alignItems="center" height="100%">
              <CircularProgress />
            </Box>
          ) : sortedStations.length === 0 ? (
            <Box display="flex" justifyContent="center" alignItems="center" height="100%">
              <Typography color="text.secondary">
                {search ? 'No stations match your search' : 'No stations found'}
              </Typography>
            </Box>
          ) : (
            <>
              <Typography variant="subtitle2" color="text.secondary" mb={2}>
                {sortedStations.length} station{sortedStations.length !== 1 ? 's' : ''} found
              </Typography>
              {sortedStations.map((station) => (
                <StationCard
                  key={station.id}
                  station={station}
                  onNavigate={handleNavigate}
                />
              ))}
            </>
          )}
        </Paper>
      </Box>
    </Box>
  );
};

export default StationDiscovery;

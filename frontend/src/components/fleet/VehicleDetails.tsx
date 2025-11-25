import React, { useEffect, useState } from 'react';
import {
  Box,
  Paper,
  Grid,
  Typography,
  Chip,
  Divider,
  Button,
  LinearProgress,
  Tabs,
  Tab,
} from '@mui/material';
import {
  Edit as EditIcon,
  LocationOn as LocationIcon,
  BatteryFull as BatteryIcon,
  Speed as SpeedIcon,
  CalendarToday as CalendarIcon,
  LocalGasStation as FuelIcon,
  EvStation as ChargingIcon,
  Build as MaintenanceIcon,
} from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@redux/hooks';
import { fetchVehicle } from '@redux/slices/vehicleSlice';
import { formatDate, formatNumber } from '@utils/formatters';
import { FuelType } from '../../types/vehicle';
import LoadingSpinner from '@components/common/LoadingSpinner';
import TripHistory from './TripHistory';
import FuelStatusPanel from './FuelStatusPanel';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
  return (
    <div role="tabpanel" hidden={value !== index}>
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  );
};

const VehicleDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { selectedVehicle, loading } = useAppSelector((state) => state.vehicles);
  const [activeTab, setActiveTab] = useState(0);

  useEffect(() => {
    if (id) {
      dispatch(fetchVehicle(id));
    }
  }, [id, dispatch]);

  if (loading || !selectedVehicle) {
    return <LoadingSpinner />;
  }

  const vehicle = selectedVehicle;
  const batteryPercentage = vehicle.battery.stateOfCharge;
  // Battery tracking only for 4-wheelers (LCV) - 2W/3W use GPS-only per 2WHEELER_GPS_ONLY_STRATEGY
  const is4Wheeler = vehicle.type === 'LCV';
  const showBattery = is4Wheeler && (vehicle.fuelType === FuelType.EV || vehicle.fuelType === FuelType.HYBRID);
  const showFuel = vehicle.fuelType === FuelType.ICE || vehicle.fuelType === FuelType.HYBRID;
  const showCharging = is4Wheeler && (vehicle.fuelType === FuelType.EV || vehicle.fuelType === FuelType.HYBRID);

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5">
          {vehicle.make} {vehicle.model} ({vehicle.year})
        </Typography>
        <Button
          variant="contained"
          startIcon={<EditIcon />}
          onClick={() => navigate(`/fleet/${id}/edit`)}
        >
          Edit Vehicle
        </Button>
      </Box>

      <Grid container spacing={3}>
        {/* Basic Information */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Vehicle Information
            </Typography>
            <Divider sx={{ mb: 2 }} />

            <Grid container spacing={2}>
              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary">
                  VIN
                </Typography>
                <Typography variant="body1" fontWeight="500">
                  {vehicle.vin}
                </Typography>
              </Grid>
              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary">
                  License Plate
                </Typography>
                <Typography variant="body1" fontWeight="500">
                  {vehicle.licensePlate}
                </Typography>
              </Grid>
              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary">
                  Type
                </Typography>
                <Typography variant="body1" fontWeight="500">
                  {vehicle.type}
                </Typography>
              </Grid>
              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary">
                  Fuel Type
                </Typography>
                <Chip 
                  label={vehicle.fuelType} 
                  size="small" 
                  color={
                    vehicle.fuelType === FuelType.EV ? 'success' : 
                    vehicle.fuelType === FuelType.ICE ? 'warning' : 
                    'info'
                  }
                />
              </Grid>
              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary">
                  Color
                </Typography>
                <Typography variant="body1" fontWeight="500">
                  {vehicle.color || 'N/A'}
                </Typography>
              </Grid>
              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary">
                  Status
                </Typography>
                <Chip label={vehicle.status} size="small" color="primary" />
              </Grid>
              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary">
                  Assigned Driver
                </Typography>
                <Typography variant="body1" fontWeight="500">
                  {vehicle.assignedDriverName || 'Unassigned'}
                </Typography>
              </Grid>
            </Grid>
          </Paper>
        </Grid>

        {/* Fuel Status Panel */}
        <Grid item xs={12} md={6}>
          <FuelStatusPanel vehicle={vehicle} />
        </Grid>

        {/* Location */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Current Location
            </Typography>
            <Divider sx={{ mb: 2 }} />

            <Box display="flex" alignItems="start" gap={2}>
              <LocationIcon color="primary" />
              <Box>
                <Typography variant="body1">
                  {vehicle.location.address || 'Address not available'}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {vehicle.location.latitude.toFixed(6)}, {vehicle.location.longitude.toFixed(6)}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Last updated: {vehicle.location.timestamp ? formatDate(vehicle.location.timestamp) : 'N/A'}
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>

        {/* Maintenance Info */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Maintenance & Performance
            </Typography>
            <Divider sx={{ mb: 2 }} />

            <Grid container spacing={2}>
              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary">
                  Odometer
                </Typography>
                <Typography variant="h6">
                  {formatNumber(vehicle.odometer)} km
                </Typography>
              </Grid>
              {showBattery && (
                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">
                    Battery Health
                  </Typography>
                  <Typography variant="h6">
                    {vehicle.battery.stateOfHealth}%
                  </Typography>
                </Grid>
              )}
              <Grid item xs={12}>
                <Typography variant="body2" color="text.secondary">
                  Last Service
                </Typography>
                <Typography variant="body1" fontWeight="500">
                  {vehicle.lastServiceDate ? formatDate(vehicle.lastServiceDate) : 'No service record'}
                </Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="body2" color="text.secondary">
                  Next Service Due
                </Typography>
                <Typography variant="body1" fontWeight="500">
                  {vehicle.nextServiceDate ? formatDate(vehicle.nextServiceDate) : 'Not scheduled'}
                </Typography>
              </Grid>
              <Grid item xs={12}>
                <Button
                  variant="outlined"
                  fullWidth
                  onClick={() => navigate(`/maintenance?vehicleId=${id}`)}
                >
                  View Maintenance History
                </Button>
              </Grid>
            </Grid>
          </Paper>
        </Grid>

        {/* Tabbed Content - Charging/Fuel/Trips */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Tabs 
              value={activeTab} 
              onChange={(_, newValue) => setActiveTab(newValue)}
              variant="scrollable"
              scrollButtons="auto"
            >
              <Tab icon={<CalendarIcon />} label="Trip History" />
              {showCharging && <Tab icon={<ChargingIcon />} label="Charging History" />}
              {showFuel && <Tab icon={<FuelIcon />} label="Fuel History" />}
              <Tab icon={<MaintenanceIcon />} label="Maintenance" />
            </Tabs>

            <TabPanel value={activeTab} index={0}>
              <TripHistory vehicleId={id || ''} />
            </TabPanel>

            {showCharging && (
              <TabPanel value={activeTab} index={1}>
                <Box textAlign="center" py={4}>
                  <ChargingIcon sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
                  <Typography variant="h6" color="text.secondary" gutterBottom>
                    Charging History
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    View charging sessions, station usage, and energy consumption
                  </Typography>
                  <Button
                    variant="outlined"
                    sx={{ mt: 2 }}
                    onClick={() => navigate(`/charging?vehicleId=${id}`)}
                  >
                    View Charging History
                  </Button>
                </Box>
              </TabPanel>
            )}

            {showFuel && (
              <TabPanel value={activeTab} index={showCharging ? 2 : 1}>
                <Box textAlign="center" py={4}>
                  <FuelIcon sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
                  <Typography variant="h6" color="text.secondary" gutterBottom>
                    Fuel History
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    View fuel consumption, refueling history, and efficiency metrics
                  </Typography>
                  <Button
                    variant="outlined"
                    sx={{ mt: 2 }}
                    onClick={() => navigate(`/fuel?vehicleId=${id}`)}
                  >
                    View Fuel History
                  </Button>
                </Box>
              </TabPanel>
            )}

            <TabPanel value={activeTab} index={showCharging && showFuel ? 3 : showCharging || showFuel ? 2 : 1}>
              <Box textAlign="center" py={4}>
                <MaintenanceIcon sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
                <Typography variant="h6" color="text.secondary" gutterBottom>
                  Maintenance Records
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {vehicle.fuelType === FuelType.EV 
                    ? 'View battery maintenance, software updates, and service history'
                    : vehicle.fuelType === FuelType.ICE
                    ? 'View oil changes, filter replacements, and engine maintenance'
                    : 'View comprehensive maintenance for electric and combustion systems'}
                </Typography>
                <Button
                  variant="outlined"
                  sx={{ mt: 2 }}
                  onClick={() => navigate(`/maintenance?vehicleId=${id}`)}
                >
                  View Full Maintenance History
                </Button>
              </Box>
            </TabPanel>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default VehicleDetails;

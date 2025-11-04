import React, { useEffect } from 'react';
import {
  Box,
  Paper,
  Grid,
  Typography,
  Chip,
  Divider,
  Button,
  LinearProgress,
} from '@mui/material';
import {
  Edit as EditIcon,
  LocationOn as LocationIcon,
  BatteryFull as BatteryIcon,
  Speed as SpeedIcon,
  CalendarToday as CalendarIcon,
} from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@redux/hooks';
import { fetchVehicle } from '@redux/slices/vehicleSlice';
import { formatDate, formatNumber } from '@utils/formatters';
import LoadingSpinner from '@components/common/LoadingSpinner';
import TripHistory from './TripHistory';

const VehicleDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { selectedVehicle, loading } = useAppSelector((state) => state.vehicles);

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

        {/* Battery & Range */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Battery & Performance
            </Typography>
            <Divider sx={{ mb: 2 }} />

            <Box mb={3}>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                <Typography variant="body2" color="text.secondary">
                  Battery Level
                </Typography>
                <Typography variant="h6">
                  {vehicle.battery.stateOfCharge.toFixed(1)}%
                </Typography>
              </Box>
              <LinearProgress
                variant="determinate"
                value={batteryPercentage}
                sx={{ height: 10, borderRadius: 5 }}
                color={batteryPercentage >= 60 ? 'success' : batteryPercentage >= 30 ? 'warning' : 'error'}
              />
              <Typography variant="caption" color="text.secondary">
                {batteryPercentage.toFixed(1)}% of {vehicle.battery.capacity} kWh
              </Typography>
            </Box>

            <Grid container spacing={2}>
              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary">
                  Current Range
                </Typography>
                <Typography variant="h6">
                  {vehicle.battery.range} km
                </Typography>
              </Grid>
              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary">
                  Battery Health
                </Typography>
                <Typography variant="h6">
                  {vehicle.battery.stateOfHealth}%
                </Typography>
              </Grid>
              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary">
                  Odometer
                </Typography>
                <Typography variant="h6">
                  {formatNumber(vehicle.odometer)} mi
                </Typography>
              </Grid>
              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary">
                  Battery Temp
                </Typography>
                <Typography variant="h6">
                  {vehicle.battery.temperature || 'N/A'}°C
                </Typography>
              </Grid>
            </Grid>
          </Paper>
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
              Maintenance
            </Typography>
            <Divider sx={{ mb: 2 }} />

            <Grid container spacing={2}>
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

        {/* Trip History */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Recent Trips
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <TripHistory vehicleId={id || ''} />
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default VehicleDetails;

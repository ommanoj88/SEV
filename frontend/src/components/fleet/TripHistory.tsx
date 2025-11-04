import React, { useEffect } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Box,
  Typography,
} from '@mui/material';
import { useAppDispatch, useAppSelector } from '@redux/hooks';
import { fetchVehicleTrips } from '@redux/slices/vehicleSlice';
import { formatDate, formatDuration, formatNumber } from '@utils/formatters';
import LoadingSpinner from '@components/common/LoadingSpinner';

interface TripHistoryProps {
  vehicleId: string;
  limit?: number;
}

const TripHistory: React.FC<TripHistoryProps> = ({ vehicleId, limit = 10 }) => {
  const dispatch = useAppDispatch();
  const { trips, loading } = useAppSelector((state) => state.vehicles);

  useEffect(() => {
    if (vehicleId) {
      dispatch(fetchVehicleTrips(vehicleId));
    }
  }, [vehicleId, dispatch]);

  if (loading) {
    return <LoadingSpinner />;
  }

  if (!trips || trips.length === 0) {
    return (
      <Box textAlign="center" py={4}>
        <Typography variant="body2" color="text.secondary">
          No trip history available
        </Typography>
      </Box>
    );
  }

  return (
    <TableContainer>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Date</TableCell>
            <TableCell>Driver</TableCell>
            <TableCell>Route</TableCell>
            <TableCell>Distance</TableCell>
            <TableCell>Duration</TableCell>
            <TableCell>Energy Used</TableCell>
            <TableCell>Efficiency</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {trips.map((trip) => (
            <TableRow key={trip.id} hover>
              <TableCell>{formatDate(trip.startTime)}</TableCell>
              <TableCell>{trip.driverName}</TableCell>
              <TableCell>
                <Box>
                  <Typography variant="body2" noWrap sx={{ maxWidth: 200 }}>
                    {trip.startLocation.address || `${trip.startLocation.latitude}, ${trip.startLocation.longitude}`}
                  </Typography>
                  <Typography variant="caption" color="text.secondary" noWrap sx={{ maxWidth: 200 }}>
                    to {trip.endLocation?.address || (trip.endLocation ? `${trip.endLocation.latitude}, ${trip.endLocation.longitude}` : 'N/A')}
                  </Typography>
                </Box>
              </TableCell>
              <TableCell>{formatNumber(trip.distance)} mi</TableCell>
              <TableCell>{trip.duration ? formatDuration(trip.duration) : 'N/A'}</TableCell>
              <TableCell>
                {trip.energyConsumed ? `${trip.energyConsumed.toFixed(1)} kWh` : 'N/A'}
                <Typography variant="caption" display="block" color="text.secondary">
                  ({trip.startBatteryLevel}% → {trip.endBatteryLevel || '?'}%)
                </Typography>
              </TableCell>
              <TableCell>
                {trip.energyConsumed && trip.distance ? (
                  <Chip
                    label={`${(trip.distance / trip.energyConsumed).toFixed(1)} mi/kWh`}
                    size="small"
                    color={(trip.distance / trip.energyConsumed) >= 3.5 ? 'success' : (trip.distance / trip.energyConsumed) >= 2.5 ? 'warning' : 'error'}
                  />
                ) : 'N/A'}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default TripHistory;

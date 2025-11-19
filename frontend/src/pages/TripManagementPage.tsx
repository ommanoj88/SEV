import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  Container,
  Paper,
  TextField,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Chip,
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
} from '@mui/material';
import { fetchAllTrips, startTrip, endTrip, pauseTrip, resumeTrip, cancelTrip, selectAllTrips, selectTripLoading, selectTripError } from '../redux/slices/tripSlice';
import { Trip, TripStatus } from '../types';

const TripManagementPage: React.FC = () => {
  const dispatch = useDispatch();
  const trips = useSelector(selectAllTrips);
  const loading = useSelector(selectTripLoading);
  const error = useSelector(selectTripError);

  const [filterStatus, setFilterStatus] = useState<TripStatus | ''>('');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedTrip, setSelectedTrip] = useState<Trip | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    dispatch(fetchAllTrips(undefined) as any);
  }, [dispatch]);

  const getStatusColor = (status: TripStatus) => {
    const colors: Record<TripStatus, 'success' | 'warning' | 'error' | 'info'> = {
      ONGOING: 'info',
      COMPLETED: 'success',
      CANCELLED: 'error',
      PAUSED: 'warning',
    };
    return colors[status] || 'default';
  };

  const filteredTrips = trips.filter(
    (trip: any) =>
      (!filterStatus || trip.status === filterStatus) &&
      (!searchTerm ||
        trip.vehicleId?.toString().includes(searchTerm) ||
        trip.driverId?.toString().includes(searchTerm))
  );

  const handleStartTrip = async (trip: Trip) => {
    if (!trip.vehicleId || !trip.driverId) return;
    
    // Validate that we have proper location data
    if (!trip.startLocation || 
        trip.startLocation.latitude === 0 && trip.startLocation.longitude === 0 ||
        !trip.startLocation.latitude || !trip.startLocation.longitude) {
      alert('Cannot start trip: Valid start location is required. Please ensure GPS/location data is available.');
      return;
    }
    
    setActionLoading(true);
    try {
      dispatch(
        startTrip({
          vehicleId: Number(trip.vehicleId),
          driverId: Number(trip.driverId),
          startLocation: trip.startLocation,
        }) as any
      );
    } finally {
      setActionLoading(false);
    }
  };

  const handleEndTrip = async (trip: Trip) => {
    if (!trip.id) return;
    
    // Validate that we have proper location and distance data
    if (!trip.endLocation || 
        trip.endLocation.latitude === 0 && trip.endLocation.longitude === 0 ||
        !trip.endLocation.latitude || !trip.endLocation.longitude) {
      alert('Cannot end trip: Valid end location is required. Please ensure GPS/location data is available.');
      return;
    }
    
    if (!trip.distance || trip.distance <= 0) {
      alert('Cannot end trip: Valid distance is required.');
      return;
    }
    
    setActionLoading(true);
    try {
      dispatch(
        endTrip({
          tripId: Number(trip.id),
          data: {
            endLocation: trip.endLocation,
            distance: trip.distance,
            energyConsumed: trip.energyConsumed,
          },
        }) as any
      );
    } finally {
      setActionLoading(false);
    }
  };

  const handlePauseTrip = async (trip: Trip) => {
    if (!trip.id) return;
    setActionLoading(true);
    try {
      dispatch(pauseTrip(Number(trip.id)) as any);
    } finally {
      setActionLoading(false);
    }
  };

  const handleResumeTrip = async (trip: Trip) => {
    if (!trip.id) return;
    setActionLoading(true);
    try {
      dispatch(resumeTrip(Number(trip.id)) as any);
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancelTrip = async (trip: Trip) => {
    if (!trip.id) return;
    setActionLoading(true);
    try {
      dispatch(cancelTrip({ tripId: Number(trip.id), reason: 'Cancelled by user' }) as any);
    } finally {
      setActionLoading(false);
    }
  };

  const handleViewDetails = (trip: Trip) => {
    setSelectedTrip(trip);
    setDialogOpen(true);
  };

  return (
    <Container maxWidth="lg" sx={{ py: 3 }}>
      <Typography variant="h4" sx={{ mb: 3, fontWeight: 'bold' }}>
        Trip Management
      </Typography>

      {error && <Alert severity="error">{error}</Alert>}

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Search by Vehicle or Driver ID"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Enter vehicle or driver ID"
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              select
              fullWidth
              label="Filter by Status"
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value as TripStatus | '')}
              SelectProps={{
                native: true,
              }}
            >
              <option value="">All Status</option>
              <option value="ONGOING">Ongoing</option>
              <option value="COMPLETED">Completed</option>
              <option value="PAUSED">Paused</option>
              <option value="CANCELLED">Cancelled</option>
            </TextField>
          </Grid>
        </Grid>
      </Paper>

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Total Trips</Typography>
              <Typography variant="h5">{trips.length}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Ongoing</Typography>
              <Typography variant="h5">{trips.filter((t: any) => t.status === 'ONGOING').length}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Completed</Typography>
              <Typography variant="h5">{trips.filter((t: any) => t.status === 'COMPLETED').length}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Cancelled</Typography>
              <Typography variant="h5">{trips.filter((t: any) => t.status === 'CANCELLED').length}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Trips Table */}
      <TableContainer component={Paper}>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
            <CircularProgress />
          </Box>
        ) : (
          <Table>
            <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
              <TableRow>
                <TableCell>Trip ID</TableCell>
                <TableCell>Vehicle ID</TableCell>
                <TableCell>Driver ID</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Start Time</TableCell>
                <TableCell>End Time</TableCell>
                <TableCell>Distance (km)</TableCell>
                <TableCell>Energy (kWh)</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredTrips.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={9} align="center">
                    No trips found
                  </TableCell>
                </TableRow>
              ) : (
                filteredTrips.map((trip: any) => (
                  <TableRow key={trip.id}>
                    <TableCell>{trip.id}</TableCell>
                    <TableCell>{trip.vehicleId}</TableCell>
                    <TableCell>{trip.driverId || '-'}</TableCell>
                    <TableCell>
                      <Chip
                        label={trip.status}
                        color={getStatusColor(trip.status)}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      {trip.startTime
                        ? new Date(trip.startTime).toLocaleString()
                        : '-'}
                    </TableCell>
                    <TableCell>
                      {trip.endTime ? new Date(trip.endTime).toLocaleString() : '-'}
                    </TableCell>
                    <TableCell>{trip.distance?.toFixed(2) || '-'}</TableCell>
                    <TableCell>{trip.energyConsumed?.toFixed(2) || '-'}</TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                        {trip.status === 'ONGOING' && (
                          <>
                            <Button
                              size="small"
                              variant="contained"
                              color="warning"
                              onClick={() => handlePauseTrip(trip)}
                              disabled={actionLoading}
                            >
                              Pause
                            </Button>
                            <Button
                              size="small"
                              variant="contained"
                              color="success"
                              onClick={() => handleEndTrip(trip)}
                              disabled={actionLoading}
                            >
                              End
                            </Button>
                          </>
                        )}
                        {trip.status === 'PAUSED' && (
                          <>
                            <Button
                              size="small"
                              variant="contained"
                              color="info"
                              onClick={() => handleResumeTrip(trip)}
                              disabled={actionLoading}
                            >
                              Resume
                            </Button>
                            <Button
                              size="small"
                              variant="contained"
                              color="error"
                              onClick={() => handleCancelTrip(trip)}
                              disabled={actionLoading}
                            >
                              Cancel
                            </Button>
                          </>
                        )}
                        <Button
                          size="small"
                          variant="outlined"
                          onClick={() => handleViewDetails(trip)}
                        >
                          Details
                        </Button>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        )}
      </TableContainer>

      {/* Trip Details Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Trip Details</DialogTitle>
        <DialogContent dividers>
          {selectedTrip && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography variant="body2">
                <strong>Trip ID:</strong> {selectedTrip.id}
              </Typography>
              <Typography variant="body2">
                <strong>Vehicle ID:</strong> {selectedTrip.vehicleId}
              </Typography>
              <Typography variant="body2">
                <strong>Driver ID:</strong> {selectedTrip.driverId || '-'}
              </Typography>
              <Typography variant="body2">
                <strong>Status:</strong> <Chip label={selectedTrip.status} color={getStatusColor(selectedTrip.status)} size="small" />
              </Typography>
              <Typography variant="body2">
                <strong>Start Time:</strong>{' '}
                {selectedTrip.startTime ? new Date(selectedTrip.startTime).toLocaleString() : '-'}
              </Typography>
              <Typography variant="body2">
                <strong>End Time:</strong>{' '}
                {selectedTrip.endTime ? new Date(selectedTrip.endTime).toLocaleString() : '-'}
              </Typography>
              <Typography variant="body2">
                <strong>Distance:</strong> {selectedTrip.distance?.toFixed(2) || '-'} km
              </Typography>
              <Typography variant="body2">
                <strong>Energy Consumed:</strong> {selectedTrip.energyConsumed?.toFixed(2) || '-'} kWh
              </Typography>
              <Typography variant="body2">
                <strong>Start Location:</strong> Lat: {selectedTrip.startLocation?.latitude || '-'}, Lng:{' '}
                {selectedTrip.startLocation?.longitude || '-'}
              </Typography>
              <Typography variant="body2">
                <strong>End Location:</strong> Lat: {selectedTrip.endLocation?.latitude || '-'}, Lng:{' '}
                {selectedTrip.endLocation?.longitude || '-'}
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default TripManagementPage;

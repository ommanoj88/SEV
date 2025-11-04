import React, { useEffect } from 'react';
import { Box, Paper, Grid, Typography, Avatar, Chip, Divider } from '@mui/material';
import { useParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@redux/hooks';
import { fetchDriverById } from '@redux/slices/driverSlice';
import { formatDate } from '@utils/formatters';

const DriverDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const dispatch = useAppDispatch();
  const { selectedDriver } = useAppSelector((state) => state.drivers);

  useEffect(() => {
    if (id) dispatch(fetchDriverById(Number(id)));
  }, [id, dispatch]);

  if (!selectedDriver) return null;

  return (
    <Grid container spacing={3}>
      <Grid item xs={12} md={4}>
        <Paper sx={{ p: 3, textAlign: 'center' }}>
          <Avatar sx={{ width: 100, height: 100, mx: 'auto', mb: 2 }}>{selectedDriver.firstName[0]}{selectedDriver.lastName[0]}</Avatar>
          <Typography variant="h6">{selectedDriver.firstName} {selectedDriver.lastName}</Typography>
          <Typography variant="body2" color="text.secondary" gutterBottom>{selectedDriver.email}</Typography>
          <Chip label={selectedDriver.status} color="success" sx={{ mt: 1 }} />
        </Paper>
      </Grid>
      <Grid item xs={12} md={8}>
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>Driver Information</Typography>
          <Divider sx={{ mb: 2 }} />
          <Grid container spacing={2}>
            <Grid item xs={6}><Typography variant="body2" color="text.secondary">Phone</Typography><Typography>{selectedDriver.phone}</Typography></Grid>
            <Grid item xs={6}><Typography variant="body2" color="text.secondary">License</Typography><Typography>{selectedDriver.licenseNumber}</Typography></Grid>
            <Grid item xs={6}><Typography variant="body2" color="text.secondary">License Expiry</Typography><Typography>{formatDate(selectedDriver.licenseExpiry)}</Typography></Grid>
            <Grid item xs={6}><Typography variant="body2" color="text.secondary">Status</Typography><Typography>{selectedDriver.status}</Typography></Grid>
            <Grid item xs={6}><Typography variant="body2" color="text.secondary">Total Trips</Typography><Typography variant="h6">{selectedDriver.totalTrips}</Typography></Grid>
            <Grid item xs={6}><Typography variant="body2" color="text.secondary">Total Distance</Typography><Typography variant="h6">{selectedDriver.totalDistance} mi</Typography></Grid>
          </Grid>
        </Paper>
      </Grid>
    </Grid>
  );
};

export default DriverDetails;

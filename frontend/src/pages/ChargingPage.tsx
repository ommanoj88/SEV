import React, { useEffect } from 'react';
import { Box, Typography, Grid, Card, CardContent, Chip, Button, LinearProgress } from '@mui/material';
import { EvStation, PlayArrow } from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '../redux/hooks';
import { fetchAllStations, fetchAllSessions, selectStations, selectSessions, selectChargingLoading } from '../redux/slices/chargingSlice';
import { formatCurrency, formatEnergy } from '../utils/formatters';

const ChargingPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const stations = useAppSelector(selectStations);
  const sessions = useAppSelector(selectSessions);
  const loading = useAppSelector(selectChargingLoading);

  useEffect(() => {
    dispatch(fetchAllStations(undefined));
    dispatch(fetchAllSessions(undefined));
  }, [dispatch]);

  if (loading) return <LinearProgress />;

  const activeSessions = sessions.filter(s => s.status === 'CHARGING');

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" fontWeight={600} gutterBottom>Charging Management</Typography>
          <Typography variant="body2" color="text.secondary">Monitor charging stations and sessions</Typography>
        </Box>
        <Button variant="contained" startIcon={<PlayArrow />}>Start Charging Session</Button>
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Typography variant="h6" gutterBottom>Active Sessions ({activeSessions.length})</Typography>
          {activeSessions.map(session => (
            <Card key={session.id} sx={{ mb: 2 }}>
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={1}>
                  <Typography variant="subtitle1" fontWeight={600}>{session.vehicleName || 'Vehicle'}</Typography>
                  <Chip label={session.status} size="small" color="primary" />
                </Box>
                <Typography variant="body2" color="text.secondary">{session.stationName}</Typography>
                <Box display="flex" justifyContent="space-between" mt={2}>
                  <Typography variant="caption">Energy: {formatEnergy(session.energyDelivered)}</Typography>
                  <Typography variant="caption">Cost: {formatCurrency(session.cost)}</Typography>
                </Box>
              </CardContent>
            </Card>
          ))}
          {activeSessions.length === 0 && (
            <Card><CardContent><Typography color="text.secondary" align="center">No active charging sessions</Typography></CardContent></Card>
          )}
        </Grid>

        <Grid item xs={12} md={6}>
          <Typography variant="h6" gutterBottom>Charging Stations ({stations.length})</Typography>
          {stations.slice(0, 5).map(station => (
            <Card key={station.id} sx={{ mb: 2 }}>
              <CardContent>
                <Box display="flex" alignItems="flex-start" gap={2}>
                  <EvStation color="primary" />
                  <Box flex={1}>
                    <Typography variant="subtitle1" fontWeight={600}>{station.name}</Typography>
                    <Typography variant="body2" color="text.secondary" gutterBottom>{station.location.address}</Typography>
                    <Box display="flex" justifyContent="space-between" alignItems="center">
                      <Chip label={station.status} size="small" color={station.status === 'ACTIVE' ? 'success' : 'default'} />
                      <Typography variant="caption">{station.availablePorts}/{station.totalPorts} ports available</Typography>
                    </Box>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          ))}
        </Grid>
      </Grid>
    </Box>
  );
};

export default ChargingPage;

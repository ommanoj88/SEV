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
  Tabs,
  Tab,
} from '@mui/material';
import {
  fetchAllStations,
  fetchAllSessions,
  startChargingSession,
  endChargingSession,
  selectStations,
  selectSessions,
  selectChargingLoading,
  selectChargingError,
} from '../redux/slices/chargingSlice';
import { ChargingStation, ChargingSession } from '../types';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`charging-tabpanel-${index}`}
      aria-labelledby={`charging-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 2 }}>{children}</Box>}
    </div>
  );
}

const ChargingManagementPage: React.FC = () => {
  const dispatch = useDispatch();
  const stations = useSelector(selectStations);
  const sessions = useSelector(selectSessions);
  const loading = useSelector(selectChargingLoading);
  const error = useSelector(selectChargingError);

  const [tabValue, setTabValue] = useState(0);
  const [selectedStation, setSelectedStation] = useState<ChargingStation | null>(null);
  const [selectedSession, setSelectedSession] = useState<ChargingSession | null>(null);
  const [stationDialogOpen, setStationDialogOpen] = useState(false);
  const [sessionDialogOpen, setSessionDialogOpen] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    dispatch(fetchAllStations(undefined) as any);
    dispatch(fetchAllSessions(undefined) as any);
  }, [dispatch]);

  const getStationStatusColor = (status: string) => {
    const colors: Record<string, any> = {
      ACTIVE: 'success',
      INACTIVE: 'error',
      MAINTENANCE: 'warning',
      FULL: 'warning',
    };
    return colors[status] || 'default';
  };

  const getSessionStatusColor = (status: string) => {
    const colors: Record<string, any> = {
      INITIATED: 'info',
      CHARGING: 'warning',
      COMPLETED: 'success',
      FAILED: 'error',
      CANCELLED: 'error',
    };
    return colors[status] || 'default';
  };

  const filteredStations = stations.filter(
    (station) =>
      !searchTerm ||
      station.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      station.id?.toString().includes(searchTerm)
  );

  const filteredSessions = sessions.filter(
    (session) =>
      !searchTerm ||
      session.vehicleId?.toString().includes(searchTerm) ||
      session.id?.toString().includes(searchTerm)
  );

  const handleViewStationDetails = (station: ChargingStation) => {
    setSelectedStation(station);
    setStationDialogOpen(true);
  };

  const handleViewSessionDetails = (session: ChargingSession) => {
    setSelectedSession(session);
    setSessionDialogOpen(true);
  };

  const handleEndSession = async (session: ChargingSession) => {
    if (!session.id) return;
    setActionLoading(true);
    try {
      dispatch(endChargingSession(Number(session.id)) as any);
      dispatch(fetchAllSessions(undefined) as any);
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <Container maxWidth="lg" sx={{ py: 3 }}>
      <Typography variant="h4" sx={{ mb: 3, fontWeight: 'bold' }}>
        Charging Management
      </Typography>

      {error && <Alert severity="error">{error}</Alert>}

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Total Stations</Typography>
              <Typography variant="h5">{stations.length}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Active Stations</Typography>
              <Typography variant="h5">{stations.filter((s) => s.status === 'ACTIVE').length}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Active Sessions</Typography>
              <Typography variant="h5">{sessions.filter((s) => s.status === 'CHARGING').length}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Total Sessions</Typography>
              <Typography variant="h5">{sessions.length}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Tabs */}
      <Paper sx={{ mb: 3 }}>
        <Tabs
          value={tabValue}
          onChange={(e, newValue) => setTabValue(newValue)}
          aria-label="charging tabs"
        >
          <Tab label="Charging Stations" id="charging-tab-0" aria-controls="charging-tabpanel-0" />
          <Tab label="Charging Sessions" id="charging-tab-1" aria-controls="charging-tabpanel-1" />
        </Tabs>

        {/* Stations Tab */}
        <TabPanel value={tabValue} index={0}>
          <TextField
            fullWidth
            label="Search Stations"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search by name or ID"
            sx={{ mb: 2 }}
          />

          <TableContainer>
            {loading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
                <CircularProgress />
              </Box>
            ) : (
              <Table>
                <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
                  <TableRow>
                    <TableCell>Station ID</TableCell>
                    <TableCell>Name</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Total Ports</TableCell>
                    <TableCell>Available Ports</TableCell>
                    <TableCell>Location</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredStations.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={7} align="center">
                        No stations found
                      </TableCell>
                    </TableRow>
                  ) : (
                    filteredStations.map((station) => (
                      <TableRow key={station.id}>
                        <TableCell>{station.id}</TableCell>
                        <TableCell>{station.name}</TableCell>
                        <TableCell>
                          <Chip
                            label={station.status}
                            color={getStationStatusColor(station.status)}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>{station.totalPorts || '-'}</TableCell>
                        <TableCell>{station.availablePorts || '-'}</TableCell>
                        <TableCell>
                          {station.location?.latitude?.toFixed(4)},{' '}
                          {station.location?.longitude?.toFixed(4)}
                        </TableCell>
                        <TableCell>
                          <Button
                            size="small"
                            variant="outlined"
                            onClick={() => handleViewStationDetails(station)}
                          >
                            Details
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            )}
          </TableContainer>
        </TabPanel>

        {/* Sessions Tab */}
        <TabPanel value={tabValue} index={1}>
          <TextField
            fullWidth
            label="Search Sessions"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search by vehicle ID or session ID"
            sx={{ mb: 2 }}
          />

          <TableContainer>
            {loading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
                <CircularProgress />
              </Box>
            ) : (
              <Table>
                <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
                  <TableRow>
                    <TableCell>Session ID</TableCell>
                    <TableCell>Vehicle ID</TableCell>
                    <TableCell>Station ID</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Start Time</TableCell>
                    <TableCell>End Time</TableCell>
                    <TableCell>Energy (kWh)</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredSessions.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={8} align="center">
                        No sessions found
                      </TableCell>
                    </TableRow>
                  ) : (
                    filteredSessions.map((session) => (
                      <TableRow key={session.id}>
                        <TableCell>{session.id}</TableCell>
                        <TableCell>{session.vehicleId}</TableCell>
                        <TableCell>{session.stationId}</TableCell>
                        <TableCell>
                          <Chip
                            label={session.status}
                            color={getSessionStatusColor(session.status)}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>
                          {session.startTime
                            ? new Date(session.startTime).toLocaleString()
                            : '-'}
                        </TableCell>
                        <TableCell>
                          {session.endTime
                            ? new Date(session.endTime).toLocaleString()
                            : '-'}
                        </TableCell>
                        <TableCell>{session.energyDelivered?.toFixed(2) || '-'}</TableCell>
                        <TableCell>
                          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                            {session.status === 'CHARGING' && (
                              <Button
                                size="small"
                                variant="contained"
                                color="success"
                                onClick={() => handleEndSession(session)}
                                disabled={actionLoading}
                              >
                                End
                              </Button>
                            )}
                            <Button
                              size="small"
                              variant="outlined"
                              onClick={() => handleViewSessionDetails(session)}
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
        </TabPanel>
      </Paper>

      {/* Station Details Dialog */}
      <Dialog
        open={stationDialogOpen}
        onClose={() => setStationDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Station Details</DialogTitle>
        <DialogContent dividers>
          {selectedStation && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography variant="body2">
                <strong>Station ID:</strong> {selectedStation.id}
              </Typography>
              <Typography variant="body2">
                <strong>Name:</strong> {selectedStation.name}
              </Typography>
              <Typography variant="body2">
                <strong>Status:</strong>{' '}
                <Chip
                  label={selectedStation.status}
                  color={getStationStatusColor(selectedStation.status)}
                  size="small"
                />
              </Typography>
              <Typography variant="body2">
                <strong>Location:</strong> Lat: {selectedStation.location?.latitude?.toFixed(4)},
                Lng: {selectedStation.location?.longitude?.toFixed(4)}
              </Typography>
              <Typography variant="body2">
                <strong>Total Ports:</strong> {selectedStation.totalPorts}
              </Typography>
              <Typography variant="body2">
                <strong>Available Ports:</strong> {selectedStation.availablePorts}
              </Typography>
              <Typography variant="body2">
                <strong>Charging Power (kW):</strong> {selectedStation.powerOutput}
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setStationDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Session Details Dialog */}
      <Dialog
        open={sessionDialogOpen}
        onClose={() => setSessionDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Session Details</DialogTitle>
        <DialogContent dividers>
          {selectedSession && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography variant="body2">
                <strong>Session ID:</strong> {selectedSession.id}
              </Typography>
              <Typography variant="body2">
                <strong>Vehicle ID:</strong> {selectedSession.vehicleId}
              </Typography>
              <Typography variant="body2">
                <strong>Station ID:</strong> {selectedSession.stationId}
              </Typography>
              <Typography variant="body2">
                <strong>Status:</strong>{' '}
                <Chip
                  label={selectedSession.status}
                  color={getSessionStatusColor(selectedSession.status)}
                  size="small"
                />
              </Typography>
              <Typography variant="body2">
                <strong>Start Time:</strong>{' '}
                {selectedSession.startTime ? new Date(selectedSession.startTime).toLocaleString() : '-'}
              </Typography>
              <Typography variant="body2">
                <strong>End Time:</strong>{' '}
                {selectedSession.endTime ? new Date(selectedSession.endTime).toLocaleString() : '-'}
              </Typography>
              <Typography variant="body2">
                <strong>Energy Delivered:</strong> {selectedSession.energyDelivered?.toFixed(2)} kWh
              </Typography>
              <Typography variant="body2">
                <strong>Cost:</strong> ${selectedSession.cost?.toFixed(2) || '-'}
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSessionDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default ChargingManagementPage;

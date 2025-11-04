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
import {
  fetchAllDrivers,
  fetchLeaderboard,
  selectAllDrivers,
  selectLeaderboard,
  selectDriverLoading,
  selectDriverError,
} from '../redux/slices/driverSlice';
import { Driver, DriverLeaderboard } from '../types';

const DriverManagementPage: React.FC = () => {
  const dispatch = useDispatch();
  const drivers = useSelector(selectAllDrivers);
  const leaderboard = useSelector(selectLeaderboard);
  const loading = useSelector(selectDriverLoading);
  const error = useSelector(selectDriverError);

  const [filterStatus, setFilterStatus] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedDriver, setSelectedDriver] = useState<Driver | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [tabValue, setTabValue] = useState(0);

  useEffect(() => {
    dispatch(fetchAllDrivers(undefined) as any);
    dispatch(fetchLeaderboard(10) as any);
  }, [dispatch]);

  const getStatusColor = (status: string) => {
    const colors: Record<string, any> = {
      ACTIVE: 'success',
      INACTIVE: 'error',
      SUSPENDED: 'error',
      ON_LEAVE: 'warning',
      TERMINATED: 'error',
    };
    return colors[status] || 'default';
  };

  const filteredDrivers = drivers.filter(
    (driver) =>
      (!filterStatus || driver.status === filterStatus) &&
      (!searchTerm ||
        driver.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        driver.lastName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        driver.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        driver.licenseNumber?.includes(searchTerm))
  );

  const activeDrivers = drivers.filter((d) => d.status === 'ACTIVE').length;
  const inactiveDrivers = drivers.filter((d) => d.status === 'INACTIVE').length;
  const suspendedDrivers = drivers.filter((d) => d.status === 'SUSPENDED').length;

  return (
    <Container maxWidth="lg" sx={{ py: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
          Driver Management
        </Typography>
        <Button variant="contained" color="primary">
          + Add Driver
        </Button>
      </Box>

      {error && <Alert severity="error">{error}</Alert>}

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Total Drivers</Typography>
              <Typography variant="h5">{drivers.length}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Active</Typography>
              <Typography variant="h5" sx={{ color: 'success.main' }}>
                {activeDrivers}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Inactive</Typography>
              <Typography variant="h5" sx={{ color: 'warning.main' }}>
                {inactiveDrivers}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Suspended</Typography>
              <Typography variant="h5" sx={{ color: 'error.main' }}>
                {suspendedDrivers}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Search Drivers"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Name, email, or license number"
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              select
              fullWidth
              label="Filter by Status"
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              SelectProps={{
                native: true,
              }}
            >
              <option value="">All Status</option>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
              <option value="SUSPENDED">Suspended</option>
              <option value="ON_LEAVE">On Leave</option>
            </TextField>
          </Grid>
        </Grid>
      </Paper>

      {/* Tabs */}
      <Paper sx={{ mb: 3 }}>
        <Box
          sx={{
            display: 'flex',
            p: 2,
            gap: 2,
            borderBottom: 1,
            borderColor: 'divider',
          }}
        >
          <Button
            variant={tabValue === 0 ? 'contained' : 'text'}
            onClick={() => setTabValue(0)}
          >
            All Drivers ({drivers.length})
          </Button>
          <Button
            variant={tabValue === 1 ? 'contained' : 'text'}
            onClick={() => setTabValue(1)}
          >
            Leaderboard ({leaderboard.length})
          </Button>
        </Box>

        {/* Drivers Tab */}
        {tabValue === 0 && (
          <TableContainer>
            {loading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
                <CircularProgress />
              </Box>
            ) : (
              <Table>
                <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell>Email</TableCell>
                    <TableCell>Phone</TableCell>
                    <TableCell>License Number</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Active Vehicle</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredDrivers.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={7} align="center">
                        No drivers found
                      </TableCell>
                    </TableRow>
                  ) : (
                    filteredDrivers.map((driver) => (
                      <TableRow key={driver.id}>
                        <TableCell>
                          {driver.firstName} {driver.lastName}
                        </TableCell>
                        <TableCell>{driver.email}</TableCell>
                        <TableCell>{driver.phone}</TableCell>
                        <TableCell>{driver.licenseNumber}</TableCell>
                        <TableCell>
                          <Chip
                            label={driver.status}
                            color={getStatusColor(driver.status) as any}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>-</TableCell>
                        <TableCell>
                          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                            <Button
                              size="small"
                              variant="outlined"
                              onClick={() => {
                                setSelectedDriver(driver);
                                setDialogOpen(true);
                              }}
                            >
                              View
                            </Button>
                            <Button size="small" variant="outlined" color="warning">
                              Edit
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
        )}

        {/* Leaderboard Tab */}
        {tabValue === 1 && (
          <Box sx={{ p: 2 }}>
            {loading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
                <CircularProgress />
              </Box>
            ) : (
              <Grid container spacing={2}>
                {leaderboard.map((entry, index) => (
                  <Grid item xs={12} sm={6} md={4} key={entry.driverId}>
                    <Card sx={{ position: 'relative' }}>
                      <Box
                        sx={{
                          position: 'absolute',
                          top: 10,
                          right: 10,
                          backgroundColor: 'primary.main',
                          color: 'white',
                          borderRadius: '50%',
                          width: 40,
                          height: 40,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          fontWeight: 'bold',
                          fontSize: '18px',
                        }}
                      >
                        #{index + 1}
                      </Box>
                      <CardContent>
                        <Typography variant="h6" sx={{ mb: 1 }}>
                          {entry.driverName}
                        </Typography>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                          <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                            <Typography variant="body2" color="textSecondary">
                              Performance Score
                            </Typography>
                            <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                              {entry.performanceScore?.toFixed(1)}/10
                            </Typography>
                          </Box>
                          <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                            <Typography variant="body2" color="textSecondary">
                              Trips
                            </Typography>
                            <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                              {entry.totalTrips}
                            </Typography>
                          </Box>
                          <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                            <Typography variant="body2" color="textSecondary">
                              Distance
                            </Typography>
                            <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                              {entry.totalDistance?.toFixed(0)} km
                            </Typography>
                          </Box>
                          <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                            <Typography variant="body2" color="textSecondary">
                              Rating
                            </Typography>
                            <Typography variant="body2" sx={{ fontWeight: 'bold', color: 'success.main' }}>
                              {'⭐'.repeat(Math.round(entry.performanceScore || 0) / 2)}
                            </Typography>
                          </Box>
                        </Box>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            )}
          </Box>
        )}
      </Paper>

      {/* Driver Details Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Driver Details</DialogTitle>
        <DialogContent dividers>
          {selectedDriver && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography variant="body2">
                <strong>Name:</strong> {selectedDriver.firstName} {selectedDriver.lastName}
              </Typography>
              <Typography variant="body2">
                <strong>Email:</strong> {selectedDriver.email}
              </Typography>
              <Typography variant="body2">
                <strong>Phone:</strong> {selectedDriver.phone}
              </Typography>
              <Typography variant="body2">
                <strong>License Number:</strong> {selectedDriver.licenseNumber}
              </Typography>
              <Typography variant="body2">
                <strong>License Expiry:</strong>{' '}
                {selectedDriver.licenseExpiry
                  ? new Date(selectedDriver.licenseExpiry).toLocaleDateString()
                  : '-'}
              </Typography>
              <Typography variant="body2">
                <strong>Status:</strong>{' '}
                <Chip
                  label={selectedDriver.status}
                  color={getStatusColor(selectedDriver.status) as any}
                  size="small"
                />
              </Typography>
              <Typography variant="body2">
                <strong>Assigned Vehicle:</strong> -
              </Typography>
              <Typography variant="body2">
                <strong>Date of Birth:</strong>{' '}
                {selectedDriver.dateOfBirth
                  ? new Date(selectedDriver.dateOfBirth).toLocaleDateString()
                  : '-'}
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

export default DriverManagementPage;

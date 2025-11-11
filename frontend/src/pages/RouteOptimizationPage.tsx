import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  Grid,
  IconButton,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
  Alert,
  Tabs,
  Tab,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  PlayArrow as StartIcon,
  CheckCircle as CompleteIcon,
  Cancel as CancelIcon,
  Map as MapIcon,
  Route as RouteIcon,
} from '@mui/icons-material';
import { format } from 'date-fns';
import axios from 'axios';

interface RoutePlan {
  id: number;
  routeName: string;
  routeDescription?: string;
  vehicleId?: number;
  driverId?: number;
  originLat: number;
  originLng: number;
  originAddress?: string;
  destinationLat: number;
  destinationLng: number;
  destinationAddress?: string;
  totalDistance?: number;
  estimatedDuration?: number;
  estimatedFuelConsumption?: number;
  estimatedCost?: number;
  plannedStartTime?: string;
  plannedEndTime?: string;
  optimizationCriteria?: string;
  status: string;
  actualDistance?: number;
  actualDuration?: number;
  createdAt: string;
}

interface RouteWaypoint {
  id?: number;
  sequenceNumber: number;
  waypointName?: string;
  latitude: number;
  longitude: number;
  address?: string;
  serviceType?: string;
  customerName?: string;
  customerPhone?: string;
  status: string;
}

const RouteOptimizationPage: React.FC = () => {
  const [routes, setRoutes] = useState<RoutePlan[]>([]);
  const [filteredRoutes, setFilteredRoutes] = useState<RoutePlan[]>([]);
  const [activeTab, setActiveTab] = useState(0);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [waypointDialogOpen, setWaypointDialogOpen] = useState(false);
  const [selectedRoute, setSelectedRoute] = useState<RoutePlan | null>(null);
  const [waypoints, setWaypoints] = useState<RouteWaypoint[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [formData, setFormData] = useState({
    routeName: '',
    routeDescription: '',
    vehicleId: '',
    driverId: '',
    originLat: '',
    originLng: '',
    originAddress: '',
    destinationLat: '',
    destinationLng: '',
    destinationAddress: '',
    plannedStartTime: '',
    plannedEndTime: '',
    optimizationCriteria: 'DISTANCE',
    trafficConsidered: true,
    tollRoadsAllowed: true,
  });

  const [waypointFormData, setWaypointFormData] = useState({
    waypointName: '',
    latitude: '',
    longitude: '',
    address: '',
    serviceType: 'DELIVERY',
    customerName: '',
    customerPhone: '',
    stopDuration: '',
  });

  useEffect(() => {
    fetchRoutes();
  }, []);

  useEffect(() => {
    filterRoutes();
  }, [routes, activeTab]);

  const fetchRoutes = async () => {
    try {
      const response = await axios.get('/api/routes');
      setRoutes(response.data);
    } catch (err) {
      setError('Failed to fetch routes');
      console.error('Error fetching routes:', err);
    }
  };

  const fetchWaypoints = async (routeId: number) => {
    try {
      const response = await axios.get(`/api/routes/${routeId}/waypoints`);
      setWaypoints(response.data);
    } catch (err) {
      console.error('Error fetching waypoints:', err);
    }
  };

  const filterRoutes = () => {
    let filtered = [...routes];
    
    switch (activeTab) {
      case 0: // All
        break;
      case 1: // Planned
        filtered = routes.filter(r => r.status === 'PLANNED');
        break;
      case 2: // In Progress
        filtered = routes.filter(r => r.status === 'IN_PROGRESS');
        break;
      case 3: // Completed
        filtered = routes.filter(r => r.status === 'COMPLETED');
        break;
    }
    
    setFilteredRoutes(filtered);
  };

  const handleOpenDialog = (route?: RoutePlan) => {
    if (route) {
      setFormData({
        routeName: route.routeName,
        routeDescription: route.routeDescription || '',
        vehicleId: route.vehicleId?.toString() || '',
        driverId: route.driverId?.toString() || '',
        originLat: route.originLat.toString(),
        originLng: route.originLng.toString(),
        originAddress: route.originAddress || '',
        destinationLat: route.destinationLat.toString(),
        destinationLng: route.destinationLng.toString(),
        destinationAddress: route.destinationAddress || '',
        plannedStartTime: route.plannedStartTime || '',
        plannedEndTime: route.plannedEndTime || '',
        optimizationCriteria: route.optimizationCriteria || 'DISTANCE',
        trafficConsidered: true,
        tollRoadsAllowed: true,
      });
      setSelectedRoute(route);
      fetchWaypoints(route.id);
    } else {
      setFormData({
        routeName: '',
        routeDescription: '',
        vehicleId: '',
        driverId: '',
        originLat: '',
        originLng: '',
        originAddress: '',
        destinationLat: '',
        destinationLng: '',
        destinationAddress: '',
        plannedStartTime: '',
        plannedEndTime: '',
        optimizationCriteria: 'DISTANCE',
        trafficConsidered: true,
        tollRoadsAllowed: true,
      });
      setSelectedRoute(null);
      setWaypoints([]);
    }
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setSelectedRoute(null);
  };

  const handleSubmit = async () => {
    try {
      const data = {
        routeName: formData.routeName,
        routeDescription: formData.routeDescription,
        vehicleId: formData.vehicleId ? parseInt(formData.vehicleId) : null,
        driverId: formData.driverId ? parseInt(formData.driverId) : null,
        originLat: parseFloat(formData.originLat),
        originLng: parseFloat(formData.originLng),
        originAddress: formData.originAddress,
        destinationLat: parseFloat(formData.destinationLat),
        destinationLng: parseFloat(formData.destinationLng),
        destinationAddress: formData.destinationAddress,
        plannedStartTime: formData.plannedStartTime || null,
        plannedEndTime: formData.plannedEndTime || null,
        optimizationCriteria: formData.optimizationCriteria,
        trafficConsidered: formData.trafficConsidered,
        tollRoadsAllowed: formData.tollRoadsAllowed,
      };

      if (selectedRoute) {
        await axios.put(`/api/routes/${selectedRoute.id}`, data);
        setSuccess('Route updated successfully');
      } else {
        await axios.post('/api/routes', data);
        setSuccess('Route created successfully');
      }
      
      handleCloseDialog();
      fetchRoutes();
    } catch (err) {
      setError('Failed to save route');
      console.error('Error saving route:', err);
    }
  };

  const handleAddWaypoint = async () => {
    if (!selectedRoute) return;

    try {
      const data = {
        waypointName: waypointFormData.waypointName,
        latitude: parseFloat(waypointFormData.latitude),
        longitude: parseFloat(waypointFormData.longitude),
        address: waypointFormData.address,
        serviceType: waypointFormData.serviceType,
        customerName: waypointFormData.customerName,
        customerPhone: waypointFormData.customerPhone,
        stopDuration: waypointFormData.stopDuration ? parseInt(waypointFormData.stopDuration) : null,
        status: 'PENDING',
      };

      await axios.post(`/api/routes/${selectedRoute.id}/waypoints`, data);
      setSuccess('Waypoint added successfully');
      setWaypointDialogOpen(false);
      fetchWaypoints(selectedRoute.id);
      
      // Reset form
      setWaypointFormData({
        waypointName: '',
        latitude: '',
        longitude: '',
        address: '',
        serviceType: 'DELIVERY',
        customerName: '',
        customerPhone: '',
        stopDuration: '',
      });
    } catch (err) {
      setError('Failed to add waypoint');
      console.error('Error adding waypoint:', err);
    }
  };

  const handleStartRoute = async (routeId: number) => {
    try {
      await axios.post(`/api/routes/${routeId}/start`);
      setSuccess('Route started successfully');
      fetchRoutes();
    } catch (err) {
      setError('Failed to start route');
      console.error('Error starting route:', err);
    }
  };

  const handleCompleteRoute = async (routeId: number) => {
    try {
      await axios.post(`/api/routes/${routeId}/complete`);
      setSuccess('Route completed successfully');
      fetchRoutes();
    } catch (err) {
      setError('Failed to complete route');
      console.error('Error completing route:', err);
    }
  };

  const handleCancelRoute = async (routeId: number) => {
    try {
      await axios.post(`/api/routes/${routeId}/cancel`);
      setSuccess('Route cancelled successfully');
      fetchRoutes();
    } catch (err) {
      setError('Failed to cancel route');
      console.error('Error cancelling route:', err);
    }
  };

  const handleDeleteRoute = async (routeId: number) => {
    if (!window.confirm('Are you sure you want to delete this route?')) return;

    try {
      await axios.delete(`/api/routes/${routeId}`);
      setSuccess('Route deleted successfully');
      fetchRoutes();
    } catch (err) {
      setError('Failed to delete route');
      console.error('Error deleting route:', err);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PLANNED': return 'info';
      case 'IN_PROGRESS': return 'warning';
      case 'COMPLETED': return 'success';
      case 'CANCELLED': return 'error';
      default: return 'default';
    }
  };

  const stats = {
    total: routes.length,
    planned: routes.filter(r => r.status === 'PLANNED').length,
    inProgress: routes.filter(r => r.status === 'IN_PROGRESS').length,
    completed: routes.filter(r => r.status === 'COMPLETED').length,
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">
          <RouteIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
          Route Optimization
        </Typography>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          Create Route
        </Button>
      </Box>

      {error && (
        <Alert severity="error" onClose={() => setError(null)} sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" onClose={() => setSuccess(null)} sx={{ mb: 2 }}>
          {success}
        </Alert>
      )}

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>Total Routes</Typography>
              <Typography variant="h4">{stats.total}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>Planned</Typography>
              <Typography variant="h4" color="info.main">{stats.planned}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>In Progress</Typography>
              <Typography variant="h4" color="warning.main">{stats.inProgress}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>Completed</Typography>
              <Typography variant="h4" color="success.main">{stats.completed}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Tabs */}
      <Paper sx={{ mb: 2 }}>
        <Tabs value={activeTab} onChange={(e, v) => setActiveTab(v)}>
          <Tab label="All Routes" />
          <Tab label="Planned" />
          <Tab label="In Progress" />
          <Tab label="Completed" />
        </Tabs>
      </Paper>

      {/* Routes Table */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Route Name</TableCell>
              <TableCell>Vehicle ID</TableCell>
              <TableCell>Driver ID</TableCell>
              <TableCell>Distance (km)</TableCell>
              <TableCell>Duration (min)</TableCell>
              <TableCell>Planned Start</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredRoutes.map((route) => (
              <TableRow key={route.id}>
                <TableCell>{route.routeName}</TableCell>
                <TableCell>{route.vehicleId || '-'}</TableCell>
                <TableCell>{route.driverId || '-'}</TableCell>
                <TableCell>{route.totalDistance?.toFixed(2) || '-'}</TableCell>
                <TableCell>{route.estimatedDuration || '-'}</TableCell>
                <TableCell>
                  {route.plannedStartTime ? format(new Date(route.plannedStartTime), 'PPp') : '-'}
                </TableCell>
                <TableCell>
                  <Chip label={route.status} color={getStatusColor(route.status)} size="small" />
                </TableCell>
                <TableCell>
                  <IconButton size="small" onClick={() => handleOpenDialog(route)} title="Edit">
                    <EditIcon />
                  </IconButton>
                  {route.status === 'PLANNED' && (
                    <IconButton size="small" onClick={() => handleStartRoute(route.id)} title="Start" color="success">
                      <StartIcon />
                    </IconButton>
                  )}
                  {route.status === 'IN_PROGRESS' && (
                    <IconButton size="small" onClick={() => handleCompleteRoute(route.id)} title="Complete" color="success">
                      <CompleteIcon />
                    </IconButton>
                  )}
                  {['PLANNED', 'IN_PROGRESS'].includes(route.status) && (
                    <IconButton size="small" onClick={() => handleCancelRoute(route.id)} title="Cancel" color="error">
                      <CancelIcon />
                    </IconButton>
                  )}
                  <IconButton size="small" onClick={() => handleDeleteRoute(route.id)} title="Delete" color="error">
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Route Dialog */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedRoute ? 'Edit Route' : 'Create New Route'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Route Name"
                value={formData.routeName}
                onChange={(e) => setFormData({ ...formData, routeName: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Description"
                value={formData.routeDescription}
                onChange={(e) => setFormData({ ...formData, routeDescription: e.target.value })}
                multiline
                rows={2}
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Vehicle ID"
                type="number"
                value={formData.vehicleId}
                onChange={(e) => setFormData({ ...formData, vehicleId: e.target.value })}
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Driver ID"
                type="number"
                value={formData.driverId}
                onChange={(e) => setFormData({ ...formData, driverId: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <Typography variant="subtitle1" sx={{ mt: 2, mb: 1 }}>Origin</Typography>
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Origin Latitude"
                type="number"
                value={formData.originLat}
                onChange={(e) => setFormData({ ...formData, originLat: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Origin Longitude"
                type="number"
                value={formData.originLng}
                onChange={(e) => setFormData({ ...formData, originLng: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Origin Address"
                value={formData.originAddress}
                onChange={(e) => setFormData({ ...formData, originAddress: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <Typography variant="subtitle1" sx={{ mt: 2, mb: 1 }}>Destination</Typography>
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Destination Latitude"
                type="number"
                value={formData.destinationLat}
                onChange={(e) => setFormData({ ...formData, destinationLat: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Destination Longitude"
                type="number"
                value={formData.destinationLng}
                onChange={(e) => setFormData({ ...formData, destinationLng: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Destination Address"
                value={formData.destinationAddress}
                onChange={(e) => setFormData({ ...formData, destinationAddress: e.target.value })}
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Planned Start Time"
                type="datetime-local"
                value={formData.plannedStartTime}
                onChange={(e) => setFormData({ ...formData, plannedStartTime: e.target.value })}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Planned End Time"
                type="datetime-local"
                value={formData.plannedEndTime}
                onChange={(e) => setFormData({ ...formData, plannedEndTime: e.target.value })}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>Optimization Criteria</InputLabel>
                <Select
                  value={formData.optimizationCriteria}
                  onChange={(e) => setFormData({ ...formData, optimizationCriteria: e.target.value })}
                  label="Optimization Criteria"
                >
                  <MenuItem value="DISTANCE">Distance</MenuItem>
                  <MenuItem value="TIME">Time</MenuItem>
                  <MenuItem value="FUEL">Fuel</MenuItem>
                  <MenuItem value="COST">Cost</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            {/* Waypoints Section */}
            {selectedRoute && (
              <Grid item xs={12}>
                <Box sx={{ mt: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                    <Typography variant="h6">Waypoints ({waypoints.length})</Typography>
                    <Button
                      variant="outlined"
                      size="small"
                      startIcon={<AddIcon />}
                      onClick={() => setWaypointDialogOpen(true)}
                    >
                      Add Waypoint
                    </Button>
                  </Box>
                  <TableContainer>
                    <Table size="small">
                      <TableHead>
                        <TableRow>
                          <TableCell>#</TableCell>
                          <TableCell>Name</TableCell>
                          <TableCell>Service Type</TableCell>
                          <TableCell>Customer</TableCell>
                          <TableCell>Status</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {waypoints.map((wp) => (
                          <TableRow key={wp.id}>
                            <TableCell>{wp.sequenceNumber}</TableCell>
                            <TableCell>{wp.waypointName || '-'}</TableCell>
                            <TableCell>{wp.serviceType || '-'}</TableCell>
                            <TableCell>{wp.customerName || '-'}</TableCell>
                            <TableCell>
                              <Chip label={wp.status} size="small" />
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </Box>
              </Grid>
            )}
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button onClick={handleSubmit} variant="contained" color="primary">
            {selectedRoute ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Waypoint Dialog */}
      <Dialog open={waypointDialogOpen} onClose={() => setWaypointDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add Waypoint</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Waypoint Name"
                value={waypointFormData.waypointName}
                onChange={(e) => setWaypointFormData({ ...waypointFormData, waypointName: e.target.value })}
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Latitude"
                type="number"
                value={waypointFormData.latitude}
                onChange={(e) => setWaypointFormData({ ...waypointFormData, latitude: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Longitude"
                type="number"
                value={waypointFormData.longitude}
                onChange={(e) => setWaypointFormData({ ...waypointFormData, longitude: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Address"
                value={waypointFormData.address}
                onChange={(e) => setWaypointFormData({ ...waypointFormData, address: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>Service Type</InputLabel>
                <Select
                  value={waypointFormData.serviceType}
                  onChange={(e) => setWaypointFormData({ ...waypointFormData, serviceType: e.target.value })}
                  label="Service Type"
                >
                  <MenuItem value="PICKUP">Pickup</MenuItem>
                  <MenuItem value="DELIVERY">Delivery</MenuItem>
                  <MenuItem value="SERVICE">Service</MenuItem>
                  <MenuItem value="REST">Rest</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Customer Name"
                value={waypointFormData.customerName}
                onChange={(e) => setWaypointFormData({ ...waypointFormData, customerName: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Customer Phone"
                value={waypointFormData.customerPhone}
                onChange={(e) => setWaypointFormData({ ...waypointFormData, customerPhone: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Stop Duration (minutes)"
                type="number"
                value={waypointFormData.stopDuration}
                onChange={(e) => setWaypointFormData({ ...waypointFormData, stopDuration: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setWaypointDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleAddWaypoint} variant="contained" color="primary">
            Add
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default RouteOptimizationPage;

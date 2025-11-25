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
  FormControlLabel,
  Checkbox,
} from '@mui/material';
import { fetchAllGeofences, createGeofence, updateGeofence, deleteGeofence, selectAllGeofences, selectGeofenceLoading, selectGeofenceError } from '../redux/slices/geofenceSlice';
import { Geofence, GeofenceType } from '../types';

const GeofenceManagementPage: React.FC = () => {
  const dispatch = useDispatch();
  const geofences = useSelector(selectAllGeofences);
  const loading = useSelector(selectGeofenceLoading);
  const error = useSelector(selectGeofenceError);

  const [filterType, setFilterType] = useState<GeofenceType | ''>('');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedGeofence, setSelectedGeofence] = useState<Geofence | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [formOpen, setFormOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);

  const [formData, setFormData] = useState({
    name: '',
    type: '' as GeofenceType | '',
    latitude: 0,
    longitude: 0,
    radius: 100,
    speedLimit: 80,
    alertOnEntry: false,
    alertOnExit: false,
  });

  useEffect(() => {
    dispatch(fetchAllGeofences(undefined) as any);
  }, [dispatch]);

  const getTypeColor = (type: GeofenceType) => {
    const colors: Record<GeofenceType, any> = {
      CHARGING_ZONE: 'success',
      DEPOT: 'info',
      RESTRICTED: 'error',
      CUSTOMER_LOCATION: 'warning',
      SERVICE_AREA: 'primary',
      NO_GO_ZONE: 'error',
      PARKING_AREA: 'secondary',
    };
    return colors[type] || 'default';
  };

  const filteredGeofences = geofences.filter(
    (geofence: any) =>
      (!filterType || geofence.type === filterType) &&
      (!searchTerm || geofence.name.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  const handleAddGeofence = () => {
    setFormData({
      name: '',
      type: '',
      latitude: 0,
      longitude: 0,
      radius: 100,
      speedLimit: 80,
      alertOnEntry: false,
      alertOnExit: false,
    });
    setIsEditing(false);
    setFormOpen(true);
  };

  const handleEditGeofence = (geofence: Geofence) => {
    setFormData({
      name: geofence.name,
      type: geofence.type,
      latitude: geofence.coordinates?.[0]?.latitude || 0,
      longitude: geofence.coordinates?.[0]?.longitude || 0,
      radius: geofence.radius || 100,
      speedLimit: 80,
      alertOnEntry: false,
      alertOnExit: false,
    });
    setSelectedGeofence(geofence);
    setIsEditing(true);
    setFormOpen(true);
  };

  const handleSubmit = async () => {
    if (!formData.name || !formData.type) {
      alert('Name and Type are required');
      return;
    }

    setActionLoading(true);
    try {
      const payload: any = {
        name: formData.name,
        type: formData.type as GeofenceType,
        shape: 'CIRCLE',
        coordinates: [{
          latitude: formData.latitude,
          longitude: formData.longitude,
        }],
        radius: formData.radius,
        speedLimit: formData.speedLimit,
        alertOnEntry: formData.alertOnEntry,
        alertOnExit: formData.alertOnExit,
        companyId: 1,
      };

      if (isEditing && selectedGeofence?.id) {
        dispatch(updateGeofence({ geofenceId: Number(selectedGeofence.id), data: payload }) as any);
      } else {
        dispatch(createGeofence(payload) as any);
      }

      setFormOpen(false);
      dispatch(fetchAllGeofences(undefined) as any);
    } finally {
      setActionLoading(false);
    }
  };

  const handleDeleteGeofence = async (geofence: Geofence) => {
    if (!geofence.id || !window.confirm('Are you sure you want to delete this geofence?')) return;
    setActionLoading(true);
    try {
      dispatch(deleteGeofence(Number(geofence.id)) as any);
      dispatch(fetchAllGeofences(undefined) as any);
    } finally {
      setActionLoading(false);
    }
  };

  const handleViewDetails = (geofence: Geofence) => {
    setSelectedGeofence(geofence);
    setDialogOpen(true);
  };

  return (
    <Container maxWidth="lg" sx={{ py: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
          Geofence Management
        </Typography>
        <Button variant="contained" color="primary" onClick={handleAddGeofence}>
          + Add Geofence
        </Button>
      </Box>

      {error && <Alert severity="error">{error}</Alert>}

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Search by Name"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Enter geofence name"
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              select
              fullWidth
              label="Filter by Type"
              value={filterType}
              onChange={(e) => setFilterType(e.target.value as GeofenceType | '')}
              SelectProps={{
                native: true,
              }}
            >
              <option value="">All Types</option>
              <option value="CHARGING_ZONE">Charging Zone</option>
              <option value="DEPOT">Depot</option>
              <option value="RESTRICTED">Restricted</option>
              <option value="CUSTOMER_LOCATION">Customer Location</option>
              <option value="SERVICE_AREA">Service Area</option>
              <option value="NO_GO_ZONE">No-Go Zone</option>
              <option value="PARKING_AREA">Parking Area</option>
            </TextField>
          </Grid>
        </Grid>
      </Paper>

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Total Geofences</Typography>
              <Typography variant="h5">{geofences.length}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Charging Zones</Typography>
              <Typography variant="h5">{geofences.filter((g: any) => g.type === 'CHARGING_ZONE').length}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Restricted Areas</Typography>
              <Typography variant="h5">{geofences.filter((g: any) => g.type === 'RESTRICTED' || g.type === 'NO_GO_ZONE').length}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Service Areas</Typography>
              <Typography variant="h5">{geofences.filter((g: any) => g.type === 'SERVICE_AREA').length}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Geofences Table */}
      <TableContainer component={Paper}>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
            <CircularProgress />
          </Box>
        ) : (
          <Table>
            <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Latitude</TableCell>
                <TableCell>Longitude</TableCell>
                <TableCell>Radius (m)</TableCell>
                <TableCell>Speed Limit</TableCell>
                <TableCell>Alerts</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredGeofences.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} align="center">
                    No geofences found
                  </TableCell>
                </TableRow>
              ) : (
                filteredGeofences.map((geofence: any) => (
                  <TableRow key={geofence.id}>
                    <TableCell>{geofence.name}</TableCell>
                    <TableCell>
                      <Chip
                        label={geofence.type}
                        color={getTypeColor(geofence.type)}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>{geofence.coordinates?.[0]?.latitude?.toFixed(4) || '-'}</TableCell>
                    <TableCell>{geofence.coordinates?.[0]?.longitude?.toFixed(4) || '-'}</TableCell>
                    <TableCell>{geofence.radius || '-'}</TableCell>
                    <TableCell>-</TableCell>
                    <TableCell>
                      -
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                        <Button
                          size="small"
                          variant="outlined"
                          onClick={() => handleEditGeofence(geofence)}
                          disabled={actionLoading}
                        >
                          Edit
                        </Button>
                        <Button
                          size="small"
                          variant="outlined"
                          color="info"
                          onClick={() => handleViewDetails(geofence)}
                        >
                          Details
                        </Button>
                        <Button
                          size="small"
                          variant="outlined"
                          color="error"
                          onClick={() => handleDeleteGeofence(geofence)}
                          disabled={actionLoading}
                        >
                          Delete
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

      {/* Geofence Form Dialog */}
      <Dialog open={formOpen} onClose={() => setFormOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{isEditing ? 'Edit Geofence' : 'Add New Geofence'}</DialogTitle>
        <DialogContent dividers>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
            <TextField
              fullWidth
              label="Geofence Name"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="e.g., Downtown Charging Hub"
            />
            <TextField
              select
              fullWidth
              label="Type"
              value={formData.type}
              onChange={(e) => setFormData({ ...formData, type: e.target.value as GeofenceType })}
              SelectProps={{
                native: true,
              }}
            >
              <option value="">Select Type</option>
              <option value="CHARGING_ZONE">Charging Zone</option>
              <option value="DEPOT">Depot</option>
              <option value="RESTRICTED">Restricted</option>
              <option value="CUSTOMER_LOCATION">Customer Location</option>
              <option value="SERVICE_AREA">Service Area</option>
              <option value="NO_GO_ZONE">No-Go Zone</option>
              <option value="PARKING_AREA">Parking Area</option>
            </TextField>
            <TextField
              fullWidth
              label="Latitude"
              type="number"
              value={formData.latitude}
              onChange={(e) => setFormData({ ...formData, latitude: parseFloat(e.target.value) })}
              inputProps={{ step: 0.0001 }}
            />
            <TextField
              fullWidth
              label="Longitude"
              type="number"
              value={formData.longitude}
              onChange={(e) => setFormData({ ...formData, longitude: parseFloat(e.target.value) })}
              inputProps={{ step: 0.0001 }}
            />
            <TextField
              fullWidth
              label="Radius (meters)"
              type="number"
              value={formData.radius}
              onChange={(e) => setFormData({ ...formData, radius: parseFloat(e.target.value) })}
            />
            <TextField
              fullWidth
              label="Speed Limit (km/h)"
              type="number"
              value={formData.speedLimit}
              onChange={(e) => setFormData({ ...formData, speedLimit: parseFloat(e.target.value) })}
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.alertOnEntry}
                  onChange={(e) => setFormData({ ...formData, alertOnEntry: e.target.checked })}
                />
              }
              label="Alert on Entry"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.alertOnExit}
                  onChange={(e) => setFormData({ ...formData, alertOnExit: e.target.checked })}
                />
              }
              label="Alert on Exit"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setFormOpen(false)}>Cancel</Button>
          <Button
            onClick={handleSubmit}
            variant="contained"
            color="primary"
            disabled={actionLoading}
          >
            {actionLoading ? <CircularProgress size={24} /> : isEditing ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Geofence Details Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Geofence Details</DialogTitle>
        <DialogContent dividers>
          {selectedGeofence && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography variant="body2">
                <strong>Name:</strong> {selectedGeofence.name}
              </Typography>
              <Typography variant="body2">
                <strong>Type:</strong> <Chip label={selectedGeofence.type} color={getTypeColor(selectedGeofence.type)} size="small" />
              </Typography>
              <Typography variant="body2">
                <strong>Coordinates:</strong> {selectedGeofence.coordinates?.[0]?.latitude?.toFixed(4)}, {selectedGeofence.coordinates?.[0]?.longitude?.toFixed(4)}
              </Typography>
              <Typography variant="body2">
                <strong>Radius:</strong> {selectedGeofence.radius} meters
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

export default GeofenceManagementPage;

import React, { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Grid,
  Card,
  CardContent,
  Chip,
  LinearProgress,
  IconButton,
  Menu,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  DialogContentText,
} from '@mui/material';
import {
  Add,
  Refresh,
  DirectionsCar,
  MoreVert as MoreVertIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
} from '@mui/icons-material';
import { useVehicles } from '../hooks/useVehicles';
import { useRealTimeLocation } from '../hooks/useRealTimeLocation';
import { useAppSelector } from '../redux/hooks';
import { selectUser } from '../redux/slices/authSlice';
import { RootState } from '../redux/store';
import { formatBatteryLevel, formatDistance } from '../utils/formatters';
import { getVehicleStatusColor, getBatteryStatusColor } from '../utils/helpers';
import VehicleFormDialog from '../components/vehicles/VehicleFormDialog';
import VehicleDetailDialog from '../components/vehicles/VehicleDetailDialog';
import { VehicleFormData, Vehicle } from '../types';
import { toast } from 'react-toastify';
import vehicleService from '../services/vehicleService';

const FleetManagementPage: React.FC = () => {
  const user = useAppSelector(selectUser);
  const isAuthenticated = useAppSelector((state: RootState) => state.auth.isAuthenticated);
  const { vehicles, loading, refetch } = useVehicles({ companyId: user?.companyId });

  // Dialog states
  const [addDialogOpen, setAddDialogOpen] = useState(false);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedVehicle, setSelectedVehicle] = useState<Vehicle | null>(null);
  const [submitting, setSubmitting] = useState(false);

  // Menu state
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [menuVehicle, setMenuVehicle] = useState<Vehicle | null>(null);

  useRealTimeLocation(undefined, user?.fleetId);

  // Handlers
  const handleCardClick = (vehicle: Vehicle) => {
    setSelectedVehicle(vehicle);
    setDetailDialogOpen(true);
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>, vehicle: Vehicle) => {
    event.stopPropagation();
    setAnchorEl(event.currentTarget);
    setMenuVehicle(vehicle);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setMenuVehicle(null);
  };

  const handleView = () => {
    if (menuVehicle) {
      setSelectedVehicle(menuVehicle);
      setDetailDialogOpen(true);
    }
    handleMenuClose();
  };

  const handleEdit = (vehicle?: Vehicle) => {
    const vehicleToEdit = vehicle || menuVehicle;
    if (vehicleToEdit) {
      setSelectedVehicle(vehicleToEdit);
      setEditDialogOpen(true);
      setDetailDialogOpen(false);
    }
    handleMenuClose();
  };

  const handleDeleteClick = (vehicle?: Vehicle) => {
    const vehicleToDelete = vehicle || menuVehicle;
    if (vehicleToDelete) {
      setSelectedVehicle(vehicleToDelete);
      setDeleteDialogOpen(true);
      setDetailDialogOpen(false);
    }
    handleMenuClose();
  };

  const handleAddVehicle = async (formData: VehicleFormData) => {
    try {
      setSubmitting(true);
      await vehicleService.createVehicle(formData);
      toast.success('Vehicle created successfully!');
      await refetch();
      setAddDialogOpen(false);
    } catch (error: any) {
      toast.error(error.message || 'Failed to create vehicle');
      throw error;
    } finally {
      setSubmitting(false);
    }
  };

  const handleUpdateVehicle = async (formData: VehicleFormData) => {
    if (!selectedVehicle?.id) return;

    try {
      setSubmitting(true);
      await vehicleService.updateVehicle(selectedVehicle.id, formData);
      toast.success('Vehicle updated successfully!');
      await refetch();
      setEditDialogOpen(false);
      setSelectedVehicle(null);
    } catch (error: any) {
      toast.error(error.message || 'Failed to update vehicle');
      throw error;
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!selectedVehicle?.id) return;

    try {
      setSubmitting(true);
      await vehicleService.deleteVehicle(selectedVehicle.id);
      toast.success('Vehicle deleted successfully!');
      await refetch();
      setDeleteDialogOpen(false);
      setSelectedVehicle(null);
    } catch (error: any) {
      toast.error(error.message || 'Failed to delete vehicle');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <LinearProgress />;
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" fontWeight={600} gutterBottom>
            Fleet Management
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Manage and monitor your vehicles - Click on any vehicle to view details
          </Typography>
        </Box>
        <Box display="flex" gap={2}>
          <Button variant="outlined" startIcon={<Refresh />} onClick={refetch} disabled={loading}>
            Refresh
          </Button>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => setAddDialogOpen(true)}
            disabled={loading || !isAuthenticated}
          >
            Add Vehicle
          </Button>
        </Box>
      </Box>

      <Grid container spacing={3}>
        {vehicles.map((vehicle) => {
          // Safely extract vehicle properties with defaults
          const batterySOC = vehicle?.battery?.stateOfCharge ?? 0;
          const batteryRange = vehicle?.battery?.range ?? 0;
          const vehicleStatus = vehicle?.status ?? 'INACTIVE';
          const make = vehicle?.make ?? 'Unknown';
          const model = vehicle?.model ?? 'Vehicle';
          const licensePlate = vehicle?.licensePlate ?? 'N/A';
          const odometer = vehicle?.odometer ?? 0;

          return (
            <Grid item xs={12} sm={6} md={4} lg={3} key={vehicle?.id || Math.random()}>
              <Card
                sx={{
                  height: '100%',
                  cursor: 'pointer',
                  '&:hover': { boxShadow: 6 },
                  transition: 'box-shadow 0.3s',
                  position: 'relative',
                }}
                onClick={() => handleCardClick(vehicle)}
              >
                <CardContent>
                  <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                    <Box>
                      <Typography variant="h6" fontWeight={600} gutterBottom>
                        {make} {model}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {licensePlate}
                      </Typography>
                    </Box>
                    <Box display="flex" alignItems="center" gap={0.5}>
                      <DirectionsCar sx={{ color: getVehicleStatusColor(vehicleStatus), fontSize: 32 }} />
                      <IconButton
                        size="small"
                        onClick={(e) => handleMenuOpen(e, vehicle)}
                        sx={{ ml: -1 }}
                      >
                        <MoreVertIcon fontSize="small" />
                      </IconButton>
                    </Box>
                  </Box>

                  <Chip
                    label={vehicleStatus}
                    size="small"
                    sx={{ mb: 2, bgcolor: getVehicleStatusColor(vehicleStatus), color: 'white' }}
                  />

                  <Box mb={1}>
                    <Box display="flex" justifyContent="space-between" mb={0.5}>
                      <Typography variant="caption" color="text.secondary">
                        Battery
                      </Typography>
                      <Typography variant="caption" fontWeight={600} sx={{ color: getBatteryStatusColor(batterySOC) }}>
                        {formatBatteryLevel(batterySOC)}
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={Math.min(Math.max(batterySOC, 0), 100)}
                      sx={{
                        height: 6,
                        borderRadius: 3,
                        bgcolor: 'grey.200',
                        '& .MuiLinearProgress-bar': {
                          bgcolor: getBatteryStatusColor(batterySOC),
                        },
                      }}
                    />
                  </Box>

                  <Box display="flex" justifyContent="space-between">
                    <Typography variant="caption" color="text.secondary">
                      Range: {formatDistance(batteryRange)}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Odometer: {formatDistance(odometer)}
                    </Typography>
                  </Box>

                  {vehicle?.assignedDriverName && (
                    <Typography variant="caption" color="text.secondary" mt={1} display="block">
                      Driver: {vehicle.assignedDriverName}
                    </Typography>
                  )}
                </CardContent>
              </Card>
            </Grid>
          );
        })}
      </Grid>

      {/* Action Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
        transformOrigin={{ horizontal: 'right', vertical: 'top' }}
        anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
      >
        <MenuItem onClick={handleView}>
          <ViewIcon fontSize="small" sx={{ mr: 1 }} />
          View Details
        </MenuItem>
        <MenuItem onClick={() => handleEdit()}>
          <EditIcon fontSize="small" sx={{ mr: 1 }} />
          Edit Vehicle
        </MenuItem>
        <MenuItem onClick={() => handleDeleteClick()} sx={{ color: 'error.main' }}>
          <DeleteIcon fontSize="small" sx={{ mr: 1 }} />
          Delete Vehicle
        </MenuItem>
      </Menu>

      {vehicles.length === 0 && (
        <Box textAlign="center" py={8}>
          <DirectionsCar sx={{ fontSize: 80, color: 'text.secondary', mb: 2 }} />
          <Typography variant="h6" color="text.secondary">
            No vehicles found
          </Typography>
          <Button
            variant="contained"
            startIcon={<Add />}
            sx={{ mt: 2 }}
            onClick={() => setAddDialogOpen(true)}
            disabled={!isAuthenticated}
          >
            Add Your First Vehicle
          </Button>
        </Box>
      )}

      {/* Add Vehicle Dialog */}
      {isAuthenticated && user?.companyId && (
        <VehicleFormDialog
          open={addDialogOpen}
          onClose={() => setAddDialogOpen(false)}
          onSubmit={handleAddVehicle}
          companyId={user.companyId}
          loading={submitting}
        />
      )}

      {/* Edit Vehicle Dialog */}
      {isAuthenticated && user?.companyId && selectedVehicle && (
        <VehicleFormDialog
          open={editDialogOpen}
          onClose={() => {
            setEditDialogOpen(false);
            setSelectedVehicle(null);
          }}
          onSubmit={handleUpdateVehicle}
          vehicle={selectedVehicle}
          companyId={user.companyId}
          loading={submitting}
        />
      )}

      {/* Vehicle Detail Dialog */}
      <VehicleDetailDialog
        open={detailDialogOpen}
        vehicle={selectedVehicle}
        onClose={() => {
          setDetailDialogOpen(false);
          setSelectedVehicle(null);
        }}
        onEdit={handleEdit}
        onDelete={handleDeleteClick}
      />

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        maxWidth="xs"
        fullWidth
      >
        <DialogTitle>Delete Vehicle</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete{' '}
            <strong>
              {selectedVehicle?.make} {selectedVehicle?.model} ({selectedVehicle?.licensePlate || selectedVehicle?.vin})
            </strong>
            ? This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)} disabled={submitting}>
            Cancel
          </Button>
          <Button onClick={handleDeleteConfirm} color="error" variant="contained" disabled={submitting}>
            {submitting ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default FleetManagementPage;

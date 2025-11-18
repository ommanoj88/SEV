import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Grid,
  Chip,
  Divider,
  IconButton,
  LinearProgress,
  Tabs,
  Tab,
  Alert,
  List,
  ListItem,
  ListItemText,
} from '@mui/material';
import {
  Close as CloseIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  LocalGasStation as FuelIcon,
  BatteryFull as BatteryIcon,
  Speed as SpeedIcon,
  CalendarToday as CalendarIcon,
  LocationOn as LocationIcon,
  DirectionsCar as CarIcon,
  Person as PersonIcon,
} from '@mui/icons-material';
import { Vehicle, FuelType, VehicleStatus } from '../../types';
import { formatBatteryLevel, formatDistance, formatDate } from '../../utils/formatters';
import { getVehicleStatusColor, getBatteryStatusColor } from '../../utils/helpers';

interface VehicleDetailDialogProps {
  open: boolean;
  vehicle: Vehicle | null;
  onClose: () => void;
  onEdit?: (vehicle: Vehicle) => void;
  onDelete?: (vehicle: Vehicle) => void;
}

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
  return (
    <div role="tabpanel" hidden={value !== index}>
      {value === index && <Box sx={{ pt: 2 }}>{children}</Box>}
    </div>
  );
};

const VehicleDetailDialog: React.FC<VehicleDetailDialogProps> = ({
  open,
  vehicle,
  onClose,
  onEdit,
  onDelete,
}) => {
  const [activeTab, setActiveTab] = useState(0);

  if (!vehicle) return null;

  const batterySOC = vehicle?.battery?.stateOfCharge ?? 0;
  const batteryRange = vehicle?.battery?.range ?? 0;
  const batteryHealth = vehicle?.battery?.stateOfHealth ?? 0;
  const batteryCapacity = vehicle?.battery?.capacity ?? 0;
  const isEV = vehicle.fuelType === FuelType.EV;
  const isICE = vehicle.fuelType === FuelType.ICE;
  const isHybrid = vehicle.fuelType === FuelType.HYBRID;
  const showBattery = isEV || isHybrid;
  const showFuel = isICE || isHybrid;

  const handleEdit = () => {
    onEdit?.(vehicle);
  };

  const handleDelete = () => {
    onDelete?.(vehicle);
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 2,
          maxHeight: '90vh',
        },
      }}
    >
      <DialogTitle
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          pb: 2,
          borderBottom: 1,
          borderColor: 'divider',
        }}
      >
        <Box display="flex" alignItems="center" gap={2}>
          <CarIcon sx={{ fontSize: 32, color: getVehicleStatusColor(vehicle.status) }} />
          <Box>
            <Typography variant="h6" fontWeight={600}>
              {vehicle.make} {vehicle.model}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {vehicle.licensePlate || vehicle.vin}
            </Typography>
          </Box>
        </Box>
        <Box display="flex" alignItems="center" gap={1}>
          {onEdit && (
            <IconButton onClick={handleEdit} color="primary" size="small">
              <EditIcon />
            </IconButton>
          )}
          {onDelete && (
            <IconButton onClick={handleDelete} color="error" size="small">
              <DeleteIcon />
            </IconButton>
          )}
          <IconButton onClick={onClose} size="small">
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>

      <DialogContent sx={{ px: 3, py: 2 }}>
        <Tabs
          value={activeTab}
          onChange={(_, newValue) => setActiveTab(newValue)}
          sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}
        >
          <Tab label="Overview" />
          <Tab label="Details" />
          {showBattery && <Tab label="Battery" />}
          {showFuel && <Tab label="Fuel" />}
        </Tabs>

        {/* Overview Tab */}
        <TabPanel value={activeTab} index={0}>
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <Box display="flex" gap={1} mb={2}>
                <Chip
                  label={vehicle.status}
                  size="small"
                  sx={{ bgcolor: getVehicleStatusColor(vehicle.status), color: 'white' }}
                />
                <Chip label={vehicle.fuelType} size="small" color="primary" variant="outlined" />
                <Chip label={vehicle.type} size="small" variant="outlined" />
              </Box>
            </Grid>

            {showBattery && (
              <Grid item xs={12}>
                <Box>
                  <Box display="flex" justifyContent="space-between" mb={1}>
                    <Typography variant="body2" color="text.secondary" display="flex" alignItems="center" gap={1}>
                      <BatteryIcon fontSize="small" />
                      Battery Level
                    </Typography>
                    <Typography variant="body1" fontWeight={600} sx={{ color: getBatteryStatusColor(batterySOC) }}>
                      {formatBatteryLevel(batterySOC)}
                    </Typography>
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={Math.min(Math.max(batterySOC, 0), 100)}
                    sx={{
                      height: 8,
                      borderRadius: 4,
                      bgcolor: 'grey.200',
                      '& .MuiLinearProgress-bar': {
                        bgcolor: getBatteryStatusColor(batterySOC),
                      },
                    }}
                  />
                  <Box display="flex" justifyContent="space-between" mt={1}>
                    <Typography variant="caption" color="text.secondary">
                      Range: {formatDistance(batteryRange)}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Health: {batteryHealth}%
                    </Typography>
                  </Box>
                </Box>
              </Grid>
            )}

            {showFuel && vehicle.fuelLevel !== undefined && vehicle.fuelTankCapacity && (
              <Grid item xs={12}>
                <Box>
                  <Box display="flex" justifyContent="space-between" mb={1}>
                    <Typography variant="body2" color="text.secondary" display="flex" alignItems="center" gap={1}>
                      <FuelIcon fontSize="small" />
                      Fuel Level
                    </Typography>
                    <Typography variant="body1" fontWeight={600}>
                      {vehicle.fuelLevel.toFixed(1)} L ({((vehicle.fuelLevel / vehicle.fuelTankCapacity) * 100).toFixed(0)}%)
                    </Typography>
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={Math.min(Math.max((vehicle.fuelLevel / vehicle.fuelTankCapacity) * 100, 0), 100)}
                    sx={{
                      height: 8,
                      borderRadius: 4,
                      bgcolor: 'grey.200',
                      '& .MuiLinearProgress-bar': {
                        bgcolor: (vehicle.fuelLevel / vehicle.fuelTankCapacity) * 100 < 20 ? 'error.main' : 'warning.main',
                      },
                    }}
                  />
                </Box>
              </Grid>
            )}

            <Grid item xs={12}>
              <Divider sx={{ my: 1 }} />
            </Grid>

            <Grid item xs={6}>
              <Typography variant="caption" color="text.secondary" display="flex" alignItems="center" gap={0.5}>
                <SpeedIcon fontSize="small" />
                Odometer
              </Typography>
              <Typography variant="body1" fontWeight={600}>
                {formatDistance(vehicle.odometer ?? 0)}
              </Typography>
            </Grid>

            <Grid item xs={6}>
              <Typography variant="caption" color="text.secondary" display="flex" alignItems="center" gap={0.5}>
                <PersonIcon fontSize="small" />
                Driver
              </Typography>
              <Typography variant="body1" fontWeight={600}>
                {vehicle.assignedDriverName || 'Unassigned'}
              </Typography>
            </Grid>

            {vehicle.location?.latitude && vehicle.location?.longitude && (
              <Grid item xs={12}>
                <Typography variant="caption" color="text.secondary" display="flex" alignItems="center" gap={0.5}>
                  <LocationIcon fontSize="small" />
                  Last Location
                </Typography>
                <Typography variant="body2">
                  {vehicle.location.latitude.toFixed(6)}, {vehicle.location.longitude.toFixed(6)}
                </Typography>
                {vehicle.location?.timestamp && (
                  <Typography variant="caption" color="text.secondary">
                    Updated: {formatDate(vehicle.location.timestamp)}
                  </Typography>
                )}
              </Grid>
            )}
          </Grid>
        </TabPanel>

        {/* Details Tab */}
        <TabPanel value={activeTab} index={1}>
          <List dense>
            <ListItem>
              <ListItemText
                primary="VIN"
                secondary={vehicle.vin || 'N/A'}
                primaryTypographyProps={{ variant: 'caption', color: 'text.secondary' }}
                secondaryTypographyProps={{ variant: 'body2', color: 'text.primary' }}
              />
            </ListItem>
            <ListItem>
              <ListItemText
                primary="License Plate"
                secondary={vehicle.licensePlate || 'N/A'}
                primaryTypographyProps={{ variant: 'caption', color: 'text.secondary' }}
                secondaryTypographyProps={{ variant: 'body2', color: 'text.primary' }}
              />
            </ListItem>
            <ListItem>
              <ListItemText
                primary="Make & Model"
                secondary={`${vehicle.make} ${vehicle.model} (${vehicle.year})`}
                primaryTypographyProps={{ variant: 'caption', color: 'text.secondary' }}
                secondaryTypographyProps={{ variant: 'body2', color: 'text.primary' }}
              />
            </ListItem>
            <ListItem>
              <ListItemText
                primary="Color"
                secondary={vehicle.color || 'N/A'}
                primaryTypographyProps={{ variant: 'caption', color: 'text.secondary' }}
                secondaryTypographyProps={{ variant: 'body2', color: 'text.primary' }}
              />
            </ListItem>
            <ListItem>
              <ListItemText
                primary="Type"
                secondary={vehicle.type}
                primaryTypographyProps={{ variant: 'caption', color: 'text.secondary' }}
                secondaryTypographyProps={{ variant: 'body2', color: 'text.primary' }}
              />
            </ListItem>
            <ListItem>
              <ListItemText
                primary="Fuel Type"
                secondary={vehicle.fuelType}
                primaryTypographyProps={{ variant: 'caption', color: 'text.secondary' }}
                secondaryTypographyProps={{ variant: 'body2', color: 'text.primary' }}
              />
            </ListItem>
            <ListItem>
              <ListItemText
                primary="Fleet ID"
                secondary={vehicle.fleetId || 'N/A'}
                primaryTypographyProps={{ variant: 'caption', color: 'text.secondary' }}
                secondaryTypographyProps={{ variant: 'body2', color: 'text.primary' }}
              />
            </ListItem>
          </List>
        </TabPanel>

        {/* Battery Tab */}
        {showBattery && (
          <TabPanel value={activeTab} index={2}>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Alert severity={batterySOC < 20 ? 'error' : batterySOC < 50 ? 'warning' : 'success'} icon={<BatteryIcon />}>
                  Battery is at {formatBatteryLevel(batterySOC)}
                  {batterySOC < 20 && ' - Charging required soon'}
                  {batterySOC >= 20 && batterySOC < 50 && ' - Consider charging'}
                </Alert>
              </Grid>

              <Grid item xs={6}>
                <Typography variant="caption" color="text.secondary">
                  State of Charge (SOC)
                </Typography>
                <Typography variant="h6" fontWeight={600} sx={{ color: getBatteryStatusColor(batterySOC) }}>
                  {formatBatteryLevel(batterySOC)}
                </Typography>
              </Grid>

              <Grid item xs={6}>
                <Typography variant="caption" color="text.secondary">
                  State of Health (SOH)
                </Typography>
                <Typography variant="h6" fontWeight={600}>
                  {batteryHealth}%
                </Typography>
              </Grid>

              <Grid item xs={6}>
                <Typography variant="caption" color="text.secondary">
                  Battery Capacity
                </Typography>
                <Typography variant="body1" fontWeight={600}>
                  {batteryCapacity} kWh
                </Typography>
              </Grid>

              <Grid item xs={6}>
                <Typography variant="caption" color="text.secondary">
                  Estimated Range
                </Typography>
                <Typography variant="body1" fontWeight={600}>
                  {formatDistance(batteryRange)}
                </Typography>
              </Grid>

              <Grid item xs={12}>
                <Divider sx={{ my: 1 }} />
                <Typography variant="caption" color="text.secondary">
                  Last Updated
                </Typography>
                <Typography variant="body2">
                  {vehicle.battery?.lastUpdated ? formatDate(vehicle.battery.lastUpdated) : 'N/A'}
                </Typography>
              </Grid>
            </Grid>
          </TabPanel>
        )}

        {/* Fuel Tab */}
        {showFuel && (
          <TabPanel value={activeTab} index={showBattery ? 3 : 2}>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                {vehicle.fuelLevel !== undefined && vehicle.fuelTankCapacity && (
                  <Alert
                    severity={
                      (vehicle.fuelLevel / vehicle.fuelTankCapacity) * 100 < 20
                        ? 'error'
                        : (vehicle.fuelLevel / vehicle.fuelTankCapacity) * 100 < 50
                        ? 'warning'
                        : 'success'
                    }
                    icon={<FuelIcon />}
                  >
                    Fuel is at {((vehicle.fuelLevel / vehicle.fuelTankCapacity) * 100).toFixed(0)}% ({vehicle.fuelLevel.toFixed(1)} L)
                    {(vehicle.fuelLevel / vehicle.fuelTankCapacity) * 100 < 20 && ' - Refueling required soon'}
                    {(vehicle.fuelLevel / vehicle.fuelTankCapacity) * 100 >= 20 && (vehicle.fuelLevel / vehicle.fuelTankCapacity) * 100 < 50 && ' - Consider refueling'}
                  </Alert>
                )}
              </Grid>

              <Grid item xs={6}>
                <Typography variant="caption" color="text.secondary">
                  Fuel Level
                </Typography>
                <Typography variant="h6" fontWeight={600}>
                  {vehicle.fuelLevel?.toFixed(1) ?? 0} L
                </Typography>
              </Grid>

              <Grid item xs={6}>
                <Typography variant="caption" color="text.secondary">
                  Fuel Type
                </Typography>
                <Typography variant="h6" fontWeight={600}>
                  {vehicle.fuelType}
                </Typography>
              </Grid>

              {vehicle.fuelTankCapacity && vehicle.fuelLevel !== undefined && (
                <>
                  <Grid item xs={6}>
                    <Typography variant="caption" color="text.secondary">
                      Tank Capacity
                    </Typography>
                    <Typography variant="body1" fontWeight={600}>
                      {vehicle.fuelTankCapacity} L
                    </Typography>
                  </Grid>

                  <Grid item xs={6}>
                    <Typography variant="caption" color="text.secondary">
                      Fuel Percentage
                    </Typography>
                    <Typography variant="body1" fontWeight={600}>
                      {((vehicle.fuelLevel / vehicle.fuelTankCapacity) * 100).toFixed(0)}%
                    </Typography>
                  </Grid>
                </>
              )}

              <Grid item xs={12}>
                <Divider sx={{ my: 1 }} />
                <Typography variant="caption" color="text.secondary">
                  Last Updated
                </Typography>
                <Typography variant="body2">
                  {vehicle.updatedAt ? formatDate(vehicle.updatedAt) : 'N/A'}
                </Typography>
              </Grid>
            </Grid>
          </TabPanel>
        )}
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2, borderTop: 1, borderColor: 'divider' }}>
        {onDelete && (
          <Button onClick={handleDelete} color="error" startIcon={<DeleteIcon />}>
            Delete
          </Button>
        )}
        {onEdit && (
          <Button onClick={handleEdit} color="primary" variant="outlined" startIcon={<EditIcon />}>
            Edit
          </Button>
        )}
        <Button onClick={onClose} variant="contained">
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default VehicleDetailDialog;

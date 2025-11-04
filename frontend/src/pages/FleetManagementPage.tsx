import React, { useState } from 'react';
import { Box, Typography, Button, Grid, Card, CardContent, Chip, LinearProgress } from '@mui/material';
import { Add, Refresh, DirectionsCar } from '@mui/icons-material';
import { useVehicles } from '../hooks/useVehicles';
import { useRealTimeLocation } from '../hooks/useRealTimeLocation';
import { useAppSelector } from '../redux/hooks';
import { selectUser } from '../redux/slices/authSlice';
import { formatBatteryLevel, formatDistance } from '../utils/formatters';
import { getVehicleStatusColor, getBatteryStatusColor } from '../utils/helpers';
import VehicleFormDialog from '../components/vehicles/VehicleFormDialog';
import { VehicleFormData } from '../types';
import { toast } from 'react-toastify';

const FleetManagementPage: React.FC = () => {
  const user = useAppSelector(selectUser);
  const { vehicles, loading, refetch } = useVehicles();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  useRealTimeLocation(undefined, user?.fleetId);

  const handleAddVehicle = async (formData: VehicleFormData) => {
    try {
      setSubmitting(true);
      // TODO: Implement API call to create vehicle
      // const response = await vehicleService.createVehicle(formData);
      console.log('Vehicle data to be submitted:', formData);
      toast.success('Vehicle created successfully!');
      await refetch();
    } catch (error: any) {
      toast.error(error.message || 'Failed to create vehicle');
      throw error;
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
            Manage and monitor your electric vehicles
          </Typography>
        </Box>
        <Box display="flex" gap={2}>
          <Button variant="outlined" startIcon={<Refresh />} onClick={refetch} disabled={loading}>
            Refresh
          </Button>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => setDialogOpen(true)}
            disabled={loading || !user?.id}
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
              <Card sx={{ height: '100%', cursor: 'pointer', '&:hover': { boxShadow: 6 } }}>
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
                    <DirectionsCar sx={{ color: getVehicleStatusColor(vehicleStatus), fontSize: 32 }} />
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
            onClick={() => setDialogOpen(true)}
            disabled={!user?.id}
          >
            Add Your First Vehicle
          </Button>
        </Box>
      )}

      {user && (
        <VehicleFormDialog
          open={dialogOpen}
          onClose={() => setDialogOpen(false)}
          onSubmit={handleAddVehicle}
          companyId={1}
          loading={submitting}
        />
      )}
    </Box>
  );
};

export default FleetManagementPage;

import React, { useEffect, useState } from 'react';
import { Paper, Typography, Grid, TextField, Button, MenuItem } from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { useAppDispatch, useAppSelector } from '../../redux/hooks';
import { scheduleMaintenance } from '../../redux/slices/maintenanceSlice';
import { toast } from 'react-toastify';
import axios from 'axios';

const ScheduleMaintenance: React.FC = () => {
  const dispatch = useAppDispatch();
  const { vehicles } = useAppSelector((state) => state.vehicles);
  const { control, handleSubmit, watch } = useForm();
  const [maintenanceTypes, setMaintenanceTypes] = useState<string[]>([]);
  const [allMaintenanceTypes, setAllMaintenanceTypes] = useState<string[]>([]);
  
  const selectedVehicleId = watch('vehicleId');

  // Fetch all maintenance types on component mount
  useEffect(() => {
    const fetchAllTypes = async () => {
      try {
        const response = await axios.get('/api/v1/maintenance/types');
        if (response.data.success) {
          setAllMaintenanceTypes(response.data.data);
          setMaintenanceTypes(response.data.data);
        }
      } catch (error) {
        console.error('Failed to fetch maintenance types:', error);
        // Fallback to hardcoded types
        const fallbackTypes = [
          'ROUTINE_SERVICE', 'TIRE_REPLACEMENT', 'BRAKE_SERVICE', 'EMERGENCY_REPAIR',
          'OIL_CHANGE', 'FILTER_REPLACEMENT', 'EMISSION_TEST', 'COOLANT_FLUSH',
          'TRANSMISSION_SERVICE', 'ENGINE_DIAGNOSTICS', 'BATTERY_CHECK',
          'HV_SYSTEM_CHECK', 'FIRMWARE_UPDATE', 'CHARGING_PORT_INSPECTION',
          'THERMAL_MANAGEMENT_CHECK', 'HYBRID_SYSTEM_CHECK'
        ];
        setAllMaintenanceTypes(fallbackTypes);
        setMaintenanceTypes(fallbackTypes);
      }
    };
    fetchAllTypes();
  }, []);

  // Fetch vehicle-specific maintenance types when vehicle is selected
  useEffect(() => {
    if (selectedVehicleId) {
      const fetchVehicleTypes = async () => {
        try {
          const response = await axios.get(`/api/v1/maintenance/types/vehicle/${selectedVehicleId}`);
          if (response.data.success) {
            setMaintenanceTypes(response.data.data);
          }
        } catch (error) {
          console.error('Failed to fetch vehicle-specific types:', error);
          // Fall back to all types if fetch fails
          setMaintenanceTypes(allMaintenanceTypes);
        }
      };
      fetchVehicleTypes();
    } else {
      // Reset to all types if no vehicle selected
      setMaintenanceTypes(allMaintenanceTypes);
    }
  }, [selectedVehicleId, allMaintenanceTypes]);

  const onSubmit = async (data: any) => {
    try {
      await dispatch(scheduleMaintenance(data)).unwrap();
      toast.success('Maintenance scheduled successfully!');
    } catch (error: any) {
      toast.error(error.message || 'Failed to schedule maintenance');
    }
  };

  const formatMaintenanceType = (type: string) => {
    return type.split('_').map(word => 
      word.charAt(0) + word.slice(1).toLowerCase()
    ).join(' ');
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h6" gutterBottom>Schedule Maintenance</Typography>
      <form onSubmit={handleSubmit(onSubmit)}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={6}>
            <Controller name="vehicleId" control={control} defaultValue="" render={({ field }) => (
              <TextField {...field} select label="Vehicle" fullWidth required>
                {vehicles.map((v) => <MenuItem key={v.id} value={v.id}>{v.make} {v.model} - {v.licensePlate}</MenuItem>)}
              </TextField>
            )} />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller name="type" control={control} defaultValue="" render={({ field }) => (
              <TextField {...field} select label="Service Type" fullWidth required>
                {maintenanceTypes.map((type) => (
                  <MenuItem key={type} value={type}>
                    {formatMaintenanceType(type)}
                  </MenuItem>
                ))}
              </TextField>
            )} />
          </Grid>
          <Grid item xs={12}>
            <Controller name="title" control={control} defaultValue="" render={({ field }) => (
              <TextField {...field} label="Title" fullWidth required />
            )} />
          </Grid>
          <Grid item xs={12}>
            <Controller name="description" control={control} defaultValue="" render={({ field }) => (
              <TextField {...field} label="Description" fullWidth multiline rows={3} required />
            )} />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller name="scheduledDate" control={control} defaultValue="" render={({ field }) => (
              <TextField {...field} label="Scheduled Date" type="date" fullWidth required InputLabelProps={{ shrink: true }} />
            )} />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller name="serviceProvider" control={control} defaultValue="" render={({ field }) => (
              <TextField {...field} label="Service Provider" fullWidth />
            )} />
          </Grid>
          <Grid item xs={12}>
            <Button type="submit" variant="contained" fullWidth>Schedule Maintenance</Button>
          </Grid>
        </Grid>
      </form>
    </Paper>
  );
};

export default ScheduleMaintenance;

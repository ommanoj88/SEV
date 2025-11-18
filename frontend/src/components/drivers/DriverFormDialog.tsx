import React, { useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Grid,
  MenuItem,
  IconButton,
  Box,
  Typography,
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { Driver, DriverStatus } from '../../types';

interface DriverFormDialogProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: DriverFormData) => Promise<void>;
  driver?: Driver | null;
  companyId: number;
  loading?: boolean;
}

export interface DriverFormData {
  name: string;
  phone: string;
  email: string;
  licenseNumber: string;
  licenseExpiry: string;
  status?: string;
}

const DriverFormDialog: React.FC<DriverFormDialogProps> = ({
  open,
  onClose,
  onSubmit,
  driver,
  companyId,
  loading = false,
}) => {
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<DriverFormData>({
    defaultValues: {
      name: '',
      phone: '',
      email: '',
      licenseNumber: '',
      licenseExpiry: '',
      status: DriverStatus.ACTIVE,
    },
  });

  useEffect(() => {
    if (driver) {
      // Combine firstName and lastName into single name for backend
      const fullName = `${driver.firstName || ''} ${driver.lastName || ''}`.trim();
      reset({
        name: fullName,
        phone: driver.phone,
        email: driver.email,
        licenseNumber: driver.licenseNumber,
        licenseExpiry: driver.licenseExpiry,
        status: driver.status,
      });
    } else {
      reset({
        name: '',
        phone: '',
        email: '',
        licenseNumber: '',
        licenseExpiry: '',
        status: DriverStatus.ACTIVE,
      });
    }
  }, [driver, reset]);

  const handleFormSubmit = async (data: DriverFormData) => {
    try {
      await onSubmit(data);
      reset();
    } catch (error) {
      // Error is handled by parent component
      console.error('Form submission error:', error);
    }
  };

  const handleClose = () => {
    reset();
    onClose();
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 2,
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
        <Typography variant="h6" fontWeight={600}>
          {driver ? 'Edit Driver' : 'Add New Driver'}
        </Typography>
        <IconButton onClick={handleClose} size="small" disabled={loading}>
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <form onSubmit={handleSubmit(handleFormSubmit)}>
        <DialogContent sx={{ px: 3, py: 3 }}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Controller
                name="name"
                control={control}
                rules={{ required: 'Driver name is required' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Full Name"
                    fullWidth
                    required
                    error={!!errors.name}
                    helperText={errors.name?.message}
                    disabled={loading}
                    placeholder="Enter driver's full name"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="phone"
                control={control}
                rules={{
                  required: 'Phone number is required',
                  pattern: {
                    value: /^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,9}$/,
                    message: 'Invalid phone number format',
                  },
                }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Phone Number"
                    fullWidth
                    required
                    error={!!errors.phone}
                    helperText={errors.phone?.message}
                    disabled={loading}
                    placeholder="+91-9999-9999"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="email"
                control={control}
                rules={{
                  pattern: {
                    value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                    message: 'Invalid email address',
                  },
                }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Email"
                    type="email"
                    fullWidth
                    error={!!errors.email}
                    helperText={errors.email?.message}
                    disabled={loading}
                    placeholder="driver@example.com"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="licenseNumber"
                control={control}
                rules={{ required: 'License number is required' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="License Number"
                    fullWidth
                    required
                    error={!!errors.licenseNumber}
                    helperText={errors.licenseNumber?.message}
                    disabled={loading}
                    placeholder="DL-123456"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="licenseExpiry"
                control={control}
                rules={{ required: 'License expiry date is required' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="License Expiry Date"
                    type="date"
                    fullWidth
                    required
                    error={!!errors.licenseExpiry}
                    helperText={errors.licenseExpiry?.message}
                    disabled={loading}
                    InputLabelProps={{
                      shrink: true,
                    }}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Controller
                name="status"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    select
                    label="Status"
                    fullWidth
                    disabled={loading}
                  >
                    <MenuItem value={DriverStatus.ACTIVE}>Active</MenuItem>
                    <MenuItem value={DriverStatus.INACTIVE}>Inactive</MenuItem>
                    <MenuItem value={DriverStatus.ON_LEAVE}>On Leave</MenuItem>
                    <MenuItem value={DriverStatus.SUSPENDED}>Suspended</MenuItem>
                  </TextField>
                )}
              />
            </Grid>
          </Grid>
        </DialogContent>

        <DialogActions sx={{ px: 3, py: 2, borderTop: 1, borderColor: 'divider' }}>
          <Button onClick={handleClose} disabled={loading}>
            Cancel
          </Button>
          <Button type="submit" variant="contained" disabled={loading}>
            {loading ? 'Saving...' : driver ? 'Update Driver' : 'Add Driver'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default DriverFormDialog;

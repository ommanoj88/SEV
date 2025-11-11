import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  List,
  ListItem,
  ListItemText,
  Divider,
  Chip,
  CircularProgress,
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';

interface VehicleDetailsModalProps {
  open: boolean;
  onClose: () => void;
  title: string;
  vehicles: any[];
  loading?: boolean;
}

const VehicleDetailsModal: React.FC<VehicleDetailsModalProps> = ({
  open,
  onClose,
  title,
  vehicles,
  loading = false,
}) => {
  return (
    <Dialog 
      open={open} 
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 2,
          boxShadow: 24,
        }
      }}
    >
      <DialogTitle sx={{ 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'space-between',
        pb: 2,
        borderBottom: 1,
        borderColor: 'divider',
      }}>
        <Typography variant="h6" fontWeight={600}>
          {title}
        </Typography>
        <Button
          onClick={onClose}
          sx={{ minWidth: 'auto', p: 0.5 }}
          color="inherit"
        >
          <CloseIcon />
        </Button>
      </DialogTitle>

      <DialogContent sx={{ mt: 2 }}>
        {loading ? (
          <Box display="flex" justifyContent="center" alignItems="center" py={4}>
            <CircularProgress />
          </Box>
        ) : vehicles.length === 0 ? (
          <Box py={4} textAlign="center">
            <Typography color="text.secondary">
              No vehicles found in this category
            </Typography>
          </Box>
        ) : (
          <List>
            {vehicles.map((vehicle, index) => (
              <React.Fragment key={vehicle.id || index}>
                <ListItem
                  sx={{
                    py: 2,
                    px: 0,
                    '&:hover': {
                      backgroundColor: 'action.hover',
                      borderRadius: 1,
                      px: 2,
                    },
                    transition: 'all 0.2s',
                  }}
                >
                  <ListItemText
                    primary={
                      <Box display="flex" alignItems="center" gap={2}>
                        <Typography variant="subtitle1" fontWeight={600}>
                          {vehicle.vehicleNumber || `Vehicle ${vehicle.id}`}
                        </Typography>
                        <Chip
                          label={vehicle.status || 'Unknown'}
                          size="small"
                          color="primary"
                          variant="outlined"
                        />
                      </Box>
                    }
                    secondary={
                      <Box mt={1}>
                        <Typography variant="body2" color="text.secondary">
                          {vehicle.make && vehicle.model 
                            ? `${vehicle.make} ${vehicle.model}` 
                            : 'Vehicle details'}
                        </Typography>
                        {vehicle.currentBatterySoc !== undefined && (
                          <Typography variant="caption" color="text.secondary">
                            Battery: {Math.round(vehicle.currentBatterySoc)}%
                          </Typography>
                        )}
                        {vehicle.fuelLevel !== undefined && (
                          <Typography variant="caption" color="text.secondary" sx={{ ml: 2 }}>
                            Fuel: {Math.round(vehicle.fuelLevel)}%
                          </Typography>
                        )}
                      </Box>
                    }
                  />
                </ListItem>
                {index < vehicles.length - 1 && <Divider />}
              </React.Fragment>
            ))}
          </List>
        )}
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2, borderTop: 1, borderColor: 'divider' }}>
        <Button onClick={onClose} variant="contained">
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default VehicleDetailsModal;

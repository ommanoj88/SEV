import React, { useEffect, useState } from 'react';
import {
  Card,
  CardContent,
  Typography,
  Box,
  CircularProgress,
  Alert,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Chip,
  Button,
} from '@mui/material';
import {
  Warning,
  Build,
  BatteryAlert,
  Schedule,
  ElectricCar,
  LocalGasStation,
  AllInclusive,
} from '@mui/icons-material';
import maintenanceService from '@services/maintenanceService';

interface MaintenanceAlert {
  id: string;
  vehicleId: number;
  vehicleNumber: string;
  fuelType: 'EV' | 'ICE' | 'HYBRID';
  maintenanceType: string;
  dueDate: string;
  status: string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
  description?: string;
}

interface MaintenanceAlertsCardProps {
  companyId?: number;
}

const MaintenanceAlertsCard: React.FC<MaintenanceAlertsCardProps> = ({ companyId = 1 }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [alerts, setAlerts] = useState<MaintenanceAlert[]>([]);

  useEffect(() => {
    loadMaintenanceAlerts();
  }, [companyId]);

  const loadMaintenanceAlerts = async () => {
    try {
      setLoading(true);
      setError(null);

      // Use the new optimized backend endpoint
      const alertsData = await maintenanceService.getMaintenanceAlerts(companyId, 30);
      
      // Map the backend response to our frontend format
      const formattedAlerts: MaintenanceAlert[] = alertsData.map((alert: any) => ({
        id: alert.id.toString(),
        vehicleId: alert.vehicleId,
        vehicleNumber: alert.vehicleNumber,
        fuelType: alert.fuelType || 'EV',
        maintenanceType: alert.maintenanceType,
        dueDate: alert.scheduledDate,
        status: alert.status,
        priority: alert.priority,
        description: alert.description,
      })).slice(0, 5); // Show top 5 alerts
      
      setAlerts(formattedAlerts);
    } catch (err: any) {
      console.error('Failed to load maintenance alerts:', err);
      setError(err.message || 'Failed to load maintenance alerts');
    } finally {
      setLoading(false);
    }
  };

  const getFuelTypeIcon = (fuelType: string) => {
    switch (fuelType) {
      case 'EV':
        return <ElectricCar sx={{ fontSize: 18, color: '#4caf50' }} />;
      case 'ICE':
        return <LocalGasStation sx={{ fontSize: 18, color: '#ff9800' }} />;
      case 'HYBRID':
        return <AllInclusive sx={{ fontSize: 18, color: '#2196f3' }} />;
      default:
        return <ElectricCar sx={{ fontSize: 18 }} />;
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'HIGH':
        return 'error';
      case 'MEDIUM':
        return 'warning';
      case 'LOW':
        return 'info';
      default:
        return 'default';
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffTime = date.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays < 0) {
      return `${Math.abs(diffDays)} days overdue`;
    } else if (diffDays === 0) {
      return 'Due today';
    } else if (diffDays === 1) {
      return 'Due tomorrow';
    } else {
      return `Due in ${diffDays} days`;
    }
  };

  const formatMaintenanceType = (type: string) => {
    return type
      .split('_')
      .map(word => word.charAt(0) + word.slice(1).toLowerCase())
      .join(' ');
  };

  if (loading) {
    return (
      <Card sx={{ height: '100%' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Maintenance Alerts
          </Typography>
          <Box display="flex" justifyContent="center" alignItems="center" minHeight={200}>
            <CircularProgress />
          </Box>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card sx={{ height: '100%' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Maintenance Alerts
          </Typography>
          <Alert severity="error">{error}</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
          <Typography variant="h6">
            Maintenance Alerts
          </Typography>
          {alerts.length > 0 && (
            <Chip 
              label={`${alerts.length} alerts`} 
              color={alerts.some(a => a.priority === 'HIGH') ? 'error' : 'default'}
              size="small"
            />
          )}
        </Box>

        {alerts.length === 0 ? (
          <Alert severity="success">
            No upcoming maintenance alerts. All vehicles are up to date!
          </Alert>
        ) : (
          <>
            <List dense>
              {alerts.map((alert) => (
                <ListItem
                  key={alert.id}
                  sx={{
                    border: 1,
                    borderColor: 'divider',
                    borderRadius: 1,
                    mb: 1,
                    backgroundColor: alert.status === 'OVERDUE' ? 'error.light' : 'background.paper',
                    '&:hover': {
                      backgroundColor: 'action.hover',
                    },
                  }}
                >
                  <ListItemIcon>
                    {alert.status === 'OVERDUE' ? (
                      <Warning color="error" />
                    ) : (
                      <Build color="action" />
                    )}
                  </ListItemIcon>
                  <ListItemText
                    primary={
                      <Box display="flex" alignItems="center" gap={1}>
                        <Typography variant="body2" fontWeight="medium">
                          {alert.vehicleNumber}
                        </Typography>
                        {getFuelTypeIcon(alert.fuelType)}
                      </Box>
                    }
                    secondary={
                      <Box>
                        <Typography variant="caption" display="block">
                          {formatMaintenanceType(alert.maintenanceType)}
                        </Typography>
                        <Box display="flex" alignItems="center" gap={1} mt={0.5}>
                          <Schedule sx={{ fontSize: 14 }} />
                          <Typography variant="caption" color={alert.status === 'OVERDUE' ? 'error' : 'text.secondary'}>
                            {formatDate(alert.dueDate)}
                          </Typography>
                          <Chip 
                            label={alert.priority} 
                            size="small" 
                            color={getPriorityColor(alert.priority) as any}
                            sx={{ height: 18, fontSize: 10 }}
                          />
                        </Box>
                      </Box>
                    }
                  />
                </ListItem>
              ))}
            </List>

            <Box mt={2} display="flex" justifyContent="center">
              <Button size="small" variant="outlined">
                View All Maintenance
              </Button>
            </Box>
          </>
        )}
      </CardContent>
    </Card>
  );
};

export default MaintenanceAlertsCard;

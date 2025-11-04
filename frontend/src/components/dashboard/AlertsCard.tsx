import React from 'react';
import {
  Paper,
  Typography,
  Box,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Chip,
  Button,
} from '@mui/material';
import {
  Warning as WarningIcon,
  Error as ErrorIcon,
  Info as InfoIcon,
  ArrowForward as ArrowForwardIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '@redux/hooks';
import { AlertSeverity } from '../../types/common';
import { formatTimeAgo } from '@utils/formatters';

const AlertsCard: React.FC = () => {
  const navigate = useNavigate();
  const { alerts } = useAppSelector((state) => state.notifications);

  const recentAlerts = alerts.slice(0, 5);

  const getSeverityIcon = (severity: AlertSeverity) => {
    switch (severity) {
      case AlertSeverity.CRITICAL:
      case AlertSeverity.HIGH:
        return <ErrorIcon color="error" />;
      case AlertSeverity.MEDIUM:
        return <WarningIcon color="warning" />;
      default:
        return <InfoIcon color="info" />;
    }
  };

  const getSeverityColor = (severity: AlertSeverity): 'error' | 'warning' | 'info' | 'default' => {
    switch (severity) {
      case AlertSeverity.CRITICAL:
      case AlertSeverity.HIGH:
        return 'error';
      case AlertSeverity.MEDIUM:
        return 'warning';
      default:
        return 'info';
    }
  };

  return (
    <Paper sx={{ p: 3, height: '100%' }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h6">
          Recent Alerts
        </Typography>
        <Button
          size="small"
          endIcon={<ArrowForwardIcon />}
          onClick={() => navigate('/alerts')}
        >
          View All
        </Button>
      </Box>

      {recentAlerts.length === 0 ? (
        <Box
          display="flex"
          flexDirection="column"
          alignItems="center"
          justifyContent="center"
          py={4}
        >
          <InfoIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
          <Typography variant="body2" color="text.secondary">
            No recent alerts
          </Typography>
        </Box>
      ) : (
        <List>
          {recentAlerts.map((alert) => (
            <ListItem
              key={alert.id}
              sx={{
                borderLeft: 3,
                borderColor: `${getSeverityColor(alert.severity as AlertSeverity)}.main`,
                mb: 1,
                bgcolor: 'background.default',
                borderRadius: 1,
              }}
            >
              <ListItemIcon>
                {getSeverityIcon(alert.severity as AlertSeverity)}
              </ListItemIcon>
              <ListItemText
                primary={
                  <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>
                      {alert.title}
                    </Typography>
                    <Chip
                      label={alert.severity}
                      size="small"
                      color={getSeverityColor(alert.severity as AlertSeverity)}
                    />
                  </Box>
                }
                secondary={
                  <Box>
                    <Typography variant="caption" display="block" color="text.secondary">
                      {alert.message}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {formatTimeAgo(alert.createdAt)}
                    </Typography>
                  </Box>
                }
              />
            </ListItem>
          ))}
        </List>
      )}
    </Paper>
  );
};

export default AlertsCard;

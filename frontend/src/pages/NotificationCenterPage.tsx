import React, { useEffect, useState } from 'react';
import {
  Container,
  Paper,
  Button,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
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
  Tabs,
  Tab,
  Badge,
} from '@mui/material';
import { Notification, Alert as AlertType } from '../types';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`notification-tabpanel-${index}`}
      aria-labelledby={`notification-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 2 }}>{children}</Box>}
    </div>
  );
}

const NotificationCenterPage: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [alerts, setAlerts] = useState<AlertType[]>([]);
  const [selectedNotification, setSelectedNotification] = useState<Notification | null>(null);
  const [selectedAlert, setSelectedAlert] = useState<AlertType | null>(null);
  const [notificationDialogOpen, setNotificationDialogOpen] = useState(false);
  const [alertDialogOpen, setAlertDialogOpen] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Simulate loading notifications and alerts
    setLoading(true);
    setTimeout(() => {
      setNotifications([
        {
          id: '1',
          fleetId: 'fleet-1',
          type: 'SUCCESS' as any,
          category: 'TRIP' as any,
          title: 'Trip Completed',
          message: 'Vehicle EV-001 completed trip successfully',
          isRead: true,
          createdAt: new Date(Date.now() - 3600000).toISOString(),
        },
        {
          id: '2',
          fleetId: 'fleet-1',
          type: 'WARNING' as any,
          category: 'VEHICLE' as any,
          title: 'Low Battery',
          message: 'Vehicle EV-002 battery is below 20%',
          isRead: false,
          createdAt: new Date(Date.now() - 1800000).toISOString(),
        },
      ]);
      setAlerts([
        {
          id: '1',
          fleetId: 'fleet-1',
          type: 'WARNING' as any,
          category: 'MAINTENANCE' as any,
          title: 'Maintenance Due',
          message: 'Vehicle EV-001 is due for maintenance',
          isRead: false,
          severity: 'MEDIUM',
          resolved: false,
          createdAt: new Date(Date.now() - 7200000).toISOString(),
        },
        {
          id: '2',
          fleetId: 'fleet-1',
          type: 'CRITICAL' as any,
          category: 'VEHICLE' as any,
          title: 'Zone Violation',
          message: 'Vehicle EV-003 entered restricted zone',
          isRead: false,
          severity: 'CRITICAL',
          resolved: false,
          createdAt: new Date(Date.now() - 600000).toISOString(),
        },
      ]);
      setLoading(false);
    }, 500);
  }, []);

  const getSeverityColor = (severity: string) => {
    const colors: Record<string, any> = {
      LOW: 'info',
      MEDIUM: 'warning',
      HIGH: 'error',
      CRITICAL: 'error',
    };
    return colors[severity] || 'default';
  };

  const getTypeColor = (type: string) => {
    const colors: Record<string, any> = {
      INFO: 'info',
      WARNING: 'warning',
      CRITICAL: 'error',
      SUCCESS: 'success',
    };
    return colors[type] || 'default';
  };

  const unreadCount = notifications.filter((n) => !n.isRead).length;
  const criticalAlerts = alerts.filter((a) => a.severity === 'CRITICAL').length;

  const handleViewNotification = (notification: Notification) => {
    setSelectedNotification(notification);
    setNotificationDialogOpen(true);
  };

  const handleViewAlert = (alert: AlertType) => {
    setSelectedAlert(alert);
    setAlertDialogOpen(true);
  };

  return (
    <Container maxWidth="lg" sx={{ py: 3 }}>
      <Typography variant="h4" sx={{ mb: 3, fontWeight: 'bold' }}>
        Notification Center
      </Typography>

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Total Notifications</Typography>
              <Typography variant="h5">{notifications.length}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Badge badgeContent={unreadCount} color="error">
                <Typography color="textSecondary">Unread</Typography>
              </Badge>
              <Typography variant="h5">{unreadCount}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Total Alerts</Typography>
              <Typography variant="h5">{alerts.length}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Badge badgeContent={criticalAlerts} color="error">
                <Typography color="textSecondary">Critical</Typography>
              </Badge>
              <Typography variant="h5">{criticalAlerts}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Tabs */}
      <Paper sx={{ mb: 3 }}>
        <Tabs
          value={tabValue}
          onChange={(e, newValue) => setTabValue(newValue)}
          aria-label="notification tabs"
        >
          <Tab
            label={`Notifications (${unreadCount})`}
            id="notification-tab-0"
            aria-controls="notification-tabpanel-0"
          />
          <Tab
            label={`Alerts (${criticalAlerts})`}
            id="notification-tab-1"
            aria-controls="notification-tabpanel-1"
          />
        </Tabs>

        {/* Notifications Tab */}
        <TabPanel value={tabValue} index={0}>
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
              <CircularProgress />
            </Box>
          ) : (
            <List>
              {notifications.length === 0 ? (
                <Typography align="center" sx={{ p: 3 }}>
                  No notifications
                </Typography>
              ) : (
                notifications.map((notification) => (
                  <ListItem
                    key={notification.id}
                    secondaryAction={
                      <Chip
                        label={notification.type}
                        color={getTypeColor(notification.type as string)}
                        size="small"
                      />
                    }
                    sx={{
                      backgroundColor: !notification.isRead ? 'action.hover' : 'transparent',
                      mb: 1,
                      borderRadius: 1,
                    }}
                  >
                    <ListItemButton
                      onClick={() => handleViewNotification(notification)}
                      sx={{ flex: 1 }}
                    >
                      <ListItemText
                        primary={
                          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                            <Typography variant="subtitle1">{notification.title}</Typography>
                            {!notification.isRead && (
                              <Box
                                sx={{
                                  width: 8,
                                  height: 8,
                                  borderRadius: '50%',
                                  backgroundColor: 'primary.main',
                                }}
                              />
                            )}
                          </Box>
                        }
                        secondary={notification.message}
                      />
                    </ListItemButton>
                  </ListItem>
                ))
              )}
            </List>
          )}
        </TabPanel>

        {/* Alerts Tab */}
        <TabPanel value={tabValue} index={1}>
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
              <CircularProgress />
            </Box>
          ) : (
            <List>
              {alerts.length === 0 ? (
                <Typography align="center" sx={{ p: 3 }}>
                  No alerts
                </Typography>
              ) : (
                alerts.map((alert) => (
                  <ListItem
                    key={alert.id}
                    secondaryAction={
                      <Chip
                        label={alert.severity}
                        color={getSeverityColor(alert.severity)}
                        size="small"
                      />
                    }
                    sx={{
                      backgroundColor:
                        alert.severity === 'CRITICAL' ? 'error.light' : 'transparent',
                      mb: 1,
                      borderRadius: 1,
                    }}
                  >
                    <ListItemButton
                      onClick={() => handleViewAlert(alert)}
                      sx={{ flex: 1 }}
                    >
                      <ListItemText
                        primary={
                          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                            <Typography variant="subtitle1">{alert.title}</Typography>
                            <Chip
                              label={alert.type}
                              color={getTypeColor(alert.type as string)}
                              size="small"
                              variant="outlined"
                            />
                          </Box>
                        }
                        secondary={alert.message}
                      />
                    </ListItemButton>
                  </ListItem>
                ))
              )}
            </List>
          )}
        </TabPanel>
      </Paper>

      {/* Notification Details Dialog */}
      <Dialog
        open={notificationDialogOpen}
        onClose={() => setNotificationDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Notification Details</DialogTitle>
        <DialogContent dividers>
          {selectedNotification && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography variant="body2">
                <strong>Title:</strong> {selectedNotification.title}
              </Typography>
              <Typography variant="body2">
                <strong>Message:</strong> {selectedNotification.message}
              </Typography>
              <Typography variant="body2">
                <strong>Status:</strong>{' '}
                <Chip
                  label={selectedNotification.isRead ? 'READ' : 'UNREAD'}
                  color={selectedNotification.isRead ? 'default' : 'primary'}
                  size="small"
                />
              </Typography>
              <Typography variant="body2">
                <strong>Type:</strong>{' '}
                <Chip
                  label={selectedNotification.type}
                  color={getTypeColor(selectedNotification.type as string)}
                  size="small"
                />
              </Typography>
              <Typography variant="body2">
                <strong>Category:</strong> {selectedNotification.category}
              </Typography>
              <Typography variant="body2">
                <strong>Time:</strong>{' '}
                {new Date(selectedNotification.createdAt).toLocaleString()}
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setNotificationDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Alert Details Dialog */}
      <Dialog
        open={alertDialogOpen}
        onClose={() => setAlertDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Alert Details</DialogTitle>
        <DialogContent dividers>
          {selectedAlert && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography variant="body2">
                <strong>Title:</strong> {selectedAlert.title}
              </Typography>
              <Typography variant="body2">
                <strong>Message:</strong> {selectedAlert.message}
              </Typography>
              <Typography variant="body2">
                <strong>Severity:</strong>{' '}
                <Chip
                  label={selectedAlert.severity}
                  color={getSeverityColor(selectedAlert.severity)}
                  size="small"
                />
              </Typography>
              <Typography variant="body2">
                <strong>Type:</strong>{' '}
                <Chip
                  label={selectedAlert.type}
                  color={getTypeColor(selectedAlert.type as string)}
                  size="small"
                />
              </Typography>
              <Typography variant="body2">
                <strong>Category:</strong> {selectedAlert.category}
              </Typography>
              <Typography variant="body2">
                <strong>Resolved:</strong> {selectedAlert.resolved ? 'Yes' : 'No'}
              </Typography>
              <Typography variant="body2">
                <strong>Time:</strong> {new Date(selectedAlert.createdAt).toLocaleString()}
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAlertDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default NotificationCenterPage;

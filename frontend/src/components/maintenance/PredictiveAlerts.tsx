/**
 * PredictiveAlerts.tsx
 * PR #38: Predictive Maintenance Alerts Dashboard
 * 
 * Features:
 * - AI-predicted maintenance needs
 * - Confidence percentages with visual indicators
 * - Schedule maintenance actions
 * - Alert priority levels
 * - Time-to-failure estimates
 * - Component health trends
 */

import React, { useState, useMemo } from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Chip,
  IconButton,
  LinearProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Alert,
  AlertTitle,
  Tooltip,
  Badge,
  Collapse,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Avatar,
  Switch,
  FormControlLabel,
  Divider,
} from '@mui/material';
import {
  Warning,
  Error,
  Info,
  CheckCircle,
  Schedule,
  TrendingDown,
  TrendingUp,
  DirectionsCar,
  BatteryAlert,
  Build,
  ExpandMore,
  ExpandLess,
  Notifications,
  NotificationsActive,
  AutoGraph,
  Timer,
  Speed,
  Tire,
  Thermostat,
  Memory,
  Settings,
  Refresh,
} from '@mui/icons-material';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  ResponsiveContainer,
  AreaChart,
  Area,
  ReferenceLine,
} from 'recharts';
import { DatePicker } from '@mui/x-date-pickers';
import { format, addDays, differenceInDays } from 'date-fns';

// Types
interface PredictiveAlert {
  id: string;
  vehicleId: string;
  vehicleName: string;
  licensePlate: string;
  component: ComponentType;
  alertType: AlertType;
  severity: 'info' | 'warning' | 'critical';
  title: string;
  description: string;
  confidence: number; // 0-100
  predictedFailureDate: Date;
  currentHealth: number; // 0-100
  degradationRate: number; // per month
  estimatedCost: number;
  recommendations: string[];
  lastUpdated: Date;
  isAcknowledged: boolean;
  isSnoozed: boolean;
  healthHistory: { date: string; value: number }[];
}

type ComponentType = 
  | 'battery'
  | 'motor'
  | 'brakes'
  | 'tires'
  | 'hvac'
  | 'suspension'
  | 'charging_port'
  | 'cooling_system'
  | 'electronics';

type AlertType = 
  | 'degradation'
  | 'anomaly'
  | 'threshold'
  | 'predictive'
  | 'pattern';

// Mock data
const mockAlerts: PredictiveAlert[] = [
  {
    id: '1',
    vehicleId: 'v1',
    vehicleName: 'Tesla Model 3 #1',
    licensePlate: 'EV-001',
    component: 'battery',
    alertType: 'degradation',
    severity: 'warning',
    title: 'Battery Capacity Declining',
    description: 'Battery health has degraded to 87% and is declining at 1.2% per month. Predicted to reach critical threshold in 8 months.',
    confidence: 92,
    predictedFailureDate: addDays(new Date(), 240),
    currentHealth: 87,
    degradationRate: 1.2,
    estimatedCost: 150000,
    recommendations: [
      'Reduce fast charging frequency',
      'Avoid charging above 80% regularly',
      'Schedule battery health diagnostic',
      'Consider thermal management system check',
    ],
    lastUpdated: new Date(),
    isAcknowledged: false,
    isSnoozed: false,
    healthHistory: [
      { date: '2024-01', value: 95 },
      { date: '2024-02', value: 94 },
      { date: '2024-03', value: 92 },
      { date: '2024-04', value: 91 },
      { date: '2024-05', value: 89 },
      { date: '2024-06', value: 87 },
    ],
  },
  {
    id: '2',
    vehicleId: 'v2',
    vehicleName: 'Tata Nexon EV',
    licensePlate: 'EV-002',
    component: 'brakes',
    alertType: 'threshold',
    severity: 'critical',
    title: 'Brake Pad Replacement Needed',
    description: 'Front brake pads are at 15% remaining. Immediate replacement recommended to avoid safety issues.',
    confidence: 98,
    predictedFailureDate: addDays(new Date(), 14),
    currentHealth: 15,
    degradationRate: 5,
    estimatedCost: 8000,
    recommendations: [
      'Schedule immediate brake service',
      'Avoid high-speed driving until serviced',
      'Check rear brake pads as well',
    ],
    lastUpdated: new Date(),
    isAcknowledged: false,
    isSnoozed: false,
    healthHistory: [
      { date: '2024-01', value: 70 },
      { date: '2024-02', value: 60 },
      { date: '2024-03', value: 45 },
      { date: '2024-04', value: 35 },
      { date: '2024-05', value: 25 },
      { date: '2024-06', value: 15 },
    ],
  },
  {
    id: '3',
    vehicleId: 'v3',
    vehicleName: 'MG ZS EV',
    licensePlate: 'EV-003',
    component: 'tires',
    alertType: 'predictive',
    severity: 'info',
    title: 'Tire Rotation Due Soon',
    description: 'Based on usage patterns, tire rotation should be performed within the next 2000 km.',
    confidence: 85,
    predictedFailureDate: addDays(new Date(), 30),
    currentHealth: 65,
    degradationRate: 2,
    estimatedCost: 2000,
    recommendations: [
      'Schedule tire rotation',
      'Check tire pressure regularly',
      'Inspect for uneven wear patterns',
    ],
    lastUpdated: new Date(),
    isAcknowledged: true,
    isSnoozed: false,
    healthHistory: [
      { date: '2024-01', value: 100 },
      { date: '2024-02', value: 92 },
      { date: '2024-03', value: 84 },
      { date: '2024-04', value: 76 },
      { date: '2024-05', value: 70 },
      { date: '2024-06', value: 65 },
    ],
  },
  {
    id: '4',
    vehicleId: 'v4',
    vehicleName: 'Hyundai Kona',
    licensePlate: 'EV-004',
    component: 'cooling_system',
    alertType: 'anomaly',
    severity: 'warning',
    title: 'Cooling System Anomaly Detected',
    description: 'Unusual temperature fluctuations detected in the cooling system. May indicate coolant leak or pump issues.',
    confidence: 78,
    predictedFailureDate: addDays(new Date(), 60),
    currentHealth: 72,
    degradationRate: 3,
    estimatedCost: 15000,
    recommendations: [
      'Inspect cooling system for leaks',
      'Check coolant levels',
      'Monitor temperature readings closely',
      'Schedule diagnostic appointment',
    ],
    lastUpdated: new Date(),
    isAcknowledged: false,
    isSnoozed: false,
    healthHistory: [
      { date: '2024-01', value: 95 },
      { date: '2024-02', value: 93 },
      { date: '2024-03', value: 88 },
      { date: '2024-04', value: 82 },
      { date: '2024-05', value: 76 },
      { date: '2024-06', value: 72 },
    ],
  },
  {
    id: '5',
    vehicleId: 'v1',
    vehicleName: 'Tesla Model 3 #1',
    licensePlate: 'EV-001',
    component: 'hvac',
    alertType: 'pattern',
    severity: 'info',
    title: 'HVAC Efficiency Decrease',
    description: 'Cabin heating/cooling efficiency has decreased by 12% over the last month. May need filter replacement or compressor check.',
    confidence: 72,
    predictedFailureDate: addDays(new Date(), 90),
    currentHealth: 78,
    degradationRate: 1.5,
    estimatedCost: 5000,
    recommendations: [
      'Replace cabin air filter',
      'Check refrigerant levels',
      'Inspect HVAC compressor',
    ],
    lastUpdated: new Date(),
    isAcknowledged: false,
    isSnoozed: true,
    healthHistory: [
      { date: '2024-01', value: 98 },
      { date: '2024-02', value: 95 },
      { date: '2024-03', value: 90 },
      { date: '2024-04', value: 86 },
      { date: '2024-05', value: 82 },
      { date: '2024-06', value: 78 },
    ],
  },
];

const componentConfig: Record<ComponentType, { icon: React.ReactNode; label: string; color: string }> = {
  battery: { icon: <BatteryAlert />, label: 'Battery', color: '#4CAF50' },
  motor: { icon: <Speed />, label: 'Motor', color: '#2196F3' },
  brakes: { icon: <Warning />, label: 'Brakes', color: '#F44336' },
  tires: { icon: <Tire />, label: 'Tires', color: '#795548' },
  hvac: { icon: <Thermostat />, label: 'HVAC', color: '#00BCD4' },
  suspension: { icon: <DirectionsCar />, label: 'Suspension', color: '#9C27B0' },
  charging_port: { icon: <Memory />, label: 'Charging Port', color: '#FF9800' },
  cooling_system: { icon: <Thermostat />, label: 'Cooling System', color: '#3F51B5' },
  electronics: { icon: <Memory />, label: 'Electronics', color: '#607D8B' },
};

const severityConfig = {
  info: { color: '#2196F3', bgcolor: '#E3F2FD', icon: <Info /> },
  warning: { color: '#FF9800', bgcolor: '#FFF3E0', icon: <Warning /> },
  critical: { color: '#F44336', bgcolor: '#FFEBEE', icon: <Error /> },
};

const Tire: React.FC = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
    <circle cx="12" cy="12" r="10" fill="none" stroke="currentColor" strokeWidth="2"/>
    <circle cx="12" cy="12" r="4" />
  </svg>
);

export const PredictiveAlerts: React.FC = () => {
  const [alerts, setAlerts] = useState<PredictiveAlert[]>(mockAlerts);
  const [selectedTab, setSelectedTab] = useState(0);
  const [expandedAlertId, setExpandedAlertId] = useState<string | null>(null);
  const [scheduleDialogOpen, setScheduleDialogOpen] = useState(false);
  const [selectedAlert, setSelectedAlert] = useState<PredictiveAlert | null>(null);
  const [scheduleDate, setScheduleDate] = useState<Date | null>(null);
  const [showSnoozed, setShowSnoozed] = useState(false);
  const [showAcknowledged, setShowAcknowledged] = useState(true);

  // Filter alerts
  const filteredAlerts = useMemo(() => {
    let filtered = alerts;
    
    if (!showSnoozed) {
      filtered = filtered.filter(a => !a.isSnoozed);
    }
    if (!showAcknowledged) {
      filtered = filtered.filter(a => !a.isAcknowledged);
    }

    switch (selectedTab) {
      case 1:
        return filtered.filter(a => a.severity === 'critical');
      case 2:
        return filtered.filter(a => a.severity === 'warning');
      case 3:
        return filtered.filter(a => a.severity === 'info');
      default:
        return filtered;
    }
  }, [alerts, selectedTab, showSnoozed, showAcknowledged]);

  // Stats
  const stats = useMemo(() => ({
    total: alerts.length,
    critical: alerts.filter(a => a.severity === 'critical' && !a.isSnoozed).length,
    warning: alerts.filter(a => a.severity === 'warning' && !a.isSnoozed).length,
    info: alerts.filter(a => a.severity === 'info' && !a.isSnoozed).length,
    snoozed: alerts.filter(a => a.isSnoozed).length,
    avgConfidence: Math.round(alerts.reduce((sum, a) => sum + a.confidence, 0) / alerts.length),
  }), [alerts]);

  // Handlers
  const handleAcknowledge = (alertId: string) => {
    setAlerts(prev =>
      prev.map(a => a.id === alertId ? { ...a, isAcknowledged: true } : a)
    );
  };

  const handleSnooze = (alertId: string) => {
    setAlerts(prev =>
      prev.map(a => a.id === alertId ? { ...a, isSnoozed: !a.isSnoozed } : a)
    );
  };

  const handleScheduleMaintenance = (alert: PredictiveAlert) => {
    setSelectedAlert(alert);
    setScheduleDate(addDays(new Date(), 7));
    setScheduleDialogOpen(true);
  };

  const handleConfirmSchedule = () => {
    // In production, this would create a maintenance event
    if (selectedAlert) {
      handleAcknowledge(selectedAlert.id);
    }
    setScheduleDialogOpen(false);
    setSelectedAlert(null);
  };

  const getHealthColor = (health: number) => {
    if (health >= 80) return '#4CAF50';
    if (health >= 60) return '#FF9800';
    if (health >= 40) return '#F44336';
    return '#9C27B0';
  };

  const getConfidenceColor = (confidence: number) => {
    if (confidence >= 90) return '#4CAF50';
    if (confidence >= 70) return '#FF9800';
    return '#F44336';
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <AutoGraph sx={{ fontSize: 40, color: 'primary.main' }} />
          <Box>
            <Typography variant="h5" fontWeight={600}>
              Predictive Maintenance Alerts
            </Typography>
            <Typography variant="body2" color="text.secondary">
              AI-powered predictions based on vehicle telemetry and patterns
            </Typography>
          </Box>
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Tooltip title="Refresh predictions">
            <IconButton>
              <Refresh />
            </IconButton>
          </Tooltip>
          <Tooltip title="Alert settings">
            <IconButton>
              <Settings />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={6} sm={4} md={2}>
          <Card>
            <CardContent sx={{ textAlign: 'center', py: 2 }}>
              <Badge badgeContent={stats.critical} color="error">
                <Error sx={{ fontSize: 40, color: '#F44336' }} />
              </Badge>
              <Typography variant="h6" sx={{ mt: 1 }}>Critical</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} sm={4} md={2}>
          <Card>
            <CardContent sx={{ textAlign: 'center', py: 2 }}>
              <Badge badgeContent={stats.warning} color="warning">
                <Warning sx={{ fontSize: 40, color: '#FF9800' }} />
              </Badge>
              <Typography variant="h6" sx={{ mt: 1 }}>Warning</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} sm={4} md={2}>
          <Card>
            <CardContent sx={{ textAlign: 'center', py: 2 }}>
              <Badge badgeContent={stats.info} color="info">
                <Info sx={{ fontSize: 40, color: '#2196F3' }} />
              </Badge>
              <Typography variant="h6" sx={{ mt: 1 }}>Info</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} sm={4} md={2}>
          <Card>
            <CardContent sx={{ textAlign: 'center', py: 2 }}>
              <NotificationsActive sx={{ fontSize: 40, color: '#9E9E9E' }} />
              <Typography variant="h6" sx={{ mt: 1 }}>{stats.snoozed}</Typography>
              <Typography variant="caption" color="text.secondary">Snoozed</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} sm={4} md={2}>
          <Card>
            <CardContent sx={{ textAlign: 'center', py: 2 }}>
              <Typography variant="h3" color="primary" fontWeight={600}>
                {stats.avgConfidence}%
              </Typography>
              <Typography variant="caption" color="text.secondary">Avg Confidence</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} sm={4} md={2}>
          <Card>
            <CardContent sx={{ textAlign: 'center', py: 2 }}>
              <Typography variant="h3" color="text.primary" fontWeight={600}>
                {stats.total}
              </Typography>
              <Typography variant="caption" color="text.secondary">Total Alerts</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Filters */}
      <Paper sx={{ mb: 3 }}>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={selectedTab} onChange={(_, v) => setSelectedTab(v)}>
            <Tab label={`All (${alerts.length})`} />
            <Tab
              label={`Critical (${stats.critical})`}
              sx={{ color: stats.critical > 0 ? '#F44336' : 'inherit' }}
            />
            <Tab label={`Warning (${stats.warning})`} />
            <Tab label={`Info (${stats.info})`} />
          </Tabs>
        </Box>
        <Box sx={{ p: 2, display: 'flex', gap: 2 }}>
          <FormControlLabel
            control={<Switch checked={showSnoozed} onChange={(e) => setShowSnoozed(e.target.checked)} />}
            label="Show Snoozed"
          />
          <FormControlLabel
            control={<Switch checked={showAcknowledged} onChange={(e) => setShowAcknowledged(e.target.checked)} />}
            label="Show Acknowledged"
          />
        </Box>
      </Paper>

      {/* Alerts List */}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        {filteredAlerts.map(alert => (
          <Card
            key={alert.id}
            sx={{
              borderLeft: `4px solid ${severityConfig[alert.severity].color}`,
              opacity: alert.isSnoozed ? 0.6 : 1,
            }}
          >
            <CardContent>
              <Grid container spacing={2}>
                {/* Left: Alert Info */}
                <Grid item xs={12} md={6}>
                  <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
                    <Avatar
                      sx={{
                        bgcolor: componentConfig[alert.component].color,
                        width: 48,
                        height: 48,
                      }}
                    >
                      {componentConfig[alert.component].icon}
                    </Avatar>
                    <Box sx={{ flex: 1 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                        <Typography variant="h6">{alert.title}</Typography>
                        {alert.isAcknowledged && (
                          <Chip size="small" label="Acknowledged" color="success" />
                        )}
                        {alert.isSnoozed && (
                          <Chip size="small" label="Snoozed" />
                        )}
                      </Box>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
                        <DirectionsCar fontSize="small" color="action" />
                        <Typography variant="body2" color="text.secondary">
                          {alert.vehicleName} ({alert.licensePlate})
                        </Typography>
                        <Chip
                          size="small"
                          label={componentConfig[alert.component].label}
                          sx={{
                            bgcolor: componentConfig[alert.component].color,
                            color: 'white',
                          }}
                        />
                      </Box>
                      <Typography variant="body2" sx={{ mt: 1 }}>
                        {alert.description}
                      </Typography>
                    </Box>
                  </Box>
                </Grid>

                {/* Right: Metrics */}
                <Grid item xs={12} md={6}>
                  <Grid container spacing={2}>
                    <Grid item xs={4}>
                      <Box sx={{ textAlign: 'center' }}>
                        <Typography variant="caption" color="text.secondary">
                          Confidence
                        </Typography>
                        <Box sx={{ position: 'relative', display: 'inline-flex', mt: 0.5 }}>
                          <Box
                            sx={{
                              width: 60,
                              height: 60,
                              borderRadius: '50%',
                              border: `4px solid ${getConfidenceColor(alert.confidence)}`,
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                            }}
                          >
                            <Typography variant="h6" fontWeight={600}>
                              {alert.confidence}%
                            </Typography>
                          </Box>
                        </Box>
                      </Box>
                    </Grid>
                    <Grid item xs={4}>
                      <Box sx={{ textAlign: 'center' }}>
                        <Typography variant="caption" color="text.secondary">
                          Health
                        </Typography>
                        <Box sx={{ mt: 0.5 }}>
                          <Typography
                            variant="h5"
                            fontWeight={600}
                            sx={{ color: getHealthColor(alert.currentHealth) }}
                          >
                            {alert.currentHealth}%
                          </Typography>
                          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                            <TrendingDown sx={{ fontSize: 16, color: '#F44336' }} />
                            <Typography variant="caption" color="error">
                              -{alert.degradationRate}%/mo
                            </Typography>
                          </Box>
                        </Box>
                      </Box>
                    </Grid>
                    <Grid item xs={4}>
                      <Box sx={{ textAlign: 'center' }}>
                        <Typography variant="caption" color="text.secondary">
                          Time to Failure
                        </Typography>
                        <Box sx={{ mt: 0.5 }}>
                          <Typography variant="h5" fontWeight={600}>
                            {differenceInDays(alert.predictedFailureDate, new Date())}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            days
                          </Typography>
                        </Box>
                      </Box>
                    </Grid>
                  </Grid>

                  {/* Mini Health Chart */}
                  <Box sx={{ height: 60, mt: 1 }}>
                    <ResponsiveContainer width="100%" height="100%">
                      <AreaChart data={alert.healthHistory}>
                        <defs>
                          <linearGradient id={`gradient-${alert.id}`} x1="0" y1="0" x2="0" y2="1">
                            <stop offset="5%" stopColor={getHealthColor(alert.currentHealth)} stopOpacity={0.3}/>
                            <stop offset="95%" stopColor={getHealthColor(alert.currentHealth)} stopOpacity={0}/>
                          </linearGradient>
                        </defs>
                        <Area
                          type="monotone"
                          dataKey="value"
                          stroke={getHealthColor(alert.currentHealth)}
                          fill={`url(#gradient-${alert.id})`}
                        />
                      </AreaChart>
                    </ResponsiveContainer>
                  </Box>
                </Grid>
              </Grid>

              {/* Expandable Recommendations */}
              <Box sx={{ mt: 2 }}>
                <Button
                  size="small"
                  onClick={() => setExpandedAlertId(expandedAlertId === alert.id ? null : alert.id)}
                  endIcon={expandedAlertId === alert.id ? <ExpandLess /> : <ExpandMore />}
                >
                  {expandedAlertId === alert.id ? 'Hide' : 'Show'} Recommendations
                </Button>
                <Collapse in={expandedAlertId === alert.id}>
                  <Box sx={{ mt: 1, p: 2, bgcolor: 'action.hover', borderRadius: 1 }}>
                    <Typography variant="subtitle2" gutterBottom>
                      Recommended Actions:
                    </Typography>
                    <List dense>
                      {alert.recommendations.map((rec, idx) => (
                        <ListItem key={idx}>
                          <ListItemIcon sx={{ minWidth: 32 }}>
                            <CheckCircle fontSize="small" color="primary" />
                          </ListItemIcon>
                          <ListItemText primary={rec} />
                        </ListItem>
                      ))}
                    </List>
                    <Divider sx={{ my: 1 }} />
                    <Typography variant="body2">
                      <strong>Estimated Cost:</strong> ₹{alert.estimatedCost.toLocaleString()}
                    </Typography>
                  </Box>
                </Collapse>
              </Box>
            </CardContent>

            <CardActions sx={{ justifyContent: 'flex-end', px: 2, pb: 2 }}>
              <Button
                size="small"
                onClick={() => handleSnooze(alert.id)}
              >
                {alert.isSnoozed ? 'Unsnooze' : 'Snooze'}
              </Button>
              {!alert.isAcknowledged && (
                <Button
                  size="small"
                  onClick={() => handleAcknowledge(alert.id)}
                >
                  Acknowledge
                </Button>
              )}
              <Button
                variant="contained"
                size="small"
                startIcon={<Schedule />}
                onClick={() => handleScheduleMaintenance(alert)}
              >
                Schedule Maintenance
              </Button>
            </CardActions>
          </Card>
        ))}

        {filteredAlerts.length === 0 && (
          <Paper sx={{ p: 4, textAlign: 'center' }}>
            <CheckCircle sx={{ fontSize: 60, color: '#4CAF50', mb: 2 }} />
            <Typography variant="h6">No alerts to display</Typography>
            <Typography variant="body2" color="text.secondary">
              All systems are operating normally
            </Typography>
          </Paper>
        )}
      </Box>

      {/* Schedule Maintenance Dialog */}
      <Dialog
        open={scheduleDialogOpen}
        onClose={() => setScheduleDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Schedule Maintenance</DialogTitle>
        <DialogContent>
          {selectedAlert && (
            <Box sx={{ mt: 1 }}>
              <Alert severity={selectedAlert.severity} sx={{ mb: 2 }}>
                <AlertTitle>{selectedAlert.title}</AlertTitle>
                {selectedAlert.vehicleName} - {componentConfig[selectedAlert.component].label}
              </Alert>

              <Typography variant="body2" color="text.secondary" gutterBottom>
                Based on our predictions, we recommend scheduling this maintenance before{' '}
                <strong>{format(selectedAlert.predictedFailureDate, 'MMMM d, yyyy')}</strong>.
              </Typography>

              <Box sx={{ mt: 2 }}>
                <DatePicker
                  label="Scheduled Date"
                  value={scheduleDate}
                  onChange={(date) => setScheduleDate(date)}
                  minDate={new Date()}
                  slotProps={{
                    textField: { fullWidth: true },
                  }}
                />
              </Box>

              <TextField
                label="Notes"
                multiline
                rows={3}
                fullWidth
                sx={{ mt: 2 }}
                placeholder="Add any special instructions or notes..."
              />

              <Box sx={{ mt: 2, p: 2, bgcolor: 'action.hover', borderRadius: 1 }}>
                <Typography variant="body2">
                  <strong>Estimated Cost:</strong> ₹{selectedAlert.estimatedCost.toLocaleString()}
                </Typography>
                <Typography variant="body2">
                  <strong>Component:</strong> {componentConfig[selectedAlert.component].label}
                </Typography>
                <Typography variant="body2">
                  <strong>Current Health:</strong> {selectedAlert.currentHealth}%
                </Typography>
              </Box>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setScheduleDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleConfirmSchedule}>
            Schedule Maintenance
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default PredictiveAlerts;

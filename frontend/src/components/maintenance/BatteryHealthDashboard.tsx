/**
 * BatteryHealthDashboard.tsx
 * PR #39: Battery Health Dashboard
 * 
 * Features:
 * - State of Health (SOH) percentage display
 * - Charge cycle count and trends
 * - Degradation trends with prediction
 * - Fleet battery health comparison
 * - Cell-level health indicators
 * - Temperature impact analysis
 */

import React, { useState, useMemo } from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Card,
  CardContent,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  LinearProgress,
  IconButton,
  Tooltip,
  Alert,
  AlertTitle,
  Button,
  Divider,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Avatar,
  CircularProgress,
} from '@mui/material';
import {
  BatteryFull,
  BatteryChargingFull,
  Battery80,
  Battery60,
  Battery30,
  BatteryAlert,
  TrendingUp,
  TrendingDown,
  TrendingFlat,
  Thermostat,
  Speed,
  EvStation,
  Warning,
  CheckCircle,
  Info,
  Refresh,
  Download,
  DirectionsCar,
  Schedule,
  Assessment,
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
  BarChart,
  Bar,
  Cell,
  PieChart,
  Pie,
  RadarChart,
  Radar,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  ReferenceLine,
  ComposedChart,
  Legend,
} from 'recharts';
import { format, subMonths } from 'date-fns';

// Types
interface BatteryHealth {
  vehicleId: string;
  vehicleName: string;
  licensePlate: string;
  stateOfHealth: number; // SOH percentage
  stateOfCharge: number; // SOC percentage
  chargeCycles: number;
  totalChargeCycles: number; // Rated cycles
  originalCapacity: number; // kWh
  currentCapacity: number; // kWh
  averageTemperature: number; // Celsius
  lastCharged: Date;
  degradationRate: number; // % per month
  estimatedLifeRemaining: number; // months
  cellHealth: CellHealth[];
  healthHistory: { date: string; soh: number; temperature: number }[];
  chargingPatterns: {
    fastChargePercent: number;
    homeChargePercent: number;
    publicChargePercent: number;
  };
  alerts: string[];
}

interface CellHealth {
  cellId: number;
  voltage: number;
  temperature: number;
  health: number;
  status: 'good' | 'fair' | 'warning' | 'critical';
}

// Mock data
const mockBatteryData: BatteryHealth[] = [
  {
    vehicleId: 'v1',
    vehicleName: 'Tesla Model 3 #1',
    licensePlate: 'EV-001',
    stateOfHealth: 92,
    stateOfCharge: 78,
    chargeCycles: 450,
    totalChargeCycles: 1500,
    originalCapacity: 75,
    currentCapacity: 69,
    averageTemperature: 32,
    lastCharged: new Date(),
    degradationRate: 0.8,
    estimatedLifeRemaining: 84,
    cellHealth: Array.from({ length: 96 }, (_, i) => ({
      cellId: i + 1,
      voltage: 3.7 + Math.random() * 0.3,
      temperature: 28 + Math.random() * 8,
      health: 88 + Math.random() * 12,
      status: Math.random() > 0.95 ? 'warning' : Math.random() > 0.8 ? 'fair' : 'good',
    })),
    healthHistory: Array.from({ length: 12 }, (_, i) => ({
      date: format(subMonths(new Date(), 11 - i), 'MMM yyyy'),
      soh: 100 - i * 0.7,
      temperature: 28 + Math.random() * 6,
    })),
    chargingPatterns: {
      fastChargePercent: 25,
      homeChargePercent: 60,
      publicChargePercent: 15,
    },
    alerts: ['Reduce fast charging frequency for optimal battery life'],
  },
  {
    vehicleId: 'v2',
    vehicleName: 'Tata Nexon EV',
    licensePlate: 'EV-002',
    stateOfHealth: 88,
    stateOfCharge: 45,
    chargeCycles: 680,
    totalChargeCycles: 1200,
    originalCapacity: 40,
    currentCapacity: 35.2,
    averageTemperature: 35,
    lastCharged: new Date(Date.now() - 86400000),
    degradationRate: 1.1,
    estimatedLifeRemaining: 60,
    cellHealth: Array.from({ length: 72 }, (_, i) => ({
      cellId: i + 1,
      voltage: 3.6 + Math.random() * 0.4,
      temperature: 30 + Math.random() * 10,
      health: 82 + Math.random() * 16,
      status: Math.random() > 0.9 ? 'warning' : Math.random() > 0.7 ? 'fair' : 'good',
    })),
    healthHistory: Array.from({ length: 12 }, (_, i) => ({
      date: format(subMonths(new Date(), 11 - i), 'MMM yyyy'),
      soh: 100 - i * 1.0,
      temperature: 30 + Math.random() * 8,
    })),
    chargingPatterns: {
      fastChargePercent: 45,
      homeChargePercent: 40,
      publicChargePercent: 15,
    },
    alerts: [
      'High fast-charging usage detected',
      'Consider reducing charging to 80% maximum',
    ],
  },
  {
    vehicleId: 'v3',
    vehicleName: 'MG ZS EV',
    licensePlate: 'EV-003',
    stateOfHealth: 95,
    stateOfCharge: 92,
    chargeCycles: 220,
    totalChargeCycles: 1500,
    originalCapacity: 50.3,
    currentCapacity: 47.8,
    averageTemperature: 29,
    lastCharged: new Date(),
    degradationRate: 0.5,
    estimatedLifeRemaining: 120,
    cellHealth: Array.from({ length: 84 }, (_, i) => ({
      cellId: i + 1,
      voltage: 3.75 + Math.random() * 0.2,
      temperature: 26 + Math.random() * 6,
      health: 92 + Math.random() * 8,
      status: Math.random() > 0.98 ? 'fair' : 'good',
    })),
    healthHistory: Array.from({ length: 12 }, (_, i) => ({
      date: format(subMonths(new Date(), 11 - i), 'MMM yyyy'),
      soh: 100 - i * 0.4,
      temperature: 26 + Math.random() * 5,
    })),
    chargingPatterns: {
      fastChargePercent: 10,
      homeChargePercent: 80,
      publicChargePercent: 10,
    },
    alerts: [],
  },
  {
    vehicleId: 'v4',
    vehicleName: 'Hyundai Kona',
    licensePlate: 'EV-004',
    stateOfHealth: 78,
    stateOfCharge: 32,
    chargeCycles: 890,
    totalChargeCycles: 1000,
    originalCapacity: 64,
    currentCapacity: 49.9,
    averageTemperature: 38,
    lastCharged: new Date(Date.now() - 172800000),
    degradationRate: 1.8,
    estimatedLifeRemaining: 36,
    cellHealth: Array.from({ length: 90 }, (_, i) => ({
      cellId: i + 1,
      voltage: 3.5 + Math.random() * 0.5,
      temperature: 32 + Math.random() * 12,
      health: 70 + Math.random() * 20,
      status: Math.random() > 0.7 ? 'warning' : Math.random() > 0.5 ? 'fair' : 'good',
    })),
    healthHistory: Array.from({ length: 12 }, (_, i) => ({
      date: format(subMonths(new Date(), 11 - i), 'MMM yyyy'),
      soh: 100 - i * 1.8,
      temperature: 34 + Math.random() * 8,
    })),
    chargingPatterns: {
      fastChargePercent: 60,
      homeChargePercent: 25,
      publicChargePercent: 15,
    },
    alerts: [
      'Battery degradation above normal rate',
      'High operating temperature detected',
      'Consider scheduling battery diagnostic',
    ],
  },
];

const getSOHColor = (soh: number): string => {
  if (soh >= 90) return '#4CAF50';
  if (soh >= 80) return '#8BC34A';
  if (soh >= 70) return '#FF9800';
  if (soh >= 60) return '#F44336';
  return '#9C27B0';
};

const getSOHStatus = (soh: number): string => {
  if (soh >= 90) return 'Excellent';
  if (soh >= 80) return 'Good';
  if (soh >= 70) return 'Fair';
  if (soh >= 60) return 'Poor';
  return 'Critical';
};

const getBatteryIcon = (soh: number) => {
  if (soh >= 90) return <BatteryFull />;
  if (soh >= 80) return <Battery80 />;
  if (soh >= 60) return <Battery60 />;
  if (soh >= 30) return <Battery30 />;
  return <BatteryAlert />;
};

export const BatteryHealthDashboard: React.FC = () => {
  const [selectedVehicle, setSelectedVehicle] = useState<string>(mockBatteryData[0].vehicleId);
  const [selectedTab, setSelectedTab] = useState(0);

  const currentBattery = useMemo(
    () => mockBatteryData.find(b => b.vehicleId === selectedVehicle) || mockBatteryData[0],
    [selectedVehicle]
  );

  // Fleet summary stats
  const fleetStats = useMemo(() => {
    const vehicles = mockBatteryData;
    return {
      avgSOH: Math.round(vehicles.reduce((sum, v) => sum + v.stateOfHealth, 0) / vehicles.length),
      minSOH: Math.min(...vehicles.map(v => v.stateOfHealth)),
      maxSOH: Math.max(...vehicles.map(v => v.stateOfHealth)),
      totalCycles: vehicles.reduce((sum, v) => sum + v.chargeCycles, 0),
      avgDegradation: (vehicles.reduce((sum, v) => sum + v.degradationRate, 0) / vehicles.length).toFixed(2),
      vehiclesNeedingAttention: vehicles.filter(v => v.stateOfHealth < 80).length,
    };
  }, []);

  // Cell health distribution
  const cellHealthDistribution = useMemo(() => {
    const cells = currentBattery.cellHealth;
    return [
      { name: 'Good', value: cells.filter(c => c.status === 'good').length, color: '#4CAF50' },
      { name: 'Fair', value: cells.filter(c => c.status === 'fair').length, color: '#FF9800' },
      { name: 'Warning', value: cells.filter(c => c.status === 'warning').length, color: '#F44336' },
      { name: 'Critical', value: cells.filter(c => c.status === 'critical').length, color: '#9C27B0' },
    ].filter(d => d.value > 0);
  }, [currentBattery]);

  // Radar data for battery metrics
  const radarData = useMemo(() => [
    { metric: 'SOH', value: currentBattery.stateOfHealth },
    { metric: 'Capacity', value: (currentBattery.currentCapacity / currentBattery.originalCapacity) * 100 },
    { metric: 'Cycles Left', value: ((currentBattery.totalChargeCycles - currentBattery.chargeCycles) / currentBattery.totalChargeCycles) * 100 },
    { metric: 'Temp Health', value: Math.max(0, 100 - (currentBattery.averageTemperature - 25) * 2) },
    { metric: 'Degradation', value: Math.max(0, 100 - currentBattery.degradationRate * 20) },
  ], [currentBattery]);

  // Fleet comparison data
  const fleetComparisonData = useMemo(() =>
    mockBatteryData.map(b => ({
      name: b.licensePlate,
      soh: b.stateOfHealth,
      cycles: (b.chargeCycles / b.totalChargeCycles) * 100,
      fullName: b.vehicleName,
    })),
    []
  );

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <BatteryChargingFull sx={{ fontSize: 40, color: 'primary.main' }} />
          <Box>
            <Typography variant="h5" fontWeight={600}>
              Battery Health Dashboard
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Monitor battery performance, degradation, and cell health
            </Typography>
          </Box>
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <FormControl size="small" sx={{ minWidth: 200 }}>
            <InputLabel>Select Vehicle</InputLabel>
            <Select
              value={selectedVehicle}
              label="Select Vehicle"
              onChange={(e) => setSelectedVehicle(e.target.value)}
            >
              {mockBatteryData.map(b => (
                <MenuItem key={b.vehicleId} value={b.vehicleId}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    {getBatteryIcon(b.stateOfHealth)}
                    {b.vehicleName}
                  </Box>
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <Tooltip title="Refresh data">
            <IconButton>
              <Refresh />
            </IconButton>
          </Tooltip>
          <Tooltip title="Download report">
            <IconButton>
              <Download />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {/* Fleet Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={6} md={2}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" fontWeight={600} color="primary">
                {fleetStats.avgSOH}%
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Fleet Avg SOH
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} md={2}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" fontWeight={600} color="success.main">
                {fleetStats.maxSOH}%
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Best SOH
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} md={2}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" fontWeight={600} color="error.main">
                {fleetStats.minSOH}%
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Lowest SOH
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} md={2}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" fontWeight={600}>
                {fleetStats.totalCycles.toLocaleString()}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Total Cycles
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} md={2}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" fontWeight={600} color="warning.main">
                {fleetStats.avgDegradation}%
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Avg Degradation/mo
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} md={2}>
          <Card sx={{ bgcolor: fleetStats.vehiclesNeedingAttention > 0 ? '#FFF3E0' : '#E8F5E9' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography
                variant="h4"
                fontWeight={600}
                color={fleetStats.vehiclesNeedingAttention > 0 ? 'warning.main' : 'success.main'}
              >
                {fleetStats.vehiclesNeedingAttention}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Need Attention
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Alerts */}
      {currentBattery.alerts.length > 0 && (
        <Alert
          severity={currentBattery.stateOfHealth < 80 ? 'error' : 'warning'}
          sx={{ mb: 3 }}
        >
          <AlertTitle>Battery Alerts for {currentBattery.vehicleName}</AlertTitle>
          <List dense disablePadding>
            {currentBattery.alerts.map((alert, idx) => (
              <ListItem key={idx} disablePadding>
                <ListItemIcon sx={{ minWidth: 28 }}>
                  <Warning fontSize="small" color="warning" />
                </ListItemIcon>
                <ListItemText primary={alert} />
              </ListItem>
            ))}
          </List>
        </Alert>
      )}

      {/* Main Dashboard */}
      <Grid container spacing={3}>
        {/* Left Column - Battery Status */}
        <Grid item xs={12} md={4}>
          {/* SOH Gauge */}
          <Paper sx={{ p: 3, textAlign: 'center', mb: 2 }}>
            <Typography variant="h6" gutterBottom>
              State of Health (SOH)
            </Typography>
            <Box
              sx={{
                width: 200,
                height: 200,
                borderRadius: '50%',
                border: `12px solid ${getSOHColor(currentBattery.stateOfHealth)}`,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                mx: 'auto',
                my: 2,
              }}
            >
              <Typography variant="h2" fontWeight={700}>
                {currentBattery.stateOfHealth}%
              </Typography>
              <Chip
                label={getSOHStatus(currentBattery.stateOfHealth)}
                sx={{
                  bgcolor: getSOHColor(currentBattery.stateOfHealth),
                  color: 'white',
                  fontWeight: 600,
                }}
              />
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'center', gap: 1, alignItems: 'center' }}>
              <TrendingDown sx={{ color: '#F44336' }} />
              <Typography variant="body2">
                Degrading at {currentBattery.degradationRate}% per month
              </Typography>
            </Box>
          </Paper>

          {/* Key Metrics */}
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Key Metrics
            </Typography>
            <List dense>
              <ListItem>
                <ListItemIcon>
                  <BatteryChargingFull color="primary" />
                </ListItemIcon>
                <ListItemText
                  primary="Current Charge"
                  secondary={`${currentBattery.stateOfCharge}%`}
                />
              </ListItem>
              <ListItem>
                <ListItemIcon>
                  <Speed color="action" />
                </ListItemIcon>
                <ListItemText
                  primary="Charge Cycles"
                  secondary={`${currentBattery.chargeCycles} / ${currentBattery.totalChargeCycles}`}
                />
                <LinearProgress
                  variant="determinate"
                  value={(currentBattery.chargeCycles / currentBattery.totalChargeCycles) * 100}
                  sx={{ width: 60 }}
                />
              </ListItem>
              <ListItem>
                <ListItemIcon>
                  <EvStation color="success" />
                </ListItemIcon>
                <ListItemText
                  primary="Capacity"
                  secondary={`${currentBattery.currentCapacity} / ${currentBattery.originalCapacity} kWh`}
                />
              </ListItem>
              <ListItem>
                <ListItemIcon>
                  <Thermostat color="error" />
                </ListItemIcon>
                <ListItemText
                  primary="Avg Temperature"
                  secondary={`${currentBattery.averageTemperature}°C`}
                />
              </ListItem>
              <ListItem>
                <ListItemIcon>
                  <Schedule color="info" />
                </ListItemIcon>
                <ListItemText
                  primary="Est. Life Remaining"
                  secondary={`${currentBattery.estimatedLifeRemaining} months`}
                />
              </ListItem>
            </List>
          </Paper>
        </Grid>

        {/* Right Column - Charts */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ mb: 2 }}>
            <Tabs value={selectedTab} onChange={(_, v) => setSelectedTab(v)}>
              <Tab label="Health Trend" />
              <Tab label="Cell Health" />
              <Tab label="Fleet Comparison" />
              <Tab label="Charging Patterns" />
            </Tabs>
          </Paper>

          {/* Health Trend Tab */}
          {selectedTab === 0 && (
            <Paper sx={{ p: 2 }}>
              <Typography variant="h6" gutterBottom>
                Battery Health Trend
              </Typography>
              <Box sx={{ height: 300 }}>
                <ResponsiveContainer>
                  <ComposedChart data={currentBattery.healthHistory}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis yAxisId="left" domain={[60, 100]} label={{ value: 'SOH %', angle: -90, position: 'insideLeft' }} />
                    <YAxis yAxisId="right" orientation="right" domain={[20, 50]} label={{ value: 'Temp °C', angle: 90, position: 'insideRight' }} />
                    <RechartsTooltip />
                    <Legend />
                    <ReferenceLine yAxisId="left" y={80} stroke="#FF9800" strokeDasharray="5 5" label="Warning" />
                    <Area
                      yAxisId="left"
                      type="monotone"
                      dataKey="soh"
                      name="State of Health"
                      fill="#4CAF50"
                      fillOpacity={0.3}
                      stroke="#4CAF50"
                      strokeWidth={2}
                    />
                    <Line
                      yAxisId="right"
                      type="monotone"
                      dataKey="temperature"
                      name="Avg Temperature"
                      stroke="#FF5722"
                      strokeWidth={2}
                      dot={false}
                    />
                  </ComposedChart>
                </ResponsiveContainer>
              </Box>

              {/* Radar Chart */}
              <Box sx={{ height: 300, mt: 3 }}>
                <Typography variant="subtitle1" gutterBottom>
                  Battery Health Metrics
                </Typography>
                <ResponsiveContainer>
                  <RadarChart data={radarData}>
                    <PolarGrid />
                    <PolarAngleAxis dataKey="metric" />
                    <PolarRadiusAxis domain={[0, 100]} />
                    <Radar
                      name="Battery"
                      dataKey="value"
                      stroke="#2196F3"
                      fill="#2196F3"
                      fillOpacity={0.5}
                    />
                  </RadarChart>
                </ResponsiveContainer>
              </Box>
            </Paper>
          )}

          {/* Cell Health Tab */}
          {selectedTab === 1 && (
            <Paper sx={{ p: 2 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6">
                  Cell Health Distribution ({currentBattery.cellHealth.length} cells)
                </Typography>
                <Box sx={{ display: 'flex', gap: 1 }}>
                  {cellHealthDistribution.map(d => (
                    <Chip
                      key={d.name}
                      size="small"
                      label={`${d.name}: ${d.value}`}
                      sx={{ bgcolor: d.color, color: 'white' }}
                    />
                  ))}
                </Box>
              </Box>

              {/* Cell Grid */}
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mb: 3 }}>
                {currentBattery.cellHealth.map(cell => (
                  <Tooltip
                    key={cell.cellId}
                    title={
                      <Box>
                        <Typography variant="body2">Cell #{cell.cellId}</Typography>
                        <Typography variant="caption">Health: {cell.health.toFixed(1)}%</Typography>
                        <br />
                        <Typography variant="caption">Voltage: {cell.voltage.toFixed(2)}V</Typography>
                        <br />
                        <Typography variant="caption">Temp: {cell.temperature.toFixed(1)}°C</Typography>
                      </Box>
                    }
                  >
                    <Box
                      sx={{
                        width: 16,
                        height: 16,
                        borderRadius: 0.5,
                        bgcolor:
                          cell.status === 'good' ? '#4CAF50' :
                          cell.status === 'fair' ? '#FF9800' :
                          cell.status === 'warning' ? '#F44336' : '#9C27B0',
                        cursor: 'pointer',
                        '&:hover': { transform: 'scale(1.2)' },
                        transition: 'transform 0.2s',
                      }}
                    />
                  </Tooltip>
                ))}
              </Box>

              {/* Cell Distribution Chart */}
              <Box sx={{ height: 200 }}>
                <ResponsiveContainer>
                  <PieChart>
                    <Pie
                      data={cellHealthDistribution}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={80}
                      dataKey="value"
                      label={({ name, value }) => `${name}: ${value}`}
                    >
                      {cellHealthDistribution.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <RechartsTooltip />
                  </PieChart>
                </ResponsiveContainer>
              </Box>
            </Paper>
          )}

          {/* Fleet Comparison Tab */}
          {selectedTab === 2 && (
            <Paper sx={{ p: 2 }}>
              <Typography variant="h6" gutterBottom>
                Fleet Battery Comparison
              </Typography>
              <Box sx={{ height: 300 }}>
                <ResponsiveContainer>
                  <BarChart data={fleetComparisonData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis domain={[0, 100]} />
                    <RechartsTooltip
                      formatter={(value, name) => [`${value}%`, name]}
                      labelFormatter={(label) => fleetComparisonData.find(d => d.name === label)?.fullName}
                    />
                    <Legend />
                    <Bar dataKey="soh" name="State of Health" fill="#4CAF50">
                      {fleetComparisonData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={getSOHColor(entry.soh)} />
                      ))}
                    </Bar>
                    <Bar dataKey="cycles" name="Cycle Usage %" fill="#2196F3" />
                  </BarChart>
                </ResponsiveContainer>
              </Box>

              {/* Fleet Table */}
              <TableContainer sx={{ mt: 2 }}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Vehicle</TableCell>
                      <TableCell align="right">SOH</TableCell>
                      <TableCell align="right">Capacity</TableCell>
                      <TableCell align="right">Cycles</TableCell>
                      <TableCell align="right">Degradation</TableCell>
                      <TableCell align="right">Status</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {mockBatteryData.map(b => (
                      <TableRow
                        key={b.vehicleId}
                        sx={{ bgcolor: b.vehicleId === selectedVehicle ? 'action.selected' : 'inherit' }}
                      >
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <DirectionsCar fontSize="small" />
                            {b.vehicleName}
                          </Box>
                        </TableCell>
                        <TableCell align="right">
                          <Typography sx={{ color: getSOHColor(b.stateOfHealth), fontWeight: 600 }}>
                            {b.stateOfHealth}%
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          {b.currentCapacity}/{b.originalCapacity} kWh
                        </TableCell>
                        <TableCell align="right">
                          {b.chargeCycles}/{b.totalChargeCycles}
                        </TableCell>
                        <TableCell align="right">
                          {b.degradationRate}%/mo
                        </TableCell>
                        <TableCell align="right">
                          <Chip
                            size="small"
                            label={getSOHStatus(b.stateOfHealth)}
                            sx={{
                              bgcolor: getSOHColor(b.stateOfHealth),
                              color: 'white',
                            }}
                          />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Paper>
          )}

          {/* Charging Patterns Tab */}
          {selectedTab === 3 && (
            <Paper sx={{ p: 2 }}>
              <Typography variant="h6" gutterBottom>
                Charging Patterns Analysis
              </Typography>
              <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                  <Box sx={{ height: 250 }}>
                    <ResponsiveContainer>
                      <PieChart>
                        <Pie
                          data={[
                            { name: 'Fast Charging', value: currentBattery.chargingPatterns.fastChargePercent, color: '#F44336' },
                            { name: 'Home Charging', value: currentBattery.chargingPatterns.homeChargePercent, color: '#4CAF50' },
                            { name: 'Public Charging', value: currentBattery.chargingPatterns.publicChargePercent, color: '#2196F3' },
                          ]}
                          cx="50%"
                          cy="50%"
                          innerRadius={50}
                          outerRadius={80}
                          dataKey="value"
                          label={({ name, value }) => `${name}: ${value}%`}
                        >
                          <Cell fill="#F44336" />
                          <Cell fill="#4CAF50" />
                          <Cell fill="#2196F3" />
                        </Pie>
                        <RechartsTooltip />
                      </PieChart>
                    </ResponsiveContainer>
                  </Box>
                </Grid>
                <Grid item xs={12} md={6}>
                  <Box sx={{ p: 2, bgcolor: 'action.hover', borderRadius: 1 }}>
                    <Typography variant="subtitle1" gutterBottom fontWeight={600}>
                      Charging Recommendations
                    </Typography>
                    <List dense>
                      <ListItem>
                        <ListItemIcon>
                          {currentBattery.chargingPatterns.fastChargePercent > 30 ? (
                            <Warning color="warning" />
                          ) : (
                            <CheckCircle color="success" />
                          )}
                        </ListItemIcon>
                        <ListItemText
                          primary="Fast Charging Usage"
                          secondary={
                            currentBattery.chargingPatterns.fastChargePercent > 30
                              ? 'Consider reducing fast charging to extend battery life'
                              : 'Fast charging usage is within optimal range'
                          }
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemIcon>
                          <Info color="info" />
                        </ListItemIcon>
                        <ListItemText
                          primary="Optimal Charging Level"
                          secondary="Keep charge between 20-80% for best longevity"
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemIcon>
                          <Thermostat color="error" />
                        </ListItemIcon>
                        <ListItemText
                          primary="Temperature Impact"
                          secondary={
                            currentBattery.averageTemperature > 35
                              ? 'High operating temperature detected - affects battery life'
                              : 'Operating temperature is within normal range'
                          }
                        />
                      </ListItem>
                    </List>
                  </Box>
                </Grid>
              </Grid>
            </Paper>
          )}
        </Grid>
      </Grid>
    </Box>
  );
};

export default BatteryHealthDashboard;

import React, { useState, useMemo } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Paper,
  Button,
  IconButton,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Tabs,
  Tab,
  ToggleButton,
  ToggleButtonGroup,
  Tooltip,
  LinearProgress,
  Divider,
} from '@mui/material';
import {
  EvStation as EvStationIcon,
  BatteryChargingFull as BatteryIcon,
  AttachMoney as MoneyIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Speed as SpeedIcon,
  AccessTime as TimeIcon,
  Bolt as BoltIcon,
  Download as DownloadIcon,
  FilterList as FilterIcon,
  CalendarMonth as CalendarIcon,
  LocalGasStation as GasIcon,
  Savings as SavingsIcon,
  Co2 as Co2Icon,
  Analytics as AnalyticsIcon,
  Eco as EcoIcon,
  Timer as TimerIcon,
} from '@mui/icons-material';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  Legend,
  PieChart,
  Pie,
  Cell,
  AreaChart,
  Area,
  ComposedChart,
} from 'recharts';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';

// Types
interface ChargingSession {
  id: string;
  vehicleId: string;
  vehicleName: string;
  stationId: string;
  stationName: string;
  startTime: Date;
  endTime: Date;
  duration: number; // minutes
  energyDelivered: number; // kWh
  cost: number;
  startSoc: number;
  endSoc: number;
  peakPower: number; // kW
  averagePower: number; // kW
  status: 'completed' | 'in-progress' | 'failed' | 'cancelled';
}

interface DailyStats {
  date: string;
  sessions: number;
  totalEnergy: number;
  totalCost: number;
  avgDuration: number;
  peakHourSessions: number;
  offPeakSessions: number;
}

interface StationUsage {
  stationId: string;
  stationName: string;
  sessions: number;
  totalEnergy: number;
  totalCost: number;
  avgRating: number;
  utilizationRate: number;
}

interface VehicleUsage {
  vehicleId: string;
  vehicleName: string;
  sessions: number;
  totalEnergy: number;
  totalCost: number;
  avgEfficiency: number;
}

const COLORS = ['#2196f3', '#4caf50', '#ff9800', '#f44336', '#9c27b0', '#00bcd4', '#795548', '#607d8b'];

const ChargingAnalyticsDashboard: React.FC = () => {
  // Date range state
  const [startDate, setStartDate] = useState<Date | null>(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000));
  const [endDate, setEndDate] = useState<Date | null>(new Date());
  const [timeRange, setTimeRange] = useState<string>('30d');
  const [activeTab, setActiveTab] = useState(0);
  const [selectedVehicle, setSelectedVehicle] = useState<string>('all');

  // Pagination
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  // Generate mock data
  const sessions: ChargingSession[] = useMemo(() => {
    const data: ChargingSession[] = [];
    const vehicles = [
      { id: 'v1', name: 'Tata Nexon EV - MH01AB1234' },
      { id: 'v2', name: 'MG ZS EV - MH02CD5678' },
      { id: 'v3', name: 'Hyundai Kona - MH03EF9012' },
      { id: 'v4', name: 'Tata Tigor EV - MH04GH3456' },
      { id: 'v5', name: 'BYD e6 - MH05IJ7890' },
    ];
    const stations = [
      { id: 's1', name: 'Tata Power EZ Charge - BKC' },
      { id: 's2', name: 'ChargeZone Hub - Andheri' },
      { id: 's3', name: 'Ather Grid - Powai' },
      { id: 's4', name: 'Jio-bp Pulse - Worli' },
      { id: 's5', name: 'EESL Fast Charger - Lower Parel' },
    ];

    for (let i = 0; i < 150; i++) {
      const vehicle = vehicles[Math.floor(Math.random() * vehicles.length)];
      const station = stations[Math.floor(Math.random() * stations.length)];
      const startTime = new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000);
      const duration = Math.floor(30 + Math.random() * 120);
      const energyDelivered = Math.round((10 + Math.random() * 50) * 10) / 10;
      const startSoc = Math.floor(10 + Math.random() * 30);
      
      data.push({
        id: `session-${i}`,
        vehicleId: vehicle.id,
        vehicleName: vehicle.name,
        stationId: station.id,
        stationName: station.name,
        startTime,
        endTime: new Date(startTime.getTime() + duration * 60 * 1000),
        duration,
        energyDelivered,
        cost: Math.round(energyDelivered * (8 + Math.random() * 6) * 100) / 100,
        startSoc,
        endSoc: Math.min(100, startSoc + Math.floor(energyDelivered / 0.7)),
        peakPower: Math.floor(50 + Math.random() * 100),
        averagePower: Math.floor(30 + Math.random() * 60),
        status: Math.random() > 0.05 ? 'completed' : (Math.random() > 0.5 ? 'failed' : 'cancelled'),
      });
    }

    return data.sort((a, b) => b.startTime.getTime() - a.startTime.getTime());
  }, []);

  // Filter sessions by date range and vehicle
  const filteredSessions = useMemo(() => {
    return sessions.filter(s => {
      if (startDate && s.startTime < startDate) return false;
      if (endDate && s.startTime > endDate) return false;
      if (selectedVehicle !== 'all' && s.vehicleId !== selectedVehicle) return false;
      return true;
    });
  }, [sessions, startDate, endDate, selectedVehicle]);

  // Calculate summary stats
  const summaryStats = useMemo(() => {
    const completed = filteredSessions.filter(s => s.status === 'completed');
    const totalEnergy = completed.reduce((sum, s) => sum + s.energyDelivered, 0);
    const totalCost = completed.reduce((sum, s) => sum + s.cost, 0);
    const totalDuration = completed.reduce((sum, s) => sum + s.duration, 0);
    
    // Estimate fuel savings (assuming 15km/kWh vs 15km/L at ₹100/L)
    const kmDriven = totalEnergy * 6; // Approx 6km per kWh
    const fuelCost = (kmDriven / 15) * 100; // Cost if ICE vehicle
    const fuelSaved = fuelCost - totalCost;
    
    // CO2 savings (0.82 kg CO2 per kWh for grid, 2.3 kg CO2 per liter petrol)
    const gridCo2 = totalEnergy * 0.82;
    const petrolCo2 = (kmDriven / 15) * 2.3;
    const co2Saved = petrolCo2 - gridCo2;

    return {
      totalSessions: completed.length,
      totalEnergy: Math.round(totalEnergy * 10) / 10,
      totalCost: Math.round(totalCost * 100) / 100,
      avgSessionDuration: completed.length ? Math.round(totalDuration / completed.length) : 0,
      avgEnergyCost: totalEnergy ? Math.round((totalCost / totalEnergy) * 100) / 100 : 0,
      avgEnergyPerSession: completed.length ? Math.round((totalEnergy / completed.length) * 10) / 10 : 0,
      fuelSaved: Math.round(fuelSaved * 100) / 100,
      co2Saved: Math.round(co2Saved * 10) / 10,
      failedSessions: filteredSessions.filter(s => s.status === 'failed').length,
      successRate: filteredSessions.length ? Math.round((completed.length / filteredSessions.length) * 100) : 0,
    };
  }, [filteredSessions]);

  // Daily trend data
  const dailyStats: DailyStats[] = useMemo(() => {
    const dailyMap = new Map<string, DailyStats>();
    
    filteredSessions.filter(s => s.status === 'completed').forEach(session => {
      const dateKey = session.startTime.toISOString().split('T')[0];
      const hour = session.startTime.getHours();
      const isPeak = hour >= 9 && hour <= 21;
      
      if (!dailyMap.has(dateKey)) {
        dailyMap.set(dateKey, {
          date: dateKey,
          sessions: 0,
          totalEnergy: 0,
          totalCost: 0,
          avgDuration: 0,
          peakHourSessions: 0,
          offPeakSessions: 0,
        });
      }
      
      const stats = dailyMap.get(dateKey)!;
      stats.sessions++;
      stats.totalEnergy += session.energyDelivered;
      stats.totalCost += session.cost;
      stats.avgDuration += session.duration;
      if (isPeak) {
        stats.peakHourSessions++;
      } else {
        stats.offPeakSessions++;
      }
    });

    return Array.from(dailyMap.values())
      .map(d => ({
        ...d,
        totalEnergy: Math.round(d.totalEnergy * 10) / 10,
        totalCost: Math.round(d.totalCost * 100) / 100,
        avgDuration: Math.round(d.avgDuration / d.sessions),
      }))
      .sort((a, b) => a.date.localeCompare(b.date));
  }, [filteredSessions]);

  // Station usage breakdown
  const stationUsage: StationUsage[] = useMemo(() => {
    const stationMap = new Map<string, StationUsage>();
    
    filteredSessions.filter(s => s.status === 'completed').forEach(session => {
      if (!stationMap.has(session.stationId)) {
        stationMap.set(session.stationId, {
          stationId: session.stationId,
          stationName: session.stationName,
          sessions: 0,
          totalEnergy: 0,
          totalCost: 0,
          avgRating: 4 + Math.random(),
          utilizationRate: 40 + Math.random() * 40,
        });
      }
      
      const stats = stationMap.get(session.stationId)!;
      stats.sessions++;
      stats.totalEnergy += session.energyDelivered;
      stats.totalCost += session.cost;
    });

    return Array.from(stationMap.values())
      .map(s => ({
        ...s,
        totalEnergy: Math.round(s.totalEnergy * 10) / 10,
        totalCost: Math.round(s.totalCost * 100) / 100,
        avgRating: Math.round(s.avgRating * 10) / 10,
        utilizationRate: Math.round(s.utilizationRate),
      }))
      .sort((a, b) => b.totalEnergy - a.totalEnergy);
  }, [filteredSessions]);

  // Vehicle usage breakdown
  const vehicleUsage: VehicleUsage[] = useMemo(() => {
    const vehicleMap = new Map<string, VehicleUsage>();
    
    filteredSessions.filter(s => s.status === 'completed').forEach(session => {
      if (!vehicleMap.has(session.vehicleId)) {
        vehicleMap.set(session.vehicleId, {
          vehicleId: session.vehicleId,
          vehicleName: session.vehicleName,
          sessions: 0,
          totalEnergy: 0,
          totalCost: 0,
          avgEfficiency: 0,
        });
      }
      
      const stats = vehicleMap.get(session.vehicleId)!;
      stats.sessions++;
      stats.totalEnergy += session.energyDelivered;
      stats.totalCost += session.cost;
    });

    return Array.from(vehicleMap.values())
      .map(v => ({
        ...v,
        totalEnergy: Math.round(v.totalEnergy * 10) / 10,
        totalCost: Math.round(v.totalCost * 100) / 100,
        avgEfficiency: Math.round((5.5 + Math.random() * 1.5) * 10) / 10,
      }))
      .sort((a, b) => b.totalEnergy - a.totalEnergy);
  }, [filteredSessions]);

  // Hourly distribution
  const hourlyDistribution = useMemo(() => {
    const hours = Array.from({ length: 24 }, (_, i) => ({
      hour: i,
      sessions: 0,
      energy: 0,
    }));

    filteredSessions.filter(s => s.status === 'completed').forEach(session => {
      const hour = session.startTime.getHours();
      hours[hour].sessions++;
      hours[hour].energy += session.energyDelivered;
    });

    return hours.map(h => ({
      ...h,
      energy: Math.round(h.energy * 10) / 10,
    }));
  }, [filteredSessions]);

  // Export to CSV
  const exportToCSV = () => {
    const headers = ['Date', 'Vehicle', 'Station', 'Duration (min)', 'Energy (kWh)', 'Cost (₹)', 'Start SOC', 'End SOC', 'Status'];
    const rows = filteredSessions.map(s => [
      s.startTime.toISOString(),
      s.vehicleName,
      s.stationName,
      s.duration,
      s.energyDelivered,
      s.cost,
      s.startSoc,
      s.endSoc,
      s.status,
    ]);

    const csv = [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `charging-analytics-${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
  };

  // Handle time range change
  const handleTimeRangeChange = (range: string) => {
    setTimeRange(range);
    const now = new Date();
    switch (range) {
      case '7d':
        setStartDate(new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000));
        break;
      case '30d':
        setStartDate(new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000));
        break;
      case '90d':
        setStartDate(new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000));
        break;
      case 'ytd':
        setStartDate(new Date(now.getFullYear(), 0, 1));
        break;
    }
    setEndDate(now);
  };

  const vehicles = [...new Set(sessions.map(s => s.vehicleId))].map(id => ({
    id,
    name: sessions.find(s => s.vehicleId === id)?.vehicleName || id,
  }));

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Box sx={{ p: 3 }}>
        {/* Header */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Box>
            <Typography variant="h4" gutterBottom>
              <AnalyticsIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Charging Analytics
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Comprehensive insights into your fleet's charging patterns and costs
            </Typography>
          </Box>
          <Button variant="outlined" startIcon={<DownloadIcon />} onClick={exportToCSV}>
            Export CSV
          </Button>
        </Box>

        {/* Filters */}
        <Paper elevation={2} sx={{ p: 2, mb: 3 }}>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={4}>
              <ToggleButtonGroup
                value={timeRange}
                exclusive
                onChange={(_, v) => v && handleTimeRangeChange(v)}
                size="small"
                fullWidth
              >
                <ToggleButton value="7d">7 Days</ToggleButton>
                <ToggleButton value="30d">30 Days</ToggleButton>
                <ToggleButton value="90d">90 Days</ToggleButton>
                <ToggleButton value="ytd">YTD</ToggleButton>
              </ToggleButtonGroup>
            </Grid>
            <Grid item xs={6} md={2}>
              <DatePicker
                label="From"
                value={startDate}
                onChange={setStartDate}
                slotProps={{ textField: { size: 'small', fullWidth: true } }}
              />
            </Grid>
            <Grid item xs={6} md={2}>
              <DatePicker
                label="To"
                value={endDate}
                onChange={setEndDate}
                slotProps={{ textField: { size: 'small', fullWidth: true } }}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <FormControl fullWidth size="small">
                <InputLabel>Vehicle</InputLabel>
                <Select
                  value={selectedVehicle}
                  onChange={(e) => setSelectedVehicle(e.target.value)}
                  label="Vehicle"
                >
                  <MenuItem value="all">All Vehicles</MenuItem>
                  {vehicles.map(v => (
                    <MenuItem key={v.id} value={v.id}>{v.name}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </Paper>

        {/* Summary KPIs */}
        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid item xs={6} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Total Sessions
                    </Typography>
                    <Typography variant="h4">{summaryStats.totalSessions}</Typography>
                    <Chip
                      size="small"
                      label={`${summaryStats.successRate}% success`}
                      color={summaryStats.successRate >= 95 ? 'success' : 'warning'}
                      sx={{ mt: 1 }}
                    />
                  </Box>
                  <EvStationIcon sx={{ fontSize: 40, color: 'primary.main', opacity: 0.7 }} />
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={6} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Total Energy
                    </Typography>
                    <Typography variant="h4">{summaryStats.totalEnergy} kWh</Typography>
                    <Typography variant="caption" color="text.secondary">
                      Avg {summaryStats.avgEnergyPerSession} kWh/session
                    </Typography>
                  </Box>
                  <BoltIcon sx={{ fontSize: 40, color: 'warning.main', opacity: 0.7 }} />
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={6} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Total Cost
                    </Typography>
                    <Typography variant="h4">₹{summaryStats.totalCost.toLocaleString()}</Typography>
                    <Typography variant="caption" color="text.secondary">
                      Avg ₹{summaryStats.avgEnergyCost}/kWh
                    </Typography>
                  </Box>
                  <MoneyIcon sx={{ fontSize: 40, color: 'error.main', opacity: 0.7 }} />
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={6} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Avg Session Time
                    </Typography>
                    <Typography variant="h4">{summaryStats.avgSessionDuration} min</Typography>
                    <Typography variant="caption" color="text.secondary">
                      ~{Math.round(summaryStats.avgSessionDuration / 60 * 10) / 10} hours
                    </Typography>
                  </Box>
                  <TimerIcon sx={{ fontSize: 40, color: 'info.main', opacity: 0.7 }} />
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        {/* Environmental Impact */}
        <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            <EcoIcon sx={{ mr: 1, verticalAlign: 'middle', color: 'success.main' }} />
            Environmental & Cost Impact
          </Typography>
          <Grid container spacing={3}>
            <Grid item xs={12} md={4}>
              <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'success.light', borderRadius: 2 }}>
                <SavingsIcon sx={{ fontSize: 48, color: 'success.dark', mb: 1 }} />
                <Typography variant="h4" color="success.dark">
                  ₹{summaryStats.fuelSaved.toLocaleString()}
                </Typography>
                <Typography variant="body2" color="success.dark">
                  Fuel Cost Savings
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  vs. equivalent petrol consumption
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={12} md={4}>
              <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'info.light', borderRadius: 2 }}>
                <Co2Icon sx={{ fontSize: 48, color: 'info.dark', mb: 1 }} />
                <Typography variant="h4" color="info.dark">
                  {summaryStats.co2Saved} kg
                </Typography>
                <Typography variant="body2" color="info.dark">
                  CO₂ Emissions Avoided
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Equivalent to planting {Math.round(summaryStats.co2Saved / 21)} trees
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={12} md={4}>
              <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'warning.light', borderRadius: 2 }}>
                <GasIcon sx={{ fontSize: 48, color: 'warning.dark', mb: 1 }} />
                <Typography variant="h4" color="warning.dark">
                  {Math.round(summaryStats.totalEnergy * 6 / 15)} L
                </Typography>
                <Typography variant="body2" color="warning.dark">
                  Petrol Saved
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Based on avg 15 km/L efficiency
                </Typography>
              </Box>
            </Grid>
          </Grid>
        </Paper>

        {/* Tabs for different views */}
        <Paper elevation={2} sx={{ mb: 3 }}>
          <Tabs value={activeTab} onChange={(_, v) => setActiveTab(v)}>
            <Tab label="Trends" />
            <Tab label="By Station" />
            <Tab label="By Vehicle" />
            <Tab label="Hourly Pattern" />
            <Tab label="Session History" />
          </Tabs>

          <Box sx={{ p: 3 }}>
            {/* Trends Tab */}
            {activeTab === 0 && (
              <Box>
                <Typography variant="h6" gutterBottom>Daily Charging Trends</Typography>
                <Box sx={{ height: 350 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <ComposedChart data={dailyStats}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="date" />
                      <YAxis yAxisId="left" label={{ value: 'kWh', angle: -90, position: 'insideLeft' }} />
                      <YAxis yAxisId="right" orientation="right" label={{ value: '₹', angle: 90, position: 'insideRight' }} />
                      <RechartsTooltip />
                      <Legend />
                      <Bar yAxisId="left" dataKey="totalEnergy" name="Energy (kWh)" fill="#2196f3" />
                      <Line yAxisId="right" type="monotone" dataKey="totalCost" name="Cost (₹)" stroke="#f44336" strokeWidth={2} />
                    </ComposedChart>
                  </ResponsiveContainer>
                </Box>

                <Divider sx={{ my: 3 }} />

                <Typography variant="h6" gutterBottom>Peak vs Off-Peak Charging</Typography>
                <Box sx={{ height: 300 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={dailyStats}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="date" />
                      <YAxis />
                      <RechartsTooltip />
                      <Legend />
                      <Bar dataKey="peakHourSessions" name="Peak Hours" fill="#f44336" stackId="a" />
                      <Bar dataKey="offPeakSessions" name="Off-Peak Hours" fill="#4caf50" stackId="a" />
                    </BarChart>
                  </ResponsiveContainer>
                </Box>
              </Box>
            )}

            {/* By Station Tab */}
            {activeTab === 1 && (
              <Box>
                <Grid container spacing={3}>
                  <Grid item xs={12} md={6}>
                    <Typography variant="h6" gutterBottom>Energy by Station</Typography>
                    <Box sx={{ height: 300 }}>
                      <ResponsiveContainer width="100%" height="100%">
                        <PieChart>
                          <Pie
                            data={stationUsage}
                            dataKey="totalEnergy"
                            nameKey="stationName"
                            cx="50%"
                            cy="50%"
                            outerRadius={100}
                            label={({ name, percent }) => `${name.split('-')[0]} (${(percent * 100).toFixed(0)}%)`}
                          >
                            {stationUsage.map((_, index) => (
                              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                            ))}
                          </Pie>
                          <RechartsTooltip formatter={(value: number) => `${value} kWh`} />
                        </PieChart>
                      </ResponsiveContainer>
                    </Box>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Typography variant="h6" gutterBottom>Station Performance</Typography>
                    <TableContainer>
                      <Table size="small">
                        <TableHead>
                          <TableRow>
                            <TableCell>Station</TableCell>
                            <TableCell align="right">Sessions</TableCell>
                            <TableCell align="right">Energy</TableCell>
                            <TableCell align="right">Cost</TableCell>
                            <TableCell align="right">Utilization</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {stationUsage.map((station) => (
                            <TableRow key={station.stationId}>
                              <TableCell>{station.stationName}</TableCell>
                              <TableCell align="right">{station.sessions}</TableCell>
                              <TableCell align="right">{station.totalEnergy} kWh</TableCell>
                              <TableCell align="right">₹{station.totalCost}</TableCell>
                              <TableCell align="right">
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                  <LinearProgress
                                    variant="determinate"
                                    value={station.utilizationRate}
                                    sx={{ width: 50 }}
                                    color={station.utilizationRate > 70 ? 'error' : station.utilizationRate > 40 ? 'warning' : 'success'}
                                  />
                                  <Typography variant="caption">{station.utilizationRate}%</Typography>
                                </Box>
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  </Grid>
                </Grid>
              </Box>
            )}

            {/* By Vehicle Tab */}
            {activeTab === 2 && (
              <Box>
                <Typography variant="h6" gutterBottom>Vehicle Charging Summary</Typography>
                <Box sx={{ height: 300, mb: 3 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={vehicleUsage} layout="vertical">
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis type="number" />
                      <YAxis dataKey="vehicleName" type="category" width={150} tick={{ fontSize: 11 }} />
                      <RechartsTooltip />
                      <Legend />
                      <Bar dataKey="totalEnergy" name="Energy (kWh)" fill="#2196f3" />
                    </BarChart>
                  </ResponsiveContainer>
                </Box>

                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Vehicle</TableCell>
                        <TableCell align="right">Sessions</TableCell>
                        <TableCell align="right">Total Energy</TableCell>
                        <TableCell align="right">Total Cost</TableCell>
                        <TableCell align="right">Avg Efficiency</TableCell>
                        <TableCell align="right">Cost/kWh</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {vehicleUsage.map((vehicle) => (
                        <TableRow key={vehicle.vehicleId}>
                          <TableCell>{vehicle.vehicleName}</TableCell>
                          <TableCell align="right">{vehicle.sessions}</TableCell>
                          <TableCell align="right">{vehicle.totalEnergy} kWh</TableCell>
                          <TableCell align="right">₹{vehicle.totalCost.toLocaleString()}</TableCell>
                          <TableCell align="right">{vehicle.avgEfficiency} km/kWh</TableCell>
                          <TableCell align="right">₹{(vehicle.totalCost / vehicle.totalEnergy).toFixed(2)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Box>
            )}

            {/* Hourly Pattern Tab */}
            {activeTab === 3 && (
              <Box>
                <Typography variant="h6" gutterBottom>Charging Pattern by Hour of Day</Typography>
                <Box sx={{ height: 350 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={hourlyDistribution}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="hour" tickFormatter={(h) => `${h}:00`} />
                      <YAxis />
                      <RechartsTooltip
                        labelFormatter={(h) => `${h}:00 - ${h}:59`}
                        formatter={(value: number, name: string) => [
                          name === 'sessions' ? value : `${value} kWh`,
                          name === 'sessions' ? 'Sessions' : 'Energy'
                        ]}
                      />
                      <Legend />
                      <Area type="monotone" dataKey="sessions" name="Sessions" fill="#2196f3" stroke="#1976d2" fillOpacity={0.6} />
                      <Area type="monotone" dataKey="energy" name="Energy (kWh)" fill="#4caf50" stroke="#388e3c" fillOpacity={0.4} />
                    </AreaChart>
                  </ResponsiveContainer>
                </Box>
                <Box sx={{ mt: 2, display: 'flex', justifyContent: 'center', gap: 4 }}>
                  <Box sx={{ textAlign: 'center' }}>
                    <Typography variant="h6" color="error.main">
                      {hourlyDistribution.filter(h => h.hour >= 9 && h.hour <= 21).reduce((s, h) => s + h.sessions, 0)}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">Peak Hour Sessions (9AM-9PM)</Typography>
                  </Box>
                  <Box sx={{ textAlign: 'center' }}>
                    <Typography variant="h6" color="success.main">
                      {hourlyDistribution.filter(h => h.hour < 9 || h.hour > 21).reduce((s, h) => s + h.sessions, 0)}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">Off-Peak Sessions</Typography>
                  </Box>
                </Box>
              </Box>
            )}

            {/* Session History Tab */}
            {activeTab === 4 && (
              <Box>
                <Typography variant="h6" gutterBottom>Session History</Typography>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Date</TableCell>
                        <TableCell>Vehicle</TableCell>
                        <TableCell>Station</TableCell>
                        <TableCell align="right">Duration</TableCell>
                        <TableCell align="right">Energy</TableCell>
                        <TableCell align="right">SOC</TableCell>
                        <TableCell align="right">Cost</TableCell>
                        <TableCell align="center">Status</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {filteredSessions
                        .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                        .map((session) => (
                          <TableRow key={session.id}>
                            <TableCell>
                              {session.startTime.toLocaleDateString()} {session.startTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </TableCell>
                            <TableCell>{session.vehicleName}</TableCell>
                            <TableCell>{session.stationName}</TableCell>
                            <TableCell align="right">{session.duration} min</TableCell>
                            <TableCell align="right">{session.energyDelivered} kWh</TableCell>
                            <TableCell align="right">
                              {session.startSoc}% → {session.endSoc}%
                            </TableCell>
                            <TableCell align="right">₹{session.cost}</TableCell>
                            <TableCell align="center">
                              <Chip
                                size="small"
                                label={session.status}
                                color={
                                  session.status === 'completed' ? 'success' :
                                  session.status === 'in-progress' ? 'info' :
                                  session.status === 'failed' ? 'error' : 'default'
                                }
                              />
                            </TableCell>
                          </TableRow>
                        ))}
                    </TableBody>
                  </Table>
                </TableContainer>
                <TablePagination
                  component="div"
                  count={filteredSessions.length}
                  page={page}
                  onPageChange={(_, p) => setPage(p)}
                  rowsPerPage={rowsPerPage}
                  onRowsPerPageChange={(e) => {
                    setRowsPerPage(parseInt(e.target.value, 10));
                    setPage(0);
                  }}
                />
              </Box>
            )}
          </Box>
        </Paper>
      </Box>
    </LocalizationProvider>
  );
};

export default ChargingAnalyticsDashboard;

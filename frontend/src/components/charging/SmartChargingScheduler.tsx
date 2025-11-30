import React, { useState, useEffect, useMemo } from 'react';
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
  Slider,
  Switch,
  FormControlLabel,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TableSortLabel,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  LinearProgress,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  ListItemSecondaryAction,
  Collapse,
  Badge,
  ToggleButton,
  ToggleButtonGroup,
  Rating,
} from '@mui/material';
import {
  AttachMoney as MoneyIcon,
  EvStation as EvStationIcon,
  Schedule as ScheduleIcon,
  Speed as SpeedIcon,
  TrendingDown as TrendingDownIcon,
  TrendingUp as TrendingUpIcon,
  LocationOn as LocationIcon,
  Star as StarIcon,
  Timer as TimerIcon,
  BatteryChargingFull as BatteryIcon,
  Bolt as BoltIcon,
  LocalOffer as OfferIcon,
  AccessTime as AccessTimeIcon,
  Navigation as NavigationIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
  Savings as SavingsIcon,
  CheckCircle as CheckCircleIcon,
  Warning as WarningIcon,
  ShowChart as ChartIcon,
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
  Area,
  AreaChart,
} from 'recharts';

// Types
interface PriceData {
  hour: number;
  price: number;
  demand: 'low' | 'medium' | 'high';
  renewable: number;
}

interface ChargingStation {
  id: string;
  name: string;
  operator: string;
  address: string;
  distance: number;
  pricePerKwh: number;
  currentWaitTime: number;
  availablePorts: number;
  totalPorts: number;
  maxPowerKw: number;
  rating: number;
  reviews: number;
  timeOfUsePricing: boolean;
  peakPrice: number;
  offPeakPrice: number;
  peakHours: string;
  offPeakHours: string;
  hasReservation: boolean;
  amenities: string[];
  estimatedCost?: number;
  estimatedTime?: number;
}

interface ChargingSchedule {
  id: string;
  stationId: string;
  stationName: string;
  scheduledTime: Date;
  duration: number;
  estimatedCost: number;
  kwhRequired: number;
  status: 'scheduled' | 'in-progress' | 'completed' | 'cancelled';
  savings: number;
}

type SortField = 'price' | 'distance' | 'waitTime' | 'power' | 'rating';
type SortOrder = 'asc' | 'desc';

const SmartChargingScheduler: React.FC = () => {
  // Vehicle and charging requirements
  const [currentSoc, setCurrentSoc] = useState<number>(25);
  const [targetSoc, setTargetSoc] = useState<number>(80);
  const [batteryCapacity, setBatteryCapacity] = useState<number>(75);
  const [departureTime, setDepartureTime] = useState<string>('');
  const [vehicleId, setVehicleId] = useState<string>('');

  // Station filtering and sorting
  const [sortField, setSortField] = useState<SortField>('price');
  const [sortOrder, setSortOrder] = useState<SortOrder>('asc');
  const [showTimeOfUseOnly, setShowTimeOfUseOnly] = useState(false);
  const [maxDistance, setMaxDistance] = useState<number>(20);
  const [minPower, setMinPower] = useState<number>(50);

  // UI state
  const [selectedStation, setSelectedStation] = useState<ChargingStation | null>(null);
  const [expandedStation, setExpandedStation] = useState<string | null>(null);
  const [scheduleDialogOpen, setScheduleDialogOpen] = useState(false);
  const [schedules, setSchedules] = useState<ChargingSchedule[]>([]);
  const [viewMode, setViewMode] = useState<'list' | 'grid'>('list');

  // Price data for chart
  const [priceData, setPriceData] = useState<PriceData[]>([]);

  // Generate mock price data for 24 hours
  useEffect(() => {
    const now = new Date();
    const data: PriceData[] = [];
    for (let i = 0; i < 24; i++) {
      const hour = (now.getHours() + i) % 24;
      const isPeak = hour >= 9 && hour <= 12 || hour >= 18 && hour <= 21;
      const isShoulder = hour >= 6 && hour <= 9 || hour >= 12 && hour <= 18 || hour >= 21 && hour <= 23;
      const basePrice = 8;
      const price = isPeak
        ? basePrice + 6 + Math.random() * 2
        : isShoulder
        ? basePrice + 2 + Math.random() * 2
        : basePrice + Math.random() * 1;
      data.push({
        hour,
        price: Math.round(price * 100) / 100,
        demand: isPeak ? 'high' : isShoulder ? 'medium' : 'low',
        renewable: Math.round((isPeak ? 30 : isShoulder ? 50 : 70) + Math.random() * 20),
      });
    }
    setPriceData(data);
  }, []);

  // Mock charging stations
  const stations: ChargingStation[] = useMemo(() => [
    {
      id: '1',
      name: 'Tata Power EZ Charge - BKC',
      operator: 'Tata Power',
      address: 'BKC, Bandra Kurla Complex',
      distance: 2.5,
      pricePerKwh: 12.50,
      currentWaitTime: 0,
      availablePorts: 3,
      totalPorts: 6,
      maxPowerKw: 60,
      rating: 4.5,
      reviews: 156,
      timeOfUsePricing: true,
      peakPrice: 15.00,
      offPeakPrice: 9.00,
      peakHours: '9 AM - 12 PM, 6 PM - 9 PM',
      offPeakHours: '12 AM - 6 AM',
      hasReservation: true,
      amenities: ['WiFi', 'Restroom', 'Café', 'Parking'],
    },
    {
      id: '2',
      name: 'ChargeZone Hub - Andheri',
      operator: 'ChargeZone',
      address: 'Andheri East, Mumbai',
      distance: 5.8,
      pricePerKwh: 10.00,
      currentWaitTime: 15,
      availablePorts: 1,
      totalPorts: 4,
      maxPowerKw: 120,
      rating: 4.2,
      reviews: 89,
      timeOfUsePricing: true,
      peakPrice: 13.00,
      offPeakPrice: 7.50,
      peakHours: '8 AM - 11 AM, 5 PM - 10 PM',
      offPeakHours: '11 PM - 7 AM',
      hasReservation: true,
      amenities: ['WiFi', 'Parking', 'Security'],
    },
    {
      id: '3',
      name: 'Ather Grid - Powai',
      operator: 'Ather Energy',
      address: 'Hiranandani Gardens, Powai',
      distance: 8.2,
      pricePerKwh: 8.50,
      currentWaitTime: 0,
      availablePorts: 2,
      totalPorts: 2,
      maxPowerKw: 22,
      rating: 4.7,
      reviews: 234,
      timeOfUsePricing: false,
      peakPrice: 8.50,
      offPeakPrice: 8.50,
      peakHours: 'N/A',
      offPeakHours: '24/7 Same Price',
      hasReservation: false,
      amenities: ['WiFi', 'Parking'],
    },
    {
      id: '4',
      name: 'Jio-bp Pulse - Worli',
      operator: 'Jio-bp',
      address: 'Worli Sea Face, Mumbai',
      distance: 4.1,
      pricePerKwh: 14.00,
      currentWaitTime: 30,
      availablePorts: 0,
      totalPorts: 8,
      maxPowerKw: 150,
      rating: 4.0,
      reviews: 67,
      timeOfUsePricing: true,
      peakPrice: 18.00,
      offPeakPrice: 10.00,
      peakHours: '9 AM - 1 PM, 6 PM - 10 PM',
      offPeakHours: '1 AM - 6 AM',
      hasReservation: true,
      amenities: ['WiFi', 'Restroom', 'Café', 'Parking', 'Lounge'],
    },
    {
      id: '5',
      name: 'EESL Fast Charger - Lower Parel',
      operator: 'EESL',
      address: 'Lower Parel, Mumbai',
      distance: 3.3,
      pricePerKwh: 11.00,
      currentWaitTime: 5,
      availablePorts: 2,
      totalPorts: 4,
      maxPowerKw: 50,
      rating: 3.8,
      reviews: 45,
      timeOfUsePricing: false,
      peakPrice: 11.00,
      offPeakPrice: 11.00,
      peakHours: 'N/A',
      offPeakHours: '24/7 Same Price',
      hasReservation: false,
      amenities: ['Parking'],
    },
    {
      id: '6',
      name: 'Statiq Station - Juhu',
      operator: 'Statiq',
      address: 'Juhu Beach Road, Mumbai',
      distance: 6.7,
      pricePerKwh: 9.50,
      currentWaitTime: 0,
      availablePorts: 4,
      totalPorts: 4,
      maxPowerKw: 60,
      rating: 4.3,
      reviews: 112,
      timeOfUsePricing: true,
      peakPrice: 12.00,
      offPeakPrice: 7.00,
      peakHours: '10 AM - 12 PM, 7 PM - 9 PM',
      offPeakHours: '12 AM - 7 AM',
      hasReservation: true,
      amenities: ['WiFi', 'Café', 'Parking', 'Beach View'],
    },
  ], []);

  // Calculate charging requirements
  const kwhRequired = useMemo(() => {
    return ((targetSoc - currentSoc) / 100) * batteryCapacity;
  }, [currentSoc, targetSoc, batteryCapacity]);

  // Calculate estimated cost and time for each station
  const stationsWithEstimates = useMemo(() => {
    return stations.map(station => {
      const estimatedTime = (kwhRequired / station.maxPowerKw) * 60; // minutes
      const estimatedCost = kwhRequired * station.pricePerKwh;
      return {
        ...station,
        estimatedCost: Math.round(estimatedCost * 100) / 100,
        estimatedTime: Math.round(estimatedTime),
      };
    });
  }, [stations, kwhRequired]);

  // Filter and sort stations
  const filteredStations = useMemo(() => {
    let result = stationsWithEstimates.filter(s => {
      if (showTimeOfUseOnly && !s.timeOfUsePricing) return false;
      if (s.distance > maxDistance) return false;
      if (s.maxPowerKw < minPower) return false;
      return true;
    });

    result.sort((a, b) => {
      let comparison = 0;
      switch (sortField) {
        case 'price':
          comparison = (a.estimatedCost || 0) - (b.estimatedCost || 0);
          break;
        case 'distance':
          comparison = a.distance - b.distance;
          break;
        case 'waitTime':
          comparison = a.currentWaitTime - b.currentWaitTime;
          break;
        case 'power':
          comparison = b.maxPowerKw - a.maxPowerKw;
          break;
        case 'rating':
          comparison = b.rating - a.rating;
          break;
      }
      return sortOrder === 'asc' ? comparison : -comparison;
    });

    return result;
  }, [stationsWithEstimates, showTimeOfUseOnly, maxDistance, minPower, sortField, sortOrder]);

  // Find cheapest station and calculate potential savings
  const cheapestStation = useMemo(() => {
    return [...filteredStations].sort((a, b) => (a.estimatedCost || 0) - (b.estimatedCost || 0))[0];
  }, [filteredStations]);

  const mostExpensiveStation = useMemo(() => {
    return [...filteredStations].sort((a, b) => (b.estimatedCost || 0) - (a.estimatedCost || 0))[0];
  }, [filteredStations]);

  const potentialSavings = useMemo(() => {
    if (!cheapestStation || !mostExpensiveStation) return 0;
    return (mostExpensiveStation.estimatedCost || 0) - (cheapestStation.estimatedCost || 0);
  }, [cheapestStation, mostExpensiveStation]);

  // Find best off-peak time
  const bestChargingTime = useMemo(() => {
    const lowestPrice = Math.min(...priceData.map(p => p.price));
    const bestHour = priceData.find(p => p.price === lowestPrice);
    return bestHour;
  }, [priceData]);

  // Schedule charging session
  const handleScheduleCharging = () => {
    if (!selectedStation) return;

    const newSchedule: ChargingSchedule = {
      id: `schedule-${Date.now()}`,
      stationId: selectedStation.id,
      stationName: selectedStation.name,
      scheduledTime: departureTime ? new Date(departureTime) : new Date(),
      duration: selectedStation.estimatedTime || 60,
      estimatedCost: selectedStation.estimatedCost || 0,
      kwhRequired,
      status: 'scheduled',
      savings: selectedStation.estimatedCost && mostExpensiveStation?.estimatedCost
        ? mostExpensiveStation.estimatedCost - selectedStation.estimatedCost
        : 0,
    };

    setSchedules(prev => [...prev, newSchedule]);
    setScheduleDialogOpen(false);
    setSelectedStation(null);
  };

  // Navigate to station
  const navigateToStation = (station: ChargingStation) => {
    window.open(`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(station.address)}`, '_blank');
  };

  // Get demand color
  const getDemandColor = (demand: string): 'success' | 'warning' | 'error' => {
    switch (demand) {
      case 'low': return 'success';
      case 'medium': return 'warning';
      case 'high': return 'error';
      default: return 'warning';
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Typography variant="h4" gutterBottom>
        Smart Charging Scheduler
      </Typography>
      <Typography variant="body2" color="text.secondary" gutterBottom>
        Find the best charging option based on price, time, and your schedule
      </Typography>

      {/* Charging Requirements */}
      <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          <BatteryIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
          Charging Requirements
        </Typography>
        <Grid container spacing={3}>
          <Grid item xs={12} md={3}>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Current SOC: {currentSoc}%
            </Typography>
            <Slider
              value={currentSoc}
              onChange={(_, v) => setCurrentSoc(v as number)}
              min={0}
              max={100}
              valueLabelDisplay="auto"
              color="primary"
            />
          </Grid>
          <Grid item xs={12} md={3}>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Target SOC: {targetSoc}%
            </Typography>
            <Slider
              value={targetSoc}
              onChange={(_, v) => setTargetSoc(v as number)}
              min={0}
              max={100}
              valueLabelDisplay="auto"
              color="success"
            />
          </Grid>
          <Grid item xs={12} md={3}>
            <TextField
              fullWidth
              label="Battery Capacity (kWh)"
              type="number"
              value={batteryCapacity}
              onChange={(e) => setBatteryCapacity(Number(e.target.value))}
              size="small"
            />
          </Grid>
          <Grid item xs={12} md={3}>
            <TextField
              fullWidth
              label="Departure Time"
              type="datetime-local"
              value={departureTime}
              onChange={(e) => setDepartureTime(e.target.value)}
              size="small"
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
        </Grid>
        <Box sx={{ mt: 2, display: 'flex', gap: 2, alignItems: 'center' }}>
          <Chip
            icon={<BoltIcon />}
            label={`${kwhRequired.toFixed(1)} kWh required`}
            color="primary"
            variant="outlined"
          />
          {cheapestStation && (
            <Chip
              icon={<SavingsIcon />}
              label={`Save up to ₹${potentialSavings.toFixed(2)}`}
              color="success"
              variant="filled"
            />
          )}
          {bestChargingTime && (
            <Chip
              icon={<AccessTimeIcon />}
              label={`Best time: ${bestChargingTime.hour}:00 (₹${bestChargingTime.price}/kWh)`}
              color="info"
              variant="outlined"
            />
          )}
        </Box>
      </Paper>

      {/* Price Chart */}
      <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          <ChartIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
          24-Hour Price Forecast
        </Typography>
        <Box sx={{ height: 250 }}>
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={priceData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis
                dataKey="hour"
                tickFormatter={(hour) => `${hour}:00`}
              />
              <YAxis
                domain={[6, 20]}
                tickFormatter={(value) => `₹${value}`}
              />
              <RechartsTooltip
                formatter={(value: number) => [`₹${value.toFixed(2)}`, 'Price']}
                labelFormatter={(hour) => `${hour}:00`}
                content={({ active, payload, label }) => {
                  if (active && payload && payload.length) {
                    const data = payload[0].payload;
                    return (
                      <Paper sx={{ p: 1 }}>
                        <Typography variant="body2"><strong>{label}:00</strong></Typography>
                        <Typography variant="body2">Price: ₹{data.price}/kWh</Typography>
                        <Typography variant="body2">Demand: <Chip size="small" label={data.demand} color={getDemandColor(data.demand)} /></Typography>
                        <Typography variant="body2">Renewable: {data.renewable}%</Typography>
                      </Paper>
                    );
                  }
                  return null;
                }}
              />
              <Area
                type="monotone"
                dataKey="price"
                stroke="#1976d2"
                fill="url(#colorPrice)"
              />
              <defs>
                <linearGradient id="colorPrice" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#1976d2" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#1976d2" stopOpacity={0} />
                </linearGradient>
              </defs>
            </AreaChart>
          </ResponsiveContainer>
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'center', gap: 3, mt: 1 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <Box sx={{ width: 12, height: 12, bgcolor: 'success.main', borderRadius: '50%' }} />
            <Typography variant="caption">Off-Peak (Lowest)</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <Box sx={{ width: 12, height: 12, bgcolor: 'warning.main', borderRadius: '50%' }} />
            <Typography variant="caption">Shoulder</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <Box sx={{ width: 12, height: 12, bgcolor: 'error.main', borderRadius: '50%' }} />
            <Typography variant="caption">Peak (Highest)</Typography>
          </Box>
        </Box>
      </Paper>

      {/* Filters */}
      <Paper elevation={2} sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} md={2}>
            <FormControl fullWidth size="small">
              <InputLabel>Sort By</InputLabel>
              <Select
                value={sortField}
                onChange={(e) => setSortField(e.target.value as SortField)}
                label="Sort By"
              >
                <MenuItem value="price">Price</MenuItem>
                <MenuItem value="distance">Distance</MenuItem>
                <MenuItem value="waitTime">Wait Time</MenuItem>
                <MenuItem value="power">Charging Power</MenuItem>
                <MenuItem value="rating">Rating</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} md={2}>
            <ToggleButtonGroup
              value={sortOrder}
              exclusive
              onChange={(_, v) => v && setSortOrder(v)}
              size="small"
              fullWidth
            >
              <ToggleButton value="asc">
                <TrendingDownIcon fontSize="small" sx={{ mr: 0.5 }} />
                Low First
              </ToggleButton>
              <ToggleButton value="desc">
                <TrendingUpIcon fontSize="small" sx={{ mr: 0.5 }} />
                High First
              </ToggleButton>
            </ToggleButtonGroup>
          </Grid>
          <Grid item xs={6} md={2}>
            <Typography variant="caption" color="text.secondary">
              Max Distance: {maxDistance} km
            </Typography>
            <Slider
              value={maxDistance}
              onChange={(_, v) => setMaxDistance(v as number)}
              min={5}
              max={50}
              size="small"
            />
          </Grid>
          <Grid item xs={6} md={2}>
            <Typography variant="caption" color="text.secondary">
              Min Power: {minPower} kW
            </Typography>
            <Slider
              value={minPower}
              onChange={(_, v) => setMinPower(v as number)}
              min={0}
              max={150}
              step={10}
              size="small"
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end', flexWrap: 'wrap' }}>
              <FormControlLabel
                control={
                  <Switch
                    checked={showTimeOfUseOnly}
                    onChange={(e) => setShowTimeOfUseOnly(e.target.checked)}
                    size="small"
                  />
                }
                label="Time-of-Use Pricing"
              />
              <ToggleButtonGroup
                value={viewMode}
                exclusive
                onChange={(_, v) => v && setViewMode(v)}
                size="small"
              >
                <ToggleButton value="list">List</ToggleButton>
                <ToggleButton value="grid">Grid</ToggleButton>
              </ToggleButtonGroup>
            </Box>
          </Grid>
        </Grid>
      </Paper>

      {/* Station comparison */}
      {viewMode === 'list' ? (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Station</TableCell>
                <TableCell align="right">
                  <TableSortLabel
                    active={sortField === 'price'}
                    direction={sortOrder}
                    onClick={() => {
                      setSortField('price');
                      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
                    }}
                  >
                    Est. Cost
                  </TableSortLabel>
                </TableCell>
                <TableCell align="right">
                  <TableSortLabel
                    active={sortField === 'distance'}
                    direction={sortOrder}
                    onClick={() => {
                      setSortField('distance');
                      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
                    }}
                  >
                    Distance
                  </TableSortLabel>
                </TableCell>
                <TableCell align="center">Availability</TableCell>
                <TableCell align="right">
                  <TableSortLabel
                    active={sortField === 'power'}
                    direction={sortOrder}
                    onClick={() => {
                      setSortField('power');
                      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
                    }}
                  >
                    Power
                  </TableSortLabel>
                </TableCell>
                <TableCell align="right">Est. Time</TableCell>
                <TableCell align="center">
                  <TableSortLabel
                    active={sortField === 'rating'}
                    direction={sortOrder}
                    onClick={() => {
                      setSortField('rating');
                      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
                    }}
                  >
                    Rating
                  </TableSortLabel>
                </TableCell>
                <TableCell align="center">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredStations.map((station, index) => (
                <React.Fragment key={station.id}>
                  <TableRow
                    hover
                    sx={{
                      bgcolor: station.id === cheapestStation?.id ? 'success.light' : undefined,
                      cursor: 'pointer',
                    }}
                    onClick={() => setExpandedStation(expandedStation === station.id ? null : station.id)}
                  >
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        {index === 0 && sortField === 'price' && sortOrder === 'asc' && (
                          <Chip label="Best Price" color="success" size="small" />
                        )}
                        <Box>
                          <Typography variant="body2" fontWeight="medium">
                            {station.name}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {station.operator}
                          </Typography>
                        </Box>
                      </Box>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="body2" fontWeight="bold" color={station.id === cheapestStation?.id ? 'success.main' : 'text.primary'}>
                        ₹{station.estimatedCost?.toFixed(2)}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        ₹{station.pricePerKwh}/kWh
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      {station.distance} km
                    </TableCell>
                    <TableCell align="center">
                      <Chip
                        size="small"
                        label={`${station.availablePorts}/${station.totalPorts}`}
                        color={station.availablePorts > 0 ? 'success' : 'error'}
                      />
                      {station.currentWaitTime > 0 && (
                        <Typography variant="caption" color="warning.main" display="block">
                          ~{station.currentWaitTime} min wait
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell align="right">
                      {station.maxPowerKw} kW
                    </TableCell>
                    <TableCell align="right">
                      {station.estimatedTime} min
                    </TableCell>
                    <TableCell align="center">
                      <Rating value={station.rating} precision={0.5} size="small" readOnly />
                      <Typography variant="caption" color="text.secondary" display="block">
                        ({station.reviews})
                      </Typography>
                    </TableCell>
                    <TableCell align="center">
                      <Box sx={{ display: 'flex', gap: 0.5 }}>
                        <Tooltip title="Navigate">
                          <IconButton size="small" onClick={(e) => { e.stopPropagation(); navigateToStation(station); }}>
                            <NavigationIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Schedule">
                          <IconButton
                            size="small"
                            color="primary"
                            onClick={(e) => {
                              e.stopPropagation();
                              setSelectedStation(station);
                              setScheduleDialogOpen(true);
                            }}
                          >
                            <ScheduleIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <IconButton size="small">
                          {expandedStation === station.id ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                        </IconButton>
                      </Box>
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell colSpan={8} sx={{ py: 0 }}>
                      <Collapse in={expandedStation === station.id} timeout="auto" unmountOnExit>
                        <Box sx={{ p: 2 }}>
                          <Grid container spacing={2}>
                            <Grid item xs={12} md={6}>
                              <Typography variant="subtitle2" gutterBottom>
                                Time-of-Use Pricing
                              </Typography>
                              {station.timeOfUsePricing ? (
                                <Box>
                                  <Typography variant="body2">
                                    Peak: ₹{station.peakPrice}/kWh ({station.peakHours})
                                  </Typography>
                                  <Typography variant="body2" color="success.main">
                                    Off-Peak: ₹{station.offPeakPrice}/kWh ({station.offPeakHours})
                                  </Typography>
                                </Box>
                              ) : (
                                <Typography variant="body2" color="text.secondary">
                                  Flat rate pricing - no time-of-use variations
                                </Typography>
                              )}
                            </Grid>
                            <Grid item xs={12} md={6}>
                              <Typography variant="subtitle2" gutterBottom>
                                Amenities
                              </Typography>
                              <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                                {station.amenities.map((amenity) => (
                                  <Chip key={amenity} label={amenity} size="small" variant="outlined" />
                                ))}
                              </Box>
                            </Grid>
                          </Grid>
                        </Box>
                      </Collapse>
                    </TableCell>
                  </TableRow>
                </React.Fragment>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      ) : (
        <Grid container spacing={2}>
          {filteredStations.map((station, index) => (
            <Grid item xs={12} md={6} lg={4} key={station.id}>
              <Card
                elevation={station.id === cheapestStation?.id ? 4 : 1}
                sx={{
                  border: station.id === cheapestStation?.id ? '2px solid' : undefined,
                  borderColor: station.id === cheapestStation?.id ? 'success.main' : undefined,
                }}
              >
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                    <Box>
                      <Typography variant="h6" gutterBottom>
                        {station.name}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {station.operator} • {station.distance} km
                      </Typography>
                    </Box>
                    {index === 0 && sortField === 'price' && sortOrder === 'asc' && (
                      <Chip label="Best Price" color="success" size="small" icon={<OfferIcon />} />
                    )}
                  </Box>

                  <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 2 }}>
                    <Chip icon={<MoneyIcon />} label={`₹${station.estimatedCost?.toFixed(2)}`} color="primary" />
                    <Chip icon={<TimerIcon />} label={`${station.estimatedTime} min`} variant="outlined" />
                    <Chip icon={<SpeedIcon />} label={`${station.maxPowerKw} kW`} variant="outlined" />
                  </Box>

                  <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <Rating value={station.rating} precision={0.5} size="small" readOnly />
                      <Typography variant="caption">({station.reviews})</Typography>
                    </Box>
                    <Chip
                      size="small"
                      label={`${station.availablePorts}/${station.totalPorts} available`}
                      color={station.availablePorts > 0 ? 'success' : 'error'}
                    />
                  </Box>

                  <Box sx={{ display: 'flex', gap: 1 }}>
                    <Button
                      variant="outlined"
                      size="small"
                      startIcon={<NavigationIcon />}
                      onClick={() => navigateToStation(station)}
                      fullWidth
                    >
                      Navigate
                    </Button>
                    <Button
                      variant="contained"
                      size="small"
                      startIcon={<ScheduleIcon />}
                      onClick={() => {
                        setSelectedStation(station);
                        setScheduleDialogOpen(true);
                      }}
                      fullWidth
                    >
                      Schedule
                    </Button>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {/* Scheduled sessions */}
      {schedules.length > 0 && (
        <Paper elevation={2} sx={{ p: 3, mt: 3 }}>
          <Typography variant="h6" gutterBottom>
            <ScheduleIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
            Scheduled Charging Sessions
          </Typography>
          <List>
            {schedules.map((schedule) => (
              <ListItem key={schedule.id}>
                <ListItemIcon>
                  <CheckCircleIcon color="success" />
                </ListItemIcon>
                <ListItemText
                  primary={schedule.stationName}
                  secondary={
                    <Box>
                      <Typography variant="body2">
                        {schedule.scheduledTime.toLocaleString()} • {schedule.duration} min • {schedule.kwhRequired.toFixed(1)} kWh
                      </Typography>
                      <Typography variant="body2" color="primary">
                        Est. Cost: ₹{schedule.estimatedCost.toFixed(2)}
                        {schedule.savings > 0 && (
                          <Chip
                            size="small"
                            label={`Saving ₹${schedule.savings.toFixed(2)}`}
                            color="success"
                            sx={{ ml: 1 }}
                          />
                        )}
                      </Typography>
                    </Box>
                  }
                />
                <ListItemSecondaryAction>
                  <Chip label={schedule.status} color="primary" size="small" />
                </ListItemSecondaryAction>
              </ListItem>
            ))}
          </List>
        </Paper>
      )}

      {/* Schedule Dialog */}
      <Dialog open={scheduleDialogOpen} onClose={() => setScheduleDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Schedule Charging Session</DialogTitle>
        <DialogContent>
          {selectedStation && (
            <Box sx={{ pt: 1 }}>
              <Typography variant="h6" gutterBottom>
                {selectedStation.name}
              </Typography>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                {selectedStation.address}
              </Typography>

              <Divider sx={{ my: 2 }} />

              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">Energy Required</Typography>
                  <Typography variant="h6">{kwhRequired.toFixed(1)} kWh</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">Estimated Cost</Typography>
                  <Typography variant="h6" color="primary">₹{selectedStation.estimatedCost?.toFixed(2)}</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">Charging Time</Typography>
                  <Typography variant="h6">{selectedStation.estimatedTime} min</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">Charging Power</Typography>
                  <Typography variant="h6">{selectedStation.maxPowerKw} kW</Typography>
                </Grid>
              </Grid>

              {selectedStation.timeOfUsePricing && (
                <Alert severity="info" sx={{ mt: 2 }}>
                  <Typography variant="body2">
                    <strong>Off-Peak Pricing:</strong> ₹{selectedStation.offPeakPrice}/kWh during {selectedStation.offPeakHours}
                  </Typography>
                </Alert>
              )}

              {mostExpensiveStation && selectedStation.estimatedCost && (
                <Alert severity="success" sx={{ mt: 2 }}>
                  <Typography variant="body2">
                    You're saving ₹{(mostExpensiveStation.estimatedCost! - selectedStation.estimatedCost).toFixed(2)} compared to the most expensive option!
                  </Typography>
                </Alert>
              )}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setScheduleDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleScheduleCharging} startIcon={<CheckCircleIcon />}>
            Confirm Booking
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default SmartChargingScheduler;

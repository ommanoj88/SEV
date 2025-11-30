import React, { useState, useEffect, useMemo } from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  SelectChangeEvent,
  Button,
  IconButton,
  Tooltip,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  ToggleButton,
  ToggleButtonGroup,
  Slider,
  useTheme,
  alpha,
  Stack,
  Avatar,
  LinearProgress,
  Card,
  CardContent,
} from '@mui/material';
import {
  DirectionsCar as VehicleIcon,
  Download as DownloadIcon,
  ZoomIn as ZoomInIcon,
  ZoomOut as ZoomOutIcon,
  Refresh as RefreshIcon,
  Info as InfoIcon,
  Close as CloseIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Schedule as ScheduleIcon,
  Today as TodayIcon,
  DateRange as DateRangeIcon,
  CalendarMonth as MonthIcon,
  Warning as WarningIcon,
  CheckCircle as CheckIcon,
  Speed as SpeedIcon,
  Route as RouteIcon,
  Timer as TimerIcon,
  Person as DriverIcon,
} from '@mui/icons-material';
import { useAppSelector } from '../../redux/hooks';

// Types
interface HeatmapCell {
  day: number; // 0-6 (Sunday-Saturday)
  hour: number; // 0-23
  value: number; // Utilization percentage
  vehicleCount: number;
  tripCount: number;
  totalDistance: number;
  avgSpeed: number;
}

interface VehicleOption {
  id: string;
  name: string;
  licensePlate: string;
  type: string;
}

interface CellDetails {
  day: string;
  hour: string;
  utilization: number;
  vehicles: Array<{
    id: string;
    name: string;
    licensePlate: string;
    tripCount: number;
    distance: number;
    driver?: string;
  }>;
  stats: {
    avgTripDuration: number;
    totalTrips: number;
    totalDistance: number;
    avgSpeed: number;
  };
}

type ViewMode = 'week' | 'month';
type ThresholdMode = 'underutilized' | 'optimal' | 'overutilized';

// Constants
const DAYS_OF_WEEK = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
const DAYS_FULL = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
const HOURS = Array.from({ length: 24 }, (_, i) => i);

const THRESHOLDS = {
  underutilized: 30,
  optimal: { min: 30, max: 70 },
  overutilized: 70,
};

// Mock data
const mockVehicles: VehicleOption[] = [
  { id: 'all', name: 'All Vehicles', licensePlate: '', type: '' },
  { id: 'v-001', name: 'Tesla Model 3', licensePlate: 'EV-1234', type: 'Sedan' },
  { id: 'v-002', name: 'Rivian R1T', licensePlate: 'EV-5678', type: 'Truck' },
  { id: 'v-003', name: 'Ford E-Transit', licensePlate: 'EV-9012', type: 'Van' },
  { id: 'v-004', name: 'Chevy Bolt', licensePlate: 'EV-3456', type: 'Compact' },
  { id: 'v-005', name: 'Tesla Model Y', licensePlate: 'EV-7890', type: 'SUV' },
];

const generateMockHeatmapData = (): HeatmapCell[] => {
  const data: HeatmapCell[] = [];
  
  for (let day = 0; day < 7; day++) {
    for (let hour = 0; hour < 24; hour++) {
      // Simulate realistic utilization patterns
      let baseValue: number;
      
      // Weekend lower utilization
      if (day === 0 || day === 6) {
        baseValue = 20 + Math.random() * 40;
      } else {
        // Weekday patterns - peak hours
        if (hour >= 7 && hour <= 9) {
          baseValue = 70 + Math.random() * 25; // Morning rush
        } else if (hour >= 16 && hour <= 18) {
          baseValue = 75 + Math.random() * 20; // Evening rush
        } else if (hour >= 10 && hour <= 15) {
          baseValue = 50 + Math.random() * 30; // Midday
        } else if (hour >= 20 || hour <= 6) {
          baseValue = 5 + Math.random() * 20; // Night
        } else {
          baseValue = 30 + Math.random() * 40;
        }
      }
      
      data.push({
        day,
        hour,
        value: Math.round(baseValue),
        vehicleCount: Math.floor(baseValue / 10) + Math.floor(Math.random() * 5),
        tripCount: Math.floor(baseValue / 5) + Math.floor(Math.random() * 10),
        totalDistance: Math.floor(baseValue * 2 + Math.random() * 100),
        avgSpeed: 25 + Math.floor(Math.random() * 35),
      });
    }
  }
  
  return data;
};

const mockCellDetails: CellDetails = {
  day: 'Monday',
  hour: '8:00 AM - 9:00 AM',
  utilization: 85,
  vehicles: [
    { id: 'v-001', name: 'Tesla Model 3', licensePlate: 'EV-1234', tripCount: 3, distance: 45, driver: 'John Smith' },
    { id: 'v-002', name: 'Rivian R1T', licensePlate: 'EV-5678', tripCount: 2, distance: 32, driver: 'Jane Doe' },
    { id: 'v-004', name: 'Chevy Bolt', licensePlate: 'EV-3456', tripCount: 4, distance: 51, driver: 'Mike Johnson' },
  ],
  stats: {
    avgTripDuration: 22,
    totalTrips: 9,
    totalDistance: 128,
    avgSpeed: 42,
  },
};

// Helper functions
const getHeatmapColor = (value: number, theme: any): string => {
  if (value < THRESHOLDS.underutilized) {
    // Underutilized - Blue shades
    const intensity = value / THRESHOLDS.underutilized;
    return alpha(theme.palette.info.main, 0.2 + intensity * 0.6);
  } else if (value <= THRESHOLDS.overutilized) {
    // Optimal - Green shades
    const normalized = (value - THRESHOLDS.optimal.min) / (THRESHOLDS.optimal.max - THRESHOLDS.optimal.min);
    return alpha(theme.palette.success.main, 0.3 + normalized * 0.5);
  } else {
    // Overutilized - Orange/Red shades
    const intensity = Math.min((value - THRESHOLDS.overutilized) / 30, 1);
    if (intensity > 0.5) {
      return alpha(theme.palette.error.main, 0.5 + intensity * 0.4);
    }
    return alpha(theme.palette.warning.main, 0.4 + intensity * 0.5);
  }
};

const getUtilizationStatus = (value: number): ThresholdMode => {
  if (value < THRESHOLDS.underutilized) return 'underutilized';
  if (value > THRESHOLDS.overutilized) return 'overutilized';
  return 'optimal';
};

const formatHour = (hour: number): string => {
  if (hour === 0) return '12 AM';
  if (hour === 12) return '12 PM';
  if (hour < 12) return `${hour} AM`;
  return `${hour - 12} PM`;
};

// Components
interface HeatmapCellProps {
  cell: HeatmapCell;
  cellSize: number;
  onClick: (cell: HeatmapCell) => void;
  highlighted: ThresholdMode | null;
}

const HeatmapCellComponent: React.FC<HeatmapCellProps> = ({ cell, cellSize, onClick, highlighted }) => {
  const theme = useTheme();
  const status = getUtilizationStatus(cell.value);
  const isHighlighted = highlighted === null || highlighted === status;
  
  return (
    <Tooltip
      title={
        <Box>
          <Typography variant="subtitle2">{DAYS_FULL[cell.day]}, {formatHour(cell.hour)}</Typography>
          <Typography variant="body2">Utilization: {cell.value}%</Typography>
          <Typography variant="caption">
            {cell.vehicleCount} vehicles • {cell.tripCount} trips • {cell.totalDistance} km
          </Typography>
        </Box>
      }
      arrow
    >
      <Box
        onClick={() => onClick(cell)}
        sx={{
          width: cellSize,
          height: cellSize,
          bgcolor: getHeatmapColor(cell.value, theme),
          borderRadius: 0.5,
          cursor: 'pointer',
          transition: 'all 0.2s ease',
          opacity: isHighlighted ? 1 : 0.3,
          border: '1px solid',
          borderColor: alpha(theme.palette.divider, 0.1),
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          '&:hover': {
            transform: 'scale(1.1)',
            zIndex: 1,
            boxShadow: theme.shadows[4],
          },
        }}
      >
        {cellSize >= 35 && (
          <Typography
            variant="caption"
            sx={{
              fontSize: cellSize >= 45 ? 10 : 8,
              fontWeight: 600,
              color: cell.value > 50 ? 'white' : 'text.primary',
            }}
          >
            {cell.value}
          </Typography>
        )}
      </Box>
    </Tooltip>
  );
};

interface CellDetailsDialogProps {
  open: boolean;
  onClose: () => void;
  cell: HeatmapCell | null;
  details: CellDetails;
}

const CellDetailsDialog: React.FC<CellDetailsDialogProps> = ({ open, onClose, cell, details }) => {
  const theme = useTheme();
  
  if (!cell) return null;
  
  const status = getUtilizationStatus(cell.value);
  
  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Typography variant="h6">
              {DAYS_FULL[cell.day]}, {formatHour(cell.hour)} - {formatHour(cell.hour + 1)}
            </Typography>
            <Chip
              size="small"
              label={status}
              color={status === 'optimal' ? 'success' : status === 'underutilized' ? 'info' : 'warning'}
            />
          </Box>
          <IconButton onClick={onClose} size="small">
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>
      <DialogContent dividers>
        {/* Summary Stats */}
        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid item xs={6} sm={3}>
            <Paper sx={{ p: 2, textAlign: 'center', bgcolor: alpha(theme.palette.primary.main, 0.1) }}>
              <SpeedIcon color="primary" />
              <Typography variant="h5" fontWeight={700}>{cell.value}%</Typography>
              <Typography variant="caption" color="text.secondary">Utilization</Typography>
            </Paper>
          </Grid>
          <Grid item xs={6} sm={3}>
            <Paper sx={{ p: 2, textAlign: 'center', bgcolor: alpha(theme.palette.success.main, 0.1) }}>
              <VehicleIcon color="success" />
              <Typography variant="h5" fontWeight={700}>{cell.vehicleCount}</Typography>
              <Typography variant="caption" color="text.secondary">Active Vehicles</Typography>
            </Paper>
          </Grid>
          <Grid item xs={6} sm={3}>
            <Paper sx={{ p: 2, textAlign: 'center', bgcolor: alpha(theme.palette.info.main, 0.1) }}>
              <RouteIcon color="info" />
              <Typography variant="h5" fontWeight={700}>{cell.tripCount}</Typography>
              <Typography variant="caption" color="text.secondary">Trips</Typography>
            </Paper>
          </Grid>
          <Grid item xs={6} sm={3}>
            <Paper sx={{ p: 2, textAlign: 'center', bgcolor: alpha(theme.palette.warning.main, 0.1) }}>
              <TimerIcon color="warning" />
              <Typography variant="h5" fontWeight={700}>{cell.totalDistance}</Typography>
              <Typography variant="caption" color="text.secondary">Total km</Typography>
            </Paper>
          </Grid>
        </Grid>

        {/* Vehicle List */}
        <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>
          Vehicles Active During This Hour
        </Typography>
        <List>
          {details.vehicles.map((vehicle, index) => (
            <React.Fragment key={vehicle.id}>
              <ListItem
                sx={{
                  borderRadius: 1,
                  '&:hover': {
                    bgcolor: alpha(theme.palette.primary.main, 0.05),
                  },
                }}
              >
                <ListItemIcon>
                  <Avatar sx={{ bgcolor: alpha(theme.palette.primary.main, 0.15) }}>
                    <VehicleIcon color="primary" />
                  </Avatar>
                </ListItemIcon>
                <ListItemText
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Typography fontWeight={600}>{vehicle.name}</Typography>
                      <Chip size="small" label={vehicle.licensePlate} variant="outlined" />
                    </Box>
                  }
                  secondary={
                    <Box sx={{ display: 'flex', gap: 3, mt: 0.5 }}>
                      <Typography variant="body2" color="text.secondary">
                        <RouteIcon sx={{ fontSize: 14, mr: 0.5, verticalAlign: 'middle' }} />
                        {vehicle.tripCount} trips
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        <SpeedIcon sx={{ fontSize: 14, mr: 0.5, verticalAlign: 'middle' }} />
                        {vehicle.distance} km
                      </Typography>
                      {vehicle.driver && (
                        <Typography variant="body2" color="text.secondary">
                          <DriverIcon sx={{ fontSize: 14, mr: 0.5, verticalAlign: 'middle' }} />
                          {vehicle.driver}
                        </Typography>
                      )}
                    </Box>
                  }
                />
              </ListItem>
              {index < details.vehicles.length - 1 && <Divider component="li" />}
            </React.Fragment>
          ))}
        </List>

        {/* Insights */}
        <Box sx={{ mt: 3 }}>
          <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>
            Insights & Recommendations
          </Typography>
          <Paper
            variant="outlined"
            sx={{
              p: 2,
              borderColor: status === 'optimal' ? 'success.main' : status === 'underutilized' ? 'info.main' : 'warning.main',
              borderLeftWidth: 4,
            }}
          >
            {status === 'optimal' && (
              <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
                <CheckIcon color="success" />
                <Box>
                  <Typography fontWeight={600} color="success.main">Optimal Utilization</Typography>
                  <Typography variant="body2" color="text.secondary">
                    Fleet utilization during this period is within the optimal range. 
                    Current vehicle allocation is efficient for the demand.
                  </Typography>
                </Box>
              </Box>
            )}
            {status === 'underutilized' && (
              <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
                <TrendingDownIcon color="info" />
                <Box>
                  <Typography fontWeight={600} color="info.main">Underutilized Period</Typography>
                  <Typography variant="body2" color="text.secondary">
                    Consider reducing fleet size during this period or scheduling maintenance 
                    tasks. {cell.vehicleCount} vehicles could be consolidated.
                  </Typography>
                </Box>
              </Box>
            )}
            {status === 'overutilized' && (
              <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
                <WarningIcon color="warning" />
                <Box>
                  <Typography fontWeight={600} color="warning.main">High Demand Period</Typography>
                  <Typography variant="body2" color="text.secondary">
                    Fleet is near capacity. Consider adding {Math.ceil(cell.vehicleCount * 0.2)} 
                    vehicles or redistributing trips to adjacent hours.
                  </Typography>
                </Box>
              </Box>
            )}
          </Paper>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
        <Button variant="contained" startIcon={<ScheduleIcon />}>
          View Full Schedule
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// Main Component
const UtilizationHeatmap: React.FC = () => {
  const theme = useTheme();
  
  // State
  const [heatmapData, setHeatmapData] = useState<HeatmapCell[]>([]);
  const [selectedVehicle, setSelectedVehicle] = useState<string>('all');
  const [viewMode, setViewMode] = useState<ViewMode>('week');
  const [cellSize, setCellSize] = useState<number>(40);
  const [highlightMode, setHighlightMode] = useState<ThresholdMode | null>(null);
  const [selectedCell, setSelectedCell] = useState<HeatmapCell | null>(null);
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  // Load data
  useEffect(() => {
    setLoading(true);
    // Simulate API call
    setTimeout(() => {
      setHeatmapData(generateMockHeatmapData());
      setLoading(false);
    }, 500);
  }, [selectedVehicle]);

  // Handlers
  const handleVehicleChange = (event: SelectChangeEvent) => {
    setSelectedVehicle(event.target.value);
  };

  const handleViewModeChange = (_: React.MouseEvent, newMode: ViewMode | null) => {
    if (newMode) setViewMode(newMode);
  };

  const handleHighlightChange = (_: React.MouseEvent, newMode: ThresholdMode | null) => {
    setHighlightMode(newMode);
  };

  const handleCellClick = (cell: HeatmapCell) => {
    setSelectedCell(cell);
    setDetailsOpen(true);
  };

  const handleExport = () => {
    // Implementation would capture the heatmap as image
    console.log('Exporting heatmap as image...');
  };

  const handleRefresh = () => {
    setHeatmapData(generateMockHeatmapData());
  };

  // Calculate summary stats
  const summaryStats = useMemo(() => {
    if (heatmapData.length === 0) return null;
    
    const totalUtilization = heatmapData.reduce((sum, cell) => sum + cell.value, 0);
    const avgUtilization = totalUtilization / heatmapData.length;
    
    const underutilizedCount = heatmapData.filter(c => c.value < THRESHOLDS.underutilized).length;
    const optimalCount = heatmapData.filter(c => c.value >= THRESHOLDS.underutilized && c.value <= THRESHOLDS.overutilized).length;
    const overutilizedCount = heatmapData.filter(c => c.value > THRESHOLDS.overutilized).length;
    
    const peakHour = heatmapData.reduce((max, cell) => cell.value > max.value ? cell : max, heatmapData[0]);
    const lowHour = heatmapData.reduce((min, cell) => cell.value < min.value ? cell : min, heatmapData[0]);
    
    return {
      avgUtilization: Math.round(avgUtilization),
      underutilizedCount,
      optimalCount,
      overutilizedCount,
      peakHour,
      lowHour,
    };
  }, [heatmapData]);

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" fontWeight={700}>
            Vehicle Utilization Heatmap
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Visualize fleet utilization patterns by hour and day
          </Typography>
        </Box>
        
        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
          {/* Vehicle Filter */}
          <FormControl size="small" sx={{ minWidth: 180 }}>
            <InputLabel>Vehicle</InputLabel>
            <Select value={selectedVehicle} onChange={handleVehicleChange} label="Vehicle">
              {mockVehicles.map((v) => (
                <MenuItem key={v.id} value={v.id}>
                  {v.name} {v.licensePlate && `(${v.licensePlate})`}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          {/* View Mode */}
          <ToggleButtonGroup
            value={viewMode}
            exclusive
            onChange={handleViewModeChange}
            size="small"
          >
            <ToggleButton value="week">
              <Tooltip title="Weekly View">
                <TodayIcon />
              </Tooltip>
            </ToggleButton>
            <ToggleButton value="month">
              <Tooltip title="Monthly View">
                <MonthIcon />
              </Tooltip>
            </ToggleButton>
          </ToggleButtonGroup>

          {/* Zoom Controls */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <IconButton size="small" onClick={() => setCellSize(Math.max(20, cellSize - 5))}>
              <ZoomOutIcon />
            </IconButton>
            <Typography variant="caption" sx={{ minWidth: 30, textAlign: 'center' }}>
              {cellSize}
            </Typography>
            <IconButton size="small" onClick={() => setCellSize(Math.min(60, cellSize + 5))}>
              <ZoomInIcon />
            </IconButton>
          </Box>

          {/* Actions */}
          <Tooltip title="Refresh data">
            <IconButton onClick={handleRefresh}>
              <RefreshIcon />
            </IconButton>
          </Tooltip>
          <Tooltip title="Export as image">
            <IconButton onClick={handleExport}>
              <DownloadIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {loading && <LinearProgress sx={{ mb: 2 }} />}

      {/* Summary Stats */}
      {summaryStats && (
        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid item xs={12} sm={6} md={2}>
            <Card>
              <CardContent sx={{ textAlign: 'center', py: 2 }}>
                <Typography variant="h4" fontWeight={700} color="primary">
                  {summaryStats.avgUtilization}%
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Average Utilization
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <Card>
              <CardContent sx={{ textAlign: 'center', py: 2 }}>
                <Typography variant="h4" fontWeight={700} color="info.main">
                  {summaryStats.underutilizedCount}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Underutilized Hours
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <Card>
              <CardContent sx={{ textAlign: 'center', py: 2 }}>
                <Typography variant="h4" fontWeight={700} color="success.main">
                  {summaryStats.optimalCount}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Optimal Hours
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <Card>
              <CardContent sx={{ textAlign: 'center', py: 2 }}>
                <Typography variant="h4" fontWeight={700} color="warning.main">
                  {summaryStats.overutilizedCount}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Overutilized Hours
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <Card>
              <CardContent sx={{ textAlign: 'center', py: 2 }}>
                <Typography variant="body2" fontWeight={700}>
                  {DAYS_OF_WEEK[summaryStats.peakHour.day]} {formatHour(summaryStats.peakHour.hour)}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Peak: {summaryStats.peakHour.value}%
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <Card>
              <CardContent sx={{ textAlign: 'center', py: 2 }}>
                <Typography variant="body2" fontWeight={700}>
                  {DAYS_OF_WEEK[summaryStats.lowHour.day]} {formatHour(summaryStats.lowHour.hour)}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Lowest: {summaryStats.lowHour.value}%
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Filter by Threshold */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Typography variant="subtitle2">Highlight:</Typography>
            <ToggleButtonGroup
              value={highlightMode}
              exclusive
              onChange={handleHighlightChange}
              size="small"
            >
              <ToggleButton value="underutilized">
                <Chip
                  size="small"
                  label={`Underutilized (<${THRESHOLDS.underutilized}%)`}
                  sx={{ bgcolor: alpha(theme.palette.info.main, 0.15) }}
                />
              </ToggleButton>
              <ToggleButton value="optimal">
                <Chip
                  size="small"
                  label={`Optimal (${THRESHOLDS.optimal.min}-${THRESHOLDS.optimal.max}%)`}
                  sx={{ bgcolor: alpha(theme.palette.success.main, 0.15) }}
                />
              </ToggleButton>
              <ToggleButton value="overutilized">
                <Chip
                  size="small"
                  label={`Overutilized (>${THRESHOLDS.overutilized}%)`}
                  sx={{ bgcolor: alpha(theme.palette.warning.main, 0.15) }}
                />
              </ToggleButton>
            </ToggleButtonGroup>
          </Box>

          {/* Legend */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Typography variant="caption" color="text.secondary">Utilization:</Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <Box sx={{ width: 16, height: 16, bgcolor: alpha(theme.palette.info.main, 0.3), borderRadius: 0.5 }} />
              <Typography variant="caption">Low</Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <Box sx={{ width: 16, height: 16, bgcolor: alpha(theme.palette.success.main, 0.5), borderRadius: 0.5 }} />
              <Typography variant="caption">Optimal</Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <Box sx={{ width: 16, height: 16, bgcolor: alpha(theme.palette.warning.main, 0.6), borderRadius: 0.5 }} />
              <Typography variant="caption">High</Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <Box sx={{ width: 16, height: 16, bgcolor: alpha(theme.palette.error.main, 0.7), borderRadius: 0.5 }} />
              <Typography variant="caption">Critical</Typography>
            </Box>
          </Box>
        </Box>
      </Paper>

      {/* Heatmap Grid */}
      <Paper sx={{ p: 3, overflow: 'auto' }}>
        <Box sx={{ display: 'flex' }}>
          {/* Hour Labels (Y-axis) */}
          <Box sx={{ display: 'flex', flexDirection: 'column', mr: 1 }}>
            <Box sx={{ height: cellSize + 8, display: 'flex', alignItems: 'flex-end', pb: 1 }}>
              <Typography variant="caption" sx={{ fontWeight: 600 }}>Hour</Typography>
            </Box>
            {HOURS.map((hour) => (
              <Box
                key={hour}
                sx={{
                  height: cellSize,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'flex-end',
                  pr: 1,
                  mb: '2px',
                }}
              >
                <Typography variant="caption" color="text.secondary">
                  {formatHour(hour)}
                </Typography>
              </Box>
            ))}
          </Box>

          {/* Heatmap Cells */}
          <Box>
            {/* Day Labels (X-axis) */}
            <Box sx={{ display: 'flex', mb: 1 }}>
              {DAYS_OF_WEEK.map((day, index) => (
                <Box
                  key={day}
                  sx={{
                    width: cellSize,
                    mr: '2px',
                    textAlign: 'center',
                  }}
                >
                  <Typography
                    variant="caption"
                    sx={{
                      fontWeight: 600,
                      color: index === 0 || index === 6 ? 'text.secondary' : 'text.primary',
                    }}
                  >
                    {day}
                  </Typography>
                </Box>
              ))}
            </Box>

            {/* Cells Grid */}
            {HOURS.map((hour) => (
              <Box key={hour} sx={{ display: 'flex', mb: '2px' }}>
                {DAYS_OF_WEEK.map((_, dayIndex) => {
                  const cell = heatmapData.find(c => c.day === dayIndex && c.hour === hour);
                  if (!cell) return <Box key={dayIndex} sx={{ width: cellSize, height: cellSize, mr: '2px' }} />;
                  
                  return (
                    <Box key={dayIndex} sx={{ mr: '2px' }}>
                      <HeatmapCellComponent
                        cell={cell}
                        cellSize={cellSize}
                        onClick={handleCellClick}
                        highlighted={highlightMode}
                      />
                    </Box>
                  );
                })}
              </Box>
            ))}
          </Box>
        </Box>
      </Paper>

      {/* Cell Details Dialog */}
      <CellDetailsDialog
        open={detailsOpen}
        onClose={() => setDetailsOpen(false)}
        cell={selectedCell}
        details={mockCellDetails}
      />
    </Box>
  );
};

export default UtilizationHeatmap;

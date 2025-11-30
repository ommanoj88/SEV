import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Card,
  CardContent,
  CardActionArea,
  Grid,
  Typography,
  Paper,
  IconButton,
  Button,
  ButtonGroup,
  Menu,
  MenuItem,
  Chip,
  LinearProgress,
  Divider,
  Tooltip,
  Badge,
  useTheme,
  alpha,
  Skeleton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemSecondaryAction,
  Avatar,
  Switch,
  FormControlLabel,
  Select,
  FormControl,
  InputLabel,
  SelectChangeEvent,
} from '@mui/material';
import {
  DirectionsCar as VehicleIcon,
  LocalShipping as TruckIcon,
  EvStation as ChargingIcon,
  Build as MaintenanceIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  TrendingFlat as TrendingFlatIcon,
  Refresh as RefreshIcon,
  Download as DownloadIcon,
  CompareArrows as CompareIcon,
  Today as TodayIcon,
  DateRange as DateRangeIcon,
  Map as MapIcon,
  List as ListIcon,
  Schedule as ScheduleIcon,
  Warning as WarningIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  BatteryChargingFull as BatteryIcon,
  Speed as SpeedIcon,
  AttachMoney as MoneyIcon,
  Eco as EcoIcon,
  LocationOn as LocationIcon,
  NotificationsActive as AlertIcon,
  Close as CloseIcon,
  MoreVert as MoreIcon,
  PictureAsPdf as PdfIcon,
  Image as ImageIcon,
  TableChart as CsvIcon,
  Fullscreen as FullscreenIcon,
  Timer as TimerIcon,
  Person as DriverIcon,
  Route as RouteIcon,
} from '@mui/icons-material';
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../redux/hooks';
import { fetchFleetSummary } from '../../redux/slices/analyticsSlice';

// Types
interface KPICardData {
  id: string;
  title: string;
  value: number;
  unit?: string;
  change: number;
  changeLabel: string;
  icon: React.ReactNode;
  color: string;
  link: string;
  subItems?: Array<{ label: string; value: number | string; status?: 'good' | 'warning' | 'error' }>;
}

interface TrendDataPoint {
  date: string;
  value: number;
  previousValue?: number;
}

interface VehicleStatus {
  id: string;
  name: string;
  licensePlate: string;
  status: 'active' | 'idle' | 'charging' | 'maintenance' | 'offline';
  battery?: number;
  location?: string;
  driver?: string;
}

interface AlertItem {
  id: string;
  type: 'warning' | 'error' | 'info';
  title: string;
  description: string;
  timestamp: Date;
  vehicleId?: string;
}

type DateRange = '7d' | '30d' | '90d' | 'custom';

// Mock data
const mockKPIData: KPICardData[] = [
  {
    id: 'total-vehicles',
    title: 'Total Vehicles',
    value: 124,
    change: 5.2,
    changeLabel: 'vs last period',
    icon: <VehicleIcon />,
    color: '#2196f3',
    link: '/vehicles',
    subItems: [
      { label: 'Electric', value: 89, status: 'good' },
      { label: 'Hybrid', value: 28, status: 'good' },
      { label: 'ICE', value: 7, status: 'warning' },
    ],
  },
  {
    id: 'active-now',
    title: 'Active Now',
    value: 78,
    change: 12.3,
    changeLabel: 'vs same time yesterday',
    icon: <LocationIcon />,
    color: '#4caf50',
    link: '/map',
    subItems: [
      { label: 'On Route', value: 56 },
      { label: 'Idle', value: 22 },
    ],
  },
  {
    id: 'on-charge',
    title: 'On Charge',
    value: 23,
    change: -8.1,
    changeLabel: 'vs average',
    icon: <ChargingIcon />,
    color: '#ff9800',
    link: '/charging/sessions',
    subItems: [
      { label: 'Fast Charging', value: 8 },
      { label: 'Standard', value: 15 },
      { label: 'Est. Ready', value: '2.5 hrs' },
    ],
  },
  {
    id: 'in-maintenance',
    title: 'In Maintenance',
    value: 12,
    change: 2.5,
    changeLabel: 'vs last week',
    icon: <MaintenanceIcon />,
    color: '#f44336',
    link: '/maintenance',
    subItems: [
      { label: 'Scheduled', value: 8, status: 'good' },
      { label: 'Unscheduled', value: 4, status: 'error' },
    ],
  },
];

const mockSecondaryKPIs = [
  { id: 'efficiency', title: 'Fleet Efficiency', value: 94.2, unit: '%', icon: <SpeedIcon />, color: '#9c27b0' },
  { id: 'cost-per-km', title: 'Cost per km', value: 0.12, unit: '$', icon: <MoneyIcon />, color: '#00bcd4' },
  { id: 'co2-saved', title: 'CO₂ Saved', value: 12.4, unit: 't', icon: <EcoIcon />, color: '#4caf50' },
  { id: 'avg-battery', title: 'Avg Battery', value: 72, unit: '%', icon: <BatteryIcon />, color: '#ff9800' },
];

const mockTrendData: TrendDataPoint[] = [
  { date: 'Mon', value: 72, previousValue: 65 },
  { date: 'Tue', value: 78, previousValue: 68 },
  { date: 'Wed', value: 85, previousValue: 72 },
  { date: 'Thu', value: 82, previousValue: 75 },
  { date: 'Fri', value: 88, previousValue: 78 },
  { date: 'Sat', value: 65, previousValue: 55 },
  { date: 'Sun', value: 58, previousValue: 48 },
];

const mockStatusDistribution = [
  { name: 'Active', value: 78, color: '#4caf50' },
  { name: 'Charging', value: 23, color: '#ff9800' },
  { name: 'Maintenance', value: 12, color: '#f44336' },
  { name: 'Idle', value: 11, color: '#9e9e9e' },
];

const mockAlerts: AlertItem[] = [
  { id: '1', type: 'error', title: 'Low Battery Alert', description: 'Vehicle V-042 battery at 8%', timestamp: new Date(), vehicleId: 'V-042' },
  { id: '2', type: 'warning', title: 'Maintenance Due', description: '3 vehicles have overdue maintenance', timestamp: new Date() },
  { id: '3', type: 'info', title: 'Charging Complete', description: 'Vehicle V-018 fully charged', timestamp: new Date(), vehicleId: 'V-018' },
];

const mockActiveVehicles: VehicleStatus[] = [
  { id: 'V-001', name: 'Tesla Model 3', licensePlate: 'EV-1234', status: 'active', battery: 85, location: 'Downtown', driver: 'John Smith' },
  { id: 'V-002', name: 'Rivian R1T', licensePlate: 'EV-5678', status: 'charging', battery: 45, location: 'Depot A' },
  { id: 'V-003', name: 'Ford E-Transit', licensePlate: 'EV-9012', status: 'maintenance', location: 'Service Center' },
  { id: 'V-004', name: 'Chevy Bolt', licensePlate: 'EV-3456', status: 'active', battery: 72, location: 'Highway 101', driver: 'Jane Doe' },
  { id: 'V-005', name: 'Tesla Model Y', licensePlate: 'EV-7890', status: 'idle', battery: 92, location: 'Depot B' },
];

// Components
interface KPICardProps {
  data: KPICardData;
  comparison: boolean;
  onDrillDown: (id: string) => void;
}

const KPICard: React.FC<KPICardProps> = ({ data, comparison, onDrillDown }) => {
  const theme = useTheme();
  const [expanded, setExpanded] = useState(false);

  const getTrendIcon = () => {
    if (data.change > 0) return <TrendingUpIcon sx={{ color: 'success.main' }} />;
    if (data.change < 0) return <TrendingDownIcon sx={{ color: 'error.main' }} />;
    return <TrendingFlatIcon sx={{ color: 'text.secondary' }} />;
  };

  const getTrendColor = () => {
    // For maintenance, lower is better
    if (data.id === 'in-maintenance') {
      return data.change > 0 ? 'error.main' : 'success.main';
    }
    return data.change > 0 ? 'success.main' : 'error.main';
  };

  return (
    <Card
      sx={{
        height: '100%',
        borderLeft: `4px solid ${data.color}`,
        transition: 'all 0.3s ease',
        '&:hover': {
          boxShadow: theme.shadows[8],
          transform: 'translateY(-4px)',
        },
      }}
    >
      <CardActionArea onClick={() => onDrillDown(data.id)}>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
            <Avatar
              sx={{
                bgcolor: alpha(data.color, 0.15),
                color: data.color,
                width: 48,
                height: 48,
              }}
            >
              {data.icon}
            </Avatar>
            {comparison && (
              <Tooltip title={data.changeLabel}>
                <Chip
                  size="small"
                  icon={getTrendIcon()}
                  label={`${data.change > 0 ? '+' : ''}${data.change}%`}
                  sx={{
                    bgcolor: alpha(theme.palette.mode === 'dark' ? '#fff' : '#000', 0.05),
                    color: getTrendColor(),
                    fontWeight: 600,
                  }}
                />
              </Tooltip>
            )}
          </Box>

          <Typography variant="h3" sx={{ fontWeight: 700, mb: 0.5 }}>
            {data.value.toLocaleString()}
            {data.unit && (
              <Typography component="span" variant="h6" color="text.secondary" sx={{ ml: 0.5 }}>
                {data.unit}
              </Typography>
            )}
          </Typography>

          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {data.title}
          </Typography>

          {data.subItems && (
            <Box sx={{ mt: 1 }}>
              <Divider sx={{ mb: 1 }} />
              <Grid container spacing={1}>
                {data.subItems.map((item, index) => (
                  <Grid item xs={4} key={index}>
                    <Box sx={{ textAlign: 'center' }}>
                      <Typography variant="body2" fontWeight={600}>
                        {item.value}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {item.label}
                      </Typography>
                    </Box>
                  </Grid>
                ))}
              </Grid>
            </Box>
          )}
        </CardContent>
      </CardActionArea>
    </Card>
  );
};

interface SecondaryKPIProps {
  data: typeof mockSecondaryKPIs[0];
}

const SecondaryKPI: React.FC<SecondaryKPIProps> = ({ data }) => {
  const theme = useTheme();

  return (
    <Paper
      sx={{
        p: 2,
        display: 'flex',
        alignItems: 'center',
        gap: 2,
        transition: 'all 0.2s ease',
        '&:hover': {
          bgcolor: alpha(data.color, 0.05),
        },
      }}
    >
      <Avatar sx={{ bgcolor: alpha(data.color, 0.15), color: data.color }}>
        {data.icon}
      </Avatar>
      <Box sx={{ flex: 1 }}>
        <Typography variant="h6" fontWeight={600}>
          {data.unit === '$' && data.unit}
          {data.value}
          {data.unit !== '$' && data.unit}
        </Typography>
        <Typography variant="caption" color="text.secondary">
          {data.title}
        </Typography>
      </Box>
    </Paper>
  );
};

interface DrillDownDialogProps {
  open: boolean;
  onClose: () => void;
  kpiId: string | null;
  vehicles: VehicleStatus[];
}

const DrillDownDialog: React.FC<DrillDownDialogProps> = ({ open, onClose, kpiId, vehicles }) => {
  const theme = useTheme();
  const navigate = useNavigate();

  const getFilteredVehicles = () => {
    switch (kpiId) {
      case 'active-now':
        return vehicles.filter(v => v.status === 'active' || v.status === 'idle');
      case 'on-charge':
        return vehicles.filter(v => v.status === 'charging');
      case 'in-maintenance':
        return vehicles.filter(v => v.status === 'maintenance');
      default:
        return vehicles;
    }
  };

  const getTitle = () => {
    switch (kpiId) {
      case 'total-vehicles':
        return 'All Vehicles';
      case 'active-now':
        return 'Active Vehicles';
      case 'on-charge':
        return 'Vehicles on Charge';
      case 'in-maintenance':
        return 'Vehicles in Maintenance';
      default:
        return 'Vehicles';
    }
  };

  const getStatusColor = (status: VehicleStatus['status']) => {
    switch (status) {
      case 'active':
        return 'success';
      case 'charging':
        return 'warning';
      case 'maintenance':
        return 'error';
      case 'idle':
        return 'default';
      case 'offline':
        return 'default';
      default:
        return 'default';
    }
  };

  const filteredVehicles = getFilteredVehicles();

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">{getTitle()}</Typography>
          <IconButton onClick={onClose} size="small">
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>
      <DialogContent dividers>
        <List>
          {filteredVehicles.map((vehicle) => (
            <ListItem
              key={vehicle.id}
              sx={{
                borderRadius: 1,
                mb: 1,
                bgcolor: alpha(theme.palette.background.paper, 0.5),
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
                    <Chip
                      size="small"
                      label={vehicle.status}
                      color={getStatusColor(vehicle.status)}
                      variant="outlined"
                    />
                  </Box>
                }
                secondary={
                  <Box sx={{ display: 'flex', gap: 2, mt: 0.5 }}>
                    <Typography variant="caption" color="text.secondary">
                      {vehicle.licensePlate}
                    </Typography>
                    {vehicle.location && (
                      <Typography variant="caption" color="text.secondary">
                        📍 {vehicle.location}
                      </Typography>
                    )}
                    {vehicle.driver && (
                      <Typography variant="caption" color="text.secondary">
                        👤 {vehicle.driver}
                      </Typography>
                    )}
                  </Box>
                }
              />
              <ListItemSecondaryAction>
                {vehicle.battery !== undefined && (
                  <Tooltip title={`Battery: ${vehicle.battery}%`}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <BatteryIcon
                        sx={{
                          color:
                            vehicle.battery > 50
                              ? 'success.main'
                              : vehicle.battery > 20
                              ? 'warning.main'
                              : 'error.main',
                        }}
                      />
                      <Typography variant="body2" fontWeight={600}>
                        {vehicle.battery}%
                      </Typography>
                    </Box>
                  </Tooltip>
                )}
              </ListItemSecondaryAction>
            </ListItem>
          ))}
        </List>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
        <Button
          variant="contained"
          onClick={() => {
            navigate(kpiId === 'active-now' ? '/map' : '/vehicles');
            onClose();
          }}
        >
          View All in Full Page
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// Main Component
const FleetSummaryDashboard: React.FC = () => {
  const theme = useTheme();
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { fleetAnalytics, loading } = useAppSelector((state) => state.analytics);

  // State
  const [dateRange, setDateRange] = useState<DateRange>('7d');
  const [comparisonMode, setComparisonMode] = useState(true);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [refreshInterval, setRefreshInterval] = useState(60);
  const [exportMenuAnchor, setExportMenuAnchor] = useState<null | HTMLElement>(null);
  const [settingsMenuAnchor, setSettingsMenuAnchor] = useState<null | HTMLElement>(null);
  const [drillDownOpen, setDrillDownOpen] = useState(false);
  const [selectedKPI, setSelectedKPI] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date>(new Date());

  // Load data
  const loadData = useCallback(() => {
    dispatch(fetchFleetSummary(undefined));
    setLastUpdated(new Date());
  }, [dispatch]);

  useEffect(() => {
    loadData();
  }, [loadData, dateRange]);

  // Auto-refresh
  useEffect(() => {
    if (!autoRefresh) return;

    const interval = setInterval(() => {
      loadData();
    }, refreshInterval * 1000);

    return () => clearInterval(interval);
  }, [autoRefresh, refreshInterval, loadData]);

  // Handlers
  const handleDateRangeChange = (event: SelectChangeEvent<DateRange>) => {
    setDateRange(event.target.value as DateRange);
  };

  const handleExport = (format: 'pdf' | 'png' | 'csv') => {
    console.log(`Exporting as ${format}...`);
    // Implementation would use libraries like jspdf, html2canvas, etc.
    setExportMenuAnchor(null);
  };

  const handleDrillDown = (kpiId: string) => {
    setSelectedKPI(kpiId);
    setDrillDownOpen(true);
  };

  const handleNavigate = (kpiId: string) => {
    const kpi = mockKPIData.find((k) => k.id === kpiId);
    if (kpi?.link) {
      navigate(kpi.link);
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" fontWeight={700}>
            Fleet Summary Dashboard
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Last updated: {lastUpdated.toLocaleTimeString()}
            {autoRefresh && (
              <Chip
                size="small"
                label={`Auto-refresh: ${refreshInterval}s`}
                color="primary"
                variant="outlined"
                sx={{ ml: 1 }}
              />
            )}
          </Typography>
        </Box>

        <Box sx={{ display: 'flex', gap: 1 }}>
          {/* Date Range Selector */}
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Period</InputLabel>
            <Select value={dateRange} onChange={handleDateRangeChange} label="Period">
              <MenuItem value="7d">Last 7 Days</MenuItem>
              <MenuItem value="30d">Last 30 Days</MenuItem>
              <MenuItem value="90d">Last 90 Days</MenuItem>
              <MenuItem value="custom">Custom Range</MenuItem>
            </Select>
          </FormControl>

          {/* Comparison Toggle */}
          <Tooltip title="Compare with previous period">
            <Button
              variant={comparisonMode ? 'contained' : 'outlined'}
              size="small"
              onClick={() => setComparisonMode(!comparisonMode)}
              startIcon={<CompareIcon />}
            >
              Compare
            </Button>
          </Tooltip>

          {/* Refresh Button */}
          <Tooltip title="Refresh data">
            <IconButton onClick={loadData} disabled={loading}>
              <RefreshIcon />
            </IconButton>
          </Tooltip>

          {/* Export Menu */}
          <Tooltip title="Export dashboard">
            <IconButton onClick={(e) => setExportMenuAnchor(e.currentTarget)}>
              <DownloadIcon />
            </IconButton>
          </Tooltip>
          <Menu
            anchorEl={exportMenuAnchor}
            open={Boolean(exportMenuAnchor)}
            onClose={() => setExportMenuAnchor(null)}
          >
            <MenuItem onClick={() => handleExport('pdf')}>
              <ListItemIcon>
                <PdfIcon />
              </ListItemIcon>
              <ListItemText>Export as PDF</ListItemText>
            </MenuItem>
            <MenuItem onClick={() => handleExport('png')}>
              <ListItemIcon>
                <ImageIcon />
              </ListItemIcon>
              <ListItemText>Export as Image</ListItemText>
            </MenuItem>
            <MenuItem onClick={() => handleExport('csv')}>
              <ListItemIcon>
                <CsvIcon />
              </ListItemIcon>
              <ListItemText>Export as CSV</ListItemText>
            </MenuItem>
          </Menu>

          {/* Settings Menu */}
          <Tooltip title="Dashboard settings">
            <IconButton onClick={(e) => setSettingsMenuAnchor(e.currentTarget)}>
              <MoreIcon />
            </IconButton>
          </Tooltip>
          <Menu
            anchorEl={settingsMenuAnchor}
            open={Boolean(settingsMenuAnchor)}
            onClose={() => setSettingsMenuAnchor(null)}
          >
            <MenuItem>
              <FormControlLabel
                control={
                  <Switch
                    checked={autoRefresh}
                    onChange={(e) => setAutoRefresh(e.target.checked)}
                    size="small"
                  />
                }
                label="Auto-refresh"
              />
            </MenuItem>
            {autoRefresh && (
              <MenuItem>
                <FormControl size="small" sx={{ minWidth: 150 }}>
                  <InputLabel>Refresh Interval</InputLabel>
                  <Select
                    value={refreshInterval}
                    onChange={(e) => setRefreshInterval(Number(e.target.value))}
                    label="Refresh Interval"
                  >
                    <MenuItem value={30}>30 seconds</MenuItem>
                    <MenuItem value={60}>1 minute</MenuItem>
                    <MenuItem value={300}>5 minutes</MenuItem>
                  </Select>
                </FormControl>
              </MenuItem>
            )}
            <MenuItem onClick={() => navigate('/analytics')}>
              <ListItemIcon>
                <FullscreenIcon />
              </ListItemIcon>
              <ListItemText>Full Analytics View</ListItemText>
            </MenuItem>
          </Menu>
        </Box>
      </Box>

      {loading && <LinearProgress sx={{ mb: 2 }} />}

      {/* Main KPI Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        {mockKPIData.map((kpi) => (
          <Grid item xs={12} sm={6} md={3} key={kpi.id}>
            <KPICard data={kpi} comparison={comparisonMode} onDrillDown={handleDrillDown} />
          </Grid>
        ))}
      </Grid>

      {/* Secondary KPIs */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        {mockSecondaryKPIs.map((kpi) => (
          <Grid item xs={12} sm={6} md={3} key={kpi.id}>
            <SecondaryKPI data={kpi} />
          </Grid>
        ))}
      </Grid>

      {/* Charts Row */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        {/* Utilization Trend */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3, height: 350 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" fontWeight={600}>
                Fleet Utilization Trend
              </Typography>
              {comparisonMode && (
                <Chip
                  size="small"
                  icon={<CompareIcon />}
                  label="vs Previous Period"
                  variant="outlined"
                />
              )}
            </Box>
            <ResponsiveContainer width="100%" height={280}>
              <AreaChart data={mockTrendData}>
                <defs>
                  <linearGradient id="colorValue" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor={theme.palette.primary.main} stopOpacity={0.3} />
                    <stop offset="95%" stopColor={theme.palette.primary.main} stopOpacity={0} />
                  </linearGradient>
                  {comparisonMode && (
                    <linearGradient id="colorPrevious" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor={theme.palette.grey[500]} stopOpacity={0.2} />
                      <stop offset="95%" stopColor={theme.palette.grey[500]} stopOpacity={0} />
                    </linearGradient>
                  )}
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke={alpha(theme.palette.divider, 0.5)} />
                <XAxis dataKey="date" stroke={theme.palette.text.secondary} />
                <YAxis stroke={theme.palette.text.secondary} />
                <RechartsTooltip
                  contentStyle={{
                    backgroundColor: theme.palette.background.paper,
                    borderColor: theme.palette.divider,
                    borderRadius: 8,
                  }}
                />
                <Legend />
                {comparisonMode && (
                  <Area
                    type="monotone"
                    dataKey="previousValue"
                    name="Previous Period"
                    stroke={theme.palette.grey[500]}
                    fill="url(#colorPrevious)"
                    strokeDasharray="5 5"
                  />
                )}
                <Area
                  type="monotone"
                  dataKey="value"
                  name="Current Period"
                  stroke={theme.palette.primary.main}
                  fill="url(#colorValue)"
                  strokeWidth={2}
                />
              </AreaChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* Status Distribution */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: 350 }}>
            <Typography variant="h6" fontWeight={600} sx={{ mb: 2 }}>
              Current Status Distribution
            </Typography>
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie
                  data={mockStatusDistribution}
                  cx="50%"
                  cy="50%"
                  innerRadius={50}
                  outerRadius={80}
                  paddingAngle={2}
                  dataKey="value"
                  label={({ name, value }) => `${name}: ${value}`}
                >
                  {mockStatusDistribution.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <RechartsTooltip />
              </PieChart>
            </ResponsiveContainer>
            <Box sx={{ display: 'flex', justifyContent: 'center', flexWrap: 'wrap', gap: 1 }}>
              {mockStatusDistribution.map((item) => (
                <Chip
                  key={item.name}
                  size="small"
                  label={`${item.name}: ${item.value}`}
                  sx={{
                    bgcolor: alpha(item.color, 0.15),
                    color: item.color,
                    fontWeight: 600,
                  }}
                />
              ))}
            </Box>
          </Paper>
        </Grid>
      </Grid>

      {/* Alerts Section */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <AlertIcon color="warning" />
            <Typography variant="h6" fontWeight={600}>
              Active Alerts
            </Typography>
            <Badge badgeContent={mockAlerts.length} color="error" sx={{ ml: 1 }} />
          </Box>
          <Button size="small" onClick={() => navigate('/alerts')}>
            View All
          </Button>
        </Box>
        <Grid container spacing={2}>
          {mockAlerts.map((alert) => (
            <Grid item xs={12} md={4} key={alert.id}>
              <Paper
                variant="outlined"
                sx={{
                  p: 2,
                  borderColor:
                    alert.type === 'error'
                      ? 'error.main'
                      : alert.type === 'warning'
                      ? 'warning.main'
                      : 'info.main',
                  borderLeftWidth: 4,
                }}
              >
                <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
                  {alert.type === 'error' ? (
                    <ErrorIcon color="error" />
                  ) : alert.type === 'warning' ? (
                    <WarningIcon color="warning" />
                  ) : (
                    <CheckCircleIcon color="info" />
                  )}
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="subtitle2" fontWeight={600}>
                      {alert.title}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {alert.description}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {alert.timestamp.toLocaleTimeString()}
                    </Typography>
                  </Box>
                </Box>
              </Paper>
            </Grid>
          ))}
        </Grid>
      </Paper>

      {/* Drill-down Dialog */}
      <DrillDownDialog
        open={drillDownOpen}
        onClose={() => setDrillDownOpen(false)}
        kpiId={selectedKPI}
        vehicles={mockActiveVehicles}
      />
    </Box>
  );
};

export default FleetSummaryDashboard;

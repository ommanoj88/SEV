import React, { useState, useEffect, useMemo } from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Card,
  CardContent,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  SelectChangeEvent,
  Button,
  IconButton,
  Tooltip,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TableSortLabel,
  Avatar,
  LinearProgress,
  Tabs,
  Tab,
  Divider,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Badge,
  useTheme,
  alpha,
  Switch,
  FormControlLabel,
} from '@mui/material';
import {
  AttachMoney as MoneyIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  DirectionsCar as VehicleIcon,
  EvStation as ChargingIcon,
  Build as MaintenanceIcon,
  Person as DriverIcon,
  LocalGasStation as FuelIcon,
  Security as InsuranceIcon,
  CalendarToday as CalendarIcon,
  Download as DownloadIcon,
  Refresh as RefreshIcon,
  Warning as WarningIcon,
  CheckCircle as CheckIcon,
  ArrowUpward as ArrowUpIcon,
  ArrowDownward as ArrowDownIcon,
  Compare as CompareIcon,
  Assessment as ReportIcon,
  Eco as EcoIcon,
  Speed as SpeedIcon,
  Route as RouteIcon,
  AccountBalance as BudgetIcon,
  Notifications as AlertIcon,
  Timeline as TimelineIcon,
  PieChart as PieIcon,
  BarChart as BarIcon,
  ShowChart as LineIcon,
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
  ComposedChart,
  ReferenceLine,
} from 'recharts';

// Types
interface CostCategory {
  id: string;
  name: string;
  amount: number;
  percentage: number;
  change: number;
  icon: React.ReactNode;
  color: string;
}

interface MonthlyTrend {
  month: string;
  energy: number;
  maintenance: number;
  insurance: number;
  driver: number;
  depreciation: number;
  total: number;
  budget: number;
}

interface VehicleCostData {
  id: string;
  name: string;
  licensePlate: string;
  type: string;
  totalCost: number;
  costPerKm: number;
  distance: number;
  efficiency: number;
  status: 'efficient' | 'normal' | 'inefficient';
}

interface CostAnomaly {
  id: string;
  type: 'spike' | 'unusual' | 'above_budget';
  category: string;
  vehicle?: string;
  amount: number;
  expectedAmount: number;
  date: string;
  severity: 'high' | 'medium' | 'low';
  description: string;
}

type DateRange = '3m' | '6m' | '12m' | 'ytd';
type ChartType = 'line' | 'bar' | 'area';

// Mock data
const mockCostCategories: CostCategory[] = [
  { id: 'energy', name: 'Energy/Charging', amount: 12450, percentage: 35, change: -8.2, icon: <ChargingIcon />, color: '#2196f3' },
  { id: 'maintenance', name: 'Maintenance', amount: 8920, percentage: 25, change: 12.5, icon: <MaintenanceIcon />, color: '#ff9800' },
  { id: 'insurance', name: 'Insurance', amount: 5600, percentage: 16, change: 0, icon: <InsuranceIcon />, color: '#9c27b0' },
  { id: 'driver', name: 'Driver Costs', amount: 4850, percentage: 14, change: 5.3, icon: <DriverIcon />, color: '#4caf50' },
  { id: 'depreciation', name: 'Depreciation', amount: 3580, percentage: 10, change: 0, icon: <VehicleIcon />, color: '#607d8b' },
];

const mockMonthlyTrends: MonthlyTrend[] = [
  { month: 'Jan', energy: 10200, maintenance: 7500, insurance: 5600, driver: 4200, depreciation: 3580, total: 31080, budget: 35000 },
  { month: 'Feb', energy: 9800, maintenance: 8200, insurance: 5600, driver: 4400, depreciation: 3580, total: 31580, budget: 35000 },
  { month: 'Mar', energy: 11500, maintenance: 9100, insurance: 5600, driver: 4600, depreciation: 3580, total: 34380, budget: 35000 },
  { month: 'Apr', energy: 10800, maintenance: 6800, insurance: 5600, driver: 4500, depreciation: 3580, total: 31280, budget: 35000 },
  { month: 'May', energy: 12100, maintenance: 8500, insurance: 5600, driver: 4700, depreciation: 3580, total: 34480, budget: 35000 },
  { month: 'Jun', energy: 13200, maintenance: 7200, insurance: 5600, driver: 4800, depreciation: 3580, total: 34380, budget: 35000 },
  { month: 'Jul', energy: 14500, maintenance: 9800, insurance: 5600, driver: 4900, depreciation: 3580, total: 38380, budget: 35000 },
  { month: 'Aug', energy: 13800, maintenance: 8400, insurance: 5600, driver: 4850, depreciation: 3580, total: 36230, budget: 35000 },
  { month: 'Sep', energy: 12800, maintenance: 7900, insurance: 5600, driver: 4700, depreciation: 3580, total: 34580, budget: 35000 },
  { month: 'Oct', energy: 12200, maintenance: 8600, insurance: 5600, driver: 4800, depreciation: 3580, total: 34780, budget: 35000 },
  { month: 'Nov', energy: 11800, maintenance: 9200, insurance: 5600, driver: 4750, depreciation: 3580, total: 34930, budget: 35000 },
  { month: 'Dec', energy: 12450, maintenance: 8920, insurance: 5600, driver: 4850, depreciation: 3580, total: 35400, budget: 35000 },
];

const mockVehicleCosts: VehicleCostData[] = [
  { id: 'v-001', name: 'Tesla Model 3', licensePlate: 'EV-1234', type: 'Sedan', totalCost: 2850, costPerKm: 0.08, distance: 35625, efficiency: 95, status: 'efficient' },
  { id: 'v-002', name: 'Rivian R1T', licensePlate: 'EV-5678', type: 'Truck', totalCost: 4200, costPerKm: 0.14, distance: 30000, efficiency: 78, status: 'normal' },
  { id: 'v-003', name: 'Ford E-Transit', licensePlate: 'EV-9012', type: 'Van', totalCost: 5100, costPerKm: 0.17, distance: 30000, efficiency: 72, status: 'inefficient' },
  { id: 'v-004', name: 'Chevy Bolt', licensePlate: 'EV-3456', type: 'Compact', totalCost: 2100, costPerKm: 0.07, distance: 30000, efficiency: 98, status: 'efficient' },
  { id: 'v-005', name: 'Tesla Model Y', licensePlate: 'EV-7890', type: 'SUV', totalCost: 3200, costPerKm: 0.10, distance: 32000, efficiency: 88, status: 'normal' },
  { id: 'v-006', name: 'Mercedes EQS', licensePlate: 'EV-2468', type: 'Sedan', totalCost: 3800, costPerKm: 0.12, distance: 31667, efficiency: 82, status: 'normal' },
  { id: 'v-007', name: 'VW ID.4', licensePlate: 'EV-1357', type: 'SUV', totalCost: 2600, costPerKm: 0.09, distance: 28889, efficiency: 92, status: 'efficient' },
  { id: 'v-008', name: 'Hyundai Ioniq 5', licensePlate: 'EV-8642', type: 'Hatchback', totalCost: 2400, costPerKm: 0.08, distance: 30000, efficiency: 94, status: 'efficient' },
];

const mockAnomalies: CostAnomaly[] = [
  { 
    id: 'a-001', 
    type: 'spike', 
    category: 'Maintenance', 
    vehicle: 'Ford E-Transit (EV-9012)', 
    amount: 2500, 
    expectedAmount: 800, 
    date: '2025-11-15',
    severity: 'high',
    description: 'Battery pack replacement required'
  },
  { 
    id: 'a-002', 
    type: 'above_budget', 
    category: 'Energy', 
    amount: 14500, 
    expectedAmount: 12000, 
    date: '2025-07-31',
    severity: 'medium',
    description: 'July energy costs exceeded budget by 20%'
  },
  { 
    id: 'a-003', 
    type: 'unusual', 
    category: 'Driver Costs', 
    vehicle: 'Tesla Model 3 (EV-1234)', 
    amount: 850, 
    expectedAmount: 400, 
    date: '2025-11-20',
    severity: 'low',
    description: 'Higher overtime charges this period'
  },
];

// Components
interface CostCategoryCardProps {
  category: CostCategory;
  showTrend: boolean;
}

const CostCategoryCard: React.FC<CostCategoryCardProps> = ({ category, showTrend }) => {
  const theme = useTheme();

  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
          <Avatar sx={{ bgcolor: alpha(category.color, 0.15), color: category.color }}>
            {category.icon}
          </Avatar>
          {showTrend && (
            <Chip
              size="small"
              icon={category.change > 0 ? <TrendingUpIcon /> : category.change < 0 ? <TrendingDownIcon /> : undefined}
              label={`${category.change > 0 ? '+' : ''}${category.change}%`}
              color={category.change > 0 ? 'error' : category.change < 0 ? 'success' : 'default'}
              variant="outlined"
            />
          )}
        </Box>
        <Typography variant="h5" fontWeight={700}>
          ${category.amount.toLocaleString()}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
          {category.name}
        </Typography>
        <LinearProgress
          variant="determinate"
          value={category.percentage}
          sx={{
            height: 6,
            borderRadius: 3,
            bgcolor: alpha(category.color, 0.15),
            '& .MuiLinearProgress-bar': {
              bgcolor: category.color,
              borderRadius: 3,
            },
          }}
        />
        <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
          {category.percentage}% of total costs
        </Typography>
      </CardContent>
    </Card>
  );
};

interface AnomalyItemProps {
  anomaly: CostAnomaly;
}

const AnomalyItem: React.FC<AnomalyItemProps> = ({ anomaly }) => {
  const theme = useTheme();
  
  const getSeverityColor = () => {
    switch (anomaly.severity) {
      case 'high': return 'error';
      case 'medium': return 'warning';
      case 'low': return 'info';
      default: return 'default';
    }
  };

  return (
    <ListItem
      sx={{
        borderRadius: 1,
        mb: 1,
        bgcolor: alpha(theme.palette[getSeverityColor()].main, 0.05),
        border: '1px solid',
        borderColor: alpha(theme.palette[getSeverityColor()].main, 0.2),
      }}
    >
      <ListItemIcon>
        <Avatar
          sx={{
            bgcolor: alpha(theme.palette[getSeverityColor()].main, 0.15),
            color: `${getSeverityColor()}.main`,
          }}
        >
          <WarningIcon />
        </Avatar>
      </ListItemIcon>
      <ListItemText
        primary={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography fontWeight={600}>{anomaly.category}</Typography>
            <Chip size="small" label={anomaly.type.replace('_', ' ')} color={getSeverityColor()} variant="outlined" />
          </Box>
        }
        secondary={
          <Box sx={{ mt: 0.5 }}>
            <Typography variant="body2" color="text.secondary">
              {anomaly.description}
            </Typography>
            <Box sx={{ display: 'flex', gap: 2, mt: 0.5 }}>
              <Typography variant="caption" color="error.main" fontWeight={600}>
                ${anomaly.amount.toLocaleString()}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Expected: ${anomaly.expectedAmount.toLocaleString()}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {new Date(anomaly.date).toLocaleDateString()}
              </Typography>
            </Box>
          </Box>
        }
      />
    </ListItem>
  );
};

// Main Component
const CostAnalyticsDashboard: React.FC = () => {
  const theme = useTheme();

  // State
  const [dateRange, setDateRange] = useState<DateRange>('12m');
  const [chartType, setChartType] = useState<ChartType>('area');
  const [showBudget, setShowBudget] = useState(true);
  const [showTrends, setShowTrends] = useState(true);
  const [tabValue, setTabValue] = useState(0);
  const [sortBy, setSortBy] = useState<keyof VehicleCostData>('costPerKm');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [loading, setLoading] = useState(false);

  // Calculations
  const totalCost = useMemo(() => {
    return mockCostCategories.reduce((sum, cat) => sum + cat.amount, 0);
  }, []);

  const avgCostPerKm = useMemo(() => {
    const totalDistance = mockVehicleCosts.reduce((sum, v) => sum + v.distance, 0);
    return totalCost / totalDistance;
  }, [totalCost]);

  const budgetVariance = useMemo(() => {
    const currentMonth = mockMonthlyTrends[mockMonthlyTrends.length - 1];
    return ((currentMonth.total - currentMonth.budget) / currentMonth.budget) * 100;
  }, []);

  const sortedVehicles = useMemo(() => {
    return [...mockVehicleCosts].sort((a, b) => {
      const aValue = a[sortBy];
      const bValue = b[sortBy];
      if (typeof aValue === 'number' && typeof bValue === 'number') {
        return sortOrder === 'asc' ? aValue - bValue : bValue - aValue;
      }
      return 0;
    });
  }, [sortBy, sortOrder]);

  // Handlers
  const handleDateRangeChange = (event: SelectChangeEvent<DateRange>) => {
    setDateRange(event.target.value as DateRange);
  };

  const handleSort = (column: keyof VehicleCostData) => {
    if (sortBy === column) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(column);
      setSortOrder('asc');
    }
  };

  const handleExport = () => {
    console.log('Exporting cost analytics...');
  };

  const COLORS = ['#2196f3', '#ff9800', '#9c27b0', '#4caf50', '#607d8b'];

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" fontWeight={700}>
            Cost Analytics Dashboard
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Comprehensive fleet cost visualization and analysis
          </Typography>
        </Box>
        
        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Period</InputLabel>
            <Select value={dateRange} onChange={handleDateRangeChange} label="Period">
              <MenuItem value="3m">Last 3 Months</MenuItem>
              <MenuItem value="6m">Last 6 Months</MenuItem>
              <MenuItem value="12m">Last 12 Months</MenuItem>
              <MenuItem value="ytd">Year to Date</MenuItem>
            </Select>
          </FormControl>

          <FormControlLabel
            control={<Switch checked={showTrends} onChange={(e) => setShowTrends(e.target.checked)} size="small" />}
            label="Trends"
          />
          <FormControlLabel
            control={<Switch checked={showBudget} onChange={(e) => setShowBudget(e.target.checked)} size="small" />}
            label="Budget"
          />

          <Tooltip title="Export report">
            <IconButton onClick={handleExport}>
              <DownloadIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {loading && <LinearProgress sx={{ mb: 2 }} />}

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Avatar sx={{ bgcolor: alpha(theme.palette.primary.main, 0.15), width: 56, height: 56 }}>
                <MoneyIcon color="primary" fontSize="large" />
              </Avatar>
              <Box>
                <Typography variant="h4" fontWeight={700}>
                  ${totalCost.toLocaleString()}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Total Monthly Cost
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Avatar sx={{ bgcolor: alpha(theme.palette.success.main, 0.15), width: 56, height: 56 }}>
                <SpeedIcon color="success" fontSize="large" />
              </Avatar>
              <Box>
                <Typography variant="h4" fontWeight={700}>
                  ${avgCostPerKm.toFixed(2)}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Cost per km
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Avatar
                sx={{
                  bgcolor: alpha(budgetVariance > 0 ? theme.palette.error.main : theme.palette.success.main, 0.15),
                  width: 56,
                  height: 56,
                }}
              >
                <BudgetIcon color={budgetVariance > 0 ? 'error' : 'success'} fontSize="large" />
              </Avatar>
              <Box>
                <Typography variant="h4" fontWeight={700} color={budgetVariance > 0 ? 'error.main' : 'success.main'}>
                  {budgetVariance > 0 ? '+' : ''}{budgetVariance.toFixed(1)}%
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Budget Variance
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Badge badgeContent={mockAnomalies.length} color="error">
                <Avatar sx={{ bgcolor: alpha(theme.palette.warning.main, 0.15), width: 56, height: 56 }}>
                  <AlertIcon color="warning" fontSize="large" />
                </Avatar>
              </Badge>
              <Box>
                <Typography variant="h4" fontWeight={700}>
                  {mockAnomalies.length}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Cost Anomalies
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>
      </Grid>

      {/* Cost Categories */}
      <Typography variant="h6" fontWeight={600} sx={{ mb: 2 }}>
        Cost Breakdown by Category
      </Typography>
      <Grid container spacing={2} sx={{ mb: 3 }}>
        {mockCostCategories.map((category) => (
          <Grid item xs={12} sm={6} md={2.4} key={category.id}>
            <CostCategoryCard category={category} showTrend={showTrends} />
          </Grid>
        ))}
      </Grid>

      {/* Charts Row */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        {/* Cost Trend Chart */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3, height: 420 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" fontWeight={600}>
                Monthly Cost Trend
              </Typography>
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Tooltip title="Line Chart">
                  <IconButton
                    size="small"
                    onClick={() => setChartType('line')}
                    color={chartType === 'line' ? 'primary' : 'default'}
                  >
                    <LineIcon />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Bar Chart">
                  <IconButton
                    size="small"
                    onClick={() => setChartType('bar')}
                    color={chartType === 'bar' ? 'primary' : 'default'}
                  >
                    <BarIcon />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Area Chart">
                  <IconButton
                    size="small"
                    onClick={() => setChartType('area')}
                    color={chartType === 'area' ? 'primary' : 'default'}
                  >
                    <TimelineIcon />
                  </IconButton>
                </Tooltip>
              </Box>
            </Box>
            <ResponsiveContainer width="100%" height={350}>
              <ComposedChart data={mockMonthlyTrends}>
                <CartesianGrid strokeDasharray="3 3" stroke={alpha(theme.palette.divider, 0.5)} />
                <XAxis dataKey="month" stroke={theme.palette.text.secondary} />
                <YAxis stroke={theme.palette.text.secondary} tickFormatter={(value) => `$${value / 1000}k`} />
                <RechartsTooltip
                  contentStyle={{
                    backgroundColor: theme.palette.background.paper,
                    borderColor: theme.palette.divider,
                    borderRadius: 8,
                  }}
                  formatter={(value: number) => [`$${value.toLocaleString()}`, '']}
                />
                <Legend />
                
                {chartType === 'area' ? (
                  <>
                    <Area type="monotone" dataKey="energy" name="Energy" stackId="1" fill={COLORS[0]} stroke={COLORS[0]} fillOpacity={0.6} />
                    <Area type="monotone" dataKey="maintenance" name="Maintenance" stackId="1" fill={COLORS[1]} stroke={COLORS[1]} fillOpacity={0.6} />
                    <Area type="monotone" dataKey="insurance" name="Insurance" stackId="1" fill={COLORS[2]} stroke={COLORS[2]} fillOpacity={0.6} />
                    <Area type="monotone" dataKey="driver" name="Driver" stackId="1" fill={COLORS[3]} stroke={COLORS[3]} fillOpacity={0.6} />
                    <Area type="monotone" dataKey="depreciation" name="Depreciation" stackId="1" fill={COLORS[4]} stroke={COLORS[4]} fillOpacity={0.6} />
                  </>
                ) : chartType === 'bar' ? (
                  <>
                    <Bar dataKey="energy" name="Energy" stackId="a" fill={COLORS[0]} />
                    <Bar dataKey="maintenance" name="Maintenance" stackId="a" fill={COLORS[1]} />
                    <Bar dataKey="insurance" name="Insurance" stackId="a" fill={COLORS[2]} />
                    <Bar dataKey="driver" name="Driver" stackId="a" fill={COLORS[3]} />
                    <Bar dataKey="depreciation" name="Depreciation" stackId="a" fill={COLORS[4]} />
                  </>
                ) : (
                  <>
                    <Line type="monotone" dataKey="total" name="Total" stroke={theme.palette.primary.main} strokeWidth={2} dot />
                    <Line type="monotone" dataKey="energy" name="Energy" stroke={COLORS[0]} strokeDasharray="5 5" />
                    <Line type="monotone" dataKey="maintenance" name="Maintenance" stroke={COLORS[1]} strokeDasharray="5 5" />
                  </>
                )}
                
                {showBudget && (
                  <ReferenceLine y={35000} stroke={theme.palette.error.main} strokeDasharray="3 3" label="Budget" />
                )}
              </ComposedChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* Pie Chart */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: 420 }}>
            <Typography variant="h6" fontWeight={600} sx={{ mb: 2 }}>
              Cost Distribution
            </Typography>
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie
                  data={mockCostCategories}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={100}
                  paddingAngle={2}
                  dataKey="amount"
                  nameKey="name"
                  label={({ name, percentage }) => `${name}: ${percentage}%`}
                  labelLine={false}
                >
                  {mockCostCategories.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <RechartsTooltip
                  formatter={(value: number) => [`$${value.toLocaleString()}`, '']}
                />
              </PieChart>
            </ResponsiveContainer>
            <Box sx={{ display: 'flex', justifyContent: 'center', flexWrap: 'wrap', gap: 1 }}>
              {mockCostCategories.map((cat) => (
                <Chip
                  key={cat.id}
                  size="small"
                  label={cat.name}
                  sx={{
                    bgcolor: alpha(cat.color, 0.15),
                    color: cat.color,
                    fontWeight: 600,
                  }}
                />
              ))}
            </Box>
          </Paper>
        </Grid>
      </Grid>

      {/* Tabs: Vehicle Comparison & Anomalies */}
      <Paper sx={{ mb: 3 }}>
        <Tabs value={tabValue} onChange={(_, v) => setTabValue(v)} sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tab label="Vehicle Cost Comparison" icon={<VehicleIcon />} iconPosition="start" />
          <Tab label={<Badge badgeContent={mockAnomalies.length} color="error">Cost Anomalies</Badge>} icon={<WarningIcon />} iconPosition="start" />
        </Tabs>

        {/* Vehicle Comparison Table */}
        {tabValue === 0 && (
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Vehicle</TableCell>
                  <TableCell align="right">
                    <TableSortLabel
                      active={sortBy === 'totalCost'}
                      direction={sortBy === 'totalCost' ? sortOrder : 'asc'}
                      onClick={() => handleSort('totalCost')}
                    >
                      Total Cost
                    </TableSortLabel>
                  </TableCell>
                  <TableCell align="right">
                    <TableSortLabel
                      active={sortBy === 'costPerKm'}
                      direction={sortBy === 'costPerKm' ? sortOrder : 'asc'}
                      onClick={() => handleSort('costPerKm')}
                    >
                      Cost/km
                    </TableSortLabel>
                  </TableCell>
                  <TableCell align="right">
                    <TableSortLabel
                      active={sortBy === 'distance'}
                      direction={sortBy === 'distance' ? sortOrder : 'asc'}
                      onClick={() => handleSort('distance')}
                    >
                      Distance
                    </TableSortLabel>
                  </TableCell>
                  <TableCell align="right">
                    <TableSortLabel
                      active={sortBy === 'efficiency'}
                      direction={sortBy === 'efficiency' ? sortOrder : 'asc'}
                      onClick={() => handleSort('efficiency')}
                    >
                      Efficiency
                    </TableSortLabel>
                  </TableCell>
                  <TableCell>Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {sortedVehicles.map((vehicle) => (
                  <TableRow
                    key={vehicle.id}
                    hover
                    sx={{
                      '&:hover': {
                        bgcolor: alpha(theme.palette.primary.main, 0.05),
                      },
                    }}
                  >
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                        <Avatar sx={{ bgcolor: alpha(theme.palette.primary.main, 0.15) }}>
                          <VehicleIcon color="primary" />
                        </Avatar>
                        <Box>
                          <Typography fontWeight={600}>{vehicle.name}</Typography>
                          <Typography variant="caption" color="text.secondary">
                            {vehicle.licensePlate} • {vehicle.type}
                          </Typography>
                        </Box>
                      </Box>
                    </TableCell>
                    <TableCell align="right">
                      <Typography fontWeight={600}>${vehicle.totalCost.toLocaleString()}</Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography
                        fontWeight={600}
                        color={vehicle.costPerKm <= 0.08 ? 'success.main' : vehicle.costPerKm >= 0.15 ? 'error.main' : 'text.primary'}
                      >
                        ${vehicle.costPerKm.toFixed(2)}
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography>{vehicle.distance.toLocaleString()} km</Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <LinearProgress
                          variant="determinate"
                          value={vehicle.efficiency}
                          sx={{
                            width: 60,
                            height: 6,
                            borderRadius: 3,
                            bgcolor: alpha(
                              vehicle.efficiency >= 90 ? theme.palette.success.main : vehicle.efficiency >= 75 ? theme.palette.warning.main : theme.palette.error.main,
                              0.15
                            ),
                            '& .MuiLinearProgress-bar': {
                              bgcolor: vehicle.efficiency >= 90 ? 'success.main' : vehicle.efficiency >= 75 ? 'warning.main' : 'error.main',
                              borderRadius: 3,
                            },
                          }}
                        />
                        <Typography variant="body2">{vehicle.efficiency}%</Typography>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        icon={vehicle.status === 'efficient' ? <CheckIcon /> : vehicle.status === 'inefficient' ? <WarningIcon /> : undefined}
                        label={vehicle.status}
                        color={vehicle.status === 'efficient' ? 'success' : vehicle.status === 'inefficient' ? 'error' : 'default'}
                        variant="outlined"
                      />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}

        {/* Anomalies Tab */}
        {tabValue === 1 && (
          <Box sx={{ p: 2 }}>
            <List>
              {mockAnomalies.map((anomaly) => (
                <AnomalyItem key={anomaly.id} anomaly={anomaly} />
              ))}
            </List>
            {mockAnomalies.length === 0 && (
              <Box sx={{ textAlign: 'center', py: 4 }}>
                <CheckIcon sx={{ fontSize: 48, color: 'success.main', mb: 2 }} />
                <Typography variant="h6" color="success.main">
                  No Cost Anomalies Detected
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  All costs are within expected ranges
                </Typography>
              </Box>
            )}
          </Box>
        )}
      </Paper>
    </Box>
  );
};

export default CostAnalyticsDashboard;

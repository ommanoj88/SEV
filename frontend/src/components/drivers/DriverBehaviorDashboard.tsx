import React, { useEffect, useState, useMemo } from 'react';
import {
  Box,
  Paper,
  Grid,
  Typography,
  Card,
  CardContent,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  LinearProgress,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Button,
  Tooltip,
  IconButton,
  CircularProgress,
  useTheme,
  alpha,
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  Download,
  Speed,
  Warning,
  CheckCircle,
  DirectionsCar,
  Schedule,
  LocalGasStation,
  Security,
  Refresh,
} from '@mui/icons-material';
import { useParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@redux/hooks';
import { fetchDriverById, fetchDriverBehavior, selectDriverBehavior } from '@redux/slices/driverSlice';
import { formatDate } from '@utils/formatters';
import {
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  Legend,
  ResponsiveContainer,
  BarChart,
  Bar,
} from 'recharts';
import { DriverBehavior } from '@/types';

/**
 * Driver Behavior Dashboard Component
 * 
 * Displays comprehensive driver behavior analytics including:
 * - Safety score gauge (0-100)
 * - Recent driving events table
 * - Event type breakdown chart
 * - Safety score trend over time
 * - Date range filtering
 * - CSV export functionality
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */

interface DateRangeOption {
  label: string;
  days: number;
}

const DATE_RANGE_OPTIONS: DateRangeOption[] = [
  { label: 'Last 7 Days', days: 7 },
  { label: 'Last 14 Days', days: 14 },
  { label: 'Last 30 Days', days: 30 },
  { label: 'Last 90 Days', days: 90 },
];

// Color scheme for event types
const EVENT_COLORS = {
  harshAccelerations: '#FF6B6B',
  harshBraking: '#FFA94D',
  harshCornering: '#FFD43B',
  speeding: '#FF4757',
  idling: '#748FFC',
};

// Safety score gauge colors
const getScoreColor = (score: number): string => {
  if (score >= 90) return '#4CAF50'; // Green - Excellent
  if (score >= 80) return '#8BC34A'; // Light Green - Good
  if (score >= 70) return '#FFEB3B'; // Yellow - Average
  if (score >= 60) return '#FFC107'; // Amber - Below Average
  return '#F44336'; // Red - Poor
};

const getScoreLabel = (score: number): string => {
  if (score >= 90) return 'Excellent';
  if (score >= 80) return 'Good';
  if (score >= 70) return 'Average';
  if (score >= 60) return 'Below Average';
  return 'Needs Improvement';
};

const DriverBehaviorDashboard: React.FC = () => {
  const theme = useTheme();
  const { id } = useParams<{ id: string }>();
  const dispatch = useAppDispatch();
  
  const { selectedDriver, loading } = useAppSelector((state) => state.drivers);
  const behaviorData = useAppSelector(selectDriverBehavior);
  
  const [dateRange, setDateRange] = useState<number>(7);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    if (id) {
      dispatch(fetchDriverById(Number(id)));
      dispatch(fetchDriverBehavior(Number(id)));
    }
  }, [id, dispatch]);

  // Filter behavior data by date range
  const filteredData = useMemo(() => {
    const cutoffDate = new Date();
    cutoffDate.setDate(cutoffDate.getDate() - dateRange);
    
    return behaviorData.filter((record) => {
      const recordDate = new Date(record.date);
      return recordDate >= cutoffDate;
    });
  }, [behaviorData, dateRange]);

  // Calculate aggregate metrics
  const aggregateMetrics = useMemo(() => {
    if (filteredData.length === 0) {
      return {
        avgSafetyScore: 0,
        totalHarshAccelerations: 0,
        totalHarshBraking: 0,
        totalHarshCornering: 0,
        totalSpeeding: 0,
        totalIdling: 0,
        totalTrips: 0,
        trend: 0,
      };
    }

    const totals = filteredData.reduce(
      (acc, record) => ({
        safetyScore: acc.safetyScore + (record.safetyScore || record.score),
        harshAccelerations: acc.harshAccelerations + record.harshAccelerations,
        harshBraking: acc.harshBraking + record.harshBraking,
        harshCornering: acc.harshCornering + record.harshCornering,
        speeding: acc.speeding + record.speeding,
        idling: acc.idling + record.idling,
      }),
      { safetyScore: 0, harshAccelerations: 0, harshBraking: 0, harshCornering: 0, speeding: 0, idling: 0 }
    );

    // Calculate trend (compare first half vs second half)
    const midpoint = Math.floor(filteredData.length / 2);
    const firstHalf = filteredData.slice(0, midpoint);
    const secondHalf = filteredData.slice(midpoint);
    
    const firstHalfAvg = firstHalf.length > 0
      ? firstHalf.reduce((sum, r) => sum + (r.safetyScore || r.score), 0) / firstHalf.length
      : 0;
    const secondHalfAvg = secondHalf.length > 0
      ? secondHalf.reduce((sum, r) => sum + (r.safetyScore || r.score), 0) / secondHalf.length
      : 0;

    return {
      avgSafetyScore: Math.round(totals.safetyScore / filteredData.length),
      totalHarshAccelerations: totals.harshAccelerations,
      totalHarshBraking: totals.harshBraking,
      totalHarshCornering: totals.harshCornering,
      totalSpeeding: totals.speeding,
      totalIdling: Math.round(totals.idling),
      totalTrips: filteredData.length,
      trend: Math.round(secondHalfAvg - firstHalfAvg),
    };
  }, [filteredData]);

  // Prepare chart data
  const pieChartData = useMemo(() => [
    { name: 'Harsh Accelerations', value: aggregateMetrics.totalHarshAccelerations, color: EVENT_COLORS.harshAccelerations },
    { name: 'Harsh Braking', value: aggregateMetrics.totalHarshBraking, color: EVENT_COLORS.harshBraking },
    { name: 'Harsh Cornering', value: aggregateMetrics.totalHarshCornering, color: EVENT_COLORS.harshCornering },
    { name: 'Speeding', value: aggregateMetrics.totalSpeeding, color: EVENT_COLORS.speeding },
    { name: 'Idling (min)', value: aggregateMetrics.totalIdling, color: EVENT_COLORS.idling },
  ], [aggregateMetrics]);

  const trendChartData = useMemo(() => {
    return filteredData
      .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())
      .map((record) => ({
        date: formatDate(record.date),
        score: record.safetyScore || record.score,
        harshEvents: record.harshAccelerations + record.harshBraking + record.harshCornering,
        speeding: record.speeding,
      }));
  }, [filteredData]);

  // Export to CSV
  const handleExportCSV = () => {
    const headers = ['Date', 'Trip ID', 'Safety Score', 'Harsh Accelerations', 'Harsh Braking', 'Harsh Cornering', 'Speeding', 'Idling (min)'];
    const rows = filteredData.map((record) => [
      formatDate(record.date),
      record.tripId,
      record.safetyScore || record.score,
      record.harshAccelerations,
      record.harshBraking,
      record.harshCornering,
      record.speeding,
      record.idling,
    ]);

    const csvContent = [
      headers.join(','),
      ...rows.map((row) => row.join(',')),
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `driver_${id}_behavior_${new Date().toISOString().split('T')[0]}.csv`;
    link.click();
  };

  // Refresh data
  const handleRefresh = async () => {
    setRefreshing(true);
    if (id) {
      await dispatch(fetchDriverBehavior(Number(id)));
    }
    setRefreshing(false);
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  if (!selectedDriver) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <Typography color="text.secondary">Driver not found</Typography>
      </Box>
    );
  }

  return (
    <Box>
      {/* Header Section */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h5" fontWeight={600}>
            Driver Behavior Dashboard
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {selectedDriver.firstName} {selectedDriver.lastName} • {selectedDriver.email}
          </Typography>
        </Box>
        <Box display="flex" gap={2}>
          <FormControl size="small" sx={{ minWidth: 150 }}>
            <InputLabel>Date Range</InputLabel>
            <Select
              value={dateRange}
              label="Date Range"
              onChange={(e) => setDateRange(e.target.value as number)}
            >
              {DATE_RANGE_OPTIONS.map((option) => (
                <MenuItem key={option.days} value={option.days}>
                  {option.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <Tooltip title="Refresh Data">
            <IconButton onClick={handleRefresh} disabled={refreshing}>
              <Refresh className={refreshing ? 'rotating' : ''} />
            </IconButton>
          </Tooltip>
          <Button
            variant="outlined"
            startIcon={<Download />}
            onClick={handleExportCSV}
            disabled={filteredData.length === 0}
          >
            Export CSV
          </Button>
        </Box>
      </Box>

      <Grid container spacing={3}>
        {/* Safety Score Gauge Card */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Typography variant="h6" gutterBottom>
              Safety Score
            </Typography>
            <Box
              display="flex"
              flexDirection="column"
              alignItems="center"
              justifyContent="center"
              py={2}
            >
              {/* Circular Score Gauge */}
              <Box position="relative" display="inline-flex">
                <CircularProgress
                  variant="determinate"
                  value={100}
                  size={160}
                  thickness={4}
                  sx={{ color: alpha(theme.palette.grey[300], 0.3) }}
                />
                <CircularProgress
                  variant="determinate"
                  value={aggregateMetrics.avgSafetyScore}
                  size={160}
                  thickness={4}
                  sx={{
                    color: getScoreColor(aggregateMetrics.avgSafetyScore),
                    position: 'absolute',
                    left: 0,
                  }}
                />
                <Box
                  position="absolute"
                  top={0}
                  left={0}
                  bottom={0}
                  right={0}
                  display="flex"
                  flexDirection="column"
                  alignItems="center"
                  justifyContent="center"
                >
                  <Typography variant="h3" fontWeight={700}>
                    {aggregateMetrics.avgSafetyScore}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    out of 100
                  </Typography>
                </Box>
              </Box>
              
              <Chip
                label={getScoreLabel(aggregateMetrics.avgSafetyScore)}
                sx={{
                  mt: 2,
                  bgcolor: alpha(getScoreColor(aggregateMetrics.avgSafetyScore), 0.1),
                  color: getScoreColor(aggregateMetrics.avgSafetyScore),
                  fontWeight: 600,
                }}
              />
              
              {/* Trend Indicator */}
              <Box display="flex" alignItems="center" mt={2}>
                {aggregateMetrics.trend > 0 ? (
                  <>
                    <TrendingUp sx={{ color: 'success.main', mr: 0.5 }} />
                    <Typography color="success.main" fontWeight={500}>
                      +{aggregateMetrics.trend} pts
                    </Typography>
                  </>
                ) : aggregateMetrics.trend < 0 ? (
                  <>
                    <TrendingDown sx={{ color: 'error.main', mr: 0.5 }} />
                    <Typography color="error.main" fontWeight={500}>
                      {aggregateMetrics.trend} pts
                    </Typography>
                  </>
                ) : (
                  <Typography color="text.secondary">No change</Typography>
                )}
                <Typography variant="caption" color="text.secondary" ml={1}>
                  vs previous period
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>

        {/* Event Stats Cards */}
        <Grid item xs={12} md={8}>
          <Grid container spacing={2}>
            <Grid item xs={6} sm={4}>
              <Card sx={{ bgcolor: alpha(EVENT_COLORS.harshAccelerations, 0.1) }}>
                <CardContent>
                  <Box display="flex" alignItems="center" mb={1}>
                    <Speed sx={{ color: EVENT_COLORS.harshAccelerations, mr: 1 }} />
                    <Typography variant="body2" color="text.secondary">
                      Harsh Accel.
                    </Typography>
                  </Box>
                  <Typography variant="h4" fontWeight={600}>
                    {aggregateMetrics.totalHarshAccelerations}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={6} sm={4}>
              <Card sx={{ bgcolor: alpha(EVENT_COLORS.harshBraking, 0.1) }}>
                <CardContent>
                  <Box display="flex" alignItems="center" mb={1}>
                    <Warning sx={{ color: EVENT_COLORS.harshBraking, mr: 1 }} />
                    <Typography variant="body2" color="text.secondary">
                      Harsh Braking
                    </Typography>
                  </Box>
                  <Typography variant="h4" fontWeight={600}>
                    {aggregateMetrics.totalHarshBraking}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={6} sm={4}>
              <Card sx={{ bgcolor: alpha(EVENT_COLORS.harshCornering, 0.1) }}>
                <CardContent>
                  <Box display="flex" alignItems="center" mb={1}>
                    <DirectionsCar sx={{ color: EVENT_COLORS.harshCornering, mr: 1 }} />
                    <Typography variant="body2" color="text.secondary">
                      Harsh Cornering
                    </Typography>
                  </Box>
                  <Typography variant="h4" fontWeight={600}>
                    {aggregateMetrics.totalHarshCornering}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={6} sm={4}>
              <Card sx={{ bgcolor: alpha(EVENT_COLORS.speeding, 0.1) }}>
                <CardContent>
                  <Box display="flex" alignItems="center" mb={1}>
                    <Speed sx={{ color: EVENT_COLORS.speeding, mr: 1 }} />
                    <Typography variant="body2" color="text.secondary">
                      Speeding Events
                    </Typography>
                  </Box>
                  <Typography variant="h4" fontWeight={600}>
                    {aggregateMetrics.totalSpeeding}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={6} sm={4}>
              <Card sx={{ bgcolor: alpha(EVENT_COLORS.idling, 0.1) }}>
                <CardContent>
                  <Box display="flex" alignItems="center" mb={1}>
                    <Schedule sx={{ color: EVENT_COLORS.idling, mr: 1 }} />
                    <Typography variant="body2" color="text.secondary">
                      Idling (min)
                    </Typography>
                  </Box>
                  <Typography variant="h4" fontWeight={600}>
                    {aggregateMetrics.totalIdling}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={6} sm={4}>
              <Card sx={{ bgcolor: alpha(theme.palette.success.main, 0.1) }}>
                <CardContent>
                  <Box display="flex" alignItems="center" mb={1}>
                    <CheckCircle sx={{ color: 'success.main', mr: 1 }} />
                    <Typography variant="body2" color="text.secondary">
                      Total Trips
                    </Typography>
                  </Box>
                  <Typography variant="h4" fontWeight={600}>
                    {aggregateMetrics.totalTrips}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </Grid>

        {/* Event Breakdown Pie Chart */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3, height: 400 }}>
            <Typography variant="h6" gutterBottom>
              Event Type Breakdown
            </Typography>
            <ResponsiveContainer width="100%" height="85%">
              <PieChart>
                <Pie
                  data={pieChartData}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  outerRadius={120}
                  label={({ name, percent }) => 
                    `${name}: ${(percent * 100).toFixed(0)}%`
                  }
                  labelLine={false}
                >
                  {pieChartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <RechartsTooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* Safety Score Trend Chart */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3, height: 400 }}>
            <Typography variant="h6" gutterBottom>
              Safety Score Trend
            </Typography>
            <ResponsiveContainer width="100%" height="85%">
              <LineChart data={trendChartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" tick={{ fontSize: 12 }} />
                <YAxis domain={[0, 100]} />
                <RechartsTooltip />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="score"
                  name="Safety Score"
                  stroke="#4CAF50"
                  strokeWidth={2}
                  dot={{ r: 4 }}
                  activeDot={{ r: 6 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* Recent Events Table */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Recent Driving Events
            </Typography>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Date</TableCell>
                    <TableCell>Trip ID</TableCell>
                    <TableCell align="center">Safety Score</TableCell>
                    <TableCell align="center">Harsh Accel.</TableCell>
                    <TableCell align="center">Harsh Braking</TableCell>
                    <TableCell align="center">Harsh Cornering</TableCell>
                    <TableCell align="center">Speeding</TableCell>
                    <TableCell align="center">Idling (min)</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredData.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={8} align="center">
                        <Typography color="text.secondary" py={3}>
                          No driving events found for the selected period
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    filteredData
                      .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
                      .slice(0, 10)
                      .map((record) => (
                        <TableRow key={record.id} hover>
                          <TableCell>{formatDate(record.date)}</TableCell>
                          <TableCell>
                            <Chip 
                              label={record.tripId.slice(0, 8)} 
                              size="small" 
                              variant="outlined" 
                            />
                          </TableCell>
                          <TableCell align="center">
                            <Box display="flex" alignItems="center" justifyContent="center">
                              <Box
                                sx={{
                                  width: 12,
                                  height: 12,
                                  borderRadius: '50%',
                                  bgcolor: getScoreColor(record.safetyScore || record.score),
                                  mr: 1,
                                }}
                              />
                              <Typography fontWeight={500}>
                                {record.safetyScore || record.score}
                              </Typography>
                            </Box>
                          </TableCell>
                          <TableCell align="center">
                            {record.harshAccelerations > 0 ? (
                              <Chip
                                label={record.harshAccelerations}
                                size="small"
                                sx={{ bgcolor: alpha(EVENT_COLORS.harshAccelerations, 0.2) }}
                              />
                            ) : (
                              '-'
                            )}
                          </TableCell>
                          <TableCell align="center">
                            {record.harshBraking > 0 ? (
                              <Chip
                                label={record.harshBraking}
                                size="small"
                                sx={{ bgcolor: alpha(EVENT_COLORS.harshBraking, 0.2) }}
                              />
                            ) : (
                              '-'
                            )}
                          </TableCell>
                          <TableCell align="center">
                            {record.harshCornering > 0 ? (
                              <Chip
                                label={record.harshCornering}
                                size="small"
                                sx={{ bgcolor: alpha(EVENT_COLORS.harshCornering, 0.2) }}
                              />
                            ) : (
                              '-'
                            )}
                          </TableCell>
                          <TableCell align="center">
                            {record.speeding > 0 ? (
                              <Chip
                                label={record.speeding}
                                size="small"
                                color="error"
                                variant="outlined"
                              />
                            ) : (
                              '-'
                            )}
                          </TableCell>
                          <TableCell align="center">
                            {record.idling > 0 ? (
                              <Typography color="text.secondary">
                                {record.idling}
                              </Typography>
                            ) : (
                              '-'
                            )}
                          </TableCell>
                        </TableRow>
                      ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
            {filteredData.length > 10 && (
              <Box display="flex" justifyContent="center" mt={2}>
                <Typography variant="body2" color="text.secondary">
                  Showing 10 of {filteredData.length} records. Export to CSV for full data.
                </Typography>
              </Box>
            )}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default DriverBehaviorDashboard;

import React from 'react';
import { 
  Paper, 
  Typography, 
  Grid, 
  Box, 
  Card, 
  CardContent, 
  Chip,
  Divider,
  Stack,
  useMediaQuery
} from '@mui/material';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  Legend, 
  ResponsiveContainer,
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell,
  Area,
  AreaChart
} from 'recharts';
import { useTheme } from '@mui/material/styles';
import { useAppSelector } from '../../redux/hooks';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import EcoIcon from '@mui/icons-material/Eco';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import TimelineIcon from '@mui/icons-material/Timeline';
import LocalGasStationIcon from '@mui/icons-material/LocalGasStation';
import BuildIcon from '@mui/icons-material/Build';

const TCOAnalysis: React.FC = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const { tcoAnalysis } = useAppSelector((state) => state.analytics);

  // Calculate values with defaults
  const purchasePrice = tcoAnalysis?.purchasePrice || 45000;
  const energyCosts = tcoAnalysis?.energyCosts || 5000;
  const maintenanceCosts = tcoAnalysis?.maintenanceCosts || 2000;
  const insuranceCosts = tcoAnalysis?.insuranceCosts || 2500;
  const totalCost = tcoAnalysis?.totalCost || (purchasePrice + energyCosts + maintenanceCosts + insuranceCosts);
  
  const totalSavings = tcoAnalysis?.comparisonWithICE?.totalSavings || 12500;
  const roi = tcoAnalysis && tcoAnalysis.totalCost > 0
    ? ((totalSavings / tcoAnalysis.totalCost) * 100)
    : 23.1;
  const paybackPeriod = tcoAnalysis?.comparisonWithICE?.paybackPeriod || 36;

  // Enhanced comparison data
  const comparisonData = [
    { name: 'Acquisition', EV: purchasePrice, ICE: 35000, category: 'Initial' },
    { name: 'Energy/Fuel (5yr)', EV: energyCosts, ICE: 15000, category: 'Operating' },
    { name: 'Maintenance (5yr)', EV: maintenanceCosts, ICE: 8000, category: 'Operating' },
    { name: 'Insurance (5yr)', EV: insuranceCosts, ICE: 2000, category: 'Operating' },
  ];

  // 5-year cost projection
  const projectionData = [
    { year: 'Year 1', EV: purchasePrice + (energyCosts * 0.2) + (maintenanceCosts * 0.2), ICE: 35000 + (15000 * 0.2) + (8000 * 0.2) },
    { year: 'Year 2', EV: purchasePrice + (energyCosts * 0.4) + (maintenanceCosts * 0.4), ICE: 35000 + (15000 * 0.4) + (8000 * 0.4) },
    { year: 'Year 3', EV: purchasePrice + (energyCosts * 0.6) + (maintenanceCosts * 0.6), ICE: 35000 + (15000 * 0.6) + (8000 * 0.6) },
    { year: 'Year 4', EV: purchasePrice + (energyCosts * 0.8) + (maintenanceCosts * 0.8), ICE: 35000 + (15000 * 0.8) + (8000 * 0.8) },
    { year: 'Year 5', EV: totalCost, ICE: 35000 + 15000 + 8000 + 2000 },
  ];

  // Cost breakdown for pie chart
  const costBreakdown = [
    { name: 'Purchase', value: purchasePrice, color: theme.palette.primary.main },
    { name: 'Energy', value: energyCosts, color: theme.palette.info.main },
    { name: 'Maintenance', value: maintenanceCosts, color: theme.palette.warning.main },
    { name: 'Insurance', value: insuranceCosts, color: theme.palette.error.light },
  ];

  // Custom tooltip for better data display
  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      return (
        <Paper sx={{ p: 2, border: `1px solid ${theme.palette.divider}` }}>
          <Typography variant="subtitle2" gutterBottom>{label}</Typography>
          {payload.map((entry: any, index: number) => (
            <Typography key={index} variant="body2" sx={{ color: entry.color }}>
              {entry.name}: ${entry.value.toLocaleString()}
            </Typography>
          ))}
          {payload.length === 2 && (
            <Typography variant="caption" color="success.main" sx={{ mt: 1, display: 'block' }}>
              Savings: ${(payload[1].value - payload[0].value).toLocaleString()}
            </Typography>
          )}
        </Paper>
      );
    }
    return null;
  };

  return (
    <Box>
      {/* Header with summary */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom fontWeight="bold">
          Total Cost of Ownership Analysis
        </Typography>
        <Typography variant="body1" color="text.secondary" paragraph>
          Comprehensive 5-year cost comparison between Electric Vehicles and ICE vehicles
        </Typography>
      </Box>

      {/* Key Metrics Cards */}
      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} sm={6} md={3}>
          <Card elevation={3} sx={{ height: '100%', background: `linear-gradient(135deg, ${theme.palette.success.light} 0%, ${theme.palette.success.main} 100%)` }}>
            <CardContent>
              <Stack direction="row" alignItems="center" spacing={1} mb={1}>
                <AttachMoneyIcon sx={{ color: 'white' }} />
                <Typography variant="overline" sx={{ color: 'white', fontWeight: 'bold' }}>
                  Total Savings
                </Typography>
              </Stack>
              <Typography variant="h4" sx={{ color: 'white', fontWeight: 'bold' }}>
                ${totalSavings.toLocaleString('en-US', { maximumFractionDigits: 0 })}
              </Typography>
              <Typography variant="body2" sx={{ color: 'white', opacity: 0.9, mt: 1 }}>
                Over 5 years vs ICE
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card elevation={3}>
            <CardContent>
              <Stack direction="row" alignItems="center" spacing={1} mb={1}>
                <TimelineIcon color="primary" />
                <Typography variant="overline" color="text.secondary" fontWeight="bold">
                  Return on Investment
                </Typography>
              </Stack>
              <Stack direction="row" alignItems="baseline" spacing={1}>
                <Typography variant="h4" color="primary.main" fontWeight="bold">
                  {roi.toFixed(1)}%
                </Typography>
                <Chip 
                  icon={roi > 0 ? <TrendingUpIcon /> : <TrendingDownIcon />}
                  label={roi > 0 ? 'Positive' : 'Negative'} 
                  size="small" 
                  color={roi > 0 ? 'success' : 'error'}
                />
              </Stack>
              <Typography variant="body2" color="text.secondary" mt={1}>
                5-year investment return
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card elevation={3}>
            <CardContent>
              <Stack direction="row" alignItems="center" spacing={1} mb={1}>
                <LocalGasStationIcon color="info" />
                <Typography variant="overline" color="text.secondary" fontWeight="bold">
                  Payback Period
                </Typography>
              </Stack>
              <Typography variant="h4" color="info.main" fontWeight="bold">
                {paybackPeriod} mo
              </Typography>
              <Typography variant="body2" color="text.secondary" mt={1}>
                Break-even timeline
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card elevation={3}>
            <CardContent>
              <Stack direction="row" alignItems="center" spacing={1} mb={1}>
                <EcoIcon sx={{ color: theme.palette.success.main }} />
                <Typography variant="overline" color="text.secondary" fontWeight="bold">
                  CO₂ Savings
                </Typography>
              </Stack>
              <Typography variant="h4" color="success.main" fontWeight="bold">
                {((totalSavings / 500) * 0.3).toFixed(1)}t
              </Typography>
              <Typography variant="body2" color="text.secondary" mt={1}>
                Carbon emissions avoided
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Main Comparison Charts */}
      <Grid container spacing={3} mb={4}>
        {/* Cost Comparison Bar Chart */}
        <Grid item xs={12} lg={8}>
          <Paper elevation={3} sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom fontWeight="bold">
              Cost Component Comparison (5-Year Period)
            </Typography>
            <Typography variant="body2" color="text.secondary" paragraph>
              Detailed breakdown of ownership costs: EV vs ICE vehicles
            </Typography>
            <Divider sx={{ mb: 3 }} />
            <ResponsiveContainer width="100%" height={isMobile ? 300 : 400}>
              <BarChart data={comparisonData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke={theme.palette.divider} />
                <XAxis 
                  dataKey="name" 
                  tick={{ fontSize: 12 }}
                  angle={isMobile ? -45 : 0}
                  textAnchor={isMobile ? 'end' : 'middle'}
                  height={isMobile ? 80 : 50}
                />
                <YAxis 
                  tick={{ fontSize: 12 }}
                  tickFormatter={(value) => `$${(value / 1000).toFixed(0)}k`}
                />
                <Tooltip content={<CustomTooltip />} />
                <Legend 
                  wrapperStyle={{ paddingTop: '20px' }}
                  iconType="circle"
                />
                <Bar 
                  dataKey="EV" 
                  fill={theme.palette.success.main} 
                  radius={[8, 8, 0, 0]}
                  name="Electric Vehicle"
                />
                <Bar 
                  dataKey="ICE" 
                  fill={theme.palette.error.main} 
                  radius={[8, 8, 0, 0]}
                  name="ICE Vehicle"
                />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* Cost Breakdown Pie Chart */}
        <Grid item xs={12} lg={4}>
          <Paper elevation={3} sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom fontWeight="bold">
              EV Cost Distribution
            </Typography>
            <Typography variant="body2" color="text.secondary" paragraph>
              Total: ${totalCost.toLocaleString()}
            </Typography>
            <Divider sx={{ mb: 3 }} />
            <ResponsiveContainer width="100%" height={isMobile ? 250 : 350}>
              <PieChart>
                <Pie
                  data={costBreakdown}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                  outerRadius={isMobile ? 70 : 100}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {costBreakdown.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip 
                  formatter={(value: any) => `$${value.toLocaleString()}`}
                />
              </PieChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
      </Grid>

      {/* 5-Year Projection */}
      <Paper elevation={3} sx={{ p: 3, mb: 4 }}>
        <Typography variant="h6" gutterBottom fontWeight="bold">
          5-Year Cumulative Cost Projection
        </Typography>
        <Typography variant="body2" color="text.secondary" paragraph>
          Track total ownership costs over time to visualize when EV becomes more economical
        </Typography>
        <Divider sx={{ mb: 3 }} />
        <ResponsiveContainer width="100%" height={isMobile ? 250 : 350}>
          <AreaChart data={projectionData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
            <defs>
              <linearGradient id="colorEV" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor={theme.palette.success.main} stopOpacity={0.8}/>
                <stop offset="95%" stopColor={theme.palette.success.main} stopOpacity={0}/>
              </linearGradient>
              <linearGradient id="colorICE" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor={theme.palette.error.main} stopOpacity={0.8}/>
                <stop offset="95%" stopColor={theme.palette.error.main} stopOpacity={0}/>
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke={theme.palette.divider} />
            <XAxis dataKey="year" tick={{ fontSize: 12 }} />
            <YAxis 
              tick={{ fontSize: 12 }}
              tickFormatter={(value) => `$${(value / 1000).toFixed(0)}k`}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend wrapperStyle={{ paddingTop: '20px' }} />
            <Area 
              type="monotone" 
              dataKey="EV" 
              stroke={theme.palette.success.main} 
              fillOpacity={1} 
              fill="url(#colorEV)" 
              name="Electric Vehicle"
              strokeWidth={3}
            />
            <Area 
              type="monotone" 
              dataKey="ICE" 
              stroke={theme.palette.error.main} 
              fillOpacity={1} 
              fill="url(#colorICE)" 
              name="ICE Vehicle"
              strokeWidth={3}
            />
          </AreaChart>
        </ResponsiveContainer>
      </Paper>

      {/* Key Insights */}
      <Paper elevation={3} sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom fontWeight="bold">
          Key Insights & Recommendations
        </Typography>
        <Divider sx={{ mb: 2 }} />
        <Grid container spacing={2}>
          <Grid item xs={12} md={6}>
            <Card variant="outlined" sx={{ p: 2, borderColor: 'success.main', borderWidth: 2 }}>
              <Stack direction="row" spacing={2} alignItems="flex-start">
                <BuildIcon color="success" />
                <Box>
                  <Typography variant="subtitle2" fontWeight="bold" gutterBottom>
                    Lower Maintenance Costs
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    EVs save ${((8000 - maintenanceCosts)).toLocaleString()} over 5 years due to fewer moving parts and reduced wear
                  </Typography>
                </Box>
              </Stack>
            </Card>
          </Grid>
          <Grid item xs={12} md={6}>
            <Card variant="outlined" sx={{ p: 2, borderColor: 'info.main', borderWidth: 2 }}>
              <Stack direction="row" spacing={2} alignItems="flex-start">
                <LocalGasStationIcon color="info" />
                <Box>
                  <Typography variant="subtitle2" fontWeight="bold" gutterBottom>
                    Energy Efficiency
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Electricity costs are ${((15000 - energyCosts)).toLocaleString()} lower than fuel over the same period
                  </Typography>
                </Box>
              </Stack>
            </Card>
          </Grid>
        </Grid>
      </Paper>
    </Box>
  );
};

export default TCOAnalysis;

import React, { useEffect, useState } from 'react';
import {
  Container,
  Paper,
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  TextField,
  Button,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
} from '@mui/material';
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';

// Sample data for charts
const utilizationData = [
  { name: 'Mon', utilization: 65, distance: 240 },
  { name: 'Tue', utilization: 59, distance: 221 },
  { name: 'Wed', utilization: 80, distance: 229 },
  { name: 'Thu', utilization: 81, distance: 200 },
  { name: 'Fri', utilization: 56, distance: 200 },
  { name: 'Sat', utilization: 55, distance: 129 },
  { name: 'Sun', utilization: 40, distance: 100 },
];

const costData = [
  { name: 'Electricity', value: 3500 },
  { name: 'Maintenance', value: 1200 },
  { name: 'Insurance', value: 2000 },
  { name: 'Other', value: 800 },
];

const energyConsumptionData = [
  { month: 'Jan', consumption: 450, target: 500 },
  { month: 'Feb', consumption: 320, target: 500 },
  { month: 'Mar', consumption: 200, target: 500 },
  { month: 'Apr', consumption: 278, target: 500 },
  { month: 'May', consumption: 189, target: 500 },
  { month: 'Jun', consumption: 239, target: 500 },
];

const carbonData = [
  { month: 'Jan', emissions: 180, target: 200 },
  { month: 'Feb', emissions: 128, target: 200 },
  { month: 'Mar', emissions: 80, target: 200 },
  { month: 'Apr', emissions: 111, target: 200 },
  { month: 'May', emissions: 75, target: 200 },
  { month: 'Jun', emissions: 95, target: 200 },
];

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042'];

const DetailedAnalyticsDashboardPage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [timeRange, setTimeRange] = useState('7d');
  const [companyFilter, setCompanyFilter] = useState('');
  const [vehicleFilter, setVehicleFilter] = useState('');

  useEffect(() => {
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
    }, 500);
  }, [timeRange, companyFilter, vehicleFilter]);

  return (
    <Container maxWidth="lg" sx={{ py: 3 }}>
      <Typography variant="h4" sx={{ mb: 3, fontWeight: 'bold' }}>
        Analytics Dashboard
      </Typography>

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={4}>
            <FormControl fullWidth>
              <InputLabel>Time Range</InputLabel>
              <Select
                value={timeRange}
                label="Time Range"
                onChange={(e) => setTimeRange(e.target.value)}
              >
                <MenuItem value="7d">Last 7 Days</MenuItem>
                <MenuItem value="30d">Last 30 Days</MenuItem>
                <MenuItem value="90d">Last 90 Days</MenuItem>
                <MenuItem value="1y">Last Year</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField
              fullWidth
              label="Company"
              value={companyFilter}
              onChange={(e) => setCompanyFilter(e.target.value)}
              placeholder="Filter by company"
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField
              fullWidth
              label="Vehicle"
              value={vehicleFilter}
              onChange={(e) => setVehicleFilter(e.target.value)}
              placeholder="Filter by vehicle"
            />
          </Grid>
        </Grid>
      </Paper>

      {/* Summary KPIs */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Avg Fleet Utilization</Typography>
              <Typography variant="h4" sx={{ color: 'primary.main' }}>
                62.5%
              </Typography>
              <Typography variant="body2" color="success.main">
                ↑ 5% from last week
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Total Distance</Typography>
              <Typography variant="h4" sx={{ color: 'info.main' }}>
                1,319 km
              </Typography>
              <Typography variant="body2" color="success.main">
                ↑ 12% from last week
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Energy Efficiency</Typography>
              <Typography variant="h4" sx={{ color: 'success.main' }}>
                8.4 km/kWh
              </Typography>
              <Typography variant="body2" color="error.main">
                ↓ 2% from last week
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Total Cost</Typography>
              <Typography variant="h4" sx={{ color: 'warning.main' }}>
                $7,500
              </Typography>
              <Typography variant="body2" color="success.main">
                ↓ 3% from last week
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 5 }}>
          <CircularProgress />
        </Box>
      ) : (
        <>
          {/* Charts Row 1 */}
          <Grid container spacing={2} sx={{ mb: 3 }}>
            <Grid item xs={12} md={8}>
              <Card>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2 }}>
                    Fleet Utilization & Distance Trend
                  </Typography>
                  <ResponsiveContainer width="100%" height={300}>
                    <LineChart data={utilizationData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Line type="monotone" dataKey="utilization" stroke="#8884d8" name="Utilization (%)" />
                      <Line type="monotone" dataKey="distance" stroke="#82ca9d" name="Distance (km)" />
                    </LineChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={4}>
              <Card>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2 }}>
                    Cost Breakdown
                  </Typography>
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={costData}
                        cx="50%"
                        cy="50%"
                        labelLine={false}
                        label={({ name, value }) => `${name}: $${value}`}
                        outerRadius={80}
                        fill="#8884d8"
                        dataKey="value"
                      >
                        {costData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          {/* Charts Row 2 */}
          <Grid container spacing={2} sx={{ mb: 3 }}>
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2 }}>
                    Energy Consumption vs Target
                  </Typography>
                  <ResponsiveContainer width="100%" height={300}>
                    <BarChart data={energyConsumptionData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="month" />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Bar dataKey="consumption" fill="#82ca9d" name="Actual (kWh)" />
                      <Bar dataKey="target" fill="#ffc658" name="Target (kWh)" />
                    </BarChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2 }}>
                    Carbon Emissions vs Target
                  </Typography>
                  <ResponsiveContainer width="100%" height={300}>
                    <BarChart data={carbonData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="month" />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Bar dataKey="emissions" fill="#ff7c7c" name="Emissions (kg CO2)" />
                      <Bar dataKey="target" fill="#8884d8" name="Target (kg CO2)" />
                    </BarChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          {/* Detailed Metrics */}
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2 }}>
                    Fleet Performance Metrics
                  </Typography>
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2">Active Vehicles</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        24/30
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2">Avg Trip Duration</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        2h 15m
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2">Avg Trip Distance</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        45 km
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2">Avg Energy Per km</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        0.18 kWh
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2">Total Trips</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        156
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2">Avg Cost Per km</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        $0.42
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2 }}>
                    Driver Performance
                  </Typography>
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2">Total Drivers</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        18
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2">Avg Driver Rating</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 'bold', color: 'success.main' }}>
                        4.6/5.0
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2">Safe Driving Days</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        156/156 (100%)
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2">Avg Trips Per Driver</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        8.7
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2">Incidents Reported</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 'bold', color: 'success.main' }}>
                        0
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2">Speeding Violations</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 'bold', color: 'error.main' }}>
                        2
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </>
      )}
    </Container>
  );
};

export default DetailedAnalyticsDashboardPage;

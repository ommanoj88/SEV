import React from 'react';
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { useTheme } from '@mui/material/styles';
import { Box, Typography } from '@mui/material';
import { useAppSelector } from '@redux/hooks';

const UtilizationChart: React.FC = () => {
  const theme = useTheme();
  const { fleetAnalytics } = useAppSelector((state) => state.analytics);

  // Generate sample data if none exists
  const data = fleetAnalytics?.trends?.utilization && fleetAnalytics.trends.utilization.length > 0
    ? fleetAnalytics.trends.utilization
    : [
        { date: 'Mon', value: 65, active: 45 },
        { date: 'Tue', value: 72, active: 52 },
        { date: 'Wed', value: 68, active: 48 },
        { date: 'Thu', value: 75, active: 55 },
        { date: 'Fri', value: 82, active: 62 },
        { date: 'Sat', value: 58, active: 38 },
        { date: 'Sun', value: 45, active: 28 },
      ];

  if (!data || data.length === 0) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height={300}>
        <Typography variant="body2" color="text.secondary">
          No utilization data available
        </Typography>
      </Box>
    );
  }

  return (
    <ResponsiveContainer width="100%" height={300}>
      <AreaChart
        data={data}
        margin={{
          top: 10,
          right: 30,
          left: 0,
          bottom: 0,
        }}
      >
        <CartesianGrid strokeDasharray="3 3" stroke={theme.palette.divider} />
        <XAxis
          dataKey="date"
          stroke={theme.palette.text.secondary}
          style={{ fontSize: '0.875rem' }}
        />
        <YAxis
          stroke={theme.palette.text.secondary}
          style={{ fontSize: '0.875rem' }}
          label={{ value: 'Utilization %', angle: -90, position: 'insideLeft' }}
        />
        <Tooltip
          contentStyle={{
            backgroundColor: theme.palette.background.paper,
            border: `1px solid ${theme.palette.divider}`,
            borderRadius: theme.shape.borderRadius,
          }}
        />
        <Legend />
        <Area
          type="monotone"
          dataKey="value"
          name="Utilization"
          stroke={theme.palette.primary.main}
          fill={theme.palette.primary.light}
          fillOpacity={0.6}
        />
        <Area
          type="monotone"
          dataKey="active"
          name="Active Vehicles"
          stroke={theme.palette.success.main}
          fill={theme.palette.success.light}
          fillOpacity={0.4}
        />
      </AreaChart>
    </ResponsiveContainer>
  );
};

export default UtilizationChart;

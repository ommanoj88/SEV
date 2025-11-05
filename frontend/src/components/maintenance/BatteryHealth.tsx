import React from 'react';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Paper, Typography } from '@mui/material';
import { useTheme } from '@mui/material/styles';

const BatteryHealth: React.FC = () => {
  const theme = useTheme();
  // TODO: Add batteryHealth to MaintenanceState when backend provides this data
  const batteryHealth = { history: [] };

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h6" gutterBottom>Battery Health Trend</Typography>
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={batteryHealth?.history || []}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="date" />
          <YAxis label={{ value: 'Health %', angle: -90, position: 'insideLeft' }} />
          <Tooltip />
          <Legend />
          <Line type="monotone" dataKey="healthPercentage" name="Health %" stroke={theme.palette.primary.main} />
          <Line type="monotone" dataKey="cycleCount" name="Cycles" stroke={theme.palette.secondary.main} />
        </LineChart>
      </ResponsiveContainer>
    </Paper>
  );
};

export default BatteryHealth;

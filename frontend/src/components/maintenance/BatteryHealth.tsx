import React, { useEffect, useState } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Paper, Typography, CircularProgress, Alert, Box } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import batteryHealthService, { BatteryHealthData } from '../../services/batteryHealthService';

interface BatteryHealthProps {
  vehicleId?: number;
}

const BatteryHealth: React.FC<BatteryHealthProps> = ({ vehicleId }) => {
  const theme = useTheme();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [batteryHistory, setBatteryHistory] = useState<BatteryHealthData[]>([]);

  useEffect(() => {
    if (vehicleId) {
      loadBatteryHealth();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [vehicleId]);

  const loadBatteryHealth = async () => {
    if (!vehicleId) return;

    try {
      setLoading(true);
      setError(null);
      const history = await batteryHealthService.getBatteryHealthHistory(vehicleId);
      setBatteryHistory(history);
    } catch (err: any) {
      console.error('Failed to load battery health:', err);
      setError(err.message || 'Failed to load battery health data');
    } finally {
      setLoading(false);
    }
  };

  const chartData = batteryHistory.map(entry => ({
    date: new Date(entry.recordedAt).toLocaleDateString(),
    healthPercentage: entry.soh,
    cycleCount: entry.cycleCount,
    temperature: entry.temperature,
  }));

  if (!vehicleId) {
    return (
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>Battery Health Trend</Typography>
        <Alert severity="info">Please select a vehicle to view battery health data</Alert>
      </Paper>
    );
  }

  if (loading) {
    return (
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>Battery Health Trend</Typography>
        <Box display="flex" justifyContent="center" alignItems="center" minHeight={300}>
          <CircularProgress />
        </Box>
      </Paper>
    );
  }

  if (error) {
    return (
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>Battery Health Trend</Typography>
        <Alert severity="error">{error}</Alert>
      </Paper>
    );
  }

  if (chartData.length === 0) {
    return (
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>Battery Health Trend</Typography>
        <Alert severity="info">No battery health data available for this vehicle</Alert>
      </Paper>
    );
  }

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h6" gutterBottom>Battery Health Trend</Typography>
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={chartData}>
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

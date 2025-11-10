import React, { useEffect, useState } from 'react';
import {
  Card,
  CardContent,
  Typography,
  Box,
  CircularProgress,
  Alert,
} from '@mui/material';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';
import { ElectricCar, LocalGasStation, AllInclusive } from '@mui/icons-material';
import vehicleService from '@services/vehicleService';

interface FleetCompositionData {
  totalVehicles: number;
  evCount: number;
  iceCount: number;
  hybridCount: number;
  evPercentage: number;
  icePercentage: number;
  hybridPercentage: number;
}

interface FleetCompositionCardProps {
  companyId?: number;
}

const COLORS = {
  EV: '#4caf50', // Green for EV
  ICE: '#ff9800', // Orange for ICE
  HYBRID: '#2196f3', // Blue for HYBRID
};

const FleetCompositionCard: React.FC<FleetCompositionCardProps> = ({ companyId = 1 }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [composition, setComposition] = useState<FleetCompositionData | null>(null);

  useEffect(() => {
    loadFleetComposition();
  }, [companyId]);

  const loadFleetComposition = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await vehicleService.getFleetComposition(companyId);
      setComposition(data);
    } catch (err: any) {
      console.error('Failed to load fleet composition:', err);
      setError(err.message || 'Failed to load fleet composition');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Card sx={{ height: '100%' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Fleet Composition
          </Typography>
          <Box display="flex" justifyContent="center" alignItems="center" minHeight={300}>
            <CircularProgress />
          </Box>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card sx={{ height: '100%' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Fleet Composition
          </Typography>
          <Alert severity="error">{error}</Alert>
        </CardContent>
      </Card>
    );
  }

  if (!composition || composition.totalVehicles === 0) {
    return (
      <Card sx={{ height: '100%' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Fleet Composition
          </Typography>
          <Alert severity="info">No vehicles found</Alert>
        </CardContent>
      </Card>
    );
  }

  const chartData = [
    { name: 'EV', value: composition.evCount, percentage: composition.evPercentage },
    { name: 'ICE', value: composition.iceCount, percentage: composition.icePercentage },
    { name: 'HYBRID', value: composition.hybridCount, percentage: composition.hybridPercentage },
  ].filter(item => item.value > 0);

  const renderCustomLabel = (entry: any) => {
    return `${entry.percentage.toFixed(1)}%`;
  };

  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Fleet Composition
        </Typography>
        <Typography variant="body2" color="text.secondary" gutterBottom>
          Distribution by Fuel Type
        </Typography>

        <Box sx={{ mt: 2 }}>
          <ResponsiveContainer width="100%" height={250}>
            <PieChart>
              <Pie
                data={chartData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={renderCustomLabel}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {chartData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[entry.name as keyof typeof COLORS]} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </Box>

        <Box sx={{ mt: 3 }}>
          {/* EV Stats */}
          {composition.evCount > 0 && (
            <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
              <Box display="flex" alignItems="center">
                <ElectricCar sx={{ color: COLORS.EV, mr: 1 }} />
                <Typography variant="body2">Electric Vehicles</Typography>
              </Box>
              <Box textAlign="right">
                <Typography variant="h6">{composition.evCount}</Typography>
                <Typography variant="caption" color="text.secondary">
                  {composition.evPercentage.toFixed(1)}%
                </Typography>
              </Box>
            </Box>
          )}

          {/* ICE Stats */}
          {composition.iceCount > 0 && (
            <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
              <Box display="flex" alignItems="center">
                <LocalGasStation sx={{ color: COLORS.ICE, mr: 1 }} />
                <Typography variant="body2">ICE Vehicles</Typography>
              </Box>
              <Box textAlign="right">
                <Typography variant="h6">{composition.iceCount}</Typography>
                <Typography variant="caption" color="text.secondary">
                  {composition.icePercentage.toFixed(1)}%
                </Typography>
              </Box>
            </Box>
          )}

          {/* HYBRID Stats */}
          {composition.hybridCount > 0 && (
            <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
              <Box display="flex" alignItems="center">
                <AllInclusive sx={{ color: COLORS.HYBRID, mr: 1 }} />
                <Typography variant="body2">Hybrid Vehicles</Typography>
              </Box>
              <Box textAlign="right">
                <Typography variant="h6">{composition.hybridCount}</Typography>
                <Typography variant="caption" color="text.secondary">
                  {composition.hybridPercentage.toFixed(1)}%
                </Typography>
              </Box>
            </Box>
          )}

          {/* Total */}
          <Box 
            display="flex" 
            alignItems="center" 
            justifyContent="space-between" 
            mt={2}
            pt={2}
            sx={{ borderTop: 1, borderColor: 'divider' }}
          >
            <Typography variant="body2" fontWeight="bold">
              Total Fleet
            </Typography>
            <Typography variant="h6">{composition.totalVehicles}</Typography>
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};

export default FleetCompositionCard;

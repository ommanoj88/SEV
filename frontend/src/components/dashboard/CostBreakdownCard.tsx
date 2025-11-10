import React, { useEffect, useState } from 'react';
import {
  Card,
  CardContent,
  Typography,
  Box,
  CircularProgress,
  Alert,
  Divider,
} from '@mui/material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { ElectricBolt, LocalGasStation, TrendingUp } from '@mui/icons-material';
import tripService from '@services/tripService';

interface CostSummary {
  totalCost: number;
  totalEVCost: number;
  totalICECost: number;
  totalHybridCost: number;
  totalDistance: number;
  avgCostPerKm: number;
  evTripCount: number;
  iceTripCount: number;
  hybridTripCount: number;
  totalTrips: number;
}

interface CostBreakdownCardProps {
  companyId?: number;
}

const CostBreakdownCard: React.FC<CostBreakdownCardProps> = ({ companyId = 1 }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [costSummary, setCostSummary] = useState<CostSummary | null>(null);

  useEffect(() => {
    loadCostBreakdown();
  }, [companyId]);

  const loadCostBreakdown = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Get last 30 days of data
      const endTime = new Date().toISOString();
      const startTime = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString();
      
      const data = await tripService.getCompanyCostSummary(companyId, {
        startTime,
        endTime,
      });
      
      setCostSummary(data);
    } catch (err: any) {
      console.error('Failed to load cost breakdown:', err);
      setError(err.message || 'Failed to load cost breakdown');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Card sx={{ height: '100%' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Cost Breakdown
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
            Cost Breakdown
          </Typography>
          <Alert severity="error">{error}</Alert>
        </CardContent>
      </Card>
    );
  }

  if (!costSummary || costSummary.totalTrips === 0) {
    return (
      <Card sx={{ height: '100%' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Cost Breakdown
          </Typography>
          <Alert severity="info">No trip data available for the last 30 days</Alert>
        </CardContent>
      </Card>
    );
  }

  const chartData = [
    {
      name: 'Energy Cost (EV)',
      cost: costSummary.totalEVCost,
      trips: costSummary.evTripCount,
    },
    {
      name: 'Fuel Cost (ICE)',
      cost: costSummary.totalICECost,
      trips: costSummary.iceTripCount,
    },
    {
      name: 'Hybrid Cost',
      cost: costSummary.totalHybridCost,
      trips: costSummary.hybridTripCount,
    },
  ].filter(item => item.cost > 0);

  const formatCurrency = (value: number) => {
    return `₹${value.toLocaleString('en-IN', { maximumFractionDigits: 0 })}`;
  };

  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Cost Breakdown
        </Typography>
        <Typography variant="body2" color="text.secondary" gutterBottom>
          Last 30 Days
        </Typography>

        {/* Summary Stats */}
        <Box sx={{ mt: 2, mb: 3 }}>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
            <Typography variant="body2" color="text.secondary">
              Total Operating Cost
            </Typography>
            <Typography variant="h5" color="primary">
              {formatCurrency(costSummary.totalCost)}
            </Typography>
          </Box>

          <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
            <Typography variant="body2" color="text.secondary">
              Total Distance
            </Typography>
            <Typography variant="body1">
              {costSummary.totalDistance.toLocaleString('en-IN', { maximumFractionDigits: 1 })} km
            </Typography>
          </Box>

          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Typography variant="body2" color="text.secondary">
              Avg Cost per Km
            </Typography>
            <Typography variant="body1">
              {formatCurrency(costSummary.avgCostPerKm)}
            </Typography>
          </Box>
        </Box>

        <Divider sx={{ my: 2 }} />

        {/* Chart */}
        {chartData.length > 0 && (
          <Box sx={{ mt: 2, mb: 2 }}>
            <ResponsiveContainer width="100%" height={200}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                <YAxis tick={{ fontSize: 12 }} />
                <Tooltip formatter={(value) => formatCurrency(Number(value))} />
                <Bar dataKey="cost" fill="#1976d2" />
              </BarChart>
            </ResponsiveContainer>
          </Box>
        )}

        {/* Detailed Breakdown */}
        <Box sx={{ mt: 3 }}>
          {costSummary.totalEVCost > 0 && (
            <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
              <Box display="flex" alignItems="center">
                <ElectricBolt sx={{ color: '#4caf50', mr: 1, fontSize: 20 }} />
                <Box>
                  <Typography variant="body2">Energy Cost (EV)</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {costSummary.evTripCount} trips
                  </Typography>
                </Box>
              </Box>
              <Box textAlign="right">
                <Typography variant="h6" color="success.main">
                  {formatCurrency(costSummary.totalEVCost)}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {((costSummary.totalEVCost / costSummary.totalCost) * 100).toFixed(1)}%
                </Typography>
              </Box>
            </Box>
          )}

          {costSummary.totalICECost > 0 && (
            <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
              <Box display="flex" alignItems="center">
                <LocalGasStation sx={{ color: '#ff9800', mr: 1, fontSize: 20 }} />
                <Box>
                  <Typography variant="body2">Fuel Cost (ICE)</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {costSummary.iceTripCount} trips
                  </Typography>
                </Box>
              </Box>
              <Box textAlign="right">
                <Typography variant="h6" color="warning.main">
                  {formatCurrency(costSummary.totalICECost)}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {((costSummary.totalICECost / costSummary.totalCost) * 100).toFixed(1)}%
                </Typography>
              </Box>
            </Box>
          )}

          {costSummary.totalHybridCost > 0 && (
            <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
              <Box display="flex" alignItems="center">
                <TrendingUp sx={{ color: '#2196f3', mr: 1, fontSize: 20 }} />
                <Box>
                  <Typography variant="body2">Hybrid Cost</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {costSummary.hybridTripCount} trips
                  </Typography>
                </Box>
              </Box>
              <Box textAlign="right">
                <Typography variant="h6" color="info.main">
                  {formatCurrency(costSummary.totalHybridCost)}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {((costSummary.totalHybridCost / costSummary.totalCost) * 100).toFixed(1)}%
                </Typography>
              </Box>
            </Box>
          )}
        </Box>
      </CardContent>
    </Card>
  );
};

export default CostBreakdownCard;

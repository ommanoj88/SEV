import React from 'react';
import { Paper, Typography, Grid, Box } from '@mui/material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { useTheme } from '@mui/material/styles';
import { useAppSelector } from '../../redux/hooks';

const TCOAnalysis: React.FC = () => {
  const theme = useTheme();
  const { tcoAnalysis } = useAppSelector((state) => state.analytics);

  // Use actual structure from TCOAnalysis type
  const data = [
    { name: 'Acquisition', EV: tcoAnalysis?.purchasePrice || 45000, ICE: 35000 },
    { name: 'Energy/Fuel', EV: tcoAnalysis?.energyCosts || 5000, ICE: 15000 },
    { name: 'Maintenance', EV: tcoAnalysis?.maintenanceCosts || 2000, ICE: 8000 },
    { name: 'Insurance', EV: tcoAnalysis?.insuranceCosts || 2500, ICE: 2000 },
  ];

  const totalSavings = tcoAnalysis?.comparisonWithICE?.totalSavings || 0;
  const roi = tcoAnalysis && tcoAnalysis.totalCost > 0
    ? ((totalSavings / tcoAnalysis.totalCost) * 100)
    : 0;
  const paybackPeriod = tcoAnalysis?.comparisonWithICE?.paybackPeriod || 36;

  return (
    <Box>
      <Grid container spacing={3} mb={3}>
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3 }}><Typography variant="h5" color="success.main">${totalSavings.toLocaleString('en-US', { maximumFractionDigits: 0 })}</Typography><Typography color="text.secondary">Total Savings</Typography></Paper>
        </Grid>
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3 }}><Typography variant="h5">{roi.toFixed(1)}%</Typography><Typography color="text.secondary">ROI</Typography></Paper>
        </Grid>
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3 }}><Typography variant="h5">{paybackPeriod} months</Typography><Typography color="text.secondary">Payback Period</Typography></Paper>
        </Grid>
      </Grid>
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>Total Cost of Ownership Comparison</Typography>
        <ResponsiveContainer width="100%" height={400}>
          <BarChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Bar dataKey="EV" fill={theme.palette.success.main} />
            <Bar dataKey="ICE" fill={theme.palette.error.main} />
          </BarChart>
        </ResponsiveContainer>
      </Paper>
    </Box>
  );
};

export default TCOAnalysis;

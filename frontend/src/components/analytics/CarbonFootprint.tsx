import React from 'react';
import { Paper, Typography, Grid, Box } from '@mui/material';
import { useAppSelector } from '../../redux/hooks';
import { Park, DirectionsCar, Home } from '@mui/icons-material';

const CarbonFootprint: React.FC = () => {
  const { carbonFootprint } = useAppSelector((state) => state.analytics);

  // Calculate aggregates from array
  const totalCO2Avoided = Array.isArray(carbonFootprint)
    ? carbonFootprint.reduce((sum, item) => sum + (item.co2Avoided || 0), 0)
    : 0;

  const totalTrees = Array.isArray(carbonFootprint)
    ? carbonFootprint.reduce((sum, item) => sum + (item.equivalentTrees || 0), 0)
    : 0;

  return (
    <Box>
      <Grid container spacing={3} mb={3}>
        <Grid item xs={12} md={3}>
          <Paper sx={{ p: 3, textAlign: 'center' }}><Park sx={{ fontSize: 40, color: 'success.main', mb: 1 }} /><Typography variant="h6">{totalTrees || 0}</Typography><Typography variant="caption" color="text.secondary">Trees Equivalent</Typography></Paper>
        </Grid>
        <Grid item xs={12} md={3}>
          <Paper sx={{ p: 3, textAlign: 'center' }}><DirectionsCar sx={{ fontSize: 40, color: 'primary.main', mb: 1 }} /><Typography variant="h6">{(totalCO2Avoided * 1.5).toLocaleString('en-US', { maximumFractionDigits: 0 })}</Typography><Typography variant="caption" color="text.secondary">Miles Equivalent</Typography></Paper>
        </Grid>
        <Grid item xs={12} md={3}>
          <Paper sx={{ p: 3, textAlign: 'center' }}><Home sx={{ fontSize: 40, color: 'warning.main', mb: 1 }} /><Typography variant="h6">{(totalCO2Avoided / 5).toFixed(1)}</Typography><Typography variant="caption" color="text.secondary">Days Powered</Typography></Paper>
        </Grid>
        <Grid item xs={12} md={3}>
          <Paper sx={{ p: 3, textAlign: 'center' }}><Typography variant="h4" color="success.main">15.2%</Typography><Typography variant="caption" color="text.secondary">CO2 Reduction</Typography></Paper>
        </Grid>
      </Grid>
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>Total Emissions Avoided</Typography>
        <Typography variant="h4" color="success.main">{totalCO2Avoided.toLocaleString('en-US', { maximumFractionDigits: 2 })} kg CO2</Typography>
      </Paper>
    </Box>
  );
};

export default CarbonFootprint;

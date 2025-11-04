import React from 'react';
import { Paper, Typography, Grid, Box, Chip, Button, Divider } from '@mui/material';
import { CheckCircle } from '@mui/icons-material';
import { useAppSelector } from '../../redux/hooks';
import { formatDate } from '../../utils/formatters';

const Subscriptions: React.FC = () => {
  const billing = useAppSelector((state) => state.billing);
  const plans = billing.pricingPlans;
  const currentPlan = plans.length > 0 ? plans[0] : null;

  return (
    <Paper sx={{ p: 3 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h6">Current Subscription</Typography>
        <Chip label="Active" color="success" />
      </Box>
      <Divider sx={{ mb: 3 }} />
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Typography variant="body2" color="text.secondary">Plan</Typography>
          <Typography variant="h5" gutterBottom>{currentPlan?.name || 'Professional'}</Typography>
          <Typography variant="h4" color="primary.main">${currentPlan?.monthlyPrice || 199}<Typography component="span" variant="body1" color="text.secondary">/month</Typography></Typography>
        </Grid>
        <Grid item xs={12} md={6}>
          <Typography variant="body2" color="text.secondary">Billing Cycle</Typography>
          <Typography variant="body1" gutterBottom>Monthly</Typography>
          <Typography variant="body2" color="text.secondary">Next Billing Date</Typography>
          <Typography variant="body1">{formatDate(new Date(Date.now() + 30 * 24 * 60 * 60 * 1000))}</Typography>
        </Grid>
        <Grid item xs={12}>
          <Typography variant="h6" gutterBottom>Plan Features</Typography>
          {['Up to 50 vehicles', 'Real-time tracking', 'Advanced analytics', 'Priority support', 'API access'].map((feature, i) => (
            <Box key={i} display="flex" alignItems="center" gap={1} mb={1}>
              <CheckCircle color="success" fontSize="small" />
              <Typography variant="body2">{feature}</Typography>
            </Box>
          ))}
        </Grid>
        <Grid item xs={12}>
          <Box display="flex" gap={2}>
            <Button variant="contained">Upgrade Plan</Button>
            <Button variant="outlined" color="error">Cancel Subscription</Button>
          </Box>
        </Grid>
      </Grid>
    </Paper>
  );
};

export default Subscriptions;

import React from 'react';
import { Grid, Card, CardContent, Typography, Button, Box, List, ListItem, ListItemIcon, ListItemText } from '@mui/material';
import { CheckCircle } from '@mui/icons-material';

const PricingPlans: React.FC = () => {
  const plans = [
    { name: 'Starter', price: 49, vehicles: 10, features: ['Real-time tracking', 'Basic analytics', 'Email support'] },
    { name: 'Professional', price: 199, vehicles: 50, features: ['Everything in Starter', 'Advanced analytics', 'Priority support', 'API access'], popular: true },
    { name: 'Enterprise', price: 499, vehicles: 'Unlimited', features: ['Everything in Professional', 'Custom integrations', 'Dedicated support', 'SLA guarantee'] },
  ];

  return (
    <Grid container spacing={3}>
      {plans.map((plan) => (
        <Grid item xs={12} md={4} key={plan.name}>
          <Card sx={{ height: '100%', border: plan.popular ? 2 : 0, borderColor: 'primary.main', position: 'relative' }}>
            {plan.popular && <Box sx={{ position: 'absolute', top: 0, right: 0, bgcolor: 'primary.main', color: 'white', px: 2, py: 0.5, borderBottomLeftRadius: 8 }}><Typography variant="caption">POPULAR</Typography></Box>}
            <CardContent>
              <Typography variant="h6" gutterBottom>{plan.name}</Typography>
              <Typography variant="h3" color="primary.main" gutterBottom>${plan.price}<Typography component="span" variant="body1" color="text.secondary">/mo</Typography></Typography>
              <Typography variant="body2" color="text.secondary" gutterBottom>Up to {plan.vehicles} vehicles</Typography>
              <List dense>
                {plan.features.map((feature, i) => (
                  <ListItem key={i} disablePadding sx={{ mb: 1 }}>
                    <ListItemIcon sx={{ minWidth: 32 }}><CheckCircle color="success" fontSize="small" /></ListItemIcon>
                    <ListItemText primary={<Typography variant="body2">{feature}</Typography>} />
                  </ListItem>
                ))}
              </List>
              <Button variant={plan.popular ? 'contained' : 'outlined'} fullWidth sx={{ mt: 2 }}>Choose Plan</Button>
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
};

export default PricingPlans;

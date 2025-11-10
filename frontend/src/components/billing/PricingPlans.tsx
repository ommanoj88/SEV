import React, { useState, useEffect, useCallback } from 'react';
import { 
  Grid, Card, CardContent, Typography, Button, Box, List, ListItem, 
  ListItemIcon, ListItemText, CircularProgress, Alert, Chip,
  FormControl, InputLabel, Select, MenuItem, TextField
} from '@mui/material';
import { CheckCircle, Star } from '@mui/icons-material';
import { PRICING_TIERS, BillingCycle, BILLING_CYCLE_LABELS, formatPrice } from '../../constants/pricingTiers';
import billingService from '../../services/billingService';

const PricingPlans: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [tiers, setTiers] = useState<any[]>([]);
  const [selectedBillingCycle, setSelectedBillingCycle] = useState<BillingCycle>(BillingCycle.MONTHLY);
  const [vehicleCount, setVehicleCount] = useState<number>(10);
  const [calculations, setCalculations] = useState<Record<string, any>>({});

  const fetchPricingTiers = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await billingService.getPricingTiers();
      setTiers(data);
    } catch (err: any) {
      console.error('Failed to fetch pricing tiers:', err);
      // Fallback to static data
      const staticTiers = Object.values(PRICING_TIERS);
      setTiers(staticTiers);
    } finally {
      setLoading(false);
    }
  };

  const calculateAllPrices = useCallback(async () => {
    const newCalculations: Record<string, any> = {};
    
    for (const tier of tiers) {
      try {
        const calculation = await billingService.calculatePricing({
          tier: tier.tierName,
          vehicleCount: vehicleCount,
          billingCycle: selectedBillingCycle
        });
        newCalculations[tier.tierName] = calculation;
      } catch (err) {
        console.error(`Failed to calculate pricing for ${tier.tierName}:`, err);
        // Fallback calculation
        const basePrice = tier.pricePerVehiclePerMonth;
        const monthlyCost = basePrice * vehicleCount;
        const months = selectedBillingCycle === BillingCycle.ANNUAL ? 12 : 
                      selectedBillingCycle === BillingCycle.QUARTERLY ? 3 : 1;
        const discount = selectedBillingCycle === BillingCycle.ANNUAL ? 0.1 : 
                        selectedBillingCycle === BillingCycle.QUARTERLY ? 0.05 : 0;
        const totalBeforeDiscount = monthlyCost * months;
        const discountAmount = totalBeforeDiscount * discount;
        const totalCost = totalBeforeDiscount - discountAmount;
        
        newCalculations[tier.tierName] = {
          tier: tier.tierName,
          vehicleCount,
          billingCycle: selectedBillingCycle,
          basePrice,
          monthlyCost,
          totalCost,
          discountPercentage: discount * 100,
          discountAmount
        };
      }
    }
    
    setCalculations(newCalculations);
  }, [tiers, vehicleCount, selectedBillingCycle]);

  useEffect(() => {
    fetchPricingTiers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (tiers.length > 0) {
      calculateAllPrices();
    }
  }, [tiers, calculateAllPrices]);

  const handleChoosePlan = (tierName: string) => {
    console.log(`Selected plan: ${tierName}`);
    // TODO: Implement subscription update logic
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      {error && (
        <Alert severity="warning" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Pricing Calculator */}
      <Card sx={{ mb: 4, p: 2 }}>
        <Typography variant="h6" gutterBottom>Calculate Your Costs</Typography>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              type="number"
              label="Number of Vehicles"
              value={vehicleCount}
              onChange={(e) => setVehicleCount(Math.max(1, parseInt(e.target.value) || 1))}
              inputProps={{ min: 1 }}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth>
              <InputLabel>Billing Cycle</InputLabel>
              <Select
                value={selectedBillingCycle}
                onChange={(e) => setSelectedBillingCycle(e.target.value as BillingCycle)}
                label="Billing Cycle"
              >
                {Object.entries(BILLING_CYCLE_LABELS).map(([value, label]) => (
                  <MenuItem key={value} value={value}>{label}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Card>

      {/* Pricing Tiers */}
      <Grid container spacing={3}>
        {tiers.map((tier) => {
          const calculation = calculations[tier.tierName];
          const isRecommended = tier.recommended;
          
          return (
            <Grid item xs={12} md={4} key={tier.tierName}>
              <Card 
                sx={{ 
                  height: '100%', 
                  border: isRecommended ? 2 : 0, 
                  borderColor: 'primary.main', 
                  position: 'relative',
                  transition: 'transform 0.2s, box-shadow 0.2s',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 4
                  }
                }}
              >
                {isRecommended && (
                  <Box 
                    sx={{ 
                      position: 'absolute', 
                      top: 0, 
                      right: 0, 
                      bgcolor: 'primary.main', 
                      color: 'white', 
                      px: 2, 
                      py: 0.5, 
                      borderBottomLeftRadius: 8,
                      display: 'flex',
                      alignItems: 'center',
                      gap: 0.5
                    }}
                  >
                    <Star fontSize="small" />
                    <Typography variant="caption" fontWeight="bold">RECOMMENDED</Typography>
                  </Box>
                )}
                
                <CardContent>
                  <Typography variant="h5" gutterBottom fontWeight="bold">
                    {tier.displayName}
                  </Typography>
                  
                  <Box sx={{ my: 2 }}>
                    <Typography variant="h3" color="primary.main" fontWeight="bold">
                      {formatPrice(tier.pricePerVehiclePerMonth)}
                      <Typography component="span" variant="body1" color="text.secondary">
                        /vehicle/mo
                      </Typography>
                    </Typography>
                    
                    {calculation && (
                      <Box sx={{ mt: 2, p: 1.5, bgcolor: 'grey.50', borderRadius: 1 }}>
                        <Typography variant="body2" color="text.secondary">
                          Total for {vehicleCount} vehicle(s):
                        </Typography>
                        <Typography variant="h6" color="primary.main" fontWeight="bold">
                          {formatPrice(calculation.totalCost)}
                          <Typography component="span" variant="caption" color="text.secondary">
                            {' '}/{selectedBillingCycle.toLowerCase()}
                          </Typography>
                        </Typography>
                        {calculation.discountPercentage > 0 && (
                          <Chip 
                            label={`Save ${formatPrice(calculation.discountAmount)}`}
                            size="small" 
                            color="success" 
                            sx={{ mt: 1 }}
                          />
                        )}
                      </Box>
                    )}
                  </Box>
                  
                  <Typography variant="body2" color="text.secondary" gutterBottom sx={{ mb: 2 }}>
                    {tier.description}
                  </Typography>
                  
                  <List dense>
                    {tier.features.map((feature: string, i: number) => (
                      <ListItem key={i} disablePadding sx={{ mb: 1 }}>
                        <ListItemIcon sx={{ minWidth: 32 }}>
                          <CheckCircle color="success" fontSize="small" />
                        </ListItemIcon>
                        <ListItemText 
                          primary={<Typography variant="body2">{feature}</Typography>} 
                        />
                      </ListItem>
                    ))}
                  </List>
                  
                  <Button 
                    variant={isRecommended ? 'contained' : 'outlined'} 
                    fullWidth 
                    size="large"
                    sx={{ mt: 3 }}
                    onClick={() => handleChoosePlan(tier.tierName)}
                  >
                    Choose {tier.displayName}
                  </Button>
                </CardContent>
              </Card>
            </Grid>
          );
        })}
      </Grid>

      <Box sx={{ mt: 4, p: 2, bgcolor: 'info.light', borderRadius: 1 }}>
        <Typography variant="body2" color="text.secondary" align="center">
          All prices are in Indian Rupees (INR). Prices exclude applicable taxes.
          Annual and quarterly plans offer significant savings.
        </Typography>
      </Box>
    </Box>
  );
};

export default PricingPlans;

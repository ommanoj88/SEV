import React, { useState } from 'react';
import { Container, Typography, Tabs, Tab } from '@mui/material';
import Subscriptions from '../components/billing/Subscriptions';
import Invoices from '../components/billing/Invoices';
import Payments from '../components/billing/Payments';
import PricingPlans from '../components/billing/PricingPlans';

const BillingPage: React.FC = () => {
  const [tab, setTab] = useState(0);

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>Billing & Subscription</Typography>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label="Subscription" />
        <Tab label="Invoices" />
        <Tab label="Payment History" />
        <Tab label="Pricing Plans" />
      </Tabs>
      {tab === 0 && <Subscriptions />}
      {tab === 1 && <Invoices />}
      {tab === 2 && <Payments />}
      {tab === 3 && <PricingPlans />}
    </Container>
  );
};

export default BillingPage;

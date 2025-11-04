import React, { useState } from 'react';
import { Container, Typography, Tabs, Tab } from '@mui/material';
import FleetAnalytics from '../components/analytics/FleetAnalytics';
import TCOAnalysis from '../components/analytics/TCOAnalysis';
import CarbonFootprint from '../components/analytics/CarbonFootprint';
import UtilizationReport from '../components/analytics/UtilizationReport';

const AnalyticsPage: React.FC = () => {
  const [tab, setTab] = useState(0);

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>Analytics</Typography>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label="Fleet Analytics" />
        <Tab label="TCO Analysis" />
        <Tab label="Carbon Footprint" />
        <Tab label="Utilization Report" />
      </Tabs>
      {tab === 0 && <FleetAnalytics />}
      {tab === 1 && <TCOAnalysis />}
      {tab === 2 && <CarbonFootprint />}
      {tab === 3 && <UtilizationReport />}
    </Container>
  );
};

export default AnalyticsPage;

import React, { useState } from 'react';
import { Container, Typography, Tabs, Tab } from '@mui/material';
import MaintenanceSchedule from '../components/maintenance/MaintenanceSchedule';
import ServiceHistory from '../components/maintenance/ServiceHistory';
import BatteryHealth from '../components/maintenance/BatteryHealth';
import ScheduleMaintenance from '../components/maintenance/ScheduleMaintenance';

const MaintenancePage: React.FC = () => {
  const [tab, setTab] = useState(0);

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>Maintenance</Typography>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label="Schedule" />
        <Tab label="Service History" />
        <Tab label="Battery Health" />
        <Tab label="Schedule New" />
      </Tabs>
      {tab === 0 && <MaintenanceSchedule />}
      {tab === 1 && <ServiceHistory />}
      {tab === 2 && <BatteryHealth />}
      {tab === 3 && <ScheduleMaintenance />}
    </Container>
  );
};

export default MaintenancePage;

import React, { useState } from 'react';
import { Container, Typography, Tabs, Tab } from '@mui/material';
import DriverList from '../components/drivers/DriverList';
import DriverLeaderboard from '../components/drivers/DriverLeaderboard';
import AssignDriver from '../components/drivers/AssignDriver';

const DriversPage: React.FC = () => {
  const [tab, setTab] = useState(0);

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>Drivers</Typography>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label="All Drivers" />
        <Tab label="Leaderboard" />
        <Tab label="Assign Driver" />
      </Tabs>
      {tab === 0 && <DriverList />}
      {tab === 1 && <DriverLeaderboard />}
      {tab === 2 && <AssignDriver />}
    </Container>
  );
};

export default DriversPage;

import React from 'react';
import { Container, Box } from '@mui/material';
import StationDiscovery from '../components/charging/StationDiscovery';

/**
 * Station Discovery Page
 * Allows users to find and navigate to charging or fuel stations based on vehicle type
 */
const StationDiscoveryPage: React.FC = () => {
  return (
    <Container maxWidth="xl" sx={{ py: 3 }}>
      <Box sx={{ height: 'calc(100vh - 120px)' }}>
        <StationDiscovery />
      </Box>
    </Container>
  );
};

export default StationDiscoveryPage;

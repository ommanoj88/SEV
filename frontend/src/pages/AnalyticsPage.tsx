import React, { useState } from 'react';
import { 
  Box, 
  Typography, 
  Tabs, 
  Tab, 
  Card,
} from '@mui/material';
import {
  Analytics as AnalyticsIcon,
  AccountBalance as TCOIcon,
  Nature as CarbonIcon,
  Speed as UtilizationIcon,
} from '@mui/icons-material';
import FleetAnalytics from '../components/analytics/FleetAnalytics';
import TCOAnalysis from '../components/analytics/TCOAnalysis';
import CarbonFootprint from '../components/analytics/CarbonFootprint';
import UtilizationReport from '../components/analytics/UtilizationReport';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
  return (
    <Box
      role="tabpanel"
      hidden={value !== index}
      id={`analytics-tabpanel-${index}`}
      aria-labelledby={`analytics-tab-${index}`}
      sx={{ pt: 3 }}
    >
      {value === index && <Box className="fade-in">{children}</Box>}
    </Box>
  );
};

const AnalyticsPage: React.FC = () => {
  const [tab, setTab] = useState(0);

  const tabs = [
    { label: 'Fleet Analytics', icon: <AnalyticsIcon sx={{ fontSize: 20 }} /> },
    { label: 'TCO Analysis', icon: <TCOIcon sx={{ fontSize: 20 }} /> },
    { label: 'Carbon Footprint', icon: <CarbonIcon sx={{ fontSize: 20 }} /> },
    { label: 'Utilization', icon: <UtilizationIcon sx={{ fontSize: 20 }} /> },
  ];

  return (
    <Box className="fade-in">
      {/* Page Header */}
      <Box mb={4}>
        <Typography variant="h4" fontWeight={700} color="text.primary" gutterBottom>
          Analytics & Insights
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Comprehensive analytics and reporting for your multi-fuel fleet operations
        </Typography>
      </Box>

      {/* Tabs */}
      <Card 
        sx={{ 
          mb: 3,
          overflow: 'visible',
        }}
      >
        <Tabs 
          value={tab} 
          onChange={(_, v) => setTab(v)}
          variant="scrollable"
          scrollButtons="auto"
          sx={{
            px: 1,
            '& .MuiTabs-indicator': {
              height: 3,
              borderRadius: '3px 3px 0 0',
            },
          }}
        >
          {tabs.map((t, index) => (
            <Tab
              key={index}
              label={
                <Box display="flex" alignItems="center" gap={1}>
                  {t.icon}
                  <span>{t.label}</span>
                </Box>
              }
              id={`analytics-tab-${index}`}
              aria-controls={`analytics-tabpanel-${index}`}
              sx={{
                py: 2,
                px: 3,
                minHeight: 56,
                '&.Mui-selected': {
                  color: 'primary.main',
                },
              }}
            />
          ))}
        </Tabs>
      </Card>

      {/* Tab Panels */}
      <TabPanel value={tab} index={0}>
        <FleetAnalytics />
      </TabPanel>
      <TabPanel value={tab} index={1}>
        <TCOAnalysis />
      </TabPanel>
      <TabPanel value={tab} index={2}>
        <CarbonFootprint />
      </TabPanel>
      <TabPanel value={tab} index={3}>
        <UtilizationReport />
      </TabPanel>
    </Box>
  );
};

export default AnalyticsPage;

import React, { useMemo, useState } from 'react';
import {
  Box,
  Typography,
  Tabs,
  Tab,
  Paper,
  AppBar,
  Toolbar,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  AccountTree as ArchitectureIcon,
  Storage as DatabaseIcon,
  Timeline as EventIcon,
  Web as FrontendIcon,
  Info as InfoIcon,
  GitHub as GitHubIcon,
} from '@mui/icons-material';
import {
  ArchitectureFlow,
  DatabaseSchema,
  EventFlow,
  FrontendFlow,
} from '../components/developer';
import {
  documentationSummary,
  techStack,
} from '../components/developer/data';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => (
  <div
    role="tabpanel"
    hidden={value !== index}
    style={{
      height: '100%',
      display: value === index ? 'flex' : 'none',
      flexDirection: 'column',
      overflow: 'hidden',
      width: '100%',
    }}
  >
    {value === index && children}
  </div>
);

const DeveloperDocsPage: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);

  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const tabs = [
    { label: 'System Architecture', icon: <ArchitectureIcon />, component: <ArchitectureFlow /> },
    { label: 'Database Schema', icon: <DatabaseIcon />, component: <DatabaseSchema /> },
    { label: 'Event Flow', icon: <EventIcon />, component: <EventFlow /> },
    { label: 'Frontend Structure', icon: <FrontendIcon />, component: <FrontendFlow /> },
  ];

  const stats = useMemo(
    () => [
      { label: 'Frontend Pages', value: documentationSummary.frontend.totalPages, detail: `${documentationSummary.frontend.categories.length} categories` },
      { label: 'API Endpoints', value: documentationSummary.api.totalEndpoints, detail: `${documentationSummary.api.totalControllers} controllers` },
      { label: 'DB Tables', value: documentationSummary.database.totalTables, detail: `${documentationSummary.database.totalEnums} enums` },
      { label: 'Backend Modules', value: documentationSummary.backend.totalModules, detail: techStack.architecture.name },
    ],
    []
  );

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100vh', width: '100%', bgcolor: 'grey.50' }}>
      {/* Header */}
      <AppBar position="static" elevation={0} sx={{ bgcolor: 'primary.dark' }}>
        <Toolbar>
          <Typography variant="h5" fontWeight="bold" sx={{ flexGrow: 1 }}>
            🚗⚡ EV Fleet Management - Developer Docs
          </Typography>
          <Chip
            label={`${techStack.framework.name} ${techStack.framework.version}`}
            size="small"
            sx={{ mr: 2, bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}
          />
          <Tooltip title="View on GitHub">
            <IconButton
              color="inherit"
              href="https://github.com/ommanoj88/SEV"
              target="_blank"
              rel="noreferrer"
            >
              <GitHubIcon />
            </IconButton>
          </Tooltip>
          <Tooltip title="Info">
            <IconButton color="inherit">
              <InfoIcon />
            </IconButton>
          </Tooltip>
        </Toolbar>
      </AppBar>

      {/* Info Banner */}
      <Paper
        elevation={0}
        sx={{
          p: 2,
          bgcolor: 'info.light',
          borderRadius: 0,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 2,
        }}
      >
        <InfoIcon color="info" />
        <Typography variant="body2" color="info.dark">
          <strong>Interactive Documentation:</strong> Click on any node to view detailed information
          including endpoints, database tables, and technologies. Use scroll to zoom, drag to pan.
        </Typography>
      </Paper>

      {/* Stats */}
      <Box
        sx={{
          px: 2,
          py: 2,
          display: 'grid',
          gridTemplateColumns: { xs: 'repeat(2, 1fr)', md: 'repeat(4, 1fr)' },
          gap: 1.5,
          bgcolor: 'grey.50',
        }}
      >
        {stats.map((item) => (
          <Paper
            key={item.label}
            elevation={0}
            sx={{
              p: 2,
              border: '1px solid',
              borderColor: 'divider',
              bgcolor: 'white',
              display: 'flex',
              flexDirection: 'column',
              gap: 0.5,
            }}
          >
            <Typography variant="overline" color="text.secondary">
              {item.label}
            </Typography>
            <Typography variant="h5" fontWeight="bold">
              {item.value}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {item.detail}
            </Typography>
          </Paper>
        ))}
      </Box>

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', bgcolor: 'white' }}>
        <Tabs
          value={tabValue}
          onChange={handleTabChange}
          variant="scrollable"
          scrollButtons="auto"
          sx={{
            '& .MuiTab-root': {
              minHeight: 64,
              textTransform: 'none',
              fontSize: '0.95rem',
            },
          }}
        >
          {tabs.map((tab, index) => (
            <Tab
              key={index}
              icon={tab.icon}
              label={tab.label}
              iconPosition="start"
              sx={{ gap: 1 }}
            />
          ))}
        </Tabs>
      </Box>

      {/* Tab Panels */}
      <div style={{ flex: 1, overflow: 'hidden', minHeight: 0, display: 'flex', width: '100%' }}>
        {tabs.map((tab, index) => (
          <TabPanel key={index} value={tabValue} index={index}>
            {tab.component}
          </TabPanel>
        ))}
      </div>

      {/* Footer */}
      <Paper
        elevation={0}
        sx={{
          p: 1.5,
          bgcolor: 'grey.100',
          borderTop: '1px solid',
          borderColor: 'divider',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <Typography variant="caption" color="text.secondary">
          Commercial EV Fleet Management Platform - Developer Documentation
        </Typography>
        <Box display="flex" gap={1}>
          <Chip label="React 18" size="small" variant="outlined" />
          <Chip label="Spring Boot" size="small" variant="outlined" />
          <Chip label="PostgreSQL" size="small" variant="outlined" />
        </Box>
        <Typography variant="caption" color="text.secondary">
          Last Updated: {documentationSummary.lastUpdated}
        </Typography>
      </Paper>
    </Box>
  );
};

export default DeveloperDocsPage;

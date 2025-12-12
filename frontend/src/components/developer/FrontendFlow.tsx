import React, { useState, useMemo } from 'react';
import {
  Box,
  Chip,
  Divider,
  InputAdornment,
  List,
  ListItemButton,
  ListItemText,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Grid,
  Card,
  CardContent,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  Search as SearchIcon,
  Code as CodeIcon,
  Api as ApiIcon,
  Storage as StorageIcon,
  ViewModule as ViewModuleIcon,
  CheckCircle as CheckIcon,
  Cancel as CancelIcon,
  Dashboard as DashboardIcon,
  DirectionsCar as FleetIcon,
  EvStation as ChargingIcon,
  Person as DriverIcon,
  Build as MaintenanceIcon,
  BarChart as AnalyticsIcon,
  Receipt as BillingIcon,
  Settings as SettingsIcon,
  Notifications as NotificationIcon,
} from '@mui/icons-material';
import { frontendPagesData, pageStats, type PageData } from './data';

// Category icons mapping
const categoryIcons: Record<string, React.ReactNode> = {
  dashboard: <DashboardIcon />,
  fleet: <FleetIcon />,
  charging: <ChargingIcon />,
  driver: <DriverIcon />,
  maintenance: <MaintenanceIcon />,
  analytics: <AnalyticsIcon />,
  billing: <BillingIcon />,
  settings: <SettingsIcon />,
  notification: <NotificationIcon />,
  auth: <CodeIcon />,
  utility: <ViewModuleIcon />,
};

// Category colors
const categoryColors: Record<string, string> = {
  dashboard: '#2196F3',
  fleet: '#4CAF50',
  charging: '#FF9800',
  driver: '#9C27B0',
  maintenance: '#795548',
  analytics: '#00BCD4',
  billing: '#F44336',
  settings: '#607D8B',
  notification: '#E91E63',
  auth: '#3F51B5',
  utility: '#9E9E9E',
};

const FrontendFlow: React.FC = () => {
  const [search, setSearch] = useState('');
  const [selectedPageId, setSelectedPageId] = useState<string>(frontendPagesData[0]?.id ?? '');
  const [categoryFilter, setCategoryFilter] = useState<string>('all');

  // Get unique categories
  const categories = useMemo(() => {
    const cats = [...new Set(frontendPagesData.map(p => p.category))];
    return ['all', ...cats];
  }, []);

  // Filter pages
  const filteredPages = useMemo(() => {
    const term = search.toLowerCase();
    return frontendPagesData
      .filter(page => {
        const matchesSearch = [page.name, page.path, page.file, page.description, page.category]
          .some(value => value?.toLowerCase().includes(term));
        const matchesCategory = categoryFilter === 'all' || page.category === categoryFilter;
        return matchesSearch && matchesCategory;
      })
      .sort((a, b) => a.category.localeCompare(b.category) || a.name.localeCompare(b.name));
  }, [search, categoryFilter]);

  // Selected page
  const selectedPage: PageData | undefined = useMemo(
    () => filteredPages.find(p => p.id === selectedPageId) ?? filteredPages[0],
    [filteredPages, selectedPageId]
  );

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', p: 2, gap: 2, overflow: 'hidden' }}>
      {/* Stats */}
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ flexWrap: 'wrap', flexShrink: 0 }}>
        <Chip icon={<ViewModuleIcon />} label={`Total Pages: ${pageStats.total}`} color="primary" />
        <Chip icon={<StorageIcon />} label={`Redux: ${pageStats.withRedux}`} variant="outlined" />
        <Chip icon={<ApiIcon />} label={`CRUD: ${pageStats.withCRUD}`} variant="outlined" />
        <Chip icon={<CodeIcon />} label={`Mock Data: ${pageStats.withMockData}`} variant="outlined" color="warning" />
      </Stack>

      <Paper
        variant="outlined"
        sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '350px 1fr' }, gap: 0, flex: 1, overflow: 'hidden', minHeight: 0 }}
      >
        {/* Left: Page List */}
        <Box sx={{ p: 2, borderRight: { md: '1px solid' }, borderColor: 'divider', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          <TextField
            fullWidth
            size="small"
            placeholder="Search pages, routes, files..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon fontSize="small" />
                </InputAdornment>
              ),
            }}
            sx={{ mb: 1, flexShrink: 0 }}
          />
          
          {/* Category Filter */}
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mb: 1, flexShrink: 0 }}>
            {categories.map(cat => (
              <Chip
                key={cat}
                label={cat === 'all' ? 'All' : cat}
                size="small"
                onClick={() => setCategoryFilter(cat)}
                color={categoryFilter === cat ? 'primary' : 'default'}
                variant={categoryFilter === cat ? 'filled' : 'outlined'}
                sx={{ textTransform: 'capitalize', cursor: 'pointer' }}
              />
            ))}
          </Box>

          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 1, flexShrink: 0 }}>
            {filteredPages.length} pages
          </Typography>

          <List dense sx={{ flex: 1, overflow: 'auto', minHeight: 0 }}>
            {filteredPages.map((page) => (
              <ListItemButton
                key={page.id}
                selected={page.id === selectedPageId}
                onClick={() => setSelectedPageId(page.id)}
                sx={{ borderRadius: 1, mb: 0.5 }}
              >
                <Box sx={{ mr: 1, color: categoryColors[page.category] || 'grey.500' }}>
                  {categoryIcons[page.category] || <ViewModuleIcon />}
                </Box>
                <ListItemText
                  primary={
                    <Typography variant="body2" fontWeight={600} noWrap>
                      {page.name}
                    </Typography>
                  }
                  secondary={
                    <Stack spacing={0.5}>
                      <Typography variant="caption" color="text.secondary" fontFamily="monospace" noWrap>
                        {page.path}
                      </Typography>
                      <Stack direction="row" spacing={0.5}>
                        <Chip
                          label={page.category}
                          size="small"
                          sx={{
                            bgcolor: categoryColors[page.category] || 'grey.200',
                            color: 'white',
                            height: 18,
                            fontSize: '0.65rem',
                          }}
                        />
                        {page.hasCRUD && <Chip label="CRUD" size="small" color="success" sx={{ height: 18, fontSize: '0.65rem' }} />}
                      </Stack>
                    </Stack>
                  }
                />
              </ListItemButton>
            ))}
          </List>
        </Box>

        {/* Right: Page Details */}
        <Box sx={{ p: 2, overflow: 'auto' }}>
          {selectedPage ? (
            <Stack spacing={3}>
              {/* Header */}
              <Box>
                <Box display="flex" alignItems="center" gap={1} flexWrap="wrap" sx={{ mb: 1 }}>
                  <Box sx={{ color: categoryColors[selectedPage.category] }}>
                    {categoryIcons[selectedPage.category] || <ViewModuleIcon fontSize="large" />}
                  </Box>
                  <Typography variant="h5" fontWeight="bold">
                    {selectedPage.name}
                  </Typography>
                  <Chip
                    label={selectedPage.category}
                    sx={{
                      bgcolor: categoryColors[selectedPage.category] || 'grey.200',
                      color: 'white',
                      textTransform: 'uppercase',
                    }}
                  />
                </Box>
                <Typography variant="body2" fontFamily="monospace" color="text.secondary">
                  📁 {selectedPage.file} &nbsp;|&nbsp; 🔗 {selectedPage.path}
                </Typography>
              </Box>

              {/* Description */}
              <Box sx={{ bgcolor: 'info.light', p: 2, borderRadius: 1, borderLeft: '4px solid', borderColor: 'info.main' }}>
                <Typography variant="subtitle2" fontWeight="bold" color="info.dark">
                  📋 What This Page Does
                </Typography>
                <Typography variant="body2" color="info.dark" sx={{ mt: 0.5 }}>
                  {selectedPage.description}
                </Typography>
              </Box>

              {/* Features */}
              {selectedPage.features && selectedPage.features.length > 0 && (
                <Box sx={{ bgcolor: 'success.light', p: 2, borderRadius: 1, borderLeft: '4px solid', borderColor: 'success.main' }}>
                  <Typography variant="subtitle2" fontWeight="bold" color="success.dark" sx={{ mb: 1 }}>
                    ✨ Features & Functionality
                  </Typography>
                  <Grid container spacing={1}>
                    {selectedPage.features.map((feature, idx) => (
                      <Grid item xs={12} sm={6} key={idx}>
                        <Card variant="outlined" sx={{ bgcolor: 'transparent', border: '1px solid', borderColor: 'success.main' }}>
                          <CardContent sx={{ p: 1.5, '&:last-child': { pb: 1.5 } }}>
                            <Typography variant="body2" fontWeight={600} color="success.dark">
                              {feature.name}
                            </Typography>
                            <Typography variant="caption" color="success.dark">
                              {feature.description}
                            </Typography>
                          </CardContent>
                        </Card>
                      </Grid>
                    ))}
                  </Grid>
                </Box>
              )}

              <Divider />

              {/* API Calls - What backend it connects to */}
              {selectedPage.apiCalls && selectedPage.apiCalls.length > 0 && (
                <Accordion defaultExpanded>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Box display="flex" alignItems="center" gap={1}>
                      <ApiIcon color="primary" />
                      <Typography variant="subtitle2" fontWeight="bold">
                        🔌 Backend API Connections ({selectedPage.apiCalls.length} endpoints)
                      </Typography>
                    </Box>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 1 }}>
                      These are the backend endpoints this page calls to fetch/send data:
                    </Typography>
                    <TableContainer component={Paper} variant="outlined">
                      <Table size="small">
                        <TableHead>
                          <TableRow sx={{ bgcolor: 'grey.100' }}>
                            <TableCell sx={{ fontWeight: 700, width: '15%' }}>Method</TableCell>
                            <TableCell sx={{ fontWeight: 700, width: '45%' }}>Endpoint</TableCell>
                            <TableCell sx={{ fontWeight: 700, width: '40%' }}>Purpose</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {selectedPage.apiCalls.map((api, idx) => (
                            <TableRow key={idx}>
                              <TableCell>
                                <Chip
                                  label={api.method}
                                  size="small"
                                  sx={{
                                    bgcolor:
                                      api.method === 'GET' ? 'success.main' :
                                      api.method === 'POST' ? 'primary.main' :
                                      api.method === 'PUT' ? 'warning.main' :
                                      'error.main',
                                    color: 'white',
                                    fontWeight: 'bold',
                                  }}
                                />
                              </TableCell>
                              <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.85rem' }}>
                                {api.endpoint}
                              </TableCell>
                              <TableCell sx={{ fontSize: '0.85rem', color: 'text.secondary' }}>
                                {api.description}
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  </AccordionDetails>
                </Accordion>
              )}

              {/* Redux State Management */}
              {selectedPage.redux && (
                <Accordion defaultExpanded>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Box display="flex" alignItems="center" gap={1}>
                      <StorageIcon color="secondary" />
                      <Typography variant="subtitle2" fontWeight="bold">
                        🗄️ Redux State Management
                      </Typography>
                    </Box>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 1 }}>
                      This page uses Redux for state management. Here's how data flows:
                    </Typography>
                    <Stack spacing={2}>
                      <Box sx={{ bgcolor: 'grey.50', p: 1.5, borderRadius: 1 }}>
                        <Typography variant="body2" fontWeight={600} color="secondary.main">
                          Redux Slice: <code>{selectedPage.redux.slice}</code>
                        </Typography>
                      </Box>
                      
                      {selectedPage.redux.selectors && selectedPage.redux.selectors.length > 0 && (
                        <Box>
                          <Typography variant="body2" fontWeight={600} sx={{ mb: 0.5 }}>
                            📖 Selectors (Read Data):
                          </Typography>
                          <Stack direction="row" spacing={0.5} flexWrap="wrap">
                            {selectedPage.redux.selectors.map((sel, idx) => (
                              <Chip key={idx} label={sel} size="small" variant="outlined" sx={{ fontFamily: 'monospace' }} />
                            ))}
                          </Stack>
                        </Box>
                      )}

                      {selectedPage.redux.actions && selectedPage.redux.actions.length > 0 && (
                        <Box>
                          <Typography variant="body2" fontWeight={600} sx={{ mb: 0.5 }}>
                            ⚡ Actions (Modify Data):
                          </Typography>
                          <Stack direction="row" spacing={0.5} flexWrap="wrap">
                            {selectedPage.redux.actions.map((action, idx) => (
                              <Chip key={idx} label={action} size="small" color="primary" variant="outlined" sx={{ fontFamily: 'monospace' }} />
                            ))}
                          </Stack>
                        </Box>
                      )}
                    </Stack>
                  </AccordionDetails>
                </Accordion>
              )}

              {/* Child Components */}
              {selectedPage.childComponents && selectedPage.childComponents.length > 0 && (
                <Accordion>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Box display="flex" alignItems="center" gap={1}>
                      <CodeIcon color="action" />
                      <Typography variant="subtitle2" fontWeight="bold">
                        🧩 Child Components Used
                      </Typography>
                    </Box>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 1 }}>
                      These React components are imported and used by this page:
                    </Typography>
                    <Stack direction="row" spacing={0.5} flexWrap="wrap">
                      {selectedPage.childComponents.map((comp, idx) => (
                        <Chip key={idx} label={comp} size="small" icon={<CodeIcon />} />
                      ))}
                    </Stack>
                  </AccordionDetails>
                </Accordion>
              )}

              {/* Tabs if present */}
              {selectedPage.tabs && selectedPage.tabs.length > 0 && (
                <Accordion>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Box display="flex" alignItems="center" gap={1}>
                      <ViewModuleIcon color="action" />
                      <Typography variant="subtitle2" fontWeight="bold">
                        📑 Page Tabs
                      </Typography>
                    </Box>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Stack direction="row" spacing={0.5} flexWrap="wrap">
                      {selectedPage.tabs.map((tab, idx) => (
                        <Chip key={idx} label={tab} size="small" variant="outlined" />
                      ))}
                    </Stack>
                  </AccordionDetails>
                </Accordion>
              )}

              {/* Page Capabilities Summary */}
              <Box sx={{ bgcolor: 'grey.100', p: 2, borderRadius: 1 }}>
                <Typography variant="subtitle2" fontWeight="bold" sx={{ mb: 1 }}>
                  📊 Page Capabilities
                </Typography>
                <Grid container spacing={1}>
                  <Grid item xs={6} sm={3}>
                    <Stack direction="row" alignItems="center" spacing={0.5}>
                      {selectedPage.hasCRUD ? <CheckIcon color="success" /> : <CancelIcon color="disabled" />}
                      <Typography variant="caption">CRUD Operations</Typography>
                    </Stack>
                  </Grid>
                  <Grid item xs={6} sm={3}>
                    <Stack direction="row" alignItems="center" spacing={0.5}>
                      {selectedPage.hasSearch ? <CheckIcon color="success" /> : <CancelIcon color="disabled" />}
                      <Typography variant="caption">Search/Filter</Typography>
                    </Stack>
                  </Grid>
                  <Grid item xs={6} sm={3}>
                    <Stack direction="row" alignItems="center" spacing={0.5}>
                      {selectedPage.hasExport ? <CheckIcon color="success" /> : <CancelIcon color="disabled" />}
                      <Typography variant="caption">Export Data</Typography>
                    </Stack>
                  </Grid>
                  <Grid item xs={6} sm={3}>
                    <Stack direction="row" alignItems="center" spacing={0.5}>
                      {selectedPage.mockData ? <CheckIcon color="warning" /> : <CancelIcon color="disabled" />}
                      <Typography variant="caption">Uses Mock Data</Typography>
                    </Stack>
                  </Grid>
                </Grid>
              </Box>

              {/* Mock Data Warning */}
              {selectedPage.mockData && (
                <Box sx={{ bgcolor: 'warning.light', p: 1.5, borderRadius: 1, borderLeft: '4px solid', borderColor: 'warning.main' }}>
                  <Typography variant="subtitle2" fontWeight="bold" color="warning.dark">
                    ⚠️ Uses Mock Data
                  </Typography>
                  <Typography variant="body2" color="warning.dark" sx={{ mt: 0.5 }}>
                    This page currently uses mock/demo data and is not fully connected to the backend API.
                  </Typography>
                </Box>
              )}

            </Stack>
          ) : (
            <Typography variant="body2" color="text.secondary">
              No pages match your search.
            </Typography>
          )}
        </Box>
      </Paper>
    </Box>
  );
};

export default FrontendFlow;

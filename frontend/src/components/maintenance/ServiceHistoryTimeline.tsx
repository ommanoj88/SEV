/**
 * ServiceHistoryTimeline.tsx
 * PR #40: Service History Timeline View
 * 
 * Features:
 * - Chronological timeline of all services
 * - Service event details with expand/collapse
 * - Filter by vehicle, date range, service type
 * - Export to PDF functionality
 * - Cost tracking and totals
 * - Before/after photos support
 */

import React, { useState, useMemo } from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Card,
  CardContent,
  Button,
  IconButton,
  Chip,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Autocomplete,
  Tooltip,
  Collapse,
  Divider,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Avatar,
  Badge,
  Tab,
  Tabs,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  ImageList,
  ImageListItem,
} from '@mui/material';
import {
  Timeline,
  TimelineItem,
  TimelineSeparator,
  TimelineConnector,
  TimelineContent,
  TimelineDot,
  TimelineOppositeContent,
} from '@mui/lab';
import {
  Build,
  DirectionsCar,
  Schedule,
  CheckCircle,
  Warning,
  Error,
  ExpandMore,
  ExpandLess,
  Download,
  Print,
  FilterList,
  Search,
  AttachMoney,
  Person,
  LocationOn,
  Description,
  Photo,
  TrendingUp,
  CalendarMonth,
  Receipt,
  Speed,
  BatteryChargingFull,
  Tire,
  AcUnit,
  Settings,
  LocalShipping,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import {
  format,
  parseISO,
  isWithinInterval,
  startOfYear,
  endOfYear,
  subMonths,
  differenceInDays,
} from 'date-fns';

// Types
interface ServiceEvent {
  id: string;
  vehicleId: string;
  vehicleName: string;
  licensePlate: string;
  serviceType: ServiceType;
  title: string;
  description: string;
  date: Date;
  status: 'completed' | 'in_progress' | 'cancelled' | 'warranty';
  mileage: number;
  cost: number;
  laborCost: number;
  partsCost: number;
  technician: string;
  serviceCenter: string;
  duration: number; // hours
  notes: string[];
  partsReplaced: Part[];
  photos: { url: string; caption: string }[];
  invoiceNumber: string;
  warranty: boolean;
  nextServiceDue?: Date;
}

interface Part {
  name: string;
  partNumber: string;
  quantity: number;
  cost: number;
}

type ServiceType = 
  | 'routine'
  | 'repair'
  | 'battery'
  | 'tire'
  | 'brake'
  | 'hvac'
  | 'recall'
  | 'inspection'
  | 'software'
  | 'other';

interface Vehicle {
  id: string;
  name: string;
  licensePlate: string;
}

// Service type configuration
const serviceTypeConfig: Record<ServiceType, { icon: React.ReactNode; label: string; color: string }> = {
  routine: { icon: <Build />, label: 'Routine Service', color: '#4CAF50' },
  repair: { icon: <Settings />, label: 'Repair', color: '#F44336' },
  battery: { icon: <BatteryChargingFull />, label: 'Battery Service', color: '#2196F3' },
  tire: { icon: <Tire />, label: 'Tire Service', color: '#795548' },
  brake: { icon: <Speed />, label: 'Brake Service', color: '#FF9800' },
  hvac: { icon: <AcUnit />, label: 'HVAC', color: '#00BCD4' },
  recall: { icon: <Warning />, label: 'Recall', color: '#9C27B0' },
  inspection: { icon: <Search />, label: 'Inspection', color: '#607D8B' },
  software: { icon: <Settings />, label: 'Software Update', color: '#3F51B5' },
  other: { icon: <Build />, label: 'Other', color: '#9E9E9E' },
};

// Tire component for icon
const Tire: React.FC = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
    <circle cx="12" cy="12" r="10" fill="none" stroke="currentColor" strokeWidth="2"/>
    <circle cx="12" cy="12" r="4" />
  </svg>
);

// Mock vehicles
const mockVehicles: Vehicle[] = [
  { id: 'v1', name: 'Tesla Model 3 #1', licensePlate: 'EV-001' },
  { id: 'v2', name: 'Tata Nexon EV', licensePlate: 'EV-002' },
  { id: 'v3', name: 'MG ZS EV', licensePlate: 'EV-003' },
  { id: 'v4', name: 'Hyundai Kona', licensePlate: 'EV-004' },
];

// Mock service events
const mockServiceEvents: ServiceEvent[] = [
  {
    id: '1',
    vehicleId: 'v1',
    vehicleName: 'Tesla Model 3 #1',
    licensePlate: 'EV-001',
    serviceType: 'routine',
    title: 'Annual Service',
    description: 'Comprehensive annual service including all fluid checks and filter replacements',
    date: new Date(),
    status: 'completed',
    mileage: 45000,
    cost: 25000,
    laborCost: 10000,
    partsCost: 15000,
    technician: 'Rajesh Kumar',
    serviceCenter: 'Tesla Service Center, Bangalore',
    duration: 4,
    notes: ['All systems checked', 'Brake fluid replaced', 'Cabin filter replaced'],
    partsReplaced: [
      { name: 'Cabin Air Filter', partNumber: 'TSLA-CAF-001', quantity: 1, cost: 3500 },
      { name: 'Brake Fluid', partNumber: 'TSLA-BF-001', quantity: 1, cost: 2500 },
      { name: 'Wiper Blades', partNumber: 'TSLA-WB-001', quantity: 2, cost: 4000 },
    ],
    photos: [
      { url: '/images/service1.jpg', caption: 'Before service' },
      { url: '/images/service2.jpg', caption: 'After service' },
    ],
    invoiceNumber: 'INV-2024-001',
    warranty: false,
    nextServiceDue: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000),
  },
  {
    id: '2',
    vehicleId: 'v2',
    vehicleName: 'Tata Nexon EV',
    licensePlate: 'EV-002',
    serviceType: 'tire',
    title: 'Tire Replacement',
    description: 'Replaced all 4 tires with new Continental ContiPremiumContact 6',
    date: subMonths(new Date(), 1),
    status: 'completed',
    mileage: 32000,
    cost: 48000,
    laborCost: 3000,
    partsCost: 45000,
    technician: 'Amit Singh',
    serviceCenter: 'Tata Motors Service, Mumbai',
    duration: 2,
    notes: ['Wheel alignment performed', 'Wheel balancing done', 'Old tires disposed'],
    partsReplaced: [
      { name: 'Continental Tire 215/60R17', partNumber: 'CNT-PC6-215', quantity: 4, cost: 45000 },
    ],
    photos: [],
    invoiceNumber: 'INV-2024-002',
    warranty: false,
  },
  {
    id: '3',
    vehicleId: 'v1',
    vehicleName: 'Tesla Model 3 #1',
    licensePlate: 'EV-001',
    serviceType: 'battery',
    title: 'Battery Health Check',
    description: 'Routine battery health diagnostic and thermal management system check',
    date: subMonths(new Date(), 2),
    status: 'completed',
    mileage: 42000,
    cost: 5000,
    laborCost: 5000,
    partsCost: 0,
    technician: 'Vikram Patel',
    serviceCenter: 'Tesla Service Center, Bangalore',
    duration: 2,
    notes: ['Battery health at 92%', 'Thermal management system normal', 'No cell imbalances detected'],
    partsReplaced: [],
    photos: [],
    invoiceNumber: 'INV-2024-003',
    warranty: false,
  },
  {
    id: '4',
    vehicleId: 'v3',
    vehicleName: 'MG ZS EV',
    licensePlate: 'EV-003',
    serviceType: 'recall',
    title: 'Safety Recall - BMS Update',
    description: 'Mandatory recall for battery management system software update',
    date: subMonths(new Date(), 3),
    status: 'warranty',
    mileage: 18000,
    cost: 0,
    laborCost: 0,
    partsCost: 0,
    technician: 'Suresh Mehta',
    serviceCenter: 'MG Motor Service, Delhi',
    duration: 1,
    notes: ['BMS software updated to v3.2.1', 'Covered under warranty'],
    partsReplaced: [],
    photos: [],
    invoiceNumber: 'INV-2024-004',
    warranty: true,
  },
  {
    id: '5',
    vehicleId: 'v4',
    vehicleName: 'Hyundai Kona',
    licensePlate: 'EV-004',
    serviceType: 'brake',
    title: 'Brake Pad Replacement',
    description: 'Front and rear brake pad replacement',
    date: subMonths(new Date(), 4),
    status: 'completed',
    mileage: 55000,
    cost: 12000,
    laborCost: 4000,
    partsCost: 8000,
    technician: 'Rahul Sharma',
    serviceCenter: 'Hyundai Service, Chennai',
    duration: 3,
    notes: ['Front pads at 10%', 'Rear pads at 15%', 'Rotors in good condition'],
    partsReplaced: [
      { name: 'Front Brake Pads', partNumber: 'HY-BP-F001', quantity: 1, cost: 4500 },
      { name: 'Rear Brake Pads', partNumber: 'HY-BP-R001', quantity: 1, cost: 3500 },
    ],
    photos: [],
    invoiceNumber: 'INV-2024-005',
    warranty: false,
  },
  {
    id: '6',
    vehicleId: 'v2',
    vehicleName: 'Tata Nexon EV',
    licensePlate: 'EV-002',
    serviceType: 'hvac',
    title: 'AC Compressor Repair',
    description: 'AC compressor replacement due to refrigerant leak',
    date: subMonths(new Date(), 5),
    status: 'completed',
    mileage: 28000,
    cost: 35000,
    laborCost: 8000,
    partsCost: 27000,
    technician: 'Amit Singh',
    serviceCenter: 'Tata Motors Service, Mumbai',
    duration: 5,
    notes: ['Compressor replaced', 'Refrigerant recharged', 'System tested'],
    partsReplaced: [
      { name: 'AC Compressor', partNumber: 'TATA-ACC-001', quantity: 1, cost: 25000 },
      { name: 'Refrigerant R134a', partNumber: 'REF-134A', quantity: 1, cost: 2000 },
    ],
    photos: [],
    invoiceNumber: 'INV-2024-006',
    warranty: false,
  },
];

export const ServiceHistoryTimeline: React.FC = () => {
  const [events, setEvents] = useState<ServiceEvent[]>(mockServiceEvents);
  const [selectedVehicle, setSelectedVehicle] = useState<Vehicle | null>(null);
  const [selectedType, setSelectedType] = useState<ServiceType | 'all'>('all');
  const [startDate, setStartDate] = useState<Date | null>(subMonths(new Date(), 12));
  const [endDate, setEndDate] = useState<Date | null>(new Date());
  const [expandedEventId, setExpandedEventId] = useState<string | null>(null);
  const [selectedTab, setSelectedTab] = useState(0);
  const [photoDialogOpen, setPhotoDialogOpen] = useState(false);
  const [selectedPhotos, setSelectedPhotos] = useState<{ url: string; caption: string }[]>([]);
  const [searchQuery, setSearchQuery] = useState('');

  // Filter events
  const filteredEvents = useMemo(() => {
    let filtered = events;

    if (selectedVehicle) {
      filtered = filtered.filter(e => e.vehicleId === selectedVehicle.id);
    }

    if (selectedType !== 'all') {
      filtered = filtered.filter(e => e.serviceType === selectedType);
    }

    if (startDate && endDate) {
      filtered = filtered.filter(e =>
        isWithinInterval(e.date, { start: startDate, end: endDate })
      );
    }

    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(e =>
        e.title.toLowerCase().includes(query) ||
        e.description.toLowerCase().includes(query) ||
        e.vehicleName.toLowerCase().includes(query) ||
        e.invoiceNumber.toLowerCase().includes(query)
      );
    }

    return filtered.sort((a, b) => b.date.getTime() - a.date.getTime());
  }, [events, selectedVehicle, selectedType, startDate, endDate, searchQuery]);

  // Statistics
  const stats = useMemo(() => {
    const total = filteredEvents.reduce((sum, e) => sum + e.cost, 0);
    const laborTotal = filteredEvents.reduce((sum, e) => sum + e.laborCost, 0);
    const partsTotal = filteredEvents.reduce((sum, e) => sum + e.partsCost, 0);
    const avgCost = filteredEvents.length > 0 ? total / filteredEvents.length : 0;

    // Cost by type
    const costByType = Object.keys(serviceTypeConfig).map(type => ({
      type,
      label: serviceTypeConfig[type as ServiceType].label,
      cost: filteredEvents
        .filter(e => e.serviceType === type)
        .reduce((sum, e) => sum + e.cost, 0),
      color: serviceTypeConfig[type as ServiceType].color,
    })).filter(d => d.cost > 0);

    // Monthly costs
    const monthlyData: Record<string, number> = {};
    filteredEvents.forEach(e => {
      const month = format(e.date, 'MMM yyyy');
      monthlyData[month] = (monthlyData[month] || 0) + e.cost;
    });
    const monthlyCosts = Object.entries(monthlyData).map(([month, cost]) => ({ month, cost }));

    return { total, laborTotal, partsTotal, avgCost, costByType, monthlyCosts };
  }, [filteredEvents]);

  // Handlers
  const handleExpandEvent = (eventId: string) => {
    setExpandedEventId(expandedEventId === eventId ? null : eventId);
  };

  const handleViewPhotos = (photos: { url: string; caption: string }[]) => {
    setSelectedPhotos(photos);
    setPhotoDialogOpen(true);
  };

  const handleExportPDF = () => {
    // In production, this would generate a PDF
    console.log('Exporting service history to PDF...');
    alert('PDF export functionality would be implemented here');
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'completed': return '#4CAF50';
      case 'in_progress': return '#FF9800';
      case 'cancelled': return '#F44336';
      case 'warranty': return '#9C27B0';
      default: return '#9E9E9E';
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Schedule sx={{ fontSize: 40, color: 'primary.main' }} />
          <Box>
            <Typography variant="h5" fontWeight={600}>
              Service History Timeline
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Track all maintenance and service events
            </Typography>
          </Box>
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button variant="outlined" startIcon={<Print />}>
            Print
          </Button>
          <Button variant="contained" startIcon={<Download />} onClick={handleExportPDF}>
            Export PDF
          </Button>
        </Box>
      </Box>

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Receipt color="primary" />
                <Typography variant="body2" color="text.secondary">
                  Total Services
                </Typography>
              </Box>
              <Typography variant="h4" fontWeight={600}>
                {filteredEvents.length}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <AttachMoney color="success" />
                <Typography variant="body2" color="text.secondary">
                  Total Cost
                </Typography>
              </Box>
              <Typography variant="h4" fontWeight={600}>
                ₹{stats.total.toLocaleString()}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Person color="info" />
                <Typography variant="body2" color="text.secondary">
                  Labor Cost
                </Typography>
              </Box>
              <Typography variant="h4" fontWeight={600}>
                ₹{stats.laborTotal.toLocaleString()}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <LocalShipping color="warning" />
                <Typography variant="body2" color="text.secondary">
                  Parts Cost
                </Typography>
              </Box>
              <Typography variant="h4" fontWeight={600}>
                ₹{stats.partsTotal.toLocaleString()}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} md={3}>
            <TextField
              fullWidth
              size="small"
              placeholder="Search services..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              InputProps={{
                startAdornment: <Search sx={{ mr: 1, color: 'text.secondary' }} />,
              }}
            />
          </Grid>
          <Grid item xs={12} md={3}>
            <Autocomplete
              size="small"
              options={mockVehicles}
              getOptionLabel={(v) => `${v.name} (${v.licensePlate})`}
              value={selectedVehicle}
              onChange={(_, value) => setSelectedVehicle(value)}
              renderInput={(params) => (
                <TextField {...params} placeholder="Filter by vehicle" />
              )}
            />
          </Grid>
          <Grid item xs={12} md={2}>
            <FormControl fullWidth size="small">
              <InputLabel>Service Type</InputLabel>
              <Select
                value={selectedType}
                label="Service Type"
                onChange={(e) => setSelectedType(e.target.value as ServiceType | 'all')}
              >
                <MenuItem value="all">All Types</MenuItem>
                {Object.entries(serviceTypeConfig).map(([key, config]) => (
                  <MenuItem key={key} value={key}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Box sx={{ width: 12, height: 12, borderRadius: '50%', bgcolor: config.color }} />
                      {config.label}
                    </Box>
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={6} md={2}>
            <DatePicker
              label="From"
              value={startDate}
              onChange={setStartDate}
              slotProps={{ textField: { size: 'small', fullWidth: true } }}
            />
          </Grid>
          <Grid item xs={6} md={2}>
            <DatePicker
              label="To"
              value={endDate}
              onChange={setEndDate}
              slotProps={{ textField: { size: 'small', fullWidth: true } }}
            />
          </Grid>
        </Grid>
      </Paper>

      {/* Tabs */}
      <Paper sx={{ mb: 3 }}>
        <Tabs value={selectedTab} onChange={(_, v) => setSelectedTab(v)}>
          <Tab label="Timeline View" />
          <Tab label="Table View" />
          <Tab label="Analytics" />
        </Tabs>
      </Paper>

      {/* Timeline View */}
      {selectedTab === 0 && (
        <Timeline position="alternate">
          {filteredEvents.map((event, index) => (
            <TimelineItem key={event.id}>
              <TimelineOppositeContent color="text.secondary">
                <Typography variant="body2">
                  {format(event.date, 'MMM dd, yyyy')}
                </Typography>
                <Typography variant="caption">
                  {event.mileage.toLocaleString()} km
                </Typography>
              </TimelineOppositeContent>
              <TimelineSeparator>
                <TimelineDot sx={{ bgcolor: serviceTypeConfig[event.serviceType].color }}>
                  {serviceTypeConfig[event.serviceType].icon}
                </TimelineDot>
                {index < filteredEvents.length - 1 && <TimelineConnector />}
              </TimelineSeparator>
              <TimelineContent>
                <Card sx={{ cursor: 'pointer' }} onClick={() => handleExpandEvent(event.id)}>
                  <CardContent sx={{ pb: expandedEventId === event.id ? 0 : 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <Box>
                        <Typography variant="h6">{event.title}</Typography>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
                          <DirectionsCar fontSize="small" color="action" />
                          <Typography variant="body2" color="text.secondary">
                            {event.vehicleName}
                          </Typography>
                          <Chip
                            size="small"
                            label={event.status}
                            sx={{ bgcolor: getStatusColor(event.status), color: 'white' }}
                          />
                        </Box>
                      </Box>
                      <Box sx={{ textAlign: 'right' }}>
                        <Typography variant="h6" color="primary">
                          ₹{event.cost.toLocaleString()}
                        </Typography>
                        <IconButton size="small">
                          {expandedEventId === event.id ? <ExpandLess /> : <ExpandMore />}
                        </IconButton>
                      </Box>
                    </Box>

                    <Collapse in={expandedEventId === event.id}>
                      <Divider sx={{ my: 2 }} />
                      <Typography variant="body2" paragraph>
                        {event.description}
                      </Typography>

                      <Grid container spacing={2}>
                        <Grid item xs={6}>
                          <Typography variant="caption" color="text.secondary">
                            Service Center
                          </Typography>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                            <LocationOn fontSize="small" />
                            <Typography variant="body2">{event.serviceCenter}</Typography>
                          </Box>
                        </Grid>
                        <Grid item xs={6}>
                          <Typography variant="caption" color="text.secondary">
                            Technician
                          </Typography>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                            <Person fontSize="small" />
                            <Typography variant="body2">{event.technician}</Typography>
                          </Box>
                        </Grid>
                        <Grid item xs={6}>
                          <Typography variant="caption" color="text.secondary">
                            Duration
                          </Typography>
                          <Typography variant="body2">{event.duration} hours</Typography>
                        </Grid>
                        <Grid item xs={6}>
                          <Typography variant="caption" color="text.secondary">
                            Invoice
                          </Typography>
                          <Typography variant="body2">{event.invoiceNumber}</Typography>
                        </Grid>
                      </Grid>

                      {event.partsReplaced.length > 0 && (
                        <Box sx={{ mt: 2 }}>
                          <Typography variant="subtitle2" gutterBottom>
                            Parts Replaced
                          </Typography>
                          <Table size="small">
                            <TableHead>
                              <TableRow>
                                <TableCell>Part</TableCell>
                                <TableCell>Part #</TableCell>
                                <TableCell align="right">Qty</TableCell>
                                <TableCell align="right">Cost</TableCell>
                              </TableRow>
                            </TableHead>
                            <TableBody>
                              {event.partsReplaced.map((part, idx) => (
                                <TableRow key={idx}>
                                  <TableCell>{part.name}</TableCell>
                                  <TableCell>{part.partNumber}</TableCell>
                                  <TableCell align="right">{part.quantity}</TableCell>
                                  <TableCell align="right">₹{part.cost.toLocaleString()}</TableCell>
                                </TableRow>
                              ))}
                            </TableBody>
                          </Table>
                        </Box>
                      )}

                      {event.notes.length > 0 && (
                        <Box sx={{ mt: 2 }}>
                          <Typography variant="subtitle2" gutterBottom>
                            Notes
                          </Typography>
                          <List dense disablePadding>
                            {event.notes.map((note, idx) => (
                              <ListItem key={idx} disablePadding>
                                <ListItemIcon sx={{ minWidth: 28 }}>
                                  <CheckCircle fontSize="small" color="success" />
                                </ListItemIcon>
                                <ListItemText primary={note} />
                              </ListItem>
                            ))}
                          </List>
                        </Box>
                      )}

                      {event.photos.length > 0 && (
                        <Box sx={{ mt: 2 }}>
                          <Button
                            size="small"
                            startIcon={<Photo />}
                            onClick={(e) => {
                              e.stopPropagation();
                              handleViewPhotos(event.photos);
                            }}
                          >
                            View Photos ({event.photos.length})
                          </Button>
                        </Box>
                      )}

                      <Box sx={{ mt: 2, p: 1, bgcolor: 'action.hover', borderRadius: 1 }}>
                        <Grid container spacing={1}>
                          <Grid item xs={4}>
                            <Typography variant="caption" color="text.secondary">
                              Labor
                            </Typography>
                            <Typography variant="body2" fontWeight={600}>
                              ₹{event.laborCost.toLocaleString()}
                            </Typography>
                          </Grid>
                          <Grid item xs={4}>
                            <Typography variant="caption" color="text.secondary">
                              Parts
                            </Typography>
                            <Typography variant="body2" fontWeight={600}>
                              ₹{event.partsCost.toLocaleString()}
                            </Typography>
                          </Grid>
                          <Grid item xs={4}>
                            <Typography variant="caption" color="text.secondary">
                              Total
                            </Typography>
                            <Typography variant="body2" fontWeight={600} color="primary">
                              ₹{event.cost.toLocaleString()}
                            </Typography>
                          </Grid>
                        </Grid>
                      </Box>
                    </Collapse>
                  </CardContent>
                </Card>
              </TimelineContent>
            </TimelineItem>
          ))}
        </Timeline>
      )}

      {/* Table View */}
      {selectedTab === 1 && (
        <Paper>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Date</TableCell>
                  <TableCell>Vehicle</TableCell>
                  <TableCell>Service</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell align="right">Mileage</TableCell>
                  <TableCell align="right">Cost</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Invoice</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredEvents.map(event => (
                  <TableRow key={event.id} hover>
                    <TableCell>{format(event.date, 'MMM dd, yyyy')}</TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <DirectionsCar fontSize="small" />
                        {event.licensePlate}
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight={500}>
                        {event.title}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {event.serviceCenter}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={serviceTypeConfig[event.serviceType].label}
                        sx={{ bgcolor: serviceTypeConfig[event.serviceType].color, color: 'white' }}
                      />
                    </TableCell>
                    <TableCell align="right">{event.mileage.toLocaleString()} km</TableCell>
                    <TableCell align="right">₹{event.cost.toLocaleString()}</TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={event.status}
                        sx={{ bgcolor: getStatusColor(event.status), color: 'white' }}
                      />
                    </TableCell>
                    <TableCell>{event.invoiceNumber}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}

      {/* Analytics View */}
      {selectedTab === 2 && (
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 2 }}>
              <Typography variant="h6" gutterBottom>
                Cost by Service Type
              </Typography>
              <Box sx={{ height: 300 }}>
                <ResponsiveContainer>
                  <PieChart>
                    <Pie
                      data={stats.costByType}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={100}
                      dataKey="cost"
                      label={({ label, cost }) => `${label}: ₹${cost.toLocaleString()}`}
                    >
                      {stats.costByType.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <RechartsTooltip formatter={(value) => `₹${Number(value).toLocaleString()}`} />
                  </PieChart>
                </ResponsiveContainer>
              </Box>
            </Paper>
          </Grid>
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 2 }}>
              <Typography variant="h6" gutterBottom>
                Monthly Service Costs
              </Typography>
              <Box sx={{ height: 300 }}>
                <ResponsiveContainer>
                  <BarChart data={stats.monthlyCosts}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="month" />
                    <YAxis tickFormatter={(v) => `₹${(v/1000).toFixed(0)}k`} />
                    <RechartsTooltip formatter={(value) => `₹${Number(value).toLocaleString()}`} />
                    <Bar dataKey="cost" fill="#2196F3" />
                  </BarChart>
                </ResponsiveContainer>
              </Box>
            </Paper>
          </Grid>
        </Grid>
      )}

      {/* Photo Dialog */}
      <Dialog
        open={photoDialogOpen}
        onClose={() => setPhotoDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Service Photos</DialogTitle>
        <DialogContent>
          <ImageList cols={2} gap={16}>
            {selectedPhotos.map((photo, index) => (
              <ImageListItem key={index}>
                <Box
                  sx={{
                    height: 200,
                    bgcolor: 'grey.200',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    borderRadius: 1,
                  }}
                >
                  <Photo sx={{ fontSize: 60, color: 'grey.400' }} />
                </Box>
                <Typography variant="caption" sx={{ mt: 1 }}>
                  {photo.caption}
                </Typography>
              </ImageListItem>
            ))}
          </ImageList>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPhotoDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ServiceHistoryTimeline;

import React, { useState, useMemo, useCallback } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Paper,
  Button,
  IconButton,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  ListItemSecondaryAction,
  Avatar,
  Badge,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tooltip,
  ToggleButton,
  ToggleButtonGroup,
  Slider,
  CircularProgress,
  LinearProgress,
  Collapse,
  Rating,
} from '@mui/material';
import {
  EvStation as EvStationIcon,
  Schedule as ScheduleIcon,
  AccessTime as TimeIcon,
  Check as CheckIcon,
  Cancel as CancelIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Alarm as AlarmIcon,
  DirectionsCar as CarIcon,
  BatteryChargingFull as BatteryIcon,
  AttachMoney as MoneyIcon,
  LocationOn as LocationIcon,
  QrCode as QrCodeIcon,
  Notifications as NotificationIcon,
  CalendarMonth as CalendarIcon,
  Speed as SpeedIcon,
  Event as EventIcon,
  CheckCircle as CheckCircleIcon,
  HourglassEmpty as PendingIcon,
  Warning as WarningIcon,
  Refresh as RefreshIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
  Add as AddIcon,
  Today as TodayIcon,
  NavigateNext as NextIcon,
  NavigateBefore as PrevIcon,
  Timer as TimerIcon,
} from '@mui/icons-material';
import { DatePicker, TimePicker } from '@mui/x-date-pickers';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { format, addDays, isSameDay, isAfter, isBefore, addMinutes, differenceInMinutes, setHours, setMinutes } from 'date-fns';

// Types
interface ChargingPort {
  id: string;
  portNumber: number;
  type: 'CCS2' | 'CHAdeMO' | 'Type2' | 'GB/T';
  powerKw: number;
  status: 'available' | 'occupied' | 'reserved' | 'maintenance';
}

interface ChargingStation {
  id: string;
  name: string;
  address: string;
  distance: number;
  pricePerKwh: number;
  ports: ChargingPort[];
  rating: number;
  amenities: string[];
}

interface Vehicle {
  id: string;
  name: string;
  licensePlate: string;
  batteryCapacity: number;
  currentSoc: number;
  chargerType: 'CCS2' | 'CHAdeMO' | 'Type2';
}

interface Reservation {
  id: string;
  stationId: string;
  stationName: string;
  portId: string;
  portNumber: number;
  vehicleId: string;
  vehicleName: string;
  date: Date;
  startTime: Date;
  endTime: Date;
  duration: number;
  estimatedEnergy: number;
  estimatedCost: number;
  status: 'pending' | 'confirmed' | 'active' | 'completed' | 'cancelled' | 'no-show';
  reminderSet: boolean;
  qrCode?: string;
  createdAt: Date;
}

interface TimeSlot {
  start: Date;
  end: Date;
  available: boolean;
  reservation?: Reservation;
}

const SLOT_DURATION = 30; // minutes

const ChargerReservationSystem: React.FC = () => {
  // State
  const [activeTab, setActiveTab] = useState(0);
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [selectedStation, setSelectedStation] = useState<ChargingStation | null>(null);
  const [selectedPort, setSelectedPort] = useState<ChargingPort | null>(null);
  const [selectedVehicle, setSelectedVehicle] = useState<string>('');
  const [selectedStartTime, setSelectedStartTime] = useState<Date | null>(null);
  const [duration, setDuration] = useState<number>(60);
  const [reservations, setReservations] = useState<Reservation[]>([]);
  
  // Dialog states
  const [bookingDialogOpen, setBookingDialogOpen] = useState(false);
  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);
  const [selectedReservation, setSelectedReservation] = useState<Reservation | null>(null);
  const [activeStep, setActiveStep] = useState(0);
  const [expandedStation, setExpandedStation] = useState<string | null>(null);

  // Mock data
  const vehicles: Vehicle[] = useMemo(() => [
    { id: 'v1', name: 'Tata Nexon EV', licensePlate: 'MH01AB1234', batteryCapacity: 40.5, currentSoc: 25, chargerType: 'CCS2' },
    { id: 'v2', name: 'MG ZS EV', licensePlate: 'MH02CD5678', batteryCapacity: 50.3, currentSoc: 35, chargerType: 'CCS2' },
    { id: 'v3', name: 'Hyundai Kona', licensePlate: 'MH03EF9012', batteryCapacity: 64, currentSoc: 15, chargerType: 'CCS2' },
  ], []);

  const stations: ChargingStation[] = useMemo(() => [
    {
      id: 's1',
      name: 'Tata Power EZ Charge - BKC',
      address: 'BKC, Bandra Kurla Complex, Mumbai',
      distance: 2.5,
      pricePerKwh: 12.50,
      rating: 4.5,
      amenities: ['WiFi', 'Restroom', 'Café'],
      ports: [
        { id: 'p1-1', portNumber: 1, type: 'CCS2', powerKw: 60, status: 'available' },
        { id: 'p1-2', portNumber: 2, type: 'CCS2', powerKw: 60, status: 'occupied' },
        { id: 'p1-3', portNumber: 3, type: 'CHAdeMO', powerKw: 50, status: 'available' },
        { id: 'p1-4', portNumber: 4, type: 'Type2', powerKw: 22, status: 'maintenance' },
      ],
    },
    {
      id: 's2',
      name: 'ChargeZone Hub - Andheri',
      address: 'Andheri East, Mumbai',
      distance: 5.8,
      pricePerKwh: 10.00,
      rating: 4.2,
      amenities: ['WiFi', 'Parking'],
      ports: [
        { id: 'p2-1', portNumber: 1, type: 'CCS2', powerKw: 120, status: 'available' },
        { id: 'p2-2', portNumber: 2, type: 'CCS2', powerKw: 120, status: 'available' },
        { id: 'p2-3', portNumber: 3, type: 'Type2', powerKw: 22, status: 'reserved' },
      ],
    },
    {
      id: 's3',
      name: 'Jio-bp Pulse - Worli',
      address: 'Worli Sea Face, Mumbai',
      distance: 4.1,
      pricePerKwh: 14.00,
      rating: 4.0,
      amenities: ['WiFi', 'Restroom', 'Café', 'Lounge'],
      ports: [
        { id: 'p3-1', portNumber: 1, type: 'CCS2', powerKw: 150, status: 'available' },
        { id: 'p3-2', portNumber: 2, type: 'CCS2', powerKw: 150, status: 'occupied' },
        { id: 'p3-3', portNumber: 3, type: 'CHAdeMO', powerKw: 50, status: 'available' },
        { id: 'p3-4', portNumber: 4, type: 'CHAdeMO', powerKw: 50, status: 'available' },
      ],
    },
  ], []);

  // Generate mock reservations
  useMemo(() => {
    const mockReservations: Reservation[] = [
      {
        id: 'r1',
        stationId: 's1',
        stationName: 'Tata Power EZ Charge - BKC',
        portId: 'p1-1',
        portNumber: 1,
        vehicleId: 'v1',
        vehicleName: 'Tata Nexon EV - MH01AB1234',
        date: new Date(),
        startTime: setMinutes(setHours(new Date(), 14), 0),
        endTime: setMinutes(setHours(new Date(), 15), 30),
        duration: 90,
        estimatedEnergy: 30,
        estimatedCost: 375,
        status: 'confirmed',
        reminderSet: true,
        qrCode: 'RES-BKC-001-202501',
        createdAt: new Date(Date.now() - 3600000),
      },
      {
        id: 'r2',
        stationId: 's2',
        stationName: 'ChargeZone Hub - Andheri',
        portId: 'p2-1',
        portNumber: 1,
        vehicleId: 'v2',
        vehicleName: 'MG ZS EV - MH02CD5678',
        date: addDays(new Date(), 1),
        startTime: setMinutes(setHours(addDays(new Date(), 1), 10), 0),
        endTime: setMinutes(setHours(addDays(new Date(), 1), 11), 0),
        duration: 60,
        estimatedEnergy: 40,
        estimatedCost: 400,
        status: 'pending',
        reminderSet: false,
        qrCode: 'RES-AND-002-202501',
        createdAt: new Date(Date.now() - 7200000),
      },
    ];
    setReservations(mockReservations);
  }, []);

  // Generate time slots for a port on a given date
  const generateTimeSlots = useCallback((station: ChargingStation, port: ChargingPort, date: Date): TimeSlot[] => {
    const slots: TimeSlot[] = [];
    const startOfDay = setMinutes(setHours(date, 6), 0); // 6 AM
    const endOfDay = setMinutes(setHours(date, 22), 0); // 10 PM
    
    let current = startOfDay;
    while (isBefore(current, endOfDay)) {
      const end = addMinutes(current, SLOT_DURATION);
      const existingReservation = reservations.find(
        r => r.stationId === station.id && 
             r.portId === port.id && 
             isSameDay(r.date, date) &&
             ((isBefore(current, r.endTime) && isAfter(end, r.startTime)))
      );

      slots.push({
        start: current,
        end,
        available: !existingReservation && port.status === 'available',
        reservation: existingReservation,
      });
      
      current = end;
    }
    
    return slots;
  }, [reservations]);

  // Calculate estimated energy based on duration and power
  const calculateEstimatedEnergy = (durationMinutes: number, powerKw: number): number => {
    return Math.round((durationMinutes / 60) * powerKw * 0.9 * 10) / 10; // 90% efficiency
  };

  // Create new reservation
  const handleCreateReservation = () => {
    if (!selectedStation || !selectedPort || !selectedVehicle || !selectedStartTime) return;

    const vehicle = vehicles.find(v => v.id === selectedVehicle);
    const endTime = addMinutes(selectedStartTime, duration);
    const estimatedEnergy = calculateEstimatedEnergy(duration, selectedPort.powerKw);
    const estimatedCost = Math.round(estimatedEnergy * selectedStation.pricePerKwh * 100) / 100;

    const newReservation: Reservation = {
      id: `r-${Date.now()}`,
      stationId: selectedStation.id,
      stationName: selectedStation.name,
      portId: selectedPort.id,
      portNumber: selectedPort.portNumber,
      vehicleId: selectedVehicle,
      vehicleName: `${vehicle?.name} - ${vehicle?.licensePlate}`,
      date: selectedDate,
      startTime: selectedStartTime,
      endTime,
      duration,
      estimatedEnergy,
      estimatedCost,
      status: 'pending',
      reminderSet: false,
      qrCode: `RES-${selectedStation.id.toUpperCase()}-${Date.now().toString(36).toUpperCase()}`,
      createdAt: new Date(),
    };

    setReservations(prev => [...prev, newReservation]);
    setBookingDialogOpen(false);
    setConfirmDialogOpen(true);
    setSelectedReservation(newReservation);
    resetBookingForm();
  };

  // Cancel reservation
  const handleCancelReservation = (reservation: Reservation) => {
    setReservations(prev => 
      prev.map(r => r.id === reservation.id ? { ...r, status: 'cancelled' as const } : r)
    );
    setDetailsDialogOpen(false);
  };

  // Toggle reminder
  const handleToggleReminder = (reservationId: string) => {
    setReservations(prev =>
      prev.map(r => r.id === reservationId ? { ...r, reminderSet: !r.reminderSet } : r)
    );
  };

  // Reset booking form
  const resetBookingForm = () => {
    setSelectedStation(null);
    setSelectedPort(null);
    setSelectedVehicle('');
    setSelectedStartTime(null);
    setDuration(60);
    setActiveStep(0);
  };

  // Get status color
  const getStatusColor = (status: string): 'success' | 'warning' | 'error' | 'info' | 'default' => {
    switch (status) {
      case 'confirmed': return 'success';
      case 'pending': return 'warning';
      case 'active': return 'info';
      case 'completed': return 'success';
      case 'cancelled': return 'error';
      case 'no-show': return 'error';
      default: return 'default';
    }
  };

  // Get port status color
  const getPortStatusColor = (status: string): string => {
    switch (status) {
      case 'available': return '#4caf50';
      case 'occupied': return '#f44336';
      case 'reserved': return '#ff9800';
      case 'maintenance': return '#9e9e9e';
      default: return '#9e9e9e';
    }
  };

  // Filter reservations for display
  const upcomingReservations = reservations.filter(r => 
    ['pending', 'confirmed'].includes(r.status) && isAfter(r.startTime, new Date())
  ).sort((a, b) => a.startTime.getTime() - b.startTime.getTime());

  const pastReservations = reservations.filter(r =>
    ['completed', 'cancelled', 'no-show'].includes(r.status) || isBefore(r.endTime, new Date())
  ).sort((a, b) => b.startTime.getTime() - a.startTime.getTime());

  // Navigate date
  const handlePrevDate = () => setSelectedDate(prev => addDays(prev, -1));
  const handleNextDate = () => setSelectedDate(prev => addDays(prev, 1));

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Box sx={{ p: 3 }}>
        {/* Header */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Box>
            <Typography variant="h4" gutterBottom>
              <ScheduleIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Charger Reservations
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Book charging slots in advance to ensure availability
            </Typography>
          </Box>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setBookingDialogOpen(true)}
          >
            New Reservation
          </Button>
        </Box>

        {/* Tabs */}
        <Paper elevation={2} sx={{ mb: 3 }}>
          <Tabs value={activeTab} onChange={(_, v) => setActiveTab(v)}>
            <Tab label="Upcoming" icon={<Badge badgeContent={upcomingReservations.length} color="primary"><CalendarIcon /></Badge>} />
            <Tab label="Browse Stations" icon={<EvStationIcon />} />
            <Tab label="History" icon={<TimerIcon />} />
          </Tabs>
        </Paper>

        {/* Upcoming Reservations Tab */}
        {activeTab === 0 && (
          <Grid container spacing={3}>
            {upcomingReservations.length === 0 ? (
              <Grid item xs={12}>
                <Paper sx={{ p: 4, textAlign: 'center' }}>
                  <CalendarIcon sx={{ fontSize: 64, color: 'text.disabled', mb: 2 }} />
                  <Typography variant="h6" color="text.secondary" gutterBottom>
                    No Upcoming Reservations
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    Book a charging slot to ensure availability when you need it
                  </Typography>
                  <Button variant="contained" onClick={() => setBookingDialogOpen(true)}>
                    Make a Reservation
                  </Button>
                </Paper>
              </Grid>
            ) : (
              upcomingReservations.map((reservation) => (
                <Grid item xs={12} md={6} key={reservation.id}>
                  <Card elevation={2}>
                    <CardContent>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                        <Box>
                          <Typography variant="h6">{reservation.stationName}</Typography>
                          <Typography variant="body2" color="text.secondary">
                            Port {reservation.portNumber}
                          </Typography>
                        </Box>
                        <Chip
                          label={reservation.status}
                          color={getStatusColor(reservation.status)}
                          size="small"
                        />
                      </Box>

                      <Grid container spacing={2} sx={{ mb: 2 }}>
                        <Grid item xs={6}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <CalendarIcon fontSize="small" color="action" />
                            <Typography variant="body2">
                              {format(reservation.date, 'EEE, MMM d')}
                            </Typography>
                          </Box>
                        </Grid>
                        <Grid item xs={6}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <TimeIcon fontSize="small" color="action" />
                            <Typography variant="body2">
                              {format(reservation.startTime, 'h:mm a')} - {format(reservation.endTime, 'h:mm a')}
                            </Typography>
                          </Box>
                        </Grid>
                        <Grid item xs={6}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <CarIcon fontSize="small" color="action" />
                            <Typography variant="body2" noWrap>
                              {reservation.vehicleName.split(' - ')[0]}
                            </Typography>
                          </Box>
                        </Grid>
                        <Grid item xs={6}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <BatteryIcon fontSize="small" color="action" />
                            <Typography variant="body2">
                              ~{reservation.estimatedEnergy} kWh
                            </Typography>
                          </Box>
                        </Grid>
                      </Grid>

                      <Divider sx={{ my: 2 }} />

                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Box>
                          <Typography variant="body2" color="text.secondary">
                            Estimated Cost
                          </Typography>
                          <Typography variant="h6" color="primary">
                            ₹{reservation.estimatedCost}
                          </Typography>
                        </Box>
                        <Box sx={{ display: 'flex', gap: 1 }}>
                          <Tooltip title={reservation.reminderSet ? 'Reminder On' : 'Set Reminder'}>
                            <IconButton
                              size="small"
                              color={reservation.reminderSet ? 'primary' : 'default'}
                              onClick={() => handleToggleReminder(reservation.id)}
                            >
                              <AlarmIcon />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="View QR Code">
                            <IconButton size="small" color="primary">
                              <QrCodeIcon />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="Details">
                            <IconButton
                              size="small"
                              onClick={() => {
                                setSelectedReservation(reservation);
                                setDetailsDialogOpen(true);
                              }}
                            >
                              <ExpandMoreIcon />
                            </IconButton>
                          </Tooltip>
                        </Box>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>
              ))
            )}
          </Grid>
        )}

        {/* Browse Stations Tab */}
        {activeTab === 1 && (
          <Box>
            {/* Date Navigation */}
            <Paper sx={{ p: 2, mb: 3, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 2 }}>
              <IconButton onClick={handlePrevDate}>
                <PrevIcon />
              </IconButton>
              <Chip
                icon={<TodayIcon />}
                label={format(selectedDate, 'EEEE, MMMM d, yyyy')}
                color="primary"
                variant="outlined"
                onClick={() => setSelectedDate(new Date())}
              />
              <IconButton onClick={handleNextDate}>
                <NextIcon />
              </IconButton>
            </Paper>

            {/* Station List */}
            {stations.map((station) => (
              <Card key={station.id} sx={{ mb: 2 }}>
                <CardContent>
                  <Box
                    sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer' }}
                    onClick={() => setExpandedStation(expandedStation === station.id ? null : station.id)}
                  >
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                      <Avatar sx={{ bgcolor: 'primary.main' }}>
                        <EvStationIcon />
                      </Avatar>
                      <Box>
                        <Typography variant="h6">{station.name}</Typography>
                        <Typography variant="body2" color="text.secondary">
                          {station.address} • {station.distance} km
                        </Typography>
                      </Box>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                      <Box sx={{ display: 'flex', gap: 0.5 }}>
                        {station.ports.map((port) => (
                          <Tooltip key={port.id} title={`Port ${port.portNumber}: ${port.type} ${port.powerKw}kW - ${port.status}`}>
                            <Box
                              sx={{
                                width: 24,
                                height: 24,
                                borderRadius: 1,
                                bgcolor: getPortStatusColor(port.status),
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                              }}
                            >
                              <Typography variant="caption" color="white" fontWeight="bold">
                                {port.portNumber}
                              </Typography>
                            </Box>
                          </Tooltip>
                        ))}
                      </Box>
                      <Chip label={`₹${station.pricePerKwh}/kWh`} size="small" />
                      <Rating value={station.rating} precision={0.5} size="small" readOnly />
                      <IconButton>
                        {expandedStation === station.id ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                      </IconButton>
                    </Box>
                  </Box>

                  <Collapse in={expandedStation === station.id}>
                    <Divider sx={{ my: 2 }} />
                    
                    {/* Port availability timeline */}
                    {station.ports.filter(p => p.status !== 'maintenance').map((port) => {
                      const slots = generateTimeSlots(station, port, selectedDate);
                      
                      return (
                        <Box key={port.id} sx={{ mb: 2 }}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                            <Chip
                              size="small"
                              label={`Port ${port.portNumber}`}
                              sx={{ bgcolor: getPortStatusColor(port.status), color: 'white' }}
                            />
                            <Typography variant="body2">
                              {port.type} • {port.powerKw} kW
                            </Typography>
                          </Box>
                          
                          {/* Time slot grid */}
                          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                            {slots.map((slot, index) => (
                              <Tooltip
                                key={index}
                                title={
                                  slot.available
                                    ? `Available: ${format(slot.start, 'h:mm a')} - ${format(slot.end, 'h:mm a')}`
                                    : slot.reservation
                                    ? `Reserved: ${slot.reservation.vehicleName}`
                                    : 'Unavailable'
                                }
                              >
                                <Box
                                  sx={{
                                    width: 28,
                                    height: 28,
                                    borderRadius: 0.5,
                                    bgcolor: slot.available ? 'success.light' : slot.reservation ? 'warning.light' : 'grey.300',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    cursor: slot.available ? 'pointer' : 'default',
                                    '&:hover': slot.available ? { bgcolor: 'success.main' } : {},
                                    border: 1,
                                    borderColor: slot.available ? 'success.main' : 'transparent',
                                  }}
                                  onClick={() => {
                                    if (slot.available) {
                                      setSelectedStation(station);
                                      setSelectedPort(port);
                                      setSelectedStartTime(slot.start);
                                      setActiveStep(2);
                                      setBookingDialogOpen(true);
                                    }
                                  }}
                                >
                                  <Typography variant="caption" fontSize={9}>
                                    {format(slot.start, 'H')}
                                  </Typography>
                                </Box>
                              </Tooltip>
                            ))}
                          </Box>
                          
                          {/* Legend */}
                          <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                              <Box sx={{ width: 12, height: 12, borderRadius: 0.5, bgcolor: 'success.light', border: 1, borderColor: 'success.main' }} />
                              <Typography variant="caption">Available</Typography>
                            </Box>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                              <Box sx={{ width: 12, height: 12, borderRadius: 0.5, bgcolor: 'warning.light' }} />
                              <Typography variant="caption">Reserved</Typography>
                            </Box>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                              <Box sx={{ width: 12, height: 12, borderRadius: 0.5, bgcolor: 'grey.300' }} />
                              <Typography variant="caption">Occupied</Typography>
                            </Box>
                          </Box>
                        </Box>
                      );
                    })}

                    <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
                      {station.amenities.map((amenity) => (
                        <Chip key={amenity} label={amenity} size="small" variant="outlined" />
                      ))}
                    </Box>
                  </Collapse>
                </CardContent>
              </Card>
            ))}
          </Box>
        )}

        {/* History Tab */}
        {activeTab === 2 && (
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Date & Time</TableCell>
                  <TableCell>Station</TableCell>
                  <TableCell>Vehicle</TableCell>
                  <TableCell align="right">Duration</TableCell>
                  <TableCell align="right">Energy</TableCell>
                  <TableCell align="right">Cost</TableCell>
                  <TableCell align="center">Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {pastReservations.map((reservation) => (
                  <TableRow key={reservation.id}>
                    <TableCell>
                      {format(reservation.date, 'MMM d, yyyy')}
                      <Typography variant="caption" display="block" color="text.secondary">
                        {format(reservation.startTime, 'h:mm a')} - {format(reservation.endTime, 'h:mm a')}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      {reservation.stationName}
                      <Typography variant="caption" display="block" color="text.secondary">
                        Port {reservation.portNumber}
                      </Typography>
                    </TableCell>
                    <TableCell>{reservation.vehicleName}</TableCell>
                    <TableCell align="right">{reservation.duration} min</TableCell>
                    <TableCell align="right">{reservation.estimatedEnergy} kWh</TableCell>
                    <TableCell align="right">₹{reservation.estimatedCost}</TableCell>
                    <TableCell align="center">
                      <Chip
                        size="small"
                        label={reservation.status}
                        color={getStatusColor(reservation.status)}
                      />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}

        {/* Booking Dialog */}
        <Dialog open={bookingDialogOpen} onClose={() => setBookingDialogOpen(false)} maxWidth="md" fullWidth>
          <DialogTitle>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <ScheduleIcon />
              New Reservation
            </Box>
          </DialogTitle>
          <DialogContent>
            <Stepper activeStep={activeStep} orientation="vertical" sx={{ mt: 2 }}>
              {/* Step 1: Select Station */}
              <Step>
                <StepLabel>Select Charging Station</StepLabel>
                <StepContent>
                  <Grid container spacing={2}>
                    {stations.map((station) => (
                      <Grid item xs={12} key={station.id}>
                        <Paper
                          sx={{
                            p: 2,
                            cursor: 'pointer',
                            border: 2,
                            borderColor: selectedStation?.id === station.id ? 'primary.main' : 'transparent',
                            '&:hover': { borderColor: 'primary.light' },
                          }}
                          onClick={() => {
                            setSelectedStation(station);
                            setActiveStep(1);
                          }}
                        >
                          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <Box>
                              <Typography variant="subtitle1" fontWeight="medium">
                                {station.name}
                              </Typography>
                              <Typography variant="body2" color="text.secondary">
                                {station.address} • {station.distance} km
                              </Typography>
                            </Box>
                            <Box sx={{ textAlign: 'right' }}>
                              <Typography variant="h6" color="primary">
                                ₹{station.pricePerKwh}/kWh
                              </Typography>
                              <Rating value={station.rating} size="small" readOnly />
                            </Box>
                          </Box>
                        </Paper>
                      </Grid>
                    ))}
                  </Grid>
                </StepContent>
              </Step>

              {/* Step 2: Select Port */}
              <Step>
                <StepLabel>Select Charging Port</StepLabel>
                <StepContent>
                  {selectedStation && (
                    <Grid container spacing={2}>
                      {selectedStation.ports
                        .filter((p) => p.status === 'available')
                        .map((port) => (
                          <Grid item xs={6} md={4} key={port.id}>
                            <Paper
                              sx={{
                                p: 2,
                                cursor: 'pointer',
                                border: 2,
                                borderColor: selectedPort?.id === port.id ? 'primary.main' : 'transparent',
                                '&:hover': { borderColor: 'primary.light' },
                                textAlign: 'center',
                              }}
                              onClick={() => {
                                setSelectedPort(port);
                                setActiveStep(2);
                              }}
                            >
                              <SpeedIcon sx={{ fontSize: 40, color: 'primary.main', mb: 1 }} />
                              <Typography variant="h6">Port {port.portNumber}</Typography>
                              <Chip label={port.type} size="small" />
                              <Typography variant="body2" sx={{ mt: 1 }}>
                                {port.powerKw} kW
                              </Typography>
                            </Paper>
                          </Grid>
                        ))}
                    </Grid>
                  )}
                  <Button onClick={() => setActiveStep(0)} sx={{ mt: 2 }}>
                    Back
                  </Button>
                </StepContent>
              </Step>

              {/* Step 3: Select Time and Vehicle */}
              <Step>
                <StepLabel>Select Time and Vehicle</StepLabel>
                <StepContent>
                  <Grid container spacing={2}>
                    <Grid item xs={12} md={6}>
                      <DatePicker
                        label="Date"
                        value={selectedDate}
                        onChange={(date) => date && setSelectedDate(date)}
                        minDate={new Date()}
                        slotProps={{ textField: { fullWidth: true } }}
                      />
                    </Grid>
                    <Grid item xs={12} md={6}>
                      <TimePicker
                        label="Start Time"
                        value={selectedStartTime}
                        onChange={setSelectedStartTime}
                        slotProps={{ textField: { fullWidth: true } }}
                      />
                    </Grid>
                    <Grid item xs={12}>
                      <Typography gutterBottom>Duration: {duration} minutes</Typography>
                      <Slider
                        value={duration}
                        onChange={(_, v) => setDuration(v as number)}
                        min={30}
                        max={180}
                        step={30}
                        marks={[
                          { value: 30, label: '30m' },
                          { value: 60, label: '1h' },
                          { value: 90, label: '1.5h' },
                          { value: 120, label: '2h' },
                          { value: 180, label: '3h' },
                        ]}
                      />
                    </Grid>
                    <Grid item xs={12}>
                      <FormControl fullWidth>
                        <InputLabel>Select Vehicle</InputLabel>
                        <Select
                          value={selectedVehicle}
                          onChange={(e) => setSelectedVehicle(e.target.value)}
                          label="Select Vehicle"
                        >
                          {vehicles.map((vehicle) => (
                            <MenuItem key={vehicle.id} value={vehicle.id}>
                              {vehicle.name} - {vehicle.licensePlate} ({vehicle.currentSoc}% SOC)
                            </MenuItem>
                          ))}
                        </Select>
                      </FormControl>
                    </Grid>
                  </Grid>

                  {/* Estimate */}
                  {selectedPort && selectedVehicle && (
                    <Paper sx={{ p: 2, mt: 2, bgcolor: 'grey.50' }}>
                      <Typography variant="subtitle2" gutterBottom>
                        Estimated Charging
                      </Typography>
                      <Grid container spacing={2}>
                        <Grid item xs={4}>
                          <Typography variant="body2" color="text.secondary">Energy</Typography>
                          <Typography variant="h6">
                            {calculateEstimatedEnergy(duration, selectedPort.powerKw)} kWh
                          </Typography>
                        </Grid>
                        <Grid item xs={4}>
                          <Typography variant="body2" color="text.secondary">Cost</Typography>
                          <Typography variant="h6" color="primary">
                            ₹{Math.round(calculateEstimatedEnergy(duration, selectedPort.powerKw) * selectedStation!.pricePerKwh * 100) / 100}
                          </Typography>
                        </Grid>
                        <Grid item xs={4}>
                          <Typography variant="body2" color="text.secondary">End Time</Typography>
                          <Typography variant="h6">
                            {selectedStartTime ? format(addMinutes(selectedStartTime, duration), 'h:mm a') : '-'}
                          </Typography>
                        </Grid>
                      </Grid>
                    </Paper>
                  )}

                  <Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
                    <Button onClick={() => setActiveStep(1)}>Back</Button>
                  </Box>
                </StepContent>
              </Step>
            </Stepper>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setBookingDialogOpen(false)}>Cancel</Button>
            <Button
              variant="contained"
              onClick={handleCreateReservation}
              disabled={!selectedStation || !selectedPort || !selectedVehicle || !selectedStartTime}
            >
              Confirm Reservation
            </Button>
          </DialogActions>
        </Dialog>

        {/* Confirmation Dialog */}
        <Dialog open={confirmDialogOpen} onClose={() => setConfirmDialogOpen(false)} maxWidth="sm" fullWidth>
          <DialogTitle sx={{ textAlign: 'center' }}>
            <CheckCircleIcon sx={{ fontSize: 64, color: 'success.main', mb: 1 }} />
            <Typography variant="h5">Reservation Confirmed!</Typography>
          </DialogTitle>
          <DialogContent>
            {selectedReservation && (
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="body1" gutterBottom>
                  Your charging slot has been reserved at:
                </Typography>
                <Typography variant="h6" color="primary" gutterBottom>
                  {selectedReservation.stationName}
                </Typography>
                <Chip label={`Port ${selectedReservation.portNumber}`} sx={{ mb: 2 }} />
                
                <Paper sx={{ p: 2, bgcolor: 'grey.50', my: 2 }}>
                  <Grid container spacing={2}>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">Date</Typography>
                      <Typography variant="body1">{format(selectedReservation.date, 'EEEE, MMM d')}</Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="text.secondary">Time</Typography>
                      <Typography variant="body1">
                        {format(selectedReservation.startTime, 'h:mm a')} - {format(selectedReservation.endTime, 'h:mm a')}
                      </Typography>
                    </Grid>
                  </Grid>
                </Paper>

                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Reservation Code
                </Typography>
                <Chip
                  icon={<QrCodeIcon />}
                  label={selectedReservation.qrCode}
                  color="primary"
                  sx={{ fontSize: '1rem', py: 2 }}
                />
              </Box>
            )}
          </DialogContent>
          <DialogActions sx={{ justifyContent: 'center' }}>
            <Button onClick={() => setConfirmDialogOpen(false)} variant="contained">
              Done
            </Button>
          </DialogActions>
        </Dialog>

        {/* Details Dialog */}
        <Dialog open={detailsDialogOpen} onClose={() => setDetailsDialogOpen(false)} maxWidth="sm" fullWidth>
          <DialogTitle>Reservation Details</DialogTitle>
          <DialogContent>
            {selectedReservation && (
              <Box>
                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <Typography variant="h6">{selectedReservation.stationName}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      Port {selectedReservation.portNumber}
                    </Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">Date</Typography>
                    <Typography variant="body1">{format(selectedReservation.date, 'EEEE, MMMM d, yyyy')}</Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">Time</Typography>
                    <Typography variant="body1">
                      {format(selectedReservation.startTime, 'h:mm a')} - {format(selectedReservation.endTime, 'h:mm a')}
                    </Typography>
                  </Grid>
                  <Grid item xs={12}>
                    <Divider />
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">Vehicle</Typography>
                    <Typography variant="body1">{selectedReservation.vehicleName}</Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">Duration</Typography>
                    <Typography variant="body1">{selectedReservation.duration} minutes</Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">Estimated Energy</Typography>
                    <Typography variant="body1">{selectedReservation.estimatedEnergy} kWh</Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">Estimated Cost</Typography>
                    <Typography variant="body1" color="primary" fontWeight="bold">
                      ₹{selectedReservation.estimatedCost}
                    </Typography>
                  </Grid>
                  <Grid item xs={12}>
                    <Divider />
                  </Grid>
                  <Grid item xs={12}>
                    <Typography variant="body2" color="text.secondary">Reservation Code</Typography>
                    <Chip icon={<QrCodeIcon />} label={selectedReservation.qrCode} />
                  </Grid>
                </Grid>
              </Box>
            )}
          </DialogContent>
          <DialogActions>
            {selectedReservation?.status !== 'cancelled' && (
              <Button
                color="error"
                onClick={() => selectedReservation && handleCancelReservation(selectedReservation)}
              >
                Cancel Reservation
              </Button>
            )}
            <Button onClick={() => setDetailsDialogOpen(false)}>Close</Button>
          </DialogActions>
        </Dialog>
      </Box>
    </LocalizationProvider>
  );
};

export default ChargerReservationSystem;

import React, { useEffect, useState, useMemo, useCallback } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  IconButton,
  Tooltip,
  Chip,
  Alert,
  Avatar,
  AvatarGroup,
  ToggleButton,
  ToggleButtonGroup,
  Autocomplete,
  useTheme,
  alpha,
  CircularProgress,
} from '@mui/material';
import {
  Add,
  Close,
  CalendarViewWeek,
  CalendarViewMonth,
  ChevronLeft,
  ChevronRight,
  Today,
  Warning,
  DirectionsCar,
  Person,
  Refresh,
} from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '@redux/hooks';
import { fetchDrivers } from '@redux/slices/driverSlice';
import { fetchVehicles } from '@redux/slices/vehicleSlice';
import { formatDate } from '@utils/formatters';

/**
 * Assignment Calendar Component
 * 
 * Calendar view for managing driver-vehicle assignments:
 * - Week/month view toggle
 * - Color-coded by vehicle type
 * - Drag-and-drop reassignment (simulated)
 * - Conflict detection
 * - Quick-add assignment modal
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */

interface Assignment {
  id: string;
  driverId: string;
  driverName: string;
  vehicleId: string;
  vehicleNumber: string;
  vehicleType: string;
  startDate: string;
  endDate: string;
  shiftType: 'morning' | 'afternoon' | 'night' | 'full';
  status: 'active' | 'upcoming' | 'completed';
}

interface CalendarDay {
  date: Date;
  isCurrentMonth: boolean;
  isToday: boolean;
  assignments: Assignment[];
  hasConflict: boolean;
}

// Vehicle type colors
const VEHICLE_TYPE_COLORS: Record<string, string> = {
  SEDAN: '#4CAF50',
  SUV: '#2196F3',
  TRUCK: '#FF9800',
  VAN: '#9C27B0',
  BUS: '#F44336',
  TWO_WHEELER: '#00BCD4',
  THREE_WHEELER: '#795548',
  DEFAULT: '#607D8B',
};

// Shift type labels
const SHIFT_LABELS: Record<string, { label: string; color: string }> = {
  morning: { label: '🌅 Morning', color: '#FFC107' },
  afternoon: { label: '☀️ Afternoon', color: '#FF9800' },
  night: { label: '🌙 Night', color: '#3F51B5' },
  full: { label: '📅 Full Day', color: '#4CAF50' },
};

// Generate mock assignments
const generateMockAssignments = (drivers: any[], vehicles: any[]): Assignment[] => {
  if (!drivers.length || !vehicles.length) return [];
  
  const assignments: Assignment[] = [];
  const today = new Date();
  
  for (let i = 0; i < Math.min(15, drivers.length * 2); i++) {
    const driver = drivers[i % drivers.length];
    const vehicle = vehicles[i % vehicles.length];
    const startOffset = Math.floor(Math.random() * 14) - 7;
    const duration = Math.floor(Math.random() * 3) + 1;
    
    const startDate = new Date(today);
    startDate.setDate(today.getDate() + startOffset);
    
    const endDate = new Date(startDate);
    endDate.setDate(startDate.getDate() + duration);

    assignments.push({
      id: `assignment-${i}`,
      driverId: driver.id?.toString() || `driver-${i}`,
      driverName: `${driver.firstName || 'Driver'} ${driver.lastName || i}`,
      vehicleId: vehicle.id?.toString() || `vehicle-${i}`,
      vehicleNumber: vehicle.vehicleNumber || `MH-12-AB-${1000 + i}`,
      vehicleType: vehicle.type || 'SEDAN',
      startDate: startDate.toISOString().split('T')[0],
      endDate: endDate.toISOString().split('T')[0],
      shiftType: ['morning', 'afternoon', 'night', 'full'][Math.floor(Math.random() * 4)] as Assignment['shiftType'],
      status: startOffset < 0 ? 'completed' : startOffset === 0 ? 'active' : 'upcoming',
    });
  }
  
  return assignments;
};

const AssignmentCalendar: React.FC = () => {
  const theme = useTheme();
  const dispatch = useAppDispatch();
  
  const { drivers } = useAppSelector((state) => state.drivers);
  const { vehicles } = useAppSelector((state) => state.vehicles);
  
  const [viewMode, setViewMode] = useState<'week' | 'month'>('month');
  const [currentDate, setCurrentDate] = useState(new Date());
  const [assignments, setAssignments] = useState<Assignment[]>([]);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [loading, setLoading] = useState(false);
  
  // Form state
  const [formData, setFormData] = useState({
    driverId: '',
    vehicleId: '',
    startDate: '',
    endDate: '',
    shiftType: 'full' as Assignment['shiftType'],
  });

  useEffect(() => {
    dispatch(fetchDrivers());
    dispatch(fetchVehicles());
  }, [dispatch]);

  useEffect(() => {
    // Generate mock assignments when data loads
    setAssignments(generateMockAssignments(drivers, vehicles));
  }, [drivers, vehicles]);

  // Generate calendar days
  const calendarDays = useMemo((): CalendarDay[] => {
    const days: CalendarDay[] = [];
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (viewMode === 'month') {
      const year = currentDate.getFullYear();
      const month = currentDate.getMonth();
      
      // Get first day of month and calculate offset
      const firstDay = new Date(year, month, 1);
      const startOffset = firstDay.getDay();
      
      // Get last day of month
      const lastDay = new Date(year, month + 1, 0);
      const totalDays = lastDay.getDate();
      
      // Add previous month days
      for (let i = startOffset - 1; i >= 0; i--) {
        const date = new Date(year, month, -i);
        days.push(createCalendarDay(date, false, today, assignments));
      }
      
      // Add current month days
      for (let i = 1; i <= totalDays; i++) {
        const date = new Date(year, month, i);
        days.push(createCalendarDay(date, true, today, assignments));
      }
      
      // Add next month days to complete grid
      const remaining = 42 - days.length;
      for (let i = 1; i <= remaining; i++) {
        const date = new Date(year, month + 1, i);
        days.push(createCalendarDay(date, false, today, assignments));
      }
    } else {
      // Week view
      const startOfWeek = new Date(currentDate);
      startOfWeek.setDate(currentDate.getDate() - currentDate.getDay());
      
      for (let i = 0; i < 7; i++) {
        const date = new Date(startOfWeek);
        date.setDate(startOfWeek.getDate() + i);
        days.push(createCalendarDay(date, true, today, assignments));
      }
    }

    return days;
  }, [currentDate, viewMode, assignments]);

  // Helper to create calendar day object
  const createCalendarDay = (
    date: Date,
    isCurrentMonth: boolean,
    today: Date,
    allAssignments: Assignment[]
  ): CalendarDay => {
    const dateStr = date.toISOString().split('T')[0];
    const dayAssignments = allAssignments.filter((a) => {
      const start = new Date(a.startDate);
      const end = new Date(a.endDate);
      return date >= start && date <= end;
    });

    // Check for conflicts (multiple assignments for same vehicle or driver)
    const hasConflict =
      dayAssignments.length > 1 &&
      (new Set(dayAssignments.map((a) => a.vehicleId)).size !== dayAssignments.length ||
        new Set(dayAssignments.map((a) => a.driverId)).size !== dayAssignments.length);

    return {
      date,
      isCurrentMonth,
      isToday: date.getTime() === today.getTime(),
      assignments: dayAssignments,
      hasConflict,
    };
  };

  // Navigation
  const navigatePrevious = () => {
    const newDate = new Date(currentDate);
    if (viewMode === 'month') {
      newDate.setMonth(newDate.getMonth() - 1);
    } else {
      newDate.setDate(newDate.getDate() - 7);
    }
    setCurrentDate(newDate);
  };

  const navigateNext = () => {
    const newDate = new Date(currentDate);
    if (viewMode === 'month') {
      newDate.setMonth(newDate.getMonth() + 1);
    } else {
      newDate.setDate(newDate.getDate() + 7);
    }
    setCurrentDate(newDate);
  };

  const goToToday = () => {
    setCurrentDate(new Date());
  };

  // Handle day click
  const handleDayClick = (day: CalendarDay) => {
    setSelectedDate(day.date);
    setFormData({
      ...formData,
      startDate: day.date.toISOString().split('T')[0],
      endDate: day.date.toISOString().split('T')[0],
    });
    setDialogOpen(true);
  };

  // Handle form submit
  const handleSubmit = async () => {
    setLoading(true);
    
    // Simulate API call
    await new Promise((resolve) => setTimeout(resolve, 1000));
    
    const driver = drivers.find((d) => d.id?.toString() === formData.driverId);
    const vehicle = vehicles.find((v) => v.id?.toString() === formData.vehicleId);
    
    if (driver && vehicle) {
      const newAssignment: Assignment = {
        id: `assignment-${Date.now()}`,
        driverId: formData.driverId,
        driverName: `${driver.firstName} ${driver.lastName}`,
        vehicleId: formData.vehicleId,
        vehicleNumber: vehicle.vehicleNumber,
        vehicleType: vehicle.type || 'SEDAN',
        startDate: formData.startDate,
        endDate: formData.endDate,
        shiftType: formData.shiftType,
        status: 'upcoming',
      };
      
      setAssignments([...assignments, newAssignment]);
    }
    
    setLoading(false);
    setDialogOpen(false);
    setFormData({
      driverId: '',
      vehicleId: '',
      startDate: '',
      endDate: '',
      shiftType: 'full',
    });
  };

  // Get month/year label
  const getHeaderLabel = () => {
    if (viewMode === 'month') {
      return currentDate.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
    } else {
      const startOfWeek = new Date(currentDate);
      startOfWeek.setDate(currentDate.getDate() - currentDate.getDay());
      const endOfWeek = new Date(startOfWeek);
      endOfWeek.setDate(startOfWeek.getDate() + 6);
      
      return `${startOfWeek.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} - ${endOfWeek.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}`;
    }
  };

  const weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  return (
    <Box>
      {/* Header */}
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box display="flex" alignItems="center" gap={2}>
            <Typography variant="h5" fontWeight={600}>
              Assignment Calendar
            </Typography>
            <ToggleButtonGroup
              value={viewMode}
              exclusive
              onChange={(_, value) => value && setViewMode(value)}
              size="small"
            >
              <ToggleButton value="week">
                <Tooltip title="Week View">
                  <CalendarViewWeek />
                </Tooltip>
              </ToggleButton>
              <ToggleButton value="month">
                <Tooltip title="Month View">
                  <CalendarViewMonth />
                </Tooltip>
              </ToggleButton>
            </ToggleButtonGroup>
          </Box>

          <Box display="flex" alignItems="center" gap={1}>
            <IconButton onClick={navigatePrevious}>
              <ChevronLeft />
            </IconButton>
            <Button variant="outlined" onClick={goToToday} startIcon={<Today />}>
              Today
            </Button>
            <Typography variant="h6" sx={{ minWidth: 200, textAlign: 'center' }}>
              {getHeaderLabel()}
            </Typography>
            <IconButton onClick={navigateNext}>
              <ChevronRight />
            </IconButton>
          </Box>

          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => {
              setSelectedDate(new Date());
              setFormData({
                ...formData,
                startDate: new Date().toISOString().split('T')[0],
                endDate: new Date().toISOString().split('T')[0],
              });
              setDialogOpen(true);
            }}
          >
            Add Assignment
          </Button>
        </Box>
      </Paper>

      {/* Legend */}
      <Box display="flex" gap={2} mb={2} flexWrap="wrap">
        {Object.entries(VEHICLE_TYPE_COLORS).filter(([k]) => k !== 'DEFAULT').map(([type, color]) => (
          <Chip
            key={type}
            label={type.replace('_', ' ')}
            size="small"
            sx={{
              bgcolor: alpha(color, 0.2),
              color: color,
              borderColor: color,
              borderWidth: 1,
              borderStyle: 'solid',
            }}
          />
        ))}
      </Box>

      {/* Calendar Grid */}
      <Paper sx={{ overflow: 'hidden' }}>
        {/* Week day headers */}
        <Box
          display="grid"
          gridTemplateColumns="repeat(7, 1fr)"
          sx={{ bgcolor: 'grey.100', borderBottom: 1, borderColor: 'divider' }}
        >
          {weekDays.map((day) => (
            <Box
              key={day}
              py={1.5}
              textAlign="center"
              fontWeight={600}
              color="text.secondary"
            >
              {day}
            </Box>
          ))}
        </Box>

        {/* Calendar days */}
        <Box
          display="grid"
          gridTemplateColumns="repeat(7, 1fr)"
          sx={{
            '& > div': {
              minHeight: viewMode === 'week' ? 300 : 120,
              borderRight: 1,
              borderBottom: 1,
              borderColor: 'divider',
              '&:nth-of-type(7n)': { borderRight: 0 },
            },
          }}
        >
          {calendarDays.map((day, index) => (
            <Box
              key={index}
              sx={{
                p: 1,
                cursor: 'pointer',
                bgcolor: day.isToday
                  ? alpha(theme.palette.primary.main, 0.08)
                  : day.isCurrentMonth
                  ? 'background.paper'
                  : 'grey.50',
                opacity: day.isCurrentMonth ? 1 : 0.6,
                transition: 'all 0.2s ease',
                '&:hover': {
                  bgcolor: alpha(theme.palette.primary.main, 0.12),
                },
              }}
              onClick={() => handleDayClick(day)}
            >
              {/* Day number */}
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={0.5}>
                <Typography
                  variant="body2"
                  fontWeight={day.isToday ? 700 : 400}
                  sx={{
                    color: day.isToday ? 'primary.main' : 'text.primary',
                    bgcolor: day.isToday ? alpha(theme.palette.primary.main, 0.2) : 'transparent',
                    borderRadius: '50%',
                    width: 28,
                    height: 28,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  {day.date.getDate()}
                </Typography>
                {day.hasConflict && (
                  <Tooltip title="Scheduling conflict detected">
                    <Warning fontSize="small" color="error" />
                  </Tooltip>
                )}
              </Box>

              {/* Assignments */}
              <Box display="flex" flexDirection="column" gap={0.5}>
                {day.assignments.slice(0, viewMode === 'week' ? 10 : 3).map((assignment) => (
                  <Tooltip
                    key={assignment.id}
                    title={
                      <Box>
                        <Typography variant="subtitle2">{assignment.driverName}</Typography>
                        <Typography variant="body2">{assignment.vehicleNumber}</Typography>
                        <Typography variant="caption">
                          {SHIFT_LABELS[assignment.shiftType].label}
                        </Typography>
                      </Box>
                    }
                  >
                    <Chip
                      size="small"
                      label={
                        <Box display="flex" alignItems="center" gap={0.5}>
                          <Person sx={{ fontSize: 12 }} />
                          <span style={{ fontSize: 11, maxWidth: 60, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                            {assignment.driverName.split(' ')[0]}
                          </span>
                        </Box>
                      }
                      sx={{
                        height: 20,
                        bgcolor: alpha(
                          VEHICLE_TYPE_COLORS[assignment.vehicleType] || VEHICLE_TYPE_COLORS.DEFAULT,
                          0.2
                        ),
                        color: VEHICLE_TYPE_COLORS[assignment.vehicleType] || VEHICLE_TYPE_COLORS.DEFAULT,
                        borderLeft: `3px solid ${VEHICLE_TYPE_COLORS[assignment.vehicleType] || VEHICLE_TYPE_COLORS.DEFAULT}`,
                        borderRadius: 1,
                        '& .MuiChip-label': { px: 1 },
                      }}
                    />
                  </Tooltip>
                ))}
                {day.assignments.length > (viewMode === 'week' ? 10 : 3) && (
                  <Typography variant="caption" color="text.secondary" textAlign="center">
                    +{day.assignments.length - (viewMode === 'week' ? 10 : 3)} more
                  </Typography>
                )}
              </Box>
            </Box>
          ))}
        </Box>
      </Paper>

      {/* Quick Add Dialog */}
      <Dialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          Add Assignment
          <IconButton
            onClick={() => setDialogOpen(false)}
            sx={{ position: 'absolute', right: 8, top: 8 }}
          >
            <Close />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <FormControl fullWidth>
              <Autocomplete
                options={drivers}
                getOptionLabel={(option: any) => `${option.firstName} ${option.lastName}`}
                value={drivers.find((d) => d.id?.toString() === formData.driverId) || null}
                onChange={(_, value) =>
                  setFormData({ ...formData, driverId: value?.id?.toString() || '' })
                }
                renderInput={(params) => (
                  <TextField {...params} label="Driver" required />
                )}
                renderOption={(props, option: any) => (
                  <li {...props}>
                    <Box display="flex" alignItems="center" gap={1}>
                      <Avatar sx={{ width: 32, height: 32 }}>
                        {option.firstName?.[0]}{option.lastName?.[0]}
                      </Avatar>
                      <Box>
                        <Typography variant="body2">
                          {option.firstName} {option.lastName}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {option.licenseNumber}
                        </Typography>
                      </Box>
                    </Box>
                  </li>
                )}
              />
            </FormControl>

            <FormControl fullWidth>
              <Autocomplete
                options={vehicles}
                getOptionLabel={(option: any) => `${option.vehicleNumber} - ${option.make} ${option.model}`}
                value={vehicles.find((v) => v.id?.toString() === formData.vehicleId) || null}
                onChange={(_, value) =>
                  setFormData({ ...formData, vehicleId: value?.id?.toString() || '' })
                }
                renderInput={(params) => (
                  <TextField {...params} label="Vehicle" required />
                )}
                renderOption={(props, option: any) => (
                  <li {...props}>
                    <Box display="flex" alignItems="center" gap={1}>
                      <DirectionsCar
                        sx={{
                          color: VEHICLE_TYPE_COLORS[option.type] || VEHICLE_TYPE_COLORS.DEFAULT,
                        }}
                      />
                      <Box>
                        <Typography variant="body2">{option.vehicleNumber}</Typography>
                        <Typography variant="caption" color="text.secondary">
                          {option.make} {option.model} • {option.type}
                        </Typography>
                      </Box>
                    </Box>
                  </li>
                )}
              />
            </FormControl>

            <Box display="flex" gap={2}>
              <TextField
                fullWidth
                label="Start Date"
                type="date"
                value={formData.startDate}
                onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                InputLabelProps={{ shrink: true }}
                required
              />
              <TextField
                fullWidth
                label="End Date"
                type="date"
                value={formData.endDate}
                onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                InputLabelProps={{ shrink: true }}
                required
              />
            </Box>

            <FormControl fullWidth>
              <InputLabel>Shift Type</InputLabel>
              <Select
                value={formData.shiftType}
                label="Shift Type"
                onChange={(e) =>
                  setFormData({ ...formData, shiftType: e.target.value as Assignment['shiftType'] })
                }
              >
                {Object.entries(SHIFT_LABELS).map(([key, { label }]) => (
                  <MenuItem key={key} value={key}>
                    {label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            {/* Conflict warning */}
            {formData.driverId && formData.vehicleId && formData.startDate && (
              <Alert severity="info">
                Check for scheduling conflicts before assigning
              </Alert>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleSubmit}
            disabled={
              loading ||
              !formData.driverId ||
              !formData.vehicleId ||
              !formData.startDate ||
              !formData.endDate
            }
            startIcon={loading ? <CircularProgress size={16} /> : <Add />}
          >
            {loading ? 'Adding...' : 'Add Assignment'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AssignmentCalendar;

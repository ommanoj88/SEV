import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Button,
  IconButton,
  Tooltip,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  SelectChangeEvent,
  Avatar,
  Badge,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemSecondaryAction,
  Divider,
  LinearProgress,
  Switch,
  FormControlLabel,
  Card,
  CardContent,
  useTheme,
  alpha,
  Fab,
  Menu,
  Popover,
  ToggleButton,
  ToggleButtonGroup,
} from '@mui/material';
import {
  Build as MaintenanceIcon,
  Warning as WarningIcon,
  Error as ErrorIcon,
  CheckCircle as CheckIcon,
  Schedule as ScheduleIcon,
  Today as TodayIcon,
  ChevronLeft as ChevronLeftIcon,
  ChevronRight as ChevronRightIcon,
  Add as AddIcon,
  DirectionsCar as VehicleIcon,
  Person as DriverIcon,
  AttachMoney as MoneyIcon,
  Timer as TimerIcon,
  Close as CloseIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  DragIndicator as DragIcon,
  FilterList as FilterIcon,
  CalendarMonth as MonthIcon,
  ViewWeek as WeekIcon,
  ViewDay as DayIcon,
  Notifications as AlertIcon,
  Refresh as RefreshIcon,
  LocalOffer as TagIcon,
} from '@mui/icons-material';
import { DatePicker, LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import {
  format,
  startOfMonth,
  endOfMonth,
  startOfWeek,
  endOfWeek,
  addDays,
  addMonths,
  subMonths,
  isSameMonth,
  isSameDay,
  isToday,
  isPast,
  isFuture,
  differenceInDays,
} from 'date-fns';

// Types
interface MaintenanceEvent {
  id: string;
  title: string;
  description: string;
  date: Date;
  duration: number; // hours
  type: 'routine' | 'emergency' | 'inspection' | 'recall';
  status: 'scheduled' | 'in-progress' | 'completed' | 'overdue';
  priority: 'high' | 'medium' | 'low';
  vehicle: {
    id: string;
    name: string;
    licensePlate: string;
  };
  assignedTo?: string;
  cost?: number;
  notes?: string;
}

type ViewMode = 'month' | 'week' | 'day';

// Mock data
const generateMockEvents = (): MaintenanceEvent[] => {
  const today = new Date();
  return [
    {
      id: 'm-001',
      title: 'Oil Change & Filter',
      description: 'Routine oil change and filter replacement',
      date: addDays(today, -2),
      duration: 1,
      type: 'routine',
      status: 'overdue',
      priority: 'high',
      vehicle: { id: 'v-001', name: 'Tesla Model 3', licensePlate: 'EV-1234' },
      assignedTo: 'John Tech',
      cost: 85,
    },
    {
      id: 'm-002',
      title: 'Brake Inspection',
      description: 'Annual brake system inspection',
      date: today,
      duration: 2,
      type: 'inspection',
      status: 'scheduled',
      priority: 'medium',
      vehicle: { id: 'v-002', name: 'Rivian R1T', licensePlate: 'EV-5678' },
      assignedTo: 'Mike Smith',
      cost: 150,
    },
    {
      id: 'm-003',
      title: 'Battery Health Check',
      description: 'Comprehensive battery diagnostic',
      date: addDays(today, 1),
      duration: 3,
      type: 'routine',
      status: 'scheduled',
      priority: 'medium',
      vehicle: { id: 'v-003', name: 'Ford E-Transit', licensePlate: 'EV-9012' },
      cost: 200,
    },
    {
      id: 'm-004',
      title: 'Emergency Brake Fix',
      description: 'Urgent brake caliper replacement',
      date: addDays(today, 2),
      duration: 4,
      type: 'emergency',
      status: 'scheduled',
      priority: 'high',
      vehicle: { id: 'v-004', name: 'Chevy Bolt', licensePlate: 'EV-3456' },
      assignedTo: 'Sarah Engineer',
      cost: 450,
    },
    {
      id: 'm-005',
      title: 'Tire Rotation',
      description: 'Quarterly tire rotation and balance',
      date: addDays(today, 5),
      duration: 1,
      type: 'routine',
      status: 'scheduled',
      priority: 'low',
      vehicle: { id: 'v-005', name: 'Tesla Model Y', licensePlate: 'EV-7890' },
      cost: 60,
    },
    {
      id: 'm-006',
      title: 'Software Update Recall',
      description: 'Factory recall for software update',
      date: addDays(today, 7),
      duration: 2,
      type: 'recall',
      status: 'scheduled',
      priority: 'medium',
      vehicle: { id: 'v-001', name: 'Tesla Model 3', licensePlate: 'EV-1234' },
      cost: 0,
    },
    {
      id: 'm-007',
      title: 'AC System Service',
      description: 'Annual AC system check and recharge',
      date: addDays(today, 10),
      duration: 2,
      type: 'routine',
      status: 'scheduled',
      priority: 'low',
      vehicle: { id: 'v-002', name: 'Rivian R1T', licensePlate: 'EV-5678' },
      cost: 120,
    },
    {
      id: 'm-008',
      title: 'State Inspection',
      description: 'Annual state safety inspection',
      date: addDays(today, 14),
      duration: 1,
      type: 'inspection',
      status: 'scheduled',
      priority: 'high',
      vehicle: { id: 'v-003', name: 'Ford E-Transit', licensePlate: 'EV-9012' },
      cost: 35,
    },
  ];
};

// Helper functions
const getEventColor = (event: MaintenanceEvent, theme: any): string => {
  if (event.status === 'overdue') return theme.palette.error.main;
  if (event.status === 'completed') return theme.palette.success.main;
  
  switch (event.type) {
    case 'emergency':
      return theme.palette.error.main;
    case 'recall':
      return theme.palette.warning.main;
    case 'inspection':
      return theme.palette.info.main;
    case 'routine':
    default:
      return theme.palette.primary.main;
  }
};

const getPriorityColor = (priority: string): 'error' | 'warning' | 'success' => {
  switch (priority) {
    case 'high': return 'error';
    case 'medium': return 'warning';
    default: return 'success';
  }
};

// Components
interface DayViewProps {
  date: Date;
  events: MaintenanceEvent[];
  onEventClick: (event: MaintenanceEvent) => void;
  onDrop?: (eventId: string, date: Date) => void;
}

const DayCell: React.FC<DayViewProps> = ({ date, events, onEventClick }) => {
  const theme = useTheme();
  const dayEvents = events.filter(e => isSameDay(e.date, date));
  const isCurrentDay = isToday(date);
  const isPastDay = isPast(date) && !isToday(date);

  return (
    <Box
      sx={{
        minHeight: 120,
        p: 0.5,
        bgcolor: isCurrentDay 
          ? alpha(theme.palette.primary.main, 0.08) 
          : isPastDay 
          ? alpha(theme.palette.grey[500], 0.05) 
          : 'transparent',
        borderRight: '1px solid',
        borderBottom: '1px solid',
        borderColor: 'divider',
        '&:hover': {
          bgcolor: alpha(theme.palette.primary.main, 0.05),
        },
      }}
    >
      <Typography
        variant="body2"
        sx={{
          fontWeight: isCurrentDay ? 700 : 400,
          color: isCurrentDay 
            ? 'primary.main' 
            : isPastDay 
            ? 'text.disabled' 
            : 'text.primary',
          mb: 0.5,
        }}
      >
        {format(date, 'd')}
      </Typography>
      
      {dayEvents.slice(0, 3).map((event) => (
        <Chip
          key={event.id}
          label={event.title}
          size="small"
          onClick={() => onEventClick(event)}
          sx={{
            width: '100%',
            justifyContent: 'flex-start',
            mb: 0.5,
            bgcolor: alpha(getEventColor(event, theme), 0.15),
            color: getEventColor(event, theme),
            borderLeft: `3px solid ${getEventColor(event, theme)}`,
            borderRadius: 0.5,
            cursor: 'pointer',
            '& .MuiChip-label': {
              overflow: 'hidden',
              textOverflow: 'ellipsis',
            },
            '&:hover': {
              bgcolor: alpha(getEventColor(event, theme), 0.25),
            },
          }}
        />
      ))}
      
      {dayEvents.length > 3 && (
        <Typography variant="caption" color="primary" sx={{ cursor: 'pointer' }}>
          +{dayEvents.length - 3} more
        </Typography>
      )}
    </Box>
  );
};

interface EventDialogProps {
  open: boolean;
  event: MaintenanceEvent | null;
  mode: 'view' | 'edit' | 'add';
  onClose: () => void;
  onSave: (event: Partial<MaintenanceEvent>) => void;
  onDelete: (id: string) => void;
}

const EventDialog: React.FC<EventDialogProps> = ({
  open,
  event,
  mode,
  onClose,
  onSave,
  onDelete,
}) => {
  const theme = useTheme();
  const [formData, setFormData] = useState<Partial<MaintenanceEvent>>({
    title: '',
    description: '',
    date: new Date(),
    duration: 1,
    type: 'routine',
    priority: 'medium',
    cost: 0,
  });

  useEffect(() => {
    if (event && mode !== 'add') {
      setFormData(event);
    } else {
      setFormData({
        title: '',
        description: '',
        date: new Date(),
        duration: 1,
        type: 'routine',
        priority: 'medium',
        cost: 0,
      });
    }
  }, [event, mode]);

  const handleChange = (field: keyof MaintenanceEvent, value: any) => {
    setFormData({ ...formData, [field]: value });
  };

  const isViewMode = mode === 'view';

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {event && (
              <Avatar sx={{ bgcolor: alpha(getEventColor(event, theme), 0.15) }}>
                <MaintenanceIcon sx={{ color: getEventColor(event, theme) }} />
              </Avatar>
            )}
            <Typography variant="h6">
              {mode === 'add' ? 'Add Maintenance' : mode === 'edit' ? 'Edit Maintenance' : event?.title}
            </Typography>
          </Box>
          <IconButton onClick={onClose} size="small">
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>
      <DialogContent dividers>
        <Grid container spacing={2}>
          {!isViewMode && (
            <>
              <Grid item xs={12}>
                <TextField
                  label="Title"
                  fullWidth
                  value={formData.title}
                  onChange={(e) => handleChange('title', e.target.value)}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  label="Description"
                  fullWidth
                  multiline
                  rows={2}
                  value={formData.description}
                  onChange={(e) => handleChange('description', e.target.value)}
                />
              </Grid>
            </>
          )}
          
          {isViewMode && event && (
            <>
              <Grid item xs={12}>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  {event.description}
                </Typography>
                <Divider sx={{ my: 1 }} />
              </Grid>
              
              <Grid item xs={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <VehicleIcon color="action" />
                  <Box>
                    <Typography variant="caption" color="text.secondary">Vehicle</Typography>
                    <Typography variant="body2">{event.vehicle.name}</Typography>
                    <Typography variant="caption" color="text.secondary">{event.vehicle.licensePlate}</Typography>
                  </Box>
                </Box>
              </Grid>
              
              <Grid item xs={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <ScheduleIcon color="action" />
                  <Box>
                    <Typography variant="caption" color="text.secondary">Date & Duration</Typography>
                    <Typography variant="body2">{format(event.date, 'MMM d, yyyy')}</Typography>
                    <Typography variant="caption" color="text.secondary">{event.duration} hours</Typography>
                  </Box>
                </Box>
              </Grid>
              
              <Grid item xs={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <TagIcon color="action" />
                  <Box>
                    <Typography variant="caption" color="text.secondary">Type</Typography>
                    <Chip
                      size="small"
                      label={event.type}
                      sx={{ bgcolor: alpha(getEventColor(event, theme), 0.15), color: getEventColor(event, theme) }}
                    />
                  </Box>
                </Box>
              </Grid>
              
              <Grid item xs={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <WarningIcon color="action" />
                  <Box>
                    <Typography variant="caption" color="text.secondary">Priority</Typography>
                    <Chip size="small" label={event.priority} color={getPriorityColor(event.priority)} variant="outlined" />
                  </Box>
                </Box>
              </Grid>
              
              {event.assignedTo && (
                <Grid item xs={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <DriverIcon color="action" />
                    <Box>
                      <Typography variant="caption" color="text.secondary">Assigned To</Typography>
                      <Typography variant="body2">{event.assignedTo}</Typography>
                    </Box>
                  </Box>
                </Grid>
              )}
              
              {event.cost !== undefined && (
                <Grid item xs={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <MoneyIcon color="action" />
                    <Box>
                      <Typography variant="caption" color="text.secondary">Estimated Cost</Typography>
                      <Typography variant="body2">${event.cost}</Typography>
                    </Box>
                  </Box>
                </Grid>
              )}
            </>
          )}
          
          {!isViewMode && (
            <>
              <Grid item xs={6}>
                <FormControl fullWidth>
                  <InputLabel>Type</InputLabel>
                  <Select
                    value={formData.type}
                    onChange={(e) => handleChange('type', e.target.value)}
                    label="Type"
                  >
                    <MenuItem value="routine">Routine</MenuItem>
                    <MenuItem value="emergency">Emergency</MenuItem>
                    <MenuItem value="inspection">Inspection</MenuItem>
                    <MenuItem value="recall">Recall</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              
              <Grid item xs={6}>
                <FormControl fullWidth>
                  <InputLabel>Priority</InputLabel>
                  <Select
                    value={formData.priority}
                    onChange={(e) => handleChange('priority', e.target.value)}
                    label="Priority"
                  >
                    <MenuItem value="high">High</MenuItem>
                    <MenuItem value="medium">Medium</MenuItem>
                    <MenuItem value="low">Low</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              
              <Grid item xs={6}>
                <TextField
                  label="Duration (hours)"
                  type="number"
                  fullWidth
                  value={formData.duration}
                  onChange={(e) => handleChange('duration', Number(e.target.value))}
                />
              </Grid>
              
              <Grid item xs={6}>
                <TextField
                  label="Estimated Cost"
                  type="number"
                  fullWidth
                  value={formData.cost}
                  onChange={(e) => handleChange('cost', Number(e.target.value))}
                  InputProps={{ startAdornment: '$' }}
                />
              </Grid>
            </>
          )}
        </Grid>
      </DialogContent>
      <DialogActions>
        {isViewMode && event && (
          <>
            <Button color="error" startIcon={<DeleteIcon />} onClick={() => onDelete(event.id)}>
              Delete
            </Button>
            <Box sx={{ flex: 1 }} />
          </>
        )}
        <Button onClick={onClose}>
          {isViewMode ? 'Close' : 'Cancel'}
        </Button>
        {!isViewMode && (
          <Button variant="contained" onClick={() => onSave(formData)}>
            {mode === 'add' ? 'Add' : 'Save'}
          </Button>
        )}
        {isViewMode && (
          <Button variant="contained" startIcon={<EditIcon />}>
            Edit
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};

// Main Component
const MaintenanceCalendar: React.FC = () => {
  const theme = useTheme();
  
  // State
  const [events, setEvents] = useState<MaintenanceEvent[]>([]);
  const [currentDate, setCurrentDate] = useState(new Date());
  const [viewMode, setViewMode] = useState<ViewMode>('month');
  const [selectedEvent, setSelectedEvent] = useState<MaintenanceEvent | null>(null);
  const [dialogMode, setDialogMode] = useState<'view' | 'edit' | 'add'>('view');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [filterAnchor, setFilterAnchor] = useState<HTMLElement | null>(null);
  const [typeFilter, setTypeFilter] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);

  // Load data
  useEffect(() => {
    setEvents(generateMockEvents());
  }, []);

  // Calculate calendar grid
  const monthStart = startOfMonth(currentDate);
  const monthEnd = endOfMonth(currentDate);
  const calendarStart = startOfWeek(monthStart);
  const calendarEnd = endOfWeek(monthEnd);

  const generateCalendarDays = (): Date[] => {
    const days: Date[] = [];
    let day = calendarStart;
    while (day <= calendarEnd) {
      days.push(day);
      day = addDays(day, 1);
    }
    return days;
  };

  const calendarDays = generateCalendarDays();
  const weeks: Date[][] = [];
  for (let i = 0; i < calendarDays.length; i += 7) {
    weeks.push(calendarDays.slice(i, i + 7));
  }

  // Handlers
  const handlePreviousMonth = () => {
    setCurrentDate(subMonths(currentDate, 1));
  };

  const handleNextMonth = () => {
    setCurrentDate(addMonths(currentDate, 1));
  };

  const handleToday = () => {
    setCurrentDate(new Date());
  };

  const handleEventClick = (event: MaintenanceEvent) => {
    setSelectedEvent(event);
    setDialogMode('view');
    setDialogOpen(true);
  };

  const handleAddEvent = () => {
    setSelectedEvent(null);
    setDialogMode('add');
    setDialogOpen(true);
  };

  const handleSaveEvent = (eventData: Partial<MaintenanceEvent>) => {
    console.log('Saving event:', eventData);
    setDialogOpen(false);
  };

  const handleDeleteEvent = (id: string) => {
    setEvents(events.filter(e => e.id !== id));
    setDialogOpen(false);
  };

  const filteredEvents = typeFilter.length > 0 
    ? events.filter(e => typeFilter.includes(e.type))
    : events;

  // Summary stats
  const overdueCount = events.filter(e => e.status === 'overdue').length;
  const todayCount = events.filter(e => isToday(e.date)).length;
  const thisWeekCount = events.filter(e => {
    const daysUntil = differenceInDays(e.date, new Date());
    return daysUntil >= 0 && daysUntil <= 7;
  }).length;

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Box sx={{ p: 3 }}>
        {/* Header */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Box>
            <Typography variant="h4" fontWeight={700}>
              Maintenance Calendar
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Schedule and track vehicle maintenance
            </Typography>
          </Box>
          
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
            <ToggleButtonGroup
              value={viewMode}
              exclusive
              onChange={(_, mode) => mode && setViewMode(mode)}
              size="small"
            >
              <ToggleButton value="month"><MonthIcon /></ToggleButton>
              <ToggleButton value="week"><WeekIcon /></ToggleButton>
              <ToggleButton value="day"><DayIcon /></ToggleButton>
            </ToggleButtonGroup>

            <IconButton onClick={(e) => setFilterAnchor(e.currentTarget)}>
              <Badge badgeContent={typeFilter.length} color="primary">
                <FilterIcon />
              </Badge>
            </IconButton>

            <Button variant="contained" startIcon={<AddIcon />} onClick={handleAddEvent}>
              Add Maintenance
            </Button>
          </Box>
        </Box>

        {/* Summary Cards */}
        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid item xs={12} sm={4}>
            <Card sx={{ bgcolor: alpha(theme.palette.error.main, 0.1) }}>
              <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Badge badgeContent={overdueCount} color="error">
                  <Avatar sx={{ bgcolor: alpha(theme.palette.error.main, 0.2) }}>
                    <WarningIcon color="error" />
                  </Avatar>
                </Badge>
                <Box>
                  <Typography variant="h5" fontWeight={700} color="error.main">
                    {overdueCount}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">Overdue Tasks</Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={4}>
            <Card sx={{ bgcolor: alpha(theme.palette.primary.main, 0.1) }}>
              <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Avatar sx={{ bgcolor: alpha(theme.palette.primary.main, 0.2) }}>
                  <TodayIcon color="primary" />
                </Avatar>
                <Box>
                  <Typography variant="h5" fontWeight={700} color="primary.main">
                    {todayCount}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">Today's Tasks</Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={4}>
            <Card sx={{ bgcolor: alpha(theme.palette.success.main, 0.1) }}>
              <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Avatar sx={{ bgcolor: alpha(theme.palette.success.main, 0.2) }}>
                  <ScheduleIcon color="success" />
                </Avatar>
                <Box>
                  <Typography variant="h5" fontWeight={700} color="success.main">
                    {thisWeekCount}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">This Week</Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        {/* Calendar Navigation */}
        <Paper sx={{ mb: 2, p: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <IconButton onClick={handlePreviousMonth}>
                <ChevronLeftIcon />
              </IconButton>
              <Typography variant="h6" fontWeight={600} sx={{ minWidth: 180, textAlign: 'center' }}>
                {format(currentDate, 'MMMM yyyy')}
              </Typography>
              <IconButton onClick={handleNextMonth}>
                <ChevronRightIcon />
              </IconButton>
            </Box>
            
            <Button variant="outlined" size="small" onClick={handleToday}>
              Today
            </Button>
          </Box>
        </Paper>

        {/* Calendar Grid */}
        <Paper sx={{ overflow: 'hidden' }}>
          {/* Day Headers */}
          <Grid container sx={{ bgcolor: alpha(theme.palette.primary.main, 0.1) }}>
            {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((day) => (
              <Grid item xs key={day} sx={{ p: 1, textAlign: 'center', borderRight: '1px solid', borderColor: 'divider' }}>
                <Typography variant="subtitle2" fontWeight={600}>
                  {day}
                </Typography>
              </Grid>
            ))}
          </Grid>

          {/* Calendar Weeks */}
          {weeks.map((week, weekIndex) => (
            <Grid container key={weekIndex}>
              {week.map((day, dayIndex) => (
                <Grid item xs key={dayIndex}>
                  <DayCell
                    date={day}
                    events={filteredEvents}
                    onEventClick={handleEventClick}
                  />
                </Grid>
              ))}
            </Grid>
          ))}
        </Paper>

        {/* Legend */}
        <Paper sx={{ mt: 2, p: 2 }}>
          <Typography variant="subtitle2" gutterBottom>Legend</Typography>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
            <Chip label="Routine" sx={{ bgcolor: alpha(theme.palette.primary.main, 0.15), color: theme.palette.primary.main }} />
            <Chip label="Emergency" sx={{ bgcolor: alpha(theme.palette.error.main, 0.15), color: theme.palette.error.main }} />
            <Chip label="Inspection" sx={{ bgcolor: alpha(theme.palette.info.main, 0.15), color: theme.palette.info.main }} />
            <Chip label="Recall" sx={{ bgcolor: alpha(theme.palette.warning.main, 0.15), color: theme.palette.warning.main }} />
            <Divider orientation="vertical" flexItem />
            <Chip label="Overdue" color="error" variant="outlined" />
            <Chip label="High Priority" color="error" size="small" />
            <Chip label="Medium" color="warning" size="small" />
            <Chip label="Low" color="success" size="small" />
          </Box>
        </Paper>

        {/* Filter Popover */}
        <Popover
          open={Boolean(filterAnchor)}
          anchorEl={filterAnchor}
          onClose={() => setFilterAnchor(null)}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
          transformOrigin={{ vertical: 'top', horizontal: 'right' }}
        >
          <Box sx={{ p: 2, minWidth: 200 }}>
            <Typography variant="subtitle2" gutterBottom>Filter by Type</Typography>
            {['routine', 'emergency', 'inspection', 'recall'].map((type) => (
              <FormControlLabel
                key={type}
                control={
                  <Switch
                    checked={typeFilter.includes(type)}
                    onChange={(e) => {
                      if (e.target.checked) {
                        setTypeFilter([...typeFilter, type]);
                      } else {
                        setTypeFilter(typeFilter.filter(t => t !== type));
                      }
                    }}
                    size="small"
                  />
                }
                label={type.charAt(0).toUpperCase() + type.slice(1)}
              />
            ))}
            {typeFilter.length > 0 && (
              <Button size="small" onClick={() => setTypeFilter([])}>
                Clear Filters
              </Button>
            )}
          </Box>
        </Popover>

        {/* Event Dialog */}
        <EventDialog
          open={dialogOpen}
          event={selectedEvent}
          mode={dialogMode}
          onClose={() => setDialogOpen(false)}
          onSave={handleSaveEvent}
          onDelete={handleDeleteEvent}
        />
      </Box>
    </LocalizationProvider>
  );
};

export default MaintenanceCalendar;

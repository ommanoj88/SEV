/**
 * MaintenanceScheduleCalendar.tsx
 * PR #37: Maintenance Schedule Calendar View
 * 
 * Features:
 * - Month/week/day view toggle
 * - Drag-and-drop rescheduling
 * - Color-coded by maintenance type
 * - Quick schedule popup
 * - Conflict detection
 * - Vehicle filter
 */

import React, { useState, useCallback, useMemo } from 'react';
import {
  Box,
  Paper,
  Typography,
  ButtonGroup,
  Button,
  IconButton,
  Grid,
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
  Tooltip,
  Badge,
  Alert,
  Autocomplete,
  CircularProgress,
  Card,
  CardContent,
  Popover,
} from '@mui/material';
import {
  ChevronLeft,
  ChevronRight,
  Today,
  Add,
  Build,
  Warning,
  DirectionsCar,
  Schedule,
  Edit,
  Delete,
  DragIndicator,
} from '@mui/icons-material';
import { DatePicker, TimePicker } from '@mui/x-date-pickers';
import {
  format,
  startOfMonth,
  endOfMonth,
  startOfWeek,
  endOfWeek,
  eachDayOfInterval,
  isSameMonth,
  isSameDay,
  addMonths,
  subMonths,
  addWeeks,
  subWeeks,
  addDays,
  subDays,
  isToday,
  parseISO,
  isBefore,
  isAfter,
} from 'date-fns';

// Types
interface MaintenanceEvent {
  id: string;
  vehicleId: string;
  vehicleName: string;
  licensePlate: string;
  type: MaintenanceType;
  title: string;
  description: string;
  scheduledDate: Date;
  estimatedDuration: number; // in hours
  status: 'scheduled' | 'in_progress' | 'completed' | 'overdue' | 'cancelled';
  priority: 'low' | 'medium' | 'high' | 'critical';
  assignedTechnician?: string;
  estimatedCost?: number;
  notes?: string;
}

type MaintenanceType = 
  | 'oil_change'
  | 'tire_rotation'
  | 'battery_check'
  | 'brake_inspection'
  | 'filter_replacement'
  | 'fluid_check'
  | 'general_inspection'
  | 'scheduled_service'
  | 'recall'
  | 'other';

type ViewMode = 'month' | 'week' | 'day';

interface Vehicle {
  id: string;
  name: string;
  licensePlate: string;
}

// Mock data
const mockVehicles: Vehicle[] = [
  { id: 'v1', name: 'Tesla Model 3 #1', licensePlate: 'EV-001' },
  { id: 'v2', name: 'Tata Nexon EV', licensePlate: 'EV-002' },
  { id: 'v3', name: 'MG ZS EV', licensePlate: 'EV-003' },
  { id: 'v4', name: 'Hyundai Kona', licensePlate: 'EV-004' },
  { id: 'v5', name: 'BYD Atto 3', licensePlate: 'EV-005' },
];

const mockEvents: MaintenanceEvent[] = [
  {
    id: '1',
    vehicleId: 'v1',
    vehicleName: 'Tesla Model 3 #1',
    licensePlate: 'EV-001',
    type: 'battery_check',
    title: 'Battery Health Check',
    description: 'Routine battery health assessment',
    scheduledDate: new Date(),
    estimatedDuration: 2,
    status: 'scheduled',
    priority: 'medium',
    assignedTechnician: 'Rajesh Kumar',
    estimatedCost: 5000,
  },
  {
    id: '2',
    vehicleId: 'v2',
    vehicleName: 'Tata Nexon EV',
    licensePlate: 'EV-002',
    type: 'tire_rotation',
    title: 'Tire Rotation',
    description: 'Scheduled tire rotation and balancing',
    scheduledDate: addDays(new Date(), 2),
    estimatedDuration: 1,
    status: 'scheduled',
    priority: 'low',
    assignedTechnician: 'Amit Singh',
    estimatedCost: 2000,
  },
  {
    id: '3',
    vehicleId: 'v3',
    vehicleName: 'MG ZS EV',
    licensePlate: 'EV-003',
    type: 'brake_inspection',
    title: 'Brake System Inspection',
    description: 'Check brake pads and rotors',
    scheduledDate: addDays(new Date(), 5),
    estimatedDuration: 3,
    status: 'scheduled',
    priority: 'high',
    assignedTechnician: 'Vikram Patel',
    estimatedCost: 8000,
  },
  {
    id: '4',
    vehicleId: 'v1',
    vehicleName: 'Tesla Model 3 #1',
    licensePlate: 'EV-001',
    type: 'scheduled_service',
    title: 'Annual Service',
    description: 'Comprehensive annual service',
    scheduledDate: addDays(new Date(), 10),
    estimatedDuration: 6,
    status: 'scheduled',
    priority: 'medium',
    estimatedCost: 25000,
  },
  {
    id: '5',
    vehicleId: 'v4',
    vehicleName: 'Hyundai Kona',
    licensePlate: 'EV-004',
    type: 'recall',
    title: 'Safety Recall - Software Update',
    description: 'Critical software update for battery management',
    scheduledDate: subDays(new Date(), 2),
    estimatedDuration: 2,
    status: 'overdue',
    priority: 'critical',
    estimatedCost: 0,
  },
];

const maintenanceTypeConfig: Record<MaintenanceType, { label: string; color: string }> = {
  oil_change: { label: 'Oil Change', color: '#795548' },
  tire_rotation: { label: 'Tire Rotation', color: '#607D8B' },
  battery_check: { label: 'Battery Check', color: '#4CAF50' },
  brake_inspection: { label: 'Brake Inspection', color: '#F44336' },
  filter_replacement: { label: 'Filter Replacement', color: '#9C27B0' },
  fluid_check: { label: 'Fluid Check', color: '#00BCD4' },
  general_inspection: { label: 'General Inspection', color: '#2196F3' },
  scheduled_service: { label: 'Scheduled Service', color: '#FF9800' },
  recall: { label: 'Recall', color: '#E91E63' },
  other: { label: 'Other', color: '#9E9E9E' },
};

const priorityColors = {
  low: '#4CAF50',
  medium: '#FF9800',
  high: '#F44336',
  critical: '#9C27B0',
};

const statusColors = {
  scheduled: '#2196F3',
  in_progress: '#FF9800',
  completed: '#4CAF50',
  overdue: '#F44336',
  cancelled: '#9E9E9E',
};

export const MaintenanceScheduleCalendar: React.FC = () => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [viewMode, setViewMode] = useState<ViewMode>('month');
  const [events, setEvents] = useState<MaintenanceEvent[]>(mockEvents);
  const [selectedEvent, setSelectedEvent] = useState<MaintenanceEvent | null>(null);
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [selectedVehicleFilter, setSelectedVehicleFilter] = useState<Vehicle | null>(null);
  const [draggedEvent, setDraggedEvent] = useState<MaintenanceEvent | null>(null);
  const [popoverAnchor, setPopoverAnchor] = useState<HTMLElement | null>(null);
  const [quickViewEvent, setQuickViewEvent] = useState<MaintenanceEvent | null>(null);
  const [conflictWarning, setConflictWarning] = useState<string | null>(null);

  // Form state for add/edit
  const [formData, setFormData] = useState<Partial<MaintenanceEvent>>({
    type: 'general_inspection',
    priority: 'medium',
    estimatedDuration: 2,
    scheduledDate: new Date(),
  });

  // Navigation handlers
  const handlePrevious = () => {
    switch (viewMode) {
      case 'month':
        setCurrentDate(subMonths(currentDate, 1));
        break;
      case 'week':
        setCurrentDate(subWeeks(currentDate, 1));
        break;
      case 'day':
        setCurrentDate(subDays(currentDate, 1));
        break;
    }
  };

  const handleNext = () => {
    switch (viewMode) {
      case 'month':
        setCurrentDate(addMonths(currentDate, 1));
        break;
      case 'week':
        setCurrentDate(addWeeks(currentDate, 1));
        break;
      case 'day':
        setCurrentDate(addDays(currentDate, 1));
        break;
    }
  };

  const handleToday = () => setCurrentDate(new Date());

  // Get days for current view
  const getDaysForView = useMemo(() => {
    switch (viewMode) {
      case 'month':
        const monthStart = startOfMonth(currentDate);
        const monthEnd = endOfMonth(currentDate);
        return eachDayOfInterval({
          start: startOfWeek(monthStart),
          end: endOfWeek(monthEnd),
        });
      case 'week':
        return eachDayOfInterval({
          start: startOfWeek(currentDate),
          end: endOfWeek(currentDate),
        });
      case 'day':
        return [currentDate];
    }
  }, [currentDate, viewMode]);

  // Filter events
  const filteredEvents = useMemo(() => {
    let filtered = events;
    if (selectedVehicleFilter) {
      filtered = filtered.filter(e => e.vehicleId === selectedVehicleFilter.id);
    }
    return filtered;
  }, [events, selectedVehicleFilter]);

  // Get events for a specific day
  const getEventsForDay = (day: Date) => {
    return filteredEvents.filter(event => isSameDay(event.scheduledDate, day));
  };

  // Check for conflicts
  const checkConflicts = (vehicleId: string, date: Date, excludeEventId?: string) => {
    const vehicleEvents = events.filter(
      e => e.vehicleId === vehicleId && 
           isSameDay(e.scheduledDate, date) &&
           e.id !== excludeEventId
    );
    return vehicleEvents.length > 0;
  };

  // Drag and drop handlers
  const handleDragStart = (event: MaintenanceEvent) => {
    setDraggedEvent(event);
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  const handleDrop = (day: Date) => {
    if (draggedEvent) {
      const hasConflict = checkConflicts(draggedEvent.vehicleId, day, draggedEvent.id);
      
      if (hasConflict) {
        setConflictWarning(`Vehicle ${draggedEvent.licensePlate} already has maintenance scheduled on ${format(day, 'MMM dd')}`);
        setTimeout(() => setConflictWarning(null), 3000);
      }

      setEvents(prev =>
        prev.map(e =>
          e.id === draggedEvent.id ? { ...e, scheduledDate: day } : e
        )
      );
      setDraggedEvent(null);
    }
  };

  // Event handlers
  const handleEventClick = (event: MaintenanceEvent, anchorEl: HTMLElement) => {
    setQuickViewEvent(event);
    setPopoverAnchor(anchorEl);
  };

  const handleClosePopover = () => {
    setPopoverAnchor(null);
    setQuickViewEvent(null);
  };

  const handleEditEvent = () => {
    if (quickViewEvent) {
      setSelectedEvent(quickViewEvent);
      setFormData(quickViewEvent);
      setIsEditDialogOpen(true);
      handleClosePopover();
    }
  };

  const handleDeleteEvent = () => {
    if (quickViewEvent) {
      setEvents(prev => prev.filter(e => e.id !== quickViewEvent.id));
      handleClosePopover();
    }
  };

  const handleAddEvent = (day?: Date) => {
    setFormData({
      type: 'general_inspection',
      priority: 'medium',
      estimatedDuration: 2,
      scheduledDate: day || new Date(),
    });
    setIsAddDialogOpen(true);
  };

  const handleSaveEvent = () => {
    if (isEditDialogOpen && selectedEvent) {
      setEvents(prev =>
        prev.map(e =>
          e.id === selectedEvent.id ? { ...e, ...formData } as MaintenanceEvent : e
        )
      );
      setIsEditDialogOpen(false);
    } else {
      const newEvent: MaintenanceEvent = {
        id: `new-${Date.now()}`,
        vehicleId: (formData as any).vehicleId || 'v1',
        vehicleName: mockVehicles.find(v => v.id === (formData as any).vehicleId)?.name || 'Unknown',
        licensePlate: mockVehicles.find(v => v.id === (formData as any).vehicleId)?.licensePlate || 'N/A',
        title: formData.title || 'New Maintenance',
        description: formData.description || '',
        type: formData.type as MaintenanceType,
        scheduledDate: formData.scheduledDate as Date,
        estimatedDuration: formData.estimatedDuration || 2,
        status: 'scheduled',
        priority: formData.priority as any,
        assignedTechnician: formData.assignedTechnician,
        estimatedCost: formData.estimatedCost,
      };
      setEvents(prev => [...prev, newEvent]);
      setIsAddDialogOpen(false);
    }
    setFormData({});
  };

  // Render calendar cell
  const renderCalendarCell = (day: Date) => {
    const dayEvents = getEventsForDay(day);
    const isCurrentMonth = isSameMonth(day, currentDate);
    const isCurrentDay = isToday(day);

    return (
      <Box
        key={day.toISOString()}
        sx={{
          minHeight: viewMode === 'month' ? 100 : 200,
          p: 0.5,
          border: '1px solid',
          borderColor: 'divider',
          bgcolor: isCurrentMonth ? 'background.paper' : 'action.hover',
          cursor: 'pointer',
          transition: 'background-color 0.2s',
          '&:hover': { bgcolor: 'action.selected' },
        }}
        onDragOver={handleDragOver}
        onDrop={() => handleDrop(day)}
        onDoubleClick={() => handleAddEvent(day)}
      >
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 0.5 }}>
          <Typography
            variant="body2"
            sx={{
              fontWeight: isCurrentDay ? 700 : 400,
              color: isCurrentDay ? 'primary.main' : isCurrentMonth ? 'text.primary' : 'text.secondary',
              bgcolor: isCurrentDay ? 'primary.light' : 'transparent',
              borderRadius: '50%',
              width: 28,
              height: 28,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            {format(day, 'd')}
          </Typography>
          {dayEvents.length > 0 && viewMode === 'month' && (
            <Badge badgeContent={dayEvents.length} color="primary" sx={{ mr: 1 }} />
          )}
        </Box>

        {/* Events */}
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
          {dayEvents.slice(0, viewMode === 'month' ? 3 : 10).map(event => (
            <Box
              key={event.id}
              draggable
              onDragStart={() => handleDragStart(event)}
              onClick={(e) => handleEventClick(event, e.currentTarget)}
              sx={{
                p: 0.5,
                borderRadius: 0.5,
                bgcolor: maintenanceTypeConfig[event.type].color,
                color: 'white',
                cursor: 'grab',
                display: 'flex',
                alignItems: 'center',
                gap: 0.5,
                fontSize: '0.75rem',
                overflow: 'hidden',
                '&:hover': { opacity: 0.9 },
                '&:active': { cursor: 'grabbing' },
                borderLeft: `3px solid ${priorityColors[event.priority]}`,
              }}
            >
              <DragIndicator sx={{ fontSize: 12 }} />
              <Typography variant="caption" noWrap sx={{ flex: 1 }}>
                {event.title}
              </Typography>
              {event.status === 'overdue' && (
                <Warning sx={{ fontSize: 12, color: 'white' }} />
              )}
            </Box>
          ))}
          {dayEvents.length > (viewMode === 'month' ? 3 : 10) && (
            <Typography variant="caption" color="text.secondary">
              +{dayEvents.length - (viewMode === 'month' ? 3 : 10)} more
            </Typography>
          )}
        </Box>
      </Box>
    );
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Paper sx={{ p: 2, mb: 2 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} md={4}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <IconButton onClick={handlePrevious}>
                <ChevronLeft />
              </IconButton>
              <IconButton onClick={handleToday}>
                <Today />
              </IconButton>
              <IconButton onClick={handleNext}>
                <ChevronRight />
              </IconButton>
              <Typography variant="h6">
                {viewMode === 'day'
                  ? format(currentDate, 'EEEE, MMMM d, yyyy')
                  : viewMode === 'week'
                  ? `Week of ${format(startOfWeek(currentDate), 'MMM d, yyyy')}`
                  : format(currentDate, 'MMMM yyyy')}
              </Typography>
            </Box>
          </Grid>

          <Grid item xs={12} md={4} sx={{ display: 'flex', justifyContent: 'center' }}>
            <ButtonGroup size="small">
              <Button
                variant={viewMode === 'month' ? 'contained' : 'outlined'}
                onClick={() => setViewMode('month')}
              >
                Month
              </Button>
              <Button
                variant={viewMode === 'week' ? 'contained' : 'outlined'}
                onClick={() => setViewMode('week')}
              >
                Week
              </Button>
              <Button
                variant={viewMode === 'day' ? 'contained' : 'outlined'}
                onClick={() => setViewMode('day')}
              >
                Day
              </Button>
            </ButtonGroup>
          </Grid>

          <Grid item xs={12} md={4}>
            <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
              <Autocomplete
                size="small"
                options={mockVehicles}
                getOptionLabel={(v) => `${v.name} (${v.licensePlate})`}
                value={selectedVehicleFilter}
                onChange={(_, value) => setSelectedVehicleFilter(value)}
                sx={{ minWidth: 200 }}
                renderInput={(params) => (
                  <TextField {...params} placeholder="Filter by vehicle" />
                )}
              />
              <Button
                variant="contained"
                startIcon={<Add />}
                onClick={() => handleAddEvent()}
              >
                Schedule
              </Button>
            </Box>
          </Grid>
        </Grid>
      </Paper>

      {/* Conflict Warning */}
      {conflictWarning && (
        <Alert severity="warning" sx={{ mb: 2 }} onClose={() => setConflictWarning(null)}>
          {conflictWarning}
        </Alert>
      )}

      {/* Legend */}
      <Paper sx={{ p: 1, mb: 2 }}>
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, justifyContent: 'center' }}>
          {Object.entries(maintenanceTypeConfig).map(([key, config]) => (
            <Chip
              key={key}
              size="small"
              label={config.label}
              sx={{ bgcolor: config.color, color: 'white' }}
            />
          ))}
        </Box>
      </Paper>

      {/* Calendar Grid */}
      <Paper sx={{ overflow: 'hidden' }}>
        {/* Day headers */}
        {viewMode !== 'day' && (
          <Grid container>
            {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => (
              <Grid item xs key={day} sx={{ textAlign: 'center', py: 1, bgcolor: 'primary.main', color: 'white' }}>
                <Typography variant="body2" fontWeight={600}>
                  {day}
                </Typography>
              </Grid>
            ))}
          </Grid>
        )}

        {/* Calendar cells */}
        <Grid container>
          {getDaysForView.map(day => (
            <Grid
              item
              xs={viewMode === 'day' ? 12 : 12 / 7}
              key={day.toISOString()}
            >
              {renderCalendarCell(day)}
            </Grid>
          ))}
        </Grid>
      </Paper>

      {/* Event Quick View Popover */}
      <Popover
        open={Boolean(popoverAnchor)}
        anchorEl={popoverAnchor}
        onClose={handleClosePopover}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
        transformOrigin={{ vertical: 'top', horizontal: 'left' }}
      >
        {quickViewEvent && (
          <Card sx={{ minWidth: 300, maxWidth: 400 }}>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                <Box>
                  <Typography variant="h6">{quickViewEvent.title}</Typography>
                  <Chip
                    size="small"
                    label={maintenanceTypeConfig[quickViewEvent.type].label}
                    sx={{ bgcolor: maintenanceTypeConfig[quickViewEvent.type].color, color: 'white', mt: 0.5 }}
                  />
                </Box>
                <Box>
                  <IconButton size="small" onClick={handleEditEvent}>
                    <Edit fontSize="small" />
                  </IconButton>
                  <IconButton size="small" onClick={handleDeleteEvent} color="error">
                    <Delete fontSize="small" />
                  </IconButton>
                </Box>
              </Box>

              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <DirectionsCar fontSize="small" color="action" />
                <Typography variant="body2">
                  {quickViewEvent.vehicleName} ({quickViewEvent.licensePlate})
                </Typography>
              </Box>

              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <Schedule fontSize="small" color="action" />
                <Typography variant="body2">
                  {format(quickViewEvent.scheduledDate, 'MMM dd, yyyy')} • {quickViewEvent.estimatedDuration}h
                </Typography>
              </Box>

              <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
                <Chip
                  size="small"
                  label={quickViewEvent.status.replace('_', ' ').toUpperCase()}
                  sx={{ bgcolor: statusColors[quickViewEvent.status], color: 'white' }}
                />
                <Chip
                  size="small"
                  label={quickViewEvent.priority.toUpperCase()}
                  sx={{ bgcolor: priorityColors[quickViewEvent.priority], color: 'white' }}
                />
              </Box>

              {quickViewEvent.description && (
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                  {quickViewEvent.description}
                </Typography>
              )}

              {quickViewEvent.estimatedCost !== undefined && (
                <Typography variant="body2" fontWeight={600} sx={{ mt: 1 }}>
                  Est. Cost: ₹{quickViewEvent.estimatedCost.toLocaleString()}
                </Typography>
              )}
            </CardContent>
          </Card>
        )}
      </Popover>

      {/* Add/Edit Dialog */}
      <Dialog
        open={isAddDialogOpen || isEditDialogOpen}
        onClose={() => { setIsAddDialogOpen(false); setIsEditDialogOpen(false); }}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          {isEditDialogOpen ? 'Edit Maintenance Schedule' : 'Schedule New Maintenance'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <FormControl fullWidth>
              <InputLabel>Vehicle</InputLabel>
              <Select
                value={(formData as any).vehicleId || ''}
                label="Vehicle"
                onChange={(e) => setFormData(prev => ({ ...prev, vehicleId: e.target.value }))}
              >
                {mockVehicles.map(v => (
                  <MenuItem key={v.id} value={v.id}>
                    {v.name} ({v.licensePlate})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <TextField
              label="Title"
              value={formData.title || ''}
              onChange={(e) => setFormData(prev => ({ ...prev, title: e.target.value }))}
              fullWidth
            />

            <FormControl fullWidth>
              <InputLabel>Maintenance Type</InputLabel>
              <Select
                value={formData.type || 'general_inspection'}
                label="Maintenance Type"
                onChange={(e) => setFormData(prev => ({ ...prev, type: e.target.value as MaintenanceType }))}
              >
                {Object.entries(maintenanceTypeConfig).map(([key, config]) => (
                  <MenuItem key={key} value={key}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Box sx={{ width: 12, height: 12, borderRadius: '50%', bgcolor: config.color }} />
                      {config.label}
                    </Box>
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <DatePicker
              label="Scheduled Date"
              value={formData.scheduledDate || null}
              onChange={(date) => setFormData(prev => ({ ...prev, scheduledDate: date || new Date() }))}
            />

            <TextField
              label="Estimated Duration (hours)"
              type="number"
              value={formData.estimatedDuration || 2}
              onChange={(e) => setFormData(prev => ({ ...prev, estimatedDuration: Number(e.target.value) }))}
              fullWidth
            />

            <FormControl fullWidth>
              <InputLabel>Priority</InputLabel>
              <Select
                value={formData.priority || 'medium'}
                label="Priority"
                onChange={(e) => setFormData(prev => ({ ...prev, priority: e.target.value as any }))}
              >
                <MenuItem value="low">Low</MenuItem>
                <MenuItem value="medium">Medium</MenuItem>
                <MenuItem value="high">High</MenuItem>
                <MenuItem value="critical">Critical</MenuItem>
              </Select>
            </FormControl>

            <TextField
              label="Assigned Technician"
              value={formData.assignedTechnician || ''}
              onChange={(e) => setFormData(prev => ({ ...prev, assignedTechnician: e.target.value }))}
              fullWidth
            />

            <TextField
              label="Estimated Cost (₹)"
              type="number"
              value={formData.estimatedCost || ''}
              onChange={(e) => setFormData(prev => ({ ...prev, estimatedCost: Number(e.target.value) }))}
              fullWidth
            />

            <TextField
              label="Description"
              value={formData.description || ''}
              onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
              fullWidth
              multiline
              rows={3}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => { setIsAddDialogOpen(false); setIsEditDialogOpen(false); }}>
            Cancel
          </Button>
          <Button variant="contained" onClick={handleSaveEvent}>
            {isEditDialogOpen ? 'Save Changes' : 'Schedule'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default MaintenanceScheduleCalendar;

import React, { useEffect, useState } from 'react';
import {
  Container,
  Paper,
  TextField,
  Button,
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
  Chip,
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
} from '@mui/material';
import { MaintenanceSchedule, MaintenanceRecord } from '../types';

const MaintenanceSchedulingPage: React.FC = () => {
  const [schedules, setSchedules] = useState<MaintenanceSchedule[]>([]);
  const [records, setRecords] = useState<MaintenanceRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedSchedule, setSelectedSchedule] = useState<MaintenanceSchedule | null>(null);
  const [selectedRecord, setSelectedRecord] = useState<MaintenanceRecord | null>(null);
  const [scheduleDialogOpen, setScheduleDialogOpen] = useState(false);
  const [recordDialogOpen, setRecordDialogOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [tabValue, setTabValue] = useState(0);

  useEffect(() => {
    setLoading(true);
    setTimeout(() => {
      setSchedules([
        {
          id: '1',
          vehicleId: '1',
          type: 'ROUTINE' as any,
          intervalType: 'TIME',
          intervalValue: 365,
          lastServiceDate: new Date(Date.now() - 86400000 * 30).toISOString(),
          nextServiceDate: new Date(Date.now() + 86400000 * 30).toISOString(),
          isActive: true,
          description: 'Regular oil change service',
        },
        {
          id: '2',
          vehicleId: '2',
          type: 'TIRE_SERVICE' as any,
          intervalType: 'TIME',
          intervalValue: 180,
          lastServiceDate: new Date(Date.now() - 86400000 * 90).toISOString(),
          nextServiceDate: new Date(Date.now() - 86400000).toISOString(),
          isActive: true,
          description: 'Tire rotation service',
        },
      ]);
      setRecords([
        {
          id: '1',
          vehicleId: '1',
          type: 'INSPECTION' as any,
          status: 'COMPLETED' as any,
          priority: 'MEDIUM' as any,
          scheduledDate: new Date(Date.now() - 86400000 * 7).toISOString(),
          completedDate: new Date(Date.now() - 86400000 * 7).toISOString(),
          cost: 150.0,
          description: 'Regular inspection completed',
          technician: 'John Doe',
          createdAt: new Date(Date.now() - 86400000 * 7).toISOString(),
          updatedAt: new Date(Date.now() - 86400000 * 7).toISOString(),
        },
        {
          id: '2',
          vehicleId: '2',
          type: 'ROUTINE' as any,
          status: 'COMPLETED' as any,
          priority: 'MEDIUM' as any,
          scheduledDate: new Date(Date.now() - 86400000 * 30).toISOString(),
          completedDate: new Date(Date.now() - 86400000 * 30).toISOString(),
          cost: 75.0,
          description: 'Oil and filter change',
          technician: 'Jane Smith',
          createdAt: new Date(Date.now() - 86400000 * 30).toISOString(),
          updatedAt: new Date(Date.now() - 86400000 * 30).toISOString(),
        },
      ]);
      setLoading(false);
    }, 500);
  }, []);

  const getStatusColor = (status: string) => {
    const colors: Record<string, any> = {
      PENDING: 'info',
      OVERDUE: 'error',
      COMPLETED: 'success',
      IN_PROGRESS: 'warning',
    };
    return colors[status] || 'default';
  };

  const filteredSchedules = schedules.filter(
    (schedule) =>
      !searchTerm ||
      schedule.vehicleId?.toString().includes(searchTerm) ||
      schedule.type.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const filteredRecords = records.filter(
    (record) =>
      !searchTerm ||
      record.vehicleId?.toString().includes(searchTerm) ||
      record.type.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Calculate status based on nextServiceDate
  const overdueCount = schedules.filter(
    (s) => s.nextServiceDate && new Date(s.nextServiceDate) < new Date()
  ).length;
  const pendingCount = schedules.filter(
    (s) => s.nextServiceDate && new Date(s.nextServiceDate) >= new Date()
  ).length;

  return (
    <Container maxWidth="lg" sx={{ py: 3 }}>
      <Typography variant="h4" sx={{ mb: 3, fontWeight: 'bold' }}>
        Maintenance Scheduling
      </Typography>

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Total Schedules</Typography>
              <Typography variant="h5">{schedules.length}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Pending</Typography>
              <Typography variant="h5" sx={{ color: 'info.main' }}>
                {pendingCount}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Overdue</Typography>
              <Typography variant="h5" sx={{ color: 'error.main' }}>
                {overdueCount}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Total Records</Typography>
              <Typography variant="h5">{records.length}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Tabs */}
      <Paper sx={{ mb: 3 }}>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Box
            sx={{
              display: 'flex',
              p: 2,
              gap: 2,
              borderBottom: 1,
              borderColor: 'divider',
            }}
          >
            <Button
              variant={tabValue === 0 ? 'contained' : 'text'}
              onClick={() => setTabValue(0)}
            >
              Schedules
            </Button>
            <Button
              variant={tabValue === 1 ? 'contained' : 'text'}
              onClick={() => setTabValue(1)}
            >
              Service Records
            </Button>
          </Box>
        </Box>

        {/* Schedules Tab */}
        {tabValue === 0 && (
          <Box sx={{ p: 2 }}>
            <TextField
              fullWidth
              label="Search Schedules"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Search by vehicle ID or type"
              sx={{ mb: 2 }}
            />

            <TableContainer>
              {loading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
                  <CircularProgress />
                </Box>
              ) : (
                <Table>
                  <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
                    <TableRow>
                      <TableCell>Schedule ID</TableCell>
                      <TableCell>Vehicle ID</TableCell>
                      <TableCell>Service Type</TableCell>
                      <TableCell>Next Service Date</TableCell>
                      <TableCell>Last Service</TableCell>
                      <TableCell>Interval</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {filteredSchedules.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={8} align="center">
                          No schedules found
                        </TableCell>
                      </TableRow>
                    ) : (
                      filteredSchedules.map((schedule) => {
                        const isOverdue =
                          schedule.nextServiceDate &&
                          new Date(schedule.nextServiceDate) < new Date();
                        const status = isOverdue ? 'OVERDUE' : 'PENDING';
                        return (
                          <TableRow key={schedule.id}>
                            <TableCell>{schedule.id}</TableCell>
                            <TableCell>{schedule.vehicleId}</TableCell>
                            <TableCell>{schedule.type}</TableCell>
                            <TableCell>
                              {schedule.nextServiceDate
                                ? new Date(schedule.nextServiceDate).toLocaleDateString()
                                : 'N/A'}
                            </TableCell>
                            <TableCell>
                              {schedule.lastServiceDate
                                ? new Date(schedule.lastServiceDate).toLocaleDateString()
                                : 'N/A'}
                            </TableCell>
                            <TableCell>
                              {schedule.intervalValue}{' '}
                              {schedule.intervalType === 'TIME'
                                ? 'days'
                                : schedule.intervalType === 'MILEAGE'
                                ? 'km'
                                : 'cycles'}
                            </TableCell>
                            <TableCell>
                              <Chip
                                label={status}
                                color={getStatusColor(status) as any}
                                size="small"
                              />
                            </TableCell>
                            <TableCell>
                              <Button
                                size="small"
                                variant="outlined"
                                onClick={() => {
                                  setSelectedSchedule(schedule);
                                  setScheduleDialogOpen(true);
                                }}
                              >
                                Details
                              </Button>
                            </TableCell>
                          </TableRow>
                        );
                      })
                    )}
                  </TableBody>
                </Table>
              )}
            </TableContainer>
          </Box>
        )}

        {/* Service Records Tab */}
        {tabValue === 1 && (
          <Box sx={{ p: 2 }}>
            <TextField
              fullWidth
              label="Search Records"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Search by vehicle ID or type"
              sx={{ mb: 2 }}
            />

            <TableContainer>
              {loading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
                  <CircularProgress />
                </Box>
              ) : (
                <Table>
                  <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
                    <TableRow>
                      <TableCell>Record ID</TableCell>
                      <TableCell>Vehicle ID</TableCell>
                      <TableCell>Service Type</TableCell>
                      <TableCell>Service Date</TableCell>
                      <TableCell>Technician</TableCell>
                      <TableCell>Cost</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {filteredRecords.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={8} align="center">
                          No records found
                        </TableCell>
                      </TableRow>
                    ) : (
                      filteredRecords.map((record) => (
                        <TableRow key={record.id}>
                          <TableCell>{record.id}</TableCell>
                          <TableCell>{record.vehicleId}</TableCell>
                          <TableCell>{record.type}</TableCell>
                          <TableCell>
                            {new Date(
                              record.completedDate || record.scheduledDate
                            ).toLocaleDateString()}
                          </TableCell>
                          <TableCell>{record.technician}</TableCell>
                          <TableCell>${record.cost?.toFixed(2)}</TableCell>
                          <TableCell>
                            <Chip
                              label={record.status}
                              color={getStatusColor(record.status) as any}
                              size="small"
                            />
                          </TableCell>
                          <TableCell>
                            <Button
                              size="small"
                              variant="outlined"
                              onClick={() => {
                                setSelectedRecord(record);
                                setRecordDialogOpen(true);
                              }}
                            >
                              Details
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              )}
            </TableContainer>
          </Box>
        )}
      </Paper>

      {/* Schedule Details Dialog */}
      <Dialog
        open={scheduleDialogOpen}
        onClose={() => setScheduleDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Schedule Details</DialogTitle>
        <DialogContent dividers>
          {selectedSchedule && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography variant="body2">
                <strong>Schedule ID:</strong> {selectedSchedule.id}
              </Typography>
              <Typography variant="body2">
                <strong>Vehicle ID:</strong> {selectedSchedule.vehicleId}
              </Typography>
              <Typography variant="body2">
                <strong>Service Type:</strong> {selectedSchedule.type}
              </Typography>
              <Typography variant="body2">
                <strong>Next Service Date:</strong>{' '}
                {selectedSchedule.nextServiceDate
                  ? new Date(selectedSchedule.nextServiceDate).toLocaleDateString()
                  : 'N/A'}
              </Typography>
              <Typography variant="body2">
                <strong>Last Service:</strong>{' '}
                {selectedSchedule.lastServiceDate
                  ? new Date(selectedSchedule.lastServiceDate).toLocaleDateString()
                  : 'N/A'}
              </Typography>
              <Typography variant="body2">
                <strong>Interval:</strong> {selectedSchedule.intervalValue}{' '}
                {selectedSchedule.intervalType === 'TIME'
                  ? 'days'
                  : selectedSchedule.intervalType === 'MILEAGE'
                  ? 'km'
                  : 'cycles'}
              </Typography>
              <Typography variant="body2">
                <strong>Status:</strong>{' '}
                <Chip
                  label={
                    selectedSchedule.nextServiceDate &&
                    new Date(selectedSchedule.nextServiceDate) < new Date()
                      ? 'OVERDUE'
                      : 'PENDING'
                  }
                  color={
                    getStatusColor(
                      selectedSchedule.nextServiceDate &&
                        new Date(selectedSchedule.nextServiceDate) < new Date()
                        ? 'OVERDUE'
                        : 'PENDING'
                    ) as any
                  }
                  size="small"
                />
              </Typography>
              <Typography variant="body2">
                <strong>Description:</strong> {selectedSchedule.description}
              </Typography>
              <Typography variant="body2">
                <strong>Active:</strong> {selectedSchedule.isActive ? 'Yes' : 'No'}
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setScheduleDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Record Details Dialog */}
      <Dialog
        open={recordDialogOpen}
        onClose={() => setRecordDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Service Record Details</DialogTitle>
        <DialogContent dividers>
          {selectedRecord && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography variant="body2">
                <strong>Record ID:</strong> {selectedRecord.id}
              </Typography>
              <Typography variant="body2">
                <strong>Vehicle ID:</strong> {selectedRecord.vehicleId}
              </Typography>
              <Typography variant="body2">
                <strong>Service Type:</strong> {selectedRecord.type}
              </Typography>
              <Typography variant="body2">
                <strong>Scheduled Date:</strong>{' '}
                {new Date(selectedRecord.scheduledDate).toLocaleDateString()}
              </Typography>
              {selectedRecord.completedDate && (
                <Typography variant="body2">
                  <strong>Completed Date:</strong>{' '}
                  {new Date(selectedRecord.completedDate).toLocaleDateString()}
                </Typography>
              )}
              <Typography variant="body2">
                <strong>Technician:</strong> {selectedRecord.technician}
              </Typography>
              <Typography variant="body2">
                <strong>Cost:</strong> ${selectedRecord.cost?.toFixed(2)}
              </Typography>
              <Typography variant="body2">
                <strong>Description:</strong> {selectedRecord.description}
              </Typography>
              <Typography variant="body2">
                <strong>Priority:</strong>{' '}
                <Chip label={selectedRecord.priority} size="small" />
              </Typography>
              <Typography variant="body2">
                <strong>Status:</strong>{' '}
                <Chip
                  label={selectedRecord.status}
                  color={getStatusColor(selectedRecord.status) as any}
                  size="small"
                />
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRecordDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default MaintenanceSchedulingPage;

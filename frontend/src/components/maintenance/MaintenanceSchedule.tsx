import React, { useEffect } from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Chip, Typography, Box } from '@mui/material';
import { useAppDispatch, useAppSelector } from '../../redux/hooks';
import { fetchMaintenanceSchedule } from '../../redux/slices/maintenanceSlice';
import { formatDate } from '../../utils/formatters';

const MaintenanceSchedule: React.FC = () => {
  const dispatch = useAppDispatch();
  const { reminders: schedule, loading } = useAppSelector((state) => state.maintenance);
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);

  useEffect(() => {
    // Only fetch when user is authenticated with a companyId
    if (isAuthenticated && user?.companyId) {
      dispatch(fetchMaintenanceSchedule());
    }
  }, [dispatch, isAuthenticated, user?.companyId]);

  if (loading) {
    return <Typography>Loading...</Typography>;
  }

  if (!schedule || schedule.length === 0) {
    return (
      <Box p={2}>
        <Typography color="text.secondary">No scheduled maintenance found.</Typography>
      </Box>
    );
  }

  return (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Vehicle</TableCell>
            <TableCell>Service Type</TableCell>
            <TableCell>Next Service Date</TableCell>
            <TableCell>Next Service Odometer</TableCell>
            <TableCell>Status</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {schedule.map((item) => (
            <TableRow key={item.id}>
              <TableCell>{item.vehicleName}</TableCell>
              <TableCell>{item.type}</TableCell>
              <TableCell>{formatDate(item.dueDate)}</TableCell>
              <TableCell>{item.dueOdometer || 'N/A'} mi</TableCell>
              <TableCell><Chip label={item.status} color={item.status === 'OVERDUE' ? 'error' : item.status === 'DUE' ? 'warning' : 'success'} size="small" /></TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default MaintenanceSchedule;

import React, { useEffect } from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Chip } from '@mui/material';
import { useAppDispatch, useAppSelector } from '../../redux/hooks';
import { fetchMaintenanceSchedule } from '../../redux/slices/maintenanceSlice';
import { formatDate } from '../../utils/formatters';

const MaintenanceSchedule: React.FC = () => {
  const dispatch = useAppDispatch();
  const { reminders: schedule } = useAppSelector((state) => state.maintenance);

  useEffect(() => {
    dispatch(fetchMaintenanceSchedule());
  }, [dispatch]);

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

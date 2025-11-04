import React, { useEffect } from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Chip } from '@mui/material';
import { useAppDispatch, useAppSelector } from '@redux/hooks';
import { fetchAllSessions } from '@redux/slices/chargingSlice';
import { ChargingSessionStatus } from '../../types';
import { formatDate, formatDuration } from '@utils/formatters';

const ChargingSessionList: React.FC = () => {
  const dispatch = useAppDispatch();
  const { sessions } = useAppSelector((state) => state.charging);

  useEffect(() => {
    dispatch(fetchAllSessions(undefined));
  }, [dispatch]);

  return (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Date</TableCell>
            <TableCell>Vehicle</TableCell>
            <TableCell>Station</TableCell>
            <TableCell>Duration</TableCell>
            <TableCell>Energy</TableCell>
            <TableCell>Cost</TableCell>
            <TableCell>Status</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {sessions.map((session) => (
            <TableRow key={session.id}>
              <TableCell>{formatDate(session.startTime)}</TableCell>
              <TableCell>{session.vehicleName}</TableCell>
              <TableCell>{session.stationName}</TableCell>
              <TableCell>{session.duration ? formatDuration(session.duration) : 'In progress'}</TableCell>
              <TableCell>{session.energyDelivered.toFixed(1)} kWh</TableCell>
              <TableCell>${session.cost.toFixed(2)}</TableCell>
              <TableCell><Chip label={session.status} size="small" color={session.status === ChargingSessionStatus.COMPLETED ? 'success' : 'info'} /></TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default ChargingSessionList;

import React from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Chip } from '@mui/material';
import { useAppSelector } from '../../redux/hooks';

const UtilizationReport: React.FC = () => {
  const { utilizationReports } = useAppSelector((state) => state.analytics);

  return (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Vehicle</TableCell>
            <TableCell>Utilization</TableCell>
            <TableCell>Active Hours</TableCell>
            <TableCell>Trips</TableCell>
            <TableCell>Distance</TableCell>
            <TableCell>Efficiency</TableCell>
            <TableCell>Status</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {utilizationReports?.map((item: any) => (
            <TableRow key={item.vehicleId}>
              <TableCell>{item.vehicleName}</TableCell>
              <TableCell>{item.utilization.toFixed(1)}%</TableCell>
              <TableCell>{item.activeHours}h</TableCell>
              <TableCell>{item.trips}</TableCell>
              <TableCell>{item.distance} mi</TableCell>
              <TableCell>{item.efficiency.toFixed(1)}</TableCell>
              <TableCell><Chip label={item.status} size="small" color={item.status === 'optimal' ? 'success' : item.status === 'underutilized' ? 'warning' : 'error'} /></TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default UtilizationReport;

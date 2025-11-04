import React from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Chip } from '@mui/material';
import { useAppSelector } from '../../redux/hooks';
import { formatDate } from '../../utils/formatters';

const ServiceHistory: React.FC = () => {
  const { records } = useAppSelector((state) => state.maintenance);

  return (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Date</TableCell>
            <TableCell>Vehicle</TableCell>
            <TableCell>Service Type</TableCell>
            <TableCell>Description</TableCell>
            <TableCell>Service Provider</TableCell>
            <TableCell>Cost</TableCell>
            <TableCell>Status</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {records.map((record) => (
            <TableRow key={record.id}>
              <TableCell>{formatDate(record.scheduledDate)}</TableCell>
              <TableCell>{record.vehicleName}</TableCell>
              <TableCell><Chip label={record.type} size="small" /></TableCell>
              <TableCell>{record.description}</TableCell>
              <TableCell>{record.serviceProvider || 'N/A'}</TableCell>
              <TableCell>${record.cost?.toFixed(2) || 'N/A'}</TableCell>
              <TableCell><Chip label={record.status} size="small" color={record.status === 'COMPLETED' ? 'success' : 'warning'} /></TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default ServiceHistory;

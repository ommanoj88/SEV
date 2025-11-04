import React from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Chip } from '@mui/material';
import { useAppSelector } from '../../redux/hooks';
import { formatDate } from '../../utils/formatters';

const Payments: React.FC = () => {
  const { paymentHistory: payments } = useAppSelector((state) => state.billing);

  return (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Date</TableCell>
            <TableCell>Transaction ID</TableCell>
            <TableCell>Method</TableCell>
            <TableCell>Amount</TableCell>
            <TableCell>Status</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {payments.map((payment) => (
            <TableRow key={payment.id}>
              <TableCell>{formatDate(payment.createdAt)}</TableCell>
              <TableCell>{payment.id}</TableCell>
              <TableCell>{payment.paymentMethod}</TableCell>
              <TableCell>${payment.amount.toFixed(2)}</TableCell>
              <TableCell><Chip label={payment.status} size="small" color={payment.status === 'SUCCEEDED' ? 'success' : 'error'} /></TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default Payments;

import React from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Chip, IconButton } from '@mui/material';
import { Download as DownloadIcon } from '@mui/icons-material';
import { useAppSelector } from '../../redux/hooks';
import { formatDate } from '../../utils/formatters';

const Invoices: React.FC = () => {
  const { invoices } = useAppSelector((state) => state.billing);

  return (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Invoice #</TableCell>
            <TableCell>Date</TableCell>
            <TableCell>Description</TableCell>
            <TableCell>Amount</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {invoices.map((invoice) => (
            <TableRow key={invoice.id}>
              <TableCell>{invoice.invoiceNumber}</TableCell>
              <TableCell>{formatDate(invoice.createdAt)}</TableCell>
              <TableCell>{invoice.description}</TableCell>
              <TableCell>${invoice.amount.toFixed(2)}</TableCell>
              <TableCell><Chip label={invoice.status} size="small" color={invoice.status === 'PAID' ? 'success' : 'warning'} /></TableCell>
              <TableCell><IconButton size="small"><DownloadIcon fontSize="small" /></IconButton></TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default Invoices;

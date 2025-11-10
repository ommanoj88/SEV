import React, { useState, useEffect } from 'react';
import {
  Container,
  Card,
  CardContent,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Typography,
  Box,
  Chip,
  Stack,
  Alert,
  CircularProgress,
  Grid,
  Paper,
} from '@mui/material';
import {
  GetApp as DownloadIcon,
  Payment as PaymentIcon,
  CheckCircle as FinalizeIcon,
  Refresh as RefreshIcon,
  WarningAmber as OverdueIcon,
} from '@mui/icons-material';

/**
 * PR 18: Invoice Management Component
 * Handles invoice generation, finalization, and payment processing.
 * Supports multi-fuel vehicle billing with tier-based pricing.
 */

interface Invoice {
  id: string;
  invoiceNumber: string;
  companyId: string;
  amount: number;
  totalAmount: number;
  dueDate: string;
  paidDate?: string;
  status: 'DRAFT' | 'FINALIZED' | 'PAID' | 'OVERDUE';
  vehicleCount?: number;
  invoiceMonth?: string;
  chargesByTier?: Record<string, number>;
}

interface Payment {
  id: string;
  invoiceId: string;
  amount: number;
  paymentMethod: string;
  status: 'SUCCESS' | 'FAILED' | 'PROCESSING';
  processedAt: string;
  failureReason?: string;
}

const InvoiceManagement: React.FC = () => {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedInvoice, setSelectedInvoice] = useState<Invoice | null>(null);
  const [paymentDialogOpen, setPaymentDialogOpen] = useState(false);
  const [invoiceDetailsDialogOpen, setInvoiceDetailsDialogOpen] = useState(false);
  const [paymentAmount, setPaymentAmount] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('CREDIT_CARD');
  const [payments, setPayments] = useState<Payment[]>([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Load invoices on component mount
  useEffect(() => {
    loadInvoices();
  }, []);

  const loadInvoices = async () => {
    setLoading(true);
    try {
      // In production, replace with actual API call
      // const response = await fetch('/api/v1/billing/invoices');
      // const data = await response.json();
      // setInvoices(data);
      setLoading(false);
    } catch (err) {
      setError('Failed to load invoices');
      setLoading(false);
    }
  };

  const handleFinalizeInvoice = async (invoiceId: string) => {
    setLoading(true);
    try {
      // In production, replace with actual API call
      // const response = await fetch(`/api/v1/billing/invoices/${invoiceId}/finalize`, {
      //   method: 'POST',
      // });
      // if (response.ok) {
      setSuccess('Invoice finalized successfully');
      loadInvoices();
      setTimeout(() => setSuccess(''), 3000);
      // }
    } catch (err) {
      setError('Failed to finalize invoice');
    } finally {
      setLoading(false);
    }
  };

  const handleProcessPayment = async () => {
    if (!selectedInvoice || !paymentAmount) {
      setError('Please fill in all required fields');
      return;
    }

    setLoading(true);
    try {
      // In production, replace with actual API call
      // const response = await fetch(`/api/v1/billing/invoices/${selectedInvoice.id}/pay`, {
      //   method: 'POST',
      //   headers: { 'Content-Type': 'application/json' },
      //   body: JSON.stringify({
      //     invoiceId: selectedInvoice.id,
      //     amount: parseFloat(paymentAmount),
      //     paymentMethod,
      //   }),
      // });
      // if (response.ok) {
      setSuccess(`Payment of ₹${paymentAmount} processed successfully`);
      setPaymentDialogOpen(false);
      loadInvoices();
      // }
    } catch (err) {
      setError('Failed to process payment');
    } finally {
      setLoading(false);
    }
  };

  const handleRetryPayment = async (invoiceId: string) => {
    setLoading(true);
    try {
      // In production, replace with actual API call
      // const response = await fetch(`/api/v1/billing/invoices/${invoiceId}/retry-payment`, {
      //   method: 'POST',
      //   params: { paymentMethod: 'CREDIT_CARD' },
      // });
      setSuccess('Payment retry initiated');
      loadInvoices();
    } catch (err) {
      setError('Failed to retry payment');
    } finally {
      setLoading(false);
    }
  };

  const handleLoadPaymentHistory = async (invoiceId: string) => {
    try {
      // In production, replace with actual API call
      // const response = await fetch(`/api/v1/billing/invoices/${invoiceId}/payment-history`);
      // const data = await response.json();
      // setPayments(data);
      setSelectedInvoice(invoices.find((inv) => inv.id === invoiceId) || null);
      setInvoiceDetailsDialogOpen(true);
    } catch (err) {
      setError('Failed to load payment history');
    }
  };

  const getStatusColor = (status: string) => {
    const colors: Record<string, 'default' | 'primary' | 'secondary' | 'error' | 'warning' | 'info' | 'success'> = {
      DRAFT: 'default',
      FINALIZED: 'info',
      PAID: 'success',
      OVERDUE: 'error',
    };
    return colors[status] || 'default';
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'PAID':
        return <CheckCircle sx={{ mr: 1, color: 'green' }} />;
      case 'OVERDUE':
        return <OverdueIcon sx={{ mr: 1, color: 'red' }} />;
      default:
        return null;
    }
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h4" gutterBottom sx={{ mb: 3 }}>
        Invoice Management (PR 18)
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>
          {success}
        </Alert>
      )}

      {loading && <CircularProgress />}

      {!loading && (
        <TableContainer component={Card}>
          <Table>
            <TableHead>
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                <TableCell>Invoice #</TableCell>
                <TableCell>Month</TableCell>
                <TableCell align="right">Amount</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Due Date</TableCell>
                <TableCell align="center">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {invoices.map((invoice) => (
                <TableRow key={invoice.id} sx={{ '&:hover': { backgroundColor: '#f9f9f9' } }}>
                  <TableCell>{invoice.invoiceNumber}</TableCell>
                  <TableCell>{invoice.invoiceMonth || 'N/A'}</TableCell>
                  <TableCell align="right">₹{invoice.totalAmount.toLocaleString()}</TableCell>
                  <TableCell>
                    <Chip label={invoice.status} color={getStatusColor(invoice.status)} size="small" />
                  </TableCell>
                  <TableCell>{invoice.dueDate}</TableCell>
                  <TableCell align="center">
                    <Stack direction="row" spacing={1} justifyContent="center">
                      {invoice.status === 'DRAFT' && (
                        <Button
                          size="small"
                          variant="contained"
                          color="primary"
                          startIcon={<FinalizeIcon />}
                          onClick={() => handleFinalizeInvoice(invoice.id)}
                        >
                          Finalize
                        </Button>
                      )}
                      {invoice.status !== 'PAID' && (
                        <Button
                          size="small"
                          variant="contained"
                          color="success"
                          startIcon={<PaymentIcon />}
                          onClick={() => {
                            setSelectedInvoice(invoice);
                            setPaymentAmount(invoice.totalAmount.toString());
                            setPaymentDialogOpen(true);
                          }}
                        >
                          Pay
                        </Button>
                      )}
                      <Button size="small" onClick={() => handleLoadPaymentHistory(invoice.id)}>
                        Details
                      </Button>
                    </Stack>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Payment Dialog */}
      <Dialog open={paymentDialogOpen} onClose={() => setPaymentDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Process Payment</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          {selectedInvoice && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography variant="body2">
                Invoice: <strong>{selectedInvoice.invoiceNumber}</strong>
              </Typography>
              <Typography variant="body2">
                Amount Due: <strong>₹{selectedInvoice.totalAmount.toLocaleString()}</strong>
              </Typography>
              <TextField
                label="Payment Amount"
                type="number"
                value={paymentAmount}
                onChange={(e) => setPaymentAmount(e.target.value)}
                fullWidth
                inputProps={{ step: '0.01' }}
              />
              <FormControl fullWidth>
                <InputLabel>Payment Method</InputLabel>
                <Select value={paymentMethod} label="Payment Method" onChange={(e) => setPaymentMethod(e.target.value)}>
                  <MenuItem value="CREDIT_CARD">Credit Card</MenuItem>
                  <MenuItem value="DEBIT_CARD">Debit Card</MenuItem>
                  <MenuItem value="BANK_TRANSFER">Bank Transfer</MenuItem>
                  <MenuItem value="UPI">UPI</MenuItem>
                </Select>
              </FormControl>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPaymentDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleProcessPayment} variant="contained" color="success" disabled={loading}>
            {loading ? 'Processing...' : 'Pay Now'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Invoice Details Dialog */}
      <Dialog
        open={invoiceDetailsDialogOpen}
        onClose={() => setInvoiceDetailsDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Invoice Details & Payment History</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          {selectedInvoice && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Paper sx={{ p: 2, backgroundColor: '#f5f5f5' }}>
                <Typography variant="body2">
                  Invoice Number: <strong>{selectedInvoice.invoiceNumber}</strong>
                </Typography>
                <Typography variant="body2">
                  Status: <strong>{selectedInvoice.status}</strong>
                </Typography>
                <Typography variant="body2">
                  Total Amount: <strong>₹{selectedInvoice.totalAmount.toLocaleString()}</strong>
                </Typography>
                <Typography variant="body2">
                  Vehicles: <strong>{selectedInvoice.vehicleCount || 'N/A'}</strong>
                </Typography>
              </Paper>

              {selectedInvoice.chargesByTier && Object.keys(selectedInvoice.chargesByTier).length > 0 && (
                <Box>
                  <Typography variant="subtitle2" sx={{ mb: 1 }}>
                    Charges by Tier:
                  </Typography>
                  {Object.entries(selectedInvoice.chargesByTier).map(([tier, amount]) => (
                    <Typography key={tier} variant="body2">
                      {tier}: ₹{Number(amount).toLocaleString()}
                    </Typography>
                  ))}
                </Box>
              )}

              {payments.length > 0 && (
                <Box>
                  <Typography variant="subtitle2" sx={{ mb: 1 }}>
                    Payment History:
                  </Typography>
                  {payments.map((payment) => (
                    <Paper key={payment.id} sx={{ p: 1, mb: 1 }}>
                      <Typography variant="body2">
                        {payment.paymentMethod}: ₹{payment.amount.toLocaleString()} - {payment.status}
                      </Typography>
                      <Typography variant="caption" color="textSecondary">
                        {payment.processedAt}
                      </Typography>
                    </Paper>
                  ))}
                </Box>
              )}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setInvoiceDetailsDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default InvoiceManagement;

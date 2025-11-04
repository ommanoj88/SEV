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
import { Invoice, Payment } from '../types';

const InvoicingPage: React.FC = () => {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedInvoice, setSelectedInvoice] = useState<Invoice | null>(null);
  const [selectedPayment, setSelectedPayment] = useState<Payment | null>(null);
  const [invoiceDialogOpen, setInvoiceDialogOpen] = useState(false);
  const [paymentDialogOpen, setPaymentDialogOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [tabValue, setTabValue] = useState(0);

  useEffect(() => {
    setLoading(true);
    setTimeout(() => {
      setInvoices([
        {
          id: '1',
          invoiceNumber: 'INV-2024-001',
          fleetId: '1',
          subscriptionId: '1',
          amount: 5000.0,
          amountPaid: 5000.0,
          amountDue: 0,
          status: 'PAID',
          dueDate: new Date(Date.now() - 86400000 * 15).toISOString().split('T')[0],
          paidAt: new Date(Date.now() - 86400000 * 10).toISOString().split('T')[0],
          description: 'Monthly vehicle maintenance',
          billingPeriod: { start: '2024-09-01', end: '2024-09-30' },
          lineItems: [],
          subtotal: 5000,
          total: 5000,
          currency: 'USD',
          createdAt: new Date(Date.now() - 86400000 * 30).toISOString(),
        },
        {
          id: '2',
          invoiceNumber: 'INV-2024-002',
          fleetId: '2',
          subscriptionId: '2',
          amount: 3500.0,
          amountPaid: 0,
          amountDue: 3500,
          status: 'OPEN',
          dueDate: new Date(Date.now() + 86400000 * 10).toISOString().split('T')[0],
          description: 'Quarterly analytics report',
          billingPeriod: { start: '2024-10-01', end: '2024-10-31' },
          lineItems: [],
          subtotal: 3500,
          total: 3500,
          currency: 'USD',
          createdAt: new Date(Date.now() - 86400000 * 5).toISOString(),
        },
        {
          id: '3',
          invoiceNumber: 'INV-2024-003',
          fleetId: '1',
          subscriptionId: '3',
          amount: 2000.0,
          amountPaid: 0,
          amountDue: 2000,
          status: 'OPEN',
          dueDate: new Date(Date.now() - 86400000 * 30).toISOString().split('T')[0],
          description: 'Subscription renewal',
          billingPeriod: { start: '2024-08-01', end: '2024-08-31' },
          lineItems: [],
          subtotal: 2000,
          total: 2000,
          currency: 'USD',
          createdAt: new Date(Date.now() - 86400000 * 60).toISOString(),
        },
      ]);
      setPayments([
        {
          id: '1',
          invoiceId: '1',
          fleetId: '1',
          amount: 5000.0,
          currency: 'USD',
          paymentMethod: 'CARD',
          status: 'SUCCEEDED',
          last4: '4242',
          brand: 'visa',
          createdAt: new Date(Date.now() - 86400000 * 10).toISOString().split('T')[0],
          updatedAt: new Date(Date.now() - 86400000 * 10).toISOString().split('T')[0],
        },
        {
          id: '2',
          invoiceId: '3',
          fleetId: '1',
          amount: 1000.0,
          currency: 'USD',
          paymentMethod: 'BANK_TRANSFER',
          status: 'PENDING',
          createdAt: new Date(Date.now() - 86400000 * 5).toISOString().split('T')[0],
          updatedAt: new Date(Date.now() - 86400000 * 5).toISOString().split('T')[0],
        },
      ]);
      setLoading(false);
    }, 500);
  }, []);

  const getStatusColor = (status: string) => {
    const colors: Record<string, any> = {
      PAID: 'success',
      PENDING: 'warning',
      OVERDUE: 'error',
      CANCELLED: 'error',
      COMPLETED: 'success',
      FAILED: 'error',
    };
    return colors[status] || 'default';
  };

  const filteredInvoices = invoices.filter(
    (invoice) =>
      !searchTerm ||
      invoice.invoiceNumber.includes(searchTerm) ||
      invoice.fleetId?.toString().includes(searchTerm)
  );

  const filteredPayments = payments.filter(
    (payment) =>
      !searchTerm ||
      payment.fleetId?.toString().includes(searchTerm) ||
      payment.invoiceId?.toString().includes(searchTerm)
  );

  const totalRevenue = invoices.reduce((sum, inv) => sum + inv.amount, 0);
  const paidAmount = invoices
    .filter((inv) => inv.status === 'PAID')
    .reduce((sum, inv) => sum + inv.amount, 0);
  const pendingAmount = invoices
    .filter((inv) => inv.status === 'OPEN' || inv.status === 'DRAFT')
    .reduce((sum, inv) => sum + inv.amount, 0);
  const overdueCount = invoices.filter((inv) => inv.amountDue > 0 && inv.status !== 'PAID').length;

  return (
    <Container maxWidth="lg" sx={{ py: 3 }}>
      <Typography variant="h4" sx={{ mb: 3, fontWeight: 'bold' }}>
        Invoicing & Payments
      </Typography>

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Total Revenue</Typography>
              <Typography variant="h5">${totalRevenue.toFixed(2)}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Paid</Typography>
              <Typography variant="h5" sx={{ color: 'success.main' }}>
                ${paidAmount.toFixed(2)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Pending/Overdue</Typography>
              <Typography variant="h5" sx={{ color: 'error.main' }}>
                ${pendingAmount.toFixed(2)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary">Overdue Invoices</Typography>
              <Typography variant="h5" sx={{ color: 'error.main' }}>
                {overdueCount}
              </Typography>
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
              Invoices ({invoices.length})
            </Button>
            <Button
              variant={tabValue === 1 ? 'contained' : 'text'}
              onClick={() => setTabValue(1)}
            >
              Payments ({payments.length})
            </Button>
          </Box>
        </Box>

        {/* Invoices Tab */}
        {tabValue === 0 && (
          <Box sx={{ p: 2 }}>
            <TextField
              fullWidth
              label="Search Invoices"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Search by invoice number or fleet ID"
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
                      <TableCell>Invoice Number</TableCell>
                      <TableCell>Fleet ID</TableCell>
                      <TableCell>Amount</TableCell>
                      <TableCell>Issue Date</TableCell>
                      <TableCell>Due Date</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {filteredInvoices.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={7} align="center">
                          No invoices found
                        </TableCell>
                      </TableRow>
                    ) : (
                      filteredInvoices.map((invoice) => (
                        <TableRow key={invoice.id}>
                          <TableCell>{invoice.invoiceNumber}</TableCell>
                          <TableCell>{invoice.fleetId}</TableCell>
                          <TableCell>${invoice.amount.toFixed(2)}</TableCell>
                          <TableCell>
                            {new Date(invoice.createdAt).toLocaleDateString()}
                          </TableCell>
                          <TableCell>
                            {new Date(invoice.dueDate).toLocaleDateString()}
                          </TableCell>
                          <TableCell>
                            <Chip
                              label={invoice.status}
                              color={getStatusColor(invoice.status) as any}
                              size="small"
                            />
                          </TableCell>
                          <TableCell>
                            <Box sx={{ display: 'flex', gap: 1 }}>
                              <Button
                                size="small"
                                variant="outlined"
                                onClick={() => {
                                  setSelectedInvoice(invoice);
                                  setInvoiceDialogOpen(true);
                                }}
                              >
                                View
                              </Button>
                              {invoice.status !== 'PAID' && (
                                <Button size="small" variant="contained" color="success">
                                  Pay
                                </Button>
                              )}
                            </Box>
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

        {/* Payments Tab */}
        {tabValue === 1 && (
          <Box sx={{ p: 2 }}>
            <TextField
              fullWidth
              label="Search Payments"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Search by invoice ID or fleet ID"
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
                      <TableCell>Invoice ID</TableCell>
                      <TableCell>Amount</TableCell>
                      <TableCell>Payment Method</TableCell>
                      <TableCell>Payment Date</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {filteredPayments.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={6} align="center">
                          No payments found
                        </TableCell>
                      </TableRow>
                    ) : (
                      filteredPayments.map((payment) => (
                        <TableRow key={payment.id}>
                          <TableCell>{payment.invoiceId}</TableCell>
                          <TableCell>${payment.amount.toFixed(2)}</TableCell>
                          <TableCell>{payment.paymentMethod}</TableCell>
                          <TableCell>
                            {payment.createdAt
                              ? new Date(payment.createdAt).toLocaleDateString()
                              : '-'}
                          </TableCell>
                          <TableCell>
                            <Chip
                              label={payment.status}
                              color={getStatusColor(payment.status) as any}
                              size="small"
                            />
                          </TableCell>
                          <TableCell>
                            <Button
                              size="small"
                              variant="outlined"
                              onClick={() => {
                                setSelectedPayment(payment);
                                setPaymentDialogOpen(true);
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

      {/* Invoice Details Dialog */}
      <Dialog
        open={invoiceDialogOpen}
        onClose={() => setInvoiceDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Invoice Details</DialogTitle>
        <DialogContent dividers>
          {selectedInvoice && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography variant="body2">
                <strong>Invoice Number:</strong> {selectedInvoice.invoiceNumber}
              </Typography>
              <Typography variant="body2">
                <strong>Fleet ID:</strong> {selectedInvoice.fleetId}
              </Typography>
              <Typography variant="body2">
                <strong>Amount:</strong> ${selectedInvoice.amount.toFixed(2)}
              </Typography>
              <Typography variant="body2">
                <strong>Status:</strong>{' '}
                <Chip
                  label={selectedInvoice.status}
                  color={getStatusColor(selectedInvoice.status) as any}
                  size="small"
                />
              </Typography>
              <Typography variant="body2">
                <strong>Issue Date:</strong>{' '}
                {new Date(selectedInvoice.createdAt).toLocaleDateString()}
              </Typography>
              <Typography variant="body2">
                <strong>Due Date:</strong>{' '}
                {new Date(selectedInvoice.dueDate).toLocaleDateString()}
              </Typography>
              {selectedInvoice.paidAt && (
                <Typography variant="body2">
                  <strong>Paid Date:</strong>{' '}
                  {new Date(selectedInvoice.paidAt).toLocaleDateString()}
                </Typography>
              )}
              <Typography variant="body2">
                <strong>Description:</strong> {selectedInvoice.description}
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setInvoiceDialogOpen(false)}>Close</Button>
          <Button variant="contained" color="success">
            Download PDF
          </Button>
        </DialogActions>
      </Dialog>

      {/* Payment Details Dialog */}
      <Dialog
        open={paymentDialogOpen}
        onClose={() => setPaymentDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Payment Details</DialogTitle>
        <DialogContent dividers>
          {selectedPayment && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography variant="body2">
                <strong>Invoice ID:</strong> {selectedPayment.invoiceId}
              </Typography>
              <Typography variant="body2">
                <strong>Amount:</strong> ${selectedPayment.amount.toFixed(2)}
              </Typography>
              <Typography variant="body2">
                <strong>Payment Method:</strong> {selectedPayment.paymentMethod}
              </Typography>
              <Typography variant="body2">
                <strong>Status:</strong>{' '}
                <Chip
                  label={selectedPayment.status}
                  color={getStatusColor(selectedPayment.status) as any}
                  size="small"
                />
              </Typography>
              {selectedPayment.createdAt && (
                <Typography variant="body2">
                  <strong>Payment Date:</strong>{' '}
                  {new Date(selectedPayment.createdAt).toLocaleDateString()}
                </Typography>
              )}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPaymentDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default InvoicingPage;

import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
  Alert,
  Tabs,
  Tab,
  IconButton,
  Badge,
} from '@mui/material';
import {
  Add as AddIcon,
  Receipt as ReceiptIcon,
  CheckCircle as ApproveIcon,
  Cancel as RejectIcon,
  AttachMoney as MoneyIcon,
  TrendingUp as TrendingUpIcon,
  Visibility as ViewIcon,
} from '@mui/icons-material';
import { format } from 'date-fns';
import { PieChart, Pie, Cell, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip } from 'recharts';

interface Expense {
  id: number;
  expenseNumber: string;
  category: string;
  description: string;
  amount: number;
  currency: string;
  expenseDate: string;
  status: string;
  submittedBy?: string;
  vehicleId?: number;
  driverId?: number;
  vendorName?: string;
  paymentMethod?: string;
  isReimbursable: boolean;
  reimbursed: boolean;
}

const ExpenseManagementPage: React.FC = () => {
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [filteredExpenses, setFilteredExpenses] = useState<Expense[]>([]);
  const [activeTab, setActiveTab] = useState(0);
  const [addDialogOpen, setAddDialogOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [formData, setFormData] = useState({
    category: 'FUEL',
    subcategory: '',
    description: '',
    amount: '',
    expenseDate: format(new Date(), 'yyyy-MM-dd'),
    entityType: 'VEHICLE',
    entityId: '',
    vehicleId: '',
    driverId: '',
    vendorName: '',
    vendorContact: '',
    receiptNumber: '',
    paymentMethod: 'CASH',
    isReimbursable: false,
    notes: '',
  });

  const expenseCategories = [
    { value: 'FUEL', label: 'Fuel' },
    { value: 'CHARGING', label: 'Charging' },
    { value: 'MAINTENANCE', label: 'Maintenance' },
    { value: 'REPAIRS', label: 'Repairs' },
    { value: 'PARTS', label: 'Parts' },
    { value: 'TOLLS', label: 'Tolls' },
    { value: 'PARKING', label: 'Parking' },
    { value: 'INSURANCE', label: 'Insurance' },
    { value: 'TAXES', label: 'Taxes' },
    { value: 'DRIVER_WAGES', label: 'Driver Wages' },
    { value: 'DRIVER_ALLOWANCE', label: 'Driver Allowance' },
    { value: 'FINES', label: 'Fines' },
    { value: 'PERMITS', label: 'Permits' },
    { value: 'CLEANING', label: 'Cleaning' },
    { value: 'OTHER', label: 'Other' },
  ];

  const paymentMethods = [
    'CASH',
    'CARD',
    'UPI',
    'BANK_TRANSFER',
    'CHEQUE',
    'OTHER',
  ];

  const filterExpenses = useCallback(() => {
    let filtered = expenses;
    
    switch (activeTab) {
      case 0: // All
        filtered = expenses;
        break;
      case 1: // Pending
        filtered = expenses.filter(exp => exp.status === 'PENDING_APPROVAL');
        break;
      case 2: // Approved
        filtered = expenses.filter(exp => exp.status === 'APPROVED');
        break;
      case 3: // Rejected
        filtered = expenses.filter(exp => exp.status === 'REJECTED');
        break;
    }
    
    setFilteredExpenses(filtered);
  }, [expenses, activeTab]);

  useEffect(() => {
    fetchExpenses();
  }, []);

  useEffect(() => {
    filterExpenses();
  }, [expenses, activeTab, filterExpenses]);

  const fetchExpenses = async () => {
    try {
      // Mock data for now
      const mockExpenses: Expense[] = [
        {
          id: 1,
          expenseNumber: 'EXP-20250111-000001',
          category: 'FUEL',
          description: 'Diesel refill',
          amount: 2500.00,
          currency: 'INR',
          expenseDate: '2025-01-10',
          status: 'PENDING_APPROVAL',
          submittedBy: 'driver1',
          vehicleId: 1,
          vendorName: 'Indian Oil',
          paymentMethod: 'CARD',
          isReimbursable: false,
          reimbursed: false,
        },
        {
          id: 2,
          expenseNumber: 'EXP-20250111-000002',
          category: 'MAINTENANCE',
          description: 'Oil change and filter replacement',
          amount: 1200.00,
          currency: 'INR',
          expenseDate: '2025-01-09',
          status: 'APPROVED',
          submittedBy: 'driver2',
          vehicleId: 2,
          vendorName: 'AutoCare Service',
          paymentMethod: 'CASH',
          isReimbursable: true,
          reimbursed: false,
        },
      ];
      setExpenses(mockExpenses);
    } catch (err) {
      setError('Failed to fetch expenses');
    }
  };

  const handleAddExpense = async () => {
    try {
      const expenseData = {
        category: formData.category,
        subcategory: formData.subcategory,
        description: formData.description,
        amount: parseFloat(formData.amount),
        currency: 'INR',
        expenseDate: formData.expenseDate,
        entityType: formData.entityType,
        entityId: parseInt(formData.entityId),
        vehicleId: formData.vehicleId ? parseInt(formData.vehicleId) : null,
        driverId: formData.driverId ? parseInt(formData.driverId) : null,
        vendorName: formData.vendorName,
        vendorContact: formData.vendorContact,
        receiptNumber: formData.receiptNumber,
        paymentMethod: formData.paymentMethod,
        isReimbursable: formData.isReimbursable,
        notes: formData.notes,
        status: 'DRAFT',
        submittedBy: 'current-user',
        createdBy: 'current-user',
      };

      const response = await fetch('/api/expenses', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(expenseData),
      });

      if (response.ok) {
        setSuccess('Expense added successfully');
        setAddDialogOpen(false);
        fetchExpenses();
        resetForm();
      } else {
        setError('Failed to add expense');
      }
    } catch (err) {
      setError('Error adding expense');
    }
  };

  const handleApproveExpense = async (id: number) => {
    try {
      const response = await fetch(`/api/expenses/${id}/approve`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ approvedBy: 'current-user' }),
      });

      if (response.ok) {
        setSuccess('Expense approved successfully');
        fetchExpenses();
      } else {
        setError('Failed to approve expense');
      }
    } catch (err) {
      setError('Error approving expense');
    }
  };

  const handleRejectExpense = async (id: number) => {
    const reason = window.prompt('Enter rejection reason:');
    if (!reason) return;

    try {
      const response = await fetch(`/api/expenses/${id}/reject`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ rejectedBy: 'current-user', reason }),
      });

      if (response.ok) {
        setSuccess('Expense rejected');
        fetchExpenses();
      } else {
        setError('Failed to reject expense');
      }
    } catch (err) {
      setError('Error rejecting expense');
    }
  };

  const resetForm = () => {
    setFormData({
      category: 'FUEL',
      subcategory: '',
      description: '',
      amount: '',
      expenseDate: format(new Date(), 'yyyy-MM-dd'),
      entityType: 'VEHICLE',
      entityId: '',
      vehicleId: '',
      driverId: '',
      vendorName: '',
      vendorContact: '',
      receiptNumber: '',
      paymentMethod: 'CASH',
      isReimbursable: false,
      notes: '',
    });
  };

  const getStatusChip = (status: string) => {
    const statusConfig: Record<string, { label: string; color: any }> = {
      'DRAFT': { label: 'Draft', color: 'default' },
      'PENDING_APPROVAL': { label: 'Pending', color: 'warning' },
      'APPROVED': { label: 'Approved', color: 'success' },
      'REJECTED': { label: 'Rejected', color: 'error' },
      'PAID': { label: 'Paid', color: 'info' },
      'CANCELLED': { label: 'Cancelled', color: 'default' },
    };

    const config = statusConfig[status] || { label: status, color: 'default' };
    return <Chip label={config.label} color={config.color} size="small" />;
  };

  // Calculate summary statistics
  const totalExpenses = expenses.reduce((sum, exp) => sum + exp.amount, 0);
  const pendingCount = expenses.filter(exp => exp.status === 'PENDING_APPROVAL').length;
  const approvedTotal = expenses.filter(exp => exp.status === 'APPROVED').reduce((sum, exp) => sum + exp.amount, 0);
  const reimbursableTotal = expenses.filter(exp => exp.isReimbursable && !exp.reimbursed).reduce((sum, exp) => sum + exp.amount, 0);

  // Prepare chart data
  const categoryData = expenseCategories.map(cat => ({
    name: cat.label,
    value: expenses.filter(exp => exp.category === cat.value).reduce((sum, exp) => sum + exp.amount, 0),
  })).filter(item => item.value > 0);

  const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8', '#82CA9D', '#FFC658'];

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Expense Management</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setAddDialogOpen(true)}
        >
          Add Expense
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess(null)}>{success}</Alert>}

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Box>
                  <Typography color="textSecondary" gutterBottom>Total Expenses</Typography>
                  <Typography variant="h4">₹{totalExpenses.toLocaleString()}</Typography>
                </Box>
                <MoneyIcon sx={{ fontSize: 40, color: 'primary.main' }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: 'warning.light' }}>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Box>
                  <Typography color="textSecondary" gutterBottom>Pending Approval</Typography>
                  <Typography variant="h4">{pendingCount}</Typography>
                </Box>
                <ReceiptIcon sx={{ fontSize: 40 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: 'success.light' }}>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Box>
                  <Typography color="textSecondary" gutterBottom>Approved</Typography>
                  <Typography variant="h4">₹{approvedTotal.toLocaleString()}</Typography>
                </Box>
                <ApproveIcon sx={{ fontSize: 40 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: 'info.light' }}>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Box>
                  <Typography color="textSecondary" gutterBottom>Reimbursable</Typography>
                  <Typography variant="h4">₹{reimbursableTotal.toLocaleString()}</Typography>
                </Box>
                <TrendingUpIcon sx={{ fontSize: 40 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Charts */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>Expenses by Category</Typography>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={categoryData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {categoryData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(value: number) => `₹${value.toLocaleString()}`} />
              </PieChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>Top Expense Categories</Typography>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={categoryData.slice(0, 5)}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip formatter={(value: number) => `₹${value.toLocaleString()}`} />
                <Bar dataKey="value" fill="#8884d8" />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
      </Grid>

      {/* Tabs */}
      <Paper sx={{ mb: 2 }}>
        <Tabs value={activeTab} onChange={(e, newValue) => setActiveTab(newValue)}>
          <Tab label="All Expenses" />
          <Tab 
            label={
              <Badge badgeContent={pendingCount} color="warning">
                Pending Approval
              </Badge>
            } 
          />
          <Tab label="Approved" />
          <Tab label="Rejected" />
        </Tabs>
      </Paper>

      {/* Expenses Table */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Expense #</TableCell>
              <TableCell>Date</TableCell>
              <TableCell>Category</TableCell>
              <TableCell>Description</TableCell>
              <TableCell>Vendor</TableCell>
              <TableCell>Amount</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredExpenses.map((expense) => (
              <TableRow key={expense.id}>
                <TableCell>{expense.expenseNumber}</TableCell>
                <TableCell>
                  {format(new Date(expense.expenseDate), 'MMM dd, yyyy')}
                </TableCell>
                <TableCell>
                  <Chip label={expense.category.replace(/_/g, ' ')} size="small" />
                </TableCell>
                <TableCell>{expense.description}</TableCell>
                <TableCell>{expense.vendorName || '-'}</TableCell>
                <TableCell>₹{expense.amount.toLocaleString()}</TableCell>
                <TableCell>{getStatusChip(expense.status)}</TableCell>
                <TableCell>
                  {expense.status === 'PENDING_APPROVAL' && (
                    <>
                      <IconButton size="small" onClick={() => handleApproveExpense(expense.id)} color="success">
                        <ApproveIcon />
                      </IconButton>
                      <IconButton size="small" onClick={() => handleRejectExpense(expense.id)} color="error">
                        <RejectIcon />
                      </IconButton>
                    </>
                  )}
                  <IconButton size="small">
                    <ViewIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Add Expense Dialog */}
      <Dialog open={addDialogOpen} onClose={() => setAddDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>Add New Expense</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Category</InputLabel>
                <Select
                  value={formData.category}
                  onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                >
                  {expenseCategories.map(cat => (
                    <MenuItem key={cat.value} value={cat.value}>{cat.label}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Subcategory"
                value={formData.subcategory}
                onChange={(e) => setFormData({ ...formData, subcategory: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Description"
                required
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Amount (₹)"
                type="number"
                required
                value={formData.amount}
                onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Expense Date"
                type="date"
                required
                InputLabelProps={{ shrink: true }}
                value={formData.expenseDate}
                onChange={(e) => setFormData({ ...formData, expenseDate: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Vehicle ID"
                type="number"
                value={formData.vehicleId}
                onChange={(e) => setFormData({ ...formData, vehicleId: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Driver ID"
                type="number"
                value={formData.driverId}
                onChange={(e) => setFormData({ ...formData, driverId: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Vendor Name"
                value={formData.vendorName}
                onChange={(e) => setFormData({ ...formData, vendorName: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Receipt Number"
                value={formData.receiptNumber}
                onChange={(e) => setFormData({ ...formData, receiptNumber: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Payment Method</InputLabel>
                <Select
                  value={formData.paymentMethod}
                  onChange={(e) => setFormData({ ...formData, paymentMethod: e.target.value })}
                >
                  {paymentMethods.map(method => (
                    <MenuItem key={method} value={method}>{method.replace(/_/g, ' ')}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Reimbursable</InputLabel>
                <Select
                  value={formData.isReimbursable ? 'yes' : 'no'}
                  onChange={(e) => setFormData({ ...formData, isReimbursable: e.target.value === 'yes' })}
                >
                  <MenuItem value="no">No</MenuItem>
                  <MenuItem value="yes">Yes</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Notes"
                multiline
                rows={3}
                value={formData.notes}
                onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAddDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleAddExpense} variant="contained">
            Add Expense
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ExpenseManagementPage;

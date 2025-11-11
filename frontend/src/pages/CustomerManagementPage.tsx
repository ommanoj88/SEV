import React, { useState, useEffect } from 'react';
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
  IconButton,
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
  Rating,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Person as PersonIcon,
  Business as BusinessIcon,
  Feedback as FeedbackIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  LocationOn as LocationIcon,
} from '@mui/icons-material';
import { format } from 'date-fns';
import axios from 'axios';

interface Customer {
  id: number;
  customerCode: string;
  customerName: string;
  customerType: string;
  primaryContactName?: string;
  primaryPhone?: string;
  secondaryPhone?: string;
  email?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country: string;
  gstin?: string;
  pan?: string;
  isActive: boolean;
  creditLimit?: number;
  outstandingBalance: number;
  serviceRating?: number;
  totalDeliveries: number;
  successfulDeliveries: number;
  failedDeliveries: number;
  createdAt: string;
}

interface CustomerFeedback {
  id: number;
  customerId: number;
  rating: number;
  feedbackText?: string;
  feedbackCategory?: string;
  isAddressed: boolean;
  responseText?: string;
  respondedBy?: string;
  createdAt: string;
}

const CustomerManagementPage: React.FC = () => {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [filteredCustomers, setFilteredCustomers] = useState<Customer[]>([]);
  const [feedbackList, setFeedbackList] = useState<CustomerFeedback[]>([]);
  const [activeTab, setActiveTab] = useState(0);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [feedbackDialogOpen, setFeedbackDialogOpen] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [formData, setFormData] = useState({
    customerCode: '',
    customerName: '',
    customerType: 'INDIVIDUAL',
    primaryContactName: '',
    primaryPhone: '',
    secondaryPhone: '',
    email: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    postalCode: '',
    country: 'India',
    gstin: '',
    pan: '',
    creditLimit: '',
    specialInstructions: '',
  });

  const [feedbackFormData, setFeedbackFormData] = useState({
    rating: 5,
    feedbackText: '',
    feedbackCategory: 'DELIVERY_QUALITY',
  });

  useEffect(() => {
    fetchCustomers();
    fetchFeedback();
  }, []);

  useEffect(() => {
    filterCustomers();
  }, [customers, activeTab]);

  const fetchCustomers = async () => {
    try {
      const response = await axios.get('/api/customers');
      setCustomers(response.data);
    } catch (err) {
      setError('Failed to fetch customers');
      console.error('Error fetching customers:', err);
    }
  };

  const fetchFeedback = async () => {
    try {
      const response = await axios.get('/api/customers/feedback/unaddressed');
      setFeedbackList(response.data);
    } catch (err) {
      console.error('Error fetching feedback:', err);
    }
  };

  const filterCustomers = () => {
    let filtered = [...customers];
    
    switch (activeTab) {
      case 0: // All
        break;
      case 1: // Active
        filtered = customers.filter(c => c.isActive);
        break;
      case 2: // Business
        filtered = customers.filter(c => c.customerType === 'BUSINESS');
        break;
      case 3: // Top Rated
        filtered = customers.filter(c => c.serviceRating && c.serviceRating >= 4);
        break;
    }
    
    setFilteredCustomers(filtered);
  };

  const handleOpenDialog = (customer?: Customer) => {
    if (customer) {
      setFormData({
        customerCode: customer.customerCode,
        customerName: customer.customerName,
        customerType: customer.customerType,
        primaryContactName: customer.primaryContactName || '',
        primaryPhone: customer.primaryPhone || '',
        secondaryPhone: customer.secondaryPhone || '',
        email: customer.email || '',
        addressLine1: customer.addressLine1 || '',
        addressLine2: customer.addressLine2 || '',
        city: customer.city || '',
        state: customer.state || '',
        postalCode: customer.postalCode || '',
        country: customer.country,
        gstin: customer.gstin || '',
        pan: customer.pan || '',
        creditLimit: customer.creditLimit?.toString() || '',
        specialInstructions: '',
      });
      setSelectedCustomer(customer);
    } else {
      setFormData({
        customerCode: '',
        customerName: '',
        customerType: 'INDIVIDUAL',
        primaryContactName: '',
        primaryPhone: '',
        secondaryPhone: '',
        email: '',
        addressLine1: '',
        addressLine2: '',
        city: '',
        state: '',
        postalCode: '',
        country: 'India',
        gstin: '',
        pan: '',
        creditLimit: '',
        specialInstructions: '',
      });
      setSelectedCustomer(null);
    }
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setSelectedCustomer(null);
  };

  const handleSubmit = async () => {
    try {
      const data = {
        customerCode: formData.customerCode,
        customerName: formData.customerName,
        customerType: formData.customerType,
        primaryContactName: formData.primaryContactName,
        primaryPhone: formData.primaryPhone,
        secondaryPhone: formData.secondaryPhone,
        email: formData.email,
        addressLine1: formData.addressLine1,
        addressLine2: formData.addressLine2,
        city: formData.city,
        state: formData.state,
        postalCode: formData.postalCode,
        country: formData.country,
        gstin: formData.gstin,
        pan: formData.pan,
        creditLimit: formData.creditLimit ? parseFloat(formData.creditLimit) : null,
        specialInstructions: formData.specialInstructions,
      };

      if (selectedCustomer) {
        await axios.put(`/api/customers/${selectedCustomer.id}`, data);
        setSuccess('Customer updated successfully');
      } else {
        await axios.post('/api/customers', data);
        setSuccess('Customer created successfully');
      }
      
      handleCloseDialog();
      fetchCustomers();
    } catch (err) {
      setError('Failed to save customer');
      console.error('Error saving customer:', err);
    }
  };

  const handleDeleteCustomer = async (customerId: number) => {
    if (!window.confirm('Are you sure you want to delete this customer?')) return;

    try {
      await axios.delete(`/api/customers/${customerId}`);
      setSuccess('Customer deleted successfully');
      fetchCustomers();
    } catch (err) {
      setError('Failed to delete customer');
      console.error('Error deleting customer:', err);
    }
  };

  const handleOpenFeedbackDialog = (customer: Customer) => {
    setSelectedCustomer(customer);
    setFeedbackDialogOpen(true);
  };

  const handleSubmitFeedback = async () => {
    if (!selectedCustomer) return;

    try {
      const data = {
        customerId: selectedCustomer.id,
        rating: feedbackFormData.rating,
        feedbackText: feedbackFormData.feedbackText,
        feedbackCategory: feedbackFormData.feedbackCategory,
      };

      await axios.post('/api/customers/feedback', data);
      setSuccess('Feedback submitted successfully');
      setFeedbackDialogOpen(false);
      fetchCustomers();
      fetchFeedback();
      
      // Reset form
      setFeedbackFormData({
        rating: 5,
        feedbackText: '',
        feedbackCategory: 'DELIVERY_QUALITY',
      });
    } catch (err) {
      setError('Failed to submit feedback');
      console.error('Error submitting feedback:', err);
    }
  };

  const stats = {
    total: customers.length,
    active: customers.filter(c => c.isActive).length,
    business: customers.filter(c => c.customerType === 'BUSINESS').length,
    topRated: customers.filter(c => c.serviceRating && c.serviceRating >= 4).length,
    unaddressedFeedback: feedbackList.length,
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">
          <PersonIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
          Customer Management
        </Typography>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          Add Customer
        </Button>
      </Box>

      {error && (
        <Alert severity="error" onClose={() => setError(null)} sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" onClose={() => setSuccess(null)} sx={{ mb: 2 }}>
          {success}
        </Alert>
      )}

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={2.4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>Total Customers</Typography>
              <Typography variant="h4">{stats.total}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>Active</Typography>
              <Typography variant="h4" color="success.main">{stats.active}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>Business</Typography>
              <Typography variant="h4" color="info.main">{stats.business}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>Top Rated</Typography>
              <Typography variant="h4" color="warning.main">{stats.topRated}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>Pending Feedback</Typography>
              <Typography variant="h4" color="error.main">{stats.unaddressedFeedback}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Tabs */}
      <Paper sx={{ mb: 2 }}>
        <Tabs value={activeTab} onChange={(e, v) => setActiveTab(v)}>
          <Tab label="All Customers" />
          <Tab label="Active" />
          <Tab label="Business" />
          <Tab label="Top Rated" />
        </Tabs>
      </Paper>

      {/* Customers Table */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Customer Code</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Contact</TableCell>
              <TableCell>City</TableCell>
              <TableCell>Rating</TableCell>
              <TableCell>Deliveries</TableCell>
              <TableCell>Balance</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredCustomers.map((customer) => (
              <TableRow key={customer.id}>
                <TableCell>{customer.customerCode}</TableCell>
                <TableCell>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    {customer.customerType === 'BUSINESS' ? <BusinessIcon sx={{ mr: 1 }} /> : <PersonIcon sx={{ mr: 1 }} />}
                    {customer.customerName}
                  </Box>
                </TableCell>
                <TableCell>{customer.customerType}</TableCell>
                <TableCell>
                  <Box>
                    {customer.primaryPhone && (
                      <Box sx={{ display: 'flex', alignItems: 'center', fontSize: '0.875rem' }}>
                        <PhoneIcon sx={{ fontSize: 16, mr: 0.5 }} />
                        {customer.primaryPhone}
                      </Box>
                    )}
                    {customer.email && (
                      <Box sx={{ display: 'flex', alignItems: 'center', fontSize: '0.875rem' }}>
                        <EmailIcon sx={{ fontSize: 16, mr: 0.5 }} />
                        {customer.email}
                      </Box>
                    )}
                  </Box>
                </TableCell>
                <TableCell>{customer.city || '-'}</TableCell>
                <TableCell>
                  {customer.serviceRating ? (
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <Rating value={customer.serviceRating} readOnly size="small" precision={0.1} />
                      <Typography variant="caption" sx={{ ml: 1 }}>
                        ({customer.serviceRating.toFixed(1)})
                      </Typography>
                    </Box>
                  ) : (
                    '-'
                  )}
                </TableCell>
                <TableCell>
                  {customer.totalDeliveries} ({customer.successfulDeliveries} success)
                </TableCell>
                <TableCell>
                  {customer.outstandingBalance > 0 ? (
                    <Chip label={`₹${customer.outstandingBalance.toFixed(2)}`} color="warning" size="small" />
                  ) : (
                    '-'
                  )}
                </TableCell>
                <TableCell>
                  <Chip
                    label={customer.isActive ? 'Active' : 'Inactive'}
                    color={customer.isActive ? 'success' : 'default'}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <IconButton size="small" onClick={() => handleOpenDialog(customer)} title="Edit">
                    <EditIcon />
                  </IconButton>
                  <IconButton size="small" onClick={() => handleOpenFeedbackDialog(customer)} title="Add Feedback" color="primary">
                    <FeedbackIcon />
                  </IconButton>
                  <IconButton size="small" onClick={() => handleDeleteCustomer(customer.id)} title="Delete" color="error">
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Customer Dialog */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedCustomer ? 'Edit Customer' : 'Add New Customer'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Customer Code"
                value={formData.customerCode}
                onChange={(e) => setFormData({ ...formData, customerCode: e.target.value })}
                required
                disabled={!!selectedCustomer}
              />
            </Grid>
            <Grid item xs={6}>
              <FormControl fullWidth>
                <InputLabel>Customer Type</InputLabel>
                <Select
                  value={formData.customerType}
                  onChange={(e) => setFormData({ ...formData, customerType: e.target.value })}
                  label="Customer Type"
                >
                  <MenuItem value="INDIVIDUAL">Individual</MenuItem>
                  <MenuItem value="BUSINESS">Business</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Customer Name"
                value={formData.customerName}
                onChange={(e) => setFormData({ ...formData, customerName: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={12}>
              <Typography variant="subtitle1" sx={{ mt: 1, mb: 1 }}>Contact Information</Typography>
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Primary Contact Name"
                value={formData.primaryContactName}
                onChange={(e) => setFormData({ ...formData, primaryContactName: e.target.value })}
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Primary Phone"
                value={formData.primaryPhone}
                onChange={(e) => setFormData({ ...formData, primaryPhone: e.target.value })}
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Secondary Phone"
                value={formData.secondaryPhone}
                onChange={(e) => setFormData({ ...formData, secondaryPhone: e.target.value })}
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Email"
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <Typography variant="subtitle1" sx={{ mt: 1, mb: 1 }}>Address</Typography>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Address Line 1"
                value={formData.addressLine1}
                onChange={(e) => setFormData({ ...formData, addressLine1: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Address Line 2"
                value={formData.addressLine2}
                onChange={(e) => setFormData({ ...formData, addressLine2: e.target.value })}
              />
            </Grid>
            <Grid item xs={4}>
              <TextField
                fullWidth
                label="City"
                value={formData.city}
                onChange={(e) => setFormData({ ...formData, city: e.target.value })}
              />
            </Grid>
            <Grid item xs={4}>
              <TextField
                fullWidth
                label="State"
                value={formData.state}
                onChange={(e) => setFormData({ ...formData, state: e.target.value })}
              />
            </Grid>
            <Grid item xs={4}>
              <TextField
                fullWidth
                label="Postal Code"
                value={formData.postalCode}
                onChange={(e) => setFormData({ ...formData, postalCode: e.target.value })}
              />
            </Grid>
            {formData.customerType === 'BUSINESS' && (
              <>
                <Grid item xs={12}>
                  <Typography variant="subtitle1" sx={{ mt: 1, mb: 1 }}>Business Details</Typography>
                </Grid>
                <Grid item xs={6}>
                  <TextField
                    fullWidth
                    label="GSTIN"
                    value={formData.gstin}
                    onChange={(e) => setFormData({ ...formData, gstin: e.target.value })}
                  />
                </Grid>
                <Grid item xs={6}>
                  <TextField
                    fullWidth
                    label="PAN"
                    value={formData.pan}
                    onChange={(e) => setFormData({ ...formData, pan: e.target.value })}
                  />
                </Grid>
              </>
            )}
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Credit Limit"
                type="number"
                value={formData.creditLimit}
                onChange={(e) => setFormData({ ...formData, creditLimit: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Special Instructions"
                value={formData.specialInstructions}
                onChange={(e) => setFormData({ ...formData, specialInstructions: e.target.value })}
                multiline
                rows={2}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button onClick={handleSubmit} variant="contained" color="primary">
            {selectedCustomer ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Feedback Dialog */}
      <Dialog open={feedbackDialogOpen} onClose={() => setFeedbackDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add Customer Feedback</DialogTitle>
        <DialogContent>
          {selectedCustomer && (
            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle2" color="textSecondary">
                Customer: {selectedCustomer.customerName}
              </Typography>
            </Box>
          )}
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <Typography component="legend">Rating</Typography>
              <Rating
                value={feedbackFormData.rating}
                onChange={(e, value) => setFeedbackFormData({ ...feedbackFormData, rating: value || 5 })}
                size="large"
              />
            </Grid>
            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>Category</InputLabel>
                <Select
                  value={feedbackFormData.feedbackCategory}
                  onChange={(e) => setFeedbackFormData({ ...feedbackFormData, feedbackCategory: e.target.value })}
                  label="Category"
                >
                  <MenuItem value="DELIVERY_QUALITY">Delivery Quality</MenuItem>
                  <MenuItem value="DRIVER_BEHAVIOR">Driver Behavior</MenuItem>
                  <MenuItem value="TIMELINESS">Timeliness</MenuItem>
                  <MenuItem value="COMMUNICATION">Communication</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Feedback"
                value={feedbackFormData.feedbackText}
                onChange={(e) => setFeedbackFormData({ ...feedbackFormData, feedbackText: e.target.value })}
                multiline
                rows={4}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setFeedbackDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleSubmitFeedback} variant="contained" color="primary">
            Submit
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default CustomerManagementPage;

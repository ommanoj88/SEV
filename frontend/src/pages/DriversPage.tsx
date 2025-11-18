import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Button,
  Grid,
  Card,
  CardContent,
  Chip,
  IconButton,
  Menu,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  DialogContentText,
  Avatar,
  LinearProgress,
  Paper,
  Tab,
  Tabs,
} from '@mui/material';
import {
  Add,
  Refresh,
  Person as PersonIcon,
  MoreVert as MoreVertIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  DirectionsCar,
  EmojiEvents as TrophyIcon,
  AssignmentInd,
  TrendingUp,
  CheckCircle,
} from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '../redux/hooks';
import { fetchAllDrivers, deleteDriver, selectAllDrivers, selectDriverLoading } from '../redux/slices/driverSlice';
import { selectUser } from '../redux/slices/authSlice';
import { RootState } from '../redux/store';
import { Driver, DriverStatus } from '../types';
import { toast } from 'react-toastify';
import DriverLeaderboard from '../components/drivers/DriverLeaderboard';
import AssignDriver from '../components/drivers/AssignDriver';
import DriverFormDialog, { DriverFormData } from '../components/drivers/DriverFormDialog';
import { driverService } from '../services/driverService';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
  return (
    <div role="tabpanel" hidden={value !== index}>
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  );
};

const DriversPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const user = useAppSelector(selectUser);
  const isAuthenticated = useAppSelector((state: RootState) => state.auth.isAuthenticated);
  const drivers = useAppSelector(selectAllDrivers);
  const loading = useAppSelector(selectDriverLoading);

  // State
  const [activeTab, setActiveTab] = useState(0);
  const [selectedDriver, setSelectedDriver] = useState<Driver | null>(null);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [addDialogOpen, setAddDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // Menu state
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [menuDriver, setMenuDriver] = useState<Driver | null>(null);

  useEffect(() => {
    if (isAuthenticated) {
      dispatch(fetchAllDrivers(undefined));
    }
  }, [dispatch, isAuthenticated]);

  // Calculate KPIs
  const totalDrivers = drivers.length;
  const activeDrivers = drivers.filter(d => d.status === DriverStatus.ACTIVE).length;
  const onTripDrivers = drivers.filter(d => d.status === DriverStatus.ON_TRIP).length;
  const avgPerformance = drivers.length > 0
    ? Math.round(drivers.reduce((sum, d) => sum + (d.performanceScore || 0), 0) / drivers.length)
    : 0;

  // Handlers
  const handleRefresh = () => {
    dispatch(fetchAllDrivers(undefined));
  };

  const handleCardClick = (driver: Driver) => {
    setSelectedDriver(driver);
    setDetailDialogOpen(true);
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>, driver: Driver) => {
    event.stopPropagation();
    setAnchorEl(event.currentTarget);
    setMenuDriver(driver);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setMenuDriver(null);
  };

  const handleView = () => {
    if (menuDriver) {
      setSelectedDriver(menuDriver);
      setDetailDialogOpen(true);
    }
    handleMenuClose();
  };

  const handleEdit = (driver?: Driver) => {
    const driverToEdit = driver || menuDriver;
    if (driverToEdit) {
      setSelectedDriver(driverToEdit);
      setEditDialogOpen(true);
      setDetailDialogOpen(false);
    }
    handleMenuClose();
  };

  const handleDeleteClick = (driver?: Driver) => {
    const driverToDelete = driver || menuDriver;
    if (driverToDelete) {
      setSelectedDriver(driverToDelete);
      setDeleteDialogOpen(true);
      setDetailDialogOpen(false);
    }
    handleMenuClose();
  };

  const handleDeleteConfirm = async () => {
    if (!selectedDriver?.id) return;

    try {
      setSubmitting(true);
      await dispatch(deleteDriver(Number(selectedDriver.id))).unwrap();
      toast.success('Driver deleted successfully!');
      setDeleteDialogOpen(false);
      setSelectedDriver(null);
    } catch (error: any) {
      toast.error(error.message || 'Failed to delete driver');
    } finally {
      setSubmitting(false);
    }
  };

  const handleAddDriver = async (data: DriverFormData) => {
    if (!user?.companyId) {
      toast.error('Company ID not found');
      return;
    }

    try {
      setSubmitting(true);
      await driverService.createDriver(user.companyId, data);
      toast.success('Driver created successfully!');
      setAddDialogOpen(false);
      dispatch(fetchAllDrivers(undefined));
    } catch (error: any) {
      toast.error(error.message || 'Failed to create driver');
      throw error;
    } finally {
      setSubmitting(false);
    }
  };

  const handleUpdateDriver = async (data: DriverFormData) => {
    if (!selectedDriver?.id) return;

    try {
      setSubmitting(true);
      await driverService.updateDriver(Number(selectedDriver.id), data);
      toast.success('Driver updated successfully!');
      setEditDialogOpen(false);
      setSelectedDriver(null);
      dispatch(fetchAllDrivers(undefined));
    } catch (error: any) {
      toast.error(error.message || 'Failed to update driver');
      throw error;
    } finally {
      setSubmitting(false);
    }
  };

  const getStatusColor = (status: DriverStatus) => {
    switch (status) {
      case DriverStatus.ACTIVE:
        return 'success.main';
      case DriverStatus.ON_TRIP:
        return 'info.main';
      case DriverStatus.SUSPENDED:
        return 'error.main';
      case DriverStatus.INACTIVE:
        return 'grey.500';
      default:
        return 'grey.500';
    }
  };

  if (loading && drivers.length === 0) {
    return <LinearProgress />;
  }

  return (
    <Box>
      {/* Header Section */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" fontWeight={600} gutterBottom>
            Driver Management
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Manage and monitor your drivers - Click on any driver to view details
          </Typography>
        </Box>
        <Box display="flex" gap={2}>
          <Button variant="outlined" startIcon={<Refresh />} onClick={handleRefresh} disabled={loading}>
            Refresh
          </Button>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => setAddDialogOpen(true)}
            disabled={loading || !isAuthenticated}
          >
            Add Driver
          </Button>
        </Box>
      </Box>

      {/* KPI Cards */}
      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: 'primary.main', color: 'white' }}>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center">
                <Box>
                  <Typography variant="body2" sx={{ opacity: 0.9 }}>
                    Total Drivers
                  </Typography>
                  <Typography variant="h4" fontWeight={600}>
                    {totalDrivers}
                  </Typography>
                </Box>
                <PersonIcon sx={{ fontSize: 48, opacity: 0.3 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: 'success.main', color: 'white' }}>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center">
                <Box>
                  <Typography variant="body2" sx={{ opacity: 0.9 }}>
                    Active Drivers
                  </Typography>
                  <Typography variant="h4" fontWeight={600}>
                    {activeDrivers}
                  </Typography>
                </Box>
                <CheckCircle sx={{ fontSize: 48, opacity: 0.3 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: 'info.main', color: 'white' }}>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center">
                <Box>
                  <Typography variant="body2" sx={{ opacity: 0.9 }}>
                    On Trip
                  </Typography>
                  <Typography variant="h4" fontWeight={600}>
                    {onTripDrivers}
                  </Typography>
                </Box>
                <DirectionsCar sx={{ fontSize: 48, opacity: 0.3 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: 'warning.main', color: 'white' }}>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center">
                <Box>
                  <Typography variant="body2" sx={{ opacity: 0.9 }}>
                    Avg Performance
                  </Typography>
                  <Typography variant="h4" fontWeight={600}>
                    {avgPerformance}%
                  </Typography>
                </Box>
                <TrendingUp sx={{ fontSize: 48, opacity: 0.3 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Tabs Section */}
      <Paper sx={{ mb: 3 }}>
        <Tabs value={activeTab} onChange={(_, newValue) => setActiveTab(newValue)}>
          <Tab icon={<PersonIcon />} label="All Drivers" iconPosition="start" />
          <Tab icon={<TrophyIcon />} label="Leaderboard" iconPosition="start" />
          <Tab icon={<AssignmentInd />} label="Assign Driver" iconPosition="start" />
        </Tabs>
      </Paper>

      {/* Tab Panels */}
      <TabPanel value={activeTab} index={0}>
        {drivers.length === 0 ? (
          <Box textAlign="center" py={8}>
            <PersonIcon sx={{ fontSize: 80, color: 'text.secondary', mb: 2 }} />
            <Typography variant="h6" color="text.secondary">
              No drivers found
            </Typography>
            <Button
              variant="contained"
              startIcon={<Add />}
              sx={{ mt: 2 }}
              onClick={() => setAddDialogOpen(true)}
              disabled={!isAuthenticated}
            >
              Add Your First Driver
            </Button>
          </Box>
        ) : (
          <Grid container spacing={3}>
            {drivers.map((driver) => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={driver.id}>
                <Card
                  sx={{
                    height: '100%',
                    cursor: 'pointer',
                    '&:hover': { boxShadow: 6 },
                    transition: 'box-shadow 0.3s',
                  }}
                  onClick={() => handleCardClick(driver)}
                >
                  <CardContent>
                    <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                      <Box display="flex" alignItems="center" gap={2}>
                        <Avatar sx={{ width: 48, height: 48, bgcolor: 'primary.main' }}>
                          {driver.firstName?.[0]}{driver.lastName?.[0]}
                        </Avatar>
                        <Box>
                          <Typography variant="h6" fontWeight={600} fontSize="1rem">
                            {driver.firstName} {driver.lastName}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {driver.licenseNumber}
                          </Typography>
                        </Box>
                      </Box>
                      <IconButton
                        size="small"
                        onClick={(e) => handleMenuOpen(e, driver)}
                      >
                        <MoreVertIcon fontSize="small" />
                      </IconButton>
                    </Box>

                    <Box mb={2}>
                      <Chip
                        label={driver.status}
                        size="small"
                        sx={{ bgcolor: getStatusColor(driver.status), color: 'white' }}
                      />
                    </Box>

                    <Box display="flex" flexDirection="column" gap={1}>
                      <Box display="flex" justifyContent="space-between">
                        <Typography variant="caption" color="text.secondary">
                          Performance
                        </Typography>
                        <Typography variant="caption" fontWeight={600}>
                          {driver.performanceScore || 0}%
                        </Typography>
                      </Box>
                      <LinearProgress
                        variant="determinate"
                        value={driver.performanceScore || 0}
                        sx={{
                          height: 6,
                          borderRadius: 3,
                          bgcolor: 'grey.200',
                          '& .MuiLinearProgress-bar': {
                            bgcolor: driver.performanceScore >= 80 ? 'success.main' :
                                    driver.performanceScore >= 60 ? 'warning.main' : 'error.main',
                          },
                        }}
                      />

                      <Box display="flex" justifyContent="space-between" mt={1}>
                        <Typography variant="caption" color="text.secondary">
                          Trips: {driver.totalTrips || 0}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          Rating: {(driver.averageRating || 0).toFixed(1)}⭐
                        </Typography>
                      </Box>

                      {driver.email && (
                        <Typography variant="caption" color="text.secondary" noWrap>
                          {driver.email}
                        </Typography>
                      )}
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        )}
      </TabPanel>

      <TabPanel value={activeTab} index={1}>
        <DriverLeaderboard />
      </TabPanel>

      <TabPanel value={activeTab} index={2}>
        <AssignDriver />
      </TabPanel>

      {/* Action Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
        transformOrigin={{ horizontal: 'right', vertical: 'top' }}
        anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
      >
        <MenuItem onClick={handleView}>
          <ViewIcon fontSize="small" sx={{ mr: 1 }} />
          View Details
        </MenuItem>
        <MenuItem onClick={() => handleEdit()}>
          <EditIcon fontSize="small" sx={{ mr: 1 }} />
          Edit Driver
        </MenuItem>
        <MenuItem onClick={() => handleDeleteClick()} sx={{ color: 'error.main' }}>
          <DeleteIcon fontSize="small" sx={{ mr: 1 }} />
          Delete Driver
        </MenuItem>
      </Menu>

      {/* Driver Detail Dialog */}
      <Dialog
        open={detailDialogOpen}
        onClose={() => {
          setDetailDialogOpen(false);
          setSelectedDriver(null);
        }}
        maxWidth="md"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: 2,
            maxHeight: '90vh',
          },
        }}
      >
        <DialogTitle
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            pb: 2,
            borderBottom: 1,
            borderColor: 'divider',
          }}
        >
          <Box display="flex" alignItems="center" gap={2}>
            <Avatar sx={{ width: 48, height: 48, bgcolor: 'primary.main' }}>
              {selectedDriver?.firstName?.[0]}{selectedDriver?.lastName?.[0]}
            </Avatar>
            <Box>
              <Typography variant="h6" fontWeight={600}>
                {selectedDriver?.firstName} {selectedDriver?.lastName}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {selectedDriver?.licenseNumber}
              </Typography>
            </Box>
          </Box>
          <Box display="flex" alignItems="center" gap={1}>
            <IconButton onClick={() => handleEdit(selectedDriver || undefined)} color="primary" size="small">
              <EditIcon />
            </IconButton>
            <IconButton onClick={() => handleDeleteClick(selectedDriver || undefined)} color="error" size="small">
              <DeleteIcon />
            </IconButton>
            <IconButton onClick={() => setDetailDialogOpen(false)} size="small">
              <ViewIcon />
            </IconButton>
          </Box>
        </DialogTitle>

        <DialogContent sx={{ px: 3, py: 3 }}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Box display="flex" gap={1} mb={2}>
                <Chip
                  label={selectedDriver?.status}
                  size="small"
                  sx={{ bgcolor: getStatusColor(selectedDriver?.status || DriverStatus.INACTIVE), color: 'white' }}
                />
                <Chip label={`${selectedDriver?.totalTrips || 0} Trips`} size="small" variant="outlined" />
                <Chip label={`${(selectedDriver?.averageRating || 0).toFixed(1)} ⭐`} size="small" variant="outlined" />
              </Box>
            </Grid>

            <Grid item xs={6}>
              <Typography variant="caption" color="text.secondary">
                Email
              </Typography>
              <Typography variant="body1" fontWeight={600}>
                {selectedDriver?.email || 'N/A'}
              </Typography>
            </Grid>

            <Grid item xs={6}>
              <Typography variant="caption" color="text.secondary">
                Phone
              </Typography>
              <Typography variant="body1" fontWeight={600}>
                {selectedDriver?.phone || 'N/A'}
              </Typography>
            </Grid>

            <Grid item xs={6}>
              <Typography variant="caption" color="text.secondary">
                License Number
              </Typography>
              <Typography variant="body1" fontWeight={600}>
                {selectedDriver?.licenseNumber || 'N/A'}
              </Typography>
            </Grid>

            <Grid item xs={6}>
              <Typography variant="caption" color="text.secondary">
                License Expiry
              </Typography>
              <Typography variant="body1" fontWeight={600}>
                {selectedDriver?.licenseExpiry || 'N/A'}
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <Typography variant="caption" color="text.secondary">
                Performance Score
              </Typography>
              <Box display="flex" alignItems="center" gap={2} mt={1}>
                <LinearProgress
                  variant="determinate"
                  value={selectedDriver?.performanceScore || 0}
                  sx={{
                    flex: 1,
                    height: 8,
                    borderRadius: 4,
                    bgcolor: 'grey.200',
                    '& .MuiLinearProgress-bar': {
                      bgcolor: (selectedDriver?.performanceScore || 0) >= 80 ? 'success.main' :
                              (selectedDriver?.performanceScore || 0) >= 60 ? 'warning.main' : 'error.main',
                    },
                  }}
                />
                <Typography variant="h6" fontWeight={600}>
                  {selectedDriver?.performanceScore || 0}%
                </Typography>
              </Box>
            </Grid>

            <Grid item xs={6}>
              <Typography variant="caption" color="text.secondary">
                Total Distance
              </Typography>
              <Typography variant="h6" fontWeight={600}>
                {selectedDriver?.totalDistance?.toLocaleString() || 0} km
              </Typography>
            </Grid>

            <Grid item xs={6}>
              <Typography variant="caption" color="text.secondary">
                Driving Hours
              </Typography>
              <Typography variant="h6" fontWeight={600}>
                {selectedDriver?.totalDrivingHours || 0} hrs
              </Typography>
            </Grid>

            {selectedDriver?.address && (
              <Grid item xs={12}>
                <Typography variant="caption" color="text.secondary">
                  Address
                </Typography>
                <Typography variant="body1" fontWeight={600}>
                  {selectedDriver.address}
                </Typography>
              </Grid>
            )}

            {selectedDriver?.emergencyContact && (
              <Grid item xs={12}>
                <Typography variant="caption" color="text.secondary">
                  Emergency Contact
                </Typography>
                <Typography variant="body2" fontWeight={500}>
                  {selectedDriver.emergencyContact.name} ({selectedDriver.emergencyContact.relationship})
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {selectedDriver.emergencyContact.phone}
                </Typography>
              </Grid>
            )}
          </Grid>
        </DialogContent>

        <DialogActions sx={{ px: 3, py: 2, borderTop: 1, borderColor: 'divider' }}>
          <Button onClick={() => handleDeleteClick(selectedDriver || undefined)} color="error" startIcon={<DeleteIcon />}>
            Delete
          </Button>
          <Button onClick={() => handleEdit(selectedDriver || undefined)} color="primary" variant="outlined" startIcon={<EditIcon />}>
            Edit
          </Button>
          <Button onClick={() => setDetailDialogOpen(false)} variant="contained">
            Close
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        maxWidth="xs"
        fullWidth
      >
        <DialogTitle>Delete Driver</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete{' '}
            <strong>
              {selectedDriver?.firstName} {selectedDriver?.lastName} ({selectedDriver?.licenseNumber})
            </strong>
            ? This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)} disabled={submitting}>
            Cancel
          </Button>
          <Button onClick={handleDeleteConfirm} color="error" variant="contained" disabled={submitting}>
            {submitting ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Add Driver Dialog */}
      <DriverFormDialog
        open={addDialogOpen}
        onClose={() => setAddDialogOpen(false)}
        onSubmit={handleAddDriver}
        companyId={user?.companyId || 0}
        loading={submitting}
      />

      {/* Edit Driver Dialog */}
      <DriverFormDialog
        open={editDialogOpen}
        onClose={() => {
          setEditDialogOpen(false);
          setSelectedDriver(null);
        }}
        onSubmit={handleUpdateDriver}
        driver={selectedDriver}
        companyId={user?.companyId || 0}
        loading={submitting}
      />
    </Box>
  );
};

export default DriversPage;

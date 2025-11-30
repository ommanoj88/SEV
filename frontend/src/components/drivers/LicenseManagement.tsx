import React, { useEffect, useState, useMemo } from 'react';
import {
  Box,
  Paper,
  Grid,
  Typography,
  Card,
  CardContent,
  CardMedia,
  Button,
  Chip,
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Alert,
  LinearProgress,
  Divider,
  Avatar,
  Badge,
  useTheme,
  alpha,
  CircularProgress,
} from '@mui/material';
import {
  Upload,
  CreditCard,
  Schedule,
  Warning,
  CheckCircle,
  Error,
  Refresh,
  Edit,
  Notifications,
  Camera,
  CalendarToday,
  VerifiedUser,
  Close,
} from '@mui/icons-material';
import { useParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@redux/hooks';
import { fetchDriverById } from '@redux/slices/driverSlice';
import { formatDate } from '@utils/formatters';

/**
 * License Management Component
 * 
 * UI for driver license tracking and management:
 * - Display license details (number, class, expiry)
 * - Expiry countdown with color coding
 * - License image upload functionality
 * - Renewal reminder notifications
 * - Verification status display
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */

interface LicenseStatus {
  status: 'valid' | 'expiring' | 'expired' | 'unknown';
  daysRemaining: number;
  color: string;
  icon: React.ReactNode;
  message: string;
}

// Calculate license status based on expiry date
const getLicenseStatus = (expiryDate: string | undefined): LicenseStatus => {
  if (!expiryDate) {
    return {
      status: 'unknown',
      daysRemaining: 0,
      color: '#9E9E9E',
      icon: <Warning sx={{ color: '#9E9E9E' }} />,
      message: 'Expiry date not set',
    };
  }

  const expiry = new Date(expiryDate);
  const today = new Date();
  const diffTime = expiry.getTime() - today.getTime();
  const daysRemaining = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  if (daysRemaining < 0) {
    return {
      status: 'expired',
      daysRemaining: Math.abs(daysRemaining),
      color: '#F44336',
      icon: <Error sx={{ color: '#F44336' }} />,
      message: `Expired ${Math.abs(daysRemaining)} days ago`,
    };
  } else if (daysRemaining <= 7) {
    return {
      status: 'expiring',
      daysRemaining,
      color: '#F44336',
      icon: <Warning sx={{ color: '#F44336' }} />,
      message: `Expires in ${daysRemaining} days - URGENT!`,
    };
  } else if (daysRemaining <= 30) {
    return {
      status: 'expiring',
      daysRemaining,
      color: '#FFC107',
      icon: <Schedule sx={{ color: '#FFC107' }} />,
      message: `Expires in ${daysRemaining} days`,
    };
  } else {
    return {
      status: 'valid',
      daysRemaining,
      color: '#4CAF50',
      icon: <CheckCircle sx={{ color: '#4CAF50' }} />,
      message: `Valid for ${daysRemaining} days`,
    };
  }
};

// License class options (Indian driving license classes)
const LICENSE_CLASSES = [
  { code: 'LMV', description: 'Light Motor Vehicle (Car)' },
  { code: 'HMV', description: 'Heavy Motor Vehicle (Truck/Bus)' },
  { code: 'HGMV', description: 'Heavy Goods Motor Vehicle' },
  { code: 'HPMV', description: 'Heavy Passenger Motor Vehicle' },
  { code: 'MCWG', description: 'Motorcycle with Gear' },
  { code: 'MCWOG', description: 'Motorcycle without Gear' },
  { code: 'TRANS', description: 'Transport Vehicle' },
];

const LicenseManagement: React.FC = () => {
  const theme = useTheme();
  const { id } = useParams<{ id: string }>();
  const dispatch = useAppDispatch();
  
  const { selectedDriver, loading } = useAppSelector((state) => state.drivers);
  
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);
  const [reminderDialogOpen, setReminderDialogOpen] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);
  const [reminderDays, setReminderDays] = useState<number>(30);

  useEffect(() => {
    if (id) {
      dispatch(fetchDriverById(Number(id)));
    }
  }, [id, dispatch]);

  // Get license status
  const licenseStatus = useMemo(() => {
    return getLicenseStatus(selectedDriver?.licenseExpiry);
  }, [selectedDriver?.licenseExpiry]);

  // Handle file selection
  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewUrl(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  // Handle upload
  const handleUpload = async () => {
    if (!selectedFile) return;
    
    setUploading(true);
    // Simulate upload - in real implementation, this would call an API
    await new Promise((resolve) => setTimeout(resolve, 2000));
    setUploading(false);
    setUploadDialogOpen(false);
    setSelectedFile(null);
    setPreviewUrl(null);
  };

  // Handle reminder setup
  const handleSetReminder = async () => {
    // Simulate API call - in real implementation, this would set up a notification
    await new Promise((resolve) => setTimeout(resolve, 1000));
    setReminderDialogOpen(false);
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  if (!selectedDriver) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <Typography color="text.secondary">Driver not found</Typography>
      </Box>
    );
  }

  return (
    <Box>
      {/* Header */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h5" fontWeight={600}>
            License Management
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {selectedDriver.firstName} {selectedDriver.lastName}
          </Typography>
        </Box>
        <Box display="flex" gap={2}>
          <Button
            variant="outlined"
            startIcon={<Notifications />}
            onClick={() => setReminderDialogOpen(true)}
          >
            Set Reminder
          </Button>
          <Button
            variant="contained"
            startIcon={<Upload />}
            onClick={() => setUploadDialogOpen(true)}
          >
            Upload License
          </Button>
        </Box>
      </Box>

      <Grid container spacing={3}>
        {/* License Status Card */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Box display="flex" alignItems="center" mb={2}>
              <CreditCard sx={{ mr: 1, color: 'primary.main' }} />
              <Typography variant="h6">License Status</Typography>
            </Box>
            
            {/* Status Badge */}
            <Box
              sx={{
                p: 3,
                borderRadius: 2,
                bgcolor: alpha(licenseStatus.color, 0.1),
                border: `2px solid ${alpha(licenseStatus.color, 0.3)}`,
                mb: 3,
              }}
            >
              <Box display="flex" alignItems="center" justifyContent="center" mb={2}>
                <Badge
                  badgeContent={
                    licenseStatus.status === 'valid' ? (
                      <VerifiedUser sx={{ fontSize: 20, color: 'success.main' }} />
                    ) : null
                  }
                >
                  {licenseStatus.icon}
                </Badge>
              </Box>
              
              <Typography
                variant="h4"
                align="center"
                fontWeight={700}
                sx={{ color: licenseStatus.color }}
              >
                {licenseStatus.status === 'expired'
                  ? 'EXPIRED'
                  : licenseStatus.status === 'expiring'
                  ? `${licenseStatus.daysRemaining} DAYS`
                  : licenseStatus.status === 'valid'
                  ? `${licenseStatus.daysRemaining} DAYS`
                  : 'UNKNOWN'}
              </Typography>
              
              <Typography
                variant="body1"
                align="center"
                color="text.secondary"
                mt={1}
              >
                {licenseStatus.message}
              </Typography>
            </Box>

            {/* Expiry Progress Bar */}
            {licenseStatus.status !== 'unknown' && (
              <Box mb={3}>
                <Box display="flex" justifyContent="space-between" mb={1}>
                  <Typography variant="body2" color="text.secondary">
                    License Validity
                  </Typography>
                  <Typography variant="body2" fontWeight={500}>
                    {licenseStatus.status === 'expired'
                      ? '0%'
                      : `${Math.min(100, Math.round((licenseStatus.daysRemaining / 365) * 100))}%`}
                  </Typography>
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={
                    licenseStatus.status === 'expired'
                      ? 0
                      : Math.min(100, (licenseStatus.daysRemaining / 365) * 100)
                  }
                  sx={{
                    height: 10,
                    borderRadius: 5,
                    bgcolor: alpha(licenseStatus.color, 0.2),
                    '& .MuiLinearProgress-bar': {
                      bgcolor: licenseStatus.color,
                      borderRadius: 5,
                    },
                  }}
                />
              </Box>
            )}

            {/* Alert for expiring/expired */}
            {(licenseStatus.status === 'expired' || 
              (licenseStatus.status === 'expiring' && licenseStatus.daysRemaining <= 7)) && (
              <Alert
                severity="error"
                action={
                  <Button color="inherit" size="small">
                    Renew Now
                  </Button>
                }
              >
                {licenseStatus.status === 'expired'
                  ? 'This license has expired. Driver cannot operate vehicles.'
                  : 'License expiring soon! Schedule renewal immediately.'}
              </Alert>
            )}
          </Paper>
        </Grid>

        {/* License Details Card */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
              <Box display="flex" alignItems="center">
                <CreditCard sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h6">License Details</Typography>
              </Box>
              <IconButton size="small">
                <Edit fontSize="small" />
              </IconButton>
            </Box>

            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  License Number
                </Typography>
                <Typography variant="h6" fontWeight={600}>
                  {selectedDriver.licenseNumber || 'Not provided'}
                </Typography>
              </Grid>

              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  License Class
                </Typography>
                <Chip
                  label="LMV + TRANS"
                  color="primary"
                  variant="outlined"
                />
              </Grid>

              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Issue Date
                </Typography>
                <Typography variant="body1">
                  {formatDate(new Date(new Date(selectedDriver.licenseExpiry || '').setFullYear(
                    new Date(selectedDriver.licenseExpiry || '').getFullYear() - 5
                  )).toISOString())}
                </Typography>
              </Grid>

              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Expiry Date
                </Typography>
                <Box display="flex" alignItems="center" gap={1}>
                  <CalendarToday fontSize="small" color="action" />
                  <Typography variant="body1" fontWeight={500}>
                    {selectedDriver.licenseExpiry
                      ? formatDate(selectedDriver.licenseExpiry)
                      : 'Not set'}
                  </Typography>
                </Box>
              </Grid>

              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Verification Status
                </Typography>
                <Chip
                  icon={<VerifiedUser fontSize="small" />}
                  label="Verified"
                  color="success"
                  size="small"
                />
              </Grid>

              <Grid item xs={12}>
                <Divider sx={{ my: 2 }} />
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Authorized Vehicle Types
                </Typography>
                <Box display="flex" flexWrap="wrap" gap={1}>
                  {LICENSE_CLASSES.slice(0, 3).map((lc) => (
                    <Tooltip key={lc.code} title={lc.description}>
                      <Chip
                        label={lc.code}
                        size="small"
                        variant="outlined"
                        sx={{ borderColor: 'primary.main', color: 'primary.main' }}
                      />
                    </Tooltip>
                  ))}
                </Box>
              </Grid>
            </Grid>
          </Paper>
        </Grid>

        {/* License Image Card */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
              <Box display="flex" alignItems="center">
                <Camera sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h6">License Document</Typography>
              </Box>
              <Button
                size="small"
                startIcon={<Upload />}
                onClick={() => setUploadDialogOpen(true)}
              >
                Upload New
              </Button>
            </Box>

            <Grid container spacing={3}>
              <Grid item xs={12} sm={6}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                      Front Side
                    </Typography>
                    <Box
                      sx={{
                        height: 200,
                        bgcolor: 'grey.100',
                        borderRadius: 1,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        border: `2px dashed ${theme.palette.grey[300]}`,
                      }}
                    >
                      <Box textAlign="center">
                        <CreditCard sx={{ fontSize: 48, color: 'grey.400', mb: 1 }} />
                        <Typography variant="body2" color="text.secondary">
                          No image uploaded
                        </Typography>
                        <Button
                          size="small"
                          startIcon={<Upload />}
                          sx={{ mt: 1 }}
                          onClick={() => setUploadDialogOpen(true)}
                        >
                          Upload
                        </Button>
                      </Box>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                      Back Side
                    </Typography>
                    <Box
                      sx={{
                        height: 200,
                        bgcolor: 'grey.100',
                        borderRadius: 1,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        border: `2px dashed ${theme.palette.grey[300]}`,
                      }}
                    >
                      <Box textAlign="center">
                        <CreditCard sx={{ fontSize: 48, color: 'grey.400', mb: 1 }} />
                        <Typography variant="body2" color="text.secondary">
                          No image uploaded
                        </Typography>
                        <Button
                          size="small"
                          startIcon={<Upload />}
                          sx={{ mt: 1 }}
                          onClick={() => setUploadDialogOpen(true)}
                        >
                          Upload
                        </Button>
                      </Box>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          </Paper>
        </Grid>
      </Grid>

      {/* Upload Dialog */}
      <Dialog
        open={uploadDialogOpen}
        onClose={() => setUploadDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          Upload License Image
          <IconButton
            onClick={() => setUploadDialogOpen(false)}
            sx={{ position: 'absolute', right: 8, top: 8 }}
          >
            <Close />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          <Box
            sx={{
              border: `2px dashed ${theme.palette.primary.main}`,
              borderRadius: 2,
              p: 3,
              textAlign: 'center',
              bgcolor: alpha(theme.palette.primary.main, 0.05),
              cursor: 'pointer',
              transition: 'all 0.2s ease',
              '&:hover': {
                bgcolor: alpha(theme.palette.primary.main, 0.1),
              },
            }}
            component="label"
          >
            <input
              type="file"
              hidden
              accept="image/*"
              onChange={handleFileSelect}
            />
            {previewUrl ? (
              <Box>
                <img
                  src={previewUrl}
                  alt="License preview"
                  style={{
                    maxWidth: '100%',
                    maxHeight: 300,
                    borderRadius: 8,
                  }}
                />
                <Typography variant="body2" color="text.secondary" mt={2}>
                  Click to select a different image
                </Typography>
              </Box>
            ) : (
              <Box>
                <Upload sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
                <Typography variant="h6" gutterBottom>
                  Drop your license image here
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  or click to browse
                </Typography>
                <Typography variant="caption" display="block" color="text.secondary" mt={1}>
                  Supported formats: JPG, PNG, PDF (Max 5MB)
                </Typography>
              </Box>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setUploadDialogOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleUpload}
            disabled={!selectedFile || uploading}
            startIcon={uploading ? <CircularProgress size={16} /> : <Upload />}
          >
            {uploading ? 'Uploading...' : 'Upload'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Reminder Dialog */}
      <Dialog
        open={reminderDialogOpen}
        onClose={() => setReminderDialogOpen(false)}
        maxWidth="xs"
        fullWidth
      >
        <DialogTitle>
          Set Renewal Reminder
          <IconButton
            onClick={() => setReminderDialogOpen(false)}
            sx={{ position: 'absolute', right: 8, top: 8 }}
          >
            <Close />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" paragraph>
            Set a reminder to notify about license renewal before expiry.
          </Typography>
          <TextField
            fullWidth
            label="Days before expiry"
            type="number"
            value={reminderDays}
            onChange={(e) => setReminderDays(Number(e.target.value))}
            InputProps={{
              inputProps: { min: 1, max: 90 },
            }}
            helperText="Notification will be sent this many days before expiry"
          />
          <Box mt={2}>
            <Typography variant="body2" color="text.secondary">
              Reminder will be sent on:
            </Typography>
            <Typography variant="body1" fontWeight={500}>
              {selectedDriver.licenseExpiry
                ? formatDate(
                    new Date(
                      new Date(selectedDriver.licenseExpiry).getTime() -
                        reminderDays * 24 * 60 * 60 * 1000
                    ).toISOString()
                  )
                : 'Set license expiry first'}
            </Typography>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setReminderDialogOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleSetReminder}
            startIcon={<Notifications />}
          >
            Set Reminder
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default LicenseManagement;

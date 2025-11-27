import React, { useState, useEffect } from 'react';
import {
  Container,
  Button,
  Typography,
  Box,
  Stack,
  Switch,
  Card,
  CardContent,
  Alert,
  Divider,
  Grid,
  Chip,
  Paper,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Notifications as NotificationsIcon,
  NotificationsActive as AlertsIcon,
  Security as SecurityIcon,
  Palette as ThemeIcon,
  PrivacyTip as PrivacyIcon,
  Email as EmailIcon,
  PhoneAndroid as PhoneIcon,
  BatteryAlert as BatteryAlertIcon,
  LocalGasStation as FuelIcon,
  Build as MaintenanceIcon,
  Cloud as CloudIcon,
  LocationOn as LocationIcon,
  DarkMode as DarkModeIcon,
  LockReset as LockResetIcon,
  Info as InfoIcon,
  Save as SaveIcon,
} from '@mui/icons-material';
import { useAuth } from '../hooks/useAuth';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { toast } from 'react-toastify';
import { firebaseAuth } from '../services/firebase';

interface SettingItemProps {
  icon: React.ReactNode;
  title: string;
  description: string;
  checked: boolean;
  onChange: () => void;
  disabled?: boolean;
  badge?: string;
}

const SettingItem: React.FC<SettingItemProps> = ({
  icon,
  title,
  description,
  checked,
  onChange,
  disabled = false,
  badge,
}) => (
  <Box
    sx={{
      display: 'flex',
      alignItems: 'flex-start',
      py: 2,
      px: 2,
      borderRadius: 2,
      transition: 'background-color 0.2s',
      '&:hover': { bgcolor: 'action.hover' },
    }}
  >
    <Box sx={{ color: 'primary.main', mr: 2, mt: 0.5 }}>{icon}</Box>
    <Box sx={{ flex: 1 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <Typography variant="subtitle1" fontWeight={500}>
          {title}
        </Typography>
        {badge && (
          <Chip label={badge} size="small" color="info" variant="outlined" />
        )}
      </Box>
      <Typography variant="body2" color="text.secondary">
        {description}
      </Typography>
    </Box>
    <Switch checked={checked} onChange={onChange} disabled={disabled} />
  </Box>
);

export const SettingsPage: React.FC = () => {
  const { isAuthenticated, loading: authLoading, user, firebaseUser } = useAuth();
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);

  const [settings, setSettings] = useState({
    // Notification Settings
    emailNotifications: true,
    pushNotifications: true,
    smsNotifications: false,

    // Alert Settings - Multi-Fuel Support
    lowBatteryAlert: true,
    lowFuelAlert: true,
    maintenanceAlert: true,
    costAlert: false,
    emissionsAlert: true,

    // Privacy Settings
    shareAnalytics: true,
    shareLocation: false,

    // Theme Settings
    darkMode: false,
  });

  // Load settings from localStorage on mount
  useEffect(() => {
    const savedSettings = localStorage.getItem('userSettings');
    if (savedSettings) {
      try {
        const parsed = JSON.parse(savedSettings);
        setSettings(parsed);
      } catch (e) {
        console.error('Failed to parse saved settings:', e);
      }
    }
  }, []);

  const handleSettingChange = (settingKey: keyof typeof settings) => {
    setSettings((prev) => ({
      ...prev,
      [settingKey]: !prev[settingKey],
    }));
    setHasUnsavedChanges(true);
  };

  const handleSaveSettings = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      // Save settings to localStorage (client-side only for now)
      // Note: Backend settings API would need to be implemented for server-side persistence
      localStorage.setItem('userSettings', JSON.stringify(settings));
      
      setSuccessMessage('Settings saved successfully!');
      setHasUnsavedChanges(false);
      toast.success('Settings saved successfully!');
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (error) {
      console.error('Error saving settings:', error);
      setErrorMessage('Failed to save settings. Please try again.');
      toast.error('Failed to save settings.');
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async () => {
    const email = user?.email || firebaseUser?.email;
    if (!email) {
      setErrorMessage('No email address found. Please contact support.');
      return;
    }

    try {
      setLoading(true);
      // Use Firebase password reset via our wrapper
      await firebaseAuth.resetPassword(email);
      setSuccessMessage(`Password reset email sent to ${email}. Please check your inbox.`);
      toast.success('Password reset email sent!');
      setTimeout(() => setSuccessMessage(''), 5000);
    } catch (error: any) {
      console.error('Error resetting password:', error);
      const message = error?.message || 'Failed to reset password. Please try again.';
      setErrorMessage(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  if (authLoading) {
    return <LoadingSpinner message="Loading settings..." />;
  }

  if (!isAuthenticated) {
    return (
      <Container maxWidth="md" sx={{ py: 4, textAlign: 'center' }}>
        <Typography variant="h5" color="text.secondary">
          Please log in to view settings.
        </Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" fontWeight={600} gutterBottom>
          Settings
        </Typography>
        <Typography color="text.secondary">
          Manage your account preferences and application settings
        </Typography>
      </Box>

      {/* Alert Messages */}
      {successMessage && (
        <Alert severity="success" sx={{ mb: 3 }} onClose={() => setSuccessMessage('')}>
          {successMessage}
        </Alert>
      )}

      {errorMessage && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setErrorMessage('')}>
          {errorMessage}
        </Alert>
      )}

      {hasUnsavedChanges && (
        <Alert severity="info" sx={{ mb: 3 }} icon={<InfoIcon />}>
          You have unsaved changes. Don't forget to save your settings.
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* Left Column */}
        <Grid item xs={12} lg={6}>
          {/* Notification Settings */}
          <Card sx={{ mb: 3 }}>
            <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1, borderBottom: 1, borderColor: 'divider' }}>
              <NotificationsIcon color="primary" />
              <Typography variant="h6" fontWeight={600}>
                Notification Preferences
              </Typography>
            </Box>
            <CardContent sx={{ pt: 1 }}>
              <SettingItem
                icon={<EmailIcon />}
                title="Email Notifications"
                description="Receive important updates and alerts via email"
                checked={settings.emailNotifications}
                onChange={() => handleSettingChange('emailNotifications')}
              />
              <Divider sx={{ my: 1 }} />
              <SettingItem
                icon={<NotificationsIcon />}
                title="Push Notifications"
                description="Get real-time browser notifications"
                checked={settings.pushNotifications}
                onChange={() => handleSettingChange('pushNotifications')}
              />
              <Divider sx={{ my: 1 }} />
              <SettingItem
                icon={<PhoneIcon />}
                title="SMS Notifications"
                description="Receive text message alerts for critical events"
                checked={settings.smsNotifications}
                onChange={() => handleSettingChange('smsNotifications')}
                badge="Premium"
              />
            </CardContent>
          </Card>

          {/* Privacy & Analytics */}
          <Card sx={{ mb: 3 }}>
            <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1, borderBottom: 1, borderColor: 'divider' }}>
              <PrivacyIcon color="primary" />
              <Typography variant="h6" fontWeight={600}>
                Privacy & Data
              </Typography>
            </Box>
            <CardContent sx={{ pt: 1 }}>
              <SettingItem
                icon={<CloudIcon />}
                title="Share Analytics"
                description="Help us improve by sharing anonymous usage data"
                checked={settings.shareAnalytics}
                onChange={() => handleSettingChange('shareAnalytics')}
              />
              <Divider sx={{ my: 1 }} />
              <SettingItem
                icon={<LocationIcon />}
                title="Location Services"
                description="Enable GPS tracking for fleet optimization and route planning"
                checked={settings.shareLocation}
                onChange={() => handleSettingChange('shareLocation')}
              />
            </CardContent>
          </Card>

          {/* Appearance */}
          <Card>
            <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1, borderBottom: 1, borderColor: 'divider' }}>
              <ThemeIcon color="primary" />
              <Typography variant="h6" fontWeight={600}>
                Appearance
              </Typography>
            </Box>
            <CardContent sx={{ pt: 1 }}>
              <SettingItem
                icon={<DarkModeIcon />}
                title="Dark Mode"
                description="Switch to a darker color scheme for comfortable viewing"
                checked={settings.darkMode}
                onChange={() => handleSettingChange('darkMode')}
                badge="Coming Soon"
                disabled
              />
            </CardContent>
          </Card>
        </Grid>

        {/* Right Column */}
        <Grid item xs={12} lg={6}>
          {/* Alert Settings */}
          <Card sx={{ mb: 3 }}>
            <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1, borderBottom: 1, borderColor: 'divider' }}>
              <AlertsIcon color="primary" />
              <Typography variant="h6" fontWeight={600}>
                Fleet Alert Settings
              </Typography>
              <Tooltip title="Configure which vehicle alerts you want to receive">
                <IconButton size="small">
                  <InfoIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            </Box>
            <CardContent sx={{ pt: 1 }}>
              <SettingItem
                icon={<BatteryAlertIcon />}
                title="Low Battery Alerts"
                description="Get notified when EV or Hybrid vehicle battery is below 20%"
                checked={settings.lowBatteryAlert}
                onChange={() => handleSettingChange('lowBatteryAlert')}
                badge="EV/Hybrid"
              />
              <Divider sx={{ my: 1 }} />
              <SettingItem
                icon={<FuelIcon />}
                title="Low Fuel Alerts"
                description="Get notified when ICE or Hybrid vehicle fuel tank is below 15%"
                checked={settings.lowFuelAlert}
                onChange={() => handleSettingChange('lowFuelAlert')}
                badge="ICE/Hybrid"
              />
              <Divider sx={{ my: 1 }} />
              <SettingItem
                icon={<MaintenanceIcon />}
                title="Maintenance Reminders"
                description="Receive alerts for scheduled maintenance (oil change, tires, etc.)"
                checked={settings.maintenanceAlert}
                onChange={() => handleSettingChange('maintenanceAlert')}
              />
              <Divider sx={{ my: 1 }} />
              <SettingItem
                icon={<CloudIcon />}
                title="Emissions Monitoring"
                description="Track and alert on high CO2 emissions from ICE vehicles"
                checked={settings.emissionsAlert}
                onChange={() => handleSettingChange('emissionsAlert')}
                badge="ICE"
              />
            </CardContent>
          </Card>

          {/* Security Settings */}
          <Card>
            <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1, borderBottom: 1, borderColor: 'divider' }}>
              <SecurityIcon color="primary" />
              <Typography variant="h6" fontWeight={600}>
                Security
              </Typography>
            </Box>
            <CardContent>
              <Box sx={{ py: 2 }}>
                <Typography variant="subtitle1" fontWeight={500} gutterBottom>
                  Password Management
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Reset your password via email. You'll receive a link to create a new password.
                </Typography>
                <Stack direction="row" spacing={2} alignItems="center">
                  <Button
                    variant="outlined"
                    color="primary"
                    startIcon={<LockResetIcon />}
                    onClick={handleResetPassword}
                    disabled={loading}
                  >
                    {loading ? 'Sending...' : 'Reset Password'}
                  </Button>
                  {(user?.email || firebaseUser?.email) && (
                    <Typography variant="caption" color="text.secondary">
                      Email will be sent to: {user?.email || firebaseUser?.email}
                    </Typography>
                  )}
                </Stack>
              </Box>

              <Divider sx={{ my: 2 }} />

              <Box sx={{ py: 1 }}>
                <Typography variant="subtitle1" fontWeight={500} gutterBottom>
                  Account Information
                </Typography>
                <Stack spacing={1}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2" color="text.secondary">
                      Account Status
                    </Typography>
                    <Chip label="Active" color="success" size="small" />
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2" color="text.secondary">
                      Email Verified
                    </Typography>
                    <Chip
                      label={user?.emailVerified || firebaseUser?.emailVerified ? 'Verified' : 'Pending'}
                      color={user?.emailVerified || firebaseUser?.emailVerified ? 'success' : 'warning'}
                      size="small"
                    />
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2" color="text.secondary">
                      Two-Factor Auth
                    </Typography>
                    <Chip label="Not Enabled" color="default" size="small" />
                  </Box>
                </Stack>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Save Button Footer */}
      <Paper
        elevation={3}
        sx={{
          position: 'sticky',
          bottom: 16,
          mt: 4,
          p: 2,
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          borderRadius: 2,
        }}
      >
        <Typography variant="body2" color="text.secondary">
          Settings are saved to your browser. Some features require backend integration.
        </Typography>
        <Stack direction="row" spacing={2}>
          <Button
            variant="outlined"
            color="inherit"
            onClick={() => window.history.back()}
            disabled={loading}
          >
            Cancel
          </Button>
          <Button
            variant="contained"
            color="primary"
            onClick={handleSaveSettings}
            disabled={loading}
            startIcon={<SaveIcon />}
          >
            {loading ? 'Saving...' : 'Save Settings'}
          </Button>
        </Stack>
      </Paper>
    </Container>
  );
};

export default SettingsPage;

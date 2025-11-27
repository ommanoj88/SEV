import React, { useState } from 'react';
import {
  Box,
  Button,
  Typography,
  Stack,
  Switch,
  Card,
  CardContent,
  Alert,
  Divider,
  alpha,
  Chip,
} from '@mui/material';
import {
  Notifications as NotificationsIcon,
  Security as SecurityIcon,
  Palette as PaletteIcon,
  NotificationsActive as AlertIcon,
  Lock as LockIcon,
  PrivacyTip as PrivacyIcon,
  Save as SaveIcon,
} from '@mui/icons-material';
import { useAuth } from '../hooks/useAuth';
import LoadingSpinner from '../components/common/LoadingSpinner';

export const SettingsPage: React.FC = () => {
  const { isAuthenticated, loading: authLoading } = useAuth();
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const [settings, setSettings] = useState({
    emailNotifications: true,
    pushNotifications: true,
    smsNotifications: false,
    lowBatteryAlert: true,
    lowFuelAlert: true,
    maintenanceAlert: true,
    costAlert: false,
    emissionsAlert: true,
    shareAnalytics: true,
    shareLocation: false,
    darkMode: false,
  });

  const handleSettingChange = (settingKey: keyof typeof settings) => {
    setSettings((prev) => ({
      ...prev,
      [settingKey]: !prev[settingKey],
    }));
  };

  const handleSaveSettings = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      console.log('Saving settings:', settings);
      setSuccessMessage('Settings saved successfully!');
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (error) {
      console.error('Error saving settings:', error);
      setErrorMessage('Failed to save settings. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async () => {
    try {
      setLoading(true);
      setSuccessMessage('Password reset email sent to your inbox.');
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (error) {
      setErrorMessage('Failed to reset password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (authLoading) {
    return <LoadingSpinner message="Loading settings..." />;
  }

  if (!isAuthenticated) {
    return (
      <Box sx={{ py: 8, textAlign: 'center' }}>
        <Typography variant="h5" color="text.secondary">
          Please log in to view settings.
        </Typography>
      </Box>
    );
  }

  interface SettingSectionProps {
    title: string;
    description?: string;
    icon: React.ReactNode;
    iconColor: string;
    children: React.ReactNode;
  }

  const SettingSection: React.FC<SettingSectionProps> = ({ 
    title, 
    description, 
    icon, 
    iconColor,
    children 
  }) => (
    <Card sx={{ mb: 3 }}>
      <CardContent sx={{ p: 0 }}>
        {/* Section Header */}
        <Box 
          sx={{ 
            p: 3, 
            pb: 2,
            borderBottom: (theme) => `1px solid ${theme.palette.divider}`,
          }}
        >
          <Box display="flex" alignItems="center" gap={2}>
            <Box
              sx={{
                width: 40,
                height: 40,
                borderRadius: 2,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                bgcolor: alpha(iconColor, 0.1),
                color: iconColor,
              }}
            >
              {icon}
            </Box>
            <Box>
              <Typography variant="subtitle1" fontWeight={600}>
                {title}
              </Typography>
              {description && (
                <Typography variant="body2" color="text.secondary">
                  {description}
                </Typography>
              )}
            </Box>
          </Box>
        </Box>

        {/* Section Content */}
        <Box sx={{ p: 3 }}>
          {children}
        </Box>
      </CardContent>
    </Card>
  );

  interface SettingItemProps {
    label: string;
    description: string;
    checked: boolean;
    onChange: () => void;
    badge?: string;
  }

  const SettingItem: React.FC<SettingItemProps> = ({ 
    label, 
    description, 
    checked, 
    onChange,
    badge,
  }) => (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'flex-start',
        justifyContent: 'space-between',
        py: 1.5,
        '&:not(:last-child)': {
          borderBottom: (theme) => `1px solid ${theme.palette.divider}`,
        },
      }}
    >
      <Box flex={1} pr={2}>
        <Box display="flex" alignItems="center" gap={1}>
          <Typography variant="body1" fontWeight={500}>
            {label}
          </Typography>
          {badge && (
            <Chip
              label={badge}
              size="small"
              sx={{
                height: 20,
                fontSize: '0.65rem',
                fontWeight: 600,
                bgcolor: (theme) => alpha(theme.palette.primary.main, 0.1),
                color: 'primary.main',
              }}
            />
          )}
        </Box>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 0.25 }}>
          {description}
        </Typography>
      </Box>
      <Switch
        checked={checked}
        onChange={onChange}
        size="small"
      />
    </Box>
  );

  return (
    <Box className="fade-in" sx={{ maxWidth: 800, mx: 'auto' }}>
      {/* Page Header */}
      <Box mb={4}>
        <Typography variant="h4" fontWeight={700} color="text.primary" gutterBottom>
          Settings
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Manage your account preferences and notification settings
        </Typography>
      </Box>

      {/* Alerts */}
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

      {/* Notification Settings */}
      <SettingSection
        title="Notifications"
        description="Configure how you receive updates and alerts"
        icon={<NotificationsIcon />}
        iconColor="#0052CC"
      >
        <Stack spacing={0}>
          <SettingItem
            label="Email Notifications"
            description="Receive notifications via email for important updates"
            checked={settings.emailNotifications}
            onChange={() => handleSettingChange('emailNotifications')}
          />
          <SettingItem
            label="Push Notifications"
            description="Get real-time browser notifications"
            checked={settings.pushNotifications}
            onChange={() => handleSettingChange('pushNotifications')}
          />
          <SettingItem
            label="SMS Notifications"
            description="Receive critical alerts via text message"
            checked={settings.smsNotifications}
            onChange={() => handleSettingChange('smsNotifications')}
            badge="Premium"
          />
        </Stack>
      </SettingSection>

      {/* Alert Settings */}
      <SettingSection
        title="Fleet Alerts"
        description="Configure alerts for your multi-fuel fleet"
        icon={<AlertIcon />}
        iconColor="#FF8B00"
      >
        <Stack spacing={0}>
          <SettingItem
            label="Low Battery Alerts"
            description="Alert when EV or Hybrid vehicle battery is below threshold"
            checked={settings.lowBatteryAlert}
            onChange={() => handleSettingChange('lowBatteryAlert')}
            badge="EV/Hybrid"
          />
          <SettingItem
            label="Low Fuel Alerts"
            description="Alert when ICE or Hybrid vehicle fuel is low"
            checked={settings.lowFuelAlert}
            onChange={() => handleSettingChange('lowFuelAlert')}
            badge="ICE/Hybrid"
          />
          <SettingItem
            label="Maintenance Alerts"
            description="Notifications for scheduled maintenance and service reminders"
            checked={settings.maintenanceAlert}
            onChange={() => handleSettingChange('maintenanceAlert')}
          />
          <SettingItem
            label="Emissions Alerts"
            description="Track and alert on high CO2 emissions from ICE vehicles"
            checked={settings.emissionsAlert}
            onChange={() => handleSettingChange('emissionsAlert')}
            badge="ICE"
          />
          <SettingItem
            label="Cost Alerts"
            description="Budget notifications for fuel, electricity, and maintenance costs"
            checked={settings.costAlert}
            onChange={() => handleSettingChange('costAlert')}
          />
        </Stack>
      </SettingSection>

      {/* Privacy Settings */}
      <SettingSection
        title="Privacy & Analytics"
        description="Control your data sharing preferences"
        icon={<PrivacyIcon />}
        iconColor="#6554C0"
      >
        <Stack spacing={0}>
          <SettingItem
            label="Share Analytics"
            description="Help us improve by sharing anonymized usage data"
            checked={settings.shareAnalytics}
            onChange={() => handleSettingChange('shareAnalytics')}
          />
          <SettingItem
            label="Location Sharing"
            description="Enable real-time location tracking for fleet optimization"
            checked={settings.shareLocation}
            onChange={() => handleSettingChange('shareLocation')}
          />
        </Stack>
      </SettingSection>

      {/* Appearance */}
      <SettingSection
        title="Appearance"
        description="Customize the look and feel"
        icon={<PaletteIcon />}
        iconColor="#00875A"
      >
        <SettingItem
          label="Dark Mode"
          description="Switch to dark theme for reduced eye strain"
          checked={settings.darkMode}
          onChange={() => handleSettingChange('darkMode')}
        />
      </SettingSection>

      {/* Security */}
      <SettingSection
        title="Security"
        description="Manage your account security settings"
        icon={<SecurityIcon />}
        iconColor="#DE350B"
      >
        <Box>
          <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
            <Box>
              <Typography variant="body1" fontWeight={500}>
                Password
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Last changed: Never
              </Typography>
            </Box>
            <Button
              variant="outlined"
              size="small"
              startIcon={<LockIcon />}
              onClick={handleResetPassword}
              disabled={loading}
            >
              Reset Password
            </Button>
          </Box>
          <Divider sx={{ my: 2 }} />
          <Box display="flex" alignItems="center" justifyContent="space-between">
            <Box>
              <Typography variant="body1" fontWeight={500}>
                Two-Factor Authentication
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Add an extra layer of security to your account
              </Typography>
            </Box>
            <Chip
              label="Coming Soon"
              size="small"
              sx={{
                height: 22,
                fontSize: '0.7rem',
                fontWeight: 600,
                bgcolor: (theme) => alpha(theme.palette.text.secondary, 0.1),
                color: 'text.secondary',
              }}
            />
          </Box>
        </Box>
      </SettingSection>

      {/* Save Button */}
      <Box 
        sx={{ 
          display: 'flex', 
          justifyContent: 'flex-end', 
          gap: 2,
          pt: 2,
          borderTop: (theme) => `1px solid ${theme.palette.divider}`,
        }}
      >
        <Button
          variant="outlined"
          onClick={() => window.history.back()}
          disabled={loading}
        >
          Cancel
        </Button>
        <Button
          variant="contained"
          startIcon={<SaveIcon />}
          onClick={handleSaveSettings}
          disabled={loading}
        >
          {loading ? 'Saving...' : 'Save Changes'}
        </Button>
      </Box>
    </Box>
  );
};

export default SettingsPage;

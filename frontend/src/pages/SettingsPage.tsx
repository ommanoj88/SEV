import React, { useState } from 'react';
import {
  Container,
  Button,
  Typography,
  Box,
  Stack,
  FormControlLabel,
  Switch,
  Card,
  CardContent,
  CardHeader,
  Alert,
} from '@mui/material';
import { useAuth } from '../hooks/useAuth';
import LoadingSpinner from '../components/common/LoadingSpinner';

export const SettingsPage: React.FC = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const [settings, setSettings] = useState({
    // Notification Settings
    emailNotifications: true,
    pushNotifications: true,
    smsNotifications: false,

    // Alert Settings
    lowBatteryAlert: true,
    maintenanceAlert: true,
    costAlert: false,

    // Privacy Settings
    shareAnalytics: true,
    shareLocation: false,

    // Theme Settings
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
      // Save settings logic here
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
      // Reset password logic here
      setSuccessMessage('Password reset email sent to your inbox.');
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (error) {
      setErrorMessage('Failed to reset password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (!user) {
    return <LoadingSpinner />;
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Settings
        </Typography>
        <Typography color="textSecondary">
          Manage your account settings and preferences
        </Typography>
      </Box>

      {successMessage && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccessMessage('')}>
          {successMessage}
        </Alert>
      )}

      {errorMessage && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setErrorMessage('')}>
          {errorMessage}
        </Alert>
      )}

      <Stack spacing={3}>
        {/* Notification Settings */}
        <Card>
          <CardHeader title="Notification Settings" />
          <CardContent>
            <Stack spacing={2}>
              <FormControlLabel
                control={
                  <Switch
                    checked={settings.emailNotifications}
                    onChange={() => handleSettingChange('emailNotifications')}
                  />
                }
                label="Email Notifications"
              />
              <Typography variant="body2" color="textSecondary" sx={{ ml: 4, mt: -1 }}>
                Receive notifications via email
              </Typography>

              <FormControlLabel
                control={
                  <Switch
                    checked={settings.pushNotifications}
                    onChange={() => handleSettingChange('pushNotifications')}
                  />
                }
                label="Push Notifications"
              />
              <Typography variant="body2" color="textSecondary" sx={{ ml: 4, mt: -1 }}>
                Receive browser notifications
              </Typography>

              <FormControlLabel
                control={
                  <Switch
                    checked={settings.smsNotifications}
                    onChange={() => handleSettingChange('smsNotifications')}
                  />
                }
                label="SMS Notifications"
              />
              <Typography variant="body2" color="textSecondary" sx={{ ml: 4, mt: -1 }}>
                Receive text message alerts
              </Typography>
            </Stack>
          </CardContent>
        </Card>

        {/* Alert Settings */}
        <Card>
          <CardHeader title="Alert Settings" />
          <CardContent>
            <Stack spacing={2}>
              <FormControlLabel
                control={
                  <Switch
                    checked={settings.lowBatteryAlert}
                    onChange={() => handleSettingChange('lowBatteryAlert')}
                  />
                }
                label="Low Battery Alerts"
              />
              <Typography variant="body2" color="textSecondary" sx={{ ml: 4, mt: -1 }}>
                Alert when vehicle battery is low
              </Typography>

              <FormControlLabel
                control={
                  <Switch
                    checked={settings.maintenanceAlert}
                    onChange={() => handleSettingChange('maintenanceAlert')}
                  />
                }
                label="Maintenance Alerts"
              />
              <Typography variant="body2" color="textSecondary" sx={{ ml: 4, mt: -1 }}>
                Alert when maintenance is due
              </Typography>

              <FormControlLabel
                control={
                  <Switch
                    checked={settings.costAlert}
                    onChange={() => handleSettingChange('costAlert')}
                  />
                }
                label="Cost Alerts"
              />
              <Typography variant="body2" color="textSecondary" sx={{ ml: 4, mt: -1 }}>
                Alert about fleet operating costs
              </Typography>
            </Stack>
          </CardContent>
        </Card>

        {/* Privacy Settings */}
        <Card>
          <CardHeader title="Privacy & Analytics" />
          <CardContent>
            <Stack spacing={2}>
              <FormControlLabel
                control={
                  <Switch
                    checked={settings.shareAnalytics}
                    onChange={() => handleSettingChange('shareAnalytics')}
                  />
                }
                label="Share Analytics"
              />
              <Typography variant="body2" color="textSecondary" sx={{ ml: 4, mt: -1 }}>
                Help us improve by sharing usage data
              </Typography>

              <FormControlLabel
                control={
                  <Switch
                    checked={settings.shareLocation}
                    onChange={() => handleSettingChange('shareLocation')}
                  />
                }
                label="Location Sharing"
              />
              <Typography variant="body2" color="textSecondary" sx={{ ml: 4, mt: -1 }}>
                Allow location tracking for fleet optimization
              </Typography>
            </Stack>
          </CardContent>
        </Card>

        {/* Theme Settings */}
        <Card>
          <CardHeader title="Appearance" />
          <CardContent>
            <FormControlLabel
              control={
                <Switch
                  checked={settings.darkMode}
                  onChange={() => handleSettingChange('darkMode')}
                />
              }
              label="Dark Mode"
            />
            <Typography variant="body2" color="textSecondary" sx={{ ml: 4, mt: 1 }}>
              Enable dark theme for the application
            </Typography>
          </CardContent>
        </Card>

        {/* Security Settings */}
        <Card>
          <CardHeader title="Security" />
          <CardContent>
            <Stack spacing={2}>
              <Box>
                <Typography variant="body2" gutterBottom>
                  Last Password Change: Never
                </Typography>
                <Button
                  variant="outlined"
                  color="primary"
                  onClick={handleResetPassword}
                  disabled={loading}
                  sx={{ mt: 1 }}
                >
                  Reset Password
                </Button>
              </Box>
            </Stack>
          </CardContent>
        </Card>

        {/* Save Button */}
        <Stack direction="row" spacing={2} justifyContent="flex-end" sx={{ mt: 4 }}>
          <Button
            variant="outlined"
            color="inherit"
            onClick={() => window.history.back()}
          >
            Cancel
          </Button>
          <Button
            variant="contained"
            color="primary"
            onClick={handleSaveSettings}
            disabled={loading}
          >
            {loading ? 'Saving...' : 'Save Settings'}
          </Button>
        </Stack>
      </Stack>
    </Container>
  );
};

export default SettingsPage;

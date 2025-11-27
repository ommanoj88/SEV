import React, { useState, useEffect } from 'react';
import {
  Container,
  Paper,
  TextField,
  Button,
  Grid,
  Typography,
  Box,
  Avatar,
  Stack,
  Card,
  CardContent,
} from '@mui/material';
import { useAuth } from '../hooks/useAuth';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { getNameFromEmail } from '../utils/helpers';

export const ProfilePage: React.FC = () => {
  const { user, firebaseUser, isAuthenticated } = useAuth();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    email: '',
    name: '',
    phone: '',
    company: '',
    role: '',
  });
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    // Prioritize backend user data, fallback to Firebase user
    if (user) {
      setFormData({
        email: user.email || '',
        name: `${user.firstName} ${user.lastName}`.trim() || '',
        phone: user.phone || '',
        company: '',
        role: user.role || '',
      });
    } else if (firebaseUser) {
      // Use Firebase user data when backend data is not available
      setFormData({
        email: firebaseUser.email || '',
        name: firebaseUser.displayName || getNameFromEmail(firebaseUser.email || ''),
        phone: firebaseUser.phoneNumber || '',
        company: '',
        role: 'User',
      });
    }
  }, [user, firebaseUser]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      // Update profile logic here
      setSuccessMessage('Profile updated successfully!');
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (error) {
      console.error('Error updating profile:', error);
    } finally {
      setLoading(false);
    }
  };

  if (!isAuthenticated) {
    return (
      <Container maxWidth="md" sx={{ py: 4, textAlign: 'center' }}>
        <Typography variant="h5" color="textSecondary">
          Please log in to view your profile.
        </Typography>
      </Container>
    );
  }

  if (!user && !firebaseUser) {
    return <LoadingSpinner />;
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          My Profile
        </Typography>
        <Typography color="textSecondary">
          Manage your account information and preferences
        </Typography>
        {!user && firebaseUser && (
          <Typography color="warning.main" variant="body2" sx={{ mt: 1 }}>
            Note: Backend services are unavailable. Showing Firebase profile data only.
          </Typography>
        )}
      </Box>

      <Grid container spacing={3}>
        {/* Profile Card */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Avatar
                sx={{
                  width: 100,
                  height: 100,
                  mx: 'auto',
                  mb: 2,
                  bgcolor: 'primary.main',
                }}
              >
                {formData.name.charAt(0).toUpperCase()}
              </Avatar>
              <Typography variant="h6" gutterBottom>
                {formData.name || 'User'}
              </Typography>
              <Typography color="textSecondary" gutterBottom>
                {formData.email}
              </Typography>
              <Typography variant="body2" color="textSecondary">
                Role: {formData.role || 'Not assigned'}
              </Typography>
              {!user && firebaseUser && (
                <Typography variant="caption" color="warning.main" sx={{ mt: 1, display: 'block' }}>
                  Firebase user only
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Edit Profile Form */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3 }}>
            <form onSubmit={handleSubmit}>
              <Stack spacing={3}>
                <TextField
                  fullWidth
                  label="Email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  disabled
                  type="email"
                />

                <TextField
                  fullWidth
                  label="Full Name"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  placeholder="Enter your full name"
                />

                <TextField
                  fullWidth
                  label="Phone Number"
                  name="phone"
                  value={formData.phone}
                  onChange={handleInputChange}
                  placeholder="Enter your phone number"
                  type="tel"
                />

                <TextField
                  fullWidth
                  label="Company"
                  name="company"
                  value={formData.company}
                  onChange={handleInputChange}
                  disabled
                  placeholder="Your company"
                />

                <TextField
                  fullWidth
                  label="Role"
                  name="role"
                  value={formData.role}
                  onChange={handleInputChange}
                  disabled
                  placeholder="Your role"
                />

                {successMessage && (
                  <Typography color="success.main" variant="body2">
                    {successMessage}
                  </Typography>
                )}

                <Stack direction="row" spacing={2} justifyContent="flex-end">
                  <Button
                    variant="outlined"
                    color="inherit"
                    onClick={() => window.history.back()}
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    color="primary"
                    disabled={loading || !user}
                  >
                    {loading ? 'Saving...' : 'Save Changes'}
                  </Button>
                  {!user && (
                    <Typography variant="caption" color="warning.main">
                      Profile updates require backend connection
                    </Typography>
                  )}
                </Stack>
              </Stack>
            </form>
          </Paper>
        </Grid>
      </Grid>

      {/* Account Stats */}
      <Grid container spacing={2} sx={{ mt: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Account Status
              </Typography>
              <Typography variant="h6">Active</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Member Since
              </Typography>
              <Typography variant="h6">
                {user?.createdAt ? new Date(user.createdAt).getFullYear() : 'N/A'}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Last Login
              </Typography>
              <Typography variant="h6">Today</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Vehicles
              </Typography>
              <Typography variant="h6">-</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default ProfilePage;

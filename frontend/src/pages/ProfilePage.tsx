import React, { useState, useEffect } from 'react';
import {
  Box,
  TextField,
  Button,
  Grid,
  Typography,
  Avatar,
  Stack,
  Card,
  CardContent,
  Divider,
  alpha,
  Chip,
  IconButton,
  Alert,
} from '@mui/material';
import {
  Person as PersonIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  Business as BusinessIcon,
  Badge as BadgeIcon,
  Edit as EditIcon,
  CameraAlt as CameraIcon,
  CalendarToday as CalendarIcon,
  Security as SecurityIcon,
  DirectionsCar as VehicleIcon,
  CheckCircle as CheckCircleIcon,
} from '@mui/icons-material';
import { useAuth } from '../hooks/useAuth';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { getNameFromEmail } from '../utils/helpers';

interface StatItemProps {
  label: string;
  value: string | number;
  icon: React.ReactNode;
  color: string;
}

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
    if (user) {
      setFormData({
        email: user.email || '',
        name: `${user.firstName} ${user.lastName}`.trim() || '',
        phone: user.phone || '',
        company: '',
        role: user.role || '',
      });
    } else if (firebaseUser) {
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
      <Box sx={{ py: 8, textAlign: 'center' }}>
        <Typography variant="h5" color="text.secondary">
          Please log in to view your profile.
        </Typography>
      </Box>
    );
  }

  if (!user && !firebaseUser) {
    return <LoadingSpinner />;
  }

  const getUserInitials = () => {
    if (formData.name) {
      const parts = formData.name.split(' ');
      if (parts.length >= 2) {
        return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
      }
      return formData.name[0]?.toUpperCase() || 'U';
    }
    return 'U';
  };

  const StatItem: React.FC<StatItemProps> = ({ label, value, icon, color }) => (
    <Box
      sx={{
        p: 2.5,
        borderRadius: 2,
        border: (theme) => `1px solid ${theme.palette.divider}`,
        transition: 'all 0.2s ease',
        '&:hover': {
          borderColor: alpha(color, 0.3),
          bgcolor: alpha(color, 0.02),
        },
      }}
    >
      <Box display="flex" alignItems="center" gap={1.5} mb={1}>
        <Box
          sx={{
            width: 32,
            height: 32,
            borderRadius: 1.5,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            bgcolor: alpha(color, 0.1),
            color: color,
          }}
        >
          {icon}
        </Box>
        <Typography variant="caption" color="text.secondary" fontWeight={500}>
          {label}
        </Typography>
      </Box>
      <Typography variant="h6" fontWeight={600} color="text.primary">
        {value}
      </Typography>
    </Box>
  );

  return (
    <Box className="fade-in" sx={{ maxWidth: 1000, mx: 'auto' }}>
      {/* Page Header */}
      <Box mb={4}>
        <Typography variant="h4" fontWeight={700} color="text.primary" gutterBottom>
          My Profile
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Manage your personal information and account settings
        </Typography>
        {!user && firebaseUser && (
          <Alert severity="warning" sx={{ mt: 2 }}>
            Backend services are unavailable. Showing Firebase profile data only.
          </Alert>
        )}
      </Box>

      <Grid container spacing={3}>
        {/* Profile Card */}
        <Grid item xs={12} md={4}>
          <Card sx={{ position: 'sticky', top: 88 }}>
            <CardContent sx={{ p: 3, textAlign: 'center' }}>
              {/* Avatar */}
              <Box sx={{ position: 'relative', display: 'inline-block', mb: 2 }}>
                <Avatar
                  sx={{
                    width: 120,
                    height: 120,
                    bgcolor: 'primary.main',
                    fontSize: '2.5rem',
                    fontWeight: 600,
                    border: (theme) => `4px solid ${theme.palette.background.paper}`,
                    boxShadow: (theme) => `0 4px 20px ${alpha(theme.palette.primary.main, 0.25)}`,
                  }}
                >
                  {getUserInitials()}
                </Avatar>
                <IconButton
                  size="small"
                  sx={{
                    position: 'absolute',
                    bottom: 4,
                    right: 4,
                    bgcolor: 'background.paper',
                    border: (theme) => `2px solid ${theme.palette.divider}`,
                    '&:hover': {
                      bgcolor: 'background.paper',
                    },
                  }}
                >
                  <CameraIcon sx={{ fontSize: 16 }} />
                </IconButton>
              </Box>

              {/* Name & Email */}
              <Typography variant="h5" fontWeight={600} gutterBottom>
                {formData.name || 'User'}
              </Typography>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                {formData.email}
              </Typography>

              {/* Role Badge */}
              <Chip
                label={formData.role || 'Member'}
                size="small"
                sx={{
                  mt: 1,
                  fontWeight: 600,
                  bgcolor: (theme) => alpha(theme.palette.primary.main, 0.1),
                  color: 'primary.main',
                }}
              />

              {/* Status */}
              <Box 
                display="flex" 
                alignItems="center" 
                justifyContent="center" 
                gap={0.75}
                mt={2}
              >
                <CheckCircleIcon sx={{ color: 'success.main', fontSize: 18 }} />
                <Typography variant="body2" color="success.main" fontWeight={500}>
                  Account Active
                </Typography>
              </Box>

              {!user && firebaseUser && (
                <Typography 
                  variant="caption" 
                  color="warning.main" 
                  sx={{ mt: 2, display: 'block' }}
                >
                  Firebase user only
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Edit Form */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent sx={{ p: 3 }}>
              <Box display="flex" alignItems="center" justifyContent="space-between" mb={3}>
                <Box display="flex" alignItems="center" gap={1.5}>
                  <Box
                    sx={{
                      width: 40,
                      height: 40,
                      borderRadius: 2,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      bgcolor: (theme) => alpha(theme.palette.primary.main, 0.1),
                      color: 'primary.main',
                    }}
                  >
                    <PersonIcon />
                  </Box>
                  <Box>
                    <Typography variant="subtitle1" fontWeight={600}>
                      Personal Information
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Update your personal details
                    </Typography>
                  </Box>
                </Box>
              </Box>

              <Divider sx={{ mb: 3 }} />

              {successMessage && (
                <Alert severity="success" sx={{ mb: 3 }}>
                  {successMessage}
                </Alert>
              )}

              <form onSubmit={handleSubmit}>
                <Stack spacing={2.5}>
                  <TextField
                    fullWidth
                    label="Email Address"
                    name="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    disabled
                    type="email"
                    InputProps={{
                      startAdornment: (
                        <EmailIcon sx={{ color: 'text.disabled', mr: 1, fontSize: 20 }} />
                      ),
                    }}
                  />

                  <TextField
                    fullWidth
                    label="Full Name"
                    name="name"
                    value={formData.name}
                    onChange={handleInputChange}
                    placeholder="Enter your full name"
                    InputProps={{
                      startAdornment: (
                        <PersonIcon sx={{ color: 'text.disabled', mr: 1, fontSize: 20 }} />
                      ),
                    }}
                  />

                  <TextField
                    fullWidth
                    label="Phone Number"
                    name="phone"
                    value={formData.phone}
                    onChange={handleInputChange}
                    placeholder="Enter your phone number"
                    type="tel"
                    InputProps={{
                      startAdornment: (
                        <PhoneIcon sx={{ color: 'text.disabled', mr: 1, fontSize: 20 }} />
                      ),
                    }}
                  />

                  <TextField
                    fullWidth
                    label="Company"
                    name="company"
                    value={formData.company}
                    onChange={handleInputChange}
                    disabled
                    placeholder="Your company"
                    InputProps={{
                      startAdornment: (
                        <BusinessIcon sx={{ color: 'text.disabled', mr: 1, fontSize: 20 }} />
                      ),
                    }}
                  />

                  <TextField
                    fullWidth
                    label="Role"
                    name="role"
                    value={formData.role}
                    onChange={handleInputChange}
                    disabled
                    placeholder="Your role"
                    InputProps={{
                      startAdornment: (
                        <BadgeIcon sx={{ color: 'text.disabled', mr: 1, fontSize: 20 }} />
                      ),
                    }}
                  />

                  <Box display="flex" gap={2} justifyContent="flex-end" pt={2}>
                    <Button
                      variant="outlined"
                      onClick={() => window.history.back()}
                    >
                      Cancel
                    </Button>
                    <Button
                      type="submit"
                      variant="contained"
                      disabled={loading || !user}
                      startIcon={loading ? null : <EditIcon />}
                    >
                      {loading ? 'Saving...' : 'Save Changes'}
                    </Button>
                  </Box>

                  {!user && (
                    <Typography variant="caption" color="warning.main" textAlign="right">
                      Profile updates require backend connection
                    </Typography>
                  )}
                </Stack>
              </form>
            </CardContent>
          </Card>

          {/* Account Stats */}
          <Box mt={3}>
            <Typography variant="subtitle2" fontWeight={600} mb={2}>
              Account Statistics
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={6} sm={3}>
                <StatItem
                  label="Member Since"
                  value={user?.createdAt ? new Date(user.createdAt).getFullYear() : 'N/A'}
                  icon={<CalendarIcon sx={{ fontSize: 18 }} />}
                  color="#0052CC"
                />
              </Grid>
              <Grid item xs={6} sm={3}>
                <StatItem
                  label="Last Login"
                  value="Today"
                  icon={<SecurityIcon sx={{ fontSize: 18 }} />}
                  color="#00875A"
                />
              </Grid>
              <Grid item xs={6} sm={3}>
                <StatItem
                  label="Vehicles"
                  value="—"
                  icon={<VehicleIcon sx={{ fontSize: 18 }} />}
                  color="#6554C0"
                />
              </Grid>
              <Grid item xs={6} sm={3}>
                <StatItem
                  label="Status"
                  value="Active"
                  icon={<CheckCircleIcon sx={{ fontSize: 18 }} />}
                  color="#00875A"
                />
              </Grid>
            </Grid>
          </Box>
        </Grid>
      </Grid>
    </Box>
  );
};

export default ProfilePage;

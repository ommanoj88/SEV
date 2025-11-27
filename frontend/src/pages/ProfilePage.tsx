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
  Divider,
  Alert,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Person as PersonIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  Business as BusinessIcon,
  Badge as BadgeIcon,
  Edit as EditIcon,
  PhotoCamera as PhotoCameraIcon,
  CheckCircle as CheckCircleIcon,
  AccessTime as AccessTimeIcon,
  CalendarToday as CalendarIcon,
  DirectionsCar as CarIcon,
} from '@mui/icons-material';
import { useAuth } from '../hooks/useAuth';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { getNameFromEmail } from '../utils/helpers';
import authService from '../services/authService';
import { toast } from 'react-toastify';

export const ProfilePage: React.FC = () => {
  const { user, firebaseUser, isAuthenticated } = useAuth();
  const [loading, setLoading] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    email: '',
    firstName: '',
    lastName: '',
    phone: '',
    company: '',
    role: '',
  });
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    // Prioritize backend user data, fallback to Firebase user
    if (user) {
      setFormData({
        email: user.email || '',
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        phone: user.phone || '',
        company: user.companyName || '',
        role: user.role || '',
      });
    } else if (firebaseUser) {
      // Use Firebase user data when backend data is not available
      const displayName = firebaseUser.displayName || getNameFromEmail(firebaseUser.email || '');
      const nameParts = displayName.split(' ');
      setFormData({
        email: firebaseUser.email || '',
        firstName: nameParts[0] || '',
        lastName: nameParts.slice(1).join(' ') || '',
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
    if (!user?.id) {
      setErrorMessage('User ID not available. Cannot update profile.');
      return;
    }

    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      // Call backend API to update profile using PUT /auth/users/{id}
      await authService.updateProfile(user.id, {
        firstName: formData.firstName,
        lastName: formData.lastName,
        phone: formData.phone,
      });
      
      setSuccessMessage('Profile updated successfully!');
      setIsEditing(false);
      toast.success('Profile updated successfully!');
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (error: any) {
      console.error('Error updating profile:', error);
      const message = error?.response?.data?.message || 'Failed to update profile. Please try again.';
      setErrorMessage(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelEdit = () => {
    setIsEditing(false);
    // Reset form data to current user values
    if (user) {
      setFormData({
        email: user.email || '',
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        phone: user.phone || '',
        company: user.companyName || '',
        role: user.role || '',
      });
    }
    setErrorMessage('');
  };

  const getFullName = () => {
    const name = `${formData.firstName} ${formData.lastName}`.trim();
    return name || 'User';
  };

  const getInitials = () => {
    const first = formData.firstName?.charAt(0) || '';
    const last = formData.lastName?.charAt(0) || '';
    return (first + last).toUpperCase() || 'U';
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
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
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Header Section */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" fontWeight={600} gutterBottom>
          My Profile
        </Typography>
        <Typography color="text.secondary">
          View and manage your account information
        </Typography>
        {!user && firebaseUser && (
          <Alert severity="warning" sx={{ mt: 2 }}>
            Backend services are unavailable. Showing Firebase profile data only. Profile updates require backend connection.
          </Alert>
        )}
      </Box>

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

      <Grid container spacing={3}>
        {/* Profile Card - Left Side */}
        <Grid item xs={12} md={4}>
          <Card sx={{ position: 'relative', overflow: 'visible' }}>
            <Box
              sx={{
                height: 100,
                background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
                borderRadius: '12px 12px 0 0',
              }}
            />
            <CardContent sx={{ textAlign: 'center', pt: 0, mt: -6 }}>
              <Box sx={{ position: 'relative', display: 'inline-block' }}>
                <Avatar
                  src={user?.profileImageUrl}
                  sx={{
                    width: 120,
                    height: 120,
                    mx: 'auto',
                    border: '4px solid white',
                    bgcolor: 'primary.main',
                    fontSize: '2.5rem',
                    boxShadow: 3,
                  }}
                >
                  {getInitials()}
                </Avatar>
                <Tooltip title="Change photo (coming soon)">
                  <IconButton
                    size="small"
                    disabled
                    sx={{
                      position: 'absolute',
                      bottom: 0,
                      right: 0,
                      bgcolor: 'background.paper',
                      boxShadow: 1,
                      '&:hover': { bgcolor: 'grey.100' },
                    }}
                  >
                    <PhotoCameraIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              </Box>

              <Typography variant="h5" fontWeight={600} sx={{ mt: 2 }}>
                {getFullName()}
              </Typography>
              <Typography color="text.secondary" gutterBottom>
                {formData.email}
              </Typography>

              <Stack direction="row" spacing={1} justifyContent="center" sx={{ mt: 2 }}>
                <Chip
                  icon={<BadgeIcon />}
                  label={formData.role || 'User'}
                  color="primary"
                  variant="outlined"
                  size="small"
                />
                {user?.isActive !== false && (
                  <Chip
                    icon={<CheckCircleIcon />}
                    label="Active"
                    color="success"
                    variant="outlined"
                    size="small"
                  />
                )}
              </Stack>

              {!user && firebaseUser && (
                <Chip
                  label="Firebase Only"
                  color="warning"
                  size="small"
                  sx={{ mt: 2 }}
                />
              )}
            </CardContent>
          </Card>

          {/* Quick Stats Card */}
          <Card sx={{ mt: 3 }}>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom fontWeight={600}>
                ACCOUNT INFORMATION
              </Typography>
              <Divider sx={{ mb: 2 }} />

              <Stack spacing={2}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <CalendarIcon color="action" />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Member Since
                    </Typography>
                    <Typography variant="body1" fontWeight={500}>
                      {formatDate(user?.createdAt)}
                    </Typography>
                  </Box>
                </Box>

                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <AccessTimeIcon color="action" />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Last Login
                    </Typography>
                    <Typography variant="body1" fontWeight={500}>
                      {user?.lastLogin ? formatDate(user.lastLogin) : 'Today'}
                    </Typography>
                  </Box>
                </Box>

                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <BusinessIcon color="action" />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Company
                    </Typography>
                    <Typography variant="body1" fontWeight={500}>
                      {formData.company || 'Not assigned'}
                    </Typography>
                  </Box>
                </Box>

                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <CarIcon color="action" />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Fleet Access
                    </Typography>
                    <Typography variant="body1" fontWeight={500}>
                      {user?.fleetName || (user?.companyId ? 'Assigned' : 'Not assigned')}
                    </Typography>
                  </Box>
                </Box>
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        {/* Edit Profile Form - Right Side */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 4 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Box>
                <Typography variant="h6" fontWeight={600}>
                  Personal Information
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Update your personal details here
                </Typography>
              </Box>
              {!isEditing && user && (
                <Button
                  variant="outlined"
                  startIcon={<EditIcon />}
                  onClick={() => setIsEditing(true)}
                >
                  Edit Profile
                </Button>
              )}
            </Box>

            <Divider sx={{ mb: 3 }} />

            <form onSubmit={handleSubmit}>
              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                    <EmailIcon color="action" />
                    <Typography variant="subtitle2" color="text.secondary">
                      Email Address
                    </Typography>
                  </Box>
                  <TextField
                    fullWidth
                    name="email"
                    value={formData.email}
                    disabled
                    type="email"
                    size="small"
                    helperText="Email cannot be changed"
                    sx={{ 
                      '& .MuiInputBase-input.Mui-disabled': {
                        WebkitTextFillColor: 'rgba(0, 0, 0, 0.87)',
                      }
                    }}
                  />
                </Grid>

                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                    <PersonIcon color="action" />
                    <Typography variant="subtitle2" color="text.secondary">
                      First Name
                    </Typography>
                  </Box>
                  <TextField
                    fullWidth
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleInputChange}
                    disabled={!isEditing}
                    placeholder="Enter your first name"
                    size="small"
                    sx={{ 
                      '& .MuiInputBase-input.Mui-disabled': {
                        WebkitTextFillColor: 'rgba(0, 0, 0, 0.87)',
                      }
                    }}
                  />
                </Grid>

                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                    <PersonIcon color="action" />
                    <Typography variant="subtitle2" color="text.secondary">
                      Last Name
                    </Typography>
                  </Box>
                  <TextField
                    fullWidth
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleInputChange}
                    disabled={!isEditing}
                    placeholder="Enter your last name"
                    size="small"
                    sx={{ 
                      '& .MuiInputBase-input.Mui-disabled': {
                        WebkitTextFillColor: 'rgba(0, 0, 0, 0.87)',
                      }
                    }}
                  />
                </Grid>

                <Grid item xs={12}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                    <PhoneIcon color="action" />
                    <Typography variant="subtitle2" color="text.secondary">
                      Phone Number
                    </Typography>
                  </Box>
                  <TextField
                    fullWidth
                    name="phone"
                    value={formData.phone}
                    onChange={handleInputChange}
                    disabled={!isEditing}
                    placeholder="Enter your phone number"
                    type="tel"
                    size="small"
                    sx={{ 
                      '& .MuiInputBase-input.Mui-disabled': {
                        WebkitTextFillColor: 'rgba(0, 0, 0, 0.87)',
                      }
                    }}
                  />
                </Grid>

                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                    <BusinessIcon color="action" />
                    <Typography variant="subtitle2" color="text.secondary">
                      Company
                    </Typography>
                  </Box>
                  <TextField
                    fullWidth
                    name="company"
                    value={formData.company}
                    disabled
                    placeholder="Your company"
                    size="small"
                    helperText="Contact admin to change company"
                    sx={{ 
                      '& .MuiInputBase-input.Mui-disabled': {
                        WebkitTextFillColor: 'rgba(0, 0, 0, 0.87)',
                      }
                    }}
                  />
                </Grid>

                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                    <BadgeIcon color="action" />
                    <Typography variant="subtitle2" color="text.secondary">
                      Role
                    </Typography>
                  </Box>
                  <TextField
                    fullWidth
                    name="role"
                    value={formData.role}
                    disabled
                    placeholder="Your role"
                    size="small"
                    helperText="Role is assigned by admin"
                    sx={{ 
                      '& .MuiInputBase-input.Mui-disabled': {
                        WebkitTextFillColor: 'rgba(0, 0, 0, 0.87)',
                      }
                    }}
                  />
                </Grid>
              </Grid>

              {isEditing && (
                <Stack direction="row" spacing={2} justifyContent="flex-end" sx={{ mt: 4 }}>
                  <Button
                    variant="outlined"
                    color="inherit"
                    onClick={handleCancelEdit}
                    disabled={loading}
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
                </Stack>
              )}
            </form>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default ProfilePage;

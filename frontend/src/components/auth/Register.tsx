import React from 'react';
import { Box, Card, CardContent, TextField, Button, Typography, Link, Alert, Grid } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { useAppDispatch, useAppSelector } from '../../redux/hooks';
import { register as registerUser, selectAuthError, selectAuthLoading } from '../../redux/slices/authSlice';
import { registerSchema } from '../../utils/validators';
import LoadingSpinner from '../common/LoadingSpinner';

interface RegisterFormData {
  email: string;
  password: string;
  confirmPassword: string;
  firstName: string;
  lastName: string;
  phone?: string;
  companyName?: string;
  fleetSize?: number;
}

const Register: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const error = useAppSelector(selectAuthError);
  const loading = useAppSelector(selectAuthLoading);

  const { register, handleSubmit, formState: { errors } } = useForm<RegisterFormData>({
    resolver: yupResolver(registerSchema) as any,
  });

  const onSubmit = async (data: RegisterFormData) => {
    try {
      await dispatch(registerUser(data)).unwrap();
      navigate('/dashboard');
    } catch (err) {
      // Error handled by Redux
    }
  };

  return (
    <Box display="flex" alignItems="center" justifyContent="center" minHeight="100vh" sx={{ backgroundColor: 'background.default', p: 2 }}>
      <Card sx={{ maxWidth: 600, width: '100%' }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h4" align="center" gutterBottom fontWeight={600}>
            Create Account
          </Typography>
          <Typography variant="body2" align="center" color="text.secondary" paragraph>
            Join EV Fleet Management today
          </Typography>

          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

          <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth label="First Name" {...register('firstName')} error={!!errors.firstName} helperText={errors.firstName?.message} disabled={loading} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth label="Last Name" {...register('lastName')} error={!!errors.lastName} helperText={errors.lastName?.message} disabled={loading} />
              </Grid>
              <Grid item xs={12}>
                <TextField fullWidth label="Email" type="email" {...register('email')} error={!!errors.email} helperText={errors.email?.message} disabled={loading} />
              </Grid>
              <Grid item xs={12}>
                <TextField fullWidth label="Phone (Optional)" {...register('phone')} error={!!errors.phone} helperText={errors.phone?.message} disabled={loading} />
              </Grid>
              <Grid item xs={12}>
                <TextField fullWidth label="Company Name (Optional)" {...register('companyName')} disabled={loading} />
              </Grid>
              <Grid item xs={12}>
                <TextField fullWidth label="Password" type="password" {...register('password')} error={!!errors.password} helperText={errors.password?.message} disabled={loading} />
              </Grid>
              <Grid item xs={12}>
                <TextField fullWidth label="Confirm Password" type="password" {...register('confirmPassword')} error={!!errors.confirmPassword} helperText={errors.confirmPassword?.message} disabled={loading} />
              </Grid>
            </Grid>

            <Button fullWidth variant="contained" size="large" type="submit" disabled={loading} sx={{ mt: 3, mb: 2 }}>
              {loading ? <LoadingSpinner size={24} /> : 'Sign Up'}
            </Button>

            <Typography variant="body2" align="center" color="text.secondary">
              Already have an account?{' '}
              <Link component="button" type="button" variant="body2" onClick={() => navigate('/login')} sx={{ textDecoration: 'none', fontWeight: 600 }}>
                Sign In
              </Link>
            </Typography>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default Register;

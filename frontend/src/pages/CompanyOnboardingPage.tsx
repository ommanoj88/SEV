import React, { useState } from 'react';
import { Box, Card, CardContent, TextField, Button, Typography, Alert } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useAppDispatch, useAppSelector } from '../redux/hooks';
import { updateProfile, selectUser } from '../redux/slices/authSlice';
import authService from '../services/authService';
import { toast } from 'react-toastify';
import LoadingSpinner from '../components/common/LoadingSpinner';

const companyOnboardingSchema = yup.object({
  companyName: yup
    .string()
    .required('Company name is required')
    .min(2, 'Company name must be at least 2 characters'),
  phone: yup
    .string()
    .matches(/^[\d\s\-\+\(\)]+$/, { message: 'Invalid phone number format', excludeEmptyString: true })
    .optional(),
});

interface CompanyOnboardingFormData {
  companyName: string;
  phone?: string;
}

const CompanyOnboardingPage: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const user = useAppSelector(selectUser);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { register, handleSubmit, formState: { errors } } = useForm<CompanyOnboardingFormData>({
    resolver: yupResolver(companyOnboardingSchema) as any,
    defaultValues: {
      companyName: user?.companyName || '',
      phone: user?.phone || '',
    }
  });

  const onSubmit = async (data: CompanyOnboardingFormData) => {
    try {
      setLoading(true);
      setError(null);

      // Update user profile with company information
      await authService.updateProfile({
        ...data,
        firstName: user?.firstName,
        lastName: user?.lastName,
      });

      // Refetch user to get updated companyId
      await dispatch(updateProfile(data)).unwrap();

      toast.success('Company information saved successfully!');
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Failed to save company information');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box display="flex" alignItems="center" justifyContent="center" minHeight="100vh" sx={{ backgroundColor: 'background.default', p: 2 }}>
      <Card sx={{ maxWidth: 500, width: '100%' }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h4" align="center" gutterBottom fontWeight={600}>
            Welcome to SEV Fleet!
          </Typography>
          <Typography variant="body1" align="center" color="text.secondary" paragraph>
            Please provide your company information to complete your registration.
          </Typography>

          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

          <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
            <TextField
              fullWidth
              label="Company Name"
              {...register('companyName')}
              error={!!errors.companyName}
              helperText={errors.companyName?.message}
              disabled={loading}
              required
              sx={{ mb: 2 }}
            />

            <TextField
              fullWidth
              label="Phone Number (Optional)"
              {...register('phone')}
              error={!!errors.phone}
              helperText={errors.phone?.message}
              disabled={loading}
              sx={{ mb: 3 }}
            />

            <Button
              fullWidth
              variant="contained"
              size="large"
              type="submit"
              disabled={loading}
            >
              {loading ? <LoadingSpinner size={24} /> : 'Complete Setup'}
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default CompanyOnboardingPage;

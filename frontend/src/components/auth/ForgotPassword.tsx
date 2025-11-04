import React, { useState } from 'react';
import { Box, Card, CardContent, TextField, Button, Typography, Link, Alert } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { useAppDispatch } from '../../redux/hooks';
import { resetPassword } from '../../redux/slices/authSlice';
import { forgotPasswordSchema } from '../../utils/validators';

interface ForgotPasswordFormData {
  email: string;
}

const ForgotPassword: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  const { register, handleSubmit, formState: { errors } } = useForm<ForgotPasswordFormData>({
    resolver: yupResolver(forgotPasswordSchema),
  });

  const onSubmit = async (data: ForgotPasswordFormData) => {
    setLoading(true);
    setError('');
    try {
      await dispatch(resetPassword(data.email)).unwrap();
      setSuccess(true);
    } catch (err: any) {
      setError(err.message || 'Failed to send reset email');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box display="flex" alignItems="center" justifyContent="center" minHeight="100vh" sx={{ backgroundColor: 'background.default', p: 2 }}>
      <Card sx={{ maxWidth: 450, width: '100%' }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h4" align="center" gutterBottom fontWeight={600}>
            Reset Password
          </Typography>
          <Typography variant="body2" align="center" color="text.secondary" paragraph>
            Enter your email address and we'll send you a link to reset your password
          </Typography>

          {success ? (
            <Alert severity="success" sx={{ mb: 2 }}>
              Password reset email sent! Check your inbox.
            </Alert>
          ) : (
            <>
              {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

              <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
                <TextField fullWidth label="Email" type="email" margin="normal" {...register('email')} error={!!errors.email} helperText={errors.email?.message} disabled={loading} />

                <Button fullWidth variant="contained" size="large" type="submit" disabled={loading} sx={{ mt: 3, mb: 2 }}>
                  {loading ? 'Sending...' : 'Send Reset Link'}
                </Button>
              </Box>
            </>
          )}

          <Typography variant="body2" align="center" color="text.secondary">
            <Link component="button" type="button" variant="body2" onClick={() => navigate('/login')} sx={{ textDecoration: 'none', fontWeight: 600 }}>
              Back to Sign In
            </Link>
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
};

export default ForgotPassword;

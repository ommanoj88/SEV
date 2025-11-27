import React, { useState } from 'react';
import {
  Box,
  TextField,
  Button,
  Typography,
  Link,
  Divider,
  Alert,
  InputAdornment,
  IconButton,
  alpha,
  Chip,
} from '@mui/material';
import { 
  Visibility, 
  VisibilityOff, 
  Google as GoogleIcon, 
  Email, 
  Lock,
  DirectionsCar,
  TrendingUp,
  Security,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { useAppDispatch, useAppSelector } from '../../redux/hooks';
import { loginWithEmail, loginWithGoogle, selectAuthError, selectAuthLoading } from '../../redux/slices/authSlice';
import { loginSchema } from '../../utils/validators';
import LoadingSpinner from '../common/LoadingSpinner';

interface LoginFormData {
  email: string;
  password: string;
}

const Login: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const error = useAppSelector(selectAuthError);
  const loading = useAppSelector(selectAuthLoading);

  const [showPassword, setShowPassword] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: yupResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    try {
      await dispatch(loginWithEmail(data)).unwrap();
      navigate('/dashboard');
    } catch (err: any) {
      console.error('[Login] Email login error:', err);
    }
  };

  const handleGoogleLogin = async () => {
    try {
      await dispatch(loginWithGoogle()).unwrap();
      navigate('/dashboard');
    } catch (err: any) {
      console.error('[Login] Google login error:', err);
    }
  };

  const features = [
    { icon: <DirectionsCar />, text: 'Multi-Fuel Fleet Management' },
    { icon: <TrendingUp />, text: 'Real-time Analytics & Insights' },
    { icon: <Security />, text: 'Enterprise-Grade Security' },
  ];

  return (
    <Box
      display="flex"
      alignItems="stretch"
      minHeight="100vh"
      sx={{ 
        background: (theme) => theme.palette.background.default,
      }}
    >
      {/* Left Panel - Branding */}
      <Box
        sx={{
          display: { xs: 'none', lg: 'flex' },
          width: '45%',
          background: (theme) => 
            theme.palette.mode === 'light'
              ? 'linear-gradient(135deg, #0052CC 0%, #0747A6 50%, #003A8C 100%)'
              : 'linear-gradient(135deg, #161B22 0%, #21262D 50%, #30363D 100%)',
          flexDirection: 'column',
          justifyContent: 'center',
          p: 8,
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        {/* Abstract Background Shapes */}
        <Box
          sx={{
            position: 'absolute',
            top: '10%',
            right: '-20%',
            width: '500px',
            height: '500px',
            borderRadius: '50%',
            background: 'rgba(255, 255, 255, 0.03)',
          }}
        />
        <Box
          sx={{
            position: 'absolute',
            bottom: '-10%',
            left: '-10%',
            width: '400px',
            height: '400px',
            borderRadius: '50%',
            background: 'rgba(255, 255, 255, 0.02)',
          }}
        />

        <Box sx={{ position: 'relative', zIndex: 1, maxWidth: 480 }}>
          {/* Logo/Brand */}
          <Box display="flex" alignItems="center" gap={1.5} mb={6}>
            <Box
              sx={{
                width: 48,
                height: 48,
                borderRadius: 2,
                background: 'rgba(255, 255, 255, 0.1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                backdropFilter: 'blur(10px)',
              }}
            >
              <DirectionsCar sx={{ color: '#FFFFFF', fontSize: 28 }} />
            </Box>
            <Typography 
              variant="h5" 
              fontWeight={700} 
              color="#FFFFFF"
              letterSpacing="-0.02em"
            >
              Smart Fleet
            </Typography>
          </Box>

          <Typography
            variant="h2"
            fontWeight={700}
            color="#FFFFFF"
            sx={{ mb: 2, lineHeight: 1.2 }}
          >
            Intelligent Fleet Management for the Modern Enterprise
          </Typography>

          <Typography
            variant="body1"
            sx={{ 
              color: 'rgba(255, 255, 255, 0.75)',
              mb: 5,
              lineHeight: 1.7,
              fontSize: '1.0625rem',
            }}
          >
            Streamline operations, reduce costs, and gain actionable insights with our comprehensive multi-fuel fleet management platform.
          </Typography>

          {/* Feature Pills */}
          <Box display="flex" flexDirection="column" gap={2}>
            {features.map((feature, index) => (
              <Box 
                key={index}
                display="flex" 
                alignItems="center" 
                gap={2}
                className="fade-in"
                sx={{ 
                  animationDelay: `${0.2 + index * 0.1}s`,
                  opacity: 0,
                  animationFillMode: 'forwards',
                }}
              >
                <Box
                  sx={{
                    width: 40,
                    height: 40,
                    borderRadius: 2,
                    background: 'rgba(255, 255, 255, 0.1)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#FFFFFF',
                  }}
                >
                  {feature.icon}
                </Box>
                <Typography 
                  variant="body1" 
                  fontWeight={500} 
                  color="rgba(255, 255, 255, 0.9)"
                >
                  {feature.text}
                </Typography>
              </Box>
            ))}
          </Box>

          {/* Trust Badges */}
          <Box mt={8}>
            <Typography 
              variant="caption" 
              color="rgba(255, 255, 255, 0.5)" 
              fontWeight={600}
              letterSpacing="0.1em"
              textTransform="uppercase"
            >
              Trusted by leading enterprises
            </Typography>
            <Box display="flex" gap={3} mt={2}>
              <Chip 
                label="SOC 2 Compliant" 
                size="small"
                sx={{ 
                  bgcolor: 'rgba(255, 255, 255, 0.1)',
                  color: 'rgba(255, 255, 255, 0.8)',
                  fontWeight: 600,
                  fontSize: '0.75rem',
                }}
              />
              <Chip 
                label="ISO 27001" 
                size="small"
                sx={{ 
                  bgcolor: 'rgba(255, 255, 255, 0.1)',
                  color: 'rgba(255, 255, 255, 0.8)',
                  fontWeight: 600,
                  fontSize: '0.75rem',
                }}
              />
              <Chip 
                label="GDPR Ready" 
                size="small"
                sx={{ 
                  bgcolor: 'rgba(255, 255, 255, 0.1)',
                  color: 'rgba(255, 255, 255, 0.8)',
                  fontWeight: 600,
                  fontSize: '0.75rem',
                }}
              />
            </Box>
          </Box>
        </Box>
      </Box>

      {/* Right Panel - Login Form */}
      <Box
        sx={{
          flex: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          p: { xs: 3, sm: 4, md: 6 },
        }}
      >
        <Box sx={{ width: '100%', maxWidth: 420 }} className="fade-in">
          {/* Mobile Logo */}
          <Box 
            sx={{ 
              display: { xs: 'flex', lg: 'none' },
              alignItems: 'center',
              gap: 1.5,
              mb: 4,
              justifyContent: 'center',
            }}
          >
            <Box
              sx={{
                width: 40,
                height: 40,
                borderRadius: 2,
                background: (theme) => alpha(theme.palette.primary.main, 0.1),
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <DirectionsCar sx={{ color: 'primary.main', fontSize: 24 }} />
            </Box>
            <Typography variant="h6" fontWeight={700} color="text.primary">
              Smart Fleet
            </Typography>
          </Box>

          <Box mb={4}>
            <Typography 
              variant="h4" 
              fontWeight={700}
              color="text.primary"
              sx={{ mb: 1 }}
            >
              Welcome back
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Enter your credentials to access your account
            </Typography>
          </Box>

          {error && (
            <Alert 
              severity="error" 
              sx={{ mb: 3 }}
            >
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
            <Box mb={2.5}>
              <Typography 
                variant="body2" 
                fontWeight={600} 
                color="text.primary" 
                mb={0.75}
              >
                Email address
              </Typography>
              <TextField
                fullWidth
                placeholder="name@company.com"
                type="email"
                {...register('email')}
                error={!!errors.email}
                helperText={errors.email?.message}
                disabled={loading}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Email sx={{ color: 'text.disabled', fontSize: 20 }} />
                    </InputAdornment>
                  ),
                }}
              />
            </Box>

            <Box mb={1}>
              <Typography 
                variant="body2" 
                fontWeight={600} 
                color="text.primary" 
                mb={0.75}
              >
                Password
              </Typography>
              <TextField
                fullWidth
                placeholder="Enter your password"
                type={showPassword ? 'text' : 'password'}
                {...register('password')}
                error={!!errors.password}
                helperText={errors.password?.message}
                disabled={loading}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Lock sx={{ color: 'text.disabled', fontSize: 20 }} />
                    </InputAdornment>
                  ),
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        onClick={() => setShowPassword(!showPassword)}
                        edge="end"
                        size="small"
                        aria-label="toggle password visibility"
                      >
                        {showPassword ? 
                          <VisibilityOff sx={{ fontSize: 20 }} /> : 
                          <Visibility sx={{ fontSize: 20 }} />
                        }
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />
            </Box>

            <Box textAlign="right" mb={3}>
              <Link
                component="button"
                type="button"
                variant="body2"
                onClick={() => navigate('/forgot-password')}
                sx={{ 
                  textDecoration: 'none',
                  fontWeight: 600,
                  color: 'primary.main',
                  '&:hover': {
                    textDecoration: 'underline',
                  },
                }}
              >
                Forgot password?
              </Link>
            </Box>

            <Button
              fullWidth
              variant="contained"
              size="large"
              type="submit"
              disabled={loading}
              sx={{ 
                mb: 2.5,
                py: 1.5,
                fontWeight: 600,
                fontSize: '0.9375rem',
              }}
            >
              {loading ? <LoadingSpinner size={22} /> : 'Sign in'}
            </Button>

            <Divider sx={{ my: 3 }}>
              <Typography 
                variant="caption" 
                color="text.secondary" 
                fontWeight={600}
                sx={{ px: 2 }}
              >
                OR CONTINUE WITH
              </Typography>
            </Divider>

            <Button
              fullWidth
              variant="outlined"
              size="large"
              startIcon={<GoogleIcon />}
              onClick={handleGoogleLogin}
              disabled={loading}
              sx={{ 
                mb: 4,
                py: 1.5,
                fontWeight: 600,
                fontSize: '0.9375rem',
                color: 'text.primary',
                borderColor: (theme) => theme.palette.divider,
                '&:hover': {
                  borderColor: 'text.secondary',
                  backgroundColor: (theme) => alpha(theme.palette.primary.main, 0.04),
                },
              }}
            >
              Google
            </Button>

            <Box textAlign="center">
              <Typography variant="body2" color="text.secondary">
                Don't have an account?{' '}
                <Link
                  component="button"
                  type="button"
                  variant="body2"
                  onClick={() => navigate('/register')}
                  sx={{ 
                    textDecoration: 'none',
                    fontWeight: 600,
                    color: 'primary.main',
                    '&:hover': {
                      textDecoration: 'underline',
                    },
                  }}
                >
                  Create account
                </Link>
              </Typography>
            </Box>
          </Box>

          {/* Footer */}
          <Box mt={6} textAlign="center">
            <Typography variant="caption" color="text.disabled">
              By signing in, you agree to our{' '}
              <Link href="#" color="inherit" sx={{ fontWeight: 600 }}>Terms of Service</Link>
              {' '}and{' '}
              <Link href="#" color="inherit" sx={{ fontWeight: 600 }}>Privacy Policy</Link>
            </Typography>
          </Box>
        </Box>
      </Box>
    </Box>
  );
};

export default Login;

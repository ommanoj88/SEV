import React from 'react';
import { Box, CircularProgress, Typography, alpha } from '@mui/material';

interface LoadingSpinnerProps {
  message?: string;
  size?: number;
  fullScreen?: boolean;
}

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ 
  message = 'Loading...', 
  size = 40,
  fullScreen = false,
}) => {
  const content = (
    <Box
      display="flex"
      flexDirection="column"
      alignItems="center"
      justifyContent="center"
      gap={2}
    >
      {/* Modern spinner with gradient */}
      <Box
        sx={{
          position: 'relative',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        {/* Background circle */}
        <CircularProgress
          variant="determinate"
          value={100}
          size={size}
          thickness={4}
          sx={{
            color: (theme) => alpha(theme.palette.primary.main, 0.1),
            position: 'absolute',
          }}
        />
        {/* Spinning circle */}
        <CircularProgress
          size={size}
          thickness={4}
          sx={{
            '& .MuiCircularProgress-circle': {
              strokeLinecap: 'round',
            },
          }}
        />
      </Box>

      {/* Message */}
      {message && (
        <Typography 
          variant="body2" 
          color="text.secondary"
          fontWeight={500}
          sx={{ mt: 0.5 }}
        >
          {message}
        </Typography>
      )}
    </Box>
  );

  if (fullScreen) {
    return (
      <Box
        sx={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          bgcolor: (theme) => alpha(theme.palette.background.default, 0.9),
          backdropFilter: 'blur(4px)',
          zIndex: 9999,
        }}
      >
        {content}
      </Box>
    );
  }

  return (
    <Box
      display="flex"
      alignItems="center"
      justifyContent="center"
      minHeight="200px"
    >
      {content}
    </Box>
  );
};

export default LoadingSpinner;

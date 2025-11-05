import React from 'react';
import { Box } from '@mui/material';
import { keyframes } from '@mui/system';

const fadeIn = keyframes`
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
`;

const slideInFromLeft = keyframes`
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
`;

const slideInFromRight = keyframes`
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
`;

const scaleIn = keyframes`
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
`;

interface PageTransitionProps {
  children: React.ReactNode;
  animation?: 'fadeIn' | 'slideLeft' | 'slideRight' | 'scaleIn';
  duration?: number;
  delay?: number;
}

const PageTransition: React.FC<PageTransitionProps> = ({
  children,
  animation = 'fadeIn',
  duration = 0.5,
  delay = 0,
}) => {
  const animationMap = {
    fadeIn,
    slideLeft: slideInFromLeft,
    slideRight: slideInFromRight,
    scaleIn,
  };

  return (
    <Box
      sx={{
        animation: `${animationMap[animation]} ${duration}s cubic-bezier(0.4, 0, 0.2, 1) ${delay}s both`,
      }}
    >
      {children}
    </Box>
  );
};

export default PageTransition;

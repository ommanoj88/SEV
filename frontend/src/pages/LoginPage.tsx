import React from 'react';
import { Container, Box } from '@mui/material';
import Login from '@components/auth/Login';

const LoginPage: React.FC = () => {
  return (
    <Container maxWidth="sm">
      <Box sx={{ mt: 8 }}>
        <Login />
      </Box>
    </Container>
  );
};

export default LoginPage;

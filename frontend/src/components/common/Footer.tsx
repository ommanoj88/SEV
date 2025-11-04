import React from 'react';
import { Box, Container, Typography, Link, Grid } from '@mui/material';

const Footer: React.FC = () => {
  const currentYear = new Date().getFullYear();

  return (
    <Box
      component="footer"
      sx={{
        py: 3,
        px: 2,
        mt: 'auto',
        backgroundColor: (theme) =>
          theme.palette.mode === 'light'
            ? theme.palette.grey[200]
            : theme.palette.grey[800],
      }}
    >
      <Container maxWidth="lg">
        <Grid container spacing={4}>
          <Grid item xs={12} sm={4}>
            <Typography variant="h6" color="text.primary" gutterBottom>
              EV Fleet Management
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Comprehensive electric vehicle fleet management solution for modern businesses.
            </Typography>
          </Grid>
          <Grid item xs={12} sm={4}>
            <Typography variant="h6" color="text.primary" gutterBottom>
              Quick Links
            </Typography>
            <Link href="/dashboard" color="inherit" display="block" sx={{ mb: 1 }}>
              Dashboard
            </Link>
            <Link href="/fleet" color="inherit" display="block" sx={{ mb: 1 }}>
              Fleet Management
            </Link>
            <Link href="/analytics" color="inherit" display="block" sx={{ mb: 1 }}>
              Analytics
            </Link>
          </Grid>
          <Grid item xs={12} sm={4}>
            <Typography variant="h6" color="text.primary" gutterBottom>
              Support
            </Typography>
            <Link href="#" color="inherit" display="block" sx={{ mb: 1 }}>
              Documentation
            </Link>
            <Link href="#" color="inherit" display="block" sx={{ mb: 1 }}>
              Contact Us
            </Link>
            <Link href="#" color="inherit" display="block" sx={{ mb: 1 }}>
              Privacy Policy
            </Link>
          </Grid>
        </Grid>
        <Box mt={3}>
          <Typography variant="body2" color="text.secondary" align="center">
            {'Copyright © '}
            <Link color="inherit" href="/">
              EV Fleet Management
            </Link>{' '}
            {currentYear}
            {'. All rights reserved.'}
          </Typography>
        </Box>
      </Container>
    </Box>
  );
};

export default Footer;

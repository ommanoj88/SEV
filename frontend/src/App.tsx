import React, { useState, useMemo } from 'react';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider, CssBaseline, Box } from '@mui/material';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { useAuth } from './hooks/useAuth';
import getTheme from './styles/theme';
import './styles/global.css';
import ErrorBoundary from './components/common/ErrorBoundary';
import Header from './components/common/Header';
import Sidebar, { DRAWER_WIDTH } from './components/common/Sidebar';
import AppRoutes from './routes';
import LoadingSpinner from './components/common/LoadingSpinner';

const App: React.FC = () => {
  const { isAuthenticated, loading } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [themeMode] = useState<'light' | 'dark'>('light');

  const theme = useMemo(() => getTheme(themeMode), [themeMode]);

  const handleSidebarToggle = () => {
    setSidebarOpen(!sidebarOpen);
  };

  if (loading) {
    return (
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <LoadingSpinner message="Loading application..." />
      </ThemeProvider>
    );
  }

  return (
    <ErrorBoundary>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <BrowserRouter>
          {isAuthenticated ? (
            <Box sx={{ display: 'flex' }}>
              <Header onMenuClick={handleSidebarToggle} />
              <Sidebar open={sidebarOpen} onClose={handleSidebarToggle} />
              <Box
                component="main"
                sx={{
                  flexGrow: 1,
                  p: 3,
                  width: { sm: `calc(100% - ${DRAWER_WIDTH}px)` },
                  mt: 8,
                }}
              >
                <AppRoutes />
              </Box>
            </Box>
          ) : (
            <AppRoutes />
          )}
          <ToastContainer
            position="top-right"
            autoClose={5000}
            hideProgressBar={false}
            newestOnTop
            closeOnClick
            rtl={false}
            pauseOnFocusLoss
            draggable
            pauseOnHover
          />
        </BrowserRouter>
      </ThemeProvider>
    </ErrorBoundary>
  );
};

export default App;

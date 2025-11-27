import React, { useState, useMemo } from 'react';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider, CssBaseline, Box } from '@mui/material';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { useAuth } from './hooks/useAuth';
import getModernTheme from './styles/modernTheme';
import './styles/global.css';
import ErrorBoundary from './components/common/ErrorBoundary';
import Header from './components/common/Header';
import Sidebar, { DRAWER_WIDTH } from './components/common/Sidebar';
import AppRoutes from './routes';
import LoadingSpinner from './components/common/LoadingSpinner';

const App: React.FC = () => {
  const auth = useAuth();
  const { isAuthenticated, loading, user } = auth;
  const initialized = (auth as any).initialized;
  
  // Debug logging
  console.log('[App] Auth state:', { isAuthenticated, loading, initialized, hasUser: !!user });
  
  const [sidebarOpen, setSidebarOpen] = useState(false);
  
  // Get system preference for dark mode
  const prefersDarkMode = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
  const [themeMode, setThemeMode] = useState<'light' | 'dark'>(
    localStorage.getItem('themeMode') as 'light' | 'dark' || (prefersDarkMode ? 'dark' : 'light')
  );

  const theme = useMemo(() => getModernTheme(themeMode), [themeMode]);

  const handleSidebarToggle = () => {
    setSidebarOpen(!sidebarOpen);
  };

  const handleThemeToggle = () => {
    const newMode = themeMode === 'light' ? 'dark' : 'light';
    setThemeMode(newMode);
    localStorage.setItem('themeMode', newMode);
  };

  // Show loading until auth is fully initialized (Firebase checked AND user fetched if logged in)
  if (!initialized || loading) {
    return (
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <LoadingSpinner message="Loading application..." />
      </ThemeProvider>
    );
  }

  // Show Header and Sidebar for authenticated users (just need isAuthenticated)
  const showNavigation = isAuthenticated;

  return (
    <ErrorBoundary>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
          {showNavigation ? (
            <Box sx={{ display: 'flex' }}>
              <Header
                onMenuClick={handleSidebarToggle}
                onThemeToggle={handleThemeToggle}
                themeMode={themeMode}
              />
              <Sidebar open={sidebarOpen} onClose={handleSidebarToggle} />
              <Box
                component="main"
                sx={{
                  flexGrow: 1,
                  p: 3,
                  width: { sm: `calc(100% - ${DRAWER_WIDTH}px)` },
                  ml: { md: `${DRAWER_WIDTH}px` },
                  mt: 8,
                  minHeight: '100vh',
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
            theme={themeMode}
          />
        </BrowserRouter>
      </ThemeProvider>
    </ErrorBoundary>
  );
};

export default App;

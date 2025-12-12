import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAppSelector } from './redux/hooks';
import { selectIsAuthenticated } from './redux/slices/authSlice';
import ProtectedRoute from './components/common/ProtectedRoute';
import Login from './components/auth/Login';
import Register from './components/auth/Register';
import ForgotPassword from './components/auth/ForgotPassword';
import AnalyticsPage from './pages/AnalyticsPage';
import BillingPage from './pages/BillingPage';
import ChargingPage from './pages/ChargingPage';
import CompanyOnboardingPage from './pages/CompanyOnboardingPage';
import CustomerManagementPage from './pages/CustomerManagementPage';
import DashboardPage from './pages/DashboardPage';
import DocumentManagementPage from './pages/DocumentManagementPage';
import DriversPage from './pages/DriversPage';
import ExpenseManagementPage from './pages/ExpenseManagementPage';
import FleetManagementPage from './pages/FleetManagementPage';
import GeofenceManagementPage from './pages/GeofenceManagementPage';
import MaintenancePage from './pages/MaintenancePage';
import ProfilePage from './pages/ProfilePage';
import RouteOptimizationPage from './pages/RouteOptimizationPage';
import SettingsPage from './pages/SettingsPage';
import StationDiscoveryPage from './pages/StationDiscoveryPage';
import VehicleReportPage from './pages/VehicleReportPage';
import DeveloperDocsPage from './pages/DeveloperDocsPage';

// Component to redirect authenticated users away from auth pages
const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isAuthenticated = useAppSelector(selectIsAuthenticated);

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};

const AppRoutes: React.FC = () => {
  return (
    <Routes>
      {/* Public routes - redirect to dashboard if already authenticated */}
      <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
      <Route path="/register" element={<PublicRoute><Register /></PublicRoute>} />
      <Route path="/forgot-password" element={<PublicRoute><ForgotPassword /></PublicRoute>} />
      
      {/* Developer documentation - no auth required */}
      <Route path="/developer-docs" element={<DeveloperDocsPage />} />

      {/* Company onboarding - for users who logged in via Google without company info */}
      <Route
        path="/onboarding/company"
        element={
          <ProtectedRoute>
            <CompanyOnboardingPage />
          </ProtectedRoute>
        }
      />

      {/* Protected routes */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/fleet/*"
        element={
          <ProtectedRoute>
            <FleetManagementPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/charging"
        element={
          <ProtectedRoute>
            <ChargingPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/stations"
        element={
          <ProtectedRoute>
            <StationDiscoveryPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/drivers/*"
        element={
          <ProtectedRoute>
            <DriversPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/maintenance"
        element={
          <ProtectedRoute>
            <MaintenancePage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/analytics"
        element={
          <ProtectedRoute>
            <AnalyticsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/billing"
        element={
          <ProtectedRoute>
            <BillingPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <ProfilePage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/settings"
        element={
          <ProtectedRoute>
            <SettingsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/reports"
        element={
          <ProtectedRoute>
            <VehicleReportPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/documents"
        element={
          <ProtectedRoute>
            <DocumentManagementPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/expenses"
        element={
          <ProtectedRoute>
            <ExpenseManagementPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/routes"
        element={
          <ProtectedRoute>
            <RouteOptimizationPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/customers"
        element={
          <ProtectedRoute>
            <CustomerManagementPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/geofencing"
        element={
          <ProtectedRoute>
            <GeofenceManagementPage />
          </ProtectedRoute>
        }
      />

      {/* Default redirect */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
};

export default AppRoutes;

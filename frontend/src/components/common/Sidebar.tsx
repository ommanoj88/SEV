import React from 'react';
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Box,
  Typography,
  alpha,
  Chip,
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  DirectionsCar as VehicleIcon,
  EvStation as ChargingIcon,
  People as DriversIcon,
  Build as MaintenanceIcon,
  Analytics as AnalyticsIcon,
  Receipt as BillingIcon,
  Settings as SettingsIcon,
  PictureAsPdf as ReportIcon,
  Description as DocumentIcon,
  AttachMoney as ExpenseIcon,
  Route as RouteIcon,
  PersonOutline as CustomerIcon,
  Notifications as NotificationsIcon,
  HelpOutline as HelpIcon,
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';

const DRAWER_WIDTH = 260;

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

interface MenuItemType {
  text: string;
  icon: React.ReactNode;
  path: string;
  badge?: string;
}

interface MenuSectionType {
  title: string;
  items: MenuItemType[];
}

const menuSections: MenuSectionType[] = [
  {
    title: 'Overview',
    items: [
      { text: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard' },
      { text: 'Analytics', icon: <AnalyticsIcon />, path: '/analytics' },
    ],
  },
  {
    title: 'Fleet Operations',
    items: [
      { text: 'Vehicles', icon: <VehicleIcon />, path: '/fleet' },
      { text: 'Drivers', icon: <DriversIcon />, path: '/drivers' },
      { text: 'Routes', icon: <RouteIcon />, path: '/routes' },
      { text: 'Charging', icon: <ChargingIcon />, path: '/charging' },
    ],
  },
  {
    title: 'Management',
    items: [
      { text: 'Maintenance', icon: <MaintenanceIcon />, path: '/maintenance' },
      { text: 'Documents', icon: <DocumentIcon />, path: '/documents' },
      { text: 'Expenses', icon: <ExpenseIcon />, path: '/expenses' },
      { text: 'Customers', icon: <CustomerIcon />, path: '/customers' },
    ],
  },
  {
    title: 'Finance',
    items: [
      { text: 'Billing', icon: <BillingIcon />, path: '/billing' },
      { text: 'Reports', icon: <ReportIcon />, path: '/reports' },
    ],
  },
];

const bottomMenuItems: MenuItemType[] = [
  { text: 'Notifications', icon: <NotificationsIcon />, path: '/notifications' },
  { text: 'Settings', icon: <SettingsIcon />, path: '/settings' },
  { text: 'Help & Support', icon: <HelpIcon />, path: '/help' },
];

const Sidebar: React.FC<SidebarProps> = ({ open, onClose }) => {
  const navigate = useNavigate();
  const location = useLocation();

  const handleNavigation = (path: string) => {
    navigate(path);
    onClose();
  };

  const isActive = (path: string) => {
    return location.pathname === path || location.pathname.startsWith(path + '/');
  };

  const renderMenuItem = (item: MenuItemType) => {
    const active = isActive(item.path);
    return (
      <ListItem key={item.text} disablePadding sx={{ mb: 0.25 }}>
        <ListItemButton
          selected={active}
          onClick={() => handleNavigation(item.path)}
          sx={{
            minHeight: 42,
            px: 1.5,
            position: 'relative',
          }}
        >
          <ListItemIcon 
            sx={{ 
              color: active ? 'primary.main' : 'text.secondary',
              minWidth: 36,
            }}
          >
            {item.icon}
          </ListItemIcon>
          <ListItemText 
            primary={item.text}
            primaryTypographyProps={{
              fontWeight: active ? 600 : 500,
              fontSize: '0.875rem',
              color: active ? 'text.primary' : 'text.secondary',
            }}
          />
          {item.badge && (
            <Chip
              label={item.badge}
              size="small"
              sx={{
                height: 18,
                fontSize: '0.65rem',
                fontWeight: 700,
                bgcolor: active ? 'primary.main' : 'action.selected',
                color: active ? 'primary.contrastText' : 'text.secondary',
              }}
            />
          )}
        </ListItemButton>
      </ListItem>
    );
  };

  const drawer = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      {/* Spacer for AppBar */}
      <Toolbar sx={{ minHeight: { xs: 56, sm: 64 } }} />
      
      {/* Main Navigation */}
      <Box sx={{ flexGrow: 1, overflowY: 'auto', py: 1 }}>
        {menuSections.map((section, sectionIndex) => (
          <Box key={section.title} sx={{ mb: 1.5 }}>
            <Typography
              variant="overline"
              sx={{
                px: 2.5,
                py: 1,
                display: 'block',
                color: 'text.disabled',
                fontSize: '0.65rem',
                letterSpacing: '0.1em',
              }}
            >
              {section.title}
            </Typography>
            <List disablePadding sx={{ px: 1 }}>
              {section.items.map(renderMenuItem)}
            </List>
          </Box>
        ))}
      </Box>

      {/* Bottom Section */}
      <Box sx={{ borderTop: (theme) => `1px solid ${theme.palette.divider}`, py: 1 }}>
        <List disablePadding sx={{ px: 1 }}>
          {bottomMenuItems.map(renderMenuItem)}
        </List>
      </Box>

      {/* Footer */}
      <Box 
        sx={{ 
          px: 2, 
          py: 2, 
          borderTop: (theme) => `1px solid ${theme.palette.divider}`,
        }}
      >
        <Box
          sx={{
            p: 1.5,
            borderRadius: 2,
            background: (theme) => 
              theme.palette.mode === 'light'
                ? alpha(theme.palette.primary.main, 0.04)
                : alpha(theme.palette.primary.main, 0.08),
            border: (theme) => 
              `1px solid ${alpha(theme.palette.primary.main, 0.1)}`,
          }}
        >
          <Typography 
            variant="caption" 
            fontWeight={600} 
            color="primary.main"
            sx={{ display: 'block', mb: 0.25 }}
          >
            Smart Fleet Pro
          </Typography>
          <Typography 
            variant="caption" 
            color="text.secondary"
            sx={{ fontSize: '0.6875rem' }}
          >
            Enterprise Edition v2.0
          </Typography>
        </Box>
      </Box>
    </Box>
  );

  return (
    <>
      {/* Desktop drawer */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          width: DRAWER_WIDTH,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            boxSizing: 'border-box',
          },
        }}
      >
        {drawer}
      </Drawer>

      {/* Mobile drawer */}
      <Drawer
        variant="temporary"
        open={open}
        onClose={onClose}
        ModalProps={{
          keepMounted: true,
          disableEnforceFocus: true,
          disableRestoreFocus: true,
        }}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            boxSizing: 'border-box',
          },
        }}
      >
        {drawer}
      </Drawer>
    </>
  );
};

export default Sidebar;
export { DRAWER_WIDTH };

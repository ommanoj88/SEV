import React, { useState } from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  Badge,
  Menu,
  MenuItem,
  Avatar,
  Box,
  Divider,
  ListItemIcon,
  Tooltip,
  alpha,
  Chip,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Notifications as NotificationsIcon,
  AccountCircle,
  Settings,
  Logout,
  Brightness4,
  Brightness7,
  DirectionsCar,
  KeyboardArrowDown,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../redux/hooks';
import { logout, selectUser } from '../../redux/slices/authSlice';
import { selectUnreadCount } from '../../redux/slices/notificationSlice';

interface HeaderProps {
  onMenuClick: () => void;
  onThemeToggle: () => void;
  themeMode: 'light' | 'dark';
}

const Header: React.FC<HeaderProps> = ({ onMenuClick, onThemeToggle, themeMode }) => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const user = useAppSelector(selectUser);
  const unreadCount = useAppSelector(selectUnreadCount);

  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleProfile = () => {
    handleMenuClose();
    navigate('/profile');
  };

  const handleSettings = () => {
    handleMenuClose();
    navigate('/settings');
  };

  const handleLogout = async () => {
    handleMenuClose();
    await dispatch(logout());
    navigate('/login');
  };

  const getUserInitials = () => {
    if (user?.firstName && user?.lastName) {
      return `${user.firstName[0]}${user.lastName[0]}`.toUpperCase();
    }
    return user?.email?.[0]?.toUpperCase() || 'U';
  };

  return (
    <AppBar 
      position="fixed" 
      elevation={0}
      sx={{ 
        zIndex: (theme) => theme.zIndex.drawer + 1,
        borderBottom: (theme) => `1px solid ${theme.palette.divider}`,
      }}
    >
      <Toolbar sx={{ minHeight: { xs: 56, sm: 64 }, px: { xs: 2, sm: 3 } }}>
        {/* Mobile Menu Button */}
        <IconButton
          color="inherit"
          edge="start"
          onClick={onMenuClick}
          sx={{ 
            mr: 2,
            display: { md: 'none' },
          }}
        >
          <MenuIcon />
        </IconButton>

        {/* Logo & Brand */}
        <Box 
          display="flex" 
          alignItems="center" 
          gap={1.5}
          sx={{ cursor: 'pointer' }}
          onClick={() => navigate('/dashboard')}
        >
          <Box
            sx={{
              width: 36,
              height: 36,
              borderRadius: 1.5,
              background: (theme) => 
                theme.palette.mode === 'light'
                  ? 'linear-gradient(135deg, #0052CC 0%, #0747A6 100%)'
                  : 'linear-gradient(135deg, #4C9AFF 0%, #2684FF 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: (theme) => 
                theme.palette.mode === 'light'
                  ? '0 2px 8px rgba(0, 82, 204, 0.25)'
                  : '0 2px 8px rgba(76, 154, 255, 0.3)',
            }}
          >
            <DirectionsCar sx={{ color: '#FFFFFF', fontSize: 22 }} />
          </Box>
          <Box display={{ xs: 'none', sm: 'block' }}>
            <Typography 
              variant="h6" 
              fontWeight={700}
              color="text.primary"
              sx={{ lineHeight: 1.2 }}
            >
              Smart Fleet
            </Typography>
          </Box>
        </Box>

        {/* Spacer */}
        <Box sx={{ flexGrow: 1 }} />

        {/* Right Actions */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
          {/* Theme Toggle */}
          <Tooltip title={`Switch to ${themeMode === 'light' ? 'dark' : 'light'} mode`}>
            <IconButton 
              onClick={onThemeToggle}
              sx={{
                color: 'text.secondary',
                '&:hover': {
                  color: 'text.primary',
                },
              }}
            >
              {themeMode === 'light' ? 
                <Brightness4 sx={{ fontSize: 22 }} /> : 
                <Brightness7 sx={{ fontSize: 22 }} />
              }
            </IconButton>
          </Tooltip>

          {/* Notifications */}
          <Tooltip title="Notifications">
            <IconButton 
              onClick={() => navigate('/notifications')}
              sx={{
                color: 'text.secondary',
                '&:hover': {
                  color: 'text.primary',
                },
              }}
            >
              <Badge 
                badgeContent={unreadCount} 
                color="error"
                max={99}
                sx={{
                  '& .MuiBadge-badge': {
                    fontSize: '0.65rem',
                    minWidth: 16,
                    height: 16,
                  },
                }}
              >
                <NotificationsIcon sx={{ fontSize: 22 }} />
              </Badge>
            </IconButton>
          </Tooltip>

          {/* Vertical Divider */}
          <Divider 
            orientation="vertical" 
            flexItem 
            sx={{ 
              mx: 1.5, 
              my: 1.5,
              display: { xs: 'none', sm: 'block' },
            }} 
          />

          {/* User Menu */}
          <Box
            onClick={handleMenuOpen}
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              cursor: 'pointer',
              py: 0.75,
              px: 1,
              borderRadius: 2,
              transition: 'all 0.15s ease',
              '&:hover': {
                bgcolor: (theme) => alpha(theme.palette.primary.main, 0.06),
              },
            }}
          >
            {user?.profileImageUrl ? (
              <Avatar 
                src={user.profileImageUrl} 
                sx={{ 
                  width: 34, 
                  height: 34,
                }} 
              />
            ) : (
              <Avatar
                sx={{
                  width: 34,
                  height: 34,
                  bgcolor: 'primary.main',
                  fontSize: '0.875rem',
                  fontWeight: 600,
                }}
              >
                {getUserInitials()}
              </Avatar>
            )}
            <Box display={{ xs: 'none', md: 'block' }}>
              <Typography 
                variant="body2" 
                fontWeight={600} 
                color="text.primary"
                sx={{ lineHeight: 1.3 }}
              >
                {user?.firstName || 'User'}
              </Typography>
              <Typography 
                variant="caption" 
                color="text.secondary"
                sx={{ lineHeight: 1 }}
              >
                {user?.role || 'Member'}
              </Typography>
            </Box>
            <KeyboardArrowDown 
              sx={{ 
                color: 'text.secondary', 
                fontSize: 18,
                display: { xs: 'none', md: 'block' },
              }} 
            />
          </Box>

          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleMenuClose}
            transformOrigin={{ horizontal: 'right', vertical: 'top' }}
            anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
            PaperProps={{
              sx: {
                mt: 1,
                minWidth: 240,
              },
            }}
          >
            {/* User Info Header */}
            <Box sx={{ px: 2, py: 1.5 }}>
              <Box display="flex" alignItems="center" gap={1.5}>
                {user?.profileImageUrl ? (
                  <Avatar 
                    src={user.profileImageUrl} 
                    sx={{ width: 40, height: 40 }} 
                  />
                ) : (
                  <Avatar
                    sx={{
                      width: 40,
                      height: 40,
                      bgcolor: 'primary.main',
                      fontSize: '1rem',
                      fontWeight: 600,
                    }}
                  >
                    {getUserInitials()}
                  </Avatar>
                )}
                <Box>
                  <Typography variant="subtitle2" fontWeight={600}>
                    {user?.firstName} {user?.lastName}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {user?.email}
                  </Typography>
                </Box>
              </Box>
              {user?.role && (
                <Chip
                  label={user.role}
                  size="small"
                  sx={{
                    mt: 1.5,
                    height: 22,
                    fontSize: '0.7rem',
                    fontWeight: 600,
                    bgcolor: (theme) => alpha(theme.palette.primary.main, 0.1),
                    color: 'primary.main',
                  }}
                />
              )}
            </Box>
            
            <Divider sx={{ my: 1 }} />
            
            <MenuItem onClick={handleProfile}>
              <ListItemIcon>
                <AccountCircle fontSize="small" />
              </ListItemIcon>
              <Typography variant="body2" fontWeight={500}>Profile</Typography>
            </MenuItem>
            <MenuItem onClick={handleSettings}>
              <ListItemIcon>
                <Settings fontSize="small" />
              </ListItemIcon>
              <Typography variant="body2" fontWeight={500}>Settings</Typography>
            </MenuItem>
            
            <Divider sx={{ my: 1 }} />
            
            <MenuItem 
              onClick={handleLogout}
              sx={{ color: 'error.main' }}
            >
              <ListItemIcon>
                <Logout fontSize="small" color="error" />
              </ListItemIcon>
              <Typography variant="body2" fontWeight={500}>Sign out</Typography>
            </MenuItem>
          </Menu>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header;

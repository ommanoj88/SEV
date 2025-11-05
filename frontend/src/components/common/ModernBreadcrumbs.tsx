import React from 'react';
import { Breadcrumbs, Link, Typography, Box, alpha } from '@mui/material';
import { NavigateNext, Home } from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';

interface BreadcrumbItem {
  label: string;
  path?: string;
  icon?: React.ReactNode;
}

interface ModernBreadcrumbsProps {
  items?: BreadcrumbItem[];
  showHome?: boolean;
}

const ModernBreadcrumbs: React.FC<ModernBreadcrumbsProps> = ({ items, showHome = true }) => {
  const navigate = useNavigate();
  const location = useLocation();

  // Auto-generate breadcrumbs from path if items not provided
  const breadcrumbItems = items || generateBreadcrumbsFromPath(location.pathname);

  const handleClick = (path?: string) => {
    if (path) {
      navigate(path);
    }
  };

  return (
    <Box
      sx={{
        mb: 3,
        p: 2,
        borderRadius: 2,
        background: (theme) =>
          theme.palette.mode === 'light'
            ? alpha(theme.palette.background.paper, 0.6)
            : alpha(theme.palette.background.paper, 0.4),
        backdropFilter: 'blur(10px)',
        border: (theme) => `1px solid ${alpha(theme.palette.divider, 0.1)}`,
      }}
    >
      <Breadcrumbs
        separator={<NavigateNext fontSize="small" sx={{ color: 'text.secondary' }} />}
        aria-label="breadcrumb"
      >
        {showHome && (
          <Link
            component="button"
            onClick={() => handleClick('/dashboard')}
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 0.5,
              color: 'text.secondary',
              textDecoration: 'none',
              fontWeight: 600,
              fontSize: '0.875rem',
              transition: 'all 0.2s ease',
              '&:hover': {
                color: 'primary.main',
                transform: 'translateY(-1px)',
              },
            }}
          >
            <Home fontSize="small" />
            Home
          </Link>
        )}
        
        {breadcrumbItems.map((item, index) => {
          const isLast = index === breadcrumbItems.length - 1;
          
          if (isLast) {
            return (
              <Typography
                key={item.label}
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 0.5,
                  color: 'primary.main',
                  fontWeight: 700,
                  fontSize: '0.875rem',
                }}
              >
                {item.icon}
                {item.label}
              </Typography>
            );
          }

          return (
            <Link
              key={item.label}
              component="button"
              onClick={() => handleClick(item.path)}
              sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 0.5,
                color: 'text.secondary',
                textDecoration: 'none',
                fontWeight: 600,
                fontSize: '0.875rem',
                transition: 'all 0.2s ease',
                '&:hover': {
                  color: 'primary.main',
                  transform: 'translateY(-1px)',
                },
              }}
            >
              {item.icon}
              {item.label}
            </Link>
          );
        })}
      </Breadcrumbs>
    </Box>
  );
};

// Helper function to generate breadcrumbs from path
function generateBreadcrumbsFromPath(pathname: string): BreadcrumbItem[] {
  const paths = pathname.split('/').filter(Boolean);
  
  return paths.map((path, index) => {
    const label = path
      .split('-')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
    
    const fullPath = '/' + paths.slice(0, index + 1).join('/');
    
    return {
      label,
      path: fullPath,
    };
  });
}

export default ModernBreadcrumbs;

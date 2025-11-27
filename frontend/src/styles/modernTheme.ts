import { createTheme, ThemeOptions, alpha, Shadows } from '@mui/material/styles';

/**
 * Modern Enhanced Theme for EV Fleet Management Platform
 * Implements latest UI/UX trends from 2025:
 * - Neomorphism/Glassmorphism effects
 * - Vibrant color palette with depth
 * - Advanced typography scale
 * - Micro-interactions and animations
 * - Enhanced accessibility
 */

const getModernTheme = (mode: 'light' | 'dark') => {
  // Modern color palette with vibrant gradients
  const colors = {
    light: {
      primary: {
        main: '#3B82F6', // Modern blue
        light: '#60A5FA',
        dark: '#2563EB',
        contrastText: '#FFFFFF',
      },
      secondary: {
        main: '#10B981', // Modern green (EV theme)
        light: '#34D399',
        dark: '#059669',
        contrastText: '#FFFFFF',
      },
      accent: {
        main: '#8B5CF6', // Purple accent
        light: '#A78BFA',
        dark: '#7C3AED',
      },
      error: {
        main: '#EF4444',
        light: '#F87171',
        dark: '#DC2626',
      },
      warning: {
        main: '#F59E0B',
        light: '#FBBF24',
        dark: '#D97706',
      },
      info: {
        main: '#06B6D4',
        light: '#22D3EE',
        dark: '#0891B2',
      },
      success: {
        main: '#10B981',
        light: '#34D399',
        dark: '#059669',
      },
      background: {
        default: '#F8FAFC',
        paper: '#FFFFFF',
        elevated: '#FFFFFF',
      },
      text: {
        primary: '#0F172A',
        secondary: '#64748B',
        disabled: '#CBD5E1',
      },
      divider: alpha('#0F172A', 0.08),
    },
    dark: {
      primary: {
        main: '#60A5FA',
        light: '#93C5FD',
        dark: '#3B82F6',
        contrastText: '#0F172A',
      },
      secondary: {
        main: '#34D399',
        light: '#6EE7B7',
        dark: '#10B981',
        contrastText: '#0F172A',
      },
      accent: {
        main: '#A78BFA',
        light: '#C4B5FD',
        dark: '#8B5CF6',
      },
      error: {
        main: '#F87171',
        light: '#FCA5A5',
        dark: '#EF4444',
      },
      warning: {
        main: '#FBBF24',
        light: '#FCD34D',
        dark: '#F59E0B',
      },
      info: {
        main: '#22D3EE',
        light: '#67E8F9',
        dark: '#06B6D4',
      },
      success: {
        main: '#34D399',
        light: '#6EE7B7',
        dark: '#10B981',
      },
      background: {
        default: '#0F172A',
        paper: '#1E293B',
        elevated: '#334155',
      },
      text: {
        primary: '#F1F5F9',
        secondary: '#94A3B8',
        disabled: '#475569',
      },
      divider: alpha('#F1F5F9', 0.12),
    },
  };

  const palette = mode === 'light' ? colors.light : colors.dark;

  const themeOptions: ThemeOptions = {
    palette: {
      mode,
      ...palette,
    },
    typography: {
      fontFamily: '"Inter", "SF Pro Display", "Segoe UI", "Roboto", -apple-system, BlinkMacSystemFont, sans-serif',
      h1: {
        fontSize: '3rem',
        fontWeight: 800,
        lineHeight: 1.2,
        letterSpacing: '-0.02em',
      },
      h2: {
        fontSize: '2.5rem',
        fontWeight: 700,
        lineHeight: 1.25,
        letterSpacing: '-0.01em',
      },
      h3: {
        fontSize: '2rem',
        fontWeight: 700,
        lineHeight: 1.3,
        letterSpacing: '-0.01em',
      },
      h4: {
        fontSize: '1.5rem',
        fontWeight: 600,
        lineHeight: 1.4,
        letterSpacing: '-0.005em',
      },
      h5: {
        fontSize: '1.25rem',
        fontWeight: 600,
        lineHeight: 1.5,
      },
      h6: {
        fontSize: '1.125rem',
        fontWeight: 600,
        lineHeight: 1.6,
      },
      subtitle1: {
        fontSize: '1rem',
        fontWeight: 500,
        lineHeight: 1.75,
      },
      subtitle2: {
        fontSize: '0.875rem',
        fontWeight: 600,
        lineHeight: 1.57,
        letterSpacing: '0.01em',
      },
      body1: {
        fontSize: '1rem',
        fontWeight: 400,
        lineHeight: 1.6,
      },
      body2: {
        fontSize: '0.875rem',
        fontWeight: 400,
        lineHeight: 1.5,
      },
      button: {
        fontSize: '0.875rem',
        fontWeight: 600,
        textTransform: 'none',
        letterSpacing: '0.02em',
      },
      caption: {
        fontSize: '0.75rem',
        fontWeight: 400,
        lineHeight: 1.4,
      },
      overline: {
        fontSize: '0.75rem',
        fontWeight: 700,
        lineHeight: 2,
        letterSpacing: '0.1em',
        textTransform: 'uppercase',
      },
    },
    shape: {
      borderRadius: 12,
    },
    shadows: (mode === 'light' 
      ? [
          'none',
          '0px 1px 2px rgba(0, 0, 0, 0.05)',
          '0px 2px 4px rgba(0, 0, 0, 0.05)',
          '0px 4px 6px -1px rgba(0, 0, 0, 0.1), 0px 2px 4px -1px rgba(0, 0, 0, 0.06)',
          '0px 10px 15px -3px rgba(0, 0, 0, 0.1), 0px 4px 6px -2px rgba(0, 0, 0, 0.05)',
          '0px 20px 25px -5px rgba(0, 0, 0, 0.1), 0px 10px 10px -5px rgba(0, 0, 0, 0.04)',
          '0px 25px 50px -12px rgba(0, 0, 0, 0.25)',
          ...Array(18).fill('none'),
        ]
      : [
          'none',
          '0px 1px 2px rgba(0, 0, 0, 0.3)',
          '0px 2px 4px rgba(0, 0, 0, 0.3)',
          '0px 4px 6px -1px rgba(0, 0, 0, 0.5), 0px 2px 4px -1px rgba(0, 0, 0, 0.3)',
          '0px 10px 15px -3px rgba(0, 0, 0, 0.5), 0px 4px 6px -2px rgba(0, 0, 0, 0.3)',
          '0px 20px 25px -5px rgba(0, 0, 0, 0.5), 0px 10px 10px -5px rgba(0, 0, 0, 0.2)',
          '0px 25px 50px -12px rgba(0, 0, 0, 0.6)',
          ...Array(18).fill('none'),
        ]) as Shadows,
    components: {
      MuiCssBaseline: {
        styleOverrides: {
          '*': {
            scrollbarWidth: 'thin',
            scrollbarColor: mode === 'light' 
              ? '#CBD5E1 #F1F5F9' 
              : '#475569 #1E293B',
          },
          '*::-webkit-scrollbar': {
            width: '8px',
            height: '8px',
          },
          '*::-webkit-scrollbar-track': {
            background: mode === 'light' ? '#F1F5F9' : '#1E293B',
            borderRadius: '4px',
          },
          '*::-webkit-scrollbar-thumb': {
            backgroundColor: mode === 'light' ? '#CBD5E1' : '#475569',
            borderRadius: '4px',
            '&:hover': {
              backgroundColor: mode === 'light' ? '#94A3B8' : '#64748B',
            },
          },
        },
      },
      MuiButton: {
        styleOverrides: {
          root: {
            borderRadius: 10,
            textTransform: 'none',
            fontWeight: 600,
            padding: '10px 20px',
            transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
            '&:hover': {
              transform: 'translateY(-2px)',
            },
          },
          contained: {
            boxShadow: mode === 'light'
              ? '0px 4px 12px rgba(59, 130, 246, 0.25)'
              : '0px 4px 12px rgba(96, 165, 250, 0.35)',
            '&:hover': {
              boxShadow: mode === 'light'
                ? '0px 8px 24px rgba(59, 130, 246, 0.35)'
                : '0px 8px 24px rgba(96, 165, 250, 0.45)',
            },
            '&:active': {
              transform: 'translateY(0)',
            },
          },
          outlined: {
            borderWidth: '2px',
            '&:hover': {
              borderWidth: '2px',
            },
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            borderRadius: 16,
            border: mode === 'light' 
              ? '1px solid rgba(226, 232, 240, 0.8)'
              : '1px solid rgba(71, 85, 105, 0.3)',
            boxShadow: mode === 'light'
              ? '0px 4px 20px rgba(0, 0, 0, 0.04)'
              : '0px 4px 20px rgba(0, 0, 0, 0.3)',
            backdropFilter: 'blur(10px)',
            transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
            '&:hover': {
              transform: 'translateY(-4px)',
              boxShadow: mode === 'light'
                ? '0px 12px 32px rgba(0, 0, 0, 0.08)'
                : '0px 12px 32px rgba(0, 0, 0, 0.4)',
            },
          },
        },
      },
      MuiPaper: {
        styleOverrides: {
          root: {
            borderRadius: 12,
            backgroundImage: 'none',
          },
          elevation1: {
            boxShadow: mode === 'light'
              ? '0px 2px 8px rgba(0, 0, 0, 0.04)'
              : '0px 2px 8px rgba(0, 0, 0, 0.3)',
          },
          elevation2: {
            boxShadow: mode === 'light'
              ? '0px 4px 12px rgba(0, 0, 0, 0.06)'
              : '0px 4px 12px rgba(0, 0, 0, 0.35)',
          },
        },
      },
      MuiChip: {
        styleOverrides: {
          root: {
            borderRadius: 10,
            fontWeight: 600,
            fontSize: '0.813rem',
            transition: 'all 0.2s ease',
            '&:hover': {
              transform: 'scale(1.05)',
            },
          },
          filled: {
            border: '1px solid transparent',
          },
        },
      },
      MuiTextField: {
        styleOverrides: {
          root: {
            '& .MuiOutlinedInput-root': {
              borderRadius: 10,
              transition: 'all 0.2s ease',
              '&:hover': {
                '& .MuiOutlinedInput-notchedOutline': {
                  borderWidth: '2px',
                },
              },
              '&.Mui-focused': {
                '& .MuiOutlinedInput-notchedOutline': {
                  borderWidth: '2px',
                },
              },
            },
          },
        },
      },
      MuiAppBar: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            boxShadow: mode === 'light'
              ? '0px 2px 12px rgba(0, 0, 0, 0.04)'
              : '0px 2px 12px rgba(0, 0, 0, 0.3)',
            backdropFilter: 'blur(10px)',
            backgroundColor: mode === 'light'
              ? alpha('#FFFFFF', 0.8)
              : alpha('#1E293B', 0.8),
            color: mode === 'light' ? '#0F172A' : '#F1F5F9',
          },
        },
      },
      MuiDrawer: {
        styleOverrides: {
          paper: {
            borderRight: mode === 'light'
              ? '1px solid rgba(226, 232, 240, 0.8)'
              : '1px solid rgba(71, 85, 105, 0.3)',
            backgroundImage: 'none',
          },
        },
      },
      MuiListItemButton: {
        styleOverrides: {
          root: {
            borderRadius: 10,
            margin: '4px 8px',
            transition: 'all 0.2s ease',
            '&:hover': {
              backgroundColor: mode === 'light'
                ? alpha('#3B82F6', 0.08)
                : alpha('#60A5FA', 0.12),
            },
            '&.Mui-selected': {
              background: mode === 'light'
                ? `linear-gradient(135deg, ${alpha('#3B82F6', 0.1)}, ${alpha('#8B5CF6', 0.1)})`
                : `linear-gradient(135deg, ${alpha('#60A5FA', 0.2)}, ${alpha('#A78BFA', 0.2)})`,
              '&:hover': {
                background: mode === 'light'
                  ? `linear-gradient(135deg, ${alpha('#3B82F6', 0.15)}, ${alpha('#8B5CF6', 0.15)})`
                  : `linear-gradient(135deg, ${alpha('#60A5FA', 0.25)}, ${alpha('#A78BFA', 0.25)})`,
              },
            },
          },
        },
      },
      MuiLinearProgress: {
        styleOverrides: {
          root: {
            borderRadius: 4,
            height: 6,
          },
        },
      },
      MuiAvatar: {
        styleOverrides: {
          root: {
            border: mode === 'light'
              ? '2px solid rgba(255, 255, 255, 0.8)'
              : '2px solid rgba(30, 41, 59, 0.8)',
          },
        },
      },
      MuiTooltip: {
        styleOverrides: {
          tooltip: {
            borderRadius: 8,
            fontSize: '0.813rem',
            fontWeight: 500,
            padding: '8px 12px',
            backgroundColor: mode === 'light' ? '#0F172A' : '#F1F5F9',
            color: mode === 'light' ? '#F1F5F9' : '#0F172A',
          },
        },
      },
      MuiBadge: {
        styleOverrides: {
          badge: {
            fontWeight: 700,
            fontSize: '0.688rem',
          },
        },
      },
    },
  };

  return createTheme(themeOptions);
};

export default getModernTheme;

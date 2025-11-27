import { createTheme, ThemeOptions, alpha, Shadows } from '@mui/material/styles';

/**
 * Enterprise-Grade Premium Theme for Smart Fleet Management Platform
 * Professional Design System v2.0
 * 
 * Features:
 * - Sophisticated enterprise color palette
 * - Premium glassmorphism & depth effects
 * - Advanced micro-interactions
 * - Professional typography hierarchy
 * - Refined component styling
 * - Enhanced accessibility (WCAG 2.1 AA)
 */

const getModernTheme = (mode: 'light' | 'dark') => {
  // Enterprise color palette - Sophisticated & Professional
  const colors = {
    light: {
      primary: {
        main: '#0052CC', // Enterprise blue - trusted, professional
        light: '#4C9AFF',
        dark: '#0747A6',
        contrastText: '#FFFFFF',
      },
      secondary: {
        main: '#00875A', // Enterprise green - growth, sustainability
        light: '#36B37E',
        dark: '#006644',
        contrastText: '#FFFFFF',
      },
      accent: {
        main: '#6554C0', // Enterprise purple - innovation
        light: '#998DD9',
        dark: '#5243AA',
      },
      error: {
        main: '#DE350B',
        light: '#FF5630',
        dark: '#BF2600',
      },
      warning: {
        main: '#FF8B00',
        light: '#FFAB00',
        dark: '#FF5630',
      },
      info: {
        main: '#0065FF',
        light: '#4C9AFF',
        dark: '#0747A6',
      },
      success: {
        main: '#00875A',
        light: '#36B37E',
        dark: '#006644',
      },
      background: {
        default: '#FAFBFC',
        paper: '#FFFFFF',
        elevated: '#FFFFFF',
      },
      text: {
        primary: '#172B4D',
        secondary: '#5E6C84',
        disabled: '#A5ADBA',
      },
      divider: alpha('#172B4D', 0.06),
    },
    dark: {
      primary: {
        main: '#4C9AFF',
        light: '#85B8FF',
        dark: '#2684FF',
        contrastText: '#0D1117',
      },
      secondary: {
        main: '#36B37E',
        light: '#57D9A3',
        dark: '#00875A',
        contrastText: '#0D1117',
      },
      accent: {
        main: '#998DD9',
        light: '#C0B6F2',
        dark: '#6554C0',
      },
      error: {
        main: '#FF6B6B',
        light: '#FF8787',
        dark: '#FF5252',
      },
      warning: {
        main: '#FFC107',
        light: '#FFD54F',
        dark: '#FFA000',
      },
      info: {
        main: '#4C9AFF',
        light: '#85B8FF',
        dark: '#2684FF',
      },
      success: {
        main: '#36B37E',
        light: '#57D9A3',
        dark: '#00875A',
      },
      background: {
        default: '#0D1117',
        paper: '#161B22',
        elevated: '#21262D',
      },
      text: {
        primary: '#E6EDF3',
        secondary: '#8B949E',
        disabled: '#484F58',
      },
      divider: alpha('#E6EDF3', 0.08),
    },
  };

  const palette = mode === 'light' ? colors.light : colors.dark;

  const themeOptions: ThemeOptions = {
    palette: {
      mode,
      ...palette,
    },
    typography: {
      fontFamily: '"Inter", "SF Pro Display", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
      h1: {
        fontSize: '2.75rem',
        fontWeight: 700,
        lineHeight: 1.15,
        letterSpacing: '-0.025em',
      },
      h2: {
        fontSize: '2.25rem',
        fontWeight: 600,
        lineHeight: 1.2,
        letterSpacing: '-0.02em',
      },
      h3: {
        fontSize: '1.875rem',
        fontWeight: 600,
        lineHeight: 1.25,
        letterSpacing: '-0.015em',
      },
      h4: {
        fontSize: '1.5rem',
        fontWeight: 600,
        lineHeight: 1.35,
        letterSpacing: '-0.01em',
      },
      h5: {
        fontSize: '1.25rem',
        fontWeight: 600,
        lineHeight: 1.4,
        letterSpacing: '-0.005em',
      },
      h6: {
        fontSize: '1.0625rem',
        fontWeight: 600,
        lineHeight: 1.5,
      },
      subtitle1: {
        fontSize: '1rem',
        fontWeight: 500,
        lineHeight: 1.6,
        letterSpacing: '0.0025em',
      },
      subtitle2: {
        fontSize: '0.875rem',
        fontWeight: 600,
        lineHeight: 1.5,
        letterSpacing: '0.005em',
      },
      body1: {
        fontSize: '0.9375rem',
        fontWeight: 400,
        lineHeight: 1.65,
        letterSpacing: '0.01em',
      },
      body2: {
        fontSize: '0.8125rem',
        fontWeight: 400,
        lineHeight: 1.55,
        letterSpacing: '0.01em',
      },
      button: {
        fontSize: '0.875rem',
        fontWeight: 600,
        textTransform: 'none',
        letterSpacing: '0.015em',
      },
      caption: {
        fontSize: '0.75rem',
        fontWeight: 500,
        lineHeight: 1.4,
        letterSpacing: '0.02em',
      },
      overline: {
        fontSize: '0.6875rem',
        fontWeight: 700,
        lineHeight: 1.8,
        letterSpacing: '0.08em',
        textTransform: 'uppercase',
      },
    },
    shape: {
      borderRadius: 10,
    },
    shadows: (mode === 'light' 
      ? [
          'none',
          '0 1px 2px 0 rgba(23, 43, 77, 0.04)',
          '0 1px 3px 0 rgba(23, 43, 77, 0.06), 0 1px 2px -1px rgba(23, 43, 77, 0.04)',
          '0 4px 6px -2px rgba(23, 43, 77, 0.08), 0 2px 4px -2px rgba(23, 43, 77, 0.06)',
          '0 12px 16px -4px rgba(23, 43, 77, 0.1), 0 4px 6px -2px rgba(23, 43, 77, 0.08)',
          '0 20px 24px -4px rgba(23, 43, 77, 0.12), 0 8px 8px -4px rgba(23, 43, 77, 0.08)',
          '0 24px 48px -12px rgba(23, 43, 77, 0.18)',
          '0 32px 64px -12px rgba(23, 43, 77, 0.2)',
          ...Array(17).fill('none'),
        ]
      : [
          'none',
          '0 1px 2px 0 rgba(0, 0, 0, 0.16)',
          '0 1px 3px 0 rgba(0, 0, 0, 0.2), 0 1px 2px -1px rgba(0, 0, 0, 0.16)',
          '0 4px 6px -2px rgba(0, 0, 0, 0.24), 0 2px 4px -2px rgba(0, 0, 0, 0.2)',
          '0 12px 16px -4px rgba(0, 0, 0, 0.28), 0 4px 6px -2px rgba(0, 0, 0, 0.24)',
          '0 20px 24px -4px rgba(0, 0, 0, 0.32), 0 8px 8px -4px rgba(0, 0, 0, 0.24)',
          '0 24px 48px -12px rgba(0, 0, 0, 0.4)',
          '0 32px 64px -12px rgba(0, 0, 0, 0.48)',
          ...Array(17).fill('none'),
        ]) as Shadows,
    components: {
      MuiCssBaseline: {
        styleOverrides: {
          '*': {
            scrollbarWidth: 'thin',
            scrollbarColor: mode === 'light' 
              ? '#DFE1E6 #F4F5F7' 
              : '#484F58 #21262D',
          },
          '*::-webkit-scrollbar': {
            width: '6px',
            height: '6px',
          },
          '*::-webkit-scrollbar-track': {
            background: mode === 'light' ? '#F4F5F7' : '#21262D',
            borderRadius: '3px',
          },
          '*::-webkit-scrollbar-thumb': {
            backgroundColor: mode === 'light' ? '#C1C7D0' : '#484F58',
            borderRadius: '3px',
            '&:hover': {
              backgroundColor: mode === 'light' ? '#A5ADBA' : '#6E7681',
            },
          },
        },
      },
      MuiButton: {
        styleOverrides: {
          root: {
            borderRadius: 8,
            textTransform: 'none',
            fontWeight: 600,
            padding: '10px 20px',
            minHeight: 40,
            transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
            '&:hover': {
              transform: 'translateY(-1px)',
            },
            '&:active': {
              transform: 'translateY(0)',
            },
          },
          contained: {
            boxShadow: mode === 'light'
              ? '0 1px 3px 0 rgba(0, 82, 204, 0.15), 0 1px 2px -1px rgba(0, 82, 204, 0.1)'
              : '0 1px 3px 0 rgba(76, 154, 255, 0.2), 0 1px 2px -1px rgba(76, 154, 255, 0.15)',
            '&:hover': {
              boxShadow: mode === 'light'
                ? '0 4px 12px 0 rgba(0, 82, 204, 0.25), 0 2px 4px -1px rgba(0, 82, 204, 0.15)'
                : '0 4px 12px 0 rgba(76, 154, 255, 0.3), 0 2px 4px -1px rgba(76, 154, 255, 0.2)',
            },
          },
          containedPrimary: {
            background: mode === 'light'
              ? 'linear-gradient(135deg, #0052CC 0%, #0747A6 100%)'
              : 'linear-gradient(135deg, #4C9AFF 0%, #2684FF 100%)',
            '&:hover': {
              background: mode === 'light'
                ? 'linear-gradient(135deg, #0747A6 0%, #003A8C 100%)'
                : 'linear-gradient(135deg, #2684FF 0%, #0052CC 100%)',
            },
          },
          containedSecondary: {
            background: mode === 'light'
              ? 'linear-gradient(135deg, #00875A 0%, #006644 100%)'
              : 'linear-gradient(135deg, #36B37E 0%, #00875A 100%)',
            '&:hover': {
              background: mode === 'light'
                ? 'linear-gradient(135deg, #006644 0%, #004D33 100%)'
                : 'linear-gradient(135deg, #00875A 0%, #006644 100%)',
            },
          },
          outlined: {
            borderWidth: '1.5px',
            '&:hover': {
              borderWidth: '1.5px',
              backgroundColor: mode === 'light' 
                ? alpha('#0052CC', 0.04) 
                : alpha('#4C9AFF', 0.08),
            },
          },
          sizeSmall: {
            padding: '6px 14px',
            fontSize: '0.8125rem',
            minHeight: 32,
          },
          sizeLarge: {
            padding: '12px 28px',
            fontSize: '0.9375rem',
            minHeight: 48,
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            borderRadius: 12,
            border: mode === 'light' 
              ? '1px solid rgba(23, 43, 77, 0.06)'
              : '1px solid rgba(230, 237, 243, 0.08)',
            boxShadow: mode === 'light'
              ? '0 1px 3px 0 rgba(23, 43, 77, 0.06), 0 1px 2px -1px rgba(23, 43, 77, 0.04)'
              : '0 1px 3px 0 rgba(0, 0, 0, 0.2), 0 1px 2px -1px rgba(0, 0, 0, 0.16)',
            transition: 'all 0.25s cubic-bezier(0.4, 0, 0.2, 1)',
            '&:hover': {
              borderColor: mode === 'light'
                ? alpha('#0052CC', 0.15)
                : alpha('#4C9AFF', 0.2),
              boxShadow: mode === 'light'
                ? '0 12px 24px -4px rgba(23, 43, 77, 0.12), 0 4px 8px -2px rgba(23, 43, 77, 0.08)'
                : '0 12px 24px -4px rgba(0, 0, 0, 0.32), 0 4px 8px -2px rgba(0, 0, 0, 0.24)',
              transform: 'translateY(-2px)',
            },
          },
        },
      },
      MuiPaper: {
        styleOverrides: {
          root: {
            borderRadius: 10,
            backgroundImage: 'none',
          },
          elevation0: {
            border: mode === 'light'
              ? '1px solid rgba(23, 43, 77, 0.06)'
              : '1px solid rgba(230, 237, 243, 0.08)',
          },
          elevation1: {
            boxShadow: mode === 'light'
              ? '0 1px 3px 0 rgba(23, 43, 77, 0.06)'
              : '0 1px 3px 0 rgba(0, 0, 0, 0.2)',
          },
          elevation2: {
            boxShadow: mode === 'light'
              ? '0 4px 8px -2px rgba(23, 43, 77, 0.08)'
              : '0 4px 8px -2px rgba(0, 0, 0, 0.24)',
          },
          elevation3: {
            boxShadow: mode === 'light'
              ? '0 8px 16px -4px rgba(23, 43, 77, 0.1)'
              : '0 8px 16px -4px rgba(0, 0, 0, 0.28)',
          },
        },
      },
      MuiChip: {
        styleOverrides: {
          root: {
            borderRadius: 6,
            fontWeight: 600,
            fontSize: '0.75rem',
            height: 28,
            transition: 'all 0.15s ease',
          },
          filled: {
            '&:hover': {
              boxShadow: mode === 'light'
                ? '0 2px 6px rgba(0, 0, 0, 0.1)'
                : '0 2px 6px rgba(0, 0, 0, 0.3)',
            },
          },
          outlined: {
            borderWidth: '1.5px',
          },
          sizeSmall: {
            height: 22,
            fontSize: '0.6875rem',
          },
        },
      },
      MuiTextField: {
        styleOverrides: {
          root: {
            '& .MuiOutlinedInput-root': {
              borderRadius: 8,
              transition: 'all 0.2s ease',
              '& .MuiOutlinedInput-notchedOutline': {
                borderWidth: '1.5px',
                borderColor: mode === 'light' ? '#DFE1E6' : '#30363D',
              },
              '&:hover': {
                '& .MuiOutlinedInput-notchedOutline': {
                  borderColor: mode === 'light' ? '#C1C7D0' : '#484F58',
                },
              },
              '&.Mui-focused': {
                '& .MuiOutlinedInput-notchedOutline': {
                  borderWidth: '2px',
                  borderColor: mode === 'light' ? '#0052CC' : '#4C9AFF',
                },
                boxShadow: mode === 'light'
                  ? `0 0 0 4px ${alpha('#0052CC', 0.1)}`
                  : `0 0 0 4px ${alpha('#4C9AFF', 0.15)}`,
              },
            },
            '& .MuiInputLabel-root': {
              fontWeight: 500,
            },
          },
        },
      },
      MuiAppBar: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            boxShadow: mode === 'light'
              ? '0 1px 0 0 rgba(23, 43, 77, 0.08)'
              : '0 1px 0 0 rgba(230, 237, 243, 0.08)',
            backdropFilter: 'blur(12px)',
            backgroundColor: mode === 'light'
              ? alpha('#FFFFFF', 0.85)
              : alpha('#161B22', 0.9),
            color: mode === 'light' ? '#172B4D' : '#E6EDF3',
          },
        },
      },
      MuiDrawer: {
        styleOverrides: {
          paper: {
            borderRight: mode === 'light'
              ? '1px solid rgba(23, 43, 77, 0.06)'
              : '1px solid rgba(230, 237, 243, 0.08)',
            backgroundImage: 'none',
            backgroundColor: mode === 'light' ? '#FFFFFF' : '#161B22',
          },
        },
      },
      MuiListItemButton: {
        styleOverrides: {
          root: {
            borderRadius: 8,
            margin: '2px 8px',
            padding: '10px 12px',
            transition: 'all 0.15s ease',
            '&:hover': {
              backgroundColor: mode === 'light'
                ? alpha('#0052CC', 0.06)
                : alpha('#4C9AFF', 0.1),
            },
            '&.Mui-selected': {
              backgroundColor: mode === 'light'
                ? alpha('#0052CC', 0.08)
                : alpha('#4C9AFF', 0.15),
              color: mode === 'light' ? '#0052CC' : '#4C9AFF',
              '&:hover': {
                backgroundColor: mode === 'light'
                  ? alpha('#0052CC', 0.12)
                  : alpha('#4C9AFF', 0.2),
              },
              '& .MuiListItemIcon-root': {
                color: mode === 'light' ? '#0052CC' : '#4C9AFF',
              },
              '&::before': {
                content: '""',
                position: 'absolute',
                left: 0,
                top: '15%',
                bottom: '15%',
                width: '3px',
                borderRadius: '0 3px 3px 0',
                backgroundColor: mode === 'light' ? '#0052CC' : '#4C9AFF',
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
            backgroundColor: mode === 'light' ? '#EBECF0' : '#30363D',
          },
          bar: {
            borderRadius: 4,
          },
        },
      },
      MuiCircularProgress: {
        styleOverrides: {
          root: {
            strokeLinecap: 'round',
          },
        },
      },
      MuiAvatar: {
        styleOverrides: {
          root: {
            border: '2px solid',
            borderColor: mode === 'light' ? '#FFFFFF' : '#21262D',
            boxShadow: mode === 'light'
              ? '0 2px 8px rgba(0, 0, 0, 0.1)'
              : '0 2px 8px rgba(0, 0, 0, 0.3)',
          },
        },
      },
      MuiTooltip: {
        styleOverrides: {
          tooltip: {
            borderRadius: 6,
            fontSize: '0.75rem',
            fontWeight: 500,
            padding: '8px 12px',
            backgroundColor: mode === 'light' ? '#172B4D' : '#F4F5F7',
            color: mode === 'light' ? '#FFFFFF' : '#172B4D',
            boxShadow: mode === 'light'
              ? '0 4px 12px rgba(0, 0, 0, 0.15)'
              : '0 4px 12px rgba(0, 0, 0, 0.4)',
          },
          arrow: {
            color: mode === 'light' ? '#172B4D' : '#F4F5F7',
          },
        },
      },
      MuiBadge: {
        styleOverrides: {
          badge: {
            fontWeight: 700,
            fontSize: '0.625rem',
            minWidth: 18,
            height: 18,
            borderRadius: 9,
          },
        },
      },
      MuiTabs: {
        styleOverrides: {
          root: {
            minHeight: 44,
          },
          indicator: {
            height: 3,
            borderRadius: '3px 3px 0 0',
            background: mode === 'light'
              ? 'linear-gradient(90deg, #0052CC, #0065FF)'
              : 'linear-gradient(90deg, #4C9AFF, #85B8FF)',
          },
        },
      },
      MuiTab: {
        styleOverrides: {
          root: {
            minHeight: 44,
            textTransform: 'none',
            fontWeight: 600,
            fontSize: '0.875rem',
            padding: '10px 20px',
            transition: 'all 0.15s ease',
            '&.Mui-selected': {
              color: mode === 'light' ? '#0052CC' : '#4C9AFF',
            },
          },
        },
      },
      MuiSwitch: {
        styleOverrides: {
          root: {
            width: 44,
            height: 24,
            padding: 0,
          },
          switchBase: {
            padding: 2,
            '&.Mui-checked': {
              transform: 'translateX(20px)',
              '& + .MuiSwitch-track': {
                backgroundColor: mode === 'light' ? '#0052CC' : '#4C9AFF',
                opacity: 1,
              },
              '& .MuiSwitch-thumb': {
                backgroundColor: '#FFFFFF',
              },
            },
          },
          thumb: {
            width: 20,
            height: 20,
            boxShadow: '0 1px 2px rgba(0, 0, 0, 0.2)',
          },
          track: {
            borderRadius: 12,
            opacity: 1,
            backgroundColor: mode === 'light' ? '#DFE1E6' : '#484F58',
          },
        },
      },
      MuiDivider: {
        styleOverrides: {
          root: {
            borderColor: mode === 'light' 
              ? 'rgba(23, 43, 77, 0.08)' 
              : 'rgba(230, 237, 243, 0.08)',
          },
        },
      },
      MuiTableCell: {
        styleOverrides: {
          root: {
            borderColor: mode === 'light' 
              ? 'rgba(23, 43, 77, 0.08)' 
              : 'rgba(230, 237, 243, 0.08)',
          },
          head: {
            fontWeight: 600,
            backgroundColor: mode === 'light' ? '#FAFBFC' : '#21262D',
          },
        },
      },
      MuiDialog: {
        styleOverrides: {
          paper: {
            borderRadius: 12,
            boxShadow: mode === 'light'
              ? '0 24px 48px -12px rgba(23, 43, 77, 0.25)'
              : '0 24px 48px -12px rgba(0, 0, 0, 0.5)',
          },
        },
      },
      MuiDialogTitle: {
        styleOverrides: {
          root: {
            fontSize: '1.25rem',
            fontWeight: 600,
            padding: '20px 24px 16px',
          },
        },
      },
      MuiDialogContent: {
        styleOverrides: {
          root: {
            padding: '16px 24px',
          },
        },
      },
      MuiDialogActions: {
        styleOverrides: {
          root: {
            padding: '16px 24px 20px',
            gap: 12,
          },
        },
      },
      MuiAlert: {
        styleOverrides: {
          root: {
            borderRadius: 8,
            fontWeight: 500,
          },
          standardSuccess: {
            backgroundColor: mode === 'light' 
              ? alpha('#00875A', 0.08) 
              : alpha('#36B37E', 0.15),
            color: mode === 'light' ? '#006644' : '#57D9A3',
          },
          standardError: {
            backgroundColor: mode === 'light' 
              ? alpha('#DE350B', 0.08) 
              : alpha('#FF6B6B', 0.15),
            color: mode === 'light' ? '#BF2600' : '#FF8787',
          },
          standardWarning: {
            backgroundColor: mode === 'light' 
              ? alpha('#FF8B00', 0.08) 
              : alpha('#FFC107', 0.15),
            color: mode === 'light' ? '#FF5630' : '#FFD54F',
          },
          standardInfo: {
            backgroundColor: mode === 'light' 
              ? alpha('#0065FF', 0.08) 
              : alpha('#4C9AFF', 0.15),
            color: mode === 'light' ? '#0747A6' : '#85B8FF',
          },
        },
      },
      MuiMenu: {
        styleOverrides: {
          paper: {
            borderRadius: 10,
            boxShadow: mode === 'light'
              ? '0 8px 24px -4px rgba(23, 43, 77, 0.16), 0 4px 12px -2px rgba(23, 43, 77, 0.12)'
              : '0 8px 24px -4px rgba(0, 0, 0, 0.4), 0 4px 12px -2px rgba(0, 0, 0, 0.32)',
            border: mode === 'light'
              ? '1px solid rgba(23, 43, 77, 0.08)'
              : '1px solid rgba(230, 237, 243, 0.12)',
          },
        },
      },
      MuiMenuItem: {
        styleOverrides: {
          root: {
            borderRadius: 6,
            margin: '2px 6px',
            padding: '10px 14px',
            fontSize: '0.875rem',
            fontWeight: 500,
            transition: 'all 0.15s ease',
            '&:hover': {
              backgroundColor: mode === 'light'
                ? alpha('#0052CC', 0.06)
                : alpha('#4C9AFF', 0.12),
            },
          },
        },
      },
      MuiSelect: {
        styleOverrides: {
          select: {
            borderRadius: 8,
          },
        },
      },
      MuiIconButton: {
        styleOverrides: {
          root: {
            borderRadius: 8,
            transition: 'all 0.15s ease',
            '&:hover': {
              backgroundColor: mode === 'light'
                ? alpha('#0052CC', 0.08)
                : alpha('#4C9AFF', 0.12),
            },
          },
        },
      },
      MuiCardHeader: {
        styleOverrides: {
          root: {
            padding: '20px 24px 12px',
          },
          title: {
            fontSize: '1.0625rem',
            fontWeight: 600,
          },
          subheader: {
            fontSize: '0.8125rem',
            color: mode === 'light' ? '#5E6C84' : '#8B949E',
            marginTop: 2,
          },
        },
      },
      MuiCardContent: {
        styleOverrides: {
          root: {
            padding: '16px 24px',
            '&:last-child': {
              paddingBottom: 24,
            },
          },
        },
      },
      MuiCardActions: {
        styleOverrides: {
          root: {
            padding: '12px 24px 20px',
          },
        },
      },
    },
  };

  return createTheme(themeOptions);
};

export default getModernTheme;

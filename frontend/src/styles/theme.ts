import { createTheme, ThemeOptions } from '@mui/material/styles';

const getTheme = (mode: 'light' | 'dark') => {
  const themeOptions: ThemeOptions = {
    palette: {
      mode,
      primary: {
        main: '#1976d2',
        light: '#42a5f5',
        dark: '#1565c0',
        contrastText: '#fff',
      },
      secondary: {
        main: '#4caf50',
        light: '#81c784',
        dark: '#388e3c',
        contrastText: '#fff',
      },
      error: {
        main: '#f44336',
        light: '#e57373',
        dark: '#d32f2f',
      },
      warning: {
        main: '#ff9800',
        light: '#ffb74d',
        dark: '#f57c00',
      },
      info: {
        main: '#2196f3',
        light: '#64b5f6',
        dark: '#1976d2',
      },
      success: {
        main: '#4caf50',
        light: '#81c784',
        dark: '#388e3c',
      },
      background: {
        default: mode === 'light' ? '#f5f5f5' : '#121212',
        paper: mode === 'light' ? '#ffffff' : '#1e1e1e',
      },
      text: {
        primary: mode === 'light' ? '#212121' : '#ffffff',
        secondary: mode === 'light' ? '#757575' : '#b0b0b0',
      },
    },
    typography: {
      fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
      h1: {
        fontSize: '2.5rem',
        fontWeight: 500,
        lineHeight: 1.2,
      },
      h2: {
        fontSize: '2rem',
        fontWeight: 500,
        lineHeight: 1.3,
      },
      h3: {
        fontSize: '1.75rem',
        fontWeight: 500,
        lineHeight: 1.4,
      },
      h4: {
        fontSize: '1.5rem',
        fontWeight: 500,
        lineHeight: 1.4,
      },
      h5: {
        fontSize: '1.25rem',
        fontWeight: 500,
        lineHeight: 1.5,
      },
      h6: {
        fontSize: '1rem',
        fontWeight: 500,
        lineHeight: 1.6,
      },
      subtitle1: {
        fontSize: '1rem',
        fontWeight: 400,
        lineHeight: 1.75,
      },
      subtitle2: {
        fontSize: '0.875rem',
        fontWeight: 500,
        lineHeight: 1.57,
      },
      body1: {
        fontSize: '1rem',
        fontWeight: 400,
        lineHeight: 1.5,
      },
      body2: {
        fontSize: '0.875rem',
        fontWeight: 400,
        lineHeight: 1.43,
      },
      button: {
        fontSize: '0.875rem',
        fontWeight: 500,
        textTransform: 'none',
      },
    },
    shape: {
      borderRadius: 8,
    },
    shadows: [
      'none',
      mode === 'light'
        ? '0px 2px 4px rgba(0,0,0,0.1)'
        : '0px 2px 4px rgba(0,0,0,0.4)',
      mode === 'light'
        ? '0px 4px 8px rgba(0,0,0,0.1)'
        : '0px 4px 8px rgba(0,0,0,0.4)',
      mode === 'light'
        ? '0px 6px 12px rgba(0,0,0,0.1)'
        : '0px 6px 12px rgba(0,0,0,0.4)',
      mode === 'light'
        ? '0px 8px 16px rgba(0,0,0,0.1)'
        : '0px 8px 16px rgba(0,0,0,0.4)',
      mode === 'light'
        ? '0px 10px 20px rgba(0,0,0,0.1)'
        : '0px 10px 20px rgba(0,0,0,0.4)',
      mode === 'light'
        ? '0px 12px 24px rgba(0,0,0,0.1)'
        : '0px 12px 24px rgba(0,0,0,0.4)',
      mode === 'light'
        ? '0px 14px 28px rgba(0,0,0,0.1)'
        : '0px 14px 28px rgba(0,0,0,0.4)',
      mode === 'light'
        ? '0px 16px 32px rgba(0,0,0,0.1)'
        : '0px 16px 32px rgba(0,0,0,0.4)',
      mode === 'light'
        ? '0px 18px 36px rgba(0,0,0,0.1)'
        : '0px 18px 36px rgba(0,0,0,0.4)',
      ...Array(15).fill('none'),
    ] as any,
    components: {
      MuiButton: {
        styleOverrides: {
          root: {
            borderRadius: 8,
            textTransform: 'none',
            fontWeight: 500,
            padding: '8px 16px',
          },
          contained: {
            boxShadow: 'none',
            '&:hover': {
              boxShadow: 'none',
            },
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            borderRadius: 12,
            boxShadow: mode === 'light'
              ? '0px 4px 12px rgba(0,0,0,0.08)'
              : '0px 4px 12px rgba(0,0,0,0.3)',
          },
        },
      },
      MuiPaper: {
        styleOverrides: {
          root: {
            borderRadius: 8,
          },
          elevation1: {
            boxShadow: mode === 'light'
              ? '0px 2px 8px rgba(0,0,0,0.08)'
              : '0px 2px 8px rgba(0,0,0,0.3)',
          },
        },
      },
      MuiChip: {
        styleOverrides: {
          root: {
            borderRadius: 8,
            fontWeight: 500,
          },
        },
      },
      MuiTextField: {
        styleOverrides: {
          root: {
            '& .MuiOutlinedInput-root': {
              borderRadius: 8,
            },
          },
        },
      },
    },
  };

  return createTheme(themeOptions);
};

export default getTheme;

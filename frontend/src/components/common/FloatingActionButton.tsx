import React, { useState } from 'react';
import { Fab, Tooltip, Box, Zoom, SpeedDial, SpeedDialAction, alpha } from '@mui/material';
import { Add as AddIcon, Close as CloseIcon } from '@mui/icons-material';

interface Action {
  icon: React.ReactElement;
  name: string;
  onClick: () => void;
}

interface FloatingActionButtonProps {
  actions?: Action[];
  mainAction?: () => void;
  mainIcon?: React.ReactElement;
  tooltip?: string;
  variant?: 'single' | 'speedDial';
  position?: 'bottom-right' | 'bottom-left' | 'top-right' | 'top-left';
}

const FloatingActionButton: React.FC<FloatingActionButtonProps> = ({
  actions = [],
  mainAction,
  mainIcon = <AddIcon />,
  tooltip = 'Add',
  variant = 'single',
  position = 'bottom-right',
}) => {
  const [open, setOpen] = useState(false);

  const getPosition = () => {
    switch (position) {
      case 'bottom-right':
        return { bottom: 24, right: 24 };
      case 'bottom-left':
        return { bottom: 24, left: 24 };
      case 'top-right':
        return { top: 88, right: 24 };
      case 'top-left':
        return { top: 88, left: 24 };
      default:
        return { bottom: 24, right: 24 };
    }
  };

  const positionStyles = getPosition();

  if (variant === 'speedDial' && actions.length > 0) {
    return (
      <SpeedDial
        ariaLabel="Actions"
        sx={{
          position: 'fixed',
          ...positionStyles,
          '& .MuiSpeedDial-fab': {
            background: (theme) =>
              `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.secondary.main} 100%)`,
            '&:hover': {
              background: (theme) =>
                `linear-gradient(135deg, ${theme.palette.primary.dark} 0%, ${theme.palette.secondary.dark} 100%)`,
            },
          },
        }}
        icon={mainIcon}
        onClose={() => setOpen(false)}
        onOpen={() => setOpen(true)}
        open={open}
        FabProps={{
          sx: {
            boxShadow: (theme) => `0px 8px 24px ${alpha(theme.palette.primary.main, 0.35)}`,
          },
        }}
      >
        {actions.map((action) => (
          <SpeedDialAction
            key={action.name}
            icon={action.icon}
            tooltipTitle={action.name}
            onClick={() => {
              action.onClick();
              setOpen(false);
            }}
            sx={{
              '&:hover': {
                transform: 'scale(1.1)',
              },
            }}
          />
        ))}
      </SpeedDial>
    );
  }

  return (
    <Zoom in timeout={300}>
      <Box sx={{ position: 'fixed', ...positionStyles }}>
        <Tooltip title={tooltip} placement="left">
          <Fab
            color="primary"
            aria-label={tooltip}
            onClick={mainAction}
            sx={{
              background: (theme) =>
                `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.secondary.main} 100%)`,
              boxShadow: (theme) => `0px 8px 24px ${alpha(theme.palette.primary.main, 0.35)}`,
              transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
              '&:hover': {
                background: (theme) =>
                  `linear-gradient(135deg, ${theme.palette.primary.dark} 0%, ${theme.palette.secondary.dark} 100%)`,
                transform: 'scale(1.1) rotate(90deg)',
                boxShadow: (theme) => `0px 12px 32px ${alpha(theme.palette.primary.main, 0.45)}`,
              },
              '&:active': {
                transform: 'scale(1.05) rotate(90deg)',
              },
            }}
          >
            {mainIcon}
          </Fab>
        </Tooltip>
      </Box>
    </Zoom>
  );
};

export default FloatingActionButton;

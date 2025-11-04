import React from 'react';
import { Chip, ChipProps } from '@mui/material';

interface StatusBadgeProps extends Omit<ChipProps, 'label'> {
  status: string;
  statusColors?: Record<string, any>;
}

const defaultStatusColors: Record<string, any> = {
  ACTIVE: 'success',
  INACTIVE: 'error',
  PENDING: 'warning',
  COMPLETED: 'success',
  ONGOING: 'info',
  PAUSED: 'warning',
  CANCELLED: 'error',
  SUSPENDED: 'error',
  ON_LEAVE: 'warning',
  TERMINATED: 'error',
  PAID: 'success',
  OVERDUE: 'error',
  DRAFT: 'default',
  CONFIRMED: 'info',
  CHARGING: 'warning',
  INITIATED: 'info',
  FAILED: 'error',
  MAINTENANCE: 'warning',
  IN_TRIP: 'info',
  IN_PROGRESS: 'warning',
};

const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  statusColors = defaultStatusColors,
  color = statusColors[status] || 'default',
  size = 'small',
  ...props
}) => {
  return (
    <Chip
      label={status}
      color={color as any}
      size={size}
      {...props}
    />
  );
};

export default StatusBadge;

import React from 'react';
import { Card, CardContent, Typography, Box, alpha, LinearProgress, SxProps, Theme } from '@mui/material';
import { TrendingUp, TrendingDown } from '@mui/icons-material';

interface StatCardProps {
  title: string;
  value: string | number;
  icon?: React.ReactNode;
  color?: string;
  subtitle?: string;
  trend?: number; // percentage change
  trendLabel?: string;
  loading?: boolean;
  gradient?: boolean;
  onClick?: () => void;
  sx?: SxProps<Theme>;
}

const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  icon,
  color = '#3B82F6',
  subtitle,
  trend,
  trendLabel = 'vs last period',
  loading = false,
  gradient = true,
  onClick,
  sx = {},
}) => {
  const getTrendColor = (trendValue: number) => {
    return trendValue >= 0 ? '#10B981' : '#EF4444';
  };

  return (
    <Card
      onClick={onClick}
      className="fade-in"
      sx={{
        position: 'relative',
        overflow: 'hidden',
        height: '100%',
        cursor: onClick ? 'pointer' : 'default',
        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        '&:hover': onClick
          ? {
              transform: 'translateY(-4px)',
              boxShadow: (theme) => `0px 12px 32px ${alpha(color, 0.2)}`,
            }
          : {},
        '&::before': gradient
          ? {
              content: '""',
              position: 'absolute',
              top: 0,
              right: 0,
              width: '120px',
              height: '120px',
              borderRadius: '50%',
              background: alpha(color, 0.1),
              transform: 'translate(30%, -30%)',
              transition: 'all 0.3s ease',
            }
          : {},
        '&:hover::before': gradient
          ? {
              transform: 'translate(35%, -35%) scale(1.2)',
            }
          : {},
        ...sx,
      }}
    >
      {loading && <LinearProgress sx={{ position: 'absolute', top: 0, left: 0, right: 0 }} />}
      
      <CardContent sx={{ p: 3 }}>
        <Box display="flex" alignItems="center" gap={1.5} mb={2}>
          {icon && (
            <Box
              sx={{
                color,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: 48,
                height: 48,
                borderRadius: 2,
                background: alpha(color, 0.1),
                transition: 'transform 0.3s ease',
                '.MuiCard-root:hover &': {
                  transform: 'scale(1.1) rotate(5deg)',
                },
              }}
            >
              {icon}
            </Box>
          )}
          <Typography
            variant="subtitle2"
            color="text.secondary"
            fontWeight={600}
            sx={{ textTransform: 'uppercase', letterSpacing: '0.05em' }}
          >
            {title}
          </Typography>
        </Box>

        <Typography
          variant="h3"
          fontWeight={800}
          sx={{
            color,
            mb: subtitle || trend !== undefined ? 1 : 0,
            wordBreak: 'break-word',
          }}
        >
          {value}
        </Typography>

        {subtitle && (
          <Typography variant="caption" color="text.secondary" fontWeight={500}>
            {subtitle}
          </Typography>
        )}

        {trend !== undefined && (
          <Box display="flex" alignItems="center" gap={0.5} mt={1}>
            {trend >= 0 ? (
              <TrendingUp sx={{ fontSize: 16, color: getTrendColor(trend) }} />
            ) : (
              <TrendingDown sx={{ fontSize: 16, color: getTrendColor(trend) }} />
            )}
            <Typography
              variant="caption"
              fontWeight={700}
              sx={{ color: getTrendColor(trend) }}
            >
              {Math.abs(trend)}%
            </Typography>
            <Typography variant="caption" color="text.secondary" fontWeight={500}>
              {trendLabel}
            </Typography>
          </Box>
        )}
      </CardContent>
    </Card>
  );
};

export default StatCard;

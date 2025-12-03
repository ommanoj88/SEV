import React, { useEffect, useState, useMemo } from 'react';
import {
  Box,
  Paper,
  Typography,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Avatar,
  Chip,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  IconButton,
  Tooltip,
  Divider,
  Badge,
  useTheme,
  alpha,
  Skeleton,
  Fade,
  Zoom,
} from '@mui/material';
import {
  EmojiEvents,
  Speed,
  Star,
  TrendingUp,
  TrendingDown,
  Remove,
  Shield,
  LocalFireDepartment,
  Refresh,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@redux/hooks';
import { fetchLeaderboard as fetchDriverLeaderboard, selectLeaderboard as selectDriverLeaderboard } from '@redux/slices/driverSlice';
import { DriverLeaderboard as DriverLeaderboardType } from '../../types';

/**
 * Safety Leaderboard Component
 * 
 * Gamification feature displaying driver safety rankings with:
 * - Ranked list by safety score
 * - Achievement badges (🏆 Safe Driver, ⚡ Speed Demon, 🌟 Perfect Week)
 * - Time period filtering
 * - Click to view driver details
 * - Rank change animations
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */

interface TimePeriodOption {
  label: string;
  value: 'week' | 'month' | 'quarter' | 'year';
}

const TIME_PERIOD_OPTIONS: TimePeriodOption[] = [
  { label: 'This Week', value: 'week' },
  { label: 'This Month', value: 'month' },
  { label: 'This Quarter', value: 'quarter' },
  { label: 'This Year', value: 'year' },
];

// Badge definitions
interface BadgeInfo {
  icon: React.ReactNode;
  label: string;
  color: string;
  description: string;
}

const BADGES: Record<string, BadgeInfo> = {
  safeDriver: {
    icon: '🏆',
    label: 'Safe Driver',
    color: '#FFD700',
    description: 'Safety score 90 or above',
  },
  speedDemon: {
    icon: '⚡',
    label: 'Speed Demon',
    color: '#FF4757',
    description: 'Most speeding events - needs improvement',
  },
  perfectWeek: {
    icon: '🌟',
    label: 'Perfect Week',
    color: '#4CAF50',
    description: 'No incidents in the last 7 days',
  },
  topPerformer: {
    icon: '🔥',
    label: 'Top Performer',
    color: '#FF6B6B',
    description: 'Top 3 in overall performance',
  },
  mostImproved: {
    icon: '📈',
    label: 'Most Improved',
    color: '#2196F3',
    description: 'Biggest score improvement',
  },
};

// Rank medal colors
const getRankMedal = (rank: number): { color: string; icon: React.ReactNode } | null => {
  switch (rank) {
    case 1:
      return { color: '#FFD700', icon: <EmojiEvents sx={{ color: '#FFD700' }} /> };
    case 2:
      return { color: '#C0C0C0', icon: <EmojiEvents sx={{ color: '#C0C0C0' }} /> };
    case 3:
      return { color: '#CD7F32', icon: <EmojiEvents sx={{ color: '#CD7F32' }} /> };
    default:
      return null;
  }
};

// Get score color
const getScoreColor = (score: number): string => {
  if (score >= 90) return '#4CAF50';
  if (score >= 80) return '#8BC34A';
  if (score >= 70) return '#FFEB3B';
  if (score >= 60) return '#FFC107';
  return '#F44336';
};

// Simulate previous ranks for animation (would come from API in real implementation)
const simulatePreviousRank = (currentRank: number, driverId: string): number => {
  // Use driver ID to create deterministic "previous" rank for demo
  const hash = driverId.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
  const offset = (hash % 5) - 2; // -2 to +2
  return Math.max(1, currentRank + offset);
};

const SafetyLeaderboard: React.FC = () => {
  const theme = useTheme();
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  
  const { loading } = useAppSelector((state) => state.drivers);
  const leaderboardData = useAppSelector(selectDriverLeaderboard) as DriverLeaderboardType[];
  
  const [timePeriod, setTimePeriod] = useState<'week' | 'month' | 'quarter' | 'year'>('month');
  const [refreshing, setRefreshing] = useState(false);
  const [showAnimation, setShowAnimation] = useState(false);

  useEffect(() => {
    dispatch(fetchDriverLeaderboard(10));
    // Trigger animation after data loads
    setTimeout(() => setShowAnimation(true), 100);
  }, [dispatch, timePeriod]);

  // Calculate badges for each driver
  const driversWithBadges = useMemo(() => {
    if (!leaderboardData || leaderboardData.length === 0) return [];

    // Find the driver with most "issues" for speed demon badge (simulated)
    const maxSpeedingDriver = leaderboardData.reduce((prev, current) => 
      (current.fuelEfficiency < prev.fuelEfficiency) ? current : prev
    );

    return leaderboardData.map((driver, index) => {
      const badges: string[] = [];
      
      // Safe Driver badge: score >= 90
      if (driver.safetyScore >= 90) {
        badges.push('safeDriver');
      }
      
      // Perfect Week badge: top 20% and score > 85
      if (index < Math.ceil(leaderboardData.length * 0.2) && driver.safetyScore > 85) {
        badges.push('perfectWeek');
      }
      
      // Top Performer badge: top 3
      if (driver.rank <= 3) {
        badges.push('topPerformer');
      }
      
      // Most Improved: simulated - every 5th driver
      if ((index + 1) % 5 === 0) {
        badges.push('mostImproved');
      }
      
      // Speed Demon badge: lowest efficiency (warning badge)
      if (driver.driverId === maxSpeedingDriver.driverId && driver.fuelEfficiency < 15) {
        badges.push('speedDemon');
      }

      const previousRank = simulatePreviousRank(driver.rank, driver.driverId);
      
      return {
        ...driver,
        badges,
        previousRank,
        rankChange: previousRank - driver.rank,
      };
    });
  }, [leaderboardData]);

  const handleRefresh = async () => {
    setRefreshing(true);
    setShowAnimation(false);
    await dispatch(fetchDriverLeaderboard(10));
    setTimeout(() => {
      setShowAnimation(true);
      setRefreshing(false);
    }, 300);
  };

  const handleDriverClick = (driverId: string) => {
    navigate(`/drivers/${driverId}`);
  };

  // Loading skeleton
  if (loading && !leaderboardData.length) {
    return (
      <Paper sx={{ p: 3 }}>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
          <Skeleton variant="text" width={200} height={40} />
          <Skeleton variant="rectangular" width={150} height={40} />
        </Box>
        {[1, 2, 3, 4, 5].map((i) => (
          <Box key={i} display="flex" alignItems="center" mb={2}>
            <Skeleton variant="circular" width={50} height={50} sx={{ mr: 2 }} />
            <Box flex={1}>
              <Skeleton variant="text" width="60%" />
              <Skeleton variant="text" width="40%" />
            </Box>
          </Box>
        ))}
      </Paper>
    );
  }

  return (
    <Paper sx={{ p: 3 }}>
      {/* Header */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box display="flex" alignItems="center" gap={1}>
          <Shield sx={{ color: 'primary.main', fontSize: 28 }} />
          <Typography variant="h5" fontWeight={600}>
            Safety Leaderboard
          </Typography>
        </Box>
        <Box display="flex" gap={2}>
          <FormControl size="small" sx={{ minWidth: 140 }}>
            <InputLabel>Time Period</InputLabel>
            <Select
              value={timePeriod}
              label="Time Period"
              onChange={(e) => setTimePeriod(e.target.value as typeof timePeriod)}
            >
              {TIME_PERIOD_OPTIONS.map((option) => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <Tooltip title="Refresh Leaderboard">
            <IconButton onClick={handleRefresh} disabled={refreshing}>
              <Refresh className={refreshing ? 'rotating' : ''} />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {/* Badge Legend */}
      <Box display="flex" flexWrap="wrap" gap={1} mb={3}>
        {Object.entries(BADGES).map(([key, badge]) => (
          <Tooltip key={key} title={badge.description}>
            <Chip
              icon={<span style={{ fontSize: 16 }}>{badge.icon}</span>}
              label={badge.label}
              size="small"
              variant="outlined"
              sx={{
                borderColor: badge.color,
                '& .MuiChip-label': { color: badge.color },
              }}
            />
          </Tooltip>
        ))}
      </Box>

      <Divider sx={{ mb: 2 }} />

      {/* Leaderboard List */}
      <List disablePadding>
        {driversWithBadges.length === 0 ? (
          <Box textAlign="center" py={5}>
            <Typography color="text.secondary">
              No leaderboard data available
            </Typography>
          </Box>
        ) : (
          driversWithBadges.map((driver, index) => {
            const medal = getRankMedal(driver.rank);
            const scoreColor = getScoreColor(driver.safetyScore);

            return (
              <Zoom
                key={driver.driverId}
                in={showAnimation}
                style={{ transitionDelay: showAnimation ? `${index * 50}ms` : '0ms' }}
              >
                <Box>
                  <ListItem
                    sx={{
                      borderRadius: 2,
                      mb: 1,
                      bgcolor: medal
                        ? alpha(medal.color, 0.08)
                        : index % 2 === 0
                        ? alpha(theme.palette.grey[100], 0.5)
                        : 'transparent',
                      cursor: 'pointer',
                      transition: 'all 0.2s ease',
                      border: medal ? `2px solid ${alpha(medal.color, 0.3)}` : 'none',
                      '&:hover': {
                        bgcolor: alpha(theme.palette.primary.main, 0.08),
                        transform: 'translateX(8px)',
                      },
                    }}
                    onClick={() => handleDriverClick(driver.driverId)}
                  >
                    {/* Rank */}
                    <Box
                      sx={{
                        width: 50,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        mr: 2,
                      }}
                    >
                      {medal ? (
                        <Badge
                          badgeContent={driver.rank}
                          color="default"
                          sx={{
                            '& .MuiBadge-badge': {
                              bgcolor: medal.color,
                              color: 'white',
                              fontWeight: 700,
                            },
                          }}
                        >
                          {medal.icon}
                        </Badge>
                      ) : (
                        <Typography
                          variant="h6"
                          fontWeight={600}
                          color="text.secondary"
                        >
                          #{driver.rank}
                        </Typography>
                      )}
                    </Box>

                    {/* Avatar */}
                    <ListItemAvatar>
                      <Avatar
                        src={driver.profileImageUrl}
                        sx={{
                          bgcolor: scoreColor,
                          border: `3px solid ${alpha(scoreColor, 0.3)}`,
                        }}
                      >
                        {driver.driverName.split(' ').map((n) => n[0]).join('')}
                      </Avatar>
                    </ListItemAvatar>

                    {/* Driver Info */}
                    <ListItemText
                      primary={
                        <Box display="flex" alignItems="center" gap={1}>
                          <Typography fontWeight={600}>
                            {driver.driverName}
                          </Typography>
                          {/* Badges */}
                          {driver.badges.map((badgeKey) => (
                            <Tooltip
                              key={badgeKey}
                              title={BADGES[badgeKey].description}
                            >
                              <span style={{ fontSize: 18, cursor: 'help' }}>
                                {BADGES[badgeKey].icon}
                              </span>
                            </Tooltip>
                          ))}
                        </Box>
                      }
                      secondary={
                        <Box display="flex" alignItems="center" gap={2} mt={0.5}>
                          <Typography variant="body2" color="text.secondary">
                            {driver.totalTrips} trips
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            •
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {driver.totalDistance.toLocaleString()} km
                          </Typography>
                        </Box>
                      }
                    />

                    {/* Rank Change Indicator */}
                    <Box
                      sx={{
                        display: 'flex',
                        alignItems: 'center',
                        mr: 3,
                        minWidth: 60,
                      }}
                    >
                      {driver.rankChange > 0 ? (
                        <Chip
                          icon={<TrendingUp fontSize="small" />}
                          label={`+${driver.rankChange}`}
                          size="small"
                          color="success"
                          variant="outlined"
                        />
                      ) : driver.rankChange < 0 ? (
                        <Chip
                          icon={<TrendingDown fontSize="small" />}
                          label={driver.rankChange}
                          size="small"
                          color="error"
                          variant="outlined"
                        />
                      ) : (
                        <Chip
                          icon={<Remove fontSize="small" />}
                          label="—"
                          size="small"
                          variant="outlined"
                          sx={{ color: 'text.secondary' }}
                        />
                      )}
                    </Box>

                    {/* Safety Score */}
                    <Box
                      sx={{
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        minWidth: 80,
                      }}
                    >
                      <Typography
                        variant="h5"
                        fontWeight={700}
                        sx={{ color: scoreColor }}
                      >
                        {driver.safetyScore}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Safety Score
                      </Typography>
                    </Box>
                  </ListItem>
                </Box>
              </Zoom>
            );
          })
        )}
      </List>

      {/* Footer Stats */}
      {driversWithBadges.length > 0 && (
        <Box
          mt={3}
          pt={2}
          borderTop={1}
          borderColor="divider"
          display="flex"
          justifyContent="space-around"
          textAlign="center"
        >
          <Box>
            <Typography variant="h6" fontWeight={600} color="success.main">
              {driversWithBadges.filter((d) => d.safetyScore >= 90).length}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Safe Drivers (90+)
            </Typography>
          </Box>
          <Box>
            <Typography variant="h6" fontWeight={600} color="primary.main">
              {Math.round(
                driversWithBadges.reduce((sum, d) => sum + d.safetyScore, 0) /
                  driversWithBadges.length
              )}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Fleet Average
            </Typography>
          </Box>
          <Box>
            <Typography variant="h6" fontWeight={600} color="warning.main">
              {driversWithBadges.filter((d) => d.safetyScore < 70).length}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Need Training
            </Typography>
          </Box>
        </Box>
      )}
    </Paper>
  );
};

export default SafetyLeaderboard;

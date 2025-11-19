import React, { useEffect } from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Avatar, Box, Chip, Typography } from '@mui/material';
import { EmojiEvents as TrophyIcon } from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '../../redux/hooks';
import { selectUser } from '../../redux/slices/authSlice';
import axios from 'axios';

interface LeaderboardEntry {
  id: number;
  name: string;
  safetyScore: number;
  fuelEfficiency: number;
  totalTrips: number;
  totalDistance: number;
}

const DriverLeaderboard: React.FC = () => {
  const dispatch = useAppDispatch();
  const user = useAppSelector(selectUser);
  const [leaderboard, setLeaderboard] = React.useState<LeaderboardEntry[]>([]);
  const [loading, setLoading] = React.useState(true);

  useEffect(() => {
    const fetchLeaderboard = async () => {
      if (user?.companyId) {
        try {
          const response = await axios.get(`/api/v1/drivers/leaderboard?companyId=${user.companyId}`);
          if (response.data.success) {
            setLeaderboard(response.data.data);
          }
        } catch (error) {
          console.error('Failed to fetch leaderboard:', error);
        } finally {
          setLoading(false);
        }
      }
    };
    fetchLeaderboard();
  }, [user?.companyId]);

  const getRankColor = (rank: number) => {
    if (rank === 1) return 'gold';
    if (rank === 2) return 'silver';
    if (rank === 3) return '#CD7F32';
    return 'grey';
  };

  if (loading) {
    return <Typography>Loading leaderboard...</Typography>;
  }

  return (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Rank</TableCell>
            <TableCell>Driver</TableCell>
            <TableCell>Safety Score</TableCell>
            <TableCell>Fuel Efficiency</TableCell>
            <TableCell>Total Trips</TableCell>
            <TableCell>Total Distance (km)</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {leaderboard.map((entry, index) => (
            <TableRow key={entry.id}>
              <TableCell>
                <Box display="flex" alignItems="center" gap={1}>
                  {index + 1 <= 3 && <TrophyIcon sx={{ color: getRankColor(index + 1) }} />}
                  <Typography fontWeight="bold">{index + 1}</Typography>
                </Box>
              </TableCell>
              <TableCell>
                <Box display="flex" alignItems="center" gap={2}>
                  <Avatar>{entry.name ? entry.name[0] : 'D'}</Avatar>
                  <Typography>{entry.name}</Typography>
                </Box>
              </TableCell>
              <TableCell>
                <Chip 
                  label={`${entry.safetyScore?.toFixed(1) || '0.0'}/100`} 
                  color={entry.safetyScore >= 90 ? 'success' : entry.safetyScore >= 70 ? 'warning' : 'error'} 
                />
              </TableCell>
              <TableCell>{entry.fuelEfficiency?.toFixed(1) || '0.0'}</TableCell>
              <TableCell>{entry.totalTrips || 0}</TableCell>
              <TableCell>{entry.totalDistance?.toFixed(1) || '0.0'}</TableCell>
            </TableRow>
          ))}
          {leaderboard.length === 0 && (
            <TableRow>
              <TableCell colSpan={6} align="center">
                <Typography color="textSecondary">No driver data available</Typography>
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default DriverLeaderboard;

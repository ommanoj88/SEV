import React from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Avatar, Box, Chip, Typography } from '@mui/material';
import { EmojiEvents as TrophyIcon } from '@mui/icons-material';
import { useAppSelector } from '../../redux/hooks';

const DriverLeaderboard: React.FC = () => {
  const { leaderboard } = useAppSelector((state) => state.drivers);

  const getRankColor = (rank: number) => {
    if (rank === 1) return 'gold';
    if (rank === 2) return 'silver';
    if (rank === 3) return '#CD7F32';
    return 'grey';
  };

  return (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Rank</TableCell>
            <TableCell>Driver</TableCell>
            <TableCell>Performance Score</TableCell>
            <TableCell>Efficiency</TableCell>
            <TableCell>Safety Score</TableCell>
            <TableCell>Total Trips</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {leaderboard.map((entry) => (
            <TableRow key={entry.driverId}>
              <TableCell>
                <Box display="flex" alignItems="center" gap={1}>
                  {entry.rank <= 3 && <TrophyIcon sx={{ color: getRankColor(entry.rank) }} />}
                  <Typography fontWeight="bold">{entry.rank}</Typography>
                </Box>
              </TableCell>
              <TableCell>
                <Box display="flex" alignItems="center" gap={2}>
                  <Avatar>{entry.driverName[0]}</Avatar>
                  <Typography>{entry.driverName}</Typography>
                </Box>
              </TableCell>
              <TableCell><Chip label={entry.performanceScore} color="primary" /></TableCell>
              <TableCell>{entry.fuelEfficiency.toFixed(1)}</TableCell>
              <TableCell>{entry.safetyScore}/100</TableCell>
              <TableCell>{entry.totalTrips}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default DriverLeaderboard;

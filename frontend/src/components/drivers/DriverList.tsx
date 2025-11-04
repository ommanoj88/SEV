import React, { useEffect } from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Chip, Avatar, Box, Rating } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../redux/hooks';
import { fetchAllDrivers } from '../../redux/slices/driverSlice';

const DriverList: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { drivers } = useAppSelector((state) => state.drivers);

  useEffect(() => {
    dispatch(fetchAllDrivers(undefined));
  }, [dispatch]);

  return (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Driver</TableCell>
            <TableCell>Email</TableCell>
            <TableCell>Phone</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Vehicle</TableCell>
            <TableCell>Rating</TableCell>
            <TableCell>Total Trips</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {drivers.map((driver) => (
            <TableRow key={driver.id} hover onClick={() => navigate(`/drivers/${driver.id}`)} sx={{ cursor: 'pointer' }}>
              <TableCell>
                <Box display="flex" alignItems="center" gap={2}>
                  <Avatar>{driver.firstName[0]}{driver.lastName[0]}</Avatar>
                  <Box>
                    <Box fontWeight="500">{driver.firstName} {driver.lastName}</Box>
                    <Box fontSize="0.875rem" color="text.secondary">{driver.licenseNumber}</Box>
                  </Box>
                </Box>
              </TableCell>
              <TableCell>{driver.email}</TableCell>
              <TableCell>{driver.phone}</TableCell>
              <TableCell><Chip label={driver.status} size="small" color={driver.status === 'ACTIVE' ? 'success' : 'default'} /></TableCell>
              <TableCell>-</TableCell>
              <TableCell><Rating value={driver.averageRating || 0} readOnly size="small" /></TableCell>
              <TableCell>{driver.totalTrips}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default DriverList;

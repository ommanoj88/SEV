import React, { useEffect } from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Chip, Avatar, Box, Rating } from '@mui/material';
import { Warning as WarningIcon, Error as ErrorIcon } from '@mui/icons-material';
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

  // Helper function to check license status
  const getLicenseStatus = (licenseExpiry: string) => {
    const today = new Date();
    const expiryDate = new Date(licenseExpiry);
    const daysUntilExpiry = Math.ceil((expiryDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));

    if (daysUntilExpiry < 0) {
      return { status: 'expired', color: '#f44336', label: 'EXPIRED' };
    } else if (daysUntilExpiry <= 30) {
      return { status: 'expiring', color: '#ff9800', label: `${daysUntilExpiry} days` };
    } else {
      return { status: 'valid', color: '#4caf50', label: '' };
    }
  };

  // Helper function to get row background color
  const getRowBgColor = (licenseExpiry: string) => {
    const status = getLicenseStatus(licenseExpiry);
    if (status.status === 'expired') {
      return '#ffebee'; // Light red
    } else if (status.status === 'expiring') {
      return '#fff3e0'; // Light orange/yellow
    }
    return 'transparent';
  };

  return (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Driver</TableCell>
            <TableCell>Email</TableCell>
            <TableCell>Phone</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>License Expiry</TableCell>
            <TableCell>Vehicle</TableCell>
            <TableCell>Rating</TableCell>
            <TableCell>Total Trips</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {drivers.map((driver) => {
            const licenseStatus = getLicenseStatus(driver.licenseExpiry);
            const rowBgColor = getRowBgColor(driver.licenseExpiry);

            return (
              <TableRow 
                key={driver.id} 
                hover 
                onClick={() => navigate(`/drivers/${driver.id}`)} 
                sx={{ cursor: 'pointer', backgroundColor: rowBgColor }}
              >
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
                <TableCell>
                  <Box display="flex" alignItems="center" gap={1}>
                    {licenseStatus.status === 'expired' && <ErrorIcon sx={{ color: licenseStatus.color, fontSize: 20 }} />}
                    {licenseStatus.status === 'expiring' && <WarningIcon sx={{ color: licenseStatus.color, fontSize: 20 }} />}
                    <Box>
                      <Box fontWeight={licenseStatus.status !== 'valid' ? 600 : 400} color={licenseStatus.color}>
                        {new Date(driver.licenseExpiry).toLocaleDateString()}
                      </Box>
                      {licenseStatus.label && (
                        <Box fontSize="0.75rem" color={licenseStatus.color}>
                          {licenseStatus.label}
                        </Box>
                      )}
                    </Box>
                  </Box>
                </TableCell>
                <TableCell>-</TableCell>
                <TableCell><Rating value={driver.averageRating || 0} readOnly size="small" /></TableCell>
                <TableCell>{driver.totalTrips}</TableCell>
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default DriverList;

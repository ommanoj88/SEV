import React, { useEffect, useState } from 'react';
import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  InputAdornment,
  Chip,
  IconButton,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Button,
  Tooltip,
} from '@mui/material';
import {
  Search as SearchIcon,
  Visibility as VisibilityIcon,
  Edit as EditIcon,
  Add as AddIcon,
  FilterList as FilterIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@redux/hooks';
import { fetchVehicles } from '@redux/slices/vehicleSlice';
import { Vehicle, VehicleStatus } from '../../types/vehicle';
import { formatBatteryLevel } from '@utils/formatters';

const VehicleList: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { vehicles, loading } = useAppSelector((state) => state.vehicles);

  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<VehicleStatus | ''>('');

  useEffect(() => {
    loadVehicles();
  }, [page, rowsPerPage, search, statusFilter]);

  const loadVehicles = () => {
    dispatch(fetchVehicles({
      search,
      status: statusFilter ? [statusFilter] : undefined,
    }));
  };

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const getStatusColor = (status: VehicleStatus): 'success' | 'warning' | 'error' | 'info' | 'default' => {
    switch (status) {
      case 'ACTIVE':
        return 'success';
      case 'CHARGING':
        return 'info';
      case 'INACTIVE':
        return 'warning';
      case 'MAINTENANCE':
        return 'error';
      case 'IN_TRIP':
        return 'info';
      default:
        return 'default';
    }
  };

  const getBatteryColor = (level: number): 'success' | 'warning' | 'error' => {
    if (level >= 60) return 'success';
    if (level >= 30) return 'warning';
    return 'error';
  };

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box display="flex" gap={2} flex={1}>
          <TextField
            size="small"
            placeholder="Search vehicles..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
            sx={{ minWidth: 300 }}
          />
          <FormControl size="small" sx={{ minWidth: 200 }}>
            <InputLabel>Status</InputLabel>
            <Select
              value={statusFilter}
              label="Status"
              onChange={(e) => setStatusFilter(e.target.value as VehicleStatus | '')}
            >
              <MenuItem value="">All Status</MenuItem>
              <MenuItem value="active">Active</MenuItem>
              <MenuItem value="charging">Charging</MenuItem>
              <MenuItem value="idle">Idle</MenuItem>
              <MenuItem value="maintenance">Maintenance</MenuItem>
              <MenuItem value="offline">Offline</MenuItem>
            </Select>
          </FormControl>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/fleet/add')}
        >
          Add Vehicle
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Vehicle</TableCell>
              <TableCell>VIN</TableCell>
              <TableCell>License Plate</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Battery</TableCell>
              <TableCell>Range</TableCell>
              <TableCell>Driver</TableCell>
              <TableCell>Location</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {vehicles.map((vehicle) => (
              <TableRow key={vehicle.id} hover>
                <TableCell>
                  <Box>
                    <Box fontWeight="500">
                      {vehicle.make} {vehicle.model}
                    </Box>
                    <Box fontSize="0.875rem" color="text.secondary">
                      {vehicle.year} • {vehicle.type}
                    </Box>
                  </Box>
                </TableCell>
                <TableCell>{vehicle.vin}</TableCell>
                <TableCell>{vehicle.licensePlate}</TableCell>
                <TableCell>
                  <Chip
                    label={vehicle.status}
                    size="small"
                    color={getStatusColor(vehicle.status)}
                  />
                </TableCell>
                <TableCell>
                  <Chip
                    label={formatBatteryLevel(vehicle.battery.stateOfCharge)}
                    size="small"
                    color={getBatteryColor(vehicle.battery.stateOfCharge)}
                    variant="outlined"
                  />
                </TableCell>
                <TableCell>{vehicle.battery.range} km</TableCell>
                <TableCell>
                  {vehicle.assignedDriverName || (
                    <Box color="text.secondary" fontStyle="italic">
                      Unassigned
                    </Box>
                  )}
                </TableCell>
                <TableCell>
                  {vehicle.location.address || 'Unknown'}
                </TableCell>
                <TableCell align="right">
                  <Tooltip title="View Details">
                    <IconButton
                      size="small"
                      onClick={() => navigate(`/fleet/${vehicle.id}`)}
                    >
                      <VisibilityIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Edit">
                    <IconButton
                      size="small"
                      onClick={() => navigate(`/fleet/${vehicle.id}/edit`)}
                    >
                      <EditIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <TablePagination
          rowsPerPageOptions={[5, 10, 25, 50]}
          component="div"
          count={vehicles.length}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
        />
      </TableContainer>
    </Box>
  );
};

export default VehicleList;

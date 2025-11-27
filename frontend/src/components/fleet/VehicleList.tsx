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
import { Vehicle, VehicleStatus, FuelType } from '../../types/vehicle';
import { formatBatteryLevel } from '@utils/formatters';
import { getFuelTypeOption } from '../../constants/fuelTypes';

const VehicleList: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { vehicles, loading } = useAppSelector((state) => state.vehicles);
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);

  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<VehicleStatus | ''>('');

  // User is guaranteed to be available because App.tsx waits for auth initialization
  const companyId = user?.companyId;

  // Effect to load vehicles when component mounts or companyId changes
  useEffect(() => {
    if (companyId) {
      console.log('[VehicleList] Fetching vehicles for companyId:', companyId);
      dispatch(fetchVehicles({
        search,
        status: statusFilter ? [statusFilter] : undefined,
      }));
    }
  }, [dispatch, companyId]);

  // Effect to reload on filter changes
  useEffect(() => {
    if (companyId && (search || statusFilter)) {
      dispatch(fetchVehicles({
        search,
        status: statusFilter ? [statusFilter] : undefined,
      }));
    }
  }, [search, statusFilter]);

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

  const getFuelColor = (level: number, capacity: number): 'success' | 'warning' | 'error' => {
    const percentage = (level / capacity) * 100;
    if (percentage >= 60) return 'success';
    if (percentage >= 30) return 'warning';
    return 'error';
  };

  const calculateFuelPercentage = (fuelLevel: number, capacity: number): number => {
    return (fuelLevel / capacity) * 100;
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
              <MenuItem value="ACTIVE">Active</MenuItem>
              <MenuItem value="CHARGING">Charging</MenuItem>
              <MenuItem value="INACTIVE">Inactive</MenuItem>
              <MenuItem value="MAINTENANCE">Maintenance</MenuItem>
              <MenuItem value="IN_TRIP">In Trip</MenuItem>
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
              <TableCell>Fuel Type</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Battery/Fuel</TableCell>
              <TableCell>Range</TableCell>
              <TableCell>Driver</TableCell>
              <TableCell>Location</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {vehicles.map((vehicle) => {
              const fuelTypeOption = getFuelTypeOption(vehicle.fuelType);
              // Battery tracking only for 4-wheelers (LCV) with EV/HYBRID - 2W/3W use GPS-only
              const is4Wheeler = vehicle.type === 'LCV';
              const showBattery = is4Wheeler && (vehicle.fuelType === FuelType.EV || vehicle.fuelType === FuelType.HYBRID);
              const showFuel = vehicle.fuelType === FuelType.ICE || vehicle.fuelType === FuelType.HYBRID;

              return (
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
                      label={`${fuelTypeOption?.icon || ''} ${fuelTypeOption?.label || vehicle.fuelType}`}
                      size="small"
                      sx={{
                        backgroundColor: fuelTypeOption?.color || '#666666',
                        color: 'white',
                      }}
                    />
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={vehicle.status}
                      size="small"
                      color={getStatusColor(vehicle.status)}
                    />
                  </TableCell>
                  <TableCell>
                    <Box display="flex" flexDirection="column" gap={0.5}>
                      {showBattery && (
                        <Chip
                          label={`⚡ ${formatBatteryLevel(vehicle.battery.stateOfCharge)}`}
                          size="small"
                          color={getBatteryColor(vehicle.battery.stateOfCharge)}
                          variant="outlined"
                        />
                      )}
                      {showFuel && vehicle.fuelLevel !== undefined && vehicle.fuelTankCapacity && (
                        <Chip
                          label={`⛽ ${calculateFuelPercentage(vehicle.fuelLevel, vehicle.fuelTankCapacity).toFixed(0)}%`}
                          size="small"
                          color={getFuelColor(vehicle.fuelLevel, vehicle.fuelTankCapacity)}
                          variant="outlined"
                        />
                      )}
                    </Box>
                  </TableCell>
                  <TableCell>
                    {showBattery && `${vehicle.battery.range} km`}
                    {showFuel && !showBattery && vehicle.fuelLevel && vehicle.fuelTankCapacity && (
                      <Box>~{Math.round((vehicle.fuelLevel / vehicle.fuelTankCapacity) * vehicle.battery.range)} km</Box>
                    )}
                  </TableCell>
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
              );
            })}
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

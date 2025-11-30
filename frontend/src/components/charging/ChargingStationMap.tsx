import React, { useEffect, useState, useRef, useCallback } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Chip,
  Button,
  IconButton,
  TextField,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Slider,
  CircularProgress,
  Alert,
  Drawer,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  ToggleButton,
  ToggleButtonGroup,
  Badge,
  Tooltip,
  Fab,
  Collapse,
  Paper,
  Grid,
} from '@mui/material';
import {
  Search as SearchIcon,
  MyLocation as MyLocationIcon,
  FilterList as FilterIcon,
  Refresh as RefreshIcon,
  Navigation as NavigationIcon,
  EvStation as EvStationIcon,
  Speed as SpeedIcon,
  AttachMoney as MoneyIcon,
  Schedule as ScheduleIcon,
  Close as CloseIcon,
  Layers as LayersIcon,
  ZoomIn as ZoomInIcon,
  ZoomOut as ZoomOutIcon,
  DirectionsCar as CarIcon,
  BatteryChargingFull as BatteryIcon,
  CheckCircle as AvailableIcon,
  Cancel as OccupiedIcon,
  PowerOff as OfflineIcon,
  Star as StarIcon,
  Directions as DirectionsIcon,
  Map as MapIcon,
  Satellite as SatelliteIcon,
} from '@mui/icons-material';
import { useAppSelector } from '../../store/hooks';
import { selectChargingStations } from '../../store/slices/chargingSlice';

// Types for charging station with real-time status
interface ChargingPort {
  id: string;
  portNumber: number;
  type: 'CCS2' | 'CHAdeMO' | 'Type2' | 'GB/T' | 'Tesla';
  powerKw: number;
  status: 'available' | 'occupied' | 'reserved' | 'maintenance' | 'offline';
  currentSessionId?: string;
  estimatedAvailableAt?: string;
}

interface RealTimeStation {
  id: string;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  operatorName: string;
  status: 'online' | 'offline' | 'maintenance';
  ports: ChargingPort[];
  totalPorts: number;
  availablePorts: number;
  pricePerKwh: number;
  rating: number;
  totalReviews: number;
  amenities: string[];
  operatingHours: string;
  distance?: number;
  estimatedWaitTime?: number;
  lastUpdated: string;
}

interface MapPosition {
  lat: number;
  lng: number;
  zoom: number;
}

interface StationCluster {
  id: string;
  lat: number;
  lng: number;
  count: number;
  stations: RealTimeStation[];
}

// Custom marker component for canvas rendering
const getMarkerColor = (station: RealTimeStation): string => {
  const availabilityRatio = station.availablePorts / station.totalPorts;
  if (station.status === 'offline') return '#9e9e9e';
  if (station.status === 'maintenance') return '#ff9800';
  if (availabilityRatio === 0) return '#f44336';
  if (availabilityRatio < 0.3) return '#ff9800';
  return '#4caf50';
};

const ChargingStationMap: React.FC = () => {
  // State for map and stations
  const [mapPosition, setMapPosition] = useState<MapPosition>({
    lat: 19.076, // Mumbai default
    lng: 72.8777,
    zoom: 12,
  });
  const [stations, setStations] = useState<RealTimeStation[]>([]);
  const [clusters, setClusters] = useState<StationCluster[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedStation, setSelectedStation] = useState<RealTimeStation | null>(null);
  const [userLocation, setUserLocation] = useState<{ lat: number; lng: number } | null>(null);

  // Filter state
  const [showFilters, setShowFilters] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [chargerType, setChargerType] = useState<string>('all');
  const [minPower, setMinPower] = useState<number>(0);
  const [maxPrice, setMaxPrice] = useState<number>(20);
  const [onlyAvailable, setOnlyAvailable] = useState(false);
  const [maxDistance, setMaxDistance] = useState<number>(50);

  // Map controls
  const [mapStyle, setMapStyle] = useState<'map' | 'satellite'>('map');
  const [showStationList, setShowStationList] = useState(false);

  const mapRef = useRef<HTMLDivElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const refreshIntervalRef = useRef<NodeJS.Timeout | null>(null);

  // Generate mock station data with real-time status
  const generateMockStations = useCallback((): RealTimeStation[] => {
    const baseStations = [
      { id: '1', name: 'Tata Power EZ Charge - BKC', lat: 19.0596, lng: 72.8656, operator: 'Tata Power' },
      { id: '2', name: 'Ather Grid - Powai', lat: 19.1176, lng: 72.9060, operator: 'Ather Energy' },
      { id: '3', name: 'ChargeZone Hub - Andheri', lat: 19.1136, lng: 72.8697, operator: 'ChargeZone' },
      { id: '4', name: 'EESL Fast Charger - Worli', lat: 19.0178, lng: 72.8148, operator: 'EESL' },
      { id: '5', name: 'Fortum Charge - Bandra', lat: 19.0596, lng: 72.8295, operator: 'Fortum' },
      { id: '6', name: 'Statiq Station - Juhu', lat: 19.1076, lng: 72.8267, operator: 'Statiq' },
      { id: '7', name: 'MG Fast Charge - Thane', lat: 19.2183, lng: 72.9781, operator: 'MG Motor' },
      { id: '8', name: 'Jio-bp Pulse - Chembur', lat: 19.0622, lng: 72.8978, operator: 'Jio-bp' },
      { id: '9', name: 'Kazam EV Hub - Mulund', lat: 19.1753, lng: 72.9567, operator: 'Kazam' },
      { id: '10', name: 'ElectriFi Point - Malad', lat: 19.1871, lng: 72.8491, operator: 'ElectriFi' },
      { id: '11', name: 'Tata Power - Lower Parel', lat: 18.9981, lng: 72.8286, operator: 'Tata Power' },
      { id: '12', name: 'Ather Grid - Vikhroli', lat: 19.1089, lng: 72.9175, operator: 'Ather Energy' },
      { id: '13', name: 'ChargeZone - Goregaon', lat: 19.1649, lng: 72.8497, operator: 'ChargeZone' },
      { id: '14', name: 'HPCL EV Station - Dadar', lat: 19.0176, lng: 72.8430, operator: 'HPCL' },
      { id: '15', name: 'IOCL EV Charge - Vashi', lat: 19.0772, lng: 73.0075, operator: 'IOCL' },
    ];

    return baseStations.map((base, index) => {
      const totalPorts = Math.floor(Math.random() * 6) + 2;
      const availablePorts = Math.floor(Math.random() * (totalPorts + 1));
      const statusOptions: Array<'online' | 'offline' | 'maintenance'> = ['online', 'online', 'online', 'offline', 'maintenance'];
      const status = statusOptions[Math.floor(Math.random() * statusOptions.length)];

      const portTypes: Array<'CCS2' | 'CHAdeMO' | 'Type2' | 'GB/T' | 'Tesla'> = ['CCS2', 'CHAdeMO', 'Type2', 'GB/T'];
      const ports: ChargingPort[] = Array.from({ length: totalPorts }, (_, i) => ({
        id: `${base.id}-port-${i + 1}`,
        portNumber: i + 1,
        type: portTypes[Math.floor(Math.random() * portTypes.length)],
        powerKw: [22, 50, 60, 120, 150, 180][Math.floor(Math.random() * 6)],
        status: i < availablePorts ? 'available' : (['occupied', 'reserved', 'maintenance'] as const)[Math.floor(Math.random() * 3)],
        estimatedAvailableAt: i >= availablePorts ? new Date(Date.now() + Math.random() * 3600000).toISOString() : undefined,
      }));

      return {
        id: base.id,
        name: base.name,
        address: `Near ${['Metro Station', 'Shopping Mall', 'Business Park', 'Highway', 'Residential Complex'][Math.floor(Math.random() * 5)]}, Mumbai`,
        latitude: base.lat + (Math.random() - 0.5) * 0.01,
        longitude: base.lng + (Math.random() - 0.5) * 0.01,
        operatorName: base.operator,
        status,
        ports,
        totalPorts,
        availablePorts: status === 'offline' ? 0 : availablePorts,
        pricePerKwh: Math.round((8 + Math.random() * 10) * 100) / 100,
        rating: Math.round((3.5 + Math.random() * 1.5) * 10) / 10,
        totalReviews: Math.floor(Math.random() * 200) + 10,
        amenities: ['Restroom', 'Café', 'WiFi', 'Parking', 'Security'].filter(() => Math.random() > 0.5),
        operatingHours: Math.random() > 0.3 ? '24/7' : '6:00 AM - 10:00 PM',
        distance: userLocation ? calculateDistance(userLocation.lat, userLocation.lng, base.lat, base.lng) : undefined,
        estimatedWaitTime: availablePorts === 0 ? Math.floor(Math.random() * 45) + 5 : 0,
        lastUpdated: new Date().toISOString(),
      };
    });
  }, [userLocation]);

  // Calculate distance between two points (Haversine formula)
  const calculateDistance = (lat1: number, lon1: number, lat2: number, lon2: number): number => {
    const R = 6371; // Earth's radius in km
    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLon = ((lon2 - lon1) * Math.PI) / 180;
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return Math.round(R * c * 10) / 10;
  };

  // Cluster stations when zoomed out
  const clusterStations = useCallback((stations: RealTimeStation[], zoom: number): StationCluster[] => {
    if (zoom >= 13) return []; // Don't cluster when zoomed in

    const gridSize = 0.05 * Math.pow(2, 15 - zoom);
    const clusterMap = new Map<string, RealTimeStation[]>();

    stations.forEach((station) => {
      const gridX = Math.floor(station.longitude / gridSize);
      const gridY = Math.floor(station.latitude / gridSize);
      const key = `${gridX}-${gridY}`;
      if (!clusterMap.has(key)) {
        clusterMap.set(key, []);
      }
      clusterMap.get(key)!.push(station);
    });

    return Array.from(clusterMap.entries())
      .filter(([, stations]) => stations.length > 1)
      .map(([key, stations]) => ({
        id: key,
        lat: stations.reduce((sum, s) => sum + s.latitude, 0) / stations.length,
        lng: stations.reduce((sum, s) => sum + s.longitude, 0) / stations.length,
        count: stations.length,
        stations,
      }));
  }, []);

  // Filter stations based on user preferences
  const filterStations = useCallback(
    (stations: RealTimeStation[]): RealTimeStation[] => {
      return stations.filter((station) => {
        // Search query
        if (searchQuery && !station.name.toLowerCase().includes(searchQuery.toLowerCase()) && !station.operatorName.toLowerCase().includes(searchQuery.toLowerCase())) {
          return false;
        }

        // Charger type filter
        if (chargerType !== 'all' && !station.ports.some((p) => p.type === chargerType)) {
          return false;
        }

        // Min power filter
        if (minPower > 0 && !station.ports.some((p) => p.powerKw >= minPower)) {
          return false;
        }

        // Max price filter
        if (station.pricePerKwh > maxPrice) {
          return false;
        }

        // Only available filter
        if (onlyAvailable && station.availablePorts === 0) {
          return false;
        }

        // Distance filter
        if (station.distance !== undefined && station.distance > maxDistance) {
          return false;
        }

        return true;
      });
    },
    [searchQuery, chargerType, minPower, maxPrice, onlyAvailable, maxDistance]
  );

  // Get user's current location
  const getUserLocation = useCallback(() => {
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const loc = {
            lat: position.coords.latitude,
            lng: position.coords.longitude,
          };
          setUserLocation(loc);
          setMapPosition((prev) => ({ ...prev, lat: loc.lat, lng: loc.lng }));
        },
        (error) => {
          console.error('Error getting location:', error);
          setError('Unable to get your location. Please enable location services.');
        }
      );
    }
  }, []);

  // Refresh station data
  const refreshStations = useCallback(() => {
    setLoading(true);
    // Simulate API call
    setTimeout(() => {
      const newStations = generateMockStations();
      setStations(newStations);
      setClusters(clusterStations(filterStations(newStations), mapPosition.zoom));
      setLoading(false);
    }, 500);
  }, [generateMockStations, clusterStations, filterStations, mapPosition.zoom]);

  // Initialize and set up auto-refresh
  useEffect(() => {
    getUserLocation();
    refreshStations();

    // Auto-refresh every 30 seconds
    refreshIntervalRef.current = setInterval(refreshStations, 30000);

    return () => {
      if (refreshIntervalRef.current) {
        clearInterval(refreshIntervalRef.current);
      }
    };
  }, []);

  // Update clusters when filters change
  useEffect(() => {
    setClusters(clusterStations(filterStations(stations), mapPosition.zoom));
  }, [stations, filterStations, clusterStations, mapPosition.zoom]);

  // Render canvas-based map
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const rect = canvas.getBoundingClientRect();
    canvas.width = rect.width;
    canvas.height = rect.height;

    // Clear canvas
    ctx.fillStyle = mapStyle === 'satellite' ? '#1a1a2e' : '#e8f4e8';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // Draw grid lines
    ctx.strokeStyle = mapStyle === 'satellite' ? '#2a2a4e' : '#c8e8c8';
    ctx.lineWidth = 1;
    for (let i = 0; i < canvas.width; i += 50) {
      ctx.beginPath();
      ctx.moveTo(i, 0);
      ctx.lineTo(i, canvas.height);
      ctx.stroke();
    }
    for (let i = 0; i < canvas.height; i += 50) {
      ctx.beginPath();
      ctx.moveTo(0, i);
      ctx.lineTo(canvas.width, i);
      ctx.stroke();
    }

    // Project lat/lng to canvas coordinates
    const project = (lat: number, lng: number): { x: number; y: number } => {
      const scale = Math.pow(2, mapPosition.zoom - 10);
      const x = (lng - mapPosition.lng) * scale * 1000 + canvas.width / 2;
      const y = (mapPosition.lat - lat) * scale * 1000 + canvas.height / 2;
      return { x, y };
    };

    // Draw user location
    if (userLocation) {
      const { x, y } = project(userLocation.lat, userLocation.lng);
      ctx.beginPath();
      ctx.arc(x, y, 12, 0, Math.PI * 2);
      ctx.fillStyle = 'rgba(33, 150, 243, 0.3)';
      ctx.fill();
      ctx.beginPath();
      ctx.arc(x, y, 6, 0, Math.PI * 2);
      ctx.fillStyle = '#2196f3';
      ctx.fill();
      ctx.strokeStyle = 'white';
      ctx.lineWidth = 2;
      ctx.stroke();
    }

    // Draw clusters
    clusters.forEach((cluster) => {
      const { x, y } = project(cluster.lat, cluster.lng);
      if (x >= -20 && x <= canvas.width + 20 && y >= -20 && y <= canvas.height + 20) {
        ctx.beginPath();
        ctx.arc(x, y, 25, 0, Math.PI * 2);
        ctx.fillStyle = '#1976d2';
        ctx.fill();
        ctx.strokeStyle = 'white';
        ctx.lineWidth = 3;
        ctx.stroke();
        ctx.fillStyle = 'white';
        ctx.font = 'bold 14px Arial';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(cluster.count.toString(), x, y);
      }
    });

    // Draw station markers
    const filteredStations = filterStations(stations);
    filteredStations.forEach((station) => {
      const { x, y } = project(station.latitude, station.longitude);
      if (x >= -20 && x <= canvas.width + 20 && y >= -20 && y <= canvas.height + 20) {
        // Check if part of a cluster
        const isInCluster = clusters.some((c) => c.stations.some((s) => s.id === station.id));
        if (!isInCluster) {
          const color = getMarkerColor(station);
          const isSelected = selectedStation?.id === station.id;

          // Draw marker shadow
          ctx.beginPath();
          ctx.arc(x + 2, y + 2, isSelected ? 18 : 14, 0, Math.PI * 2);
          ctx.fillStyle = 'rgba(0, 0, 0, 0.2)';
          ctx.fill();

          // Draw marker
          ctx.beginPath();
          ctx.arc(x, y, isSelected ? 18 : 14, 0, Math.PI * 2);
          ctx.fillStyle = color;
          ctx.fill();
          ctx.strokeStyle = isSelected ? '#000' : 'white';
          ctx.lineWidth = isSelected ? 3 : 2;
          ctx.stroke();

          // Draw availability count
          ctx.fillStyle = 'white';
          ctx.font = `bold ${isSelected ? 12 : 10}px Arial`;
          ctx.textAlign = 'center';
          ctx.textBaseline = 'middle';
          ctx.fillText(`${station.availablePorts}/${station.totalPorts}`, x, y);
        }
      }
    });

    // Draw scale bar
    const scaleWidth = 100;
    const scaleKm = (scaleWidth / Math.pow(2, mapPosition.zoom - 10)) / 1000;
    ctx.fillStyle = mapStyle === 'satellite' ? '#fff' : '#333';
    ctx.font = '11px Arial';
    ctx.fillText(`${scaleKm.toFixed(1)} km`, 20, canvas.height - 20);
    ctx.fillRect(20, canvas.height - 35, scaleWidth, 4);
  }, [stations, clusters, selectedStation, userLocation, mapPosition, mapStyle, filterStations]);

  // Handle canvas click to select station
  const handleCanvasClick = (e: React.MouseEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    // Project click to lat/lng
    const scale = Math.pow(2, mapPosition.zoom - 10);
    const lng = (x - canvas.width / 2) / (scale * 1000) + mapPosition.lng;
    const lat = mapPosition.lat - (y - canvas.height / 2) / (scale * 1000);

    // Find nearest station
    let nearestStation: RealTimeStation | null = null;
    let minDist = Infinity;

    filterStations(stations).forEach((station) => {
      const dist = Math.sqrt(Math.pow(station.latitude - lat, 2) + Math.pow(station.longitude - lng, 2));
      if (dist < minDist && dist < 0.01) {
        minDist = dist;
        nearestStation = station;
      }
    });

    setSelectedStation(nearestStation);
  };

  // Navigate to station
  const navigateToStation = (station: RealTimeStation) => {
    const url = `https://www.google.com/maps/dir/?api=1&destination=${station.latitude},${station.longitude}`;
    window.open(url, '_blank');
  };

  // Zoom controls
  const handleZoomIn = () => {
    setMapPosition((prev) => ({ ...prev, zoom: Math.min(prev.zoom + 1, 18) }));
  };

  const handleZoomOut = () => {
    setMapPosition((prev) => ({ ...prev, zoom: Math.max(prev.zoom - 1, 8) }));
  };

  // Get status color for chips
  const getStatusColor = (status: string): 'success' | 'warning' | 'error' | 'default' => {
    switch (status) {
      case 'available':
        return 'success';
      case 'occupied':
      case 'reserved':
        return 'warning';
      case 'offline':
      case 'maintenance':
        return 'error';
      default:
        return 'default';
    }
  };

  const filteredStations = filterStations(stations);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Header with search and controls */}
      <Paper elevation={2} sx={{ p: 2, mb: 1 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              size="small"
              placeholder="Search stations or operators..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
            />
          </Grid>
          <Grid item xs={12} md={8}>
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', justifyContent: 'flex-end' }}>
              <Tooltip title="Filter stations">
                <IconButton onClick={() => setShowFilters(!showFilters)} color={showFilters ? 'primary' : 'default'}>
                  <FilterIcon />
                </IconButton>
              </Tooltip>
              <Tooltip title="My location">
                <IconButton onClick={getUserLocation} color="primary">
                  <MyLocationIcon />
                </IconButton>
              </Tooltip>
              <Tooltip title="Refresh stations">
                <IconButton onClick={refreshStations} disabled={loading}>
                  <RefreshIcon />
                </IconButton>
              </Tooltip>
              <ToggleButtonGroup value={mapStyle} exclusive onChange={(_, value) => value && setMapStyle(value)} size="small">
                <ToggleButton value="map">
                  <MapIcon fontSize="small" />
                </ToggleButton>
                <ToggleButton value="satellite">
                  <SatelliteIcon fontSize="small" />
                </ToggleButton>
              </ToggleButtonGroup>
              <Button variant="outlined" startIcon={<LayersIcon />} onClick={() => setShowStationList(!showStationList)}>
                List ({filteredStations.length})
              </Button>
            </Box>
          </Grid>
        </Grid>

        {/* Filter panel */}
        <Collapse in={showFilters}>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={6} md={3}>
              <FormControl fullWidth size="small">
                <InputLabel>Charger Type</InputLabel>
                <Select value={chargerType} onChange={(e) => setChargerType(e.target.value)} label="Charger Type">
                  <MenuItem value="all">All Types</MenuItem>
                  <MenuItem value="CCS2">CCS2</MenuItem>
                  <MenuItem value="CHAdeMO">CHAdeMO</MenuItem>
                  <MenuItem value="Type2">Type 2</MenuItem>
                  <MenuItem value="GB/T">GB/T</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={6} md={3}>
              <Typography variant="caption" color="text.secondary">
                Min Power: {minPower} kW
              </Typography>
              <Slider value={minPower} onChange={(_, v) => setMinPower(v as number)} min={0} max={150} step={10} valueLabelDisplay="auto" size="small" />
            </Grid>
            <Grid item xs={6} md={3}>
              <Typography variant="caption" color="text.secondary">
                Max Price: ₹{maxPrice}/kWh
              </Typography>
              <Slider value={maxPrice} onChange={(_, v) => setMaxPrice(v as number)} min={5} max={25} step={1} valueLabelDisplay="auto" size="small" />
            </Grid>
            <Grid item xs={6} md={3}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Chip
                  icon={<AvailableIcon />}
                  label="Available Only"
                  onClick={() => setOnlyAvailable(!onlyAvailable)}
                  color={onlyAvailable ? 'success' : 'default'}
                  variant={onlyAvailable ? 'filled' : 'outlined'}
                />
              </Box>
            </Grid>
          </Grid>
        </Collapse>
      </Paper>

      {/* Map container */}
      <Box sx={{ flex: 1, position: 'relative' }}>
        {loading && (
          <Box
            sx={{
              position: 'absolute',
              top: '50%',
              left: '50%',
              transform: 'translate(-50%, -50%)',
              zIndex: 1000,
            }}
          >
            <CircularProgress />
          </Box>
        )}

        {error && (
          <Alert severity="error" sx={{ position: 'absolute', top: 10, left: 10, right: 10, zIndex: 1000 }}>
            {error}
          </Alert>
        )}

        {/* Canvas-based map */}
        <canvas
          ref={canvasRef}
          style={{
            width: '100%',
            height: '100%',
            cursor: 'pointer',
          }}
          onClick={handleCanvasClick}
        />

        {/* Map controls */}
        <Box sx={{ position: 'absolute', right: 10, top: 10, display: 'flex', flexDirection: 'column', gap: 1 }}>
          <Fab size="small" onClick={handleZoomIn}>
            <ZoomInIcon />
          </Fab>
          <Fab size="small" onClick={handleZoomOut}>
            <ZoomOutIcon />
          </Fab>
        </Box>

        {/* Legend */}
        <Paper
          elevation={2}
          sx={{
            position: 'absolute',
            bottom: 20,
            left: 20,
            p: 1,
            display: 'flex',
            gap: 2,
            alignItems: 'center',
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <Box sx={{ width: 12, height: 12, borderRadius: '50%', bgcolor: '#4caf50' }} />
            <Typography variant="caption">Available</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <Box sx={{ width: 12, height: 12, borderRadius: '50%', bgcolor: '#ff9800' }} />
            <Typography variant="caption">Limited</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <Box sx={{ width: 12, height: 12, borderRadius: '50%', bgcolor: '#f44336' }} />
            <Typography variant="caption">Full</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <Box sx={{ width: 12, height: 12, borderRadius: '50%', bgcolor: '#9e9e9e' }} />
            <Typography variant="caption">Offline</Typography>
          </Box>
        </Paper>

        {/* Selected station details panel */}
        {selectedStation && (
          <Card
            elevation={4}
            sx={{
              position: 'absolute',
              bottom: 20,
              left: '50%',
              transform: 'translateX(-50%)',
              width: '90%',
              maxWidth: 500,
            }}
          >
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                <Box>
                  <Typography variant="h6" gutterBottom>
                    {selectedStation.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    {selectedStation.address}
                  </Typography>
                </Box>
                <IconButton size="small" onClick={() => setSelectedStation(null)}>
                  <CloseIcon />
                </IconButton>
              </Box>

              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 2 }}>
                <Chip
                  size="small"
                  icon={<EvStationIcon />}
                  label={`${selectedStation.availablePorts}/${selectedStation.totalPorts} ports`}
                  color={selectedStation.availablePorts > 0 ? 'success' : 'error'}
                />
                <Chip size="small" icon={<MoneyIcon />} label={`₹${selectedStation.pricePerKwh}/kWh`} />
                <Chip size="small" icon={<StarIcon />} label={`${selectedStation.rating} (${selectedStation.totalReviews})`} />
                {selectedStation.distance !== undefined && <Chip size="small" icon={<DirectionsIcon />} label={`${selectedStation.distance} km`} />}
                <Chip size="small" icon={<ScheduleIcon />} label={selectedStation.operatingHours} />
              </Box>

              {/* Port details */}
              <Typography variant="subtitle2" gutterBottom>
                Charging Ports
              </Typography>
              <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', mb: 2 }}>
                {selectedStation.ports.map((port) => (
                  <Tooltip
                    key={port.id}
                    title={
                      <Box>
                        <Typography variant="caption">
                          Port {port.portNumber}: {port.type}
                        </Typography>
                        <br />
                        <Typography variant="caption">Power: {port.powerKw} kW</Typography>
                        {port.estimatedAvailableAt && (
                          <>
                            <br />
                            <Typography variant="caption">Available at: {new Date(port.estimatedAvailableAt).toLocaleTimeString()}</Typography>
                          </>
                        )}
                      </Box>
                    }
                  >
                    <Chip
                      size="small"
                      label={`${port.portNumber}: ${port.powerKw}kW`}
                      color={getStatusColor(port.status)}
                      sx={{ minWidth: 80 }}
                    />
                  </Tooltip>
                ))}
              </Box>

              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button variant="contained" startIcon={<NavigationIcon />} onClick={() => navigateToStation(selectedStation)} fullWidth>
                  Navigate
                </Button>
                <Button variant="outlined" startIcon={<BatteryIcon />} fullWidth>
                  Start Charging
                </Button>
              </Box>
            </CardContent>
          </Card>
        )}
      </Box>

      {/* Station list drawer */}
      <Drawer anchor="right" open={showStationList} onClose={() => setShowStationList(false)}>
        <Box sx={{ width: 350, p: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6">Nearby Stations ({filteredStations.length})</Typography>
            <IconButton onClick={() => setShowStationList(false)}>
              <CloseIcon />
            </IconButton>
          </Box>
          <List>
            {filteredStations
              .sort((a, b) => (a.distance || 999) - (b.distance || 999))
              .map((station) => (
                <React.Fragment key={station.id}>
                  <ListItem
                    button
                    onClick={() => {
                      setSelectedStation(station);
                      setShowStationList(false);
                      setMapPosition((prev) => ({
                        ...prev,
                        lat: station.latitude,
                        lng: station.longitude,
                        zoom: 15,
                      }));
                    }}
                    sx={{
                      bgcolor: selectedStation?.id === station.id ? 'action.selected' : undefined,
                    }}
                  >
                    <ListItemIcon>
                      <Badge
                        badgeContent={station.availablePorts}
                        color={station.availablePorts > 0 ? 'success' : 'error'}
                      >
                        <EvStationIcon color={station.status === 'offline' ? 'disabled' : 'primary'} />
                      </Badge>
                    </ListItemIcon>
                    <ListItemText
                      primary={station.name}
                      secondary={
                        <Box>
                          <Typography variant="caption" color="text.secondary">
                            {station.operatorName} • ₹{station.pricePerKwh}/kWh
                          </Typography>
                          {station.distance !== undefined && (
                            <Typography variant="caption" color="primary" sx={{ display: 'block' }}>
                              {station.distance} km away
                            </Typography>
                          )}
                        </Box>
                      }
                    />
                  </ListItem>
                  <Divider />
                </React.Fragment>
              ))}
          </List>
        </Box>
      </Drawer>
    </Box>
  );
};

export default ChargingStationMap;

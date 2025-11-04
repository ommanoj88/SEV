import React, { useEffect, useRef, useState } from 'react';
import mapboxgl from 'mapbox-gl';
import { Box, Paper, TextField, InputAdornment, Chip, List, ListItem, ListItemText, Typography } from '@mui/material';
import { Search as SearchIcon } from '@mui/icons-material';
import { useAppSelector } from '@redux/hooks';

mapboxgl.accessToken = process.env.REACT_APP_MAPBOX_TOKEN || '';

const ChargingStations: React.FC = () => {
  const mapContainer = useRef<HTMLDivElement>(null);
  const map = useRef<mapboxgl.Map | null>(null);
  const [search, setSearch] = useState('');
  const { stations } = useAppSelector((state) => state.charging);

  useEffect(() => {
    if (!mapContainer.current) return;
    map.current = new mapboxgl.Map({
      container: mapContainer.current,
      style: 'mapbox://styles/mapbox/streets-v12',
      center: [-98.5795, 39.8283],
      zoom: 10,
    });
    map.current.addControl(new mapboxgl.NavigationControl());
    return () => map.current?.remove();
  }, []);

  useEffect(() => {
    if (!map.current || !stations.length) return;
    stations.forEach((station) => {
      const el = document.createElement('div');
      el.innerHTML = '<svg width="30" height="30"><circle cx="15" cy="15" r="12" fill="#2196f3" stroke="white" stroke-width="2"/></svg>';
      new mapboxgl.Marker(el)
        .setLngLat([station.location.longitude, station.location.latitude])
        .setPopup(new mapboxgl.Popup().setHTML(`
          <h4>${station.name}</h4>
          <p>${station.availablePorts}/${station.totalPorts} available</p>
          <p>$${station.costPerKwh}/kWh</p>
        `))
        .addTo(map.current!);
    });
  }, [stations]);

  return (
    <Box display="flex" gap={2} height="600px">
      <Paper sx={{ flex: 1, overflow: 'hidden' }}>
        <Box ref={mapContainer} sx={{ height: '100%', width: '100%' }} />
      </Paper>
      <Paper sx={{ width: 350, p: 2, overflow: 'auto' }}>
        <TextField
          size="small"
          fullWidth
          placeholder="Search stations..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          InputProps={{
            startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment>,
          }}
          sx={{ mb: 2 }}
        />
        <List>
          {stations.filter((s) => s.name.toLowerCase().includes(search.toLowerCase())).map((station) => (
            <ListItem key={station.id} divider>
              <ListItemText
                primary={station.name}
                secondary={
                  <>
                    <Typography variant="caption" display="block">{station.location.address}</Typography>
                    <Chip label={`${station.availablePorts}/${station.totalPorts} available`} size="small" color={station.availablePorts > 0 ? 'success' : 'default'} sx={{ mt: 0.5 }} />
                  </>
                }
              />
            </ListItem>
          ))}
        </List>
      </Paper>
    </Box>
  );
};

export default ChargingStations;

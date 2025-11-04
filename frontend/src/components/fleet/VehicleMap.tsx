import React, { useEffect, useRef, useState } from 'react';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';
import { Box, Paper } from '@mui/material';
import { useAppSelector } from '@redux/hooks';
import { Vehicle } from '../../types/vehicle';

mapboxgl.accessToken = process.env.REACT_APP_MAPBOX_TOKEN || '';

interface VehicleMapProps {
  vehicles?: Vehicle[];
  height?: number | string;
  selectedVehicleId?: string;
}

const VehicleMap: React.FC<VehicleMapProps> = ({
  vehicles: propVehicles,
  height = 500,
  selectedVehicleId,
}) => {
  const mapContainer = useRef<HTMLDivElement>(null);
  const map = useRef<mapboxgl.Map | null>(null);
  const markers = useRef<mapboxgl.Marker[]>([]);

  const { vehicles: storeVehicles } = useAppSelector((state) => state.vehicles);
  const vehicles = propVehicles || storeVehicles;

  useEffect(() => {
    if (!mapContainer.current) return;

    map.current = new mapboxgl.Map({
      container: mapContainer.current,
      style: 'mapbox://styles/mapbox/streets-v12',
      center: [-98.5795, 39.8283], // Center of US
      zoom: 4,
    });

    map.current.addControl(new mapboxgl.NavigationControl(), 'top-right');

    return () => {
      map.current?.remove();
    };
  }, []);

  useEffect(() => {
    if (!map.current || !vehicles.length) return;

    // Clear existing markers
    markers.current.forEach((marker) => marker.remove());
    markers.current = [];

    // Add markers for each vehicle
    const bounds = new mapboxgl.LngLatBounds();

    vehicles.forEach((vehicle) => {
      if (!vehicle.location.longitude || !vehicle.location.latitude) return;

      const el = document.createElement('div');
      el.className = 'vehicle-marker';
      el.style.width = '30px';
      el.style.height = '30px';
      el.style.borderRadius = '50%';
      el.style.backgroundColor = getVehicleColor(vehicle);
      el.style.border = selectedVehicleId === vehicle.id ? '3px solid #1976d2' : '2px solid white';
      el.style.boxShadow = '0 2px 4px rgba(0,0,0,0.3)';
      el.style.cursor = 'pointer';

      const marker = new mapboxgl.Marker(el)
        .setLngLat([vehicle.location.longitude, vehicle.location.latitude])
        .setPopup(
          new mapboxgl.Popup({ offset: 25 })
            .setHTML(`
              <div style="padding: 8px;">
                <h4 style="margin: 0 0 8px 0;">${vehicle.make} ${vehicle.model}</h4>
                <p style="margin: 4px 0;"><strong>Status:</strong> ${vehicle.status}</p>
                <p style="margin: 4px 0;"><strong>Battery:</strong> ${vehicle.battery.stateOfCharge.toFixed(1)}%</p>
                <p style="margin: 4px 0;"><strong>Range:</strong> ${vehicle.battery.range} km</p>
                ${vehicle.assignedDriverName ? `<p style="margin: 4px 0;"><strong>Driver:</strong> ${vehicle.assignedDriverName}</p>` : ''}
              </div>
            `)
        )
        .addTo(map.current!);

      markers.current.push(marker);
      bounds.extend([vehicle.location.longitude, vehicle.location.latitude]);
    });

    // Fit map to markers
    if (vehicles.length > 0) {
      map.current.fitBounds(bounds, { padding: 50, maxZoom: 15 });
    }
  }, [vehicles, selectedVehicleId]);

  const getVehicleColor = (vehicle: Vehicle): string => {
    switch (vehicle.status) {
      case 'ACTIVE':
        return '#4caf50';
      case 'CHARGING':
        return '#2196f3';
      case 'INACTIVE':
        return '#ff9800';
      case 'MAINTENANCE':
        return '#f44336';
      case 'IN_TRIP':
        return '#2196f3';
      default:
        return '#9e9e9e';
    }
  };

  return (
    <Paper sx={{ overflow: 'hidden', height }}>
      <Box ref={mapContainer} sx={{ width: '100%', height: '100%' }} />
    </Paper>
  );
};

export default VehicleMap;

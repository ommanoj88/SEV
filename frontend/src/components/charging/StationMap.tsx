import React, { useEffect, useRef } from 'react';
import mapboxgl from 'mapbox-gl';
import { Box } from '@mui/material';
import { ChargingStation, FuelStation, isChargingStation, isFuelStation } from '../../types';

mapboxgl.accessToken = process.env.REACT_APP_MAPBOX_TOKEN || '';

interface StationMapProps {
  stations: (ChargingStation | FuelStation)[];
  center?: [number, number]; // [longitude, latitude]
  zoom?: number;
  onStationClick?: (station: ChargingStation | FuelStation) => void;
}

const StationMap: React.FC<StationMapProps> = ({
  stations,
  center = [-98.5795, 39.8283],
  zoom = 10,
  onStationClick,
}) => {
  const mapContainer = useRef<HTMLDivElement>(null);
  const map = useRef<mapboxgl.Map | null>(null);
  const markers = useRef<mapboxgl.Marker[]>([]);

  // Initialize map
  useEffect(() => {
    if (!mapContainer.current || map.current) return;

    map.current = new mapboxgl.Map({
      container: mapContainer.current,
      style: 'mapbox://styles/mapbox/streets-v12',
      center,
      zoom,
    });

    // Add navigation controls
    map.current.addControl(new mapboxgl.NavigationControl(), 'top-right');

    // Add geolocate control
    map.current.addControl(
      new mapboxgl.GeolocateControl({
        positionOptions: {
          enableHighAccuracy: true,
        },
        trackUserLocation: true,
        showUserHeading: true,
      }),
      'top-right'
    );

    return () => {
      map.current?.remove();
    };
  }, [center, zoom]);

  // Update markers when stations change
  useEffect(() => {
    if (!map.current) return;

    // Clear existing markers
    markers.current.forEach((marker) => marker.remove());
    markers.current = [];

    // Add new markers
    stations.forEach((station) => {
      const isCharging = isChargingStation(station);
      const isFuel = isFuelStation(station);

      // Create custom marker element
      const el = document.createElement('div');
      el.style.cursor = 'pointer';

      if (isCharging) {
        // Electric charging station marker (blue)
        el.innerHTML = `
          <svg width="30" height="30">
            <circle cx="15" cy="15" r="12" fill="#2196f3" stroke="white" stroke-width="2"/>
            <text x="15" y="20" text-anchor="middle" fill="white" font-size="16" font-weight="bold">⚡</text>
          </svg>
        `;
      } else if (isFuel) {
        // Fuel station marker (orange)
        el.innerHTML = `
          <svg width="30" height="30">
            <circle cx="15" cy="15" r="12" fill="#ff9800" stroke="white" stroke-width="2"/>
            <text x="15" y="20" text-anchor="middle" fill="white" font-size="16" font-weight="bold">⛽</text>
          </svg>
        `;
      }

      // Create popup content
      const popupContent = document.createElement('div');
      popupContent.innerHTML = `
        <div style="padding: 8px;">
          <h4 style="margin: 0 0 8px 0;">${station.name}</h4>
          ${
            isCharging
              ? `<p style="margin: 4px 0;"><strong>${station.availablePorts}/${station.totalPorts}</strong> ports available</p>
                 <p style="margin: 4px 0;">₹${station.costPerKwh.toFixed(2)}/kWh</p>
                 <p style="margin: 4px 0;">${station.type}</p>`
              : ''
          }
          ${
            isFuel
              ? `<p style="margin: 4px 0;"><strong>${station.availablePumps}/${station.totalPumps}</strong> pumps available</p>
                 <p style="margin: 4px 0;">${station.fuelTypes.join(', ')}</p>
                 ${
                   Object.entries(station.pricePerLiter)
                     .filter(([_, price]) => price !== undefined)
                     .map(([type, price]) => `<p style="margin: 4px 0;">${type}: ₹${price?.toFixed(2)}/L</p>`)
                     .join('')
                 }`
              : ''
          }
          ${station.distance ? `<p style="margin: 4px 0; color: #666;">${station.distance.toFixed(1)} km away</p>` : ''}
        </div>
      `;

      // Add click handler to popup
      if (onStationClick) {
        el.addEventListener('click', () => {
          onStationClick(station);
        });
      }

      // Create and add marker
      const marker = new mapboxgl.Marker(el)
        .setLngLat([station.location.longitude, station.location.latitude])
        .setPopup(new mapboxgl.Popup({ offset: 25 }).setDOMContent(popupContent))
        .addTo(map.current!);

      markers.current.push(marker);
    });

    // Fit bounds to show all markers if there are any
    if (stations.length > 0) {
      const bounds = new mapboxgl.LngLatBounds();
      stations.forEach((station) => {
        bounds.extend([station.location.longitude, station.location.latitude]);
      });

      // Only fit bounds if we have multiple stations, otherwise it might zoom in too much
      if (stations.length > 1) {
        map.current.fitBounds(bounds, {
          padding: { top: 50, bottom: 50, left: 50, right: 50 },
          maxZoom: 15,
        });
      } else {
        // For single station, just center on it
        map.current.setCenter([stations[0].location.longitude, stations[0].location.latitude]);
        map.current.setZoom(14);
      }
    }
  }, [stations, onStationClick]);

  return (
    <Box
      ref={mapContainer}
      sx={{
        height: '100%',
        width: '100%',
        borderRadius: 1,
        overflow: 'hidden',
        '& .mapboxgl-popup-content': {
          padding: 0,
          borderRadius: 2,
        },
        '& .mapboxgl-popup-close-button': {
          fontSize: '20px',
          padding: '4px 8px',
        },
      }}
    />
  );
};

export default StationMap;

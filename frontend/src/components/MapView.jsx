import React, { useEffect, useRef } from 'react';
import L from 'leaflet';

// Fix Leaflet standard marker icons in React
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// Custom Car SVG Icon
const createCarIcon = (color = '#16a34a') => {
  return L.divIcon({
    className: 'custom-car-marker',
    html: `
      <div style="background-color: ${color}; width: 34px; height: 34px; border-radius: 50%; display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.3); border: 2px solid white;">
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.5 2.8C2.1 11.1 2 11.5 2 12v4c0 .6.4 1 1 1h2"/>
          <circle cx="7" cy="17" r="2"/>
          <path d="M9 17h6"/>
          <circle cx="17" cy="17" r="2"/>
        </svg>
      </div>
    `,
    iconSize: [34, 34],
    iconAnchor: [17, 17],
  });
};

const pickupIcon = L.divIcon({
  className: 'custom-pickup-marker',
  html: `
    <div style="background-color: #059669; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 3px solid white; box-shadow: 0 4px 6px rgba(0,0,0,0.3);">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="white"><circle cx="12" cy="12" r="6"/></svg>
    </div>
  `,
  iconSize: [32, 32],
  iconAnchor: [16, 16],
});

const dropoffIcon = L.divIcon({
  className: 'custom-dropoff-marker',
  html: `
    <div style="background-color: #dc2626; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 3px solid white; box-shadow: 0 4px 6px rgba(0,0,0,0.3);">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="white"><rect x="6" y="6" width="12" height="12" rx="2"/></svg>
    </div>
  `,
  iconSize: [32, 32],
  iconAnchor: [16, 16],
});

const MapView = ({
  pickup,
  dropoff,
  nearbyDrivers = [],
  activeDriverLocation = null,
  onMapClick,
  className = "h-[450px] w-full",
}) => {
  const mapContainerRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const markersRef = useRef({
    pickup: null,
    dropoff: null,
    route: null,
    driverMarkers: [],
    activeDriver: null,
  });

  // Initialize Map
  useEffect(() => {
    if (!mapContainerRef.current) return;
    if (mapInstanceRef.current) return;

    const initialCenter = pickup ? [pickup.lat, pickup.lng] : [37.7749, -122.4194];

    const map = L.map(mapContainerRef.current, {
      center: initialCenter,
      zoom: 13,
      zoomControl: true,
    });

    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
      attribution: '&copy; <a href="https://carto.com/">CARTO</a> &copy; <a href="https://openstreetmap.org">OSM</a>',
      maxZoom: 19,
    }).addTo(map);

    map.on('click', (e) => {
      if (onMapClick) {
        onMapClick({ lat: e.latlng.lat, lng: e.latlng.lng });
      }
    });

    mapInstanceRef.current = map;

    return () => {
      map.remove();
      mapInstanceRef.current = null;
    };
  }, []);

  // Update Pickup & Dropoff Markers & Route
  useEffect(() => {
    const map = mapInstanceRef.current;
    if (!map) return;

    // Pickup Marker
    if (pickup) {
      if (markersRef.current.pickup) {
        markersRef.current.pickup.setLatLng([pickup.lat, pickup.lng]);
      } else {
        markersRef.current.pickup = L.marker([pickup.lat, pickup.lng], { icon: pickupIcon })
          .addTo(map)
          .bindPopup('<b>Pickup Location</b><br/>' + (pickup.address || ''));
      }
    } else if (markersRef.current.pickup) {
      map.removeLayer(markersRef.current.pickup);
      markersRef.current.pickup = null;
    }

    // Dropoff Marker
    if (dropoff) {
      if (markersRef.current.dropoff) {
        markersRef.current.dropoff.setLatLng([dropoff.lat, dropoff.lng]);
      } else {
        markersRef.current.dropoff = L.marker([dropoff.lat, dropoff.lng], { icon: dropoffIcon })
          .addTo(map)
          .bindPopup('<b>Dropoff Location</b><br/>' + (dropoff.address || ''));
      }
    } else if (markersRef.current.dropoff) {
      map.removeLayer(markersRef.current.dropoff);
      markersRef.current.dropoff = null;
    }

    // Route Polyline
    if (pickup && dropoff) {
      const latlngs = [
        [pickup.lat, pickup.lng],
        [dropoff.lat, dropoff.lng],
      ];

      if (markersRef.current.route) {
        markersRef.current.route.setLatLngs(latlngs);
      } else {
        markersRef.current.route = L.polyline(latlngs, {
          color: '#16a34a',
          weight: 4,
          opacity: 0.85,
          dashArray: '8, 8',
        }).addTo(map);
      }

      // Fit bounds to show both points
      const bounds = L.latLngBounds([pickup.lat, pickup.lng], [dropoff.lat, dropoff.lng]);
      map.fitBounds(bounds, { padding: [50, 50] });
    } else if (markersRef.current.route) {
      map.removeLayer(markersRef.current.route);
      markersRef.current.route = null;
    }
  }, [pickup, dropoff]);

  // Update Nearby Drivers
  useEffect(() => {
    const map = mapInstanceRef.current;
    if (!map) return;

    // Remove old nearby driver markers
    markersRef.current.driverMarkers.forEach((m) => map.removeLayer(m));
    markersRef.current.driverMarkers = [];

    // Add new ones
    nearbyDrivers.forEach((driver) => {
      if (driver.currentLat && driver.currentLng) {
        const marker = L.marker([driver.currentLat, driver.currentLng], {
          icon: createCarIcon('#2563eb'),
        }).addTo(map);

        marker.bindPopup(`
          <div style="font-family: sans-serif; font-size: 12px;">
            <b>${driver.name}</b><br/>
            <span>${driver.vehicleModel} (${driver.vehicleType})</span><br/>
            <span>⭐ ${driver.rating} • ${driver.vehicleNumber}</span>
          </div>
        `);
        markersRef.current.driverMarkers.push(marker);
      }
    });
  }, [nearbyDrivers]);

  // Update Active Assigned Driver Location
  useEffect(() => {
    const map = mapInstanceRef.current;
    if (!map) return;

    if (activeDriverLocation && activeDriverLocation.lat && activeDriverLocation.lng) {
      if (markersRef.current.activeDriver) {
        markersRef.current.activeDriver.setLatLng([
          activeDriverLocation.lat,
          activeDriverLocation.lng,
        ]);
      } else {
        markersRef.current.activeDriver = L.marker(
          [activeDriverLocation.lat, activeDriverLocation.lng],
          { icon: createCarIcon('#ea580c') }
        ).addTo(map);
      }
      markersRef.current.activeDriver.bindPopup('<b>Your Driver</b><br/>Arriving soon...');
    } else if (markersRef.current.activeDriver) {
      map.removeLayer(markersRef.current.activeDriver);
      markersRef.current.activeDriver = null;
    }
  }, [activeDriverLocation]);

  return (
    <div className="relative rounded-2xl overflow-hidden shadow-md border border-slate-200">
      <div ref={mapContainerRef} className={className} />
      <div className="absolute top-3 right-3 z-[400] bg-white/90 backdrop-blur-md px-3 py-1.5 rounded-lg border border-slate-200 shadow-sm text-xs font-medium text-slate-700 flex items-center gap-2">
        <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 inline-block animate-pulse"></span>
        <span>Interactive GPS Map</span>
      </div>
    </div>
  );
};

export default MapView;

import React, { useState, useEffect } from 'react';
import { useAuth } from './context/AuthContext';
import { useWebSocket } from './context/WebSocketContext';
import { rideApi, driverApi } from './services/api';
import Navbar from './components/Navbar';
import MapView from './components/MapView';
import RideRequestWidget from './components/RideRequestWidget';
import ActiveRideTracker from './components/ActiveRideTracker';
import RideHistory from './components/RideHistory';
import DriverDashboard from './components/DriverDashboard';
import AdminDashboard from './components/AdminDashboard';
import RatingModal from './components/RatingModal';

function App() {
  const { user, isRider, isDriver, isAdmin } = useAuth();
  const { subscribe } = useWebSocket();

  const [activeTab, setActiveTab] = useState('rider');
  const [pickup, setPickup] = useState({
    address: 'Market Street & 4th, San Francisco, CA',
    lat: 37.7858,
    lng: -122.4065,
  });
  const [dropoff, setDropoff] = useState({
    address: "Fisherman's Wharf, San Francisco, CA",
    lat: 37.8080,
    lng: -122.4177,
  });

  const [nearbyDrivers, setNearbyDrivers] = useState([]);
  const [activeRide, setActiveRide] = useState(null);
  const [riderHistory, setRiderHistory] = useState([]);
  const [ratingRide, setRatingRide] = useState(null);

  // Sync tab with role on change
  useEffect(() => {
    if (isDriver) setActiveTab('driver');
    else if (isAdmin) setActiveTab('admin');
    else setActiveTab('rider');
  }, [user?.role]);

  // Load nearby drivers & active ride
  useEffect(() => {
    loadNearbyDrivers();
    if (user) {
      loadActiveRide();
      loadRiderHistory();
    }
  }, [user?.id]);

  // Subscribe to WebSocket updates for active ride and nearby drivers
  useEffect(() => {
    // 1. Nearby driver location stream
    const unsubNearby = subscribe('/topic/drivers/nearby', (driverUpdate) => {
      setNearbyDrivers((prev) => {
        const idx = prev.findIndex((d) => d.id === driverUpdate.id);
        if (idx !== -1) {
          const updated = [...prev];
          updated[idx] = driverUpdate;
          return updated;
        }
        return [driverUpdate, ...prev];
      });
    });

    // 2. Rider active ride updates
    let unsubRiderRides = null;
    if (user?.id) {
      unsubRiderRides = subscribe(`/topic/rider/${user.id}/rides`, (ride) => {
        setActiveRide(ride);
        loadRiderHistory();
      });
    }

    return () => {
      if (unsubNearby) unsubNearby();
      if (unsubRiderRides) unsubRiderRides();
    };
  }, [user?.id]);

  const loadNearbyDrivers = async () => {
    try {
      const res = await driverApi.getNearby(37.7749, -122.4194, 25.0);
      if (res.data.success) {
        setNearbyDrivers(res.data.data);
      }
    } catch (e) {
      console.warn('Could not load nearby drivers', e);
    }
  };

  const loadActiveRide = async () => {
    try {
      const res = await rideApi.getActiveRide();
      if (res.data.success && res.data.data) {
        setActiveRide(res.data.data);
      }
    } catch (e) {
      console.warn('No active ride', e);
    }
  };

  const loadRiderHistory = async () => {
    try {
      const res = await rideApi.getMyRides();
      if (res.data.success) {
        setRiderHistory(res.data.data);
      }
    } catch (e) {
      console.warn('Could not load history', e);
    }
  };

  // Map click handler to select locations
  const handleMapClick = (coords) => {
    if (!pickup) {
      setPickup({
        address: `Custom Location (${coords.lat.toFixed(4)}, ${coords.lng.toFixed(4)})`,
        lat: coords.lat,
        lng: coords.lng,
      });
    } else {
      setDropoff({
        address: `Destination (${coords.lat.toFixed(4)}, ${coords.lng.toFixed(4)})`,
        lat: coords.lat,
        lng: coords.lng,
      });
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-50">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />

      <main className="flex-1 max-w-7xl w-full mx-auto p-4 sm:p-6 lg:p-8">
        
        {/* RIDER VIEW */}
        {activeTab === 'rider' && (
          <div className="space-y-8">
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
              
              {/* Left Column: Booking Widget or Active Ride Tracker */}
              <div className="lg:col-span-5 flex flex-col gap-6">
                {activeRide && activeRide.status !== 'CANCELLED' ? (
                  <ActiveRideTracker
                    ride={activeRide}
                    onRideUpdated={(updated) => {
                      setActiveRide(updated);
                      loadRiderHistory();
                    }}
                    onOpenRatingModal={(r) => setRatingRide(r)}
                  />
                ) : (
                  <RideRequestWidget
                    pickup={pickup}
                    setPickup={setPickup}
                    dropoff={dropoff}
                    setDropoff={setDropoff}
                    onRideRequested={(ride) => {
                      setActiveRide(ride);
                      loadRiderHistory();
                    }}
                  />
                )}
              </div>

              {/* Right Column: Interactive GPS Map */}
              <div className="lg:col-span-7 flex flex-col gap-2">
                <MapView
                  pickup={pickup}
                  dropoff={dropoff}
                  nearbyDrivers={nearbyDrivers}
                  activeDriverLocation={
                    activeRide?.driverLat && activeRide?.driverLng
                      ? { lat: activeRide.driverLat, lng: activeRide.driverLng }
                      : null
                  }
                  onMapClick={handleMapClick}
                  className="h-[520px] w-full"
                />
                <div className="text-[11px] text-slate-500 flex items-center justify-between px-1">
                  <span>💡 Tip: Click anywhere on the map to set pickup or drop-off pin</span>
                  <span>{nearbyDrivers.length} active drivers online</span>
                </div>
              </div>
            </div>

            {/* Rider Trip History & Receipts */}
            <RideHistory rides={riderHistory} />
          </div>
        )}

        {/* DRIVER PORTAL */}
        {activeTab === 'driver' && (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
            <div className="lg:col-span-7">
              <DriverDashboard user={user} />
            </div>
            <div className="lg:col-span-5">
              <div className="sticky top-20 space-y-2">
                <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500">Live Navigation Map</h3>
                <MapView
                  pickup={pickup}
                  dropoff={dropoff}
                  nearbyDrivers={nearbyDrivers}
                  className="h-[480px] w-full"
                />
              </div>
            </div>
          </div>
        )}

        {/* ADMIN OPERATIONS */}
        {activeTab === 'admin' && (
          <AdminDashboard />
        )}

      </main>

      {/* Rating Modal */}
      {ratingRide && (
        <RatingModal
          ride={ratingRide}
          onClose={() => setRatingRide(null)}
          onSuccess={() => {
            loadRiderHistory();
            loadActiveRide();
          }}
        />
      )}

      {/* Footer */}
      <footer className="border-t border-slate-200 bg-white py-6 mt-12 text-center text-xs text-slate-500">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
          <span>Enterprise Cab Booking Application • Java 17 + Spring Boot 3 + PostgreSQL + WebSockets + Stripe + React</span>
          <span className="font-semibold text-emerald-600">Production-Ready Architecture</span>
        </div>
      </footer>
    </div>
  );
}

export default App;

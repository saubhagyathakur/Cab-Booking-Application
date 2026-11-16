import React, { useState, useEffect, useRef } from 'react';
import { driverApi, receiptApi } from '../services/api';
import { useWebSocket } from '../context/WebSocketContext';
import { 
  Car, Power, DollarSign, Award, CheckCircle2, 
  MapPin, Clock, Navigation, AlertCircle, Play, Check 
} from 'lucide-react';

const DriverDashboard = ({ user }) => {
  const { subscribe, sendDriverLocation } = useWebSocket();
  const [profile, setProfile] = useState(null);
  const [activeRide, setActiveRide] = useState(null);
  const [incomingRequests, setIncomingRequests] = useState([]);
  const [myRides, setMyRides] = useState([]);
  const [otpInput, setOtpInput] = useState('');
  const [otpError, setOtpError] = useState('');
  const [simulating, setSimulating] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const simIntervalRef = useRef(null);

  // Load driver data
  useEffect(() => {
    loadDriverData();
  }, []);

  const loadDriverData = async () => {
    try {
      setLoading(true);
      const [profRes, activeRes, ridesRes, reqRes] = await Promise.all([
        driverApi.getProfile(),
        driverApi.getActiveRide(),
        driverApi.getMyRides(),
        driverApi.getAvailableRequests(),
      ]);

      if (profRes.data.success) setProfile(profRes.data.data);
      if (activeRes.data.success) setActiveRide(activeRes.data.data);
      if (ridesRes.data.success) setMyRides(ridesRes.data.data);
      if (reqRes.data.success) setIncomingRequests(reqRes.data.data);
    } catch (err) {
      console.error(err);
      setError('Could not load driver dashboard');
    } finally {
      setLoading(false);
    }
  };

  // Subscribe to driver-specific ride request notifications & active ride updates via STOMP
  useEffect(() => {
    if (!profile?.id) return;

    // 1. New incoming requests
    const unsubRequests = subscribe(`/topic/driver/${profile.id}/requests`, (newRide) => {
      setIncomingRequests((prev) => {
        if (prev.some((r) => r.id === newRide.id)) return prev;
        return [newRide, ...prev];
      });
    });

    // 2. Active ride updates
    const unsubRideUpdates = subscribe(`/topic/driver/${profile.id}/rides`, (updatedRide) => {
      setActiveRide((prev) => {
        if (!prev || prev.id === updatedRide.id) {
          if (updatedRide.status === 'COMPLETED' || updatedRide.status === 'CANCELLED') {
            loadDriverData(); // refresh stats
            return null;
          }
          return updatedRide;
        }
        return prev;
      });
    });

    return () => {
      if (unsubRequests) unsubRequests();
      if (unsubRideUpdates) unsubRideUpdates();
      if (simIntervalRef.current) clearInterval(simIntervalRef.current);
    };
  }, [profile?.id]);

  // Toggle online/offline
  const toggleStatus = async () => {
    if (!profile) return;
    const newStatus = profile.status === 'AVAILABLE' ? 'OFFLINE' : 'AVAILABLE';
    try {
      const res = await driverApi.updateStatus(newStatus);
      if (res.data.success) {
        setProfile(res.data.data);
      }
    } catch (err) {
      setError('Failed to update driver status');
    }
  };

  // Accept incoming ride
  const handleAcceptRide = async (rideId) => {
    try {
      const res = await driverApi.acceptRide(rideId);
      if (res.data.success) {
        setActiveRide(res.data.data);
        setIncomingRequests((prev) => prev.filter((r) => r.id !== rideId));
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Could not accept ride');
    }
  };

  // Driver arrived at pickup
  const handleMarkArrived = async () => {
    if (!activeRide) return;
    try {
      const res = await driverApi.arrived(activeRide.id);
      if (res.data.success) {
        setActiveRide(res.data.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Error updating status');
    }
  };

  // Start ride with OTP verification
  const handleStartRide = async () => {
    if (!activeRide || !otpInput.trim()) return;
    setOtpError('');
    try {
      const res = await driverApi.startRide(activeRide.id, otpInput.trim());
      if (res.data.success) {
        setActiveRide(res.data.data);
        setOtpInput('');
      }
    } catch (err) {
      setOtpError(err.response?.data?.message || 'Invalid OTP');
    }
  };

  // Complete ride
  const handleCompleteRide = async () => {
    if (!activeRide) return;
    try {
      const res = await driverApi.completeRide(activeRide.id);
      if (res.data.success) {
        setActiveRide(null);
        if (simIntervalRef.current) clearInterval(simIntervalRef.current);
        setSimulating(false);
        loadDriverData();
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Error completing ride');
    }
  };

  // Smooth GPS Simulation along route
  const toggleSimulation = () => {
    if (simulating) {
      clearInterval(simIntervalRef.current);
      setSimulating(false);
      return;
    }

    if (!activeRide || !profile) return;
    setSimulating(true);

    const targetLat = activeRide.status === 'ACCEPTED' ? activeRide.pickupLat : activeRide.dropLat;
    const targetLng = activeRide.status === 'ACCEPTED' ? activeRide.pickupLng : activeRide.dropLng;

    let currentLat = profile.currentLat || activeRide.pickupLat;
    let currentLng = profile.currentLng || activeRide.pickupLng;

    simIntervalRef.current = setInterval(() => {
      const dLat = (targetLat - currentLat) * 0.15;
      const dLng = (targetLng - currentLng) * 0.15;

      currentLat += dLat;
      currentLng += dLng;

      // Broadcast new coords over WebSocket
      sendDriverLocation(profile.id, currentLat, currentLng);
      driverApi.updateLocation(currentLat, currentLng);

      if (Math.abs(targetLat - currentLat) < 0.0005 && Math.abs(targetLng - currentLng) < 0.0005) {
        clearInterval(simIntervalRef.current);
        setSimulating(false);
      }
    }, 1500);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center p-12 text-slate-500">
        <div className="w-8 h-8 border-2 border-emerald-500 border-t-transparent rounded-full animate-spin mr-3" />
        <span>Loading Driver Portal...</span>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      
      {/* Top Banner & Status Toggle */}
      <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <div className="w-14 h-14 rounded-2xl bg-slate-900 text-white flex items-center justify-center font-bold text-xl shadow-md">
            {profile?.name?.charAt(0) || 'D'}
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold text-slate-900">{profile?.name}</h1>
              <span className={`text-xs px-2.5 py-0.5 rounded-full font-bold border ${
                profile?.status === 'AVAILABLE' 
                  ? 'bg-emerald-100 text-emerald-800 border-emerald-200' 
                  : 'bg-slate-100 text-slate-700 border-slate-200'
              }`}>
                {profile?.status}
              </span>
            </div>
            <p className="text-xs text-slate-500">
              {profile?.vehicleModel} • {profile?.vehicleNumber} • {profile?.vehicleType}
            </p>
          </div>
        </div>

        <button
          onClick={toggleStatus}
          className={`px-5 py-3 rounded-xl font-bold text-xs flex items-center gap-2 transition-all shadow-sm ${
            profile?.status === 'AVAILABLE'
              ? 'bg-red-50 text-red-700 hover:bg-red-100 border border-red-200'
              : 'bg-emerald-600 text-white hover:bg-emerald-700'
          }`}
        >
          <Power className="w-4 h-4" />
          <span>{profile?.status === 'AVAILABLE' ? 'Go Offline' : 'Go Online (Receive Rides)'}</span>
        </button>
      </div>

      {error && (
        <div className="p-3 bg-red-50 border border-red-200 text-xs text-red-700 rounded-xl">
          {error}
        </div>
      )}

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Total Earnings</span>
            <DollarSign className="w-5 h-5 text-emerald-600" />
          </div>
          <div className="text-2xl font-black text-slate-900">
            ${profile?.totalEarnings || '0.00'}
          </div>
          <div className="text-[11px] text-slate-400 mt-1">Direct Stripe payouts</div>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Completed Trips</span>
            <CheckCircle2 className="w-5 h-5 text-blue-600" />
          </div>
          <div className="text-2xl font-black text-slate-900">
            {profile?.totalRides || 0}
          </div>
          <div className="text-[11px] text-slate-400 mt-1">Lifetime rides</div>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Rating Score</span>
            <Award className="w-5 h-5 text-amber-500" />
          </div>
          <div className="text-2xl font-black text-slate-900 flex items-center gap-1">
            <span>{profile?.rating || '5.0'}</span>
            <span className="text-amber-500 text-lg">★</span>
          </div>
          <div className="text-[11px] text-slate-400 mt-1">Based on rider reviews</div>
        </div>
      </div>

      {/* Active Trip Controller (if in progress) */}
      {activeRide && (
        <div className="bg-white rounded-2xl border-2 border-emerald-500 p-5 shadow-md space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-ping" />
              <h2 className="text-base font-bold text-slate-900">Active Trip — Ride #{activeRide.id}</h2>
            </div>
            <span className="text-xs px-2.5 py-1 rounded-full bg-emerald-100 text-emerald-800 font-bold">
              {activeRide.status}
            </span>
          </div>

          {/* Passenger details */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            <div className="p-3 bg-slate-50 rounded-xl space-y-1">
              <span className="text-[10px] uppercase font-bold text-slate-400">Rider</span>
              <div className="font-bold text-slate-900">{activeRide.riderName}</div>
              <div className="text-slate-500">{activeRide.riderPhone || 'No phone'}</div>
            </div>
            <div className="p-3 bg-slate-50 rounded-xl space-y-1">
              <span className="text-[10px] uppercase font-bold text-slate-400">Estimated Fare</span>
              <div className="font-bold text-slate-900 text-base text-emerald-700">${activeRide.finalFare}</div>
              <div className="text-slate-500">{activeRide.distanceKm} km • ~{activeRide.durationMinutes} mins</div>
            </div>
          </div>

          {/* Locations */}
          <div className="text-xs space-y-2">
            <div className="flex items-start gap-2 text-slate-700">
              <MapPin className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
              <span><b>Pickup:</b> {activeRide.pickupAddress}</span>
            </div>
            <div className="flex items-start gap-2 text-slate-700">
              <Navigation className="w-4 h-4 text-red-600 shrink-0 mt-0.5" />
              <span><b>Drop-off:</b> {activeRide.dropAddress}</span>
            </div>
          </div>

          {/* Trip Actions Lifecycle */}
          <div className="pt-2 border-t border-slate-100 flex flex-wrap gap-2">
            
            {activeRide.status === 'ACCEPTED' && (
              <button
                onClick={handleMarkArrived}
                className="flex-1 py-2.5 px-4 bg-purple-600 hover:bg-purple-700 text-white font-bold text-xs rounded-xl shadow-sm transition-all"
              >
                I Have Arrived at Pickup
              </button>
            )}

            {activeRide.status === 'ARRIVED' && (
              <div className="w-full flex gap-2">
                <input
                  type="text"
                  placeholder="Enter Rider 4-digit PIN OTP"
                  value={otpInput}
                  onChange={(e) => setOtpInput(e.target.value)}
                  maxLength={4}
                  className="flex-1 px-3 py-2 text-sm font-mono font-bold tracking-widest text-center border border-slate-300 rounded-xl focus:outline-none focus:border-emerald-500"
                />
                <button
                  onClick={handleStartRide}
                  className="py-2.5 px-5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl shadow-sm transition-all"
                >
                  Start Trip
                </button>
              </div>
            )}
            {otpError && <div className="text-xs text-red-600">{otpError}</div>}

            {activeRide.status === 'IN_PROGRESS' && (
              <button
                onClick={handleCompleteRide}
                className="flex-1 py-3 px-4 bg-slate-900 hover:bg-black text-white font-bold text-xs rounded-xl shadow-md transition-all flex items-center justify-center gap-2"
              >
                <Check className="w-4 h-4" />
                <span>Complete Trip (Collect ${activeRide.finalFare})</span>
              </button>
            )}

            {/* GPS Simulator Button */}
            <button
              onClick={toggleSimulation}
              className={`py-2 px-3 text-xs font-semibold rounded-xl border flex items-center gap-1.5 transition-colors ${
                simulating
                  ? 'bg-amber-50 text-amber-700 border-amber-300'
                  : 'bg-slate-100 text-slate-700 border-slate-200 hover:bg-slate-200'
              }`}
            >
              <Play className={`w-3.5 h-3.5 ${simulating ? 'animate-spin' : ''}`} />
              <span>{simulating ? 'Simulating Driving...' : 'Simulate GPS Driving'}</span>
            </button>

          </div>
        </div>
      )}

      {/* Incoming Ride Requests Queue */}
      <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-base font-bold text-slate-900">Nearby Ride Requests</h2>
          <span className="text-xs font-bold bg-emerald-100 text-emerald-800 px-2 py-0.5 rounded-full">
            {incomingRequests.length} Available
          </span>
        </div>

        {incomingRequests.length === 0 ? (
          <div className="text-center py-8 text-xs text-slate-400">
            No incoming ride requests at this moment. When riders request a ride, they will appear here in real-time.
          </div>
        ) : (
          <div className="space-y-3">
            {incomingRequests.map((req) => (
              <div
                key={req.id}
                className="p-4 rounded-xl border border-slate-200 hover:border-emerald-300 bg-slate-50/50 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 transition-all"
              >
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-bold text-slate-900">{req.riderName}</span>
                    <span className="text-[10px] bg-slate-200 text-slate-700 px-1.5 py-0.5 rounded font-semibold">
                      {req.vehicleType}
                    </span>
                  </div>
                  <div className="text-xs text-slate-600">
                    <span className="font-semibold">From:</span> {req.pickupAddress}
                  </div>
                  <div className="text-xs text-slate-600">
                    <span className="font-semibold">To:</span> {req.dropAddress}
                  </div>
                  <div className="text-[11px] text-slate-400">
                    {req.distanceKm} km • ~{req.durationMinutes} mins
                  </div>
                </div>

                <div className="flex items-center gap-3 w-full sm:w-auto justify-between sm:justify-end">
                  <div className="text-right">
                    <div className="text-sm font-black text-emerald-700">${req.finalFare}</div>
                    <div className="text-[10px] text-slate-400">Fare payout</div>
                  </div>
                  <button
                    onClick={() => handleAcceptRide(req.id)}
                    className="py-2 px-4 rounded-xl font-bold text-xs bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm transition-all"
                  >
                    Accept Ride
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Driver Ride History */}
      <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm space-y-4">
        <h2 className="text-base font-bold text-slate-900">Your Recent Rides</h2>
        {myRides.length === 0 ? (
          <div className="text-xs text-slate-400 py-4 text-center">No past rides recorded.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left">
              <thead>
                <tr className="border-b border-slate-200 text-slate-400 uppercase font-semibold">
                  <th className="py-2">Ride #</th>
                  <th className="py-2">Rider</th>
                  <th className="py-2">Pickup / Drop</th>
                  <th className="py-2">Status</th>
                  <th className="py-2 text-right">Earnings</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {myRides.map((r) => (
                  <tr key={r.id}>
                    <td className="py-2.5 font-bold">#{r.id}</td>
                    <td className="py-2.5">{r.riderName}</td>
                    <td className="py-2.5 max-w-xs truncate text-slate-600">
                      {r.pickupAddress} &rarr; {r.dropAddress}
                    </td>
                    <td className="py-2.5 font-semibold text-emerald-600">{r.status}</td>
                    <td className="py-2.5 text-right font-bold text-slate-900">${r.finalFare}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

    </div>
  );
};

export default DriverDashboard;

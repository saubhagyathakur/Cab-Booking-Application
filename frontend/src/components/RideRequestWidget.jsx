import React, { useState, useEffect } from 'react';
import { rideApi, promoApi } from '../services/api';
import { MapPin, Navigation, Tag, Clock, Users, ArrowRight, CheckCircle2, AlertCircle } from 'lucide-react';

const PRESET_LOCATIONS = [
  { name: 'Downtown (Market St)', lat: 37.7749, lng: -122.4194 },
  { name: 'Fisherman\'s Wharf', lat: 37.8080, lng: -122.4177 },
  { name: 'Financial District', lat: 37.7946, lng: -122.3999 },
  { name: 'Mission District', lat: 37.7599, lng: -122.4148 },
  { name: 'Golden Gate Park', lat: 37.7694, lng: -122.4862 },
];

const RideRequestWidget = ({
  pickup,
  setPickup,
  dropoff,
  setDropoff,
  onRideRequested,
}) => {
  const [estimate, setEstimate] = useState(null);
  const [selectedVehicle, setSelectedVehicle] = useState('ECONOMY');
  const [promoCode, setPromoCode] = useState('');
  const [promoDiscount, setPromoDiscount] = useState(null);
  const [promoError, setPromoError] = useState('');
  const [loadingEstimate, setLoadingEstimate] = useState(false);
  const [requesting, setRequesting] = useState(false);
  const [error, setError] = useState('');

  // Fetch estimate whenever pickup or dropoff changes
  useEffect(() => {
    if (pickup && dropoff) {
      fetchEstimate();
    }
  }, [pickup, dropoff, promoDiscount]);

  const fetchEstimate = async () => {
    try {
      setLoadingEstimate(true);
      setError('');
      const res = await rideApi.getEstimate({
        pickupLat: pickup.lat,
        pickupLng: pickup.lng,
        dropLat: dropoff.lat,
        dropLng: dropoff.lng,
        promoCode: promoDiscount ? promoCode : null,
      });
      if (res.data.success) {
        setEstimate(res.data.data);
      }
    } catch (err) {
      console.error(err);
      setError('Could not calculate estimate. Please try again.');
    } finally {
      setLoadingEstimate(false);
    }
  };

  const handleApplyPromo = async () => {
    if (!promoCode.trim()) return;
    setPromoError('');
    try {
      const res = await promoApi.validate(promoCode.trim(), 25.0);
      if (res.data.success && res.data.data.valid) {
        setPromoDiscount(res.data.data);
      } else {
        setPromoError(res.data.data.message || 'Invalid promo code');
        setPromoDiscount(null);
      }
    } catch (err) {
      setPromoError('Promo code check failed');
    }
  };

  const handleBookRide = async () => {
    if (!pickup || !dropoff) {
      setError('Please select both pickup and drop-off locations');
      return;
    }

    try {
      setRequesting(true);
      setError('');
      const payload = {
        pickupAddress: pickup.address || 'Pickup Point',
        pickupLat: pickup.lat,
        pickupLng: pickup.lng,
        dropAddress: dropoff.address || 'Drop-off Point',
        dropLat: dropoff.lat,
        dropLng: dropoff.lng,
        vehicleType: selectedVehicle,
        promoCode: promoDiscount ? promoCode : null,
      };

      const res = await rideApi.requestRide(payload);
      if (res.data.success) {
        onRideRequested(res.data.data);
      } else {
        setError(res.data.message || 'Failed to request ride');
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Error booking ride';
      setError(msg);
    } finally {
      setRequesting(false);
    }
  };

  return (
    <div className="bg-white rounded-2xl border border-slate-200 shadow-md p-5 flex flex-col gap-5">
      <div>
        <h2 className="text-lg font-bold text-slate-900">Where to?</h2>
        <p className="text-xs text-slate-500">Choose your pickup & destination or click preset landmarks</p>
      </div>

      {error && (
        <div className="p-3 bg-red-50 border border-red-200 rounded-xl flex items-center gap-2 text-xs text-red-700">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Inputs */}
      <div className="space-y-3 relative">
        {/* Pickup */}
        <div className="flex items-center gap-3 bg-slate-50 border border-slate-200 rounded-xl px-3 py-2.5 focus-within:border-emerald-500 focus-within:ring-2 focus-within:ring-emerald-100 transition-all">
          <div className="w-6 h-6 rounded-full bg-emerald-100 flex items-center justify-center shrink-0">
            <MapPin className="w-3.5 h-3.5 text-emerald-600" />
          </div>
          <div className="flex-1">
            <div className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider">Pickup Location</div>
            <input
              type="text"
              placeholder="Enter pickup point"
              value={pickup?.address || ''}
              onChange={(e) => setPickup({ ...pickup, address: e.target.value })}
              className="w-full bg-transparent text-sm font-medium text-slate-800 placeholder-slate-400 focus:outline-none"
            />
          </div>
        </div>

        {/* Dropoff */}
        <div className="flex items-center gap-3 bg-slate-50 border border-slate-200 rounded-xl px-3 py-2.5 focus-within:border-emerald-500 focus-within:ring-2 focus-within:ring-emerald-100 transition-all">
          <div className="w-6 h-6 rounded-full bg-red-100 flex items-center justify-center shrink-0">
            <Navigation className="w-3.5 h-3.5 text-red-600" />
          </div>
          <div className="flex-1">
            <div className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider">Destination</div>
            <input
              type="text"
              placeholder="Where are you heading?"
              value={dropoff?.address || ''}
              onChange={(e) => setDropoff({ ...dropoff, address: e.target.value })}
              className="w-full bg-transparent text-sm font-medium text-slate-800 placeholder-slate-400 focus:outline-none"
            />
          </div>
        </div>

        {/* Quick presets */}
        <div className="flex flex-wrap gap-1.5 pt-1">
          <span className="text-[10px] font-semibold text-slate-400 self-center">Presets:</span>
          {PRESET_LOCATIONS.map((loc, i) => (
            <button
              key={i}
              type="button"
              onClick={() => {
                if (!pickup) setPickup({ address: loc.name, lat: loc.lat, lng: loc.lng });
                else setDropoff({ address: loc.name, lat: loc.lat, lng: loc.lng });
              }}
              className="text-[11px] bg-slate-100 hover:bg-slate-200 text-slate-700 px-2 py-0.5 rounded-md transition-colors"
            >
              {loc.name.split(' ')[0]}
            </button>
          ))}
        </div>
      </div>

      {/* Vehicle Options */}
      {estimate && (
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-700 uppercase tracking-wide">Available Rides</span>
            <span className="text-xs text-slate-500">{estimate.distanceKm} km • ~{estimate.durationMinutes} mins</span>
          </div>

          <div className="grid grid-cols-2 gap-2">
            {estimate.options.map((opt) => {
              const isSelected = selectedVehicle === opt.vehicleType;
              return (
                <div
                  key={opt.vehicleType}
                  onClick={() => setSelectedVehicle(opt.vehicleType)}
                  className={`p-3 rounded-xl border cursor-pointer transition-all ${
                    isSelected
                      ? 'border-emerald-500 bg-emerald-50/50 ring-2 ring-emerald-500/20'
                      : 'border-slate-200 hover:border-slate-300 bg-white'
                  }`}
                >
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-sm font-bold text-slate-800">{opt.displayName}</span>
                    <span className="text-sm font-black text-emerald-700">${opt.finalFare}</span>
                  </div>
                  {opt.discount > 0 && (
                    <div className="text-[10px] text-red-500 line-through mb-1">${opt.fare}</div>
                  )}
                  <div className="flex items-center justify-between text-[11px] text-slate-500">
                    <span className="flex items-center gap-1">
                      <Clock className="w-3 h-3 text-slate-400" />
                      {opt.etaMinutes} min away
                    </span>
                    <span className="text-[10px] bg-slate-100 text-slate-600 px-1.5 py-0.5 rounded">
                      {opt.availableDriversCount} nearby
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Promo Code */}
      <div className="border-t border-slate-100 pt-3">
        <div className="flex gap-2">
          <div className="relative flex-1">
            <Tag className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-3" />
            <input
              type="text"
              placeholder="Promo code (SAVE20, WELCOME50)"
              value={promoCode}
              onChange={(e) => setPromoCode(e.target.value.toUpperCase())}
              className="w-full text-xs pl-8 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500"
            />
          </div>
          <button
            type="button"
            onClick={handleApplyPromo}
            className="text-xs font-semibold px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl transition-colors"
          >
            Apply
          </button>
        </div>
        {promoDiscount && (
          <div className="text-[11px] text-emerald-600 font-medium mt-1 flex items-center gap-1">
            <CheckCircle2 className="w-3.5 h-3.5" />
            {promoDiscount.message} (Saved ${promoDiscount.discountApplied})
          </div>
        )}
        {promoError && (
          <div className="text-[11px] text-red-500 font-medium mt-1">{promoError}</div>
        )}
      </div>

      {/* Request Button */}
      <button
        onClick={handleBookRide}
        disabled={!pickup || !dropoff || requesting || loadingEstimate}
        className="w-full py-3.5 px-4 rounded-xl font-bold text-sm text-white bg-gradient-to-r from-emerald-600 to-green-600 hover:from-emerald-700 hover:to-green-700 disabled:opacity-50 disabled:cursor-not-allowed shadow-md shadow-emerald-600/20 transition-all flex items-center justify-center gap-2"
      >
        {requesting ? (
          <>
            <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            <span>Finding Nearby Drivers...</span>
          </>
        ) : (
          <>
            <span>Confirm & Request Ride</span>
            <ArrowRight className="w-4 h-4" />
          </>
        )}
      </button>
    </div>
  );
};

export default RideRequestWidget;

import React, { useState } from 'react';
import { rideApi, paymentApi, receiptApi } from '../services/api';
import { useWebSocket } from '../context/WebSocketContext';
import { 
  Car, ShieldAlert, CheckCircle, Clock, MapPin, 
  CreditCard, Download, Star, Phone, AlertTriangle, X 
} from 'lucide-react';

const STATUS_CONFIG = {
  REQUESTED: { label: 'Finding Nearby Driver', color: 'bg-amber-100 text-amber-800 border-amber-300', step: 1 },
  ACCEPTED: { label: 'Driver Confirmed & En Route', color: 'bg-blue-100 text-blue-800 border-blue-300', step: 2 },
  ARRIVED: { label: 'Driver Arrived at Pickup', color: 'bg-purple-100 text-purple-800 border-purple-300', step: 3 },
  IN_PROGRESS: { label: 'Trip in Progress', color: 'bg-emerald-100 text-emerald-800 border-emerald-300', step: 4 },
  COMPLETED: { label: 'Trip Completed', color: 'bg-slate-100 text-slate-800 border-slate-300', step: 5 },
  CANCELLED: { label: 'Trip Cancelled', color: 'bg-red-100 text-red-800 border-red-300', step: 0 },
};

const ActiveRideTracker = ({
  ride,
  onRideUpdated,
  onOpenRatingModal,
}) => {
  const { triggerSos } = useWebSocket();
  const [cancelling, setCancelling] = useState(false);
  const [paying, setPaying] = useState(false);
  const [sosSent, setSosSent] = useState(false);
  const [error, setError] = useState('');

  const statusInfo = STATUS_CONFIG[ride.status] || STATUS_CONFIG.REQUESTED;

  const handleCancel = async () => {
    if (!window.confirm('Are you sure you want to cancel this ride?')) return;
    try {
      setCancelling(true);
      const res = await rideApi.cancelRide(ride.id, 'Cancelled by rider');
      if (res.data.success) {
        onRideUpdated(res.data.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Could not cancel ride');
    } finally {
      setCancelling(false);
    }
  };

  const handlePayNow = async () => {
    try {
      setPaying(true);
      setError('');
      // 1. Create Payment Intent
      const intentRes = await paymentApi.createIntent(ride.id);
      const paymentIntentId = intentRes.data.data.paymentIntentId;

      // 2. Confirm Payment
      const confirmRes = await paymentApi.confirmPayment(ride.id, paymentIntentId);
      if (confirmRes.data.success) {
        // Fetch refreshed ride
        const refreshed = await rideApi.getRide(ride.id);
        onRideUpdated(refreshed.data.data);
      }
    } catch (err) {
      setError('Payment processing error');
    } finally {
      setPaying(false);
    }
  };

  const handleSos = () => {
    triggerSos(ride.id, {
      riderName: ride.riderName,
      riderPhone: ride.riderPhone,
      lat: ride.driverLat || ride.pickupLat,
      lng: ride.driverLng || ride.pickupLng,
      message: 'EMERGENCY SOS: Rider triggered distress broadcast!',
    });
    setSosSent(true);
    setTimeout(() => setSosSent(false), 5000);
  };

  return (
    <div className="bg-white rounded-2xl border border-slate-200 shadow-md p-5 flex flex-col gap-5">
      
      {/* Header status */}
      <div className="flex items-center justify-between pb-3 border-b border-slate-100">
        <div>
          <div className="text-xs font-semibold text-slate-400">RIDE #{ride.id}</div>
          <h2 className="text-base font-bold text-slate-900">{statusInfo.label}</h2>
        </div>
        <span className={`text-xs px-2.5 py-1 rounded-full font-bold border ${statusInfo.color}`}>
          {ride.status}
        </span>
      </div>

      {error && (
        <div className="p-3 bg-red-50 text-red-700 text-xs rounded-xl border border-red-200">
          {error}
        </div>
      )}

      {/* Progress Step Bar */}
      <div className="flex items-center justify-between gap-1">
        {['Requested', 'Accepted', 'Arrived', 'On Trip', 'Done'].map((stepName, idx) => {
          const stepNum = idx + 1;
          const isDone = statusInfo.step >= stepNum;
          const isCurrent = statusInfo.step === stepNum;
          return (
            <div key={idx} className="flex-1 flex flex-col items-center gap-1">
              <div
                className={`h-1.5 w-full rounded-full transition-all ${
                  isDone ? 'bg-emerald-500' : 'bg-slate-200'
                } ${isCurrent ? 'ring-2 ring-emerald-300' : ''}`}
              />
              <span className={`text-[10px] font-medium ${isDone ? 'text-emerald-700 font-bold' : 'text-slate-400'}`}>
                {stepName}
              </span>
            </div>
          );
        })}
      </div>

      {/* Security OTP Card */}
      {ride.otp && ride.status !== 'COMPLETED' && ride.status !== 'CANCELLED' && (
        <div className="bg-gradient-to-r from-amber-50 to-orange-50 border border-amber-200 rounded-xl p-3 flex items-center justify-between">
          <div>
            <div className="text-[10px] font-bold uppercase text-amber-700 tracking-wider">Ride Start PIN (OTP)</div>
            <div className="text-xs text-amber-900">Share this code with your driver before trip starts</div>
          </div>
          <div className="text-xl font-mono font-black tracking-widest text-amber-800 bg-white px-3 py-1 rounded-lg border border-amber-200 shadow-sm">
            {ride.otp}
          </div>
        </div>
      )}

      {/* Driver & Vehicle Details (if assigned) */}
      {ride.driverId ? (
        <div className="bg-slate-50 border border-slate-200 rounded-xl p-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-xl bg-emerald-600 text-white flex items-center justify-center font-bold text-lg shadow-sm">
              {ride.driverName ? ride.driverName.charAt(0) : 'D'}
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-bold text-sm text-slate-900">{ride.driverName}</span>
                <span className="flex items-center text-xs font-semibold text-amber-600 bg-amber-50 px-1.5 py-0.5 rounded">
                  <Star className="w-3 h-3 fill-amber-400 text-amber-400 mr-0.5" />
                  {ride.driverRating || '5.0'}
                </span>
              </div>
              <p className="text-xs text-slate-500">{ride.vehicleModel} • {ride.vehicleType}</p>
              <div className="text-xs font-mono font-bold text-slate-700 bg-white border border-slate-200 px-2 py-0.5 rounded mt-1 inline-block">
                {ride.vehicleNumber}
              </div>
            </div>
          </div>
          {ride.driverPhone && (
            <a
              href={`tel:${ride.driverPhone}`}
              className="p-2.5 rounded-full bg-emerald-100 text-emerald-700 hover:bg-emerald-200 transition-colors"
              title="Call Driver"
            >
              <Phone className="w-4 h-4" />
            </a>
          )}
        </div>
      ) : (
        <div className="p-4 bg-slate-50 border border-slate-200 rounded-xl text-center">
          <div className="w-8 h-8 rounded-full border-2 border-emerald-500 border-t-transparent animate-spin mx-auto mb-2" />
          <p className="text-xs font-semibold text-slate-700">Connecting you with nearby drivers...</p>
          <p className="text-[11px] text-slate-400">Drivers in your area will receive instant notifications</p>
        </div>
      )}

      {/* Route & Fare Info */}
      <div className="text-xs space-y-2 border-t border-slate-100 pt-3">
        <div className="flex items-start gap-2 text-slate-600">
          <MapPin className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
          <span className="line-clamp-1"><b>From:</b> {ride.pickupAddress}</span>
        </div>
        <div className="flex items-start gap-2 text-slate-600">
          <MapPin className="w-4 h-4 text-red-600 shrink-0 mt-0.5" />
          <span className="line-clamp-1"><b>To:</b> {ride.dropAddress}</span>
        </div>
        <div className="flex items-center justify-between pt-2 border-t border-slate-100 text-sm font-bold text-slate-800">
          <span>Final Fare</span>
          <span className="text-emerald-700 text-base font-black">${ride.finalFare}</span>
        </div>
      </div>

      {/* Completed State: Payment & Receipt & Review */}
      {ride.status === 'COMPLETED' && (
        <div className="space-y-3 bg-emerald-50/50 border border-emerald-200 rounded-xl p-4">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-emerald-800">Trip Completed Successfully!</span>
            <span className={`text-[11px] font-bold px-2 py-0.5 rounded-full ${
              ride.paymentStatus === 'PAID' ? 'bg-emerald-200 text-emerald-800' : 'bg-amber-200 text-amber-800'
            }`}>
              {ride.paymentStatus}
            </span>
          </div>

          {ride.paymentStatus !== 'PAID' ? (
            <button
              onClick={handlePayNow}
              disabled={paying}
              className="w-full py-2.5 px-4 rounded-xl font-bold text-xs text-white bg-slate-900 hover:bg-black transition-all flex items-center justify-center gap-2"
            >
              <CreditCard className="w-4 h-4" />
              <span>{paying ? 'Processing Stripe...' : `Pay $${ride.finalFare} with Stripe`}</span>
            </button>
          ) : (
            <div className="flex gap-2">
              <a
                href={receiptApi.downloadUrl(ride.id)}
                target="_blank"
                rel="noreferrer"
                className="flex-1 py-2 px-3 rounded-xl font-semibold text-xs text-slate-700 bg-white border border-slate-200 hover:bg-slate-50 transition-all flex items-center justify-center gap-1.5 shadow-sm"
              >
                <Download className="w-3.5 h-3.5" />
                <span>PDF Invoice</span>
              </a>
              <button
                onClick={() => onOpenRatingModal(ride)}
                className="flex-1 py-2 px-3 rounded-xl font-semibold text-xs text-white bg-emerald-600 hover:bg-emerald-700 transition-all flex items-center justify-center gap-1.5 shadow-sm"
              >
                <Star className="w-3.5 h-3.5" />
                <span>Rate Driver</span>
              </button>
            </div>
          )}
        </div>
      )}

      {/* Action Footer: SOS Emergency & Cancel */}
      {ride.status !== 'COMPLETED' && ride.status !== 'CANCELLED' && (
        <div className="flex gap-2 pt-2 border-t border-slate-100">
          <button
            onClick={handleSos}
            className="flex-1 py-2.5 px-3 rounded-xl font-bold text-xs text-red-700 bg-red-50 hover:bg-red-100 border border-red-200 transition-colors flex items-center justify-center gap-1.5"
          >
            <ShieldAlert className="w-4 h-4 text-red-600" />
            <span>{sosSent ? 'SOS Broadcasted!' : 'Emergency SOS'}</span>
          </button>
          
          <button
            onClick={handleCancel}
            disabled={cancelling}
            className="py-2.5 px-4 rounded-xl font-semibold text-xs text-slate-600 hover:text-slate-900 hover:bg-slate-100 transition-colors"
          >
            {cancelling ? 'Cancelling...' : 'Cancel'}
          </button>
        </div>
      )}

    </div>
  );
};

export default ActiveRideTracker;

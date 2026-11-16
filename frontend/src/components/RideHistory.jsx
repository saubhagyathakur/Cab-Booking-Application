import React from 'react';
import { receiptApi } from '../services/api';
import { Download, Calendar, MapPin, CheckCircle, Clock } from 'lucide-react';

const RideHistory = ({ rides = [], onSelectRide }) => {
  if (rides.length === 0) {
    return (
      <div className="bg-white rounded-2xl border border-slate-200 p-6 text-center text-xs text-slate-400">
        No past ride receipts available yet. Book your first ride to see it here!
      </div>
    );
  }

  return (
    <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-base font-bold text-slate-900">Your Ride History & Receipts</h3>
        <span className="text-xs text-slate-500">{rides.length} trips</span>
      </div>

      <div className="space-y-3">
        {rides.map((ride) => (
          <div
            key={ride.id}
            className="p-4 rounded-xl border border-slate-200 hover:border-slate-300 bg-slate-50/50 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 transition-all"
          >
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold text-slate-900">Ride #{ride.id}</span>
                <span className="text-[10px] bg-slate-200 text-slate-700 px-1.5 py-0.5 rounded font-semibold">
                  {ride.vehicleType}
                </span>
                <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${
                  ride.status === 'COMPLETED' ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-700'
                }`}>
                  {ride.status}
                </span>
              </div>
              <div className="text-xs text-slate-600 line-clamp-1">
                <b>From:</b> {ride.pickupAddress}
              </div>
              <div className="text-xs text-slate-600 line-clamp-1">
                <b>To:</b> {ride.dropAddress}
              </div>
              <div className="text-[11px] text-slate-400">
                {ride.distanceKm} km • ~{ride.durationMinutes} mins • {ride.createdAt ? new Date(ride.createdAt).toLocaleDateString() : 'Recent'}
              </div>
            </div>

            <div className="flex items-center gap-3 w-full sm:w-auto justify-between sm:justify-end border-t sm:border-t-0 pt-2 sm:pt-0">
              <div className="text-right">
                <div className="text-sm font-black text-slate-900">${ride.finalFare}</div>
                <div className="text-[10px] text-emerald-600 font-semibold">{ride.paymentStatus}</div>
              </div>
              <a
                href={receiptApi.downloadUrl(ride.id)}
                target="_blank"
                rel="noreferrer"
                className="py-1.5 px-3 rounded-lg text-xs font-semibold bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 shadow-sm flex items-center gap-1.5 transition-colors"
                title="Download Official PDF Invoice"
              >
                <Download className="w-3.5 h-3.5" />
                <span>Invoice PDF</span>
              </a>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default RideHistory;

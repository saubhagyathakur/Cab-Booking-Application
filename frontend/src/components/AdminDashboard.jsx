import React, { useState, useEffect } from 'react';
import { adminApi } from '../services/api';
import { useWebSocket } from '../context/WebSocketContext';
import { 
  Users, Car, DollarSign, Activity, CheckCircle, 
  AlertTriangle, ShieldAlert, LifeBuoy, Check 
} from 'lucide-react';

const AdminDashboard = () => {
  const { subscribe } = useWebSocket();
  const [stats, setStats] = useState(null);
  const [rides, setRides] = useState([]);
  const [drivers, setDrivers] = useState([]);
  const [tickets, setTickets] = useState([]);
  const [sosAlerts, setSosAlerts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();

    // Subscribe to admin alerts and SOS alerts in real time
    const unsubSos = subscribe('/topic/admin/alerts', (alert) => {
      setSosAlerts((prev) => [alert, ...prev]);
    });

    return () => {
      if (unsubSos) unsubSos();
    };
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [dashRes, ridesRes, driversRes, ticketsRes] = await Promise.all([
        adminApi.getDashboard(),
        adminApi.getRides(),
        adminApi.getDrivers(),
        adminApi.getTickets(),
      ]);

      if (dashRes.data.success) setStats(dashRes.data.data);
      if (ridesRes.data.success) setRides(ridesRes.data.data);
      if (driversRes.data.success) setDrivers(driversRes.data.data);
      if (ticketsRes.data.success) setTickets(ticketsRes.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleResolveTicket = async (ticketId) => {
    try {
      const res = await adminApi.resolveTicket(ticketId);
      if (res.data.success) {
        setTickets((prev) =>
          prev.map((t) => (t.id === ticketId ? { ...t, status: 'RESOLVED' } : t))
        );
      }
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center p-12 text-slate-500">
        <div className="w-8 h-8 border-2 border-emerald-500 border-t-transparent rounded-full animate-spin mr-3" />
        <span>Loading Admin Operations Console...</span>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      
      {/* Title */}
      <div>
        <h1 className="text-xl font-black text-slate-900 tracking-tight">System Operations & Admin Console</h1>
        <p className="text-xs text-slate-500">Real-time platform metrics, live trip monitoring, and support management</p>
      </div>

      {/* SOS Live Alert Banner (if any) */}
      {sosAlerts.length > 0 && (
        <div className="p-4 bg-red-600 text-white rounded-2xl shadow-lg flex items-center justify-between animate-pulse">
          <div className="flex items-center gap-3">
            <ShieldAlert className="w-6 h-6 shrink-0" />
            <div>
              <div className="font-bold text-sm">LIVE EMERGENCY SOS BROADCAST RECEIVED!</div>
              <div className="text-xs opacity-90">{sosAlerts[0]?.message} • Ride #{sosAlerts[0]?.rideId}</div>
            </div>
          </div>
          <button
            onClick={() => setSosAlerts([])}
            className="text-xs font-bold bg-white/20 hover:bg-white/30 px-3 py-1.5 rounded-lg"
          >
            Acknowledge
          </button>
        </div>
      )}

      {/* KPI Stats Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-1">
            <span className="text-[11px] font-bold uppercase tracking-wider">Gross Revenue</span>
            <DollarSign className="w-4 h-4 text-emerald-600" />
          </div>
          <div className="text-2xl font-black text-slate-900">${stats?.totalRevenue || '0.00'}</div>
          <div className="text-[10px] text-slate-400 mt-1">Stripe processed</div>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-1">
            <span className="text-[11px] font-bold uppercase tracking-wider">Total Rides</span>
            <Activity className="w-4 h-4 text-blue-600" />
          </div>
          <div className="text-2xl font-black text-slate-900">{stats?.totalRides || 0}</div>
          <div className="text-[10px] text-slate-400 mt-1">{stats?.completedRides || 0} completed</div>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-1">
            <span className="text-[11px] font-bold uppercase tracking-wider">Active Drivers</span>
            <Car className="w-4 h-4 text-purple-600" />
          </div>
          <div className="text-2xl font-black text-slate-900">{stats?.activeDrivers || 0}</div>
          <div className="text-[10px] text-slate-400 mt-1">of {stats?.totalDrivers || 0} registered</div>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-1">
            <span className="text-[11px] font-bold uppercase tracking-wider">Active Rides</span>
            <Activity className="w-4 h-4 text-amber-500" />
          </div>
          <div className="text-2xl font-black text-slate-900">{stats?.activeRides || 0}</div>
          <div className="text-[10px] text-slate-400 mt-1">Live in progress</div>
        </div>
      </div>

      {/* Live Rides Table */}
      <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm space-y-3">
        <h2 className="text-base font-bold text-slate-900">Live Trip Roster</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-xs text-left">
            <thead>
              <tr className="border-b border-slate-200 text-slate-400 uppercase font-semibold">
                <th className="py-2.5">ID</th>
                <th className="py-2.5">Rider</th>
                <th className="py-2.5">Driver</th>
                <th className="py-2.5">Category</th>
                <th className="py-2.5">Fare</th>
                <th className="py-2.5">Status</th>
                <th className="py-2.5">Payment</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {rides.map((r) => (
                <tr key={r.id}>
                  <td className="py-3 font-mono font-bold">#{r.id}</td>
                  <td className="py-3 font-medium text-slate-800">{r.riderName}</td>
                  <td className="py-3 text-slate-600">{r.driverName || '—'}</td>
                  <td className="py-3 font-semibold">{r.vehicleType}</td>
                  <td className="py-3 font-bold text-slate-900">${r.finalFare}</td>
                  <td className="py-3">
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-800 border">
                      {r.status}
                    </span>
                  </td>
                  <td className="py-3 font-medium text-emerald-600">{r.paymentStatus}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Driver Fleet Table */}
      <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm space-y-3">
        <h2 className="text-base font-bold text-slate-900">Driver Fleet Status</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-xs text-left">
            <thead>
              <tr className="border-b border-slate-200 text-slate-400 uppercase font-semibold">
                <th className="py-2.5">Driver Name</th>
                <th className="py-2.5">Vehicle</th>
                <th className="py-2.5">Plate Number</th>
                <th className="py-2.5">Status</th>
                <th className="py-2.5">Rating</th>
                <th className="py-2.5">Rides</th>
                <th className="py-2.5 text-right">Total Earnings</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {drivers.map((d) => (
                <tr key={d.id}>
                  <td className="py-3 font-bold text-slate-900">{d.name}</td>
                  <td className="py-3 text-slate-600">{d.vehicleModel} ({d.vehicleType})</td>
                  <td className="py-3 font-mono text-slate-500">{d.vehicleNumber}</td>
                  <td className="py-3">
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                      d.status === 'AVAILABLE' ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-700'
                    }`}>
                      {d.status}
                    </span>
                  </td>
                  <td className="py-3 font-semibold text-amber-600">⭐ {d.rating}</td>
                  <td className="py-3 text-slate-600">{d.totalRides}</td>
                  <td className="py-3 text-right font-bold text-slate-900">${d.totalEarnings}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Customer Support Tickets */}
      <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="text-base font-bold text-slate-900">Support Tickets & Inquiries</h2>
          <span className="text-xs bg-slate-100 text-slate-600 px-2 py-0.5 rounded-full font-semibold">
            {tickets.length} Total
          </span>
        </div>

        {tickets.length === 0 ? (
          <div className="text-xs text-slate-400 py-4 text-center">No open tickets.</div>
        ) : (
          <div className="space-y-2">
            {tickets.map((t) => (
              <div
                key={t.id}
                className="p-3 rounded-xl border border-slate-200 bg-slate-50/50 flex items-center justify-between gap-3 text-xs"
              >
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-slate-900">{t.subject}</span>
                    <span className={`text-[10px] px-1.5 py-0.5 rounded font-bold ${
                      t.status === 'RESOLVED' ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'
                    }`}>
                      {t.status}
                    </span>
                  </div>
                  <p className="text-slate-600">{t.message}</p>
                </div>
                {t.status !== 'RESOLVED' && (
                  <button
                    onClick={() => handleResolveTicket(t.id)}
                    className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-[11px] rounded-lg transition-colors shrink-0 flex items-center gap-1"
                  >
                    <Check className="w-3.5 h-3.5" />
                    <span>Resolve</span>
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

    </div>
  );
};

export default AdminDashboard;

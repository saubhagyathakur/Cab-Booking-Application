import React from 'react';
import { useAuth } from '../context/AuthContext';
import { useWebSocket } from '../context/WebSocketContext';
import { Car, Shield, User, LogOut, Radio, ChevronDown } from 'lucide-react';

const Navbar = ({ activeTab, setActiveTab }) => {
  const { user, logout, switchDemoAccount } = useAuth();
  const { connected } = useWebSocket();

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-50 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        
        {/* Brand */}
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-green-600 to-emerald-400 flex items-center justify-center text-white shadow-md shadow-green-500/20">
            <Car className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-bold text-lg text-slate-900 tracking-tight">CabGo</span>
              <span className="text-[10px] uppercase font-semibold px-1.5 py-0.5 rounded bg-slate-100 text-slate-600 border border-slate-200">Enterprise</span>
            </div>
            <p className="text-xs text-slate-500 hidden sm:block">Full-Stack Ride Hailing Platform</p>
          </div>
        </div>

        {/* Navigation Tabs based on Role */}
        <div className="flex items-center gap-1 bg-slate-100 p-1 rounded-xl">
          <button
            onClick={() => setActiveTab('rider')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              activeTab === 'rider'
                ? 'bg-white text-slate-900 shadow-sm'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Rider View
          </button>
          <button
            onClick={() => setActiveTab('driver')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              activeTab === 'driver'
                ? 'bg-white text-slate-900 shadow-sm'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Driver Portal
          </button>
          <button
            onClick={() => setActiveTab('admin')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              activeTab === 'admin'
                ? 'bg-white text-slate-900 shadow-sm'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Admin Ops
          </button>
        </div>

        {/* Right side items */}
        <div className="flex items-center gap-3">
          {/* WebSocket Live Status */}
          <div 
            title={connected ? "WebSockets Connected (Live STOMP)" : "WebSockets Connecting..."}
            className={`flex items-center gap-1.5 text-xs px-2.5 py-1 rounded-full font-medium border ${
              connected 
                ? 'bg-emerald-50 text-emerald-700 border-emerald-200' 
                : 'bg-amber-50 text-amber-700 border-amber-200'
            }`}
          >
            <span className={`w-2 h-2 rounded-full ${connected ? 'bg-emerald-500 animate-pulse' : 'bg-amber-500'}`} />
            <span className="hidden md:inline">{connected ? 'Live Sync' : 'Connecting'}</span>
          </div>

          {/* Quick Demo Switcher */}
          <div className="relative group">
            <button className="flex items-center gap-1.5 text-xs font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 px-2.5 py-1.5 rounded-lg transition-colors">
              <span>Demo Persona</span>
              <ChevronDown className="w-3.5 h-3.5 text-slate-500" />
            </button>
            <div className="absolute right-0 top-full mt-1 w-48 bg-white border border-slate-200 rounded-xl shadow-lg p-1.5 hidden group-hover:block z-50">
              <div className="text-[10px] font-semibold text-slate-400 uppercase px-2 py-1">Quick Switch Account</div>
              <button
                onClick={() => { switchDemoAccount('ROLE_RIDER'); setActiveTab('rider'); }}
                className="w-full text-left px-2.5 py-1.5 text-xs rounded-lg hover:bg-slate-50 text-slate-700 flex items-center justify-between"
              >
                <span>Rider (Alex)</span>
                <span className="text-[10px] bg-blue-100 text-blue-700 px-1 rounded">Rider</span>
              </button>
              <button
                onClick={() => { switchDemoAccount('ROLE_DRIVER'); setActiveTab('driver'); }}
                className="w-full text-left px-2.5 py-1.5 text-xs rounded-lg hover:bg-slate-50 text-slate-700 flex items-center justify-between"
              >
                <span>Driver (John)</span>
                <span className="text-[10px] bg-green-100 text-green-700 px-1 rounded">Driver</span>
              </button>
              <button
                onClick={() => { switchDemoAccount('ROLE_ADMIN'); setActiveTab('admin'); }}
                className="w-full text-left px-2.5 py-1.5 text-xs rounded-lg hover:bg-slate-50 text-slate-700 flex items-center justify-between"
              >
                <span>Super Admin</span>
                <span className="text-[10px] bg-purple-100 text-purple-700 px-1 rounded">Admin</span>
              </button>
            </div>
          </div>

          {/* User profile / Logout */}
          {user ? (
            <div className="flex items-center gap-2">
              <div className="hidden sm:block text-right">
                <div className="text-xs font-bold text-slate-800">{user.name}</div>
                <div className="text-[10px] text-slate-500 capitalize">{user.role?.replace('ROLE_', '').toLowerCase()}</div>
              </div>
              <button
                onClick={logout}
                title="Log out"
                className="p-1.5 rounded-lg text-slate-500 hover:text-red-600 hover:bg-red-50 transition-colors"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <button
              onClick={() => switchDemoAccount('ROLE_RIDER')}
              className="text-xs font-semibold bg-emerald-600 text-white px-3 py-1.5 rounded-lg hover:bg-emerald-700 shadow-sm transition-all"
            >
              Sign In
            </button>
          )}

        </div>
      </div>
    </header>
  );
};

export default Navbar;

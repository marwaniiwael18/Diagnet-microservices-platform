import { useQuery } from '@tanstack/react-query';
import { dataApi } from '../services/api';
import { Activity, AlertCircle, TrendingUp } from 'lucide-react';
import MachineCard from './MachineCard';
import Charts from './Charts';
import type { MachineData } from '../types';

// Dashboard with Professional Design - v2.0
export default function Dashboard() {
  const username = localStorage.getItem('username');

  // Fetch recent data every 10 seconds
  const { data: recentData, isLoading, error } = useQuery({
    queryKey: ['recentData'],
    queryFn: () => dataApi.getRecent(100),
    refetchInterval: 10000,
    staleTime: 5000, // Don't refetch for 5 seconds after mount
    retry: 1, // Only retry once on failure
    retryDelay: 1000, // Wait 1 second before retry
  });

  // Group data by machine
  const machineGroups = recentData?.reduce((acc, data) => {
    if (!acc[data.machineId]) {
      acc[data.machineId] = [];
    }
    acc[data.machineId].push(data);
    return acc;
  }, {} as Record<string, MachineData[]>);

  // Get latest reading for each machine
  const latestReadings = Object.entries(machineGroups || {}).map(([machineId, readings]) => {
    const sorted = readings.sort((a, b) => 
      new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
    );
    return sorted[0];
  });

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    window.location.href = '/';
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 via-blue-900 to-slate-900">
        <div className="text-center">
          <div className="relative">
            <div className="animate-spin rounded-full h-20 w-20 border-t-4 border-b-4 border-blue-400 mx-auto mb-6"></div>
            <Activity className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-8 h-8 text-blue-400 animate-pulse" />
          </div>
          <p className="text-gray-300 text-lg font-medium">Loading machine data...</p>
          <p className="text-gray-500 text-sm mt-2">Connecting to sensors...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 via-blue-900 to-slate-900">
        <div className="bg-red-500/10 backdrop-blur-lg border border-red-500/20 text-red-300 p-8 rounded-2xl max-w-md shadow-2xl">
          <AlertCircle className="w-16 h-16 mx-auto mb-4 text-red-400" />
          <p className="text-center text-lg font-semibold mb-2">Failed to load machine data</p>
          <p className="text-center text-sm text-red-200/70 mb-6">Please check your connection and try again</p>
          <button
            onClick={() => window.location.reload()}
            className="w-full bg-red-500 hover:bg-red-600 text-white py-3 rounded-xl transition-all duration-200 font-medium shadow-lg hover:shadow-xl transform hover:scale-105"
          >
            Retry Connection
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-blue-900 to-slate-900">
      {/* Animated Background Pattern */}
      <div className="fixed inset-0 overflow-hidden pointer-events-none opacity-20">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-blue-500 rounded-full mix-blend-multiply filter blur-3xl animate-pulse"></div>
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-purple-500 rounded-full mix-blend-multiply filter blur-3xl animate-pulse delay-700"></div>
      </div>

      {/* Header */}
      <header className="relative backdrop-blur-xl bg-white/5 border-b border-white/10 shadow-2xl">
        <div className="max-w-7xl mx-auto px-6 py-5 flex justify-between items-center">
          <div className="flex items-center gap-4">
            <div className="p-2 bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl shadow-lg">
              <Activity className="w-8 h-8 text-white" />
            </div>
            <div>
              <h1 className="text-3xl font-bold bg-gradient-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
                DiagNet Dashboard
              </h1>
              <p className="text-gray-400 text-sm">Industrial IoT Monitoring System</p>
            </div>
          </div>
          <div className="flex items-center gap-6">
            <div className="text-right">
              <p className="text-gray-400 text-xs">Logged in as</p>
              <p className="text-gray-200 font-semibold">{username}</p>
            </div>
            <button
              onClick={handleLogout}
              className="bg-gradient-to-r from-red-500 to-pink-600 text-white px-6 py-2.5 rounded-xl hover:from-red-600 hover:to-pink-700 transition-all duration-200 font-medium shadow-lg hover:shadow-xl transform hover:scale-105"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="relative max-w-7xl mx-auto px-6 py-8">
        {/* Stats Overview */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          {/* Total Machines Card */}
          <div className="group relative overflow-hidden backdrop-blur-xl bg-gradient-to-br from-blue-500/10 to-blue-600/10 border border-blue-400/20 p-6 rounded-2xl shadow-xl hover:shadow-2xl transition-all duration-300 hover:scale-105">
            <div className="absolute top-0 right-0 w-32 h-32 bg-blue-500/10 rounded-full -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-500"></div>
            <div className="relative flex items-center justify-between">
              <div>
                <p className="text-blue-300 text-sm font-medium mb-1">Total Machines</p>
                <p className="text-4xl font-bold text-white">{latestReadings.length}</p>
                <p className="text-blue-400 text-xs mt-1">Connected devices</p>
              </div>
              <div className="p-4 bg-blue-500/20 rounded-2xl">
                <Activity className="w-10 h-10 text-blue-400" />
              </div>
            </div>
          </div>
          
          {/* Active Machines Card */}
          <div className="group relative overflow-hidden backdrop-blur-xl bg-gradient-to-br from-green-500/10 to-emerald-600/10 border border-green-400/20 p-6 rounded-2xl shadow-xl hover:shadow-2xl transition-all duration-300 hover:scale-105">
            <div className="absolute top-0 right-0 w-32 h-32 bg-green-500/10 rounded-full -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-500"></div>
            <div className="relative flex items-center justify-between">
              <div>
                <p className="text-green-300 text-sm font-medium mb-1">Active Machines</p>
                <p className="text-4xl font-bold text-white">
                  {latestReadings.filter(r => r.status === 'RUNNING').length}
                </p>
                <p className="text-green-400 text-xs mt-1">Operating normally</p>
              </div>
              <div className="p-4 bg-green-500/20 rounded-2xl">
                <TrendingUp className="w-10 h-10 text-green-400" />
              </div>
            </div>
          </div>
          
          {/* Alerts Card */}
          <div className="group relative overflow-hidden backdrop-blur-xl bg-gradient-to-br from-red-500/10 to-orange-600/10 border border-red-400/20 p-6 rounded-2xl shadow-xl hover:shadow-2xl transition-all duration-300 hover:scale-105">
            <div className="absolute top-0 right-0 w-32 h-32 bg-red-500/10 rounded-full -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-500"></div>
            <div className="relative flex items-center justify-between">
              <div>
                <p className="text-red-300 text-sm font-medium mb-1">Active Alerts</p>
                <p className="text-4xl font-bold text-white">
                  {latestReadings.filter(r => r.status === 'CRITICAL' || r.status === 'WARNING').length}
                </p>
                <p className="text-red-400 text-xs mt-1">Require attention</p>
              </div>
              <div className="p-4 bg-red-500/20 rounded-2xl">
                <AlertCircle className="w-10 h-10 text-red-400 animate-pulse" />
              </div>
            </div>
          </div>
        </div>

        {/* Section Title */}
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-white mb-2">Machine Status</h2>
          <div className="h-1 w-24 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full"></div>
        </div>

        {/* Machine Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
          {latestReadings.map((reading) => (
            <MachineCard key={reading.machineId} data={reading} />
          ))}
        </div>

        {/* Charts Section */}
        {latestReadings.length > 0 && (
          <>
            <div className="mb-6 mt-12">
              <h2 className="text-2xl font-bold text-white mb-2">Sensor Analytics</h2>
              <div className="h-1 w-24 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full"></div>
            </div>
            <Charts machineId={latestReadings[0].machineId} />
          </>
        )}
      </main>
    </div>
  );
}

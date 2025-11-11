import { useQuery } from '@tanstack/react-query';
import { dataApi } from '../services/api';
import { Activity, AlertCircle, TrendingUp } from 'lucide-react';
import MachineCard from './MachineCard';
import Charts from './Charts';
import type { MachineData } from '../types';

export default function Dashboard() {
  const username = localStorage.getItem('username');

  // Fetch recent data every 10 seconds
  const { data: recentData, isLoading, error } = useQuery({
    queryKey: ['recentData'],
    queryFn: () => dataApi.getRecent(100),
    refetchInterval: 10000,
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
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading machine data...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <div className="bg-red-50 border border-red-300 text-red-700 p-6 rounded-lg max-w-md">
          <AlertCircle className="w-12 h-12 mx-auto mb-4" />
          <p className="text-center">Failed to load machine data</p>
          <button
            onClick={() => window.location.reload()}
            className="mt-4 w-full bg-red-600 text-white py-2 rounded hover:bg-red-700"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-white shadow-md">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <Activity className="w-8 h-8 text-blue-600" />
            <h1 className="text-2xl font-bold text-gray-800">DiagNet Dashboard</h1>
          </div>
          <div className="flex items-center gap-4">
            <span className="text-gray-600">Welcome, {username}</span>
            <button
              onClick={handleLogout}
              className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700 transition"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 py-6">
        {/* Stats Overview */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <div className="bg-white p-4 rounded-lg shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm">Total Machines</p>
                <p className="text-2xl font-bold">{latestReadings.length}</p>
              </div>
              <Activity className="w-10 h-10 text-blue-500" />
            </div>
          </div>
          
          <div className="bg-white p-4 rounded-lg shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm">Active Machines</p>
                <p className="text-2xl font-bold">
                  {latestReadings.filter(r => r.status === 'RUNNING').length}
                </p>
              </div>
              <TrendingUp className="w-10 h-10 text-green-500" />
            </div>
          </div>
          
          <div className="bg-white p-4 rounded-lg shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm">Alerts</p>
                <p className="text-2xl font-bold">
                  {latestReadings.filter(r => r.status === 'CRITICAL' || r.status === 'WARNING').length}
                </p>
              </div>
              <AlertCircle className="w-10 h-10 text-red-500" />
            </div>
          </div>
        </div>

        {/* Machine Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
          {latestReadings.map((reading) => (
            <MachineCard key={reading.machineId} data={reading} />
          ))}
        </div>

        {/* Charts */}
        {latestReadings.length > 0 && (
          <Charts machineId={latestReadings[0].machineId} />
        )}
      </main>
    </div>
  );
}

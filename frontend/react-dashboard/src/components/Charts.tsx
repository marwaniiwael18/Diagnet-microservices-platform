import { useQuery } from '@tanstack/react-query';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { dataApi } from '../services/api';
import { TrendingUp, AlertCircle } from 'lucide-react';

interface ChartsProps {
  machineId: string;
}

export default function Charts({ machineId }: ChartsProps) {
  const { data: machineData, isLoading, error } = useQuery({
    queryKey: ['machineData', machineId],
    queryFn: () => dataApi.getRecentByMachine(machineId, 24),
    refetchInterval: 30000, // Refresh every 30 seconds
    enabled: !!localStorage.getItem('token'), // Only run if token exists
    retry: 2, // Retry failed requests twice
    retryDelay: 1000, // Wait 1 second between retries
  });

  if (isLoading || !machineData) {
    return (
      <div className="backdrop-blur-xl bg-white/5 border border-white/10 rounded-2xl shadow-xl p-8">
        <div className="flex items-center justify-center gap-3">
          <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-400"></div>
          <p className="text-gray-300">Loading analytics...</p>
        </div>
      </div>
    );
  }

  if (error) {
    console.error('[Charts] Error loading machine data:', error);
    return (
      <div className="backdrop-blur-xl bg-red-500/10 border border-red-400/20 rounded-2xl shadow-xl p-8">
        <div className="flex items-center justify-center gap-3">
          <AlertCircle className="w-8 h-8 text-red-400" />
          <div>
            <p className="text-red-300 font-semibold">Failed to load analytics</p>
            <p className="text-red-400/70 text-sm">Unable to fetch data for {machineId}</p>
          </div>
        </div>
      </div>
    );
  }

  // Format data for charts
  const chartData = machineData
    .slice()
    .sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime())
    .map((d) => ({
      time: new Date(d.timestamp).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
      }),
      temperature: d.temperature,
      vibration: d.vibration,
      timestamp: d.timestamp,
    }));

  // Custom tooltip
  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="backdrop-blur-xl bg-slate-900/95 border border-white/20 rounded-xl p-4 shadow-2xl">
          <p className="text-gray-300 text-sm font-semibold mb-2">{label}</p>
          {payload.map((entry: any, index: number) => (
            <p key={index} className="text-sm" style={{ color: entry.color }}>
              {entry.name}: <span className="font-bold">{entry.value.toFixed(2)}</span>
              {entry.name === 'Temperature' && '°C'}
            </p>
          ))}
        </div>
      );
    }
    return null;
  };

  return (
    <div className="backdrop-blur-xl bg-white/5 border border-white/10 rounded-2xl shadow-xl p-8">
      {/* Header */}
      <div className="flex items-center gap-3 mb-8">
        <div className="p-3 bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl">
          <TrendingUp className="w-6 h-6 text-white" />
        </div>
        <div>
          <h2 className="text-2xl font-bold text-white">
            {machineId} Analytics
          </h2>
          <p className="text-gray-400 text-sm">Last 24 hours sensor data</p>
        </div>
      </div>

      <div className="space-y-8">
        {/* Temperature Chart */}
        <div className="backdrop-blur-xl bg-white/5 border border-white/10 rounded-xl p-6">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-lg font-bold text-white mb-1">Temperature Monitoring</h3>
              <p className="text-gray-400 text-sm">Thermal performance over time</p>
            </div>
            <div className="text-right">
              <p className="text-gray-400 text-xs">Current</p>
              <p className="text-2xl font-bold text-orange-400">
                {chartData[chartData.length - 1]?.temperature.toFixed(1)}°C
              </p>
            </div>
          </div>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData}>
              <defs>
                <linearGradient id="temperatureGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#f97316" stopOpacity={0.3}/>
                  <stop offset="95%" stopColor="#f97316" stopOpacity={0}/>
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#374151" opacity={0.3} />
              <XAxis 
                dataKey="time" 
                tick={{ fontSize: 11, fill: '#9ca3af' }}
                stroke="#4b5563"
                interval="preserveStartEnd"
              />
              <YAxis 
                domain={[60, 110]}
                tick={{ fontSize: 11, fill: '#9ca3af' }}
                stroke="#4b5563"
              />
              <Tooltip content={<CustomTooltip />} />
              <Legend 
                wrapperStyle={{ paddingTop: '20px' }}
                iconType="circle"
                formatter={(value) => <span className="text-gray-300 text-sm">{value}</span>}
              />
              <Line 
                type="monotone" 
                dataKey="temperature" 
                stroke="#f97316" 
                strokeWidth={3}
                dot={{ r: 3, fill: '#f97316', strokeWidth: 2, stroke: '#fff' }}
                activeDot={{ r: 6, fill: '#f97316' }}
                name="Temperature"
                fill="url(#temperatureGradient)"
              />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Vibration Chart */}
        <div className="backdrop-blur-xl bg-white/5 border border-white/10 rounded-xl p-6">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-lg font-bold text-white mb-1">Vibration Analysis</h3>
              <p className="text-gray-400 text-sm">Mechanical stability metrics</p>
            </div>
            <div className="text-right">
              <p className="text-gray-400 text-xs">Current</p>
              <p className="text-2xl font-bold text-blue-400">
                {chartData[chartData.length - 1]?.vibration.toFixed(3)}g
              </p>
            </div>
          </div>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData}>
              <defs>
                <linearGradient id="vibrationGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3}/>
                  <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#374151" opacity={0.3} />
              <XAxis 
                dataKey="time" 
                tick={{ fontSize: 11, fill: '#9ca3af' }}
                stroke="#4b5563"
                interval="preserveStartEnd"
              />
              <YAxis 
                domain={[0, 1]}
                tick={{ fontSize: 11, fill: '#9ca3af' }}
                stroke="#4b5563"
              />
              <Tooltip content={<CustomTooltip />} />
              <Legend 
                wrapperStyle={{ paddingTop: '20px' }}
                iconType="circle"
                formatter={(value) => <span className="text-gray-300 text-sm">{value}</span>}
              />
              <Line 
                type="monotone" 
                dataKey="vibration" 
                stroke="#3b82f6" 
                strokeWidth={3}
                dot={{ r: 3, fill: '#3b82f6', strokeWidth: 2, stroke: '#fff' }}
                activeDot={{ r: 6, fill: '#3b82f6' }}
                name="Vibration"
                fill="url(#vibrationGradient)"
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}

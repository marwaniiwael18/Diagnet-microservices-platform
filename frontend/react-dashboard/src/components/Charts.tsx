import { useQuery } from '@tanstack/react-query';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { dataApi } from '../services/api';

interface ChartsProps {
  machineId: string;
}

export default function Charts({ machineId }: ChartsProps) {
  const { data: machineData, isLoading } = useQuery({
    queryKey: ['machineData', machineId],
    queryFn: () => dataApi.getRecentByMachine(machineId, 24),
    refetchInterval: 30000, // Refresh every 30 seconds
  });

  if (isLoading || !machineData) {
    return (
      <div className="bg-white rounded-lg shadow-md p-6">
        <p className="text-center text-gray-500">Loading charts...</p>
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

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-xl font-bold text-gray-800 mb-4">
        Sensor Data - {machineId} (Last 24 Hours)
      </h2>

      <div className="space-y-8">
        {/* Temperature Chart */}
        <div>
          <h3 className="text-lg font-semibold text-gray-700 mb-3">Temperature (°C)</h3>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis 
                dataKey="time" 
                tick={{ fontSize: 12 }}
                interval="preserveStartEnd"
              />
              <YAxis 
                domain={[60, 110]}
                tick={{ fontSize: 12 }}
              />
              <Tooltip 
                contentStyle={{ backgroundColor: '#fff', border: '1px solid #ccc' }}
                labelStyle={{ fontWeight: 'bold' }}
              />
              <Legend />
              <Line 
                type="monotone" 
                dataKey="temperature" 
                stroke="#ef4444" 
                strokeWidth={2}
                dot={{ r: 2 }}
                name="Temperature"
              />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Vibration Chart */}
        <div>
          <h3 className="text-lg font-semibold text-gray-700 mb-3">Vibration</h3>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis 
                dataKey="time" 
                tick={{ fontSize: 12 }}
                interval="preserveStartEnd"
              />
              <YAxis 
                domain={[0, 1]}
                tick={{ fontSize: 12 }}
              />
              <Tooltip 
                contentStyle={{ backgroundColor: '#fff', border: '1px solid #ccc' }}
                labelStyle={{ fontWeight: 'bold' }}
              />
              <Legend />
              <Line 
                type="monotone" 
                dataKey="vibration" 
                stroke="#3b82f6" 
                strokeWidth={2}
                dot={{ r: 2 }}
                name="Vibration"
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}

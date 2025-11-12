import { AlertCircle, Thermometer, Activity, Gauge, Zap } from 'lucide-react';
import type { MachineData } from '../types';

interface MachineCardProps {
  data: MachineData;
}

export default function MachineCard({ data }: MachineCardProps) {
  const statusConfig = {
    RUNNING: {
      gradient: 'from-green-500/20 to-emerald-500/20',
      border: 'border-green-400/30',
      badge: 'from-green-500 to-emerald-600',
      icon: '✓',
      pulse: false,
    },
    IDLE: {
      gradient: 'from-gray-500/20 to-slate-500/20',
      border: 'border-gray-400/30',
      badge: 'from-gray-500 to-slate-600',
      icon: '○',
      pulse: false,
    },
    WARNING: {
      gradient: 'from-yellow-500/20 to-orange-500/20',
      border: 'border-yellow-400/30',
      badge: 'from-yellow-500 to-orange-600',
      icon: '⚠',
      pulse: true,
    },
    CRITICAL: {
      gradient: 'from-red-500/20 to-pink-500/20',
      border: 'border-red-400/30',
      badge: 'from-red-500 to-pink-600',
      icon: '✕',
      pulse: true,
    },
  };

  const config = statusConfig[data.status];

  return (
    <div className={`group relative overflow-hidden backdrop-blur-xl bg-gradient-to-br ${config.gradient} border ${config.border} rounded-2xl shadow-xl hover:shadow-2xl transition-all duration-300 hover:scale-105`}>
      {/* Decorative corner element */}
      <div className="absolute top-0 right-0 w-32 h-32 bg-white/5 rounded-full -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-500"></div>
      
      <div className="relative p-6">
        {/* Header */}
        <div className="flex justify-between items-start mb-6">
          <div>
            <h3 className="text-2xl font-bold text-white mb-1">{data.machineId}</h3>
            <p className="text-gray-400 text-sm">Industrial Machine</p>
          </div>
          <span className={`px-4 py-2 rounded-xl text-sm font-bold bg-gradient-to-r ${config.badge} text-white shadow-lg ${config.pulse ? 'animate-pulse' : ''}`}>
            {config.icon} {data.status}
          </span>
        </div>

        {/* Metrics Grid */}
        <div className="grid grid-cols-2 gap-4 mb-6">
          {/* Temperature */}
          <div className="bg-white/5 backdrop-blur-sm rounded-xl p-4 border border-white/10">
            <div className="flex items-center gap-2 mb-2">
              <Thermometer className="w-4 h-4 text-orange-400" />
              <span className="text-gray-400 text-xs font-medium">Temperature</span>
            </div>
            <p className="text-2xl font-bold text-white">
              {data.temperature.toFixed(1)}<span className="text-sm text-gray-400">°C</span>
            </p>
          </div>

          {/* Vibration */}
          <div className="bg-white/5 backdrop-blur-sm rounded-xl p-4 border border-white/10">
            <div className="flex items-center gap-2 mb-2">
              <Activity className="w-4 h-4 text-blue-400" />
              <span className="text-gray-400 text-xs font-medium">Vibration</span>
            </div>
            <p className="text-2xl font-bold text-white">
              {data.vibration.toFixed(3)}<span className="text-sm text-gray-400">g</span>
            </p>
          </div>

          {/* Pressure */}
          {data.pressure && (
            <div className="bg-white/5 backdrop-blur-sm rounded-xl p-4 border border-white/10">
              <div className="flex items-center gap-2 mb-2">
                <Gauge className="w-4 h-4 text-purple-400" />
                <span className="text-gray-400 text-xs font-medium">Pressure</span>
              </div>
              <p className="text-2xl font-bold text-white">
                {data.pressure.toFixed(1)}<span className="text-sm text-gray-400"> bar</span>
              </p>
            </div>
          )}

          {/* Rotation Speed */}
          {data.rotationSpeed && (
            <div className="bg-white/5 backdrop-blur-sm rounded-xl p-4 border border-white/10">
              <div className="flex items-center gap-2 mb-2">
                <Zap className="w-4 h-4 text-green-400" />
                <span className="text-gray-400 text-xs font-medium">Speed</span>
              </div>
              <p className="text-2xl font-bold text-white">
                {data.rotationSpeed.toFixed(0)}<span className="text-sm text-gray-400"> RPM</span>
              </p>
            </div>
          )}
        </div>

        {/* Timestamp */}
        <div className="flex items-center justify-between pt-4 border-t border-white/10">
          <span className="text-gray-400 text-xs">Last updated</span>
          <span className="text-gray-300 text-xs font-medium">
            {new Date(data.timestamp).toLocaleString()}
          </span>
        </div>

        {/* Alert Banner */}
        {(data.status === 'WARNING' || data.status === 'CRITICAL') && (
          <div className={`mt-4 flex items-start gap-3 backdrop-blur-xl bg-gradient-to-r ${data.status === 'CRITICAL' ? 'from-red-500/20 to-pink-500/20' : 'from-yellow-500/20 to-orange-500/20'} p-4 rounded-xl border ${data.status === 'CRITICAL' ? 'border-red-400/30' : 'border-yellow-400/30'} animate-pulse`}>
            <AlertCircle className={`w-5 h-5 flex-shrink-0 ${data.status === 'CRITICAL' ? 'text-red-400' : 'text-yellow-400'}`} />
            <div>
              <p className={`text-sm font-semibold ${data.status === 'CRITICAL' ? 'text-red-200' : 'text-yellow-200'}`}>
                {data.status === 'CRITICAL' ? 'Critical Alert' : 'Warning'}
              </p>
              <p className={`text-xs ${data.status === 'CRITICAL' ? 'text-red-300' : 'text-yellow-300'}`}>
                {data.status === 'CRITICAL' 
                  ? 'Immediate attention required!'
                  : 'Monitor this machine closely'}
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

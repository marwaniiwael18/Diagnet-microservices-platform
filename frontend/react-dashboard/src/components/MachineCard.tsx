import { AlertCircle } from 'lucide-react';
import type { MachineData } from '../types';

interface MachineCardProps {
  data: MachineData;
}

export default function MachineCard({ data }: MachineCardProps) {
  const statusColors = {
    RUNNING: 'bg-green-100 text-green-800 border-green-300',
    IDLE: 'bg-gray-100 text-gray-800 border-gray-300',
    WARNING: 'bg-yellow-100 text-yellow-800 border-yellow-300',
    CRITICAL: 'bg-red-100 text-red-800 border-red-300',
  };

  const statusIcons = {
    RUNNING: '✓',
    IDLE: '○',
    WARNING: '⚠',
    CRITICAL: '✕',
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-4 hover:shadow-lg transition">
      <div className="flex justify-between items-start mb-3">
        <h3 className="text-lg font-bold text-gray-800">{data.machineId}</h3>
        <span className={`px-3 py-1 rounded-full text-xs font-semibold border ${statusColors[data.status]}`}>
          {statusIcons[data.status]} {data.status}
        </span>
      </div>

      <div className="space-y-2">
        <div className="flex justify-between">
          <span className="text-gray-600 text-sm">Temperature:</span>
          <span className="font-semibold text-gray-800">
            {data.temperature.toFixed(1)}°C
          </span>
        </div>

        <div className="flex justify-between">
          <span className="text-gray-600 text-sm">Vibration:</span>
          <span className="font-semibold text-gray-800">
            {data.vibration.toFixed(3)}
          </span>
        </div>

        {data.pressure && (
          <div className="flex justify-between">
            <span className="text-gray-600 text-sm">Pressure:</span>
            <span className="font-semibold text-gray-800">
              {data.pressure.toFixed(1)} bar
            </span>
          </div>
        )}

        {data.rotationSpeed && (
          <div className="flex justify-between">
            <span className="text-gray-600 text-sm">Speed:</span>
            <span className="font-semibold text-gray-800">
              {data.rotationSpeed.toFixed(0)} RPM
            </span>
          </div>
        )}

        <div className="pt-2 border-t border-gray-200">
          <span className="text-gray-500 text-xs">
            {new Date(data.timestamp).toLocaleString()}
          </span>
        </div>
      </div>

      {(data.status === 'WARNING' || data.status === 'CRITICAL') && (
        <div className="mt-3 flex items-start gap-2 bg-red-50 p-2 rounded border border-red-200">
          <AlertCircle className="w-4 h-4 text-red-600 flex-shrink-0 mt-0.5" />
          <p className="text-xs text-red-700">
            {data.status === 'CRITICAL' 
              ? 'Immediate attention required!'
              : 'Monitor this machine closely'}
          </p>
        </div>
      )}
    </div>
  );
}

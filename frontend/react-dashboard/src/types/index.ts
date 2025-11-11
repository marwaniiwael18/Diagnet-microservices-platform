export interface MachineData {
  id: number;
  machineId: string;
  timestamp: string;
  temperature: number;
  vibration: number;
  pressure?: number;
  humidity?: number;
  powerConsumption?: number;
  rotationSpeed?: number;
  status: 'RUNNING' | 'IDLE' | 'WARNING' | 'CRITICAL';
  location?: string;
  createdAt?: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  expiresIn: number;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AnalysisResult {
  machineId: string;
  healthScore: number;
  anomalies: Anomaly[];
  timestamp: string;
}

export interface Anomaly {
  type: string;
  severity: 'WARNING' | 'CRITICAL';
  message: string;
  value: number;
  threshold: number;
}

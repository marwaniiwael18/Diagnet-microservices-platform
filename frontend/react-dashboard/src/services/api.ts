import axios from 'axios';
import type { AuthResponse, LoginRequest, MachineData, AnalysisResult } from '../types';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

console.log('🔧 [API] Initializing API client');
console.log('🌐 [API] Base URL:', API_URL);

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to all requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  console.log('📤 [API Request]', config.method?.toUpperCase(), config.url);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
    console.log('🔑 [API Request] Token attached');
  } else {
    console.log('⚠️  [API Request] No token found');
  }
  return config;
});

// Handle errors
api.interceptors.response.use(
  (response) => {
    console.log('✅ [API Response]', response.status, response.config.url);
    return response;
  },
  (error) => {
    console.error('❌ [API Response Error]', {
      status: error.response?.status,
      url: error.config?.url,
      message: error.message,
      data: error.response?.data
    });
    
    // Only auto-logout on 401 if we're NOT on the login page
    // This prevents race conditions during login
    if (error.response?.status === 401 && !error.config?.url?.includes('/auth/login')) {
      console.warn('🚫 [API] Unauthorized - clearing token');
      // Only clear token if we're making an authenticated request
      // Don't redirect immediately - let React Router handle it
      const token = localStorage.getItem('token');
      if (token) {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        window.dispatchEvent(new Event('authChange'));
      }
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authApi = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    console.log('🔐 [authApi.login] Calling login endpoint...');
    console.log('📝 [authApi.login] Username:', credentials.username);
    console.log('🎯 [authApi.login] Target URL:', `${API_URL}/auth/login`);
    
    const response = await api.post<AuthResponse>('/auth/login', credentials);
    
    console.log('✅ [authApi.login] Response received:', {
      token: response.data.token ? '***' + response.data.token.slice(-10) : 'none',
      username: response.data.username,
      expiresIn: response.data.expiresIn
    });
    
    return response.data;
  },
  
  logout: () => {
    console.log('👋 [authApi.logout] Logging out...');
    localStorage.removeItem('token');
    localStorage.removeItem('username');
  },
};

// Data API
export const dataApi = {
  getRecent: async (limit: number = 100): Promise<MachineData[]> => {
    const response = await api.get<MachineData[]>(`/api/data/recent?limit=${limit}`);
    return response.data;
  },
  
  getMachineData: async (machineId: string): Promise<MachineData[]> => {
    const response = await api.get<MachineData[]>(`/api/data/machine/${machineId}`);
    return response.data;
  },
  
  getRecentByMachine: async (machineId: string, hours: number = 24): Promise<MachineData[]> => {
    const response = await api.get<MachineData[]>(`/api/data/machine/${machineId}/recent?hours=${hours}`);
    return response.data;
  },
};

// Analysis API
export const analysisApi = {
  analyzeMachine: async (machineId: string): Promise<AnalysisResult> => {
    const response = await api.get<AnalysisResult>(`/api/analysis/machine/${machineId}`);
    return response.data;
  },
};

export default api;

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../services/api';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    console.log('🔐 [Login] Attempting login...');
    console.log('📝 [Login] Username:', username);
    console.log('🌐 [Login] API URL:', import.meta.env.VITE_API_URL || 'http://localhost:8080');

    try {
      console.log('📤 [Login] Sending request to /auth/login...');
      const response = await authApi.login({ username, password });
      console.log('✅ [Login] Login successful!', response);
      
      localStorage.setItem('token', response.token);
      localStorage.setItem('username', response.username);
      console.log('💾 [Login] Token saved to localStorage');
      
      navigate('/dashboard');
    } catch (err) {
      console.error('❌ [Login] Login failed:', err);
      console.error('📋 [Login] Error details:', {
        message: err instanceof Error ? err.message : 'Unknown error',
        response: (err as any)?.response,
        status: (err as any)?.response?.status,
        data: (err as any)?.response?.data
      });
      
      const errorMessage = err instanceof Error && 'response' in err 
        ? (err as any).response?.data?.message || 'Invalid username or password'
        : 'Invalid username or password';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-900 to-blue-700">
      <div className="bg-white p-8 rounded-lg shadow-2xl w-96">
        <h1 className="text-3xl font-bold text-center mb-6 text-blue-900">DiagNet</h1>
        <p className="text-center text-gray-600 mb-6">Industrial Machine Monitoring</p>
        
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-2">
              Username
            </label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
              autoFocus
            />
          </div>
          
          <div className="mb-6">
            <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
              Password
            </label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>
          
          {error && (
            <div className="mb-4 p-3 bg-red-100 border border-red-300 text-red-700 rounded-md text-sm">
              {error}
            </div>
          )}
          
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition"
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>
        
        <div className="mt-6 text-center text-sm text-gray-600">
          <p>Demo credentials:</p>
          <p className="font-mono mt-1">admin / admin123</p>
          <p className="font-mono">user / user123</p>
        </div>
      </div>
    </div>
  );
}

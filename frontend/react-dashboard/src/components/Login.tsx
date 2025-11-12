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
      
      // Trigger auth change event
      window.dispatchEvent(new Event('authChange'));
      console.log('🔄 [Login] Auth change event dispatched');
      
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
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 via-blue-900 to-slate-900 relative overflow-hidden">
      {/* Animated Background Pattern */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none opacity-20">
        <div className="absolute -top-40 -right-40 w-96 h-96 bg-blue-500 rounded-full mix-blend-multiply filter blur-3xl animate-pulse"></div>
        <div className="absolute -bottom-40 -left-40 w-96 h-96 bg-purple-500 rounded-full mix-blend-multiply filter blur-3xl animate-pulse delay-700"></div>
        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-pink-500 rounded-full mix-blend-multiply filter blur-3xl animate-pulse delay-1000"></div>
      </div>

      {/* Login Card */}
      <div className="relative backdrop-blur-xl bg-white/10 border border-white/20 p-10 rounded-3xl shadow-2xl w-96 transform hover:scale-105 transition-all duration-300">
        {/* Logo/Title */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-blue-500 to-purple-600 rounded-2xl shadow-lg mb-4">
            <span className="text-3xl font-bold text-white">D</span>
          </div>
          <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent mb-2">
            DiagNet
          </h1>
          <p className="text-gray-300 text-sm">Industrial IoT Monitoring Platform</p>
        </div>
        
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label htmlFor="username" className="block text-sm font-medium text-gray-300 mb-2">
              Username
            </label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent backdrop-blur-sm transition-all"
              placeholder="Enter your username"
              required
              autoFocus
            />
          </div>
          
          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-300 mb-2">
              Password
            </label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent backdrop-blur-sm transition-all"
              placeholder="Enter your password"
              required
            />
          </div>
          
          {error && (
            <div className="backdrop-blur-xl bg-red-500/20 border border-red-400/30 text-red-200 p-4 rounded-xl text-sm animate-pulse">
              <span className="font-semibold">⚠ Error:</span> {error}
            </div>
          )}
          
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-gradient-to-r from-blue-500 to-purple-600 text-white py-3.5 px-4 rounded-xl hover:from-blue-600 hover:to-purple-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 font-semibold shadow-lg hover:shadow-xl transform hover:scale-105 disabled:transform-none"
          >
            {loading ? (
              <span className="flex items-center justify-center gap-2">
                <div className="w-5 h-5 border-t-2 border-b-2 border-white rounded-full animate-spin"></div>
                Logging in...
              </span>
            ) : (
              'Login'
            )}
          </button>
        </form>
        
        {/* Demo Credentials */}
        <div className="mt-8 pt-6 border-t border-white/10">
          <p className="text-center text-gray-400 text-xs mb-3">Demo Credentials</p>
          <div className="grid grid-cols-2 gap-3">
            <div className="backdrop-blur-sm bg-white/5 border border-white/10 rounded-xl p-3 text-center">
              <p className="text-gray-400 text-xs mb-1">Admin</p>
              <p className="font-mono text-white text-sm">admin</p>
              <p className="font-mono text-gray-300 text-xs">admin123</p>
            </div>
            <div className="backdrop-blur-sm bg-white/5 border border-white/10 rounded-xl p-3 text-center">
              <p className="text-gray-400 text-xs mb-1">User</p>
              <p className="font-mono text-white text-sm">user</p>
              <p className="font-mono text-gray-300 text-xs">user123</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

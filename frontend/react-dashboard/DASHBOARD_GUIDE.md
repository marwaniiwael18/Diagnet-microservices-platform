# DiagNet React Dashboard - Setup Complete! 🎨

## What We Created

✅ **React + TypeScript** dashboard with Vite (fast build tool)  
✅ **Installed Dependencies:**
- `axios` - API calls to backend
- `recharts` - Beautiful charts for data visualization
- `lucide-react` - Modern icon library
- `@tanstack/react-query` - Data fetching and caching

## Project Structure

```
frontend/react-dashboard/
├── src/
│   ├── components/          # UI components
│   │   ├── Dashboard.tsx    # Main dashboard
│   │   ├── MachineCard.tsx  # Machine status cards
│   │   ├── Charts.tsx       # Temperature/vibration charts
│   │   └── Login.tsx        # JWT authentication
│   ├── services/
│   │   └── api.ts           # API client (Gateway at :8080)
│   ├── types/
│   │   └── index.ts         # TypeScript interfaces
│   ├── App.tsx              # Main app component
│   └── main.tsx             # Entry point
├── package.json
└── vite.config.ts
```

## Features to Implement

### 1. **Authentication**
- Login page with JWT
- Store token in localStorage
- Axios interceptor adds token to requests

### 2. **Dashboard Overview**
- Grid of machine cards showing:
  - Machine ID
  - Current temperature
  - Current vibration
  - Status badge (🟢 RUNNING / ⚠️ WARNING / 🔴 CRITICAL)
  - Last updated timestamp

### 3. **Real-time Charts**
- Line chart: Temperature over time
- Line chart: Vibration over time
- Auto-refresh every 10 seconds

### 4. **Machine Details Page**
- Detailed view when clicking a machine
- Historical data visualization
- Health score from analyzer service
- List of detected anomalies

### 5. **Alerts Panel**
- List of machines in WARNING/CRITICAL state
- Click to view details
- Badge count on navigation

## API Endpoints (via Gateway :8080)

```typescript
// Login
POST /auth/login
Body: { username: "admin", password: "admin123" }
Response: { token: "eyJ...", username: "admin" }

// Get recent data
GET /api/data/recent?limit=100
Headers: { Authorization: "Bearer TOKEN" }

// Get machine data
GET /api/data/machine/{id}
Headers: { Authorization: "Bearer TOKEN" }

// Get analysis
GET /api/analysis/machine/{id}
Headers: { Authorization: "Bearer TOKEN" }
```

## How to Run

### Development Mode:
```bash
cd /Users/macbook/Desktop/DiagNet/frontend/react-dashboard
npm run dev
```

**Access:** http://localhost:5173

### Build for Production:
```bash
npm run build
```

**Output:** `dist/` folder with optimized static files

### Preview Production Build:
```bash
npm run preview
```

## Next Steps - Implementation Plan

### Phase 1: Basic Setup (30 min)
1. Create `src/types/index.ts` - TypeScript interfaces
2. Create `src/services/api.ts` - API client with axios
3. Set up React Query provider in `main.tsx`

### Phase 2: Authentication (20 min)
4. Create `src/components/Login.tsx`
5. Implement JWT storage and routing
6. Add protected routes

### Phase 3: Dashboard (45 min)
7. Create `src/components/Dashboard.tsx` - Main layout
8. Create `src/components/MachineCard.tsx` - Machine cards
9. Create `src/components/Charts.tsx` - Data visualization
10. Add auto-refresh with React Query

### Phase 4: Styling (15 min)
11. Add Tailwind CSS or styled-components
12. Responsive design
13. Dark/light theme

### Phase 5: Advanced Features (30 min)
14. Machine details modal/page
15. Alerts notification system
16. Export data to CSV
17. Date range filters

## Quick Start Code Snippets

### TypeScript Types:
```typescript
export interface MachineData {
  id: number;
  machineId: string;
  timestamp: string;
  temperature: number;
  vibration: number;
  pressure?: number;
  status: 'RUNNING' | 'WARNING' | 'CRITICAL';
  location?: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  expiresIn: number;
}
```

### API Client:
```typescript
import axios from 'axios';

const API_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_URL,
  headers: { 'Content-Type': 'application/json' }
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const login = (username: string, password: string) =>
  api.post('/auth/login', { username, password });

export const getRecentData = () =>
  api.get('/api/data/recent?limit=100');
```

### Machine Card Component:
```tsx
interface Props {
  machine: MachineData;
}

export const MachineCard = ({ machine }: Props) => {
  const statusColor = {
    RUNNING: 'bg-green-500',
    WARNING: 'bg-yellow-500',
    CRITICAL: 'bg-red-500'
  }[machine.status];

  return (
    <div className="p-4 border rounded-lg shadow">
      <h3 className="font-bold">{machine.machineId}</h3>
      <div className="mt-2">
        <span className={`px-2 py-1 rounded ${statusColor}`}>
          {machine.status}
        </span>
      </div>
      <p className="mt-2">Temp: {machine.temperature}°C</p>
      <p>Vib: {machine.vibration}</p>
    </div>
  );
};
```

## Testing Checklist

- [ ] Can login with admin/admin123
- [ ] Dashboard loads machine data
- [ ] Cards show correct status colors
- [ ] Charts display temperature/vibration
- [ ] Auto-refresh works every 10s
- [ ] Logout clears token
- [ ] Responsive on mobile
- [ ] Alerts show WARNING/CRITICAL machines

## Environment Variables

Create `.env` file:
```
VITE_API_URL=http://localhost:8080
VITE_REFRESH_INTERVAL=10000
```

## Deployment

### With Docker:
```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### With Nginx:
```bash
npm run build
cp -r dist/* /var/www/diagnet-dashboard/
```

## Project Status

✅ **Completed:**
- React + TypeScript app created
- Dependencies installed
- Development server ready

🚧 **To Do:**
- Create components (Login, Dashboard, Charts)
- Implement API integration
- Add styling (Tailwind CSS recommended)
- Add routing (React Router)
- Implement authentication flow

🎯 **Estimated Time:** 2-3 hours for full implementation

---

## Quick Commands Reference

```bash
# Install dependencies
npm install

# Start dev server
npm run dev

# Build for production
npm run build

# Preview build
npm run preview

# Type check
npm run tsc

# Lint
npm run lint
```

---

**Your React dashboard foundation is ready!** 🎉

Would you like me to create the actual component files now, or would you prefer to:
1. Implement them yourself following this guide?
2. Move to the next step (Grafana monitoring)?
3. Test the current system end-to-end first?

Let me know! 😊

# 🏭 DiagNet - Industrial Diagnostic Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> A production-grade, full-stack industrial diagnostic platform for real-time machine monitoring, anomaly detection, and performance analytics.

---

## 🎯 What is DiagNet?

DiagNet is a **complete microservices-based industrial IoT platform** that simulates an industrial diagnostic system. It connects to machines via MQTT, collects sensor data in real-time, stores it in a time-series database, analyzes it for anomalies, and visualizes metrics on secure dashboards.

**Real-world use case**: Monitor temperature, vibration, pressure, and speed sensors across factory machines to predict failures before they happen!

---

## ✨ Features

- ⚡ **Real-time Data Ingestion** - MQTT pub/sub with Eclipse Mosquitto
- 🧠 **Anomaly Detection** - Statistical analysis with z-score and moving averages
- 📊 **Time-Series Storage** - PostgreSQL + TimescaleDB for optimized time-based queries
- 🔐 **Secure API Gateway** - JWT authentication + Spring Security
- 📈 **Interactive Dashboards** - React + Recharts with live data visualization
- 📦 **Fully Containerized** - Docker Compose orchestration
- � **Observability** - Prometheus metrics + Grafana dashboards
- �️ **Production-Ready** - Health checks, logging, error handling
- 🚀 **CI/CD Ready** - Prepared for GitHub Actions automation

---

## 🏗️ Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                     FRONTEND LAYER                           │
│  ┌────────────────┐              ┌──────────────────┐       │
│  │ React Dashboard│              │  Grafana         │       │
│  │ (Port 5173)    │              │  (Port 3000)     │       │
│  └────────┬───────┘              └────────┬─────────┘       │
└───────────┼──────────────────────────────┼─────────────────┘
            │ HTTP/REST                     │ HTTP
            ↓                               ↓
┌──────────────────────────────────────────────────────────────┐
│                     API GATEWAY LAYER                         │
│  ┌─────────────────────────────────────────────────────┐     │
│  │  Gateway Service (Spring Cloud Gateway)            │     │
│  │  - JWT Authentication                              │     │
│  │  - Request Routing                                 │     │
│  │  - CORS Handling                      (Port 8080)  │     │
│  └──────────────────┬──────────────────────────────────┘     │
└────────────────────┼───────────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        ↓                         ↓
┌──────────────────────────────────────────────────────────────┐
│                  MICROSERVICES LAYER                          │
│  ┌──────────────┐              ┌──────────────────┐          │
│  │  Collector   │              │    Analyzer      │          │
│  │  Service     │              │    Service       │          │
│  │ (Port 8081)  │              │  (Port 8082)     │          │
│  │              │              │                  │          │
│  │ • MQTT Sub   │              │ • Anomaly Detect │          │
│  │ • REST API   │              │ • Health Score   │          │
│  └──────┬───────┘              └────────┬─────────┘          │
└─────────┼────────────────────────────────┼───────────────────┘
          │                                │
          ↓                                ↓
┌──────────────────────────────────────────────────────────────┐
│                    DATA LAYER                                 │
│  ┌─────────────────────────────────────────────────────┐     │
│  │  TimescaleDB (PostgreSQL 16 + TimescaleDB)         │     │
│  │  - Hypertables for time-series optimization        │     │
│  │  - Automatic partitioning               (Port 5432) │     │
│  └─────────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────┘
          ↑
          │ MQTT Messages
          │
┌──────────────────────────────────────────────────────────────┐
│                  MESSAGING LAYER                              │
│  ┌─────────────────────────────────────────────────────┐     │
│  │  Mosquitto MQTT Broker              (Port 1883)     │     │
│  └────────────────────┬────────────────────────────────┘     │
└────────────────────────┼─────────────────────────────────────┘
                         ↑
                         │ Publishes sensor data
                         │
                  ┌──────────────┐
                  │ MQTT Simulator│
                  │  (Node.js)    │
                  └───────────────┘
```

---

## 🛠️ Tech Stack

| Layer | Technologies | Purpose |
|-------|-------------|---------|
| **Backend** | Java 21, Spring Boot 3.2.0, Spring Cloud Gateway, Spring Security | Microservices foundation with modern Java |
| **Frontend** | React 18, TypeScript, Vite, Tailwind CSS, Recharts | Fast, modern UI with hot reload |
| **Database** | PostgreSQL 16, TimescaleDB | Time-series optimized storage |
| **Messaging** | Eclipse Mosquitto, Eclipse Paho | MQTT broker + client for IoT |
| **Monitoring** | Prometheus, Grafana, Micrometer | Metrics collection & visualization |
| **DevOps** | Docker, Docker Compose | Containerization & orchestration |
| **Simulation** | Node.js 20 | Generate realistic sensor data |
| **Security** | JWT, BCrypt | Stateless authentication |

---

## 📋 Services Overview

### 🚪 Gateway Service (Port 8080)
**Role**: API Gateway + Security Layer

**Responsibilities**:
- JWT token validation on every request
- Route requests to appropriate microservices
- CORS configuration for frontend
- Authentication endpoint (`/auth/login`)
- Centralized logging and monitoring

**Key Endpoints**:
- `POST /auth/login` - User authentication
- `GET /api/data/*` - Proxied to Collector Service
- `GET /api/analysis/*` - Proxied to Analyzer Service
- `GET /actuator/prometheus` - Metrics endpoint

---

### 📡 Collector Service (Port 8081)
**Role**: Data Ingestion Layer

**Responsibilities**:
- Subscribe to MQTT topics (`machine/#`)
- Receive sensor data from MQTT broker
- Persist data to TimescaleDB
- Provide REST API for querying historical data

**Key Endpoints**:
- `GET /api/data/recent?limit=100` - Latest N readings
- `GET /api/data/machine/{id}` - All data for a machine
- `GET /api/data/machine/{id}/recent?hours=24` - Recent data by time
- `GET /actuator/health` - Service health status

**Database Schema**:
```sql
CREATE TABLE machine_data (
  id BIGSERIAL,
  machine_id VARCHAR(50),
  timestamp TIMESTAMP NOT NULL,
  temperature DOUBLE PRECISION,
  vibration DOUBLE PRECISION,
  pressure DOUBLE PRECISION,
  speed INTEGER,
  status VARCHAR(20)
);

-- Convert to hypertable (TimescaleDB)
SELECT create_hypertable('machine_data', 'timestamp');
```

---

### 🧠 Analyzer Service (Port 8082)
**Role**: Data Processing & Anomaly Detection

**Responsibilities**:
- Fetch machine data from Collector Service
- Calculate health scores using statistical analysis
- Detect anomalies (temperature spikes, vibration issues)
- Return analysis results to Gateway

**Analysis Algorithm**:
1. Calculate mean and standard deviation
2. Compute z-scores for each metric
3. Flag values > 2σ as anomalies
4. Generate overall health score (0-100)

**Key Endpoints**:
- `GET /api/analysis/machine/{id}` - Health analysis for machine
- `GET /actuator/health` - Service health status

---

### 📊 React Dashboard (Port 5173)
**Role**: User Interface

**Features**:
- 🔐 Login page with JWT authentication
- 📈 Real-time machine status cards
- 📉 Interactive charts (temperature, vibration trends)
- 🎨 Dark/light theme support
- 📱 Responsive design

**Pages**:
- `/login` - Authentication
- `/dashboard` - Main overview with machine cards
- `/` - Auto-redirect based on auth status

---

### 📈 Prometheus + Grafana (Ports 9090, 3000)
**Role**: Observability & Monitoring

**Prometheus Metrics**:
- HTTP request rates and latencies
- JVM memory usage (heap, non-heap)
- CPU usage per service
- Custom business metrics

**Grafana Dashboards**:
- System overview (CPU, memory, requests/sec)
- Service health status
- Database query performance
- MQTT message throughput

---

## 🚀 Quick Start

### Prerequisites

```bash
# Verify installations
java -version        # Should show 21.x
node -version        # Should show 20.x
docker --version     # Should show 20.x+
docker-compose --version
```

### 1. Clone the Repository

```bash
git clone https://github.com/marwaniiwael18/Diagnet-microservices-platform.git
cd DiagNet
```

### 2. Start All Services

```bash
# Start entire stack (database, MQTT, backend services, monitoring)
docker-compose up -d

# View logs
docker-compose logs -f

# Check service health
docker-compose ps
```

### 3. Start Frontend (Development Mode)

```bash
cd frontend/react-dashboard
npm install
npm run dev
```

Frontend will be available at: **http://localhost:5173**

### 4. Access Services

| Service | URL | Credentials |
|---------|-----|-------------|
| **React Dashboard** | http://localhost:5173 | Username: `admin`, Password: `admin123` |
| **API Gateway** | http://localhost:8080 | JWT token required |
| **Grafana** | http://localhost:3000 | Username: `admin`, Password: `admin123` |
| **Prometheus** | http://localhost:9090 | No auth |
| **pgAdmin** | http://localhost:5050 | Email: `admin@diagnet.com`, Password: `admin123` |

### 5. Verify System is Running

```bash
# Check backend services health
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:8081/actuator/health  # Collector
curl http://localhost:8082/actuator/health  # Analyzer

# Test authentication
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# View Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

---

## 📊 Monitoring & Observability

### Grafana Dashboards

1. Open **http://localhost:3000**
2. Login with `admin` / `admin123`
3. Navigate to **Dashboards** → **DiagNet - System Overview**

**Available Metrics**:
- HTTP Requests per Second (by service)
- CPU Usage (%)
- JVM Memory Usage (heap/non-heap)
- Service Health Status (up/down)

### Prometheus Queries

Access **http://localhost:9090** and try these queries:

```promql
# HTTP request rate for gateway service
rate(http_server_requests_seconds_count{service="gateway"}[5m])

# JVM memory usage
jvm_memory_used_bytes{area="heap"}

# Service uptime
up{job=~".*-service"}

# Request latency (95th percentile)
histogram_quantile(0.95, 
  rate(http_server_requests_seconds_bucket[5m]))
```

---

## 🧪 Testing & Development

### Backend Testing

```bash
# Build all services
cd backend/microservices/gateway-service
./mvnw clean install

# Run tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=JwtUtilTest
```

### Frontend Testing

```bash
cd frontend/react-dashboard

# Run tests
npm test

# Build for production
npm run build
```

### MQTT Simulator

```bash
# Adjust simulation parameters
cd mqtt-simulator
nano .env  # Edit NUM_MACHINES, INTERVAL

# Restart simulator
docker-compose restart mqtt-simulator

# View simulator logs
docker logs -f diagnet-mqtt-simulator
```

---

## 🔧 Configuration

### Environment Variables

Create `.env` files in each service:

**Gateway Service**:
```env
JWT_SECRET=your-super-secret-key-min-512-bits
JWT_EXPIRATION=86400000
SERVER_PORT=8080
```

**Collector Service**:
```env
SPRING_DATASOURCE_URL=jdbc:postgresql://timescaledb:5432/diagnet_db
MQTT_BROKER_URL=tcp://mosquitto:1883
```

**Frontend**:
```env
VITE_API_URL=http://localhost:8080
```

### Docker Compose Overrides

Create `docker-compose.override.yml` for local development:

```yaml
version: '3.8'

services:
  gateway-service:
    environment:
      SPRING_PROFILES_ACTIVE: dev
      LOG_LEVEL: DEBUG
    volumes:
      - ./backend/microservices/gateway-service/target:/app
```

---

## 📚 API Documentation

### Authentication

**Login**
```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

Response:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "admin",
  "expiresIn": 86400000
}
```

### Machine Data

**Get Recent Data**
```http
GET /api/data/machine/MACHINE-001/recent?hours=24
Authorization: Bearer <token>

Response:
[
  {
    "id": 1,
    "machineId": "MACHINE-001",
    "timestamp": "2025-11-12T22:49:27",
    "temperature": 74.3,
    "vibration": 0.410,
    "pressure": 2.4,
    "speed": 1475,
    "status": "RUNNING"
  }
]
```

**Get Analysis**
```http
GET /api/analysis/machine/MACHINE-001
Authorization: Bearer <token>

Response:
{
  "machineId": "MACHINE-001",
  "healthScore": 85,
  "anomalies": [
    {
      "metric": "temperature",
      "value": 95.2,
      "threshold": 85.0,
      "severity": "HIGH"
    }
  ]
}
```

---

## 🐛 Troubleshooting

### Services Won't Start

```bash
# Check Docker resources
docker system df

# Remove old volumes and rebuild
docker-compose down -v
docker-compose up --build -d
```

### Database Connection Errors

```bash
# Check if TimescaleDB is ready
docker exec -it diagnet-timescaledb psql -U diagnet_user -d diagnet_db -c "SELECT version();"

# View database logs
docker logs diagnet-timescaledb
```

### MQTT Connection Issues

```bash
# Test MQTT broker
docker exec -it diagnet-mosquitto mosquitto_sub -t 'machine/#' -v

# Publish test message
docker exec -it diagnet-mosquitto mosquitto_pub -t 'machine/test' -m 'hello'
```

### Frontend Build Errors

```bash
cd frontend/react-dashboard

# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install

# Check for TypeScript errors
npm run build
```

---

## 🚀 Deployment

### Production Build

```bash
# Build optimized Docker images
docker-compose -f docker-compose.yml -f docker-compose.prod.yml build

# Tag images
docker tag diagnet-gateway-service:latest ghcr.io/marwaniiwael18/diagnet-gateway:v1.0

# Push to registry
docker push ghcr.io/marwaniiwael18/diagnet-gateway:v1.0
```

### Security Checklist

- [ ] Change default passwords in `.env`
- [ ] Use strong JWT secret (min 512 bits)
- [ ] Enable HTTPS with TLS certificates
- [ ] Configure firewall rules
- [ ] Enable Docker secrets for sensitive data
- [ ] Set up log aggregation
- [ ] Configure backup strategy for database

---

## 📖 Learning Resources

### Spring Boot Concepts Used
- **Spring Cloud Gateway**: Request routing, filters
- **Spring Security**: JWT authentication, WebFlux security
- **Spring Data JPA**: Repository pattern, query methods
- **Spring Actuator**: Health checks, metrics

### React Concepts Used
- **React Hooks**: useState, useEffect
- **Axios Interceptors**: Token injection, error handling
- **React Router**: Client-side routing
- **Tailwind CSS**: Utility-first styling

### DevOps Concepts Used
- **Docker Multi-stage Builds**: Optimize image size
- **Docker Compose**: Service orchestration
- **Health Checks**: Container readiness
- **Volume Mounts**: Data persistence

---

## 🤝 Contributing

Contributions welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License - see [LICENSE](LICENSE) file.

---

## 🙏 Acknowledgments

- **Spring Boot Team** - For the amazing framework
- **TimescaleDB** - For time-series database capabilities
- **Eclipse Foundation** - For Mosquitto MQTT broker
- **Grafana Labs** - For visualization tools

---

## 📞 Support

- 📧 Email: marwaniiwael18@gmail.com
- 🐛 Issues: [GitHub Issues](https://github.com/marwaniiwael18/Diagnet-microservices-platform/issues)
- 📖 Documentation: Check the `docs/` folder

---

**Built with ❤️ for learning and practicing modern software engineering**
mvn -version         # Should show 3.8+
node -version        # Should show 20.x
docker --version     # Should show 20.x+
```

See **[GETTING_STARTED.md](GETTING_STARTED.md)** for installation instructions.

### Running the Project (Coming Soon)

```bash
# Clone the repository
git clone https://github.com/marwaniiwael18/Diagnet-microservices-platform.git
cd DiagNet

# Start all services with Docker Compose
docker-compose up

# Access the services:
# - Frontend Dashboard: http://localhost:3000
# - API Gateway: http://localhost:8080
# - Grafana: http://localhost:3000/grafana
```

---

## 📂 Project Structure

```
DiagNet/
├── backend/
│   ├── microservices/
│   │   ├── collector-service/      # Data ingestion (Port 8081)
│   │   ├── analyzer-service/       # Anomaly detection (Port 8082)
│   │   └── gateway-service/        # API Gateway (Port 8080)
│   └── common/models/              # Shared DTOs
│
├── frontend/react-dashboard/       # React UI (Port 3000)
├── mqtt-simulator/                 # Machine data simulator
├── database/migrations/            # SQL schemas
├── observability/grafana/          # Monitoring dashboards
└── ci-cd/                          # GitHub Actions workflows
```

---

## 🎯 Current Progress

✅ **Step 1: Project Architecture** - Complete  
⏳ **Step 2: Collector Service** - In Progress  
⬜ **Step 3: Analyzer Service**  
⬜ **Step 4: Gateway Service**  
⬜ **Step 5: Database Setup**  
⬜ **Step 6: MQTT Simulator**  
⬜ **Step 7: React Dashboard**  
⬜ **Step 8: Docker Configuration**  
⬜ **Step 9: CI/CD Pipeline**  
⬜ **Step 10: Observability**  

See **[ROADMAP.md](ROADMAP.md)** for detailed timeline.

---

## 🎓 Learning Objectives

This project teaches:

- ✅ Microservices architecture and communication patterns
- ✅ RESTful API design and implementation
- ✅ MQTT protocol for IoT communication
- ✅ Time-series database optimization
- ✅ JWT/OAuth2 security implementation
- ✅ Docker containerization and orchestration
- ✅ CI/CD pipeline configuration
- ✅ Frontend-backend integration
- ✅ Real-time data visualization
- ✅ Production monitoring and observability

**Perfect preparation for a backend/full-stack developer internship!** 🚀

---

## 🤝 Contributing

This is a learning project, but contributions and suggestions are welcome!

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Wael Marwani**

- GitHub: [@marwaniiwael18](https://github.com/marwaniiwael18)
- Repository: [Diagnet-microservices-platform](https://github.com/marwaniiwael18/Diagnet-microservices-platform)

---

## 🙏 Acknowledgments

- Inspired by real-world industrial IoT systems
- Built with modern enterprise technologies
- Designed for learning and skill development

---

## 📞 Support

Need help? Check out:
- **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** for common commands and troubleshooting
- **[GETTING_STARTED.md](GETTING_STARTED.md)** for detailed explanations
- Open an issue on GitHub for bugs or questions

---

**Happy Coding!** 🎉 Let's build something amazing together!

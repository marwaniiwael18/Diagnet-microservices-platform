# 🏗️ DiagNet Architecture Overview

## 📁 Project Structure Explained

```
DiagNet/
├── backend/                          # Java/Spring Boot services
│   ├── microservices/               # Independent services (microservices pattern)
│   │   ├── collector-service/       # 🔌 Data Ingestion Service
│   │   │   └── Purpose: Receives machine data via MQTT & REST
│   │   │        - Subscribes to MQTT topics (machine/*)
│   │   │        - Exposes REST endpoints for manual data push
│   │   │        - Validates and stores data in TimescaleDB
│   │   │
│   │   ├── analyzer-service/        # 🧠 Analytics Engine
│   │   │   └── Purpose: Processes data and detects anomalies
│   │   │        - Calculates health scores
│   │   │        - Detects anomalies (z-score, moving averages)
│   │   │        - Generates alerts when issues detected
│   │   │
│   │   └── gateway-service/         # 🔐 API Gateway
│   │       └── Purpose: Single entry point for frontend
│   │            - Handles authentication (OAuth2/JWT)
│   │            - Routes requests to appropriate services
│   │            - Implements rate limiting & security
│   │
│   └── common/                      # 🔗 Shared Code
│       └── models/                  # DTOs, entities shared across services
│
├── frontend/                        # React application
│   └── react-dashboard/             # 📊 Web UI
│       └── Purpose: Visualize machine data & alerts
│            - Real-time charts and graphs
│            - Alert management
│            - Machine health monitoring
│
├── mqtt-simulator/                  # 🤖 Machine Simulator
│   └── Purpose: Simulate industrial machines
│        - Generates realistic sensor data
│        - Publishes to MQTT broker
│        - Simulates normal & anomaly scenarios
│
├── database/                        # 💾 Database Setup
│   └── migrations/                  # SQL schema and migrations
│       └── Purpose: Version-controlled database changes
│
├── observability/                   # 📈 Monitoring Setup
│   └── grafana/                     # Grafana dashboards & configs
│       └── Purpose: System monitoring and metrics visualization
│
└── ci-cd/                          # 🚀 Automation
    └── Purpose: GitHub Actions workflows for CI/CD
```

---

## 🎯 Why This Architecture?

### 1. **Microservices Pattern**
- **Why?** Each service has a single responsibility and can scale independently
- **Benefit:** If data collection increases, we only scale the collector-service
- **Real-world:** This is how Netflix, Uber, and Amazon build their systems

### 2. **Separation of Concerns**
- **Collector**: Only cares about receiving data (doesn't analyze it)
- **Analyzer**: Only cares about processing data (doesn't collect it)
- **Gateway**: Only cares about security and routing (doesn't process data)
- **Benefit:** Easy to maintain, test, and update each service independently

### 3. **Time-Series Database (TimescaleDB)**
- **Why not regular PostgreSQL?** Time-series data has special needs:
  - Millions of readings per day
  - Need fast queries like "average temperature in last hour"
  - Automatic data retention (delete old data)
- **TimescaleDB** = PostgreSQL + time-series superpowers
- **Benefit:** 10-100x faster queries for time-based data

### 4. **MQTT Protocol**
- **Why MQTT?** Designed for IoT and industrial systems
  - Lightweight (works on slow networks)
  - Publish/Subscribe pattern (machines don't need to know who's listening)
  - Reliable delivery
- **Alternative:** REST API works but wastes bandwidth for continuous data

### 5. **Docker & Containerization**
- **Why?** "It works on my machine" problem solved
- **Benefit:** 
  - Same environment everywhere (dev, test, production)
  - Easy deployment
  - Resource isolation

---

## 🔄 Data Flow Diagram

```
┌─────────────┐
│   Machine   │ (Simulated)
│  Simulator  │
└──────┬──────┘
       │ Publishes sensor data
       │ (MQTT: machine/1/data)
       ↓
┌─────────────┐
│ MQTT Broker │ (Mosquitto)
└──────┬──────┘
       │ Subscribers receive data
       ↓
┌─────────────┐
│  Collector  │ (Spring Boot)
│   Service   │ - Validates data
└──────┬──────┘ - Stores in DB
       │
       ↓
┌─────────────┐
│ TimescaleDB │ (Time-series storage)
└──────┬──────┘
       │ Reads recent data
       ↓
┌─────────────┐
│  Analyzer   │ (Spring Boot)
│   Service   │ - Detects anomalies
└──────┬──────┘ - Calculates health scores
       │
       ↓
┌─────────────┐
│   Gateway   │ (Spring Boot + Security)
│   Service   │ - Authenticates users
└──────┬──────┘ - Routes API calls
       │
       ↓
┌─────────────┐
│   React     │ (Frontend)
│  Dashboard  │ - Displays charts
└─────────────┘ - Shows alerts
```

---

## 🔐 Security Layers

1. **JWT Tokens**: User logs in → receives token → uses for API calls
2. **OAuth2**: Industry standard for authentication
3. **API Gateway**: All requests go through one secure entry point
4. **Environment Variables**: Secrets never in code

---

## 📊 Observability Stack

- **OpenTelemetry**: Collects metrics, traces, logs from all services
- **Prometheus**: Stores metrics (CPU, memory, request counts)
- **Grafana**: Beautiful dashboards to visualize everything

**Why?** In production, you need to know:
- Is service healthy?
- Why is it slow?
- What happened before the crash?

---

## 🚀 CI/CD Pipeline

```
Code Push → GitHub Actions → Run Tests → Build Docker Images → Deploy
```

**Why?** Automated testing prevents bugs from reaching production.

---

## 📚 Technology Choices Explained

| Technology | Why This One? | Alternative |
|------------|---------------|-------------|
| **Spring Boot** | Industry standard for Java backend, auto-configuration, huge ecosystem | Node.js/Express (less structure) |
| **React** | Most popular frontend framework, great for real-time dashboards | Vue/Angular (smaller communities) |
| **TimescaleDB** | Best time-series DB that's also PostgreSQL-compatible | InfluxDB (different query language) |
| **Docker** | Containerization standard, works everywhere | VM (heavier, slower) |
| **MQTT** | Built for IoT, minimal bandwidth | WebSockets (more overhead) |

---

## 🎓 Learning Path (Skills You'll Gain)

1. ✅ Microservices architecture
2. ✅ RESTful API design
3. ✅ MQTT protocol & IoT communication
4. ✅ Time-series database optimization
5. ✅ JWT/OAuth2 security
6. ✅ Docker & containerization
7. ✅ CI/CD pipelines
8. ✅ Frontend-backend integration
9. ✅ Real-time data visualization
10. ✅ Production monitoring & observability

---

## 📖 Next Steps

We'll build each component step-by-step:
1. Start with collector-service (simplest)
2. Add database layer
3. Build analyzer-service
4. Add gateway for security
5. Create frontend dashboard
6. Containerize everything
7. Add CI/CD

Each step builds on the previous one, so you'll understand how everything connects!

# 🏭 DiagNet - Industrial Diagnostic Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> A full-stack, containerized industrial diagnostic platform designed to monitor and analyze machine performance in real-time.

---

## 🎯 What is DiagNet?

DiagNet simulates an **industrial diagnostic box** that connects to machines (sensors/controllers) via MQTT or REST, collects real-time data, stores it in a time-series database, analyzes it for anomalies, and displays results on a secure dashboard.

**Think of it as**: A health monitoring system for industrial machines, like a Fitbit for factories! 🏭

---

## ✨ Features

- ⚡ **Real-time Data Ingestion** via MQTT & REST APIs
- 🧠 **Anomaly Detection** using statistical analysis
- 📊 **Time-Series Storage** with TimescaleDB optimization
- 🔐 **Secure API Gateway** with OAuth2 & JWT authentication
- 📈 **Interactive Dashboards** with real-time charts
- 📦 **Fully Containerized** with Docker Compose
- 🚀 **CI/CD Pipeline** with GitHub Actions
- 🛰️ **Observability** via OpenTelemetry + Prometheus + Grafana

---

## 🏗️ Architecture

```
┌─────────────┐
│  Machines   │ (Simulated via MQTT)
└──────┬──────┘
       │
       ↓
┌─────────────┐
│   MQTT      │
│   Broker    │
└──────┬──────┘
       │
       ↓
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Collector  │────→│    Time     │←────│  Analyzer   │
│   Service   │     │  Series DB  │     │   Service   │
└─────────────┘     └─────────────┘     └─────────────┘
                                               │
                                               ↓
                                        ┌─────────────┐
                                        │   Gateway   │
                                        │   Service   │
                                        └──────┬──────┘
                                               │
                                               ↓
                                        ┌─────────────┐
                                        │    React    │
                                        │  Dashboard  │
                                        └─────────────┘
```

---

## 🛠️ Tech Stack

| Category | Technologies |
|----------|-------------|
| **Backend** | Java 21 (LTS), Spring Boot 3.x, Spring Security, Spring Data JPA |
| **Frontend** | React 18, Vite, Tailwind CSS, Recharts |
| **Database** | PostgreSQL, TimescaleDB (time-series extension) |
| **Messaging** | Eclipse Mosquitto (MQTT Broker), Eclipse Paho (MQTT Client) |
| **Monitoring** | Grafana, Prometheus, OpenTelemetry |
| **DevOps** | Docker, Docker Compose, GitHub Actions |
| **Simulation** | Node.js 20+ (MQTT data generator) |

---

## � Documentation

| Document | Description |
|----------|-------------|
| **[ARCHITECTURE.md](ARCHITECTURE.md)** | System design, data flow, and technology choices explained |
| **[GETTING_STARTED.md](GETTING_STARTED.md)** | Installation guide, prerequisites, and core concepts |
| **[ROADMAP.md](ROADMAP.md)** | Development timeline, learning objectives, and progress tracker |
| **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** | Commands, debugging tips, and troubleshooting guide |
| **[STEP_1_COMPLETE.md](STEP_1_COMPLETE.md)** | Current progress and next steps |

---

## 🚀 Quick Start

### Prerequisites

```bash
# Check if you have these installed:
java -version        # Should show 21.x (LTS)
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

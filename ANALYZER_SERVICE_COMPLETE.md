# ✅ Step 2.2: Analyzer Service - COMPLETE

## 📊 What Was Built

**Analyzer Service** - Anomaly detection microservice that analyzes machine data

### Port: 8082

---

## 🏗️ Architecture

```
DataFetcherService → AnomalyDetectionService → AnalysisController
        ↓                      ↓                        ↓
   Calls REST API      Analyzes Data              Returns Results
   (collector:8081)    (Z-score + Thresholds)     (JSON Response)
```

---

## 📝 Files Created

1. **`pom.xml`** - Java 21, Spring Boot 3.2, WebFlux, Apache Commons Math
2. **`application.yml`** - Config (port 8082, thresholds, collector URL)
3. **`AnalyzerApplication.java`** - Main Spring Boot entry point
4. **Models:**
   - `MachineDataDTO.java` - Data from collector
   - `AnalysisResult.java` - Response with health score & anomalies
5. **Services:**
   - `DataFetcherService.java` - Fetches data from collector-service
   - `AnomalyDetectionService.java` - Detects anomalies (Z-score + thresholds)
6. **Controller:**
   - `AnalysisController.java` - REST API endpoint

---

## 🧠 How Anomaly Detection Works

### Two Methods:

**1. Threshold Checking** (Fixed Limits)
- Temperature > 100°C = CRITICAL
- Temperature > 90°C = WARNING
- Vibration > 0.8 = CRITICAL
- Vibration > 0.7 = WARNING

**2. Z-Score Analysis** (Statistical)
```
Z-score = (value - mean) / standard_deviation
```
- If |Z-score| > 3 → Anomaly (value is 3σ away from mean)
- Detects unusual patterns even if below thresholds

### Health Score Calculation:
- Start: 100
- Subtract 20 for each CRITICAL anomaly
- Subtract 5 for each WARNING anomaly
- Result: 0-100 scale

### Status:
- 80-100 = HEALTHY
- 50-79 = WARNING
- 0-49 = CRITICAL

---

## 🌐 API Endpoint

### Analyze Machine
```bash
GET http://localhost:8082/api/analysis/machine/{machineId}?hours=24
```

**Example Response:**
```json
{
  "machineId": "M001",
  "analyzedAt": "2025-11-11T21:30:00",
  "healthScore": 85.0,
  "status": "HEALTHY",
  "anomalies": [
    {
      "type": "TEMPERATURE",
      "severity": "WARNING",
      "value": 92.5,
      "threshold": 90.0,
      "message": "Temperature warning: 92.5°C",
      "detectedAt": "2025-11-11T20:15:00"
    }
  ],
  "statistics": {
    "avgTemperature": 75.3,
    "maxTemperature": 92.5,
    "avgVibration": 0.4,
    "maxVibration": 0.6,
    "dataPointsAnalyzed": 120
  }
}
```

---

## 🔗 Dependencies

- **Spring Boot Starter Web** - REST API
- **Spring Boot Starter WebFlux** - WebClient for HTTP calls
- **Apache Commons Math3** - Statistical calculations
- **Lombok** - Code generation
- **PostgreSQL** - Database (shared with collector)

---

## ✅ Build Status

```
[INFO] BUILD SUCCESS
[INFO] Compiling 6 source files with Java 21
[INFO] Total time:  13.177 s
```

---

## 🎯 What You Learned

- **Microservice Communication** - Service calls another service via REST
- **WebClient** - Spring's reactive HTTP client
- **Statistical Analysis** - Z-score for anomaly detection
- **Business Logic** - Health scoring algorithm
- **DTO Pattern** - Data transfer between services
- **Builder Pattern** - Clean object construction with Lombok

---

## 🚀 Next Steps

✅ Step 1: Project Architecture  
✅ Step 2.1: Collector Service  
✅ Step 2.2: Analyzer Service ← **YOU ARE HERE**  
⏳ Step 2.3: Gateway Service (API Gateway + OAuth2)  
⏳ Step 3: Database (TimescaleDB)  
⏳ Step 4: MQTT Simulator  

---

**Status:** ✅ Analyzer Service is ready! Can analyze data once collector is running.

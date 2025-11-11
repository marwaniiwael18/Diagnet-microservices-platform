# MQTT Simulator for DiagNet

Simulates industrial machines sending real-time sensor data via MQTT protocol.

## Features

- 🏭 **Virtual Machines**: Simulates multiple industrial machines
- 📊 **Realistic Data**: Temperature, vibration, pressure, humidity, power, rotation speed
- ⚠️ **Anomaly Simulation**: Random warnings and critical alerts
- 🔄 **Continuous Publishing**: Sends data at configurable intervals
- 📡 **MQTT Protocol**: Industry-standard IoT communication

## Prerequisites

1. **Node.js** (v16 or higher)
2. **MQTT Broker** (Mosquitto) running on `localhost:1883`

## Quick Start

### 1. Install Dependencies
```bash
cd /Users/macbook/Desktop/DiagNet/mqtt-simulator
npm install
```

### 2. Start MQTT Broker (Mosquitto)

**Option A: Using Homebrew (macOS)**
```bash
# Install (if not installed)
brew install mosquitto

# Start broker
brew services start mosquitto

# Check status
brew services list | grep mosquitto
```

**Option B: Using Docker**
```bash
docker run -d --name mosquitto -p 1883:1883 eclipse-mosquitto
```

### 3. Run Simulator
```bash
npm start
```

## Usage Examples

### Default (3 machines, 10 second interval)
```bash
npm start
```

### Single Machine (for testing)
```bash
npm run start:single
```

### Multiple Machines
```bash
npm start -- --machines=5
```

### Fast Publishing (5 seconds)
```bash
npm start -- --interval=5000
```

### Custom Configuration
```bash
node simulator.js --machines=10 --interval=3000
```

### Different MQTT Broker
```bash
MQTT_BROKER=mqtt://192.168.1.100:1883 npm start
```

## Sample Output

```
🚀 DiagNet MQTT Simulator Starting...

Configuration:
  Broker:     mqtt://localhost:1883
  Machines:   3
  Interval:   10000ms (10s)
  Client ID:  mqtt-simulator-a3f5c8

✅ Initialized 3 virtual machines
🔌 Connecting to MQTT broker at mqtt://localhost:1883...
✅ Connected to MQTT broker!

📊 Starting data simulation...

🟢 MACHINE-001 | Temp: 75.2°C | Vib: 0.38 | Status: RUNNING
🟢 MACHINE-002 | Temp: 72.8°C | Vib: 0.42 | Status: RUNNING
🟢 MACHINE-003 | Temp: 78.5°C | Vib: 0.35 | Status: RUNNING

⚠️  MACHINE-002 entering anomalous state!
⚠️  MACHINE-002 | Temp: 91.3°C | Vib: 0.68 | Status: WARNING

🔴 MACHINE-002 | Temp: 108.7°C | Vib: 0.92 | Status: CRITICAL
```

## MQTT Topics

The simulator publishes to these topics:

```
machine/MACHINE-001/data
machine/MACHINE-002/data
machine/MACHINE-003/data
...
```

### Message Format (JSON)

```json
{
  "machineId": "MACHINE-001",
  "timestamp": "2025-11-11T22:30:00",
  "temperature": 75.2,
  "vibration": 0.38,
  "pressure": 2.15,
  "humidity": 45.3,
  "powerConsumption": 152.5,
  "rotationSpeed": 1498,
  "status": "RUNNING",
  "location": "Factory Floor A"
}
```

## Machine Status Types

- **RUNNING**: Normal operation (temperature < 85°C, vibration < 0.6)
- **WARNING**: Elevated readings (temp 85-100°C, vib 0.6-0.8)
- **CRITICAL**: Dangerous levels (temp > 100°C, vib > 0.8)

## Anomaly Behavior

- **5% chance** per cycle of entering anomalous state
- Anomaly lasts **3-7 cycles** (30-70 seconds with default interval)
- During anomaly:
  - 30% chance of **WARNING** (moderate elevation)
  - 70% chance of **CRITICAL** (severe elevation)
- Automatically returns to normal after anomaly period

## Testing Data Flow

### 1. Subscribe to MQTT Topics (separate terminal)
```bash
# Install mosquitto_sub
brew install mosquitto

# Subscribe to all machine topics
mosquitto_sub -h localhost -t "machine/+/data" -v
```

### 2. View in Collector Service Logs
```bash
tail -f /Users/macbook/Desktop/DiagNet/backend/microservices/collector-service/collector.log
```

### 3. Query Database
```bash
docker exec diagnet-timescaledb psql -U diagnet_user -d diagnet_db -c "SELECT machine_id, timestamp, temperature, vibration, status FROM machine_data ORDER BY timestamp DESC LIMIT 10;"
```

### 4. Check via REST API
```bash
curl -s "http://localhost:8081/api/data/machine/MACHINE-001" | python3 -m json.tool
```

## Troubleshooting

### "MQTT Connection Error"
```bash
# Check if Mosquitto is running
brew services list | grep mosquitto

# Or check the port
lsof -i :1883

# Start if not running
brew services start mosquitto
```

### "Connection Refused"
- Verify MQTT broker is running: `telnet localhost 1883`
- Check firewall settings
- Try Docker: `docker run -d -p 1883:1883 eclipse-mosquitto`

### No Data in Collector Service
- Check collector service logs: Does it show MQTT connection?
- Verify MQTT config in collector's `application.yml`
- Ensure broker URL matches: `tcp://localhost:1883`

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `MQTT_BROKER` | `mqtt://localhost:1883` | MQTT broker URL |
| `MQTT_USERNAME` | `admin` | MQTT username |
| `MQTT_PASSWORD` | `admin` | MQTT password |

## Architecture

```
MQTT Simulator (Node.js)
    ↓ (publishes to)
MQTT Broker (Mosquitto)
    ↓ (subscribes from)
Collector Service (Java/Spring Boot)
    ↓ (saves to)
TimescaleDB (PostgreSQL)
```

## Stopping the Simulator

Press `Ctrl+C` - the simulator will gracefully disconnect and shut down.

## Next Steps

After starting the simulator:
1. Verify data is being received by collector service
2. Check database for new records
3. Test analyzer service with real-time data
4. View data in pgAdmin
5. Build React dashboard to visualize

---

*Created: 2025-11-11*  
*Part of DiagNet Microservices Platform*

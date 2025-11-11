# DiagNet Database Setup - TimescaleDB

## Overview
Time-series database for storing industrial machine sensor data using **TimescaleDB** (PostgreSQL extension optimized for time-series workloads).

**Database:** PostgreSQL 16 + TimescaleDB  
**Port:** 5432  
**Management UI:** pgAdmin (port 5050)

---

## Features

### TimescaleDB Optimizations
- ✅ **Hypertables**: Automatic time-based partitioning (7-day chunks)
- ✅ **Compression**: Automatic compression for data older than 30 days
- ✅ **Continuous Aggregates**: Pre-computed hourly and daily statistics
- ✅ **Automatic Refresh**: Aggregates update every hour/day
- ✅ **Indexes**: Optimized for machine_id and time-range queries

### Schema Design
```sql
machine_data (main table)
├── id (BIGSERIAL)
├── machine_id (VARCHAR) - Machine identifier
├── timestamp (TIMESTAMPTZ) - Partition key
├── temperature (DOUBLE PRECISION)
├── vibration (DOUBLE PRECISION)
├── pressure (DOUBLE PRECISION)
├── humidity (DOUBLE PRECISION)
├── power_consumption (DOUBLE PRECISION)
├── rotation_speed (DOUBLE PRECISION)
├── status (VARCHAR) - RUNNING/WARNING/CRITICAL
├── location (VARCHAR)
└── metadata (JSONB) - Extensible JSON data
```

---

## Quick Start

### 1. Start Database
```bash
cd /Users/macbook/Desktop/DiagNet/database
docker-compose up -d
```

**Services Started:**
- TimescaleDB: `localhost:5432`
- pgAdmin: `http://localhost:5050`

### 2. Verify Database is Running
```bash
docker ps | grep diagnet-timescaledb
```

**Expected:** Container running with healthy status

### 3. Check Logs
```bash
docker logs diagnet-timescaledb
```

**Look for:** "database system is ready to accept connections"

---

## Connection Details

### Database Connection
```
Host: localhost
Port: 5432
Database: diagnet_db
Username: diagnet_user
Password: diagnet_password
```

### JDBC URL (for Spring Boot)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/diagnet_db
spring.datasource.username=diagnet_user
spring.datasource.password=diagnet_password
```

---

## Migration Script

The migration script (`V1__create_machine_data_table.sql`) is **automatically executed** when the database starts for the first time.

### What it Creates:

1. **Main Table**: `machine_data` with time-series sensor columns
2. **Hypertable**: Converts table to TimescaleDB hypertable (7-day chunks)
3. **Indexes**: 
   - `idx_machine_data_machine_id` - Fast machine lookups
   - `idx_machine_data_timestamp` - Time-range queries
   - `idx_machine_data_status` - Status filtering
   - `idx_machine_data_machine_time` - Composite index
4. **Compression**: Auto-compress data older than 30 days
5. **Continuous Aggregates**: 
   - `machine_data_hourly` - Hourly statistics
   - `machine_data_daily` - Daily statistics
6. **Sample Data**: 10 test records for 3 machines

---

## Using pgAdmin (Database Management UI)

### 1. Open pgAdmin
```
http://localhost:5050
```

### 2. Login
- **Email:** admin@diagnet.com
- **Password:** admin123

### 3. Add Server
Right-click "Servers" → Register → Server

**General Tab:**
- Name: `DiagNet TimescaleDB`

**Connection Tab:**
- Host: `timescaledb` (Docker internal network)
- Port: `5432`
- Database: `diagnet_db`
- Username: `diagnet_user`
- Password: `diagnet_password`

### 4. Browse Data
Navigate: Servers → DiagNet TimescaleDB → Databases → diagnet_db → Schemas → public → Tables → machine_data

---

## Testing the Database

### Test 1: Connect with psql
```bash
docker exec -it diagnet-timescaledb psql -U diagnet_user -d diagnet_db
```

### Test 2: Query Sample Data
```sql
-- Count records
SELECT COUNT(*) FROM machine_data;

-- Get latest data per machine
SELECT machine_id, 
       MAX(timestamp) as last_update,
       status
FROM machine_data 
GROUP BY machine_id, status
ORDER BY machine_id;

-- Get temperature readings for MACHINE-001
SELECT timestamp, temperature, vibration, status
FROM machine_data
WHERE machine_id = 'MACHINE-001'
ORDER BY timestamp DESC
LIMIT 10;
```

### Test 3: Query Hourly Aggregates
```sql
SELECT 
    machine_id,
    bucket,
    avg_temperature,
    max_temperature,
    data_points
FROM machine_data_hourly
ORDER BY bucket DESC
LIMIT 10;
```

### Test 4: Check TimescaleDB Info
```sql
-- Show hypertable info
SELECT * FROM timescaledb_information.hypertables;

-- Show chunks
SELECT * FROM timescaledb_information.chunks 
WHERE hypertable_name = 'machine_data';

-- Show compression stats
SELECT * FROM timescaledb_information.compression_settings;
```

---

## Performance Features

### 1. Time-Based Partitioning (Automatic)
- Data automatically split into 7-day chunks
- Old chunks can be dropped efficiently
- Parallel query execution across chunks

### 2. Compression (Automated)
- **Policy**: Compress chunks older than 30 days
- **Compression Ratio**: Typically 90-95% size reduction
- **Segmented by**: `machine_id` (maintains query performance)

### 3. Continuous Aggregates (Pre-computed)
- **Hourly Stats**: Updated every hour
- **Daily Stats**: Updated every day
- **Much faster** than computing aggregates on-the-fly

### 4. Optimized Indexes
```sql
-- Query by machine (fast)
SELECT * FROM machine_data 
WHERE machine_id = 'MACHINE-001' 
  AND timestamp > NOW() - INTERVAL '1 day';

-- Query by time range (fast)
SELECT * FROM machine_data 
WHERE timestamp BETWEEN '2025-11-01' AND '2025-11-10';

-- Query by status (fast)
SELECT * FROM machine_data 
WHERE status = 'CRITICAL';
```

---

## Common Operations

### Insert New Data
```sql
INSERT INTO machine_data (
    machine_id, timestamp, temperature, vibration, 
    pressure, humidity, power_consumption, rotation_speed, status, location
)
VALUES (
    'MACHINE-004', NOW(), 80.5, 0.55, 2.4, 50.0, 160.0, 1520.0, 'RUNNING', 'Factory Floor D'
);
```

### Query Recent Data (Last 24 Hours)
```sql
SELECT * FROM machine_data
WHERE timestamp > NOW() - INTERVAL '24 hours'
ORDER BY timestamp DESC;
```

### Query Anomalies
```sql
SELECT * FROM machine_data
WHERE status IN ('WARNING', 'CRITICAL')
  AND timestamp > NOW() - INTERVAL '7 days'
ORDER BY timestamp DESC;
```

### Get Machine Health Summary
```sql
SELECT 
    machine_id,
    COUNT(*) as total_readings,
    AVG(temperature) as avg_temp,
    MAX(temperature) as max_temp,
    AVG(vibration) as avg_vibration,
    MAX(vibration) as max_vibration,
    COUNT(CASE WHEN status = 'CRITICAL' THEN 1 END) as critical_count,
    COUNT(CASE WHEN status = 'WARNING' THEN 1 END) as warning_count
FROM machine_data
WHERE timestamp > NOW() - INTERVAL '24 hours'
GROUP BY machine_id;
```

### Clean Up Old Data (Manual)
```sql
-- Delete data older than 90 days
SELECT cleanup_old_data(90);
```

---

## Updating Spring Boot Services

### Collector Service (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/diagnet_db
    username: diagnet_user
    password: diagnet_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate  # Don't let JPA manage schema
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          time_zone: UTC
```

### Analyzer Service (application.yml)
No database connection needed - it fetches data via REST from collector-service.

---

## Stopping the Database

### Stop Containers (Data Persists)
```bash
cd /Users/macbook/Desktop/DiagNet/database
docker-compose stop
```

### Stop and Remove Containers (Data Persists)
```bash
docker-compose down
```

### Complete Cleanup (DELETES ALL DATA)
```bash
docker-compose down -v
```

---

## Troubleshooting

### Port 5432 Already in Use
```bash
# Check what's using port 5432
lsof -i :5432

# Option 1: Stop existing PostgreSQL
brew services stop postgresql

# Option 2: Change port in docker-compose.yml
ports:
  - "5433:5432"  # Use 5433 on host
```

### Migration Script Not Running
```bash
# Check if script is mounted correctly
docker exec diagnet-timescaledb ls -la /docker-entrypoint-initdb.d/

# Manually run migration
docker exec -i diagnet-timescaledb psql -U diagnet_user -d diagnet_db < migrations/V1__create_machine_data_table.sql
```

### Database Connection Refused
```bash
# Check container status
docker ps -a | grep timescaledb

# Check logs
docker logs diagnet-timescaledb

# Restart container
docker-compose restart timescaledb
```

---

## Next Steps

### ✅ Step 3: Database Setup - COMPLETE
- TimescaleDB running with hypertables
- Migration script with sample data
- Continuous aggregates for analytics
- pgAdmin for database management

### 🔜 Step 4: Update Collector Service
- Update `application.yml` with database credentials
- Verify JPA entity matches schema
- Test database insert operations
- Restart collector service

### 🔜 Step 5: MQTT Simulator
- Create Node.js script to publish sensor data
- Connect to MQTT broker
- Send realistic machine data
- Verify collector receives and stores data

---

## Database Maintenance

### Backup Database
```bash
docker exec diagnet-timescaledb pg_dump -U diagnet_user diagnet_db > backup.sql
```

### Restore Database
```bash
cat backup.sql | docker exec -i diagnet-timescaledb psql -U diagnet_user -d diagnet_db
```

### Monitor Database Size
```sql
SELECT 
    pg_size_pretty(pg_database_size('diagnet_db')) as database_size,
    pg_size_pretty(pg_total_relation_size('machine_data')) as table_size;
```

---

*Created: 2025-11-11*  
*Status: Ready for Integration*  
*Database: TimescaleDB (PostgreSQL 16)*

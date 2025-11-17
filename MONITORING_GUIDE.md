# DiagNet Monitoring Setup Guide

## 🎯 Quick Access

### **Grafana** - Metrics Visualization
- **URL**: http://localhost:3000
- **Username**: `admin`
- **Password**: `admin123`

### **Prometheus** - Metrics Collection
- **URL**: http://localhost:9090
- **Query Interface**: http://localhost:9090/graph
- **Targets Status**: http://localhost:9090/targets

### **React Dashboard** - Machine Monitoring
- **URL**: http://localhost:5173
- **Admin Login**: `admin` / `admin123`
- **User Login**: `user` / `user123`

---

## 📊 Grafana Dashboards

After logging into Grafana (admin/admin123), you'll find **3 pre-configured dashboards**:

### 1. **DiagNet - Microservices Overview** ⭐
   - **Purpose**: Real-time monitoring of all microservices
   - **Panels**:
     - HTTP Requests per second (by service)
     - Service Health Status (3 services)
     - Response Time (p95) for each service
     - JVM Heap Memory Usage
     - CPU Usage by Service
     - Gateway Total Requests
     - Collector Data Ingestion count
     - Analyzer Computation count
   - **Auto-refresh**: Every 10 seconds
   - **Time Range**: Last 15 minutes

### 2. **DiagNet - System Health** 🏥
   - **Purpose**: Deep dive into system health metrics
   - **Panels**:
     - Service Status (UP/DOWN indicator)
     - HTTP Error Rate (4xx, 5xx)
     - Active Threads for each service
     - JVM Garbage Collection Rate
   - **Auto-refresh**: Every 10 seconds
   - **Time Range**: Last 30 minutes

### 3. **DiagNet Overview** (Original)
   - Basic metrics overview

---

## 🚀 Getting Started with Grafana

### Step 1: Login
1. Open http://localhost:3000
2. Enter credentials: `admin` / `admin123`
3. Click "Log in"

### Step 2: View Dashboards
1. Click on **"Dashboards"** in the left sidebar (📊 icon)
2. You'll see all 3 DiagNet dashboards
3. Click on **"DiagNet - Microservices Overview"** to start

### Step 3: Explore Metrics
- **Zoom in/out**: Click and drag on any graph
- **Change time range**: Use the time picker in top-right
- **Pause auto-refresh**: Click the refresh dropdown
- **See specific service**: Hover over legend items

---

## 📈 Prometheus Queries

### Access Prometheus
1. Open http://localhost:9090
2. Click on **"Query"** tab (already selected)
3. Type any query and click **"Execute"**

### Useful Queries

#### Check if services are up:
```promql
up{job=~"gateway-service|collector-service|analyzer-service"}
```

#### HTTP Request Rate (last 5 minutes):
```promql
sum(rate(http_server_requests_seconds_count[5m])) by (job)
```

#### Memory Usage:
```promql
jvm_memory_used_bytes{area="heap"}
```

#### CPU Usage:
```promql
process_cpu_usage
```

#### Response Time (95th percentile):
```promql
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (job, le)) * 1000
```

#### Error Rate:
```promql
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (job)
```

### Verify All Targets Are UP
1. Click **"Status"** → **"Targets"**
2. You should see 4 targets, all with status **"UP"**:
   - `gateway-service` (http://gateway-service:8080/actuator/prometheus)
   - `collector-service` (http://collector-service:8081/actuator/prometheus)
   - `analyzer-service` (http://analyzer-service:8082/actuator/prometheus)
   - `prometheus` (http://localhost:9090/metrics)

---

## 🎨 Customizing Dashboards

### Add a New Panel
1. Open any dashboard
2. Click **"Add"** → **"Visualization"**
3. Select **"Prometheus"** as data source
4. Enter a PromQL query
5. Configure visualization type (Time series, Gauge, Stat, etc.)
6. Click **"Apply"**

### Edit Existing Panel
1. Hover over any panel title
2. Click the dropdown menu (⋮)
3. Select **"Edit"**
4. Modify query or visualization settings
5. Click **"Apply"**

### Save Changes
- Click **"Save dashboard"** icon (💾) in top-right
- Add a description of your changes
- Click **"Save"**

---

## 🔍 Monitoring Checklist

### Daily Health Check
- [ ] All 3 services show **UP** in Grafana
- [ ] HTTP error rate is < 1%
- [ ] Response times are < 500ms (p95)
- [ ] CPU usage is < 80%
- [ ] Memory usage is stable (no continuous growth)

### When Something Goes Wrong
1. **Check Service Status**:
   - Grafana: "DiagNet - System Health" dashboard
   - Prometheus: Status → Targets
   
2. **Check Logs**:
   ```bash
   # Gateway logs
   docker logs diagnet-gateway --tail 100
   
   # Collector logs
   docker logs diagnet-collector --tail 100
   
   # Analyzer logs
   docker logs diagnet-analyzer --tail 100
   ```

3. **Restart Service** (if needed):
   ```bash
   docker restart diagnet-gateway
   # or
   docker restart diagnet-collector
   # or
   docker restart diagnet-analyzer
   ```

---

## 📝 Key Metrics Explained

### **HTTP Requests per Second**
- **What it shows**: How many requests each service is handling
- **Normal range**: 1-100 req/s (depends on load)
- **Alert if**: Sudden drop to 0 (service down)

### **Response Time (p95)**
- **What it shows**: 95% of requests complete within this time
- **Normal range**: 10-200ms
- **Alert if**: > 500ms (performance issue)

### **JVM Heap Memory**
- **What it shows**: Memory used by Java applications
- **Normal range**: Stable with periodic drops (GC)
- **Alert if**: Continuous growth (memory leak)

### **CPU Usage**
- **What it shows**: Processor usage percentage
- **Normal range**: 10-60%
- **Alert if**: > 80% sustained (overload)

### **Error Rate**
- **What it shows**: Percentage of failed requests
- **Normal range**: < 1%
- **Alert if**: > 5% (service issues)

---

## 🛠️ Troubleshooting

### Grafana Not Loading Dashboards
```bash
# Restart Grafana
docker restart diagnet-grafana

# Check logs
docker logs diagnet-grafana --tail 50
```

### Prometheus Not Showing Data
```bash
# Check Prometheus is running
docker ps | grep prometheus

# Verify targets
curl -s http://localhost:9090/api/v1/targets | grep '"health"'

# Restart if needed
docker restart diagnet-prometheus
```

### Services Showing as "DOWN"
```bash
# Check which service is down
docker ps -a | grep diagnet

# Restart all services
cd /Users/macbook/Desktop/DiagNet
docker-compose restart
```

---

## 📚 Additional Resources

### Prometheus Documentation
- Query basics: https://prometheus.io/docs/prometheus/latest/querying/basics/
- Functions: https://prometheus.io/docs/prometheus/latest/querying/functions/

### Grafana Documentation
- Dashboard guide: https://grafana.com/docs/grafana/latest/dashboards/
- Panel types: https://grafana.com/docs/grafana/latest/panels/

### PromQL Examples
- https://prometheus.io/docs/prometheus/latest/querying/examples/

---

## ✅ Quick Test

Run these commands to verify everything is working:

```bash
# 1. Check all containers are running
docker ps | grep diagnet

# 2. Test Prometheus
curl -s http://localhost:9090/api/v1/query?query=up | grep "value"

# 3. Test Grafana (should return login page)
curl -s http://localhost:3000 | grep "Grafana"

# 4. Check service metrics
curl -s http://localhost:8080/actuator/prometheus | grep "http_server_requests"
curl -s http://localhost:8081/actuator/prometheus | grep "http_server_requests"
curl -s http://localhost:8082/actuator/prometheus | grep "http_server_requests"
```

All commands should return data successfully!

---

**🎉 Your monitoring stack is fully configured and ready to use!**

Access Grafana now: **http://localhost:3000** (admin/admin123)

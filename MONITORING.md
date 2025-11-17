# 📊 DiagNet Monitoring Guide

## Overview

This guide explains the observability stack for DiagNet: **Prometheus** (metrics collection) + **Grafana** (visualization).

---

## 🎯 Why Monitoring?

In production systems, you need to answer questions like:
- Is my service healthy?
- How many requests per second am I handling?
- Is memory usage growing over time (memory leak)?
- Which endpoint is slowest?

**Metrics = numbers collected over time** (e.g., CPU%, request count, latency)

---

## 🏗️ Architecture

```
┌──────────────┐
│  Gateway     │──┐
│  Service     │  │
└──────────────┘  │
                  │ Scrape /actuator/prometheus
┌──────────────┐  │ every 15 seconds
│  Collector   │──┼────────────────┐
│  Service     │  │                ↓
└──────────────┘  │         ┌──────────────┐
                  │         │  Prometheus  │
┌──────────────┐  │         │  (Storage)   │
│  Analyzer    │──┘         └──────┬───────┘
│  Service     │                   │
└──────────────┘                   │ Query metrics
                                   ↓
                            ┌──────────────┐
                            │   Grafana    │
                            │ (Dashboards) │
                            └──────────────┘
```

---

## 🚀 Quick Start

### 1. Start Monitoring Stack

```bash
# Services are already defined in docker-compose.yml
docker-compose up -d prometheus grafana

# Check if running
docker ps | grep -E "prometheus|grafana"
```

### 2. Access Grafana

1. Open **http://localhost:3000**
2. Login:
   - Username: `admin`
   - Password: `admin123`
3. Navigate to **Dashboards** → **DiagNet - System Overview**

### 3. Access Prometheus

1. Open **http://localhost:9090**
2. Try a query: `up{job=~".*-service"}`
3. Click **Graph** to see time series

---

## 📈 Available Metrics

### Spring Boot Actuator Metrics

All services expose metrics at `/actuator/prometheus`:

```bash
# View raw metrics from gateway
curl http://localhost:8080/actuator/prometheus

# Example output:
# http_server_requests_seconds_count{method="GET",uri="/api/data/recent"} 42
# jvm_memory_used_bytes{area="heap",id="G1 Old Gen"} 52428800
# process_cpu_usage 0.15
```

### JVM Metrics

| Metric | Description | Query |
|--------|-------------|-------|
| **Heap Memory** | Memory used by Java objects | `jvm_memory_used_bytes{area="heap"}` |
| **CPU Usage** | Processor utilization | `process_cpu_usage` |
| **Thread Count** | Number of active threads | `jvm_threads_live_threads` |
| **GC Time** | Garbage collection duration | `jvm_gc_pause_seconds_sum` |

### HTTP Metrics

| Metric | Description | Query |
|--------|-------------|-------|
| **Request Rate** | Requests per second | `rate(http_server_requests_seconds_count[5m])` |
| **Error Rate** | 4xx/5xx responses per second | `rate(http_server_requests_seconds_count{status=~"4..\\|5.."}[5m])` |
| **Latency (p95)** | 95th percentile response time | `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))` |
| **Success Rate** | Percentage of 2xx responses | `sum(rate(http_server_requests_seconds_count{status=~"2.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m]))` |

### Custom Metrics (Future)

You can add custom metrics using Micrometer:

```java
@Component
public class CustomMetrics {
    private final Counter mqttMessagesReceived;
    
    public CustomMetrics(MeterRegistry registry) {
        this.mqttMessagesReceived = Counter.builder("mqtt.messages.received")
            .description("Number of MQTT messages received")
            .tag("topic", "machine")
            .register(registry);
    }
    
    public void recordMessage() {
        mqttMessagesReceived.increment();
    }
}
```

---

## 📊 Grafana Dashboards

### Pre-configured Dashboard: "DiagNet - System Overview"

Located at: `observability/grafana/dashboards/diagnet-overview.json`

**Panels**:
1. **HTTP Requests per Second** - Line chart showing request rate by service
2. **CPU Usage** - Gauge showing current CPU utilization
3. **JVM Memory Usage (Heap)** - Memory consumption over time
4. **Service Health Status** - Up/down status for each service

### Creating Custom Dashboards

1. **Add New Dashboard**
   - Click **+** → **Dashboard**
   - Click **Add new panel**

2. **Configure Query**
   - Data source: **Prometheus**
   - Metric: `rate(http_server_requests_seconds_count[5m])`
   - Legend: `{{service}} - {{uri}}`

3. **Customize Visualization**
   - Chart type: Time series, Gauge, Stat, etc.
   - Thresholds: Red > 90%, Yellow > 70%
   - Unit: Percent, Bytes, Requests/sec

4. **Save Dashboard**
   - Click **Save dashboard**
   - Name: "My Custom Dashboard"

---

## 🔍 Useful Prometheus Queries

### Service Health

```promql
# Check if services are up (1 = up, 0 = down)
up{job=~".*-service"}

# Count healthy services
count(up{job=~".*-service"} == 1)
```

### Request Analysis

```promql
# Total requests in last 5 minutes
sum(rate(http_server_requests_seconds_count[5m]))

# Requests by HTTP method
sum by (method) (rate(http_server_requests_seconds_count[5m]))

# Slowest endpoints (top 5)
topk(5, 
  histogram_quantile(0.95, 
    sum by (uri, le) (
      rate(http_server_requests_seconds_bucket[5m])
    )
  )
)
```

### Memory Analysis

```promql
# Heap usage percentage
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# Memory usage by service
sum by (service) (jvm_memory_used_bytes{area="heap"})

# Memory growth rate (MB per minute)
rate(jvm_memory_used_bytes{area="heap"}[5m]) * 60 / 1024 / 1024
```

### Database Connections (if exposed)

```promql
# Active database connections
hikaricp_connections_active

# Connection pool usage
(hikaricp_connections_active / hikaricp_connections_max) * 100
```

---

## 🚨 Setting Up Alerts (Advanced)

### 1. Create Alert Rules

Create `observability/prometheus/alerts.yml`:

```yaml
groups:
  - name: diagnet_alerts
    interval: 30s
    rules:
      # Alert if service is down
      - alert: ServiceDown
        expr: up{job=~".*-service"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Service {{ $labels.job }} is down"
          description: "{{ $labels.job }} has been down for more than 1 minute."
      
      # Alert if CPU usage is high
      - alert: HighCPUUsage
        expr: process_cpu_usage > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High CPU usage on {{ $labels.service }}"
          description: "CPU usage is {{ $value }} (80% threshold)"
      
      # Alert if heap memory is high
      - alert: HighMemoryUsage
        expr: (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) > 0.9
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High memory usage on {{ $labels.service }}"
          description: "Heap usage is {{ $value | humanizePercentage }}"
```

### 2. Configure Alertmanager (Optional)

```yaml
# docker-compose.yml
alertmanager:
  image: prom/alertmanager:latest
  ports:
    - "9093:9093"
  volumes:
    - ./observability/alertmanager/config.yml:/etc/alertmanager/config.yml
  networks:
    - diagnet-network
```

---

## 🧪 Testing Metrics

### 1. Generate Load

```bash
# Send 100 requests to test endpoint
for i in {1..100}; do
  curl -s "http://localhost:8080/actuator/health" > /dev/null
  echo "Request $i sent"
  sleep 0.1
done
```

### 2. View Metrics in Prometheus

1. Open **http://localhost:9090**
2. Query: `rate(http_server_requests_seconds_count{uri="/actuator/health"}[1m])`
3. You should see a spike in request rate!

### 3. Visualize in Grafana

1. Open **http://localhost:3000**
2. Go to **DiagNet - System Overview**
3. See the "HTTP Requests per Second" panel update

---

## 📚 Best Practices

### 1. Metric Naming Conventions

- Use **snake_case**: `http_requests_total` ✅ not `httpRequestsTotal` ❌
- Include unit suffix: `_seconds`, `_bytes`, `_total`
- Use meaningful labels: `{service="gateway", method="GET"}`

### 2. Query Performance

- **Use recording rules** for expensive queries:
  ```yaml
  # prometheus.yml
  rule_files:
    - "recording_rules.yml"
  ```
- **Limit time range**: Use `[5m]` instead of `[1h]` when possible
- **Use aggregations**: `sum by (service)` instead of querying all instances

### 3. Dashboard Organization

- One dashboard per concern (JVM, HTTP, Database)
- Use template variables for dynamic filtering
- Set reasonable refresh intervals (5s-1m)

### 4. Retention Policy

```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  
storage:
  tsdb:
    retention.time: 15d  # Keep data for 15 days
    retention.size: 10GB # Max 10GB of data
```

---

## 🐛 Troubleshooting

### Prometheus Can't Scrape Service

**Symptom**: `up{job="gateway-service"} == 0`

**Solutions**:
```bash
# Check if service is running
docker ps | grep gateway

# Check if actuator endpoint is accessible
curl http://localhost:8080/actuator/prometheus

# Check Prometheus targets page
# http://localhost:9090/targets
# Should show state = UP
```

### Grafana Shows "No Data"

**Solutions**:
1. Check Prometheus data source: **Configuration** → **Data Sources** → Test
2. Verify query syntax in Prometheus UI first
3. Check time range (data might be outside selected range)

### High Memory Usage in Prometheus

**Solutions**:
```yaml
# Reduce retention
storage:
  tsdb:
    retention.time: 7d  # Instead of 15d
    
# Reduce scrape frequency
global:
  scrape_interval: 30s  # Instead of 15s
```

---

## 🎓 Learning Resources

### Prometheus
- [Official Documentation](https://prometheus.io/docs/)
- [PromQL Tutorial](https://prometheus.io/docs/prometheus/latest/querying/basics/)
- [Best Practices](https://prometheus.io/docs/practices/naming/)

### Grafana
- [Official Tutorials](https://grafana.com/tutorials/)
- [Dashboard Best Practices](https://grafana.com/docs/grafana/latest/dashboards/build-dashboards/best-practices/)

### Micrometer (Spring Boot Metrics)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Spring Boot Actuator Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

---

## 📝 Next Steps

- [ ] Add custom business metrics (e.g., `machines_monitored_total`)
- [ ] Set up alerting with Alertmanager
- [ ] Create dashboards for each service
- [ ] Configure long-term storage (Thanos, VictoriaMetrics)
- [ ] Add distributed tracing with OpenTelemetry

---

**Happy monitoring! 📊**

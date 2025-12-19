# Infrastructure

This directory contains all infrastructure components for observability, orchestration, and deployment.

## Components

### Kubernetes (`/kubernetes`)
**Purpose:** Container orchestration and deployment manifests

**Files:**
- `auth-service-deployment.yaml` - Auth Service deployment and service
- `user-service-deployment.yaml` - User Service deployment and service
- `bff-service-deployment.yaml` - BFF Service deployment and service
- `frontend-deployment.yaml` - Frontend deployment and service
- `configmap.yaml` - Configuration data for services
- `secret.yaml` - Sensitive data (JWT keys, database credentials)

**Deploy:**
```bash
kubectl apply -f infra/kubernetes/
```

**Features:**
- Liveness and readiness probes
- Resource limits (CPU, memory)
- Horizontal Pod Autoscaling (HPA)
- Service discovery via ClusterIP
- ConfigMaps for environment variables
- Secrets for sensitive data

---

### Logging (`/logging`)
**Purpose:** Centralized log aggregation and visualization (ELK Stack)

**Components:**
- **Elasticsearch** (Port 9200) - Log storage and search engine
- **Logstash** (Port 5000) - Log aggregation and processing
- **Kibana** (Port 5601) - Log visualization and analysis

**Start:**
```bash
cd infra/logging
docker-compose -f docker-compose-elk.yml up -d
```

**Access Kibana:** http://localhost:5601

**Configuration:**
- `logstash/logstash.conf` - Logstash pipeline configuration
- Input: TCP on port 5000 (JSON logs from services)
- Output: Elasticsearch with index pattern `logs-*`

**Index Pattern Setup:**
1. Open Kibana → Management → Index Patterns
2. Create pattern: `logs-*`
3. Timestamp field: `@timestamp`

**Log Structure:**
```json
{
  "@timestamp": "2024-01-01T12:00:00.000Z",
  "service": "auth-service",
  "level": "INFO",
  "trace_id": "abc123",
  "span_id": "def456",
  "message": "User logged in successfully",
  "username": "john_doe"
}
```

---

### Monitoring (`/monitoring`)
**Purpose:** Metrics collection and visualization

**Components:**
- **Prometheus** (Port 9090) - Metrics collection and storage
- **Grafana** (Port 3000) - Metrics visualization and dashboards

**Start:**
```bash
cd infra/monitoring
docker-compose -f docker-compose-monitoring.yml up -d
```

**Access:**
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

**Configuration:**
- `prometheus/prometheus.yml` - Scrape targets and intervals
- `prometheus/alerts.yml` - SLO alerting rules

**Scrape Targets:**
- Auth Service: `localhost:8081/actuator/prometheus`
- User Service: `localhost:8082/actuator/prometheus`
- API Gateway: `localhost:8080/actuator/prometheus`

**Alerting Rules:**
- **High Error Rate:** > 1% errors in 5 minutes
- **High Latency:** P95 > 500ms in 5 minutes
- **Low Availability:** < 99.9% uptime in 5 minutes

**Grafana Dashboards:**
- JVM Metrics (heap, threads, GC)
- HTTP Metrics (requests/sec, latency, errors)
- Business Metrics (registrations, logins, active users)

---

### Tracing (`/tracing`)
**Purpose:** Distributed tracing with OpenTelemetry

**Components:**
- **Jaeger** (Port 16686) - Tracing UI
- **OpenTelemetry Collector** (Port 4318) - OTLP endpoint

**Start:**
```bash
cd infra/tracing
docker-compose -f docker-compose-otel.yml up -d
```

**Access Jaeger:** http://localhost:16686

**Features:**
- End-to-end request tracing across services
- Latency breakdown by service
- Error tracking and root cause analysis
- Service dependency graph

**Trace Flow:**
```
Frontend → BFF → Gateway → Auth Service
                         → User Service
```

Each service adds spans with:
- Service name
- Operation name
- Duration
- Tags (HTTP method, status code, error)
- Logs (events within span)

---

## Infrastructure Stack Overview

### Observability (The Three Pillars)

**1. Logs (ELK Stack)**
- What happened? Detailed event records
- Use case: Debugging, audit trails, error investigation

**2. Metrics (Prometheus + Grafana)**
- How much? Numerical measurements over time
- Use case: Performance monitoring, capacity planning, alerting

**3. Traces (Jaeger + OpenTelemetry)**
- Where did time go? Request flow across services
- Use case: Latency analysis, bottleneck identification, dependency mapping

### Orchestration

**Kubernetes**
- Container orchestration
- Auto-scaling and self-healing
- Service discovery and load balancing
- Rolling updates and rollbacks

---

## Port Reference

| Component | Port | Purpose |
|-----------|------|---------|
| Elasticsearch | 9200 | Log storage API |
| Logstash | 5000 | Log ingestion (TCP) |
| Kibana | 5601 | Log visualization UI |
| Prometheus | 9090 | Metrics collection API |
| Grafana | 3000 | Metrics visualization UI |
| Jaeger UI | 16686 | Tracing visualization |
| OTLP Collector | 4318 | OpenTelemetry endpoint |

---

## Quick Start (All Infrastructure)

### Start Everything
```bash
# Logging
cd infra/logging
docker-compose -f docker-compose-elk.yml up -d

# Monitoring
cd ../monitoring
docker-compose -f docker-compose-monitoring.yml up -d

# Tracing
cd ../tracing
docker-compose -f docker-compose-otel.yml up -d
```

### Stop Everything
```bash
cd infra/logging && docker-compose -f docker-compose-elk.yml down
cd ../monitoring && docker-compose -f docker-compose-monitoring.yml down
cd ../tracing && docker-compose -f docker-compose-otel.yml down
```

### Check Status
```bash
docker ps
```

---

## Production Considerations

### Logging
- **Retention:** Configure Elasticsearch index lifecycle (30 days default)
- **Disk Space:** Monitor Elasticsearch disk usage (alert at 80%)
- **Performance:** Use Logstash filters to parse and enrich logs

### Monitoring
- **Retention:** Prometheus stores 15 days by default (configure longer for production)
- **Alerting:** Integrate with PagerDuty or Opsgenie for on-call notifications
- **Dashboards:** Create custom Grafana dashboards for business metrics

### Tracing
- **Sampling:** Use probabilistic sampling (10%) to reduce overhead
- **Storage:** Configure Jaeger storage backend (Elasticsearch, Cassandra)
- **Performance:** Tracing adds ~1-5ms latency per request

### Kubernetes
- **Resource Limits:** Set CPU/memory limits to prevent resource exhaustion
- **Health Checks:** Configure liveness and readiness probes
- **Secrets:** Use external secret management (Vault, AWS Secrets Manager)
- **Ingress:** Configure ingress controller for external traffic (NGINX, Traefik)

---

## Troubleshooting

### Logs Not Appearing in Kibana
1. Check Logstash is running: `docker ps | grep logstash`
2. Check services are sending logs: `nc -zv localhost 5000`
3. Check Elasticsearch indices: `curl localhost:9200/_cat/indices`
4. Verify index pattern in Kibana matches `logs-*`

### Metrics Not Showing in Grafana
1. Check Prometheus targets: http://localhost:9090/targets
2. Verify services expose `/actuator/prometheus` endpoint
3. Check Prometheus scrape interval (15s default)
4. Add Prometheus data source in Grafana

### Traces Not Appearing in Jaeger
1. Check OTLP collector is running: `docker ps | grep otel`
2. Verify services send traces to `localhost:4318`
3. Check Jaeger storage backend is healthy
4. Verify OpenTelemetry SDK is configured in services

---

## Security

- **Elasticsearch:** Enable authentication in production (X-Pack Security)
- **Kibana:** Configure SSO or LDAP authentication
- **Prometheus:** Use basic auth or OAuth2 proxy
- **Grafana:** Enable authentication and RBAC
- **Kubernetes:** Use RBAC, Network Policies, and Pod Security Standards

# User Service SLA & SLO

## SLA (Service Level Agreement)
- Uptime: 99.9% per month
- Support: Email response within 24 hours
- Incident response: Critical issues acknowledged within 1 hour

## SLO (Service Level Objectives)
| Metric                         | Target                          | Measurement Period |
|--------------------------------|---------------------------------|--------------------|
| API uptime                     | 99.9%                           | Monthly            |
| Get user info response time    | < 200ms (95th percentile)       | Hourly             |
| Update user info response time | < 250ms (95th percentile)       | Hourly             |
| Successful user updates        | 99.5% per endpoint per day      | Daily              |
| Error rate                     | < 1% per endpoint per day       | Daily              |

## Notes
- Health check endpoint: `GET /health` (should return 200 if service is healthy)
- Dependent on Auth service for authentication; Auth downtime may affect user operations
- Metrics should be monitored via Prometheus, Grafana, or similar APM tools
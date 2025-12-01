# Auth Service SLA & SLO

## SLA (Service Level Agreement)
- Uptime: 99.95% per month
- Support: Email response within 24 hours
- Incident response: Critical issues acknowledged within 30 min

## SLO (Service Level Objectives)
| Metric                      | Target                          | Measurement Period |
|-----------------------------|---------------------------------|--------------------|
| API uptime                  | 99.95%                          | Monthly            |
| Successful login requests   | 99.9%                           | Daily              |
| Response time (95th pct)    | < 150ms                         | Hourly             |
| Token refresh latency       | < 200ms                         | Hourly             |
| Error rate                  | < 0.5% per endpoint per day     | Daily              |

## Notes
- Health check endpoint: `GET /health` (should return 200 if service is healthy)
- Auth service downtime impacts all dependent services (User, Notifications)
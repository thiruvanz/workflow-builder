# Workflow Platform (Diagram-Aligned)

Java 17 + Spring Boot + Hibernate microservices with Temporal orchestration, Redis cache, Kafka eventing, trigger services, notification service, BFF, DLQ replay processor, API gateway, CDN edge, and full deployable Docker Compose stack.

## Services

- `gateway-service` (`8080` internal): API gateway entrypoint
- `cdn-service` (`8080` exposed): edge cache in front of gateway
- `discovery-service` (`8761`): Eureka service registry
- `frontend-service` (`8088` internal): UI for create/manage/trigger/monitor
- `workflow-service` (`8081`): workflow CRUD + publish + outbox producer + Redis workflow cache
- `execution-service` (`8082`): trigger execution + Temporal worker/client + idempotency + rate-limit + execution events
- `monitoring-service` (`8083`): dashboard aggregation + Kafka event counters
- `trigger-service` (`8084`): webhook trigger endpoint + cron trigger dispatcher
- `notify-service` (`8085`): consumes workflow/execution events for alert feed
- `bff-service` (`8086`): UI-shaped aggregation API
- `dlq-processor-service` (`8087`): dead-letter consumer + replay endpoint

## Infra

- PostgreSQL (workflowdb, executiondb, triggerdb)
- Redis (cache, token-bucket counters)
- Redpanda (Kafka-compatible event bus)
- Temporal auto-setup server
- MongoDB (monitoring event documents)
- Elasticsearch + Kibana (notification search/index)
- Prometheus + Grafana (metrics)
- Jaeger (tracing UI)

## Deploy and Run

### 1) Start everything

```powershell
docker compose up --build
```

### 1b) Scale required services (load-balanced via gateway + Eureka)

```powershell
docker compose up --build --scale workflow-service=2 --scale execution-service=2 --scale monitoring-service=2 --scale notify-service=2
```

### Frontend build (React + Vite)

- Source: `frontend-service/react-app`
- Production bundle output: `frontend-service/src/main/resources/static`
- Gradle automatically runs React build during `:frontend-service:processResources`

### 2) Open platform

- UI via CDN edge: `http://localhost:8080`

### 3) Key endpoints (via gateway)

- Workflows: `http://localhost:8080/api/workflows`
- Execution runs: `http://localhost:8080/api/executions/runs`
- Monitoring dashboard: `http://localhost:8080/api/monitoring/dashboard`
- Webhook trigger: `http://localhost:8080/api/triggers/webhook`
- Notification feed: `http://localhost:8080/api/notify/messages`
- BFF overview: `http://localhost:8080/bff/overview`
- DLQ messages: `http://localhost:8080/api/dlq/messages`
- DLQ replay: `POST http://localhost:8080/api/dlq/replay`

### 4) Ops endpoints

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Jaeger: `http://localhost:16686`
- Kibana: `http://localhost:5601`

## Notes

- Publish a workflow before triggering.
- `trigger-service` cron dispatch targets workflow id `1`; create/publish workflow id `1` to see scheduled runs quickly.
- CDN adds asset caching and API micro-caching. `X-Cache-Status` response header shows `HIT`/`MISS`.
- Gateway enforces JWT auth for `/api/**` and `/bff/**` paths; UI still works through `/ui-api/**` routed to frontend backend proxy.
- Gateway uses `lb://` routes and resolves instances via Eureka registry for service-level load balancing.
- This is a deployable reference implementation matching the high-level architecture components and patterns.

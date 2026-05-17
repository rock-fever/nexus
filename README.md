# Nexus — Multi-Tenant API Gateway

A production-grade multi-tenant API Gateway built with Java 21 and Spring Boot 3. Tenants register their backends; all traffic flows through Nexus which handles authentication, rate limiting, routing, observability, and resilience. Think self-hosted Kong.


---

## Features

- **Multi-tenancy** — Tenant isolation via slug-based routing; each tenant has its own routes, API keys, rate limits, and circuit breakers
- **Authentication** — API key auth (hashed, Redis-cached) and JWT passthrough per route; admin panel protected by HS256 JWT
- **Rate limiting** — Token bucket via Redis Lua; plan-based limits (FREE: 60/min, PRO: 600/min, Enterprise: unlimited)
- **Dynamic routing** — Routes stored in PostgreSQL, projected to Redis (CQRS); changes propagate via Kafka
- **Resilience** — Per-route circuit breakers (Resilience4j) + per-tenant bulkheads (semaphore); state persisted in Redis
- **Observability** — Micrometer metrics, async request logging to MongoDB via Kafka, correlation IDs on every request
- **Event sourcing** — Every admin mutation produces an immutable event stored in MongoDB; full history replay via `/admin/tenants/{id}/history`
- **Contract testing** — Pact consumer + provider tests verify API contracts on every build
- **Load testing** — Four Gatling simulations covering baseline throughput, rate limit boundary, circuit breaker trip, and multi-tenant isolation

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| SQL DB | PostgreSQL (port 5433) |
| NoSQL | MongoDB (port 27018) |
| Cache / Locks | Redis (port 6380) |
| Messaging | Apache Kafka (port 9093) |
| Auth | Spring Security + JWT (admin) / API keys (tenants) |
| DB Migrations | Flyway |
| Resilience | Resilience4j (circuit breaker + bulkhead) |
| Metrics | Micrometer |
| Testing | JUnit 5 + Testcontainers + Pact JVM |
| Load Testing | Gatling |
| API Docs | SpringDoc OpenAPI |

> Runs on port **8081**.

---

## Quick Start

### Prerequisites

- Docker & Docker Compose
- Java 21
- Maven (or use the included `./mvnw` wrapper)

### 1. Start infrastructure

```bash
docker compose up -d
```

This starts PostgreSQL, MongoDB, Redis, Kafka, and a local Pact Broker (port 9292).

### 2. Run the application

```bash
./mvnw spring-boot:run
```

### 3. Get an admin token

```bash
curl -s -X POST http://localhost:8081/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq .token
```

### 4. Create a tenant

```bash
TOKEN=<token from above>

curl -s -X POST http://localhost:8081/admin/tenants \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Acme","slug":"acme","plan":"FREE"}' | jq
```

### 5. Add a route and proxy traffic

```bash
TENANT_ID=<id from above>

# Add a route
curl -s -X POST http://localhost:8081/admin/tenants/$TENANT_ID/routes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"pathPattern":"/anything/**","targetUrl":"https://httpbin.org","method":"ANY","authType":"NONE"}' | jq

# Proxy a request through Nexus
curl -s http://localhost:8081/anything/hello \
  -H "X-Tenant-Slug: acme" | jq
```

---

## API Reference

Interactive docs are available at **http://localhost:8081/swagger-ui.html** once the app is running.

### Admin Endpoints (require `Authorization: Bearer <jwt>`)

| Method | Path | Description |
|---|---|---|
| POST | `/admin/auth/login` | Get admin JWT |
| POST | `/admin/tenants` | Create tenant |
| GET | `/admin/tenants/{id}` | Get tenant |
| GET | `/admin/tenants` | List tenants |
| PATCH | `/admin/tenants/{id}/status?status=` | Suspend / reactivate |
| GET | `/admin/tenants/{id}/history` | Event sourcing replay |
| POST | `/admin/tenants/{id}/routes` | Create route |
| GET | `/admin/tenants/{id}/routes` | List routes |
| DELETE | `/admin/tenants/{id}/routes/{routeId}` | Delete route |
| POST | `/admin/tenants/{id}/keys` | Create API key |
| GET | `/admin/tenants/{id}/keys` | List API keys |
| DELETE | `/admin/tenants/{id}/keys/{keyId}` | Revoke API key |
| GET | `/admin/tenants/{id}/logs` | Query request history |

### Gateway (tenant traffic)

All non-admin paths are proxied. Identify the tenant via header:

```
X-Tenant-Slug: <slug>
```

Authenticated routes also require:

```
X-Api-Key: <raw-key>
```

---

## Architecture

```
api/
  controller/
    admin/       AdminTenantController, AdminRouteController, AdminApiKeyController
    gateway/     GatewayController  ← catch-all, all tenant traffic enters here
  dto/           Request/Response objects (Builder pattern)

application/
  service/       TenantService, RouteCommandService, RouteQueryService, GatewayService

domain/
  model/         Tenant, Route, ApiKey, RateLimitPolicy  (no framework imports)
  repository/    ITenantRepository, IRouteRepository, IRouteQueryRepository (CQRS read side)
  filter/        GatewayFilter (interface), FilterChain, FilterRegistry
  auth/          AuthStrategy (interface)
  resilience/    CircuitBreakerState, RouteCircuitBreaker (interface)

infrastructure/
  persistence/   JPA implementations of domain repositories
  cache/         CachedRouteRepository (Decorator)
  mongo/         MongoRequestLogRepository, TenantConfigEventStore
  messaging/     Kafka producers + consumers
  resilience/    Resilience4jCircuitBreaker, TenantBulkhead
  routing/       HttpProxyClient (RestClient)
  auth/          ApiKeyAuthStrategy, JwtAuthStrategy, PassThroughAuthStrategy
  filter/        CorrelationId, Auth, RateLimit, Resilience, Routing, Proxy, Metrics filters
  ratelimit/     AbstractTenantRateLimiter + FREE/PRO/ENTERPRISE implementations
  config/        SecurityConfig, RedisConfig, KafkaConfig, MongoConfig

shared/
  exception/     GatewayException, TenantNotFoundException, CircuitOpenException, RateLimitException
  security/      AdminJwtFilter, AdminJwtUtil
```

**Layer rules:**
- `domain/` — zero Spring/JPA/Redis/Kafka imports
- `application/` — may use `@Service`, `@Transactional`; no direct JPA/Redis/Kafka
- `infrastructure/` — all framework wiring lives here

### Filter Pipeline

| Order | Filter | Phase |
|---|---|---|
| 1 | CorrelationIdFilter | Pre |
| 2 | TenantResolutionFilter | Pre |
| 10 | AuthFilter | Pre |
| 20 | RateLimitFilter | Pre |
| 45 | ResilienceFilter | Pre + Post |
| 50 | RoutingFilter | Pre |
| 60 | ProxyFilter | Pre (executes HTTP call) |
| 90 | MetricsFilter | Post |
| 100 | RequestLoggingFilter | Post |

---

## Design Patterns

| Pattern | Where |
|---|---|
| Builder | All DTO classes |
| Strategy | `AuthStrategy` interface + plan-based rate limiters |
| Chain of Responsibility | `FilterChain` + `FilterChainExecutor` |
| Plugin / Registry | `FilterRegistry` — filters self-register via `@Component` |
| Template Method | `AbstractTenantRateLimiter` |
| Decorator | `CachedRouteRepository` wraps PostgreSQL repo with Redis |
| Proxy | `GatewayController` + `HttpProxyClient` |
| CQRS | `RouteCommandService` (write → PG) + `RouteQueryService` (read → Redis) |
| State | `CircuitBreakerState` enum (CLOSED / OPEN / HALF_OPEN) |
| Circuit Breaker | `Resilience4jCircuitBreaker` per route |
| Bulkhead | `TenantBulkhead` semaphore per tenant |
| Observer | Kafka consumers reacting to route/tenant change events |
| Event Sourcing | `TenantConfigEventStore` + `TenantConfigProjector` (MongoDB) |

---

## Running Tests

```bash
# All tests (unit + integration + contract)
./mvnw test

# Single class
./mvnw test -Dtest=TenantServiceTest

# Contract tests only
./mvnw test -Dtest="AdminTenantContractTest,AdminRouteContractTest,NexusPactProviderTest"
```

Tests use **Testcontainers** — Docker must be running. No manual infrastructure setup needed for tests.

### Load Tests (Gatling)

Requires the application to be running (`./mvnw spring-boot:run`).

```bash
# Baseline throughput
./mvnw gatling:test -Dgatling.simulationClass=com.nexus.simulation.BaselineThroughputSimulation

# Rate limit boundary
./mvnw gatling:test -Dgatling.simulationClass=com.nexus.simulation.RateLimitBoundarySimulation

# Circuit breaker trip
./mvnw gatling:test -Dgatling.simulationClass=com.nexus.simulation.CircuitBreakerSimulation

# Multi-tenant isolation
./mvnw gatling:test -Dgatling.simulationClass=com.nexus.simulation.MultiTenantIsolationSimulation
```

HTML reports are written to `target/gatling/`.

---

## Database

### PostgreSQL (Flyway migrations in `src/main/resources/db/migration/`)

- `tenants` — id, name, slug, plan, status, created_at
- `routes` — id, tenant_id, path_pattern, target_url, method, strip_prefix, auth_type, retry_attempts, active
- `api_keys` — id, tenant_id, key_hash, name, active, created_at
- `rate_limit_policies` — id, tenant_id, requests_per_minute

### MongoDB

- `request_logs` — per-request audit trail (written async via Kafka)
- `tenant_config_events` — immutable event log for event sourcing

### Redis key conventions

| Key | Purpose | TTL |
|---|---|---|
| `route:{tenantId}:{pathHash}` | Cached route projection | 5 min |
| `tenant:{id}` | Cached tenant | 10 min |
| `ratelimit:{tenantId}:{windowMs}` | Token bucket counter | window |
| `apikey:{keyHash}` | API key → tenant lookup | 10 min |
| `cb:state:{routeId}` | Circuit breaker state | — |

---

## Kafka Topics

| Topic | Producer | Consumer |
|---|---|---|
| `route.updated` | RouteCommandService | RouteProjectionConsumer (Redis invalidation) |
| `request.logged` | MetricsFilter | RequestLogConsumer (MongoDB write) |
| `circuit.tripped` | ResilienceFilter | AuditConsumer (event store) |
| `tenant.config.changed` | TenantService | TenantConfigProjector (event sourcing) |

---

## Build Commands

```bash
docker compose up -d          # Start infrastructure
./mvnw spring-boot:run        # Run application (port 8081)
./mvnw test                   # Run all tests
./mvnw clean package -DskipTests  # Build JAR
./mvnw flyway:info            # Check migration status
```

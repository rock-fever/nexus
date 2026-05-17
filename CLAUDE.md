# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Project: Nexus — Multi-Tenant API Gateway

A production-grade multi-tenant API Gateway built in Java 21 + Spring Boot 3. Clients (tenants) register their backends; all traffic flows through Nexus which handles auth, rate limiting, routing, observability, and resilience. Think self-hosted Kong.

This is **Phase 2 of a 2-project learning plan** (preceded by TicketHub). Every design decision is intentional and tied to a pattern or principle — particularly patterns not covered in TicketHub: Plugin/Registry, CQRS, Event Sourcing, Circuit Breaker, and Bulkhead.

---

## Next Project Hint: Forge — Distributed Job Processing Engine

**Phase 3** of the learning plan after Nexus. Focus: **Java multithreading patterns**.

Think self-hosted Sidekiq/Celery. Clients submit jobs via REST; Forge executes them with configurable concurrency and provides real-time status tracking.

**Multithreading concepts covered:**

| Concept | Where it appears |
|---|---|
| Thread Pool (`ExecutorService`) | Worker pool that picks up jobs from a queue |
| Producer-Consumer | HTTP thread produces jobs, worker threads consume |
| `BlockingQueue` | Bounded in-memory queue between producer and workers |
| `ReentrantLock` / `synchronized` | Protecting job state transitions (PENDING → RUNNING → DONE) |
| `CountDownLatch` | Waiting for a batch of jobs to all complete |
| `Semaphore` | Limiting concurrent jobs per client |
| `CompletableFuture` | Chaining dependent jobs (job B runs after job A) |
| `AtomicLong` / `AtomicReference` | Lock-free counters for metrics |
| `volatile` | Status flags visible across threads without locking |
| `ScheduledExecutorService` | Retry scheduler with backoff for failed jobs |
| `ThreadLocal` | Per-thread MDC context for logging job IDs |
| `ForkJoinPool` | Splitting a large job into parallel subtasks |

**Suggested phases:**
1. Single-threaded job runner → add thread pool → observe contention problems
2. Fix with `BlockingQueue` + bounded workers
3. Add job chaining with `CompletableFuture`
4. Add per-client concurrency limits with `Semaphore`
5. Add retry + backoff with `ScheduledExecutorService`
6. Add `ForkJoinPool` for parallelizable job types

Run on port **8082** to avoid conflicts with TicketHub (8080) and Nexus (8081).

Nexus runs on port **8081** to avoid conflicting with TicketHub (8080).

---

## Build & Run Commands

```bash
# Start all infrastructure (PostgreSQL, MongoDB, Redis, Kafka)
docker compose up -d

# Run the application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=TenantServiceTest

# Build without tests
./mvnw clean package -DskipTests

# Check Flyway migration status
./mvnw flyway:info
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| SQL DB | PostgreSQL (port 5433) |
| NoSQL DB | MongoDB (port 27018) |
| Cache / Lock | Redis (port 6380) |
| Messaging | Apache Kafka (port 9093) |
| Auth | Spring Security + JWT (admin panel only) |
| DB Migrations | Flyway |
| Resilience | Resilience4j (circuit breaker + bulkhead) |
| Metrics | Micrometer |
| Testing | JUnit 5 + Testcontainers |
| Docs | SpringDoc OpenAPI (Swagger UI at /swagger-ui.html) |

> Ports are offset by 1 from TicketHub so both projects can run simultaneously.

---

## Architecture

```
api/
  controller/
    admin/       AdminTenantController, AdminRouteController, AdminApiKeyController
    gateway/     GatewayController  ← catch-all endpoint, all tenant traffic enters here
  dto/           Request/Response objects (Builder pattern)

application/
  service/       TenantService, RouteCommandService, RouteQueryService, GatewayService
                 ← orchestration layer: Spring @Service beans, depends on domain interfaces,
                    works with api/dto, may import framework annotations

domain/
  model/         Tenant, Route, ApiKey, Plan, RateLimitPolicy (no framework imports)
  repository/    ITenantRepository, IRouteRepository, IApiKeyRepository,
                 IRouteQueryRepository (CQRS read side), IRequestLogRepository
  filter/        GatewayFilter (interface), FilterChain, FilterRegistry
  auth/          AuthStrategy (interface)
  resilience/    CircuitBreakerState (enum), RouteCircuitBreaker (interface)

infrastructure/
  persistence/   JPA implementations of domain repository interfaces
  cache/         CachedRouteRepository (Decorator)
  mongo/         MongoRequestLogRepository, TenantConfigEventStore
  messaging/     Kafka producers + consumers + event records
  resilience/    Resilience4jCircuitBreaker, TenantBulkhead, registries
  routing/       HttpProxyClient (RestClient-based)
  auth/          ApiKeyAuthStrategy, JwtAuthStrategy, PassThroughAuthStrategy
  filter/        Concrete filters: CorrelationId, Auth, RateLimit, Resilience, Routing, Proxy, Metrics
  ratelimit/     AbstractTenantRateLimiter + plan-specific implementations
  config/        SecurityConfig, RedisConfig, KafkaConfig, MongoConfig, JpaConfig

shared/
  exception/     GatewayException, TenantNotFoundException, CircuitOpenException, RateLimitException
  security/      AdminJwtFilter, AdminJwtUtil
```

**Key rules:**
- `domain/` has zero Spring/JPA/Redis/Kafka imports. It only knows about its own interfaces and models.
- `application/` may use Spring annotations (`@Service`, `@Transactional`) and import from both `domain/` and `api/dto/`. No direct JPA/Redis/Kafka imports — those stay in `infrastructure/`.
- All framework infrastructure code lives in `infrastructure/`.

---

## Database

### PostgreSQL tables (managed by Flyway in src/main/resources/db/migration/)
- `tenants` — id, name, slug, plan (FREE/PRO/ENTERPRISE), status (ACTIVE/SUSPENDED), created_at
- `routes` — id, tenant_id, path_pattern, target_url, method, strip_prefix, auth_type, retry_attempts, active
- `api_keys` — id, tenant_id, key_hash, name, active, created_at
- `rate_limit_policies` — id, tenant_id, requests_per_minute (overrides plan default if set)

### MongoDB collections
- `request_logs` — tenantId, routeId, method, path, statusCode, latencyMs, timestamp
- `tenant_config_events` — immutable event log: tenantId, eventType, payload (JSON), timestamp

### Redis key conventions
- `route:{tenantId}:{pathHash}` — cached RouteDTO, TTL 5 min (CQRS read projection)
- `tenant:{id}` — cached TenantDTO, TTL 10 min
- `ratelimit:{tenantId}:{windowMs}` — token bucket counter
- `apikey:{keyHash}` — cached tenant lookup for API key auth, TTL 10 min
- `cb:state:{routeId}` — circuit breaker state (CLOSED/OPEN/HALF_OPEN)

---

## Filter Pipeline — Execution Order

| Order | Filter | Phase |
|---|---|---|
| 1 | `CorrelationIdFilter` | Pre |
| 2 | `TenantResolutionFilter` | Pre |
| 10 | `AuthFilter` | Pre |
| 20 | `RateLimitFilter` | Pre |
| 45 | `ResilienceFilter` | Pre + Post |
| 50 | `RoutingFilter` | Pre |
| 60 | `ProxyFilter` | Pre (executes HTTP call) |
| 90 | `MetricsFilter` | Post |
| 100 | `RequestLoggingFilter` | Post |

---

## Design Patterns — Where Each Lives

| Pattern | File / Package |
|---|---|
| Singleton | `infrastructure/config/AppConfig.java` |
| Builder | All `dto/` classes |
| Factory | `infrastructure/auth/AuthStrategyFactory.java` |
| Strategy | `domain/auth/AuthStrategy.java` + plan-based rate limiters |
| Chain of Responsibility | `domain/filter/FilterChain.java` + `FilterChainExecutor` |
| Plugin / Registry | `domain/filter/FilterRegistry.java` — filters self-register via `@Component` |
| Template Method | `infrastructure/ratelimit/AbstractTenantRateLimiter.java` |
| Decorator | `infrastructure/cache/CachedRouteRepository.java` |
| Proxy | `GatewayController` + `HttpProxyClient` — Nexus IS the proxy |
| CQRS | `RouteCommandService` (write→PostgreSQL) + `RouteQueryService` (read→Redis) |
| State | `domain/resilience/CircuitBreakerState.java` (CLOSED/OPEN/HALF_OPEN) |
| Circuit Breaker | `infrastructure/resilience/Resilience4jCircuitBreaker.java` |
| Bulkhead | `infrastructure/resilience/TenantBulkhead.java` |
| Observer | Kafka consumers reacting to route/tenant change events |
| Command | Kafka event records (immutable Java records) |
| Event Sourcing | `TenantConfigEventStore` + `TenantConfigProjector` (MongoDB) |

---

## Kafka Topics

| Topic | Producer | Consumer |
|---|---|---|
| `route.updated` | RouteCommandService | RouteProjectionConsumer (Redis invalidation) |
| `request.logged` | MetricsFilter | RequestLogConsumer (MongoDB write) |
| `circuit.tripped` | ResilienceFilter | AuditConsumer (event store) |
| `tenant.config.changed` | TenantService | TenantConfigProjector (event sourcing) |

---

## Build Status

### Phase 1 — Foundation
- [x] Docker Compose starts all 4 services cleanly
- [x] Flyway migrations run on startup
- [x] `POST /admin/auth/login` returns JWT
- [x] CRUD `/admin/tenants` — create, get, list, update status
- [x] CRUD `/admin/tenants/{id}/routes` — create, get, list, delete
- [x] CRUD `/admin/tenants/{id}/keys` — create, revoke by key UUID

### Phase 2 — Filter Pipeline
- [x] `GatewayFilter` interface + `GatewayContext` wrapper
- [x] `FilterRegistry` — auto-discovers all `GatewayFilter` beans, sorts by order
- [x] `FilterChainExecutor` — executes pre/post phases
- [x] `GatewayController` — catch-all `/**`, delegates to executor
- [x] `CorrelationIdFilter`, `TenantResolutionFilter`, `RequestLoggingFilter` working

### Phase 3 — Auth
- [x] `ApiKeyAuthStrategy` — hashed key lookup (Redis cached)
- [x] `JwtAuthStrategy` — validates admin Bearer token (for routes that should only be called by the admin panel; tenant callers use API keys, not JWTs — there is no tenant login endpoint)
- [x] `PassThroughAuthStrategy` — public routes
- [x] `AuthFilter` in pipeline; invalid key → 401

### Phase 4 — Rate Limiting
- [x] `AbstractTenantRateLimiter` — token bucket via Redis Lua
- [x] `FreePlanRateLimiter` (60/min), `ProPlanRateLimiter` (600/min), `EnterprisePlanRateLimiter` (unlimited)
- [x] `RateLimitFilter` in pipeline; exceeded → 429

### Phase 5 — Dynamic Routing + CQRS
- [x] `RouteCommandService` writes to PostgreSQL + publishes `route.updated`
- [x] `RouteProjectionConsumer` invalidates Redis on `route.updated`
- [x] `RouteQueryService` reads from Redis, falls back to PostgreSQL
- [x] `RoutingFilter` + `ProxyFilter` — actual HTTP proxying via RestClient

### Phase 6 — Observability
- [x] `MetricsFilter` — Micrometer counters/timers per tenant
- [x] `RequestLogConsumer` — async MongoDB write from `request.logged` Kafka topic
- [x] `/admin/tenants/{id}/logs` — query request history

### Phase 7 — Resilience
- [x] `CircuitBreakerState` enum + `RouteCircuitBreaker` domain interface
- [x] `Resilience4jCircuitBreaker` per route; state in Redis
- [x] `TenantBulkhead` — semaphore per tenant
- [x] `ResilienceFilter` in pipeline; circuit OPEN → 503

### Phase 8 — Event Sourcing + Polish
- [x] `TenantConfigEvent` records published on every admin mutation
- [x] `TenantConfigEventConsumer` appends to MongoDB `tenant_config_events`
- [x] `TenantConfigProjector` — replay history; `/admin/tenants/{id}/history`
- [x] `AbstractIntegrationTest` + integration test suite (Testcontainers)
- [x] OpenAPI annotations on all controllers
- [x] All tests pass: `./mvnw test` → BUILD SUCCESS

### Phase 9 — Load Testing (Gatling)
- [x] Gatling simulation: sustained load on `POST /{tenant}/api` — baseline throughput, p95/p99 latency
- [x] Scenario: rate limit boundary — ramp up to exactly FREE plan limit (60/min), verify 429 fires at the right threshold
- [x] Scenario: circuit breaker trip — simulate a slow/failing backend, assert OPEN state within configured failure rate
- [x] Scenario: multi-tenant isolation — concurrent load from 3 tenants on different plans, assert no cross-tenant rate limit bleed
- [x] Report: requests/sec, error rate, latency percentiles per scenario

### Phase 10 — Contract Testing (Pact)
- [x] Consumer contract: `AdminTenantController` — define expected request/response shapes for `POST /admin/tenants`, `GET /admin/tenants/{id}`, `PATCH /admin/tenants/{id}/status`
- [x] Consumer contract: `AdminRouteController` — `POST /admin/tenants/{id}/routes`, `DELETE /admin/tenants/{id}/routes/{routeId}`
- [x] Provider verification: Nexus verifies it satisfies all consumer contracts on every build
- [x] Pact broker (local Docker) to store and share contracts between consumer and provider tests

---

## SOLID Principles Applied

- **S** — `RouteCommandService` handles writes only; `RouteQueryService` handles reads only (CQRS)
- **O** — New auth strategies added via new class, not by modifying `AuthStrategyFactory`
- **L** — All repository and strategy implementations are fully substitutable for their interfaces
- **I** — `IRouteRepository` (CRUD) and `IRouteQueryRepository` (read-side) are separate interfaces
- **D** — Controllers depend on `application/service/` classes; application services depend on `domain/repository/` interfaces; `domain/` imports nothing from `infrastructure/` or `application/`

---

## Key Conventions

- Every request DTO uses the Builder pattern — no public constructors with multiple args
- Repository interfaces live in `domain/repository/` — never import JPA or Spring Data there
- All Kafka event types are Java records (immutable)
- Flyway migration naming: `V{n}__{description}.sql` — never edit an existing migration
- API key revoke uses key UUID (`DELETE /keys/{keyId}`), not the hash — Base64 hashes contain `/` which breaks URL path routing. The hash is internal only (gateway auth lookup)
- Integration tests use `@Testcontainers` — no mocking the database
- `GatewayContext` is the single mutable object passed through the filter chain — never pass `HttpServletRequest` directly into domain code
- Circuit breaker state is the source of truth in Redis; Resilience4j is the implementation detail behind the domain interface

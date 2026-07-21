# Architecture and engineering notes

## Context

StockPilot is a modular monolith. This is deliberate: the domain is small enough that a distributed architecture would add deployment and consistency costs without a clear business benefit.

## Components

### React dashboard

The browser application manages the operational interface. TanStack Query owns server state, request status, page changes and cache invalidation after mutations. The user exchanges credentials once for a short-lived JWT; only that token is held in React memory for the demonstration session. Previous/next controls consume explicit page metadata from the API.

### Spring Boot API

The API is divided by domain package rather than by technical layer alone:

- `product`: catalogue and product lifecycle.
- `stock`: auditable inventory movements.
- `dashboard`: aggregated operational indicators.
- `auth`: credential exchange and signed token creation.
- `common`: consistent errors, stable pagination and request correlation.
- `config`: security, CORS and OpenAPI.

Controllers validate the HTTP contract. Services apply use-case rules and transaction boundaries. Repositories isolate persistence.

### PostgreSQL

PostgreSQL stores products and movements. Flyway owns the schema. An outbound operation obtains a database lock on the active product before validating and updating stock, preventing two concurrent requests from both consuming the same units. The PostgreSQL 18 container mounts its named volume at `/var/lib/postgresql`; container CI writes a probe row, recreates that container and reads the row back to detect persistence regressions.

## Security model

The demonstration uses a stateless bearer-token flow. `POST /api/v1/auth/token` verifies BCrypt-hashed in-memory credentials and returns a short-lived HS256 JWT. The resource server validates its signature, issuer and expiration on every protected request. Role claims enforce:

- `VIEWER`: read endpoints.
- `MANAGER`: read and write endpoints.

User credentials, signing secret, issuer and lifetime are supplied as environment variables. Base configuration contains no password or signing-secret fallback, Compose requires all secrets, and the signing secret is rejected at startup if it is shorter than 32 bytes. Known demonstration values exist only in the explicitly activated `dev` profile. A production evolution would delegate authentication and asymmetric token signing to an OpenID Connect provider instead of issuing tokens locally.

Authentication failures from both the login controller and bearer-token filter, plus authorization failures, use the same Problem Details envelope and request correlation as application errors. Resource-server 401 responses also advertise the `Bearer` authentication scheme through `WWW-Authenticate`.

## Reliability

- Product and movement changes run inside transactions.
- Negative stock is rejected before persistence.
- Product rows use optimistic versioning in addition to the explicit movement lock.
- Validation failures, unreadable JSON, invalid enums/UUIDs, security failures and business conflicts return structured problem details.
- Every response receives a validated or generated `X-Request-Id`; application errors carry the same value, and a completion log records it with method, path, status and duration. The React client preserves the reference from either the Problem Details body or response header and shows it to the user.
- CORS explicitly allows the dashboard's read, create, update and deactivate methods, including `DELETE`; preflight behaviour is covered by an integration test.
- List endpoints allow-list sort fields, append an ID tie-breaker, return an explicit page contract and cap client-requested page sizes at 100.
- Health is public for orchestration; the remaining Actuator endpoints require the manager role.

## Testing strategy

- Unit tests cover SKU normalization, duplicate prevention and stock invariants.
- Full-context MockMvc tests exchange real signed tokens, verify viewer/manager permissions, persist data through JPA and assert the public error contract.
- Component tests validate frontend status rendering.
- Maven Verify generates a JaCoCo report and fails below the documented line-coverage threshold. Surefire starts the test JVM with Mockito's agent, avoiding dynamic runtime attachment on Java 21.
- CI builds and tests backend and frontend independently on every pull request.
- Root and frontend Docker contexts exclude `.git`, `.env`, dependency directories and generated output. Container CI additionally recreates PostgreSQL and verifies its named-volume data remains available.
- PostgreSQL Testcontainers tests remain the next compatibility layer; the current integration suite uses H2 in PostgreSQL mode to keep feedback fast and deterministic.

## Trade-offs

- Locally issued symmetric JWTs keep the portfolio runnable without an external identity provider, but key rotation, refresh tokens and account lifecycle belong in a production identity platform.
- A modular monolith keeps transactions local and deployment simple; modules could be separated later if operational scale justified it.
- The dashboard currently focuses on core inventory signals instead of attempting a complete ERP workflow.

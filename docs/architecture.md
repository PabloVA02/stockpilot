# Architecture and engineering notes

## Context

StockPilot is a modular monolith. This is deliberate: the domain is small enough that a distributed architecture would add deployment and consistency costs without a clear business benefit.

## Components

### React dashboard

The browser application manages the operational interface. TanStack Query owns server state, request status and cache invalidation after mutations. Credentials are held only in React memory for the demonstration session.

### Spring Boot API

The API is divided by domain package rather than by technical layer alone:

- `product`: catalogue and product lifecycle.
- `stock`: auditable inventory movements.
- `dashboard`: aggregated operational indicators.
- `common`: consistent errors and shared behavior.
- `config`: security, CORS and OpenAPI.

Controllers validate the HTTP contract. Services apply use-case rules and transaction boundaries. Repositories isolate persistence.

### PostgreSQL

PostgreSQL stores products and movements. Flyway owns the schema. An outbound operation obtains a database lock on the active product before validating and updating stock, preventing two concurrent requests from both consuming the same units.

## Security model

The demonstration uses HTTP Basic with BCrypt-hashed in-memory credentials:

- `VIEWER`: read endpoints.
- `MANAGER`: read and write endpoints.

Credentials are supplied as environment variables. A production evolution would delegate identity to an OpenID Connect provider and use short-lived access tokens.

## Reliability

- Product and movement changes run inside transactions.
- Negative stock is rejected before persistence.
- Product rows use optimistic versioning in addition to the explicit movement lock.
- Validation failures and business conflicts return structured problem details.
- Health and metrics endpoints are exposed through Actuator.

## Testing strategy

- Unit tests cover SKU normalization, duplicate prevention and stock invariants.
- Component tests validate frontend status rendering.
- CI builds and tests backend and frontend independently on every pull request.
- PostgreSQL Testcontainers tests are the next planned testing layer.

## Trade-offs

- Basic authentication keeps the portfolio runnable without an external identity provider, but it is not the final production model.
- A modular monolith keeps transactions local and deployment simple; modules could be separated later if operational scale justified it.
- The dashboard currently focuses on core inventory signals instead of attempting a complete ERP workflow.

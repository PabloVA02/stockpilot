# Interview preparation guide

Use this file to prepare before showing StockPilot. Run every flow yourself and explain it in your
own words.

## 60-second explanation

StockPilot is a full-stack inventory application built with Spring Boot, React and PostgreSQL. I
focused on the business rules that make inventory more than a CRUD demo: an outbound movement can
never create negative stock, each movement leaves an audit record and concurrent updates lock the
product row inside one transaction. The API separates viewer and manager permissions, exposes
structured errors and OpenAPI documentation, and the React client uses TanStack Query for server
state. Flyway, Docker Compose and CI make it reproducible.

## Questions you must be ready to answer

1. **Why pessimistic locking for a movement?** It serialises competing writes to the same product so
   both requests cannot validate against the same old stock value.
2. **Why also keep an optimistic `version` column?** It provides an additional change-conflict signal
   for product updates. Pessimistic locking is used where the stock invariant is critical.
3. **Why a modular monolith?** The domain is small and needs local transactions. Microservices would
   add network and consistency complexity without a demonstrated scaling need.
4. **Why DTOs?** They keep database entities and internal fields out of the public API contract.
5. **Why Flyway instead of automatic schema updates?** Versioned, reviewable migrations make
   environments reproducible.
6. **What would you improve first?** PostgreSQL integration tests with Testcontainers, OpenID Connect
   and charts/date filters for movements.

## Hands-on checklist

- Create a product and explain each validation.
- Perform inbound and outbound movements.
- Try to remove more units than exist and inspect the problem response.
- Log in as viewer and explain why a write is forbidden.
- Run all backend and frontend tests.
- Locate the transaction and lock used by the stock movement.
- Draw the request path from React through Nginx to PostgreSQL.

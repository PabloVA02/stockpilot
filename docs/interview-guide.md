# Interview preparation guide

Use this file to prepare before showing StockPilot. Run every flow yourself and explain it in your
own words.

## 60-second explanation

StockPilot is a full-stack inventory application built with Spring Boot, React and PostgreSQL. I
focused on the business rules that make inventory more than a CRUD demo: an outbound movement can
never create negative stock, each movement leaves an audit record and concurrent updates lock the
product row inside one transaction. The API issues short-lived signed JWTs and separates viewer and
manager permissions. Structured errors, logs and responses share a safe request ID, while explicit
page DTOs, allow-listed sorts and an ID tie-breaker keep traversal stable. The React client uses
TanStack Query for server state and accessible page controls, and
Flyway, Docker Compose, integration tests, a coverage gate and CI make the project reproducible.

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
7. **Why JWT instead of sending a password on every request?** Credentials are exchanged once. The
   API then validates a signed, expiring token without creating server-side sessions. The role claims
   drive authorization at the endpoint boundary.
8. **Why is the local JWT design not the final production identity solution?** A real system needs
   user lifecycle, key rotation, revocation, MFA and asymmetric signing. Those concerns should be
   delegated to an OpenID Connect provider rather than rebuilt inside this inventory service.
9. **What does `X-Request-Id` solve?** The dashboard displays the identifier returned with an error and
   an operator can find the matching completed-request log, including HTTP method, path, status and
   duration. Incoming values are length- and character-validated before entering logs to prevent
   log-forging input.
10. **Why return your own page response?** It keeps the public JSON independent from Spring Data's
    internal `PageImpl` representation and makes pagination metadata explicit. Sort fields are
    allow-listed and the UUID is a final tie-breaker, so equal business values do not jump between pages.
11. **What does the integration test prove?** It starts the complete Spring context, obtains real
    signed tokens, executes HTTP requests through the security chain and persists through JPA. That
    complements faster isolated unit tests rather than replacing them.
12. **Why are there no password defaults in the base profile or Compose file?** A forgotten override
    would otherwise deploy publicly known credentials. Compose fails fast when secrets are missing;
    known convenience values live only in the explicitly selected local `dev` profile.
13. **How are security and binding errors kept consistent?** Controller advice handles body,
    parameter and validation failures, while a Spring Security entry point/denied handler writes the
    same Problem Details fields for 401 and 403. Resource-server 401 responses include
    `WWW-Authenticate: Bearer`, and the request filter supplies the shared request ID.
14. **How do you know the PostgreSQL volume is really persistent?** The PostgreSQL 18 image mounts the
    named volume at its version-appropriate `/var/lib/postgresql` path. CI writes a probe row, forcibly
    recreates the database container and reads the row back before tearing down the stack.
15. **Why configure Mockito as a test JVM agent?** Modern Mockito uses instrumentation for inline
    mocking. Supplying the agent when Surefire starts the Java 21 process avoids unsupported dynamic
    self-attachment while preserving the same test behaviour.

## Hands-on checklist

- Create a product and explain each validation.
- Perform inbound and outbound movements.
- Try to remove more units than exist and inspect the problem response.
- Obtain viewer and manager tokens, inspect their expiry/role claims and explain why a viewer write is forbidden.
- Trigger missing/invalid-token 401, viewer 403, malformed JSON, invalid enum, UUID and sort errors; compare their contract.
- Traverse two inventory pages and explain the deterministic ordering and 100-item limit.
- Send a known `X-Request-Id`, trigger a validation error and trace the value through header, body,
  dashboard message and completed-request log.
- Run `./mvnw verify`, open `target/site/jacoco/index.html` and explain the quality gate.
- Run all frontend tests and the production build.
- Locate the transaction and lock used by the stock movement.
- Draw the request path from React through Nginx to PostgreSQL.

# AgendaFlow Notification Service

Quarkus microservice that will process AgendaFlow notifications in later phases. Its future scope
includes provider selection, reminders, delivery retries and delivery-state tracking.

## Status

**Phase 2 — Notification request contract validation.** The service exposes a validation-only
endpoint for candidate notification requests. It validates and normalizes the contract but does
not send, store, enqueue or acknowledge delivery of a notification.

The technical foundation from Phase 1 remains available: correlation IDs, uniform errors, typed
future-provider configuration, OpenAPI, health and safe logging.

## Stack

| Component | Version |
| --- | --- |
| Java | 21 |
| Maven Wrapper | 3.9.16 |
| Quarkus | 3.33.3 LTS |
| REST/JSON | Quarkus REST with Jackson |
| Validation | Hibernate Validator |
| API/health | SmallRye OpenAPI and SmallRye Health |
| Tests | JUnit 5 through Quarkus Test and REST Assured |

No persistence, messaging or provider extension is installed.

## Requirements and commands

JDK 21 is required. The first build may require internet access to obtain Maven dependencies.

```cmd
mvnw.cmd --version
mvnw.cmd quarkus:dev
mvnw.cmd clean test
mvnw.cmd clean verify
```

Development mode listens on port `8081`; tests use an automatically selected port. JVM packaging
is written to `target\quarkus-app`.

## HTTP contract

| Route | Purpose |
| --- | --- |
| `POST /api/v1/notification-requests/validate` | Validate and normalize a candidate contract only |
| `GET /api/v1/system/info` | Non-sensitive service information |
| `/q/openapi` | OpenAPI document |
| `/q/swagger-ui` | Swagger UI in development mode |
| `/q/health`, `/q/health/live`, `/q/health/ready` | Standard process health |

The service intentionally does **not** expose `POST /api/v1/notification-requests`. A successful
validation returns `200 OK`, never `202 Accepted`, and contains no request, delivery or provider
identifier. See the complete [notification request contract](docs/api/notification-request-contract.md).

Every response includes `X-Correlation-ID`. Validation errors preserve that value in both the
header and the [uniform error body](docs/api/error-format.md).

## Contract boundaries

The input accepts an organization ID, optional appointment ID, `EMAIL` channel, controlled template
code, email recipient, optional locale and future UTC schedule, plus a bounded flat map of string
variables. Unknown fields, nested objects, credentials, secrets and complete HTML documents are
rejected.

Email normalization trims the value and lowercases only the domain, preserving the local part.
Locale syntax is validated and normalized without external lookup. The service performs no DNS or
mailbox verification and makes no request to the Spring Boot API.

## Logging and health

Successful validation logs only correlation ID, organization ID, channel, template code and
`VALID`. Recipient email, variables, template content and personal data are not logged. Completed
HTTP requests continue to log method, path and status through the existing correlation filter.

Health reflects only Quarkus process readiness/liveness. There are no fake provider, PostgreSQL,
broker, email or Spring API checks.

## Package structure

```text
com.flakomencia.agendaflow.notification
├── api             # Technical and contract-validation resources, models and error mappers
├── application     # Pure contract validation and normalization
├── config          # Typed configuration and OpenAPI metadata
├── domain          # NotificationChannel (EMAIL only)
├── health          # Reserved for future real integration checks
└── infrastructure  # Correlation and HTTP infrastructure
```

## Typed future configuration

`NOTIFICATION_PROVIDER`, `EMAIL_FROM` and `SPRING_API_BASE_URL` remain documented placeholders.
They do not initialize a provider, sender or service-to-service client.

## Related projects

- [agendaflow-api](../agendaflow-api)
- [agendaflow-web](../agendaflow-web)

## Not implemented

- Real notification intake or `202 Accepted` processing.
- Email delivery, provider SDKs, templates, attachments or delivery state.
- Event consumers, Kafka, RabbitMQ, Azure Service Bus, polling, schedulers or retries.
- PostgreSQL, JDBC, ORM, Panache, Flyway, entities or repositories.
- Spring Boot communication, JWT, Basic Auth or service authorization.
- Docker, cloud deployment or CI/CD.

The validation endpoint may remain open during development. It must be protected or removed when a
real intake mechanism is introduced.

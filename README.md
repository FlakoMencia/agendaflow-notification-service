# AgendaFlow Notification Service

Quarkus microservice that will process AgendaFlow notifications in later phases. Its future scope
includes provider selection, reminders, delivery retries and delivery-state tracking.

## Status

**Phase 1 — Technical contract and observability.** The service provides correlation IDs, uniform
technical errors, typed future-provider configuration, OpenAPI metadata, readable logs and health.
It does not send notifications, consume events or use persistence.

## Service boundary

Future responsibilities include processing notification requests, selecting providers, retrying
deliveries, recording outcomes and exposing observability. Users, appointments, permissions and the
complete AgendaFlow domain belong to the main API. See
[service boundaries](docs/architecture/service-boundaries.md).

## Stack

| Component | Version |
| --- | --- |
| Java | 21 |
| Maven Wrapper | 3.9.16 |
| Quarkus | 3.33.3 LTS |
| REST | Quarkus REST with Jackson |
| Validation | Hibernate Validator |
| API/health | SmallRye OpenAPI and SmallRye Health |
| Tests | JUnit 5 through Quarkus Test and REST Assured |

## Requirements and commands

JDK 21 is required. The first build may require internet access to obtain Maven dependencies.

```cmd
mvnw.cmd --version
mvnw.cmd quarkus:dev
mvnw.cmd clean test
mvnw.cmd clean verify
```

Development mode listens on port `8081`; tests use an automatically selected port. JVM packaging
is written to `target\quarkus-app` and can be run with:

```cmd
java -jar target\quarkus-app\quarkus-run.jar
```

Native packaging is outside this phase.

## HTTP contract

| Route | Purpose |
| --- | --- |
| `GET /api/v1/system/info` | Non-sensitive service status, phase and version |
| `/q/openapi` | OpenAPI document |
| `/q/swagger-ui` | Swagger UI in development mode |
| `/q/health` | Aggregate health |
| `/q/health/live` | Liveness |
| `/q/health/ready` | Readiness |

`GET /api/v1/system/info` reports phase `technical-foundation`. No notification endpoint exists.
Every HTTP response includes `X-Correlation-ID`; see the
[correlation contract](docs/api/correlation-id.md). Technical failures use the documented
[uniform error format](docs/api/error-format.md).

OpenAPI identifies version `0.0.1`, provides generic technical contact metadata, and defines
`System` and `Health` tags. Only real production endpoints are included.

## Typed configuration

SmallRye Config Mapping exposes the following future-provider settings without initializing a
provider:

| Environment variable | Development default | Purpose |
| --- | --- | --- |
| `NOTIFICATION_PROVIDER` | `not-configured` | Future provider selection |
| `EMAIL_FROM` | `no-reply@example.com` | Future sender identity |
| `SPRING_API_BASE_URL` | `http://localhost:8080` | Future main API base URL |

`.env.example` documents these non-secret placeholders. A real `.env` file must not be committed.
The URL is parsed as a typed `URI`; no connection is opened.

## Logging and health

Development logs use INFO by default and add the correlation ID through MDC when a Jakarta REST
request is active. Completed requests log only HTTP method, path and status—never bodies,
credentials or personal data. Unexpected failures are logged with correlation context while their
public response remains generic.

Health currently reflects only Quarkus process readiness/liveness. Provider, email, PostgreSQL,
broker and Spring API checks will be added only when those integrations actually exist.

## Package structure

```text
com.flakomencia.agendaflow.notification
├── api             # Technical endpoint, response models and error mappers
├── application     # Future use cases
├── config          # Typed configuration and OpenAPI metadata
├── domain          # Future notification domain
├── health          # Future real integration checks
└── infrastructure  # Correlation and HTTP infrastructure
```

## Tests

The suite verifies system information, correlation preservation/generation, response headers,
validation/404/500 error safety, OpenAPI metadata, typed defaults and readiness. Failure-inducing
resources exist only under `src/test` and are not packaged in production.

## Related projects

- [agendaflow-api](../agendaflow-api)
- [agendaflow-web](../agendaflow-web)

## Not implemented

- `POST /notifications` or any functional notification API.
- Email, SMS, WhatsApp or external provider integration.
- Event consumers, brokers, polling, schedulers or real retries.
- PostgreSQL, ORM, Panache, JDBC, Flyway, entities or repositories.
- JWT, login, users or service-to-service calls.
- Docker, cloud deployment or CI/CD.

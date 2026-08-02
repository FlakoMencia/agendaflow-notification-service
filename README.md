# AgendaFlow Notification Service

AgendaFlow Notification Service is the future notification-processing microservice for the AgendaFlow platform. It will eventually coordinate reminders, email delivery, retries, and delivery-state tracking.

## Status

**Phase 0 — Technical bootstrap.** The service currently exposes only technical bootstrap, OpenAPI, and health endpoints. It does not send notifications or consume events.

## Service boundaries

Future responsibilities include notification orchestration, provider integration, retry policies, and delivery status. Core scheduling, appointments, organization management, authentication, and primary business data belong to the main AgendaFlow API, not this service.

## Stack

| Component | Version |
| --- | --- |
| Java | 21 |
| Maven Wrapper | 3.9.16 |
| Quarkus | 3.33.3 LTS |
| REST | Quarkus REST with Jackson |
| Tests | JUnit 5 through Quarkus Test and REST Assured |

## Requirements

- JDK 21
- Internet access on the first Maven build to download dependencies

No database, message broker, notification provider, or local infrastructure is required in Phase 0.

## Maven Wrapper commands

```cmd
mvnw.cmd --version
mvnw.cmd clean test
mvnw.cmd clean verify
```

On Unix-like systems use `./mvnw` instead of `mvnw.cmd`.

## Development mode

```cmd
mvnw.cmd quarkus:dev
```

The service listens on port `8081`. Quarkus Dev UI may expose additional development tooling, but it is not part of the public service contract.

## Tests

```cmd
mvnw.cmd clean test
```

Tests use an automatically assigned HTTP port and validate the technical information and health endpoints.

## JVM packaging

```cmd
mvnw.cmd clean package
java -jar target\quarkus-app\quarkus-run.jar
```

Native-image tests and packaging are intentionally outside Phase 0.

## Technical endpoints

| Route | Purpose |
| --- | --- |
| `GET /api/v1/system/info` | Bootstrap service information |
| `/q/openapi` | OpenAPI document |
| `/q/swagger-ui` | Swagger UI in development mode |
| `/q/health` | Aggregate health |
| `/q/health/live` | Liveness |
| `/q/health/ready` | Readiness |

## Future environment variables

`.env.example` documents placeholders for `NOTIFICATION_PROVIDER`, `EMAIL_FROM`, and `SPRING_API_BASE_URL`. The service does not read these variables yet, and a real `.env` file must not be committed.

## Package structure

```text
com.flakomencia.agendaflow.notification
├── api
├── application
├── config
├── domain
├── health
└── infrastructure
```

Only the technical system resource has an implementation in Phase 0. Empty package boundaries are preserved with `package-info.java` files.

## Related projects

- [agendaflow-api](../agendaflow-api)
- [agendaflow-web](../agendaflow-web)

## Not implemented

- Email, SMS, WhatsApp, or other delivery providers
- Event consumers, queues, topics, polling, or schedulers
- Retry policies and delivery-state persistence
- PostgreSQL, ORM, Panache, JDBC, Flyway, entities, or repositories
- JWT, login, users, or service-to-service authentication
- Docker, cloud deployment, or CI/CD

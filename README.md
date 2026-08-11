# AgendaFlow Notification Service

Quarkus microservice responsible for durable intake, preparation, and technical dispatch of
AgendaFlow notifications.

## Status

**Phase 6 - durable Inbox, idempotency and Mailer adapter.** Appointment `CREATED`, `RESCHEDULED`,
and `CANCELLED` events can now be durably accepted and asynchronously dispatched as plain-text email.

## Stack

- Java 21, Maven Wrapper 3.9.16 and Quarkus 3.33.3 LTS.
- Quarkus REST/Jackson, Hibernate ORM, PostgreSQL JDBC, Flyway, Scheduler and Mailer.
- PostgreSQL 18.4 Testcontainers through Quarkus Dev Services.
- SmallRye JWT, OpenAPI and Health.

## Development

```cmd
mvnw.cmd quarkus:dev
mvnw.cmd clean test
mvnw.cmd clean verify
```

The service listens on port `8081`. OpenAPI is at `/q/openapi`, Swagger UI at `/q/swagger-ui`, and
health/readiness at `/q/health` and `/q/health/ready`.

Database and SMTP variables are documented in `.env.example`. The service owns only the
`notification_service` schema. Hibernate validates it and Flyway V1 creates its Inbox and delivery
tables. No table or entity is shared with the Spring API.

Mailer is mocked in dev and test, so no external SMTP connection or credentials are required. The
production profile reads SMTP settings from environment variables and never stores credentials.

## Processing semantics

Authenticated intake returns `202` only after durable persistence. `eventId` is protected by a
database unique constraint and duplicate requests do not create another inbox or delivery.
Concurrent workers claim with PostgreSQL `FOR UPDATE SKIP LOCKED`; retries use bounded exponential
backoff, exhausted events stop retrying, and stale processing claims recover automatically.

`DISPATCHED` means the Mailer operation was accepted, not that the recipient's mailbox confirmed
delivery. Rendered subjects/bodies are not persisted.

See [request contract](docs/api/notification-request-contract.md),
[durable intake](docs/architecture/durable-intake.md),
[Inbox/idempotency](docs/architecture/inbox-and-idempotency.md), and
[pipeline](docs/architecture/notification-pipeline.md).

## Not implemented

- HTML, attachments, SMS, WhatsApp, reminders, waitlists, callbacks or administration UI.
- Kafka, RabbitMQ, Azure Service Bus, Redis or another broker.
- SendGrid, SES, Azure Communication Services or another provider-specific adapter.
- Final-mailbox delivery confirmation, Docker/cloud deployment or CI/CD.

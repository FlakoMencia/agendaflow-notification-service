# Service boundaries

AgendaFlow Notification Service will own notification processing while keeping AgendaFlow's core
business domain in the main Spring Boot API.

## Current Phase 2 responsibility

The service validates a candidate notification request contract at
`POST /api/v1/notification-requests/validate`. Validation is a pure in-process operation: it applies
Bean Validation, safe normalization and defensive contract rules, then returns a validation result.

It does not create a notification, delivery attempt or scheduled job. The application validation
service has no repository, broker, provider, sender or external API collaborator.

## Future responsibilities

- Receive notification requests through a contract and transport that have not yet been selected.
- Select and integrate notification providers.
- Apply retry and scheduling policies.
- Record delivery attempts and outcomes.
- Expose real integration health and operational observability.

## Explicitly outside this service

- User, role and permission administration.
- Appointment, scheduling, customer or organization management.
- Storage of the complete AgendaFlow business domain.
- Sharing JPA entities or persistence models with the Spring Boot API.
- Validating that organizations, appointments or email mailboxes exist during contract validation.

There is no communication with `agendaflow-api` in Phase 2. `organizationId` and `appointmentId`
are syntactically validated only. Kafka, RabbitMQ, Azure Service Bus and other transports remain
future architectural decisions; none is preferred or implied.

The validation endpoint may be publicly available in development. It must be protected or removed
when the real intake mechanism and service-to-service authorization are defined.

# Service boundaries

AgendaFlow Notification Service will own notification processing while the main Spring Boot API
continues to own organizations, appointments, customers, users and authorization.

## Current Phase 4 responsibility

The service validates a candidate request at
`POST /api/v1/notification-requests/validate`. This is the route already published and tested in
Phases 2 and 3; it remains unchanged for compatibility. Validation is in-process and returns the
same `200 OK` contract without rendering, sending, storing, enqueueing or acknowledging anything.

Phase 4 also provides internal-only building blocks:

- immutable provider-neutral notification models;
- mapping from the existing HTTP DTO to that model;
- a deliberately limited plain-text renderer;
- a preparation service that creates `RenderedNotification`;
- a delivery port with no production implementation.

## Future responsibilities

- Select a real authenticated intake contract and transport.
- Select a template source and governance model.
- Implement a provider adapter behind the delivery port.
- Define retry, scheduling and delivery-observation policies.
- Persist only the delivery state justified by those decisions.

## Explicitly outside this service

- Managing appointments, customers, organizations, users, roles or permissions.
- Sharing JPA entities or persistence models with `agendaflow-api`.
- Validating that an organization, appointment or mailbox exists during contract validation.
- Sending email, SMS or WhatsApp in Phase 4.
- Selecting Kafka, RabbitMQ, Azure Service Bus, SMTP or a cloud provider prematurely.

There is no outbound communication with `agendaflow-api`, provider credential, database,
repository, broker, scheduler, consumer or retry worker in Phase 4.

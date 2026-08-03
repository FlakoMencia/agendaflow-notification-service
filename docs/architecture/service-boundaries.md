# Service boundaries

AgendaFlow Notification Service will own notification processing while keeping AgendaFlow's core
business domain in the main Spring Boot API.

## Future responsibilities

- Process notification requests received through a contract that has not yet been selected.
- Select and integrate notification providers.
- Apply delivery retry policies.
- Record delivery attempts and outcomes.
- Expose health and operational observability.

## Explicitly outside this service

- User, role and permission administration.
- Appointment, scheduling, customer or organization management.
- Storage of the complete AgendaFlow business domain.
- Sharing JPA entities or persistence models with the Spring Boot API.

This phase exposes no notification endpoint and makes no network call to the Spring Boot API. The
choice among Kafka, RabbitMQ, Azure Service Bus or another transport remains an architectural
decision for a later phase; no broker is preferred or implied here.

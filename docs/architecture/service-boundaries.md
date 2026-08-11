# Service boundaries

AgendaFlow Notification Service owns durable notification intake and dispatch. The Spring Boot API
continues to own organizations, appointments, customers, users, authorization and the producer
Transactional Outbox.

## Current Phase 6 responsibility

- Accept appointment events at `POST /api/v1/notification-requests` using service JWT authorization.
- Own the `notification_service` PostgreSQL schema, Inbox and delivery-attempt records.
- Deduplicate producer-assigned `eventId` values and return `202 Accepted` only after persistence.
- Prepare built-in appointment templates and dispatch plain-text email through
  `NotificationDeliveryPort` and Quarkus Mailer.
- Claim work safely across instances, retry with bounded backoff and recover stale processing.

The service does not query Spring-owned tables and has no foreign keys to the Spring database.

## Explicitly outside this service

- Managing appointments, customers, organizations, users, roles or permissions.
- Sharing Spring entities or calling `agendaflow-api` during notification preparation.
- Kafka, RabbitMQ, Azure Service Bus, Redis or any broker.
- Provider-specific SaaS adapters, SMS, WhatsApp, HTML email, attachments, reminders or waitlists.
- Confirmation of final mailbox delivery: `DISPATCHED` only means the mail adapter accepted the
  message.

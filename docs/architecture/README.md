# Architecture documentation

This folder records service boundaries and technical design decisions.

- [Service boundaries](service-boundaries.md)
- [Durable intake](durable-intake.md)
- [Inbox and idempotency](inbox-and-idempotency.md)
- [Notification pipeline](notification-pipeline.md)
- [Service-to-service authentication](service-authentication.md)
- [Appointment notifications](appointment-notifications.md)

Phase 6 introduces the service-owned PostgreSQL Inbox, bounded asynchronous processing and a
Quarkus Mailer adapter. No broker or external SaaS mail provider is integrated.

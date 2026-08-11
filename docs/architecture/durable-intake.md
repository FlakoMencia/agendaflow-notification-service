# Durable intake

Spring's transactional outbox posts appointment events to `POST /api/v1/notification-requests`.
The service returns `202 Accepted` only after PostgreSQL accepts the inbox insert in a transaction.
Rendering and SMTP are deliberately excluded from the HTTP transaction and happen in the worker.

The accepted contract carries the producer's `BIGINT eventId`; Quarkus never replaces it. A
correlation ID is retained only for tracing and is not an idempotency key.

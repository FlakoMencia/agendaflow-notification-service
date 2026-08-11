# Inbox and idempotency

Flyway V1 owns `notification_service.notification_inbox` and
`notification_service.notification_deliveries`. There are no foreign keys to Spring's database.

`UNIQUE(event_id)` is enforced by PostgreSQL. Intake uses `INSERT ... ON CONFLICT DO NOTHING`, so
concurrent delivery of one event creates exactly one inbox row. Both first and duplicate requests
return `202`; the response distinguishes duplicates without claiming dispatch or delivery.

Workers claim `PENDING` rows using `FOR UPDATE SKIP LOCKED`, commit the short claim transaction, and
then dispatch. Failed rows use bounded exponential backoff and eventually become `EXHAUSTED`.
Abandoned `PROCESSING` claims return to `PENDING` after the configured timeout.

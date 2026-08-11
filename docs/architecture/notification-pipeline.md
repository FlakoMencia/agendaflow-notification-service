# Notification pipeline

```text
Spring Outbox -> authenticated HTTP -> Quarkus Inbox -> claim -> prepare/render -> Mailer -> audit
```

The worker reconstructs `AppointmentNotificationEvent`, invokes
`AppointmentNotificationPreparationService`, and dispatches the resulting plain-text
`RenderedNotification` through `NotificationDeliveryPort`. The infrastructure adapter uses Quarkus
Mailer; application and domain code do not depend on Mailer, Hibernate, or Panache.

Every attempt creates a delivery record without persisting subject or rendered body. `DISPATCHED`
means Quarkus Mailer accepted the SMTP operation; it does not prove arrival in the final mailbox.
Failures are recorded as `FAILED` with bounded, sanitized technical information.

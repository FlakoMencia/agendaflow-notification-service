# Appointment notifications

Phase 5 introduces an internal, provider-neutral contract for appointment events.

| Event type | Template code | Required variables |
| --- | --- | --- |
| `CREATED` | `APPOINTMENT_CONFIRMATION` | `customerName`, `serviceName`, `specialistName`, `appointmentDateTime`, `branchName` |
| `RESCHEDULED` | `APPOINTMENT_RESCHEDULED` | `customerName`, `serviceName`, `specialistName`, `appointmentDateTime`, `branchName` |
| `CANCELLED` | `APPOINTMENT_CANCELLED` | `customerName`, `serviceName`, `appointmentDateTime`, `branchName` |

The mapping is an explicit switch and never relies on matching enum names. Technical IDs remain
event metadata (`Long`) and are not inserted into template content.

## Catalog and locale

`BuiltInNotificationTemplateCatalog` contains only neutral plain-text EMAIL definitions. There is
no HTML or provider-specific metadata. `en-US` is the sole base catalog; a missing or unsupported
locale explicitly falls back to `en-US`. This is not a claim of complete internationalization and
no translations are invented.

All required values must exist and be non-blank before rendering. Additional variables remain
allowed under the existing limits and are ignored when the selected template does not reference
them.

## Delivery and intake boundary

Preparation normalizes the recipient, resolves the template, renders subject/body and returns a
`RenderedNotification`. It does not call `NotificationDeliveryPort` and does not send or persist
anything.

`AppointmentNotificationIntake` marks the future application entry boundary, but has no HTTP or
messaging adapter. A durable producer-created `eventId`, transport, inbox and
`notification:submit` permission must be designed before real intake and `202 Accepted` can exist.

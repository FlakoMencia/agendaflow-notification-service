# Notification request contract

`POST /api/v1/notification-requests` accepts the exact appointment event produced by AgendaFlow API:

```json
{
  "eventId": 123,
  "organizationId": 10,
  "appointmentId": 200,
  "type": "CREATED",
  "recipient": "customer@example.com",
  "locale": "en-US",
  "occurredAt": "2026-08-10T12:00:00Z",
  "variables": {
    "customerName": "Example",
    "serviceName": "Consultation",
    "specialistName": "Taylor",
    "appointmentDateTime": "2026-08-17T09:00:00-06:00",
    "branchName": "Central"
  }
}
```

Unknown fields and malformed values return `400`. Missing/invalid authentication returns `401`; a
service token without `notification:submit` or a user token returns `403`.

First acceptance returns `202` with `duplicate=false`. A previously accepted `eventId` also returns
`202`, with `duplicate=true`. Neither response means `SENT` or `DELIVERED`.

The existing `/validate` route remains validation-only and requires `notification:validate`.

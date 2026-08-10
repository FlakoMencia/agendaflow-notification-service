# Notification request validation contract

`POST /api/v1/notification-requests/validate` checks whether a candidate notification request has
the shape supported by AgendaFlow Notification Service Phase 2. It returns `200 OK` only after
validation and safe normalization.

This endpoint does not send, store, enqueue, schedule or acknowledge processing. It never returns
`202 Accepted`, a request ID, delivery ID, provider message ID, `ACCEPTED` or `SENT`.

## Request

```json
{
  "organizationId": 10,
  "appointmentId": 25,
  "channel": "EMAIL",
  "templateCode": "APPOINTMENT_CONFIRMATION",
  "recipient": "customer@example.com",
  "locale": "en-US",
  "scheduledAt": "2026-08-04T15:00:00Z",
  "variables": {
    "customerName": "Example",
    "appointmentTime": "10:00 AM"
  }
}
```

| Field | Required | Rules |
| --- | --- | --- |
| `organizationId` | Yes | Positive JSON integer mapped to Java `Long` |
| `appointmentId` | No | Positive JSON integer mapped to Java `Long` |
| `channel` | Yes | `EMAIL` only |
| `templateCode` | Yes | Maximum 80 characters; uppercase letters, numbers and single underscores |
| `recipient` | Yes | Valid email, maximum 254 characters |
| `locale` | No | BCP 47-style tag, maximum 35 characters |
| `scheduledAt` | No | Valid ISO-8601 UTC instant that is not in the past |
| `variables` | No | At most 20 flat string entries |

Variable keys are limited to 64 characters and controlled letters/numbers with `.`, `_` or `-`
separators. Values are limited to 500 characters. Nested objects, null values, sensitive keys such
as passwords/tokens/credentials, and complete HTML documents are rejected.

The JSON contract is strict. Unknown fields—including secret or attachment payloads—are rejected.
There is no field for raw template HTML or attachments.

## Normalization

- Surrounding whitespace is removed from the recipient.
- Only the email domain is lowercased; the local part is preserved.
- A supplied locale is parsed and normalized as a language tag.
- Variables are copied into an unmodifiable map.

No DNS lookup, mailbox verification, provider call or Spring Boot API request occurs.

## Response

```json
{
  "valid": true,
  "channel": "EMAIL",
  "templateCode": "APPOINTMENT_CONFIRMATION",
  "normalizedRecipient": "customer@example.com",
  "scheduled": true,
  "correlationId": "7cf0ae75-7fbb-4f43-8928-20b85f9ec89e"
}
```

The `scheduled` flag only confirms that a valid future instant was supplied. It does not mean a job
was created. `X-Correlation-ID` is preserved or generated and matches the response field.

## Errors

Invalid requests return `400 Bad Request` using the uniform error contract. Relevant codes include
`VALIDATION_ERROR`, `UNSUPPORTED_CHANNEL`, `SCHEDULED_AT_IN_PAST`, `INVALID_LOCALE`,
`SECRET_VARIABLE_NOT_ALLOWED`, `HTML_DOCUMENT_NOT_ALLOWED` and `MALFORMED_JSON`.

The endpoint can remain available during development. It must be protected or retired once a real
intake contract is selected. The future intake transport and integration with `agendaflow-api`
remain undecided; this document does not select Kafka, RabbitMQ or Azure Service Bus.

## Phase 4 internal mapping

After Jakarta Bean Validation, `NotificationRequestMapper` converts the DTO into the immutable
internal `NotificationRequest`. Recipient domain and locale normalization happen at that boundary;
the contract validation service then applies scheduling and defensive variable rules to the domain
model. The response shape above is unchanged.

The internal template renderer is deliberately not exposed here. This endpoint is not a preview,
does not accept template text and never returns rendered content.

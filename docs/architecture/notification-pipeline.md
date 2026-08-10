# Notification preparation pipeline

The intended long-term flow is:

```text
authenticated intake
    ↓
contract validation
    ↓
template lookup
    ↓
plain-text rendering
    ↓
delivery port
    ↓
provider adapter
```

Phase 4 implements only contract validation, mapping to an internal immutable model, deterministic
plain-text rendering, preparation of `RenderedNotification`, and the `NotificationDeliveryPort`
interface. There is no real intake endpoint, template repository, port implementation, provider
adapter, persistence, queue, scheduler or retry mechanism.

## Current internal flow

The existing validation endpoint follows this path without rendering content:

```text
NotificationRequestValidationRequest
    ↓ Bean Validation
NotificationRequestMapper
    ↓ normalization and boundary mapping
NotificationRequest
    ↓ defensive contract rules
ValidatedNotificationRequest
    ↓ response mapping
NotificationRequestValidationResponse
```

`NotificationPreparationService` is an internal, separately tested component. Given a
`ValidatedNotificationRequest` and caller-supplied synthetic template text, it delegates to the
renderer and creates a provider-neutral `RenderedNotification`. Nothing invokes a delivery port in
production.

## Rendering rules

- Exact placeholder syntax: `{{variableName}}`.
- Variable names follow the existing flat-variable key grammar.
- Missing variables fail explicitly; unused additional variables are allowed.
- Rendering is one pass: placeholders contained inside variable values are not evaluated again.
- Empty, malformed or oversized templates fail.
- Output above 20,000 characters fails while it is being built.
- HTML, Markdown-like text and `${expressions}` are ordinary text; no syntax is interpreted beyond
  the exact placeholder form.

The renderer has no access to reflection, expressions, scripts, files, environment variables,
network services or arbitrary helpers. It does not log templates, recipients, variables or rendered
content.

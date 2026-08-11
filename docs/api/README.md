# API documentation

This folder records the HTTP and OpenAPI contracts exposed by the service.

- [Notification request contract](notification-request-contract.md)
- [Uniform error format](error-format.md)
- [Correlation ID contract](correlation-id.md)

`POST /api/v1/notification-requests` requires `notification:submit` and durably accepts an event
with `202`. The separate `/validate` route retains `notification:validate`. System info, OpenAPI and
SmallRye Health remain public.

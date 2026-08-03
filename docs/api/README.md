# API documentation

This folder records HTTP contracts that currently exist:

- [Notification request validation contract](notification-request-contract.md)
- [Uniform error format](error-format.md)
- [Correlation ID contract](correlation-id.md)

Phase 2 exposes `POST /api/v1/notification-requests/validate` strictly for contract validation and
`GET /api/v1/system/info` for technical information. It does not expose a real notification intake
endpoint. OpenAPI and standard SmallRye Health routes remain under `/q`.

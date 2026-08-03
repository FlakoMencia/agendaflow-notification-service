# API documentation

This folder records the HTTP conventions and OpenAPI contract for endpoints that actually exist.

- [Uniform error format](error-format.md)
- [Correlation ID contract](correlation-id.md)

Phase 1 exposes only `GET /api/v1/system/info` as an application endpoint. Standard Quarkus
OpenAPI and SmallRye Health routes remain available under `/q`.

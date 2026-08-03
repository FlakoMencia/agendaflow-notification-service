# Uniform error format

Technical API failures use one immutable JSON representation:

```json
{
  "timestamp": "2026-08-02T18:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/v1/example",
  "correlationId": "7cf0ae75-7fbb-4f43-8928-20b85f9ec89e"
}
```

| Situation | HTTP status | Code | Public message |
| --- | ---: | --- | --- |
| Request validation failure | 400 | `VALIDATION_ERROR` | `Request validation failed` |
| Unknown resource | 404 | `RESOURCE_NOT_FOUND` | `Resource not found` |
| Unexpected failure | 500 | `INTERNAL_ERROR` | `Unexpected error` |

The response never contains stack traces, exception class names, internal exception messages,
credentials, or configuration values. Unexpected failures are logged server-side with their
correlation ID so operators can investigate without expanding the public response.

Additional domain-specific codes will be defined only when real notification endpoints exist.

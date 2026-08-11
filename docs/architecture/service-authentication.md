# Service authentication

Both notification routes require a signed service JWT with the configured issuer and audience,
`sub=agendaflow-api`, and `token_use=service`.

- `/api/v1/notification-requests/validate` requires `notification:validate`.
- `/api/v1/notification-requests` requires `notification:submit`.

The capabilities are intentionally independent. User tokens are forbidden and no token, secret, or
SMTP credential is persisted or logged.

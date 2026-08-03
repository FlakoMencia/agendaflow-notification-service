# Correlation ID

Every HTTP response carries `X-Correlation-ID`.

1. A non-blank incoming `X-Correlation-ID` is preserved.
2. If the header is absent or blank, the service generates a UUID v4 using the JDK.
3. The value is returned in the response header.
4. Jakarta REST requests expose it through a request-scoped context and the logging MDC.
5. The request context and MDC entry are cleared after the REST response completes.

A global Quarkus HTTP handler establishes the header before routing so standard technical routes,
including SmallRye Health and OpenAPI, receive the same behavior even though they do not traverse
Jakarta REST filters. The application logs only method, path, status and correlation context; it
does not log request bodies, credentials or personal data.

Clients should propagate the value unchanged across service boundaries. It is an observability
identifier, not an authentication credential, and must not contain personal information.

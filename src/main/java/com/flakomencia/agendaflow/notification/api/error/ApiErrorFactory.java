package com.flakomencia.agendaflow.notification.api.error;

import java.time.Instant;
import java.util.Map;

import com.flakomencia.agendaflow.notification.api.model.ApiErrorResponse;
import com.flakomencia.agendaflow.notification.infrastructure.http.CorrelationIdContext;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;

@RequestScoped
public class ApiErrorFactory {

    @Inject
    CorrelationIdContext correlationIdContext;

    @Inject
    UriInfo uriInfo;

    @Inject
    HttpHeaders httpHeaders;

    public ApiErrorResponse create(int status, String code, String message) {
        return create(status, code, message, Map.of());
    }

    public ApiErrorResponse create(int status, String code, String message, Map<String, String> fieldErrors) {
        String correlationId = correlationIdContext.get();
        if (correlationId == null) {
            correlationId = httpHeaders.getHeaderString("X-Correlation-ID");
        }
        return new ApiErrorResponse(
                Instant.now(),
                status,
                code,
                message,
                uriInfo.getRequestUri().getRawPath(),
                correlationId,
                fieldErrors);
    }
}

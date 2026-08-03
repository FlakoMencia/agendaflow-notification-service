package com.flakomencia.agendaflow.notification.api.error;

import java.time.Instant;
import java.util.Map;

import com.flakomencia.agendaflow.notification.api.model.ApiErrorResponse;
import com.flakomencia.agendaflow.notification.infrastructure.http.CorrelationIdContext;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriInfo;

@RequestScoped
class ApiErrorFactory {

    @Inject
    CorrelationIdContext correlationIdContext;

    @Inject
    UriInfo uriInfo;

    ApiErrorResponse create(int status, String code, String message) {
        return create(status, code, message, Map.of());
    }

    ApiErrorResponse create(int status, String code, String message, Map<String, String> fieldErrors) {
        return new ApiErrorResponse(
                Instant.now(),
                status,
                code,
                message,
                uriInfo.getRequestUri().getRawPath(),
                correlationIdContext.get(),
                fieldErrors);
    }
}

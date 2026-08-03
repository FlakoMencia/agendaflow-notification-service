package com.flakomencia.agendaflow.notification.api.error;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.util.Map;

import com.flakomencia.agendaflow.notification.application.NotificationContractValidationException;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotificationContractValidationExceptionMapper
        implements ExceptionMapper<NotificationContractValidationException> {

    @Inject
    ApiErrorFactory errorFactory;

    @Override
    public Response toResponse(NotificationContractValidationException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(APPLICATION_JSON_TYPE)
                .entity(errorFactory.create(
                        Response.Status.BAD_REQUEST.getStatusCode(),
                        exception.code(),
                        exception.getMessage(),
                        Map.of(exception.field(), exception.getMessage())))
                .build();
    }
}

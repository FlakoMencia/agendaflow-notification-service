package com.flakomencia.agendaflow.notification.api.error;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Inject
    ApiErrorFactory errorFactory;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(APPLICATION_JSON_TYPE)
                .entity(errorFactory.create(
                        Response.Status.BAD_REQUEST.getStatusCode(),
                        "VALIDATION_ERROR",
                        "Request validation failed"))
                .build();
    }
}

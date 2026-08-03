package com.flakomencia.agendaflow.notification.api.error;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Inject
    ApiErrorFactory errorFactory;

    @Override
    public Response toResponse(NotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .type(APPLICATION_JSON_TYPE)
                .entity(errorFactory.create(
                        Response.Status.NOT_FOUND.getStatusCode(),
                        "RESOURCE_NOT_FOUND",
                        "Resource not found"))
                .build();
    }
}

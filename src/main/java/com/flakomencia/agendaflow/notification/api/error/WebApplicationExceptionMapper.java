package com.flakomencia.agendaflow.notification.api.error;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Inject
    ApiErrorFactory errorFactory;

    @Override
    public Response toResponse(WebApplicationException exception) {
        int status = exception.getResponse().getStatus();
        if (status == Response.Status.BAD_REQUEST.getStatusCode()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(APPLICATION_JSON_TYPE)
                    .entity(errorFactory.create(
                            status,
                            "MALFORMED_JSON",
                            "Malformed JSON request"))
                    .build();
        }

        return Response.status(status)
                .type(APPLICATION_JSON_TYPE)
                .entity(errorFactory.create(status, "HTTP_ERROR", "HTTP request failed"))
                .build();
    }
}

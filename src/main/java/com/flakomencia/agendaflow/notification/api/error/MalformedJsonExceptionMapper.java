package com.flakomencia.agendaflow.notification.api.error;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import com.fasterxml.jackson.core.JsonParseException;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class MalformedJsonExceptionMapper implements ExceptionMapper<JsonParseException> {

    @Inject
    ApiErrorFactory errorFactory;

    @Override
    public Response toResponse(JsonParseException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(APPLICATION_JSON_TYPE)
                .entity(errorFactory.create(
                        Response.Status.BAD_REQUEST.getStatusCode(),
                        "MALFORMED_JSON",
                        "Malformed JSON request"))
                .build();
    }
}

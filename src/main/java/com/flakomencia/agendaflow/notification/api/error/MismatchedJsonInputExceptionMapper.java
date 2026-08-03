package com.flakomencia.agendaflow.notification.api.error;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.flakomencia.agendaflow.notification.domain.NotificationChannel;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class MismatchedJsonInputExceptionMapper implements ExceptionMapper<MismatchedInputException> {

    @Inject
    ApiErrorFactory errorFactory;

    @Override
    public Response toResponse(MismatchedInputException exception) {
        if (NotificationChannel.class.equals(exception.getTargetType())) {
            return badRequest(
                    "UNSUPPORTED_CHANNEL",
                    "channel must be EMAIL",
                    Map.of("channel", "channel must be EMAIL"));
        }
        if (Instant.class.equals(exception.getTargetType())) {
            return badRequest(
                    "INVALID_SCHEDULED_AT",
                    "scheduledAt must be a valid UTC instant",
                    Map.of("scheduledAt", "scheduledAt must be a valid UTC instant"));
        }
        return badRequest("MALFORMED_JSON", "Malformed or unsupported JSON request", Map.of());
    }

    private Response badRequest(String code, String message, Map<String, String> fieldErrors) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(APPLICATION_JSON_TYPE)
                .entity(errorFactory.create(
                        Response.Status.BAD_REQUEST.getStatusCode(),
                        code,
                        message,
                        fieldErrors))
                .build();
    }
}

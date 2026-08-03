package com.flakomencia.agendaflow.notification.api.error;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.util.Map;
import java.util.TreeMap;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
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
        Map<String, String> fieldErrors = new TreeMap<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            fieldErrors.putIfAbsent(publicField(violation), violation.getMessage());
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .type(APPLICATION_JSON_TYPE)
                .entity(errorFactory.create(
                        Response.Status.BAD_REQUEST.getStatusCode(),
                        "VALIDATION_ERROR",
                        "Request validation failed",
                        fieldErrors))
                .build();
    }

    private String publicField(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        String[] fields = {
                "organizationId", "appointmentId", "channel", "templateCode",
                "recipient", "locale", "scheduledAt", "variables"
        };
        for (String field : fields) {
            if (path.contains(field)) {
                return field;
            }
        }
        return "request";
    }
}

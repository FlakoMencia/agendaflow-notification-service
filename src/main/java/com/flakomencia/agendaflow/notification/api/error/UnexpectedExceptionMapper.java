package com.flakomencia.agendaflow.notification.api.error;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import org.jboss.logging.Logger;

import com.flakomencia.agendaflow.notification.infrastructure.http.CorrelationIdContext;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UnexpectedExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(UnexpectedExceptionMapper.class);

    @Inject
    ApiErrorFactory errorFactory;

    @Inject
    CorrelationIdContext correlationIdContext;

    @Override
    public Response toResponse(Throwable exception) {
        LOG.errorf(exception, "Unexpected request failure [correlationId=%s]", correlationIdContext.get());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(APPLICATION_JSON_TYPE)
                .entity(errorFactory.create(
                        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                        "INTERNAL_ERROR",
                        "Unexpected error"))
                .build();
    }
}

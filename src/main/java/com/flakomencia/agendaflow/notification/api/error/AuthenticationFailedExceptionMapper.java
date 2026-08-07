package com.flakomencia.agendaflow.notification.api.error;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import org.jboss.logging.Logger;

import com.flakomencia.agendaflow.notification.infrastructure.security.ServiceTokenClaimsFilter;
import com.flakomencia.agendaflow.notification.infrastructure.http.CorrelationIdFilter;

import io.quarkus.security.AuthenticationFailedException;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFailedExceptionMapper implements ExceptionMapper<AuthenticationFailedException> {
    private static final Logger LOG = Logger.getLogger(AuthenticationFailedExceptionMapper.class);

    @Inject
    ApiErrorFactory errorFactory;

    @Inject
    HttpHeaders httpHeaders;

    @Override
    public Response toResponse(AuthenticationFailedException exception) {
        LOG.warnf(
                "Service JWT authentication correlationId=%s subject=unavailable issuer=unavailable result=failed permission=%s",
                httpHeaders.getHeaderString(CorrelationIdFilter.HEADER_NAME),
                ServiceTokenClaimsFilter.REQUIRED_PERMISSION);
        return unauthorized();
    }

    private Response unauthorized() {
        return Response.status(Response.Status.UNAUTHORIZED)
                .type(APPLICATION_JSON_TYPE)
                .entity(errorFactory.create(
                        Response.Status.UNAUTHORIZED.getStatusCode(),
                        "AUTHENTICATION_REQUIRED",
                        "Service authentication is required"))
                .build();
    }
}

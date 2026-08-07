package com.flakomencia.agendaflow.notification.api.error;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import com.flakomencia.agendaflow.notification.infrastructure.security.ServiceTokenClaimsFilter;
import com.flakomencia.agendaflow.notification.infrastructure.http.CorrelationIdFilter;

import io.quarkus.security.ForbiddenException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {
    private static final Logger LOG = Logger.getLogger(ForbiddenExceptionMapper.class);

    @Inject
    ApiErrorFactory errorFactory;

    @Inject
    JsonWebToken token;

    @Inject
    HttpHeaders httpHeaders;

    @Override
    public Response toResponse(ForbiddenException exception) {
        LOG.warnf(
                "Service JWT authorization correlationId=%s subject=%s issuer=%s result=access_denied permission=%s",
                httpHeaders.getHeaderString(CorrelationIdFilter.HEADER_NAME),
                token.getSubject(), token.getIssuer(), ServiceTokenClaimsFilter.REQUIRED_PERMISSION);
        return Response.status(Response.Status.FORBIDDEN)
                .type(APPLICATION_JSON_TYPE)
                .entity(errorFactory.create(
                        Response.Status.FORBIDDEN.getStatusCode(),
                        "ACCESS_DENIED",
                        "Access to this resource is denied"))
                .build();
    }
}

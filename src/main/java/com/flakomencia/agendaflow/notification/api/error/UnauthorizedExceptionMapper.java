package com.flakomencia.agendaflow.notification.api.error;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import org.jboss.logging.Logger;

import com.flakomencia.agendaflow.notification.infrastructure.http.CorrelationIdFilter;
import com.flakomencia.agendaflow.notification.infrastructure.security.ServiceTokenClaimsFilter;

import io.quarkus.security.UnauthorizedException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {
    private static final Logger LOG = Logger.getLogger(UnauthorizedExceptionMapper.class);

    @Inject
    ApiErrorFactory errorFactory;

    @Inject
    HttpHeaders httpHeaders;

    @Inject
    UriInfo uriInfo;

    @Override
    public Response toResponse(UnauthorizedException exception) {
        LOG.warnf(
                "Service JWT authentication correlationId=%s subject=unavailable issuer=unavailable result=missing permission=%s",
                httpHeaders.getHeaderString(CorrelationIdFilter.HEADER_NAME),
                ServiceTokenClaimsFilter.permissionForPath(uriInfo.getPath()));
        return Response.status(Response.Status.UNAUTHORIZED)
                .type(APPLICATION_JSON_TYPE)
                .entity(errorFactory.create(
                        Response.Status.UNAUTHORIZED.getStatusCode(),
                        "AUTHENTICATION_REQUIRED",
                        "Service authentication is required"))
                .build();
    }
}

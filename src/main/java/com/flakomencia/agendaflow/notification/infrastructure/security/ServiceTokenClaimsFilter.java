package com.flakomencia.agendaflow.notification.infrastructure.security;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.io.IOException;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import com.flakomencia.agendaflow.notification.api.error.ApiErrorFactory;
import com.flakomencia.agendaflow.notification.infrastructure.http.CorrelationIdFilter;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@ServiceTokenRequired
@Priority(Priorities.AUTHORIZATION - 100)
public class ServiceTokenClaimsFilter implements ContainerRequestFilter {
    public static final String VALIDATE_PERMISSION = "notification:validate";
    public static final String SUBMIT_PERMISSION = "notification:submit";
    public static final String REQUIRED_PERMISSION = VALIDATE_PERMISSION;
    private static final String EXPECTED_SUBJECT = "agendaflow-api";
    private static final Logger LOG = Logger.getLogger(ServiceTokenClaimsFilter.class);

    @Inject
    JsonWebToken token;

    @Inject
    ApiErrorFactory errorFactory;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requiredPermission = permissionForPath(requestContext.getUriInfo().getPath());
        String tokenUse = token.getClaim("token_use");
        if (!"service".equals(tokenUse)) {
            deny(requestContext, "INVALID_TOKEN_USE", "Only service tokens can access this resource", "invalid_token_use");
            return;
        }
        if (!EXPECTED_SUBJECT.equals(token.getSubject())) {
            deny(requestContext, "ACCESS_DENIED", "Access to this resource is denied", "invalid_subject");
            return;
        }

        LOG.infof(
                "Service JWT authorization correlationId=%s subject=%s issuer=%s result=claims_valid permission=%s",
                requestContext.getHeaderString(CorrelationIdFilter.HEADER_NAME),
                token.getSubject(), token.getIssuer(), requiredPermission);
    }

    private void deny(
            ContainerRequestContext requestContext,
            String code,
            String message,
            String result) {
        LOG.warnf(
                "Service JWT authorization correlationId=%s subject=%s issuer=%s result=%s permission=%s",
                requestContext.getHeaderString(CorrelationIdFilter.HEADER_NAME),
                token.getSubject(), token.getIssuer(), result,
                permissionForPath(requestContext.getUriInfo().getPath()));
        requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                .type(APPLICATION_JSON_TYPE)
                .entity(errorFactory.create(Response.Status.FORBIDDEN.getStatusCode(), code, message))
                .build());
    }

    public static String permissionForPath(String path) {
        return path != null && path.endsWith("/validate") ? VALIDATE_PERMISSION : SUBMIT_PERMISSION;
    }
}

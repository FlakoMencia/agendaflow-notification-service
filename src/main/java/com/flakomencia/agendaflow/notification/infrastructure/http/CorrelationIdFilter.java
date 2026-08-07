package com.flakomencia.agendaflow.notification.infrastructure.http;

import java.io.IOException;
import java.util.UUID;

import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;

@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 100)
public class CorrelationIdFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String HEADER_NAME = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";
    private static final Logger LOG = Logger.getLogger(CorrelationIdFilter.class);

    @Inject
    CorrelationIdContext correlationIdContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String receivedCorrelationId = requestContext.getHeaderString(HEADER_NAME);
        String correlationId = receivedCorrelationId == null || receivedCorrelationId.isBlank()
                ? UUID.randomUUID().toString()
                : receivedCorrelationId;

        correlationIdContext.set(correlationId);
        MDC.put(MDC_KEY, correlationId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        String correlationId = correlationIdContext.get();
        if (correlationId == null) {
            correlationId = requestContext.getHeaderString(HEADER_NAME);
        }
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        responseContext.getHeaders().putSingle(HEADER_NAME, correlationId);
        LOG.infof("HTTP %s %s completed with status %d",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri().getRawPath(),
                responseContext.getStatus());

        MDC.remove(MDC_KEY);
        correlationIdContext.clear();
    }
}

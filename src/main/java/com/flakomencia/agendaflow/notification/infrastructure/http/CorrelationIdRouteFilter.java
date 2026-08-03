package com.flakomencia.agendaflow.notification.infrastructure.http;

import java.util.UUID;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class CorrelationIdRouteFilter {

    void register(@Observes Router router) {
        router.route().order(-10_000).handler(this::applyCorrelationId);
    }

    private void applyCorrelationId(RoutingContext routingContext) {
        String receivedCorrelationId = routingContext.request().getHeader(CorrelationIdFilter.HEADER_NAME);
        String correlationId = receivedCorrelationId == null || receivedCorrelationId.isBlank()
                ? UUID.randomUUID().toString()
                : receivedCorrelationId;

        routingContext.request().headers().set(CorrelationIdFilter.HEADER_NAME, correlationId);
        routingContext.response().headers().set(CorrelationIdFilter.HEADER_NAME, correlationId);
        routingContext.next();
    }
}

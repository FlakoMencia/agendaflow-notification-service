package com.flakomencia.agendaflow.notification.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flakomencia.agendaflow.notification.application.port.NotificationDeliveryPort;
import com.flakomencia.agendaflow.notification.config.InboxWorkerConfig;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class NotificationInboxWorker {
    private static final Logger LOG = Logger.getLogger(NotificationInboxWorker.class);
    @Inject InboxClaimService claims;
    @Inject DeliveryStateService states;
    @Inject AppointmentNotificationPreparationService preparation;
    @Inject NotificationDeliveryPort delivery;
    @Inject InboxWorkerConfig config;
    @Inject ObjectMapper objectMapper;

    @Scheduled(every = "${notification.inbox.poll-interval:5s}")
    void scheduledProcess() {
        if (!config.enabled()) return;
        process();
    }

    public void process() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        int recovered = claims.recoverStale(now.minus(config.processingTimeout()), now);
        if (recovered > 0) LOG.infof("Recovered stale notification inbox claims count=%d", recovered);
        for (var inbox : claims.claim(config.batchSize(), now)) {
            Long deliveryId = null;
            try {
                AppointmentNotificationMessage message = objectMapper.readValue(inbox.getPayload(),
                        AppointmentNotificationMessage.class);
                var rendered = preparation.prepare(message.toDomainEvent());
                deliveryId = states.start(inbox, rendered, OffsetDateTime.now(ZoneOffset.UTC));
                delivery.deliver(rendered);
                states.success(inbox.getId(), deliveryId, OffsetDateTime.now(ZoneOffset.UTC));
                LOG.infof("Notification dispatched eventId=%d inboxId=%d appointmentId=%d attempt=%d result=DISPATCHED",
                        inbox.getEventId(), inbox.getId(), inbox.getAppointmentId(), inbox.getAttemptCount());
            } catch (Exception failure) {
                states.failure(inbox.getId(), deliveryId, failure, OffsetDateTime.now(ZoneOffset.UTC));
                LOG.warnf("Notification processing failed eventId=%d inboxId=%d appointmentId=%d attempt=%d result=FAILED error=%s",
                        inbox.getEventId(), inbox.getId(), inbox.getAppointmentId(), inbox.getAttemptCount(),
                        failure.getClass().getSimpleName());
            }
        }
    }
}

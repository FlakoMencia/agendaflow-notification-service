package com.flakomencia.agendaflow.notification.application;

import java.time.Duration;
import java.time.OffsetDateTime;

import com.flakomencia.agendaflow.notification.config.InboxWorkerConfig;
import com.flakomencia.agendaflow.notification.domain.RenderedNotification;
import com.flakomencia.agendaflow.notification.infrastructure.persistence.NotificationDeliveryEntity;
import com.flakomencia.agendaflow.notification.infrastructure.persistence.NotificationInboxEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DeliveryStateService {
    @Inject EntityManager entityManager;
    @Inject InboxWorkerConfig config;

    @Transactional
    public Long start(NotificationInboxEntity claimed, RenderedNotification notification, OffsetDateTime now) {
        NotificationInboxEntity inbox = entityManager.getReference(NotificationInboxEntity.class, claimed.getId());
        NotificationDeliveryEntity delivery = new NotificationDeliveryEntity(inbox, notification.channel().name(),
                notification.templateCode().value(), claimed.getAttemptCount(), now);
        entityManager.persist(delivery); entityManager.flush(); return delivery.getId();
    }

    @Transactional
    public void success(Long inboxId, Long deliveryId, OffsetDateTime now) {
        entityManager.find(NotificationDeliveryEntity.class, deliveryId).dispatched(now);
        entityManager.find(NotificationInboxEntity.class, inboxId).processed(now);
    }

    @Transactional
    public void failure(Long inboxId, Long deliveryId, Throwable failure, OffsetDateTime now) {
        NotificationInboxEntity inbox = entityManager.find(NotificationInboxEntity.class, inboxId);
        if (deliveryId != null) entityManager.find(NotificationDeliveryEntity.class, deliveryId)
                .failed(now, safeCode(failure), safeMessage(failure));
        Duration delay = config.initialDelay().multipliedBy(1L << Math.min(Math.max(inbox.getAttemptCount() - 1, 0), 12));
        if (delay.compareTo(config.maxDelay()) > 0) delay = config.maxDelay();
        inbox.failed(now, now.plus(delay), safeMessage(failure), config.maxAttempts());
    }

    private String safeCode(Throwable failure) {
        return failure == null ? "DELIVERY_FAILED" : safe(failure.getClass().getSimpleName(), 80);
    }
    private String safeMessage(Throwable failure) {
        String value = failure == null || failure.getMessage() == null ? "Notification dispatch failed" : failure.getMessage();
        return safe(value, 500);
    }
    private String safe(String value, int maximum) {
        String normalized = value.replaceAll("[\\r\\n]+", " ");
        return normalized.length() <= maximum ? normalized : normalized.substring(0, maximum);
    }
}

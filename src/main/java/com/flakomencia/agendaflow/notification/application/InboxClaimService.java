package com.flakomencia.agendaflow.notification.application;

import java.time.OffsetDateTime;
import java.util.List;

import com.flakomencia.agendaflow.notification.infrastructure.persistence.DeliveryStatus;
import com.flakomencia.agendaflow.notification.infrastructure.persistence.NotificationDeliveryEntity;
import com.flakomencia.agendaflow.notification.infrastructure.persistence.NotificationInboxEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class InboxClaimService {
    @Inject EntityManager entityManager;

    @Transactional
    public List<NotificationInboxEntity> claim(int batchSize, OffsetDateTime now) {
        @SuppressWarnings("unchecked")
        List<Number> ids = entityManager.createNativeQuery("""
                SELECT id FROM notification_service.notification_inbox
                 WHERE status = 'PENDING' AND next_attempt_at <= :now
                 ORDER BY next_attempt_at, id
                 FOR UPDATE SKIP LOCKED
                """).setParameter("now", now).setMaxResults(batchSize).getResultList();
        List<NotificationInboxEntity> events = ids.stream().map(Number::longValue)
                .map(id -> entityManager.find(NotificationInboxEntity.class, id)).toList();
        events.forEach(event -> event.claim(now));
        entityManager.flush();
        return events;
    }

    @Transactional
    public int recoverStale(OffsetDateTime staleBefore, OffsetDateTime now) {
        @SuppressWarnings("unchecked")
        List<Number> ids = entityManager.createNativeQuery("""
                SELECT id FROM notification_service.notification_inbox
                 WHERE status = 'PROCESSING' AND processing_started_at < :staleBefore
                 FOR UPDATE SKIP LOCKED
                """).setParameter("staleBefore", staleBefore).getResultList();
        for (Number value : ids) {
            NotificationInboxEntity inbox = entityManager.find(NotificationInboxEntity.class, value.longValue());
            inbox.recover(now);
            entityManager.createQuery("""
                    UPDATE NotificationDeliveryEntity delivery
                       SET delivery.status = :failed, delivery.finishedAt = :now,
                           delivery.errorCode = 'STALE_PROCESSING',
                           delivery.errorMessage = 'Worker claim expired before completion'
                     WHERE delivery.inbox.id = :inboxId AND delivery.status = :processing
                    """).setParameter("failed", DeliveryStatus.FAILED)
                    .setParameter("processing", DeliveryStatus.PROCESSING)
                    .setParameter("now", now).setParameter("inboxId", inbox.getId()).executeUpdate();
        }
        return ids.size();
    }
}

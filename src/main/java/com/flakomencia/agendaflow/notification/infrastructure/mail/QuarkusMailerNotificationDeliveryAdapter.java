package com.flakomencia.agendaflow.notification.infrastructure.mail;

import com.flakomencia.agendaflow.notification.application.port.NotificationDeliveryPort;
import com.flakomencia.agendaflow.notification.domain.RenderedNotification;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class QuarkusMailerNotificationDeliveryAdapter implements NotificationDeliveryPort {
    @Inject Mailer mailer;

    @Override
    public void deliver(RenderedNotification notification) {
        mailer.send(Mail.withText(notification.recipient().value(), notification.subject(), notification.content()));
    }
}

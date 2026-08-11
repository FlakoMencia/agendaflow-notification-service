package com.flakomencia.agendaflow.notification.application;

import com.flakomencia.agendaflow.notification.application.port.AppointmentNotificationIntake;
import com.flakomencia.agendaflow.notification.application.port.NotificationTemplateCatalog;
import com.flakomencia.agendaflow.notification.application.rendering.NotificationTemplateRenderer;
import com.flakomencia.agendaflow.notification.application.template.AppointmentNotificationTemplateMapper;
import com.flakomencia.agendaflow.notification.application.template.TemplateVariablesValidator;
import com.flakomencia.agendaflow.notification.domain.AppointmentNotificationEvent;
import com.flakomencia.agendaflow.notification.domain.AppointmentNotificationTemplateCode;
import com.flakomencia.agendaflow.notification.domain.NotificationChannel;
import com.flakomencia.agendaflow.notification.domain.NotificationRecipient;
import com.flakomencia.agendaflow.notification.domain.NotificationTemplate;
import com.flakomencia.agendaflow.notification.domain.RenderedNotification;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AppointmentNotificationPreparationService implements AppointmentNotificationIntake {

    private final NotificationTemplateCatalog templateCatalog;
    private final AppointmentNotificationTemplateMapper templateMapper;
    private final TemplateVariablesValidator variablesValidator;
    private final NotificationTemplateRenderer renderer;
    private final NotificationRecipientNormalizer recipientNormalizer;

    @Inject
    public AppointmentNotificationPreparationService(
            NotificationTemplateCatalog templateCatalog,
            AppointmentNotificationTemplateMapper templateMapper,
            TemplateVariablesValidator variablesValidator,
            NotificationTemplateRenderer renderer,
            NotificationRecipientNormalizer recipientNormalizer) {
        this.templateCatalog = templateCatalog;
        this.templateMapper = templateMapper;
        this.variablesValidator = variablesValidator;
        this.renderer = renderer;
        this.recipientNormalizer = recipientNormalizer;
    }

    @Override
    public RenderedNotification prepare(AppointmentNotificationEvent event) {
        AppointmentNotificationTemplateCode templateCode = templateMapper.toTemplateCode(event.type());
        NotificationChannel channel = NotificationChannel.EMAIL;
        NotificationTemplate template = templateCatalog.get(templateCode, channel, event.locale());
        variablesValidator.validate(template, event.variables());

        String subject = renderer.render(template.subject(), event.variables());
        String body = renderer.render(template.plainTextBody(), event.variables());
        NotificationRecipient recipient = recipientNormalizer.normalize(event.recipient());

        return new RenderedNotification(
                event.organizationId(),
                event.appointmentId(),
                channel,
                templateCode.toTemplateCode(),
                recipient,
                templateCatalog.resolveLocale(event.locale()),
                null,
                subject,
                body);
    }
}

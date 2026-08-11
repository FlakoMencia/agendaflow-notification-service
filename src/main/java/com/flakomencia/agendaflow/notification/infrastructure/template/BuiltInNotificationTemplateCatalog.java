package com.flakomencia.agendaflow.notification.infrastructure.template;

import static com.flakomencia.agendaflow.notification.domain.AppointmentNotificationTemplateCode.APPOINTMENT_CANCELLED;
import static com.flakomencia.agendaflow.notification.domain.AppointmentNotificationTemplateCode.APPOINTMENT_CONFIRMATION;
import static com.flakomencia.agendaflow.notification.domain.AppointmentNotificationTemplateCode.APPOINTMENT_RESCHEDULED;
import static com.flakomencia.agendaflow.notification.domain.NotificationChannel.EMAIL;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.flakomencia.agendaflow.notification.application.port.NotificationTemplateCatalog;
import com.flakomencia.agendaflow.notification.domain.AppointmentNotificationTemplateCode;
import com.flakomencia.agendaflow.notification.domain.NotificationChannel;
import com.flakomencia.agendaflow.notification.domain.NotificationTemplate;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BuiltInNotificationTemplateCatalog implements NotificationTemplateCatalog {

    public static final String BASE_LOCALE = "en-US";

    private static final Set<String> CONFIRMATION_VARIABLES = Set.of(
            "customerName", "serviceName", "specialistName", "appointmentDateTime", "branchName");
    private static final Set<String> CANCELLED_VARIABLES = Set.of(
            "customerName", "serviceName", "appointmentDateTime", "branchName");

    private final Map<TemplateKey, NotificationTemplate> templates = Map.of(
            new TemplateKey(APPOINTMENT_CONFIRMATION, EMAIL),
            new NotificationTemplate(
                    APPOINTMENT_CONFIRMATION,
                    EMAIL,
                    "Appointment confirmed: {{serviceName}}",
                    "Hello {{customerName}}, your {{serviceName}} appointment with {{specialistName}} "
                            + "at {{branchName}} is scheduled for {{appointmentDateTime}}.",
                    CONFIRMATION_VARIABLES),
            new TemplateKey(APPOINTMENT_RESCHEDULED, EMAIL),
            new NotificationTemplate(
                    APPOINTMENT_RESCHEDULED,
                    EMAIL,
                    "Appointment rescheduled: {{serviceName}}",
                    "Hello {{customerName}}, your {{serviceName}} appointment with {{specialistName}} "
                            + "at {{branchName}} has been rescheduled to {{appointmentDateTime}}.",
                    CONFIRMATION_VARIABLES),
            new TemplateKey(APPOINTMENT_CANCELLED, EMAIL),
            new NotificationTemplate(
                    APPOINTMENT_CANCELLED,
                    EMAIL,
                    "Appointment cancelled: {{serviceName}}",
                    "Hello {{customerName}}, your {{serviceName}} appointment at {{branchName}} scheduled for "
                            + "{{appointmentDateTime}} has been cancelled.",
                    CANCELLED_VARIABLES));

    @Override
    public NotificationTemplate get(
            AppointmentNotificationTemplateCode code,
            NotificationChannel channel,
            String requestedLocale) {
        Objects.requireNonNull(code, "code must not be null");
        resolveLocale(requestedLocale);
        NotificationTemplate template = templates.get(new TemplateKey(code, channel));
        if (template == null) {
            throw new NotificationTemplateNotFoundException(code, channel);
        }
        return template;
    }

    @Override
    public String resolveLocale(String requestedLocale) {
        return BASE_LOCALE;
    }

    private record TemplateKey(
            AppointmentNotificationTemplateCode code,
            NotificationChannel channel) {
    }
}

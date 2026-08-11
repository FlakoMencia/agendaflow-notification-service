package com.flakomencia.agendaflow.notification.application.template;

import static com.flakomencia.agendaflow.notification.domain.AppointmentNotificationTemplateCode.APPOINTMENT_CONFIRMATION;
import static com.flakomencia.agendaflow.notification.domain.NotificationChannel.EMAIL;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.flakomencia.agendaflow.notification.domain.NotificationLimits;
import com.flakomencia.agendaflow.notification.domain.NotificationTemplate;
import com.flakomencia.agendaflow.notification.domain.NotificationVariables;
import com.flakomencia.agendaflow.notification.domain.RequiredTemplateVariableException;
import com.flakomencia.agendaflow.notification.infrastructure.template.BuiltInNotificationTemplateCatalog;

class TemplateVariablesValidatorTest {

    private final NotificationTemplate template = new BuiltInNotificationTemplateCatalog()
            .get(APPOINTMENT_CONFIRMATION, EMAIL, "en-US");
    private final TemplateVariablesValidator validator = new TemplateVariablesValidator();

    @Test
    void acceptsAllRequiredVariables() {
        assertDoesNotThrow(() -> validator.validate(template, requiredVariables()));
    }

    @Test
    void rejectsMissingCustomerName() {
        assertMissing("customerName", without("customerName"));
    }

    @Test
    void rejectsMissingServiceName() {
        assertMissing("serviceName", without("serviceName"));
    }

    @Test
    void rejectsBlankRequiredVariable() {
        Map<String, String> values = new LinkedHashMap<>(requiredVariables().values());
        values.put("branchName", "   ");
        assertMissing("branchName", new NotificationVariables(values));
    }

    @Test
    void allowsExtraVariables() {
        Map<String, String> values = new LinkedHashMap<>(requiredVariables().values());
        values.put("unusedContext", "Ignored safely");
        assertDoesNotThrow(() -> validator.validate(template, new NotificationVariables(values)));
    }

    @Test
    void preservesExistingVariableCountLimit() {
        Map<String, String> values = new LinkedHashMap<>();
        IntStream.rangeClosed(0, NotificationLimits.MAX_VARIABLES)
                .forEach(index -> values.put("variable" + index, "value"));
        assertThrows(IllegalArgumentException.class, () -> new NotificationVariables(values));
    }

    private NotificationVariables requiredVariables() {
        return new NotificationVariables(Map.of(
                "customerName", "Example Customer",
                "serviceName", "Consultation",
                "specialistName", "Example Specialist",
                "appointmentDateTime", "August 10 at 10:00 AM",
                "branchName", "Main Branch"));
    }

    private NotificationVariables without(String key) {
        Map<String, String> values = new LinkedHashMap<>(requiredVariables().values());
        values.remove(key);
        return new NotificationVariables(values);
    }

    private void assertMissing(String variableName, NotificationVariables variables) {
        RequiredTemplateVariableException exception = assertThrows(
                RequiredTemplateVariableException.class,
                () -> validator.validate(template, variables));
        assertEquals(variableName, exception.variableName());
    }
}

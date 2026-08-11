package com.flakomencia.agendaflow.notification.application.template;

import com.flakomencia.agendaflow.notification.domain.NotificationTemplate;
import com.flakomencia.agendaflow.notification.domain.NotificationVariables;
import com.flakomencia.agendaflow.notification.domain.RequiredTemplateVariableException;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TemplateVariablesValidator {

    public void validate(NotificationTemplate template, NotificationVariables variables) {
        template.requiredVariables().forEach(variableName -> {
            String value = variables.get(variableName);
            if (value == null || value.isBlank()) {
                throw new RequiredTemplateVariableException(variableName);
            }
        });
    }
}

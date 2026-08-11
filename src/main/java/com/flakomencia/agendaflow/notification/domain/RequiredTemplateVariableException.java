package com.flakomencia.agendaflow.notification.domain;

public final class RequiredTemplateVariableException extends IllegalArgumentException {

    private final String variableName;

    public RequiredTemplateVariableException(String variableName) {
        super("Required template variable is missing or blank: " + variableName);
        this.variableName = variableName;
    }

    public String variableName() {
        return variableName;
    }
}

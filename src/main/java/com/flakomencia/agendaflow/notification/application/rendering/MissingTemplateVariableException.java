package com.flakomencia.agendaflow.notification.application.rendering;

public final class MissingTemplateVariableException extends TemplateRenderingException {

    private final String variableName;

    public MissingTemplateVariableException(String variableName) {
        super("Missing required template variable: " + variableName);
        this.variableName = variableName;
    }

    public String variableName() {
        return variableName;
    }
}

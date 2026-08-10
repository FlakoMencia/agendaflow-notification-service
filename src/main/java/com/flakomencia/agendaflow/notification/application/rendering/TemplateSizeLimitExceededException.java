package com.flakomencia.agendaflow.notification.application.rendering;

public final class TemplateSizeLimitExceededException extends InvalidTemplateException {

    public TemplateSizeLimitExceededException(int maximumLength) {
        super("Template text must contain at most " + maximumLength + " characters");
    }
}

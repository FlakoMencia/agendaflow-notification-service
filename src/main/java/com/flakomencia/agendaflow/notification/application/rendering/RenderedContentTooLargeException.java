package com.flakomencia.agendaflow.notification.application.rendering;

public final class RenderedContentTooLargeException extends TemplateRenderingException {

    public RenderedContentTooLargeException(int maximumLength) {
        super("Rendered content must contain at most " + maximumLength + " characters");
    }
}

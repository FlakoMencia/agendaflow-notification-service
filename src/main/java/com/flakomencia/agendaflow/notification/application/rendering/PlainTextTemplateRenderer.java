package com.flakomencia.agendaflow.notification.application.rendering;

import java.util.Objects;
import java.util.regex.Pattern;

import com.flakomencia.agendaflow.notification.domain.NotificationLimits;
import com.flakomencia.agendaflow.notification.domain.NotificationVariables;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PlainTextTemplateRenderer implements NotificationTemplateRenderer {

    private static final String OPENING_DELIMITER = "{{";
    private static final String CLOSING_DELIMITER = "}}";
    private static final Pattern VARIABLE_NAME = Pattern.compile(
            "^[A-Za-z][A-Za-z0-9]*(?:[._-][A-Za-z0-9]+)*$");

    @Override
    public String render(String templateText, NotificationVariables variables) {
        Objects.requireNonNull(templateText, "templateText must not be null");
        Objects.requireNonNull(variables, "variables must not be null");
        validateTemplateSize(templateText);

        StringBuilder rendered = new StringBuilder(Math.min(
                templateText.length(),
                NotificationLimits.MAX_RENDERED_CONTENT_LENGTH));
        int cursor = 0;

        while (cursor < templateText.length()) {
            int opening = templateText.indexOf(OPENING_DELIMITER, cursor);
            int unexpectedClosing = templateText.indexOf(CLOSING_DELIMITER, cursor);

            if (opening < 0) {
                if (unexpectedClosing >= 0) {
                    throw invalidSyntax();
                }
                append(rendered, templateText, cursor, templateText.length());
                break;
            }
            if (unexpectedClosing >= 0 && unexpectedClosing < opening) {
                throw invalidSyntax();
            }

            append(rendered, templateText, cursor, opening);
            int closing = templateText.indexOf(CLOSING_DELIMITER, opening + OPENING_DELIMITER.length());
            if (closing < 0) {
                throw invalidSyntax();
            }

            String variableName = templateText.substring(opening + OPENING_DELIMITER.length(), closing);
            if (!VARIABLE_NAME.matcher(variableName).matches()) {
                throw invalidSyntax();
            }
            if (!variables.contains(variableName)) {
                throw new MissingTemplateVariableException(variableName);
            }
            append(rendered, variables.get(variableName));
            cursor = closing + CLOSING_DELIMITER.length();
        }

        return rendered.toString();
    }

    private void validateTemplateSize(String templateText) {
        if (templateText.isEmpty()) {
            throw new InvalidTemplateException("Template text must not be empty");
        }
        if (templateText.length() > NotificationLimits.MAX_TEMPLATE_LENGTH) {
            throw new TemplateSizeLimitExceededException(NotificationLimits.MAX_TEMPLATE_LENGTH);
        }
    }

    private void append(StringBuilder target, String value) {
        ensureResultLimit(target.length(), value.length());
        target.append(value);
    }

    private void append(StringBuilder target, String source, int start, int end) {
        ensureResultLimit(target.length(), end - start);
        target.append(source, start, end);
    }

    private void ensureResultLimit(int currentLength, int additionalLength) {
        if (additionalLength > NotificationLimits.MAX_RENDERED_CONTENT_LENGTH - currentLength) {
            throw new RenderedContentTooLargeException(NotificationLimits.MAX_RENDERED_CONTENT_LENGTH);
        }
    }

    private InvalidTemplateException invalidSyntax() {
        return new InvalidTemplateException(
                "Template placeholders must use the exact syntax {{variableName}}");
    }
}

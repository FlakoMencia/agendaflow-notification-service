package com.flakomencia.agendaflow.notification.application.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.flakomencia.agendaflow.notification.domain.NotificationLimits;
import com.flakomencia.agendaflow.notification.domain.NotificationVariables;

class PlainTextTemplateRendererTest {

    private final PlainTextTemplateRenderer renderer = new PlainTextTemplateRenderer();

    @Test
    void rendersTextWithoutVariables() {
        assertEquals("Synthetic notification text.", render("Synthetic notification text.", Map.of()));
    }

    @Test
    void rendersOneVariable() {
        assertEquals("Hello Laura.", render("Hello {{customerName}}.", Map.of("customerName", "Laura")));
    }

    @Test
    void rendersMultipleVariables() {
        assertEquals(
                "Hello Laura, your appointment is at 10:00 AM.",
                render(
                        "Hello {{customerName}}, your appointment is at {{appointmentTime}}.",
                        Map.of("customerName", "Laura", "appointmentTime", "10:00 AM")));
    }

    @Test
    void rendersRepeatedVariableDeterministically() {
        assertEquals("Laura / Laura", render("{{name}} / {{name}}", Map.of("name", "Laura")));
    }

    @Test
    void failsExplicitlyForMissingVariable() {
        MissingTemplateVariableException exception = assertThrows(
                MissingTemplateVariableException.class,
                () -> render("Hello {{name}}", Map.of()));

        assertEquals("name", exception.variableName());
    }

    @Test
    void allowsAdditionalUnusedVariables() {
        assertEquals(
                "Hello Laura",
                render("Hello {{name}}", Map.of("name", "Laura", "unused", "allowed")));
    }

    @Test
    void rejectsEmptyTemplate() {
        assertThrows(InvalidTemplateException.class, () -> render("", Map.of()));
    }

    @Test
    void rejectsTemplateAboveLimit() {
        assertThrows(
                TemplateSizeLimitExceededException.class,
                () -> render("x".repeat(NotificationLimits.MAX_TEMPLATE_LENGTH + 1), Map.of()));
    }

    @Test
    void rejectsRenderedContentAboveLimit() {
        String template = "{{value}}".repeat(50);
        String value = "x".repeat(NotificationLimits.MAX_VARIABLE_VALUE_LENGTH);

        assertThrows(
                RenderedContentTooLargeException.class,
                () -> render(template, Map.of("value", value)));
    }

    @Test
    void preservesSpecialCharactersAndDoesNotRecursivelyRenderValues() {
        String value = "$5 \\ path {{notEvaluated}} ñ";

        assertEquals(value, render("{{value}}", Map.of("value", value)));
    }

    @Test
    void treatsHtmlAsPlainText() {
        String html = "<strong>Hello {{name}}</strong>";

        assertEquals("<strong>Hello Laura</strong>", render(html, Map.of("name", "Laura")));
    }

    @Test
    void doesNotExecuteExpressions() {
        assertEquals("Result: ${7 * 7}", render("Result: ${7 * 7}", Map.of()));
        assertThrows(InvalidTemplateException.class, () -> render("{{7 * 7}}", Map.of()));
    }

    @Test
    void rejectsMalformedDelimiters() {
        assertThrows(InvalidTemplateException.class, () -> render("Hello {{name", Map.of("name", "Laura")));
        assertThrows(InvalidTemplateException.class, () -> render("Hello name}}", Map.of("name", "Laura")));
    }

    private String render(String template, Map<String, String> values) {
        return renderer.render(template, new NotificationVariables(values));
    }
}

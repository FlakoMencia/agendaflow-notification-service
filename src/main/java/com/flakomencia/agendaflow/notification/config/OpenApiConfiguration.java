package com.flakomencia.agendaflow.notification.config;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
@OpenAPIDefinition(
        info = @Info(
                title = "AgendaFlow Notification Service API",
                version = "0.0.1",
                description = "AgendaFlow notification service under construction.",
                contact = @Contact(name = "AgendaFlow Technical Team")),
        tags = {
                @Tag(name = "System", description = "Technical service information"),
                @Tag(name = "Health", description = "Standard SmallRye Health endpoints")
        })
public class OpenApiConfiguration extends Application {
}

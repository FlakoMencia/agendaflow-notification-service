package com.flakomencia.agendaflow.notification.config;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

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
                @Tag(
                        name = "Notification Contract",
                        description = "Validation-only contract; it does not send or store notifications"),
                @Tag(name = "Health", description = "Standard SmallRye Health endpoints")
        })
@SecurityScheme(
        securitySchemeName = "serviceBearer",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Short-lived AgendaFlow service token with token_use=service. Endpoints require either notification:validate or notification:submit; user tokens are rejected.")
public class OpenApiConfiguration extends Application {
}

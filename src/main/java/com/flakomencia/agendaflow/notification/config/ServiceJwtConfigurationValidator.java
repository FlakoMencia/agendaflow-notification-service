package com.flakomencia.agendaflow.notification.config;

import java.nio.charset.StandardCharsets;

import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class ServiceJwtConfigurationValidator {

    @Inject
    ServiceJwtConfig config;

    void validate(@Observes StartupEvent event) {
        if (config.secret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("SERVICE_JWT_SECRET must contain at least 32 UTF-8 bytes for HS256");
        }
        if (LaunchMode.current() != LaunchMode.TEST && isPlaceholder(config.secret())) {
            throw new IllegalStateException("SERVICE_JWT_SECRET must be supplied securely outside the test profile");
        }
    }

    private boolean isPlaceholder(String value) {
        String normalized = value.toLowerCase();
        return normalized.contains("change-me") || normalized.contains("test-only");
    }
}

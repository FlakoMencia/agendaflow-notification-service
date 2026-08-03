package com.flakomencia.agendaflow.notification.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class NotificationProviderConfigTest {

    @Inject
    NotificationProviderConfig configuration;

    @Test
    void exposesSafeDevelopmentDefaults() {
        assertEquals("not-configured", configuration.provider());
        assertEquals("no-reply@example.com", configuration.emailSender());
        assertEquals(URI.create("http://localhost:8080"), configuration.springApiBaseUrl());
    }
}

package com.flakomencia.agendaflow.notification.config;

import java.net.URI;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "notification")
public interface NotificationProviderConfig {

    String provider();

    String emailSender();

    URI springApiBaseUrl();
}

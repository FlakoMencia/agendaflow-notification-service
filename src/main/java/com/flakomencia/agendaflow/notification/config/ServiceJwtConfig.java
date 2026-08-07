package com.flakomencia.agendaflow.notification.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "service.jwt")
public interface ServiceJwtConfig {
    String secret();
    String issuer();
    String audience();
}

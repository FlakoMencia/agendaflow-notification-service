package com.flakomencia.agendaflow.notification.config;

import java.time.Duration;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "notification.inbox")
public interface InboxWorkerConfig {
    @WithDefault("true") boolean enabled();
    @WithDefault("5s") Duration pollInterval();
    @WithDefault("20") int batchSize();
    @WithDefault("5") int maxAttempts();
    @WithDefault("10s") Duration initialDelay();
    @WithDefault("1h") Duration maxDelay();
    @WithDefault("5m") Duration processingTimeout();
}

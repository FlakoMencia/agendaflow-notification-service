package com.flakomencia.agendaflow.notification.health;

import java.sql.Connection;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@Readiness
@ApplicationScoped
public class DatabaseReadinessCheck implements HealthCheck {
    @Inject AgroalDataSource dataSource;
    @Override public HealthCheckResponse call() {
        try (Connection connection = dataSource.getConnection(); var statement = connection.createStatement();
                var result = statement.executeQuery("SELECT 1")) {
            return result.next() ? HealthCheckResponse.up("notification-database")
                    : HealthCheckResponse.down("notification-database");
        } catch (Exception exception) {
            return HealthCheckResponse.down("notification-database");
        }
    }
}

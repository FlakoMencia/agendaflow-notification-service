package com.flakomencia.agendaflow.notification.api;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

import java.sql.Connection;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.flakomencia.agendaflow.notification.application.InboxClaimService;
import com.flakomencia.agendaflow.notification.application.NotificationInboxWorker;
import com.flakomencia.agendaflow.notification.application.port.NotificationDeliveryPort;

import io.agroal.api.AgroalDataSource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;

@QuarkusTest
class NotificationInboxWorkerFailureTest {
    private static final String PATH = "/api/v1/notification-requests";
    @Inject AgroalDataSource dataSource;
    @Inject NotificationInboxWorker worker;
    @Inject InboxClaimService claims;
    @InjectMock NotificationDeliveryPort deliveryPort;

    @BeforeEach void clean() throws Exception {
        reset(deliveryPort);
        try (Connection connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM notification_service.notification_deliveries");
            statement.executeUpdate("DELETE FROM notification_service.notification_inbox");
        }
    }

    @Test void retriesWithDeliveryAuditAndExhaustsAtConfiguredMaximum() throws Exception {
        accept(901L);
        doThrow(new IllegalStateException("smtp unavailable")).when(deliveryPort).deliver(any());
        for (int attempt = 1; attempt <= 5; attempt++) {
            execute("UPDATE notification_service.notification_inbox SET next_attempt_at=CURRENT_TIMESTAMP - INTERVAL '1 second' "
                    + "WHERE event_id=901");
            worker.process();
            assertEquals(attempt, scalarLong("SELECT attempt_count FROM notification_service.notification_inbox WHERE event_id=901"));
        }
        assertEquals("EXHAUSTED", scalarString("SELECT status FROM notification_service.notification_inbox WHERE event_id=901"));
        assertEquals(5L, scalarLong("SELECT count(*) FROM notification_service.notification_deliveries delivery "
                + "JOIN notification_service.notification_inbox inbox ON inbox.id=delivery.inbox_id "
                + "WHERE inbox.event_id=901 AND delivery.status='FAILED'"));
        accept(901L);
        assertEquals(1L, scalarLong("SELECT count(*) FROM notification_service.notification_inbox WHERE event_id=901"));
        assertEquals(5L, scalarLong("SELECT count(*) FROM notification_service.notification_deliveries"));
    }

    @Test void concurrentClaimsDoNotOverlapAndStaleProcessingRecovers() throws Exception {
        accept(902L);
        OffsetDateTime now = OffsetDateTime.now().plusSeconds(1);
        var executor = Executors.newFixedThreadPool(2); var gate = new CountDownLatch(1);
        var first = executor.submit(() -> { gate.await(); return claims.claim(1, now).size(); });
        var second = executor.submit(() -> { gate.await(); return claims.claim(1, now).size(); });
        gate.countDown();
        assertEquals(1, first.get(15, TimeUnit.SECONDS) + second.get(15, TimeUnit.SECONDS));
        executor.shutdown(); assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals("PROCESSING", scalarString("SELECT status FROM notification_service.notification_inbox WHERE event_id=902"));
        execute("UPDATE notification_service.notification_inbox SET processing_started_at=CURRENT_TIMESTAMP - INTERVAL '10 minutes' "
                + "WHERE event_id=902");
        assertEquals(1, claims.recoverStale(OffsetDateTime.now().minusMinutes(5), OffsetDateTime.now()));
        assertEquals("PENDING", scalarString("SELECT status FROM notification_service.notification_inbox WHERE event_id=902"));
    }

    private void accept(long eventId) {
        given().auth().oauth2(ServiceJwtTestTokens.submit()).contentType(ContentType.JSON).body(request(eventId))
                .post(PATH).then().statusCode(202);
    }
    private Map<String, Object> request(long eventId) {
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("customerName", "Grace Hopper"); variables.put("serviceName", "Consultation");
        variables.put("specialistName", "Taylor"); variables.put("appointmentDateTime", "2026-08-17T09:00:00-06:00");
        variables.put("branchName", "Central");
        Map<String, Object> request = new LinkedHashMap<>(); request.put("eventId", eventId);
        request.put("organizationId", 10); request.put("appointmentId", 20); request.put("type", "CREATED");
        request.put("recipient", "failure@example.com"); request.put("locale", "en-US");
        request.put("occurredAt", "2026-08-10T12:00:00Z"); request.put("variables", variables); return request;
    }
    private void execute(String sql) {
        try (Connection connection=dataSource.getConnection();var statement=connection.createStatement()) { statement.executeUpdate(sql); }
        catch(Exception exception){throw new IllegalStateException(exception);}
    }
    private long scalarLong(String sql) {
        try(Connection connection=dataSource.getConnection();var statement=connection.createStatement();var result=statement.executeQuery(sql))
        {result.next();return result.getLong(1);}catch(Exception exception){throw new IllegalStateException(exception);}
    }
    private String scalarString(String sql) {
        try(Connection connection=dataSource.getConnection();var statement=connection.createStatement();var result=statement.executeQuery(sql))
        {result.next();return result.getString(1);}catch(Exception exception){throw new IllegalStateException(exception);}
    }
}

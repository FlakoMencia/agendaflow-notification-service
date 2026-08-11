package com.flakomencia.agendaflow.notification.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.flakomencia.agendaflow.notification.application.NotificationInboxWorker;
import com.flakomencia.agendaflow.notification.infrastructure.http.CorrelationIdFilter;

import io.agroal.api.AgroalDataSource;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;

@QuarkusTest
class DurableNotificationIntakeTest {
    private static final String PATH = "/api/v1/notification-requests";
    @Inject AgroalDataSource dataSource;
    @Inject NotificationInboxWorker worker;
    @Inject MockMailbox mailbox;

    @BeforeEach void clean() throws Exception {
        try (Connection connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM notification_service.notification_deliveries");
            statement.executeUpdate("DELETE FROM notification_service.notification_inbox");
        }
        mailbox.clear();
    }

    @Test void migrationCreatesOwnedTablesAndUniqueEventConstraint() throws Exception {
        given().get("/q/health/ready").then().statusCode(200).body("status", is("UP"));
        assertEquals(2L, scalarLong("SELECT count(*) FROM information_schema.tables WHERE table_schema='notification_service' "
                + "AND table_name IN ('notification_inbox','notification_deliveries')"));
        assertEquals(1L, scalarLong("SELECT count(*) FROM pg_constraint WHERE conname='uq_notification_inbox_event_id'"));
        assertEquals(1L, scalarLong("SELECT count(*) FROM notification_service.flyway_schema_history "
                + "WHERE version='1' AND success=TRUE"));
    }

    @Test void acceptsOnlyAfterPersistenceAndHandlesDuplicateIdempotently() {
        given().auth().oauth2(ServiceJwtTestTokens.submit()).contentType(ContentType.JSON)
                .header(CorrelationIdFilter.HEADER_NAME, "spring-outbox-123").body(request(123L, "CREATED", "one@example.com"))
                .post(PATH).then().statusCode(202).body("eventId", is(123)).body("status", is("ACCEPTED"))
                .body("duplicate", is(false));
        assertEquals(1L, scalarLong("SELECT count(*) FROM notification_service.notification_inbox WHERE event_id=123"));
        assertEquals("spring-outbox-123", scalarString("SELECT correlation_id FROM notification_service.notification_inbox WHERE event_id=123"));

        given().auth().oauth2(ServiceJwtTestTokens.submit()).contentType(ContentType.JSON)
                .body(request(123L, "CREATED", "one@example.com")).post(PATH).then().statusCode(202)
                .body("duplicate", is(true));
        assertEquals(1L, scalarLong("SELECT count(*) FROM notification_service.notification_inbox WHERE event_id=123"));
        assertEquals(0L, scalarLong("SELECT count(*) FROM notification_service.notification_deliveries"));
    }

    @Test void rejectsMalformedMissingWrongPermissionAndUserTokens() {
        given().auth().oauth2(ServiceJwtTestTokens.submit()).contentType(ContentType.JSON)
                .body(Map.of("eventId", 44)).post(PATH).then().statusCode(400);
        given().auth().oauth2(ServiceJwtTestTokens.submit()).contentType(ContentType.JSON)
                .body(new LinkedHashMap<>(Map.of("eventId", 44, "unknown", true))).post(PATH).then().statusCode(400);
        given().contentType(ContentType.JSON).body(request(44L, "CREATED", "x@example.com"))
                .post(PATH).then().statusCode(401);
        given().auth().oauth2(ServiceJwtTestTokens.valid()).contentType(ContentType.JSON)
                .body(request(44L, "CREATED", "x@example.com")).post(PATH).then().statusCode(403);
        given().auth().oauth2(ServiceJwtTestTokens.submit()).contentType(ContentType.JSON)
                .body(request(45L, "CREATED", "x@example.com")).post(PATH + "/validate").then().statusCode(403);
        String userToken = ServiceJwtTestTokens.token(ServiceJwtTestTokens.testSecret(), ServiceJwtTestTokens.ISSUER,
                ServiceJwtTestTokens.AUDIENCE, ServiceJwtTestTokens.SUBJECT, Set.of(ServiceJwtTestTokens.SUBMIT_GROUP),
                "user", Instant.now().plusSeconds(300));
        given().auth().oauth2(userToken).contentType(ContentType.JSON)
                .body(request(44L, "CREATED", "x@example.com")).post(PATH).then().statusCode(403);
        assertEquals(0L, scalarLong("SELECT count(*) FROM notification_service.notification_inbox"));
    }

    @Test void concurrentDuplicateIntakeCreatesExactlyOneInbox() throws Exception {
        int callers = 8;
        var executor = Executors.newFixedThreadPool(callers);
        var gate = new CountDownLatch(1);
        List<Callable<Integer>> calls = new ArrayList<>();
        for (int index = 0; index < callers; index++) calls.add(() -> {
            assertTrue(gate.await(10, TimeUnit.SECONDS));
            return given().auth().oauth2(ServiceJwtTestTokens.submit()).contentType(ContentType.JSON)
                    .body(request(700L, "RESCHEDULED", "race@example.com")).post(PATH).statusCode();
        });
        var futures = calls.stream().map(executor::submit).toList(); gate.countDown();
        for (var future : futures) assertEquals(202, future.get(20, TimeUnit.SECONDS));
        executor.shutdown(); assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(1L, scalarLong("SELECT count(*) FROM notification_service.notification_inbox WHERE event_id=700"));
        assertEquals(0L, scalarLong("SELECT count(*) FROM notification_service.notification_deliveries"));
    }

    @Test void workerPreparesDispatchesAndAuditsAllAppointmentTemplatesWithMockMailer() {
        given().auth().oauth2(ServiceJwtTestTokens.submit()).contentType(ContentType.JSON)
                .body(request(801L, "CREATED", "created@example.com")).post(PATH).then().statusCode(202);
        given().auth().oauth2(ServiceJwtTestTokens.submit()).contentType(ContentType.JSON)
                .body(request(802L, "RESCHEDULED", "rescheduled@example.com")).post(PATH).then().statusCode(202);
        given().auth().oauth2(ServiceJwtTestTokens.submit()).contentType(ContentType.JSON)
                .body(request(803L, "CANCELLED", "cancelled@example.com")).post(PATH).then().statusCode(202);

        worker.process();
        assertEquals(3L, scalarLong("SELECT count(*) FROM notification_service.notification_inbox WHERE status='PROCESSED'"));
        assertEquals(3L, scalarLong("SELECT count(*) FROM notification_service.notification_deliveries WHERE status='DISPATCHED'"));
        assertEquals(1, mailbox.getMessagesSentTo("created@example.com").size());
        assertEquals("Appointment confirmed: Consultation", mailbox.getMessagesSentTo("created@example.com").getFirst().getSubject());
        assertEquals("Appointment rescheduled: Consultation", mailbox.getMessagesSentTo("rescheduled@example.com").getFirst().getSubject());
        assertEquals("Appointment cancelled: Consultation", mailbox.getMessagesSentTo("cancelled@example.com").getFirst().getSubject());
        assertTrue(mailbox.getMessagesSentTo("created@example.com").getFirst().getText().contains("Grace Hopper"));
    }

    private Map<String, Object> request(long eventId, String type, String recipient) {
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("customerName", "Grace Hopper"); variables.put("serviceName", "Consultation");
        variables.put("specialistName", "Taylor"); variables.put("appointmentDateTime", "2026-08-17T09:00:00-06:00");
        variables.put("branchName", "Central");
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("eventId", eventId); request.put("organizationId", 10); request.put("appointmentId", 20);
        request.put("type", type); request.put("recipient", recipient); request.put("locale", "en-US");
        request.put("occurredAt", "2026-08-10T12:00:00Z"); request.put("variables", variables); return request;
    }

    private long scalarLong(String sql) {
        try (Connection connection = dataSource.getConnection(); var statement = connection.createStatement();
                var result = statement.executeQuery(sql)) { result.next(); return result.getLong(1); }
        catch (Exception exception) { throw new IllegalStateException(exception); }
    }
    private String scalarString(String sql) {
        try (Connection connection = dataSource.getConnection(); var statement = connection.createStatement();
                var result = statement.executeQuery(sql)) { result.next(); return result.getString(1); }
        catch (Exception exception) { throw new IllegalStateException(exception); }
    }
}

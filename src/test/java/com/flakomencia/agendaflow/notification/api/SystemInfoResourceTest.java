package com.flakomencia.agendaflow.notification.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;

import com.flakomencia.agendaflow.notification.infrastructure.http.CorrelationIdFilter;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SystemInfoResourceTest {

    @Test
    void returnsTechnicalFoundationInformation() {
        given()
                .when().get("/api/v1/system/info")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .header(CorrelationIdFilter.HEADER_NAME, not(blankOrNullString()))
                .body("service", is("agendaflow-notification-service"))
                .body("status", is("UP"))
                .body("phase", is("technical-foundation"))
                .body("version", is("0.0.1"));
    }

    @Test
    void preservesReceivedCorrelationId() {
        String correlationId = "agenda-flow-test-correlation";

        given()
                .header(CorrelationIdFilter.HEADER_NAME, correlationId)
                .when().get("/api/v1/system/info")
                .then()
                .statusCode(200)
                .header(CorrelationIdFilter.HEADER_NAME, equalTo(correlationId));
    }

    @Test
    void generatesCorrelationIdWhenHeaderIsMissing() {
        given()
                .when().get("/api/v1/system/info")
                .then()
                .statusCode(200)
                .header(CorrelationIdFilter.HEADER_NAME,
                        matchesPattern("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"));
    }

    @Test
    void returnsUniformValidationError() {
        String correlationId = "validation-correlation";

        given()
                .header(CorrelationIdFilter.HEADER_NAME, correlationId)
                .when().get("/test/validation")
                .then()
                .statusCode(400)
                .contentType("application/json")
                .header(CorrelationIdFilter.HEADER_NAME, equalTo(correlationId))
                .body("timestamp", not(blankOrNullString()))
                .body("status", is(400))
                .body("code", is("VALIDATION_ERROR"))
                .body("message", is("Request validation failed"))
                .body("path", is("/test/validation"))
                .body("correlationId", is(correlationId));
    }

    @Test
    void returnsSafeUniformNotFoundError() {
        String correlationId = "not-found-correlation";

        given()
                .header(CorrelationIdFilter.HEADER_NAME, correlationId)
                .when().get("/api/v1/does-not-exist")
                .then()
                .statusCode(404)
                .contentType("application/json")
                .header(CorrelationIdFilter.HEADER_NAME, equalTo(correlationId))
                .body("status", is(404))
                .body("code", is("RESOURCE_NOT_FOUND"))
                .body("message", is("Resource not found"))
                .body("path", is("/api/v1/does-not-exist"))
                .body("correlationId", is(correlationId));
    }

    @Test
    void returnsSafeUniformUnexpectedError() {
        String correlationId = "unexpected-correlation";

        given()
                .header(CorrelationIdFilter.HEADER_NAME, correlationId)
                .when().get("/test/failure")
                .then()
                .statusCode(500)
                .contentType("application/json")
                .header(CorrelationIdFilter.HEADER_NAME, equalTo(correlationId))
                .body("status", is(500))
                .body("code", is("INTERNAL_ERROR"))
                .body("message", is("Unexpected error"))
                .body("path", is("/test/failure"))
                .body("correlationId", is(correlationId))
                .body(not(containsString("IllegalStateException")))
                .body(not(containsString("Internal implementation detail")));
    }

    @Test
    void exposesConsolidatedOpenApiMetadata() {
        given()
                .queryParam("format", "json")
                .when().get("/q/openapi")
                .then()
                .statusCode(200)
                .header(CorrelationIdFilter.HEADER_NAME, not(blankOrNullString()))
                .body("info.title", is("AgendaFlow Notification Service API"))
                .body("info.version", is("0.0.1"))
                .body("info.description", is("AgendaFlow notification service under construction."))
                .body("info.contact.name", is("AgendaFlow Technical Team"))
                .body("tags.name", hasItems("System", "Health"));
    }

    @Test
    void reportsReadyHealth() {
        given()
                .when().get("/q/health/ready")
                .then()
                .statusCode(200)
                .header(CorrelationIdFilter.HEADER_NAME, not(blankOrNullString()))
                .body("status", is("UP"));
    }
}

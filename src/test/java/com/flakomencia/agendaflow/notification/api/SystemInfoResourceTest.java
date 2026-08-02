package com.flakomencia.agendaflow.notification.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SystemInfoResourceTest {

    @Test
    void returnsBootstrapInformation() {
        given()
                .when().get("/api/v1/system/info")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("service", is("agendaflow-notification-service"))
                .body("status", is("UP"))
                .body("phase", is("bootstrap"));
    }

    @Test
    void reportsReadyHealth() {
        given()
                .when().get("/q/health/ready")
                .then()
                .statusCode(200)
                .body("status", is("UP"));
    }
}

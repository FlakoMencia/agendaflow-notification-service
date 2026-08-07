package com.flakomencia.agendaflow.notification.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.flakomencia.agendaflow.notification.infrastructure.http.CorrelationIdFilter;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@QuarkusTest
class ServiceAuthenticationTest {
    private static final String PATH = "/api/v1/notification-requests/validate";

    @Test
    void acceptsValidShortLivedServiceTokenWithoutChangingFunctionalContract() {
        authorized(ServiceJwtTestTokens.valid())
                .body(validRequest())
                .when().post(PATH)
                .then()
                .statusCode(200)
                .body("valid", is(true))
                .body("channel", is("EMAIL"))
                .body("templateCode", is("APPOINTMENT_CONFIRMATION"))
                .body("$", not(hasKey("deliveryId")));
    }

    @Test
    void rejectsMissingTokenWithUniformCorrelatedError() {
        String correlationId = "missing-token-correlation";

        given()
                .contentType(ContentType.JSON)
                .header(CorrelationIdFilter.HEADER_NAME, correlationId)
                .body(validRequest())
                .when().post(PATH)
                .then()
                .statusCode(401)
                .contentType(ContentType.JSON)
                .header(CorrelationIdFilter.HEADER_NAME, equalTo(correlationId))
                .body("code", is("AUTHENTICATION_REQUIRED"))
                .body("correlationId", is(correlationId))
                .body(not(containsString("secret")));
    }

    @Test
    void rejectsTokenSignedWithAnotherSecret() {
        assertUnauthorized(token(ServiceJwtTestTokens.WRONG_SECRET, ServiceJwtTestTokens.ISSUER,
                ServiceJwtTestTokens.AUDIENCE, ServiceJwtTestTokens.SUBJECT,
                Set.of(ServiceJwtTestTokens.REQUIRED_GROUP), "service", Instant.now().plusSeconds(300)));
    }

    @Test
    void rejectsExpiredToken() {
        assertUnauthorized(token(ServiceJwtTestTokens.testSecret(), ServiceJwtTestTokens.ISSUER,
                ServiceJwtTestTokens.AUDIENCE, ServiceJwtTestTokens.SUBJECT,
                Set.of(ServiceJwtTestTokens.REQUIRED_GROUP), "service", Instant.now().minusSeconds(5)));
    }

    @Test
    void rejectsIncorrectIssuer() {
        assertUnauthorized(token(ServiceJwtTestTokens.testSecret(), "another-issuer",
                ServiceJwtTestTokens.AUDIENCE, ServiceJwtTestTokens.SUBJECT,
                Set.of(ServiceJwtTestTokens.REQUIRED_GROUP), "service", Instant.now().plusSeconds(300)));
    }

    @Test
    void rejectsIncorrectAudience() {
        assertUnauthorized(token(ServiceJwtTestTokens.testSecret(), ServiceJwtTestTokens.ISSUER,
                "another-audience", ServiceJwtTestTokens.SUBJECT,
                Set.of(ServiceJwtTestTokens.REQUIRED_GROUP), "service", Instant.now().plusSeconds(300)));
    }

    @Test
    void rejectsIncorrectSubject() {
        assertForbidden(token(ServiceJwtTestTokens.testSecret(), ServiceJwtTestTokens.ISSUER,
                ServiceJwtTestTokens.AUDIENCE, "another-service",
                Set.of(ServiceJwtTestTokens.REQUIRED_GROUP), "service", Instant.now().plusSeconds(300)),
                "ACCESS_DENIED");
    }

    @Test
    void rejectsMissingValidationGroup() {
        assertForbidden(token(ServiceJwtTestTokens.testSecret(), ServiceJwtTestTokens.ISSUER,
                ServiceJwtTestTokens.AUDIENCE, ServiceJwtTestTokens.SUBJECT,
                Set.of(), "service", Instant.now().plusSeconds(300)), "ACCESS_DENIED");
    }

    @Test
    void rejectsIncorrectGroup() {
        assertForbidden(token(ServiceJwtTestTokens.testSecret(), ServiceJwtTestTokens.ISSUER,
                ServiceJwtTestTokens.AUDIENCE, ServiceJwtTestTokens.SUBJECT,
                Set.of("notification:other"), "service", Instant.now().plusSeconds(300)), "ACCESS_DENIED");
    }

    @Test
    void rejectsUserTokenExplicitly() {
        assertForbidden(token(ServiceJwtTestTokens.testSecret(), ServiceJwtTestTokens.ISSUER,
                ServiceJwtTestTokens.AUDIENCE, "42",
                Set.of(ServiceJwtTestTokens.REQUIRED_GROUP), "user", Instant.now().plusSeconds(300)),
                "INVALID_TOKEN_USE");
    }

    @Test
    void rejectsTokenWithoutTokenUseExplicitly() {
        assertForbidden(token(ServiceJwtTestTokens.testSecret(), ServiceJwtTestTokens.ISSUER,
                ServiceJwtTestTokens.AUDIENCE, ServiceJwtTestTokens.SUBJECT,
                Set.of(ServiceJwtTestTokens.REQUIRED_GROUP), null, Instant.now().plusSeconds(300)),
                "INVALID_TOKEN_USE");
    }

    @Test
    void preservesCorrelationIdInForbiddenResponse() {
        String correlationId = "forbidden-correlation";
        String token = token(ServiceJwtTestTokens.testSecret(), ServiceJwtTestTokens.ISSUER,
                ServiceJwtTestTokens.AUDIENCE, ServiceJwtTestTokens.SUBJECT,
                Set.of(), "service", Instant.now().plusSeconds(300));

        authorized(token)
                .header(CorrelationIdFilter.HEADER_NAME, correlationId)
                .body(validRequest())
                .when().post(PATH)
                .then()
                .statusCode(403)
                .header(CorrelationIdFilter.HEADER_NAME, equalTo(correlationId))
                .body("correlationId", is(correlationId));
    }

    @Test
    void keepsSystemInfoAndHealthPublic() {
        given().when().get("/api/v1/system/info").then()
                .statusCode(200)
                .header(CorrelationIdFilter.HEADER_NAME, not(blankOrNullString()));
        for (String path : new String[]{"/q/health", "/q/health/live", "/q/health/ready"}) {
            given().when().get(path).then()
                    .statusCode(200)
                    .header(CorrelationIdFilter.HEADER_NAME, not(blankOrNullString()));
        }
    }

    @Test
    void documentsBearerSchemeSecurityAndSecurityResponses() {
        given()
                .queryParam("format", "json")
                .when().get("/q/openapi")
                .then()
                .statusCode(200)
                .body("components.securitySchemes.serviceBearer.type", is("http"))
                .body("components.securitySchemes.serviceBearer.scheme", is("bearer"))
                .body("paths.'" + PATH + "'.post.security[0].keySet()", hasItem("serviceBearer"))
                .body("paths.'" + PATH + "'.post.responses.keySet()", org.hamcrest.Matchers.hasItems("401", "403"));
    }

    private void assertUnauthorized(String token) {
        Response response = authorized(token)
                .body(validRequest())
                .when().post(PATH);

        assertEquals(401, response.statusCode());
        assertEquals(ContentType.JSON.toString(), response.contentType());
        assertEquals("AUTHENTICATION_REQUIRED", response.jsonPath().getString("code"));
        assertCorrelated(response);
        assertFalse(response.asString().contains("signature"));
    }

    private void assertForbidden(String token, String expectedCode) {
        Response response = authorized(token)
                .body(validRequest())
                .when().post(PATH);

        assertEquals(403, response.statusCode());
        assertEquals(ContentType.JSON.toString(), response.contentType());
        assertEquals(expectedCode, response.jsonPath().getString("code"));
        assertCorrelated(response);
    }

    private void assertCorrelated(Response response) {
        String correlationId = response.header(CorrelationIdFilter.HEADER_NAME);
        assertNotNull(correlationId);
        assertFalse(correlationId.isBlank());
        assertEquals(correlationId, response.jsonPath().getString("correlationId"));
    }

    private RequestSpecification authorized(String token) {
        return given().auth().oauth2(token).contentType(ContentType.JSON);
    }

    private String token(
            String secret,
            String issuer,
            String audience,
            String subject,
            Set<String> groups,
            String tokenUse,
            Instant expiresAt) {
        return ServiceJwtTestTokens.token(secret, issuer, audience, subject, groups, tokenUse, expiresAt);
    }

    private Map<String, Object> validRequest() {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("organizationId", 10);
        request.put("appointmentId", 25);
        request.put("channel", "EMAIL");
        request.put("templateCode", "APPOINTMENT_CONFIRMATION");
        request.put("recipient", "customer@example.com");
        request.put("locale", "en-US");
        request.put("variables", Map.of("customerName", "Example"));
        return request;
    }
}

package com.flakomencia.agendaflow.notification.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.flakomencia.agendaflow.notification.infrastructure.http.CorrelationIdFilter;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
class NotificationContractResourceTest {

    private static final String PATH = "/api/v1/notification-requests/validate";

    @Test
    void validatesImmediateRequestWithoutSendingIt() {
        Map<String, Object> request = validRequest();
        request.remove("scheduledAt");
        request.put("recipient", "Customer@EXAMPLE.COM");

        authorized()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post(PATH)
                .then()
                .statusCode(200)
                .body("valid", is(true))
                .body("channel", is("EMAIL"))
                .body("templateCode", is("APPOINTMENT_CONFIRMATION"))
                .body("normalizedRecipient", is("Customer@example.com"))
                .body("scheduled", is(false));
    }

    @Test
    void validatesScheduledRequest() {
        Map<String, Object> request = validRequest();
        request.put("scheduledAt", Instant.now().plus(1, ChronoUnit.DAYS).toString());

        authorized()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post(PATH)
                .then()
                .statusCode(200)
                .body("valid", is(true))
                .body("scheduled", is(true));
    }

    @Test
    void preservesReceivedCorrelationIdInHeaderAndBody() {
        String correlationId = "notification-contract-correlation";

        authorized()
                .contentType(ContentType.JSON)
                .header(CorrelationIdFilter.HEADER_NAME, correlationId)
                .body(validRequest())
                .when().post(PATH)
                .then()
                .statusCode(200)
                .header(CorrelationIdFilter.HEADER_NAME, equalTo(correlationId))
                .body("correlationId", is(correlationId));
    }

    @Test
    void generatesCorrelationIdAndReturnsTheSameValueInBody() {
        Response response = authorized()
                .contentType(ContentType.JSON)
                .body(validRequest())
                .when().post(PATH);

        String correlationId = response.header(CorrelationIdFilter.HEADER_NAME);
        response.then()
                .statusCode(200)
                .header(CorrelationIdFilter.HEADER_NAME,
                        matchesPattern("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"))
                .body("correlationId", is(correlationId));
    }

    @Test
    void rejectsInvalidOrganizationId() {
        Map<String, Object> request = validRequest();
        request.put("organizationId", 0);

        givenInvalid(request)
                .body("code", is("VALIDATION_ERROR"))
                .body("fieldErrors.organizationId", not(blankOrNullString()));
    }

    @Test
    void rejectsInvalidEmail() {
        Map<String, Object> request = validRequest();
        request.put("recipient", "not-an-email");

        givenInvalid(request)
                .body("code", is("VALIDATION_ERROR"))
                .body("fieldErrors.recipient", containsString("valid email"));
    }

    @Test
    void rejectsUnsupportedChannel() {
        Map<String, Object> request = validRequest();
        request.put("channel", "SMS");

        givenInvalid(request)
                .body("code", is("UNSUPPORTED_CHANNEL"))
                .body("fieldErrors.channel", is("channel must be EMAIL"));
    }

    @Test
    void rejectsInvalidTemplateCode() {
        Map<String, Object> request = validRequest();
        request.put("templateCode", "appointment confirmation");

        givenInvalid(request)
                .body("code", is("VALIDATION_ERROR"))
                .body("fieldErrors.templateCode", containsString("uppercase"));
    }

    @Test
    void rejectsTooManyVariables() {
        Map<String, String> variables = new LinkedHashMap<>();
        for (int index = 0; index < 21; index++) {
            variables.put("variable" + index, "value");
        }
        Map<String, Object> request = validRequest();
        request.put("variables", variables);

        givenInvalid(request)
                .body("code", is("VALIDATION_ERROR"))
                .body("fieldErrors.variables", containsString("20"));
    }

    @Test
    void rejectsVariableKeyThatIsTooLong() {
        Map<String, Object> request = validRequest();
        request.put("variables", Map.of("k".repeat(65), "value"));

        givenInvalid(request)
                .body("code", is("VALIDATION_ERROR"))
                .body("fieldErrors.variables", containsString("64"));
    }

    @Test
    void rejectsVariableValueThatIsTooLong() {
        Map<String, Object> request = validRequest();
        request.put("variables", Map.of("customerName", "v".repeat(501)));

        givenInvalid(request)
                .body("code", is("VALIDATION_ERROR"))
                .body("fieldErrors.variables", containsString("500"));
    }

    @Test
    void rejectsPastSchedule() {
        Map<String, Object> request = validRequest();
        request.put("scheduledAt", Instant.now().minus(1, ChronoUnit.DAYS).toString());

        givenInvalid(request)
                .body("code", is("SCHEDULED_AT_IN_PAST"))
                .body("fieldErrors.scheduledAt", containsString("future UTC instant"));
    }

    @Test
    void rejectsMalformedJson() {
        authorized()
                .contentType(ContentType.JSON)
                .body("{\"organizationId\":10,")
                .when().post(PATH)
                .then()
                .statusCode(400)
                .body("code", is("MALFORMED_JSON"))
                .body("correlationId", not(blankOrNullString()));
    }

    @Test
    void rejectsUnknownFieldsAndNestedVariableObjects() {
        Map<String, Object> withSecret = validRequest();
        withSecret.put("secret", "must-not-be-accepted");
        givenInvalid(withSecret).body("code", is("MALFORMED_JSON"));

        Map<String, Object> withNestedVariable = validRequest();
        withNestedVariable.put("variables", Map.of("customer", Map.of("name", "Nested")));
        givenInvalid(withNestedVariable).body("code", is("MALFORMED_JSON"));
    }

    @Test
    void responseContainsNoFalseDeliveryIdentifiersOrStatuses() {
        authorized()
                .contentType(ContentType.JSON)
                .body(validRequest())
                .when().post(PATH)
                .then()
                .statusCode(200)
                .body("$", not(hasKey("requestId")))
                .body("$", not(hasKey("deliveryId")))
                .body("$", not(hasKey("providerMessageId")))
                .body("$", not(hasKey("status")));
    }

    @Test
    void documentsValidationAndDurableIntakeContracts() {
        authorized()
                .queryParam("format", "json")
                .when().get("/q/openapi")
                .then()
                .statusCode(200)
                .body("tags.name", hasItem("Notification Contract"))
                .body("paths.keySet()", hasItem(PATH))
                .body("paths.keySet()", hasItem("/api/v1/notification-requests"))
                .body(containsString("Durably accept an appointment notification event"))
                .body(containsString("does not send, store, enqueue or acknowledge delivery"));
    }

    private io.restassured.response.ValidatableResponse givenInvalid(Map<String, Object> request) {
        return authorized()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post(PATH)
                .then()
                .statusCode(400)
                .body("correlationId", not(blankOrNullString()));
    }

    private io.restassured.specification.RequestSpecification authorized() {
        return given().auth().oauth2(ServiceJwtTestTokens.valid());
    }

    private Map<String, Object> validRequest() {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("organizationId", 10);
        request.put("appointmentId", 25);
        request.put("channel", "EMAIL");
        request.put("templateCode", "APPOINTMENT_CONFIRMATION");
        request.put("recipient", "customer@example.com");
        request.put("locale", "en-US");
        request.put("scheduledAt", Instant.now().plus(1, ChronoUnit.HOURS).toString());
        request.put("variables", Map.of(
                "customerName", "Example",
                "appointmentTime", "10:00 AM"));
        return request;
    }
}

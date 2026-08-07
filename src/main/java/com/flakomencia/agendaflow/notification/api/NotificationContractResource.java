package com.flakomencia.agendaflow.notification.api;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.flakomencia.agendaflow.notification.api.model.ApiErrorResponse;
import com.flakomencia.agendaflow.notification.api.model.NotificationRequestValidationRequest;
import com.flakomencia.agendaflow.notification.api.model.NotificationRequestValidationResponse;
import com.flakomencia.agendaflow.notification.application.NotificationContractValidationService;
import com.flakomencia.agendaflow.notification.infrastructure.http.CorrelationIdContext;
import com.flakomencia.agendaflow.notification.infrastructure.http.CorrelationIdFilter;
import com.flakomencia.agendaflow.notification.infrastructure.security.ServiceTokenClaimsFilter;
import com.flakomencia.agendaflow.notification.infrastructure.security.ServiceTokenRequired;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

@Path("/api/v1/notification-requests")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Tag(name = "Notification Contract")
public class NotificationContractResource {

    @Inject
    NotificationContractValidationService validationService;

    @Inject
    CorrelationIdContext correlationIdContext;

    @POST
    @Path("/validate")
    @ServiceTokenRequired
    @RolesAllowed(ServiceTokenClaimsFilter.REQUIRED_PERMISSION)
    @SecurityRequirement(name = "serviceBearer")
    @Operation(
            summary = "Validate a notification request contract",
            description = "Validates and safely normalizes a candidate request. "
                    + "It does not send, store, enqueue or acknowledge delivery of a notification.")
    @RequestBody(
            required = true,
            description = "Candidate notification request",
            content = @Content(
                    mediaType = APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationRequestValidationRequest.class),
                    examples = @ExampleObject(
                            name = "Valid scheduled email",
                            value = "{\"organizationId\":10,\"appointmentId\":25,\"channel\":\"EMAIL\","
                                    + "\"templateCode\":\"APPOINTMENT_CONFIRMATION\","
                                    + "\"recipient\":\"customer@example.com\",\"locale\":\"en-US\","
                                    + "\"scheduledAt\":\"2026-08-04T15:00:00Z\","
                                    + "\"variables\":{\"customerName\":\"Example\","
                                    + "\"appointmentTime\":\"10:00 AM\"}}")))
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Contract is valid; no notification was sent or stored",
                    headers = @Header(
                            name = CorrelationIdFilter.HEADER_NAME,
                            description = "Request correlation identifier",
                            schema = @Schema(implementation = String.class)),
                    content = @Content(
                            mediaType = APPLICATION_JSON,
                            schema = @Schema(implementation = NotificationRequestValidationResponse.class))),
            @APIResponse(
                    responseCode = "400",
                    description = "Malformed or invalid notification request contract",
                    content = @Content(
                            mediaType = APPLICATION_JSON,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @APIResponse(
                    responseCode = "401",
                    description = "Missing, invalid, expired, or incorrectly signed service token",
                    content = @Content(
                            mediaType = APPLICATION_JSON,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @APIResponse(
                    responseCode = "403",
                    description = "Token is not a service token or lacks notification:validate",
                    content = @Content(
                            mediaType = APPLICATION_JSON,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public NotificationRequestValidationResponse validate(
            @NotNull(message = "request body is required")
            @Valid NotificationRequestValidationRequest request) {
        return validationService.validate(request, correlationIdContext.get());
    }
}

package com.flakomencia.agendaflow.notification.api;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.flakomencia.agendaflow.notification.api.model.SystemInfoResponse;
import com.flakomencia.agendaflow.notification.infrastructure.http.CorrelationIdFilter;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("/api/v1/system/info")
@Produces(APPLICATION_JSON)
@Tag(name = "System")
public class SystemInfoResource {

    @GET
    @Operation(
            summary = "Get technical service information",
            description = "Returns non-sensitive service identity and technical phase information.")
    @APIResponse(
            responseCode = "200",
            description = "Technical service information",
            headers = @Header(
                    name = CorrelationIdFilter.HEADER_NAME,
                    description = "Request correlation identifier",
                    schema = @Schema(implementation = String.class)),
            content = @Content(
                    mediaType = APPLICATION_JSON,
                    schema = @Schema(implementation = SystemInfoResponse.class)))
    public SystemInfoResponse info() {
        return new SystemInfoResponse(
                "agendaflow-notification-service",
                "UP",
                "technical-foundation",
                "0.0.1");
    }
}

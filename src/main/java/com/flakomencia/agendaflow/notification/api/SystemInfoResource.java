package com.flakomencia.agendaflow.notification.api;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("/api/v1/system/info")
@Produces(APPLICATION_JSON)
@Tag(name = "System", description = "Technical bootstrap endpoints")
public class SystemInfoResource {

    @GET
    @Operation(summary = "Get bootstrap service information")
    public Map<String, String> info() {
        return Map.of(
                "service", "agendaflow-notification-service",
                "status", "UP",
                "phase", "bootstrap");
    }
}

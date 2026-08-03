package com.flakomencia.agendaflow.notification.api.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "SystemInfo", description = "Non-sensitive technical service information")
public record SystemInfoResponse(
        String service,
        String status,
        String phase,
        String version) {
}

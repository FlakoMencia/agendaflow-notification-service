package com.flakomencia.agendaflow.notification.api;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

@Path("/test")
@Produces(APPLICATION_JSON)
public class ValidationTestResource {

    @GET
    @Path("/validation")
    public Map<String, String> validate(@QueryParam("value") @NotBlank String value) {
        return Map.of("value", value);
    }

    @GET
    @Path("/failure")
    public void failSafely() {
        throw new IllegalStateException("Internal implementation detail must not be returned");
    }
}

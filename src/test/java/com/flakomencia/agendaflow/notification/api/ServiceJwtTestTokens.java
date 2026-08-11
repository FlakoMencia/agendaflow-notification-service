package com.flakomencia.agendaflow.notification.api;

import java.time.Instant;
import java.util.Set;

import org.eclipse.microprofile.config.ConfigProvider;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;

final class ServiceJwtTestTokens {
    static final String WRONG_SECRET = "wrong-test-only-service-jwt-secret-32-bytes-minimum";
    static final String ISSUER = "agendaflow-api";
    static final String AUDIENCE = "agendaflow-notification-service";
    static final String SUBJECT = "agendaflow-api";
    static final String REQUIRED_GROUP = "notification:validate";
    static final String SUBMIT_GROUP = "notification:submit";

    private ServiceJwtTestTokens() {
    }

    static String valid() {
        return token(testSecret(), ISSUER, AUDIENCE, SUBJECT, Set.of(REQUIRED_GROUP), "service",
                Instant.now().plusSeconds(300));
    }

    static String submit() {
        return token(testSecret(), ISSUER, AUDIENCE, SUBJECT, Set.of(SUBMIT_GROUP), "service",
                Instant.now().plusSeconds(300));
    }

    static String testSecret() {
        return ConfigProvider.getConfig().getValue("service.jwt.secret", String.class);
    }

    static String token(
            String secret,
            String issuer,
            String audience,
            String subject,
            Set<String> groups,
            String tokenUse,
            Instant expiresAt) {
        JwtClaimsBuilder claims = Jwt.claims()
                .issuer(issuer)
                .audience(audience)
                .subject(subject)
                .issuedAt(Instant.now().minusSeconds(5))
                .expiresAt(expiresAt)
                .groups(groups)
                .claim("jti", "test-" + System.nanoTime());
        if (tokenUse != null) {
            claims.claim("token_use", tokenUse);
        }
        return claims.jws().keyId("test-service-key").signWithSecret(secret);
    }
}

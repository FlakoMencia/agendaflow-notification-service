# Autenticación servicio-a-servicio

## Frontera protegida

Solo `POST /api/v1/notification-requests/validate` requiere autenticación. System info, OpenAPI y
los endpoints estándar de SmallRye Health permanecen públicos. Swagger UI conserva el comportamiento
de desarrollo configurado por Quarkus.

## Verificación

SmallRye JWT valida localmente la firma HS256, `iss`, `aud`, `exp` y la presencia de `sub`. Un
filtro ligado únicamente al endpoint valida explícitamente `token_use = service` y
`sub = agendaflow-api`. `@RolesAllowed("notification:validate")` aplica RBAC sobre los groups del
JWT.

La clave llega exclusivamente mediante `SERVICE_JWT_SECRET`, debe tener al menos 32 bytes UTF-8 y
no puede ser un placeholder fuera del perfil de test. No se solicita ni emite un token por HTTP.

## Fallos seguros

- Credencial ausente, firma inválida, token expirado, issuer o audience incorrectos: 401
  `AUTHENTICATION_REQUIRED`.
- `token_use` ausente o distinto de `service`: 403 `INVALID_TOKEN_USE`.
- Subject incorrecto o group ausente/incorrecto: 403 `ACCESS_DENIED`.

Los mappers reutilizan el error uniforme. Header y cuerpo comparten correlation ID. No se devuelven
detalles criptográficos, nombres de clases, stack traces o secretos.

Los logs contienen únicamente correlation ID, resultado, permiso y subject/issuer cuando el token
ya fue autenticado. El Bearer token nunca se registra.

## Estrategia de pruebas

El perfil de test declara un secret HMAC exclusivo y suficientemente largo. Un JWK `oct` derivado
de ese mismo valor desactiva la clave RSA automática de Quarkus. `smallrye-jwt-build` genera tokens
efímeros con expiración cercana y un `jti` distinto por prueba.

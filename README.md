# AgendaFlow Notification Service

Microservicio Quarkus que procesará notificaciones de AgendaFlow en fases posteriores. Actualmente
valida contratos de solicitud y protege esa operación mediante autenticación servicio-a-servicio.

## Estado

**Fase 3 — Autenticación servicio-a-servicio.** `POST /api/v1/notification-requests/validate`
requiere un JWT HS256 emitido por `agendaflow-api`, dirigido a este servicio y autorizado con el
grupo `notification:validate`.

La validación no envía, almacena, encola ni confirma la entrega de notificaciones.

## Stack

| Componente | Versión |
| --- | --- |
| Java | 21 |
| Maven Wrapper | 3.9.16 |
| Quarkus | 3.33.3 LTS |
| REST/JSON | Quarkus REST con Jackson |
| Validación | Hibernate Validator |
| Seguridad | SmallRye JWT, HS256 y RBAC |
| API/health | SmallRye OpenAPI y SmallRye Health |
| Pruebas | Quarkus Test, JUnit 5 y REST Assured |

No hay extensiones de persistencia, mensajería, scheduler o proveedores de correo.

## Requisitos y comandos

Se requiere JDK 21. El servicio usa el puerto `8081` en desarrollo y un puerto automático en
pruebas.

```cmd
mvnw.cmd --version
mvnw.cmd quarkus:dev
mvnw.cmd clean test
mvnw.cmd clean verify
```

## Configuración JWT

La configuración local/productiva depende de variables externas:

```text
SERVICE_JWT_SECRET=<secreto aleatorio de al menos 32 bytes UTF-8>
SERVICE_JWT_ISSUER=agendaflow-api
SERVICE_JWT_AUDIENCE=agendaflow-notification-service
```

`SERVICE_JWT_SECRET` no tiene default productivo. El proceso falla al iniciar si el valor es corto o
parece un placeholder. `.env.example` contiene solo una indicación no funcional y no existe `.env`
versionado.

Un token aceptado debe cumplir simultáneamente:

- firma HS256 válida;
- `iss = agendaflow-api`;
- audiencia `agendaflow-notification-service`;
- `sub = agendaflow-api`;
- `token_use = service`;
- grupo `notification:validate`;
- claims `sub` y `exp` presentes y token no expirado.

Los tokens de usuario se rechazan explícitamente. El servicio no emite tokens ni implementa login.

## Rutas

| Ruta | Acceso | Propósito |
| --- | --- | --- |
| `POST /api/v1/notification-requests/validate` | JWT de servicio | Validar y normalizar un contrato |
| `GET /api/v1/system/info` | Público | Información técnica no sensible |
| `/q/openapi` | Público | Documento OpenAPI |
| `/q/swagger-ui` | Desarrollo | Swagger UI |
| `/q/health`, `/q/health/live`, `/q/health/ready` | Público | Salud local del proceso |

Una validación correcta devuelve `200 OK`, nunca `202 Accepted`, y no incluye identificadores de
request, entrega o proveedor. El contrato completo está en
[notification-request-contract.md](docs/api/notification-request-contract.md).

## Errores, correlación y logs

Los fallos de autenticación devuelven 401 `AUTHENTICATION_REQUIRED`. Los tokens autenticados que no
son de servicio, tienen subject incorrecto o carecen de autorización devuelven 403
`INVALID_TOKEN_USE` o `ACCESS_DENIED`.

Todos los errores conservan el formato uniforme y el mismo `X-Correlation-ID` en header y cuerpo.
Las respuestas no exponen firmas, algoritmos internos, excepciones, stack traces ni secretos. Los
logs de seguridad se limitan a correlation ID, subject/issuer cuando son confiables, resultado y
permiso requerido; nunca registran el token.

## Pruebas JWT

El perfil de test usa un secret claramente exclusivo de pruebas y un JWK HMAC equivalente para
evitar la autogeneración RSA de Quarkus. Cada test genera un JWT efímero durante su ejecución con
`smallrye-jwt-build`; no existe un token estático de larga duración en el repositorio.

La suite cubre firma, expiración, issuer, audience, subject, `token_use`, grupo RBAC, 401/403,
correlation ID, OpenAPI, rutas públicas y el contrato funcional heredado.

## Documentación

- [Autenticación servicio-a-servicio](docs/architecture/service-authentication.md)
- [Límites del servicio](docs/architecture/service-boundaries.md)
- [Contrato de validación](docs/api/notification-request-contract.md)
- [Formato uniforme de errores](docs/api/error-format.md)
- [Correlation ID](docs/api/correlation-id.md)

## Configuración futura sin implementación

`NOTIFICATION_PROVIDER`, `EMAIL_FROM` y `SPRING_API_BASE_URL` siguen siendo placeholders
documentales. No inicializan proveedor, sender ni cliente HTTP hacia Spring Boot.

## Repositorios relacionados

- [agendaflow-api](../agendaflow-api)
- [agendaflow-web](../agendaflow-web)

## No implementado

- Intake real, envío de email, plantillas, attachments o estado de entrega.
- Kafka, RabbitMQ, Azure Service Bus, consumers, polling, schedulers o retries.
- PostgreSQL, JDBC, Hibernate, Panache, Flyway, entidades o repositorios.
- Llamadas a Spring Boot, login, usuarios, refresh token o emisión HTTP de tokens.
- Docker, nube o CI/CD.

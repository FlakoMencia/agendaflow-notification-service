# API documentation

Esta carpeta registra los contratos HTTP existentes:

- [Notification request validation contract](notification-request-contract.md)
- [Uniform error format](error-format.md)
- [Correlation ID contract](correlation-id.md)

`POST /api/v1/notification-requests/validate` requiere Bearer JWT de servicio con el grupo
`notification:validate`. `GET /api/v1/system/info`, OpenAPI y SmallRye Health permanecen públicos.
No existe un endpoint real de intake o envío.

El renderer introducido en Fase 4 es exclusivamente interno: no existe `/preview` ni una ruta de
entrega.

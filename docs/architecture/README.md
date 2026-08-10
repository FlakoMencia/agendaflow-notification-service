# Architecture documentation

Esta carpeta describe los límites del microservicio, su base técnica y futuras decisiones de
integración.

- [Service boundaries](service-boundaries.md)
- [Service-to-service authentication](service-authentication.md)
- [Notification preparation pipeline](notification-pipeline.md)

La Fase 4 agrega modelo interno, mapping, rendering de texto plano y un delivery port sin
implementación. Conserva JWT HS256 y RBAC, y no introduce intake real, proveedores, persistencia,
mensajería ni comunicación saliente.

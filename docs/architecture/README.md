# Architecture documentation

Esta carpeta describe los límites del microservicio, su base técnica y futuras decisiones de
integración.

- [Service boundaries](service-boundaries.md)
- [Service-to-service authentication](service-authentication.md)

La Fase 3 protege el validador de contratos mediante JWT HS256 y RBAC. No introduce intake real,
proveedores, persistencia, mensajería ni comunicación saliente.

# Architecture documentation

This folder describes the microservice boundary, its internal technical foundation, and future
integration decisions. See [service boundaries](service-boundaries.md) for ownership rules.

Phase 2 adds a pure notification-request contract validator to the cross-cutting HTTP foundation.
It defines request shape and validation boundaries but no intake transport, provider, persistence or
messaging architecture.

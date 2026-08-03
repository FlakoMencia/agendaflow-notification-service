# Architecture documentation

This folder describes the microservice boundary, its internal technical foundation, and future
integration decisions. See [service boundaries](service-boundaries.md) for ownership rules.

Phase 1 introduces only cross-cutting HTTP concerns: correlation, safe errors, typed future-provider
configuration, OpenAPI and logging. It defines no provider, persistence or messaging architecture.

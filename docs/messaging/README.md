# Messaging documentation

Event-driven integration is planned but not implemented. No topic, queue, broker, consumer,
delivery guarantee, retry topology or final event schema has been selected.

The choice among Kafka, RabbitMQ, Azure Service Bus or another mechanism is deliberately pending.
It will be based on confirmed delivery, ordering, retry, dead-letter and operational requirements
instead of assumptions made during the technical foundation phase.

This repository currently contains no messaging extension or runtime connection.

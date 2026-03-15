# System Architecture

The platform follows an event-driven microservices architecture.

Worker requests are handled by the Backend API which publishes events to Kafka.

Different services subscribe to these events:

- Risk Engine calculates dynamic weekly premiums using AI.
- Trigger Engine monitors environmental disruptions.
- Fraud Detection validates worker activity and location.
- Claim Service processes claims automatically.
- Payout Service sends compensation through a simulated payment gateway.

Redis is used for real-time caching and fast data access.
